(ns features.customizer.panel.backend.resolvers
  (:require
    [clojure.java.io                       :as io]
    [com.wsscode.pathom3.connect.operation :as pco]
    [zero.backend.state.postgres           :as postgres]
    [cheshire.core                         :as json]
   
    [features.customizer.panel.backend.data :as data]
    [features.customizer.panel.backend.db :as db]))

;; -----------------------------------------------------------------------------
;; ---- SQL ----
;; SQL functions are now in db.clj
;; ---- SQL ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Menu ----
 

(def menu (atom {}))

;; ---- Breadcrumb Menu ----


(defn get-populars-fn [env]
  (let [params (-> env pco/params)]
    (try
      (db/get-popular-parts)
      (catch Exception e
        (println "Error getting popular parts:" e)
        nil))))

(pco/defresolver get-populars-r [env _]
  {::pco/output [{:customizer.menu/get-populars []}]}
  {:customizer.menu/get-populars (get-populars-fn env)})

;; ---- Menu ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- List Looks ----

(defn list-looks-fn [env]
  (let [params (-> env pco/params)]
    (try
      (db/list-looks)
      (catch Exception e
        (println "Error listing looks:" e)
        nil))))

;; (keys (first (list-looks-fn {:search ""})))

(pco/defresolver list-looks-r [env _]
  {::pco/output [{:customizer.looks/list! [:id :name :basecolor :color_family :tags :thumbnail :texture :price_group_key]}]}
  {:customizer.looks/list! (list-looks-fn env)})

;; ---- List Looks ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Looks Filters ----

(defn looks-filters-fn [env]
  (let [params (-> env pco/params)]
    (try
      (db/list-looks-filters (:color_family params)
                             (:price_group_key params)
                             (vec (remove nil? [(:coat params) (:surface params)])))
      (catch Exception e
        (println "Error listing looks filters:" e)
        nil))))

(pco/defresolver looks-filters-r [env _]
  {::pco/output [{:customizer.looks/filters! [:id :name :basecolor :color_family :tags :price_group_key]}]}
  {:customizer.looks/filters! (looks-filters-fn env)})

;; ---- Looks Filters ----
;; -----------------------------------------------------------------------------
    
;; -----------------------------------------------------------------------------
;; ---- Get Categories ----

(defn fetch-categories [category-id]
  (if (nil? category-id)
    (postgres/execute-sql "SELECT id, name, description, category_id, picture_url FROM categories WHERE category_id IS NULL ORDER BY order_position ASC, name ASC")
    (postgres/execute-sql "SELECT id, name, description, category_id, picture_url FROM categories WHERE category_id = $1 ORDER BY order_position ASC, name ASC" {:params [category-id]})))

(defn fetch-packages [category-id]
  (if (nil? category-id)
    []
    (postgres/execute-sql "SELECT id, name, description, picture_url, prefix, model_url, popular, category_id, form_id FROM packages WHERE category_id = $1 ORDER BY order_position ASC, name ASC" {:params [category-id]})))

(defn fetch-parts [package-id]
  (postgres/execute-sql "SELECT id, name, description, picture_url, mesh_id, popular, package_id, form_id FROM parts WHERE package_id = $1 ORDER BY order_position ASC, name ASC" {:params [package-id]}))

(defn build-part-map [part]
  (let [part-data (assoc part :type "part")]
    [(:id part) part-data]))

(defn build-category-map [category get-categories-fn]
  (let [children (get-categories-fn (:id category))
        category-data (assoc category :type "category" :children children)]
    [(:id category) category-data]))

(defn build-package-map [package]
  (let [parts (fetch-parts (:id package))
        parts-map (into {} (map build-part-map parts))
        package-data (assoc package :type "package" :children parts-map)]
    [(:id package) package-data]))

(defn get_categories [category-id]
  (try
    (let [categories (fetch-categories category-id)
          packages (fetch-packages category-id)]
      (into {} 
            (concat
             (map #(build-category-map % get_categories) categories)
             (map build-package-map packages))))
    (catch Exception e
      (println "Error getting categories:" e)
      {})))

(defn get_categories_json [category-id]
  (json/generate-string (get_categories category-id)))

(defn refresh-menu! []
  (let [tree-data (get_categories nil)
        category-count (count tree-data)]
    (reset! menu tree-data)
    (println "Menu refreshed successfully! Loaded" category-count "root categories with packages and parts.")))

(defn get-menu-fn [env]
  @menu)

(pco/defresolver get-menu-r [env _]
  {::pco/output [{:customizer.menu/fetch! [:id]}]}
  {:customizer.menu/fetch! (get-menu-fn env)})

;; Initialize menu on startup
(refresh-menu!)

;; Add function to manually refresh menu if needed
(defn refresh-menu-with-order! []
  (println "Refreshing customizer menu with new order...")
  (refresh-menu!))

;; ---- Get Categories ----
;; -----------------------------------------------------------------------------


;; ---- TEMP ----

(defn get-all-workspaces-fn [env]
  (let [params (-> env pco/params)]
    (try
      (postgres/execute-sql "SELECT w.id, w.name, bi.facility_state, bi.facility_city FROM workspaces w LEFT JOIN business_info bi ON w.id = bi.workspace_id")
      (catch Exception e
        (println "Error getting all workspaces:" e)))))

(pco/defresolver temp-get-all-workspaces-r [env _]
  {::pco/output [{:temp/get-all-workspaces [:id :name :facility_state :facility_city]}]}
  {:temp/get-all-workspaces (get-all-workspaces-fn env)})

;; ---- CLOSEST WORKSPACE ----
;;
;; ZIP CODES TABLE STRUCTURE:
;; - Row 1 (id=1): Contains ALL 33k+ US zip codes with full data
;;   Structure: {schema: [...], data: [[zip, population, lat, lng, workspace-id, status], ...]}
;;   
;; - Row 2 (id=2): Contains FILTERED zip codes (automatically maintained by trigger)
;;   Only includes zip codes where status="r" (reserved) and workspace-id is assigned
;;   Same structure as row 1 but much smaller dataset (typically 3-20 entries)
;;   
;; The database trigger updates row 2 whenever row 1 is modified, ensuring
;; row 2 always contains the current set of reserved/serviceable zip codes.

(defn deg-to-rad [degrees]
  "Convert degrees to radians"
  (* degrees (/ Math/PI 180)))

(defn calculate-haversine-distance 
  "Calculate distance between two points using Haversine formula. Returns distance in miles."
  [[lat1 lng1] [lat2 lng2]]
  (let [earth-radius-miles 3959
        dlat (deg-to-rad (- lat2 lat1))
        dlng (deg-to-rad (- lng2 lng1))
        lat1-rad (deg-to-rad lat1)
        lat2-rad (deg-to-rad lat2)
        a (+ (* (Math/sin (/ dlat 2)) (Math/sin (/ dlat 2)))
             (* (Math/cos lat1-rad) (Math/cos lat2-rad) 
                (Math/sin (/ dlng 2)) (Math/sin (/ dlng 2))))
        c (* 2 (Math/atan2 (Math/sqrt a) (Math/sqrt (- 1 a))))]
    (* earth-radius-miles c)))

(defn parse-zip-codes-jsonb 
  "Parse the JSONB zip_codes column - now returns the full structure with object format"
  [zip-codes-jsonb]
  (try
    (let [parsed (cond
                   (string? zip-codes-jsonb) (json/parse-string zip-codes-jsonb true)
                   (nil? zip-codes-jsonb) {}
                   :else zip-codes-jsonb)]
      parsed)
    (catch Exception e
      (println "Error parsing zip codes JSONB:" (.getMessage e))
      {})))

(defn find-closest-workspace-fn [env zip-code]
  (try
    (println "🔧 Finding closest workspace for zip:" zip-code)
    ;; BACKEND VALIDATION: Check if zip code exists in row 1 (complete database)
    ;; Get reserved workspaces from row 2 (filtered zip codes in object format)  
    (let [;; First check if zip code exists in our database (row 1) - O(1) lookup
          all-zip-record (postgres/execute-sql "SELECT zip_codes->'data'->$1 as zip_data FROM zip_codes WHERE id = 1" 
                                               {:params [zip-code]})
          input-zip-data (when-let [data (-> all-zip-record first :zip_data)]
                          (cond
                            (string? data) (json/parse-string data)
                            (nil? data) nil
                            :else data))
          _ (println "🔧 Backend validation - Input zip" zip-code "exists in database:" (boolean input-zip-data) 
                    "- coords:" (when input-zip-data [(nth input-zip-data 1) (nth input-zip-data 2)]))]
      
      ;; VALIDATION: If zip code doesn't exist in row 1, return error immediately
      (if (nil? input-zip-data)
        (do
          (println "🔧 VALIDATION FAILED: Zip code" zip-code "not found in database")
          {:error-type :zip-code-not-found
           :message (str "Zip code " zip-code " not found in our database")
           :available-samples ["10001" "90210" "60601" "77001" "30301"]})
        
        ;; ZIP CODE EXISTS: Continue with finding nearest reserved workspace
        (let [;; Get reserved workspace zip codes from row 2 (object format)
              reserved-zip-records (postgres/execute-sql "SELECT zip_codes FROM zip_codes WHERE id = 2")
              reserved-data (-> reserved-zip-records first :zip_codes parse-zip-codes-jsonb)
              reserved-zip-codes (:data reserved-data)
              _ (println "🔧 Found" (count reserved-zip-codes) "reserved zip codes")
              
              ;; Transform object entries back to vectors for distance calculation
              workspace-zip-codes (map (fn [[zip-code-key values]]
                                         (concat [zip-code-key] values))
                                       reserved-zip-codes)
              _ (println "🔧 Reserved workspace zip codes:")
              _ (doseq [[zip-key values] reserved-zip-codes]
                  (println "🔧   Zip:" zip-key 
                          "| Lat:" (nth values 1) 
                          "| Lng:" (nth values 2) 
                          "| Workspace:" (nth values 3)))]
          
          ;; Calculate distances to all reserved workspaces
          (if (seq workspace-zip-codes)
            (let [;; Coordinates are now at indices 1 (lat) and 2 (lng) in the new format
                  input-coords [(nth input-zip-data 1) (nth input-zip-data 2)]
                  
                  ;; HAVERSINE ALGORITHM: Calculate distances using object format from row 2
                  ;; O(1) lookup for input zip, then only calculate distances to reserved zips
                  distances (map (fn [zip-data]
                                  (let [;; zip-data structure: [zip-code population lat lng workspace-id status]
                                        workspace-coords [(nth zip-data 2) (nth zip-data 3)]
                                        distance (calculate-haversine-distance input-coords workspace-coords)]
                                    {:workspace-id (str (nth zip-data 4))
                                     :distance distance
                                     :zip-code (str (nth zip-data 0))}))
                                workspace-zip-codes)
                  
                  ;; Find the closest one
                  closest (first (sort-by :distance distances))]
              
              closest)
            
            ;; No reserved workspaces found
            {:error-type :no-reserved-areas
             :message "No service areas available at this time"}))))
    
    (catch Exception e
      (println "Error finding closest workspace:" (.getMessage e))
      nil)))

(pco/defresolver find-closest-workspace-r [env {:keys [zip-code]}]
  {::pco/op-name 'customizer/find-closest-workspace
   ::pco/input [:zip-code]
   ::pco/output [:customizer/find-closest-workspace]}
  (try
    (println "🔧 Resolver called with zip-code:" zip-code)
    {:customizer/find-closest-workspace (find-closest-workspace-fn env zip-code)}
    (catch Exception e
      (println "🔧 Error in find-closest-workspace-r:" (.getMessage e))
      (.printStackTrace e)
      {:customizer/find-closest-workspace nil})))

;; ---- CLOSEST WORKSPACE ----

;; ---- TEMP ----

(def resolvers [list-looks-r
                looks-filters-r
                get-menu-r
                get-populars-r
                temp-get-all-workspaces-r
                find-closest-workspace-r])
