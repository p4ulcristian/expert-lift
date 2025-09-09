(ns features.flex.business-info.backend.read
  (:require
   [features.flex.business-info.backend.db :as db]
   [features.flex.business-info.backend.utils :as utils]))

(defn get-business-info
  "Get business info for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (let [db-record (db/get-business-info-by-workspace {:workspace-id workspace-id})]
          (utils/transform-business-info-from-db db-record))))
    (catch Exception e
      (println "Error fetching business info:" (.getMessage e))
      nil)))