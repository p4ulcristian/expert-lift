(ns features.flex.business-info.backend.write
  (:require
   [features.flex.business-info.backend.db :as db]
   [features.flex.business-info.backend.utils :as utils]))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workspace-id params)
      (:workspace/id context)))

(defn save-business-info
  "Save or update business info for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          business-data (:data params)]
      (when (and workspace-id business-data)
        (let [db-params (utils/transform-business-info-to-db workspace-id business-data)
              saved-record (db/upsert-business-info db-params)]
          (utils/transform-business-info-from-db saved-record))))
    (catch Exception e
      (println "Error saving business info:" (.getMessage e))
      {:error (.getMessage e)})))