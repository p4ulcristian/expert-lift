(ns features.flex.service-areas.backend.read
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn get-service-areas
  "Get service areas from zip codes table"
  [{:parquery/keys [context request] :as params}]
  (try
    (-> (postgres/execute-honey
         {:select [:zip_codes]
          :from [:zip_codes]
          :where [:= :id 1]
          :limit 1})
        first
        :zip_codes)
    (catch Exception e
      (println "Error getting service areas:" (.getMessage e))
      nil)))