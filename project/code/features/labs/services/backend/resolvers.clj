(ns features.labs.services.backend.resolvers
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [features.labs.services.backend.db :as db]))

(defn format-services-for-frontend 
   "Transform database services for frontend"
  [services] 
  (let [result (mapv (fn [service]
                      [(:id service) 
                       (-> service
                           ;; Remove id from the service data (it becomes the key)
                           (dissoc :id)
                           ;; Keep all other schema fields as-is
                           ;; :name, :description, :picture_url, :created_at, :updated_at
                           )])
                     services)]
    (println "Formatted services for frontend:" (pr-str result))
    result))

(pco/defresolver get-services-res [_env _]
  {::pco/op-name 'workspace-services/get-services
   ::pco/output  [:workspace-services/get-services]}
  (let [services (db/get-services)]
    {:workspace-services/get-services 
     (format-services-for-frontend services)}))

(def resolvers [get-services-res])