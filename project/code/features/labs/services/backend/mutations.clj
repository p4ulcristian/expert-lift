(ns features.labs.services.backend.mutations
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [features.labs.services.backend.db :as db]))

(pco/defmutation create-service-mut [_env {:keys [name description picture-url]}]
  {::pco/op-name 'workspace-services/create-service
   ::pco/output  [:workspace-services/create-service]}
  (try
    (let [service-id (java.util.UUID/randomUUID)
          service-data {:id service-id
                       :name name
                       :description description
                       :picture_url picture-url}]
      (println "Creating service:" service-data)
      (let [result (db/create-service service-data)]
        {:success true
         :message "Service created successfully"
         :service-id (:id result)}))
    (catch Exception e
      (println "Error creating service:" (.getMessage e))
      {:success false
       :error (.getMessage e)})))

(pco/defmutation update-service-mut [_env {:keys [id name description picture-url]}]
  {::pco/op-name 'workspace-services/update-service
   ::pco/output  [:workspace-services/update-service]}
  (try
    (let [service-data {:id id
                       :name name
                       :description description
                       :picture_url picture-url}]
      (println "Updating service:" service-data)
      (let [result (db/update-service service-data)]
        (if result
          {:success true
           :message "Service updated successfully"
           :service-id id}
          {:success false
           :error "Service not found"})))
    (catch Exception e
      (println "Error updating service:" (.getMessage e))
      {:success false
       :error (.getMessage e)})))

(pco/defmutation delete-service-mut [_env {:keys [id]}]
  {::pco/op-name 'workspace-services/delete-service
   ::pco/output  [:workspace-services/delete-service]}
  (try
    (println "Deleting service with id:" id)
    (let [result (db/delete-service {:id id})]
      (if result
        {:success true
         :message "Service deleted successfully"
         :service-id id}
        {:success false
         :error "Service not found"}))
    (catch Exception e
      (println "Error deleting service:" (.getMessage e))
      {:success false
       :error (.getMessage e)})))

(def mutations [create-service-mut update-service-mut delete-service-mut])