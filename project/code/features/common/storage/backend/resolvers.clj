(ns features.common.storage.backend.resolvers
  (:require
   [clojure.java.io :as io]
   [com.wsscode.pathom3.entity-tree       :as pe]
   [com.wsscode.pathom3.connect.operation :as pco]
   [zero.backend.state.postgres           :as postgres]
   [features.common.storage.backend.utils :as storage.utils]
   [features.common.storage.backend.db    :as db]))
 
;; SQL functions are now in db.clj

;; -----------------------------------------------------------------------------
;; ---- Search suggestion ----

(defn search-by-alias-with-path-f [env _]
  (let [params      (-> env pco/params)
        search-term (get params :search-term)
        filters     (get params :filters)
        workspace-id  (-> env pe/entity :workspace/id)
        user-roles    (get-in env [:request :session :user-roles])
        is-superadmin (storage.utils/is-superadmin? user-roles workspace-id)]
    (try
      (println "search-term" search-term)
      (storage.utils/convert-instances
        (db/search-by-alias-with-path "storage" search-term workspace-id is-superadmin (:mime filters)))
      (catch Exception e
        (ex-message e)))))

;; (search-by-alias-with-path-f nil {:search-term "test"})

(pco/defresolver search-by-alias-with-path-r [env resolver-props]
  {:storage/search-suggestions 
    (search-by-alias-with-path-f env resolver-props)})


;; ---- Search suggestion ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Search ----

(defn search-by-alias-f [is-superadmin workspace-id {:keys [search-term filters]}]
  (try 
    (storage.utils/convert-instances
      (db/search-by-alias search-term workspace-id is-superadmin (:mime filters)))
    (catch Exception e
      (ex-message e))))

(defn search-items-f [env _props]
  (let [params        (-> env pco/params)
        workspace-id  (-> env pe/entity :workspace/id)
        user-roles    (get-in env [:request :session :user-roles])
        is-superadmin (storage.utils/is-superadmin? user-roles workspace-id)]
    (search-by-alias-f is-superadmin workspace-id params)))

(pco/defresolver search-items-r [env resolver-props]
  {:storage/search (search-items-f env resolver-props)})

;; ---- Search ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Open Dir ----

(defn open-home-f [workspace-id filters is-superadmin]
  (try
    (storage.utils/convert-instances
      (db/open-home workspace-id is-superadmin (:mime filters)))
    (catch Exception e
      (ex-message e))))

;; (open-home-f "fed3a3e6-672b-44c5-adc2-cc3a31b1d77e" {:mime [""]})

;; (open-dir-f "aa97a858-5902-4a4c-9717-1c29203ca680" "aa97a858-5902-4a4c-9717-1c29203ca680" {:mime ["image/png"]}) 

(defn open-dir-f [dir-id workspace-id filters is-superadmin]
  (try
    (storage.utils/convert-instances
      (db/open-dir dir-id workspace-id is-superadmin (:mime filters)))
    (catch Exception e
      (ex-data e))))

(defn open-dir-handler [env _props]
  (let [params        (-> env pco/params)
        workspace-id  (-> env pe/entity :workspace/id)
        user-roles    (get-in env [:request :session :user-roles])
        is-superadmin (storage.utils/is-superadmin? user-roles workspace-id)]
    
    (if-let [dir-id (:dir-id params)]
      (open-dir-f dir-id workspace-id (:filters params) is-superadmin)
      (open-home-f workspace-id (:filters params) is-superadmin))))



(pco/defresolver open-dir-r [env resolver-props]
  {:storage/open-dir (open-dir-handler env resolver-props)})

;; ---- Open Dir ----
;; -----------------------------------------------------------------------------

(def resolvers [search-items-r open-dir-r
                search-by-alias-with-path-r])
