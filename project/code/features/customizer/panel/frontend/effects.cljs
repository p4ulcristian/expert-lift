
(ns features.customizer.panel.frontend.effects
  (:require
    [re-frame.core   :as r]))

;; -----------------------------------------------------------------------------
;; ---- Init customizer ----

(defn preload-textures [looks]
  "Preload textures for the first 3 looks to make them instantly available"
  (when (seq looks)
    (doseq [look (take 3 looks)]
      (when-let [texture-maps (get-in look [:texture :maps])]
        (when (and texture-maps (not-empty texture-maps))
          (try
            ;; Create img elements to preload textures in browser cache
            (doseq [[_key texture-url] texture-maps]
              (when (and texture-url 
                         (string? texture-url)
                         (not= texture-url "")
                         (not= texture-url "null"))
                (let [img (js/Image.)]
                  (set! (.-crossOrigin img) "anonymous") ;; Handle CORS
                  (set! (.-src img) texture-url))))
            (catch js/Error e
              (println "Texture preload error for look" (:id look) ":" e))))))))

(defn extract-popular-parts [menu-items]
  "Extract parts and packages marked as popular from menu data"
  (letfn [(collect-popular-items [node]
            (concat
              (when (and (contains? #{"part" "package"} (:type node))
                         (:popular node)) 
                [node])
              (mapcat collect-popular-items (vals (:children node {})))))]
    (let [popular-items (mapcat collect-popular-items (vals menu-items))]
      ;; Sort by order_position if available, then take up to 12 items
      (->> popular-items
           (sort-by #(or (:order_position %) 999))
           (take 12)))))

(defn customizer-init-callback [response target-id]
  
  (let [menu-items   (get-in response [:customizer.menu/fetch!])
        looks        (get-in response [:customizer.looks/list!])
        populars     (get-in response [:customizer.menu/get-populars])
        user-profile (get-in response [:site/user-profile])
        derived-populars (extract-popular-parts menu-items)]
    
    (r/dispatch [:db/assoc-in [:customizer/menu :items] menu-items])
    (r/dispatch [:customizer.looks/init! looks])
    (r/dispatch [:db/assoc-in [:customizer/menu :populars] (if (seq populars) populars derived-populars)])
    (r/dispatch [:db/assoc-in [:user-profile] user-profile])
    
    ;; Open the menu drawer, details drawer, and location drawer on first load
    (r/dispatch [:customizer.menu/open!])
    (r/dispatch [:details.drawer/open!])
    (r/dispatch [:temp/get-all-workspaces])
    (r/dispatch [:customizer.location/load-from-storage])
    (r/dispatch [:db/assoc-in [:customizer/location :state] true])
    
    ;; If target-id exists in URL, restore menu state
    (when target-id
      (r/dispatch [:customizer.menu.restore/from-url! target-id menu-items]))
    
    ;; Preload first few textures for instant availability
    (preload-textures looks)))

(r/reg-event-fx
  :customizer/init!
  (fn [{:keys [db]} [_ & [target-id]]]
    ;; Prevent multiple concurrent initializations and re-init when already loaded
    (when-not (or (get-in db [:customizer :initializing?])
                  (get-in db [:customizer/looks :items]))
      (let [params {:id target-id}]
        {:dispatch-n [[:db/assoc-in [:customizer :initializing?] true]]
         :pathom/request {:query    [`(:customizer.menu/fetch! ~params)
                                     `(:customizer.looks/list!)
                                     `(:customizer.menu/get-populars)
                                     `(:site/user-profile)]
                          :callback (fn [response]
                                      (println "Customizer init completed")
                                      (customizer-init-callback response target-id)
                                      (r/dispatch [:db/assoc-in [:customizer :initializing?] false]))
                          :on-error (fn [error]
                                      (println "Customizer init error:" error)
                                      (r/dispatch [:db/assoc-in [:customizer :initializing?] false])
                                      ;; Still initialize with empty looks to prevent infinite loading
                                      (r/dispatch [:customizer.looks/init! []]))}}))))

;; ---- Init customizer ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Events ----

(defn look-cost [color-data part]
  (let [part-price-group      (get part :price-group {"basic" 10 "premium" 20 "premium+" 30}) ;; TODO: Get actual price groups from backend
        color-price-group-key (get-in color-data [:price_group_key] "basic")]
    (get part-price-group color-price-group-key 0)))

(r/reg-event-db
  :customizer.look-cost/remove!
  (fn [db [_ cursor]]
    (update-in db (concat cursor ["formdata"]) dissoc "look-cost")))

(r/reg-event-db
  :customizer.look-cost/calc!
  (fn [db [_ cursor]]
    (if cursor
      (let [color-data (get-in db [:customizer :selected-look])
            part-data  (get-in db cursor)
            look-cost  (look-cost color-data part-data)]
        (assoc-in db (concat cursor ["formdata" "look-cost"]) {:value look-cost}))
      db)))


(r/reg-event-db
  :customizer.state/set!
  (fn [db [_ state]]
    (assoc-in db [:customizer :state] state)))

(r/reg-event-db
  :customizer.formdata/set!
  (fn [db [_ formdata]]
    (assoc db :formdata formdata)))

(r/reg-event-db
  :customizer.cursor/set!
  (fn [db [_ cursor]]
    (assoc-in db [:customizer :cursor] cursor)))

(r/reg-event-db
  :customizer.mesh->part/set!
  (fn [db [_ {:keys [id children] :as package-data}]]
    (if (and (nil? (get-in db [:customizer :mesh->part id]))
             children)
      (let [links (reduce (fn [acc [part-id {:keys [id mesh_id]}]]
                            (if mesh_id
                              (assoc acc mesh_id id)
                              acc))
                          {}  
                          (filter #(= (:type (second %)) "part") children))]
        (update-in db [:customizer :mesh->part id] merge links))
      db)))

;; ---- Events ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

;; -----------------------------------------------------------------------------
;; ---- Customizer Load Effects ----

(r/reg-event-fx
  :customizer/select-part!
  (fn [_ [_ {:keys [id] :as part-data}]]
    {:dispatch-n [[:customizer.state/set! :add]
                  [:customizer.cursor/set! [:customizer :parts id]]]}))

(r/reg-event-fx
  :customizer/init-part!
  (fn [{:keys [db]} [_ {:keys [id form package_id mesh_id] :as part-data}]]
    (let [cursor               [:customizer :parts id]
          look                 (get-in db [:customizer :selected-look])
          package              (get-in db [:customizer :packages package_id])]

          ;; TODO: Get inherited input keys from package form
          ;; inherited-input-keys (map #(when (:inherit %) (:id %)) (get-in package [:form :template]))
          ;; package-formdata     (select-keys (get-in db [:customizer :packages package_id :formdata])
          ;;                                   inherited-input-keys)
          
          ;; default-formdata (merge {:quantity  {:qty 1 :value 1}
          ;;                          :look-cost {:value (look-cost look part-data)}}
          ;;                         package-formdata)]
      
      {:dispatch-n [[:customizer.state/set! :add]
                    [:customizer.cursor/set! cursor]
                    [:db/assoc-in cursor (assoc part-data
                                                "formdata" {"look-cost" {:value (look-cost look part-data)}}
                                                :look      look)]]})))                                                

(r/reg-event-fx
 :customizer/select-package!
 (fn [_ [_ {:keys [id]}]]
   {:dispatch-n [[:customizer.state/set! :add]
                 [:customizer.cursor/set! [:customizer :packages id]]]}))                 

(r/reg-event-fx
  :customizer/init-package!
  (fn [{:keys [db]} [_ {:keys [id] :as package-data}]]
    (let [cursor           [:customizer :packages id]
          default-formdata {:quantity {:qty 1 :value 1}}]
      {:dispatch-n [[:db/assoc-in cursor package-data]
                    [:customizer.state/set! :add]
                    [:customizer.cursor/set! cursor]
                    [:customizer.mesh->part/set! package-data]]})))
                                                ;; :formdata default-formdata)]]})))

(r/reg-event-fx
  :customizer/load!
  (fn [{:keys [db]} [_ {:keys [id type] :as props}]]

    (if (= "package" type)
      (if-let [package-data (get-in db [:customizer :packages id])]
        ;; EDIT Package
        {:dispatch-n [[:db/assoc-in [:customizer :package-id] id]
                      [:customizer/select-package! package-data]]}
        ;; INIT Package
        {:dispatch-n [[:db/assoc-in [:customizer :package-id] id]
                      [:customizer/init-package! props]]})

      (if-let [part-data (get-in db [:customizer :parts id])]
        ;; EDIT Part
        {:dispatch-n [[:db/assoc-in [:customizer :package-id] (:package_id part-data)]
                      [:customizer/select-part! part-data]]}
        ;; INIT Part
        {:dispatch-n [[:db/assoc-in [:customizer :package-id] (:package_id props)]
                      [:customizer/init-part! props]]}))))


;; ---- Customizer Load Effects ----
;; -----------------------------------------------------------------------------

(r/reg-event-fx
  :customizer/clean!
  (fn [{:keys [db]} [_]]
    {:dispatch-n [[:db/dissoc-in [:customizer :cursor]]
                  [:db/dissoc-in [:customizer :state]]]}))

;; -----------------------------------------------------------------------------
;; -----------------------------------------------------------------------------

(r/reg-event-fx
  :customizer/cart-handler
  (fn [{:keys [db]} [_]]
    (let [cursor              (get-in db [:customizer :cursor])
          selected-item-props (get-in db cursor)
          package-job-id      (get-in db [:customizer :pjid])
          new-job-id          (random-uuid)]
      
     ;; CHECK SELECTED ITEM TYPE
      (if (= "package" (:type selected-item-props))
        
        ;; Packgage
        {:dispatch-n [[:notifications/notify! :success (str "Package " (:name selected-item-props))]
                      [:db/assoc-in [:cart :content new-job-id] (assoc selected-item-props :job-id new-job-id)]
                      ;[:x.db/set-item! [:customizer :pjid] new-job-id]
                      [:cart.count/inc!]]}

        ;; PART
        (if-not (contains? (get-in db [:cart :content]) package-job-id)
          
          (if (contains? (get db :packages) (:package-id selected-item-props))
            {:dispatch-n [[:notifications/notify! :success "No package Add part"]
                          [:db/assoc-in [:customizer :pjid] new-job-id]
                          [:cart.count/inc!]
                          [:db/assoc-in [:cart :content new-job-id] (assoc (get-in db [:packages (:package-id selected-item-props)])
                                                                           :job-id new-job-id
                                                                           :parts  {(:id selected-item-props) (assoc selected-item-props :job-id (random-uuid))})]]}
            {:dispatch-n [[:notifications/notify! :success "Solo Part Add part"]
                          [:cart.count/inc!]
                          [:db/assoc-in [:cart :content new-job-id] (assoc selected-item-props :job-id new-job-id)]]})

          (if-let [cart-item (get-in db [:cart :content package-job-id :parts (:id selected-item-props)])]
          
            {:dispatch-n [[:notifications/notify! :success (str "Part already exist in Package " (:name selected-item-props))]
                          [:cart.count/inc!]
                          [:db/assoc-in [:customizer :pjid] new-job-id]
                          [:db/assoc-in [:cart :content new-job-id] (assoc (get-in db [:packages (:package-id selected-item-props)])
                                                                           :job-id new-job-id
                                                                           :parts  {(:id selected-item-props) (assoc selected-item-props :job-id (random-uuid))})]]}
          
            
            {:dispatch-n [[:notifications/notify! :success (str "New Part New package Package " (:name selected-item-props))]
                          [:db/assoc-in [:cart :content package-job-id :parts (:id selected-item-props)] (assoc selected-item-props :job-id new-job-id)]]}))))))

(r/reg-event-fx
  :customizer/cart-handler-2
  (fn [{:keys [db]} [_]]
    (let [package-id          (get-in db [:customizer :package-id])
          package-data        (get-in db [:customizer :packages package-id])
          new-job-id          (str (random-uuid))]

     ;; CHECK SELECTED ITEM TYPE
      (if (= 1 (count (:children package-data)))

        (if-let [part-data (get-in db [:customizer :parts (name (first (first (:children package-data))))])]
          {:dispatch-n [[:notifications/notify! :success (str "Part " (:name package-data))]
                        [:db/assoc-in [:cart :content new-job-id] (assoc part-data :job-id new-job-id)]
                        [:cart.count/inc!]]})
        
        (let [new-job (assoc package-data :job-id new-job-id
                             :parts  (select-keys (get-in db [:customizer :parts])
                                                  (map #(-> % first name) (:children package-data))))]
          
          {:dispatch-n [[:notifications/notify! :success (str "Package " (:name package-data))]
                        [:db/assoc-in [:cart :content new-job-id] new-job]
                        [:cart.count/inc!]]})))))
        

(r/reg-event-fx
  :customizer/add-to-cart!
  (fn [{:keys [db]} [_]]
    {:dispatch-n [[:customizer/cart-handler-2]]}))

(r/reg-event-fx
  :customizer/proceed!
  (fn [{:keys [db]} [_]]
    (if (or (get-in db [:user :disable-cart-dialog?] false)
            (= 1 (count (get-in db [:customizer :packages (get-in db [:customizer :package-id]) :children]))))
      {:dispatch [:customizer/add-to-cart!]}
      {:dispatch [:customizer.cart-dialog/open!]})))


(r/reg-event-fx
 :customizer/close!
 (fn [{:keys [db]} [_]]
   (let [item (get-in db [:customizer :item])]
     {:dispatch-n [[:x.db/set-item! [:customizer :item]  {:look (:look item)}]
                   [:x.db/set-item! [:customizer :state] :close]
                   [:x.db/set-item! [:formdata]          nil]]})))

(r/reg-event-fx
  :customizer/edit!
  (fn [_ [_ _ {:keys [grouped?] :as job-props}]]
    {:dispatch-n [[:colors/force-move!]
                  [:x.db/set-item! [:customizer] {:item  (if grouped? 
                                                          (dissoc job-props :look)
                                                          job-props)
                                                  :state :edit
                                                  :total 0}]]}))
                 
(r/reg-event-fx
  :customizer.item.part/edit!
  (fn [{:keys [db]} [_ job-id {:keys [id form formdata look]}]]

    {:dispatch-n [[:colors/force-move!]
                  [:x.db/set-item! [:customizer] {:item  (assoc (get-in db [:cart :content job-id])
                                                                :part-id id)
                                                  :state :edit}]
                  [:x.db/set-item! [:customizer/categories :selected] id]
                  [:x.db/set-item! [:customizer :item :form] form]
                  [:customizer.formdata/set! formdata]
                  [:colors/select! look]]}))
                                                            

(r/reg-event-fx
  :customizer.edit/save!
  (fn [{:keys [db]} [_]]
    (let [{:keys [item total]} (get-in db [:customizer])
          job-id               (:job-id item)
          part-id              (:part-id item)
          formdata             (get-in db [:formdata])
          new-item             (update-in item [:packages part-id] assoc :price total :formdata formdata)]
          
      {:dispatch-n [[:x.db/set-item! [:cart :content job-id] new-item]
                    [:notifications/notify! :success "Changes saved!"]
                    [:customizer/close!]]})))


(defn- update-grouped-item-props [{:keys [part-id parts] :as item-props} formdata total]
  (assoc item-props 
         :total total
         :parts {part-id (assoc (get parts part-id)
                                :price    total
                                :formdata formdata)}))

(r/reg-event-fx
  :customizer/init-grouped-job!
  (fn [_ [_ item-props]]
    (let [job-id (random-uuid)]
      {:dispatch-n [[:x.db/set-item! [:cart :content job-id] (assoc item-props :job-id job-id)]
                    [:x.db/set-item! [:customizer :item :job-id] job-id]
                    [:notifications/notify! :success "Job added to cart!"]
                    [:cart.count/inc!]]})))

(r/reg-event-fx
 :customizer/init-job!
 (fn [_ [_ item-props]]
   (let [job-id (random-uuid)]
     {:dispatch-n [[:x.db/set-item! [:cart :content job-id] (assoc item-props :job-id job-id)]
                   [:notifications/notify! :success "Job added to cart!"]
                   [:cart.count/inc!]]})))


(r/reg-event-fx
  :customizer/add-to-cart2!
  (fn [{:keys [db]} [_ {:keys [part-id parts job-id] :as item-props} total]]
    ; check job-id already exist in cart
    ; false => create job-id
    ; true ->
    ; check selected-part exist in cart-item
    ; true => create new job in cart
    ; false => add to existing job

    ;; CHECK IF JOB EXISTS
    (if-let [cart-item-props (get-in db [:cart :content job-id])]
      
      ;; CHECK JOB TYPE
      (if (get-in cart-item-props [:parts part-id])
        

        {:dispatch-n [[:notifications/notify! :success (str "part exist: " part-id)]
                      [:customizer/init-grouped-job! (assoc item-props 
                                                            :total total
                                                            :parts {part-id (assoc (get parts part-id) 
                                                                                   :price total
                                                                                   :formdata (get db :formdata))})]]}
        
        {:dispatch-n [[:notifications/notify! :success (str "new part: " part-id)]
                      [:x.db/set-item! [:cart :content job-id :total] (+ (:total cart-item-props) total)]
                      [:x.db/set-item! [:cart :content job-id :parts part-id] (assoc (get parts part-id) 
                                                                                     :price total
                                                                                     :formdata (get db :formdata))]]})
      
      ; new job
      (if (:grouped? item-props)
        {:dispatch-n [[:customizer/init-grouped-job! (update-grouped-item-props item-props (get db :formdata) total)]]}
                  
        {:dispatch-n [[:customizer/init-job! (assoc item-props :formdata (get db :formdata)
                                                               :price    total
                                                               :total    total)]]}))))

(r/reg-event-fx
  :customizer/add!
  (fn [{:keys [db]} [_]]
    (let [{:keys [item total]} (get-in db [:customizer])
          item-data            (assoc item :price total)]
      {:dispatch-n [[:customizer/add-to-cart! item-data total]]})))

;; ---- Effects ----
;; -----------------------------------------------------------------------------


;; TODO
;; 
;; Load back customizer stash after save
;; Remove customizer stash & state if menu changed
;; Add Cursor state for customizer to see what item selected currently (being helpfull with grouped items)

;; Problem: Inherite data from formdata
;; Example: Have a Three pieace rim, and we fill its form now we want some value(qty, condition,etc..) to inherite its part.
;; Update form data structure to contain a vector of input ids so we can select the exact values we want 

;; Localstorage
;; - Save added packages & forms
;; - Save added parts & forms
;; - Save customizer cursor, state
;; - Save category menu state