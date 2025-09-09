(ns features.labs.zip-codes.backend.resolvers
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [zero.backend.state.postgres :as postgres]
   [cheshire.core :as json]))

(defn parse-zip-codes-jsonb 
  "Parse the JSONB zip_codes column to extract structured data"
  [zip-codes-jsonb]
  (try
    (cond
      (string? zip-codes-jsonb) (json/parse-string zip-codes-jsonb true)
      (nil? zip-codes-jsonb) []
      :else zip-codes-jsonb)
    (catch Exception e
      (println "Error parsing zip codes JSONB:" (.getMessage e))
      [])))

(defn format-zip-codes-for-frontend 
  "Transform database zip codes for frontend display"
  [zip-codes-records]
  (try
    (mapcat (fn [record]
              (let [zip-codes-data (parse-zip-codes-jsonb (:zip_codes record))]
                (if (map? zip-codes-data)
                  ;; If it's a single zip code object
                  [(assoc zip-codes-data :db-id (:id record))]
                  ;; If it's an array of zip codes
                  (map #(assoc % :db-id (:id record)) zip-codes-data))))
            zip-codes-records)
    (catch Exception e
      (println "Error formatting zip codes for frontend:" (.getMessage e))
      [])))

(defn get-zip-codes-data-fn [{:keys [_request] :as _env}]
  (try
    (let [result (postgres/execute-honey
                  {:select [:id :zip_codes]
                   :from [:zip_codes]
                   :order-by [:id]})]
      (format-zip-codes-for-frontend result))
    (catch Exception e
      (println "Error fetching zip codes data:" (.getMessage e))
      ;; Return demo data if database fails
    [])))

(pco/defresolver get-zip-codes-res [env _]
  {:zip-codes/get-zip-codes (get-zip-codes-data-fn env)})

(defn update-zip-codes-fn [{:keys [_request] :as _env} {:keys [zip-codes-data]}]
  (try
    (println "🔧 Backend received zip-codes-data:")
    (println "🔧 Data type:" (type zip-codes-data))
    (println "🔧 Data keys:" (keys zip-codes-data))
    (println "🔧 Schema:" (:schema zip-codes-data))
    (println "🔧 Data count:" (count (:data zip-codes-data)))
    
    (let [json-data (json/generate-string zip-codes-data)
          _ (println "🔧 JSON string length:" (count json-data))
          result (postgres/execute-honey
                  {:update :zip_codes
                   :set {:zip_codes json-data}
                   :where [:= :id 1]
                   :returning [:id]})]
      (println "🔧 Database result:" result)
      (if (seq result)
        {:success true :message "Zip codes updated successfully"}
        {:success false :message "No rows updated"}))
    (catch Exception e
      (println "Error updating zip codes:" (.getMessage e))
      {:success false :message (.getMessage e)})))

(pco/defmutation update-zip-codes-mutation [env params]
  {::pco/op-name 'zip-codes/update-zip-codes!}
  (update-zip-codes-fn env params))

(defn get-all-workspaces-fn [{:keys [_request] :as _env}]
  (try
    (postgres/execute-honey
     {:select [:id :name]
      :from [:workspaces]
      :order-by [:name]})
    (catch Exception e
      (println "Error fetching all workspaces:" (.getMessage e))
      [])))

(pco/defresolver get-all-workspaces-res [env _]
  {:zip-codes/get-all-workspaces (get-all-workspaces-fn env)})

(def resolvers [get-zip-codes-res get-all-workspaces-res update-zip-codes-mutation])
