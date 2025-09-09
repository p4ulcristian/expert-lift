
(ns features.labs.looks.backend.resolvers
  (:require
    [com.wsscode.pathom3.connect.operation :as pco]
    [features.labs.looks.backend.db :as db]))

;; -----------------------------------------------------------------------------
;; ---- Get Layers Suggestions ----

(defn get-layers-fn [_env]
  (try
    (->> (db/get-layers-suggestions)
         (mapcat :layers)
         (distinct))
    (catch Exception e
      (println "Error getting layers:" e)
      nil)))

;; (distinct (get-layers-fn {:id "d6fd9ddd-bc23-4bca-9b94-c44470572ab4"}))

(pco/defresolver get-layers-suggestions-r [env _]
  {:looks.layers/get-suggestions! (get-layers-fn env)})

;; ---- Get Layers Suggestions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- List Looks ----

(defn list-looks-fn [env]
  (let [params (-> env pco/params)]
    
    (try
      (db/list-looks
        {:search          (if (empty? (:search params)) nil (:search params))
         :tags            (:tags params)
         :color-family    (:color-family params)
         :price-group-key (:price-group-key params)})
      (catch Exception e
        (println "Error listing looks:" e)
        nil))))

;; (list-looks-fn {:search ""})

(pco/defresolver list-looks-r [env _]
  {:looks/list! (list-looks-fn env)})

;; ---- List Looks ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Look ----

(defn get-look-fn [env]
  (let [params (-> env pco/params)]
    (println params)
    (try
      (db/get-look
        {:id (:id params)})
      (catch Exception e
        (println "Error getting look:" e)
        nil))))

;; (get-look-fn {:id "d6fd9ddd-bc23-4bca-9b94-c44470572ab4"})

(pco/defresolver get-look-r [env _]
  {:looks/get! (get-look-fn env)})

;; ---- Get Look ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Name Suggestions ----

(defn get-name-suggestions-fn [env]
  (println "get-name-suggestions-fn")
  (let [params (-> env pco/params)]
    (println params)
    (try
      (db/get-name-suggestions
        {:search (:search params)})
      (catch Exception e
        (println "Error getting name suggestions:" e)
        nil))))

;; (get-name-suggestions-fn {:search ""})
;; (get-name-suggestions-fn {:search "test"})

(pco/defresolver get-name-suggestions-r [env _]
  {:looks.name/get-suggestions! (get-name-suggestions-fn env)})

;; ---- Get Name Suggestions ----
;; -----------------------------------------------------------------------------

(def resolvers [list-looks-r
                get-look-r
                get-layers-suggestions-r
                get-name-suggestions-r])