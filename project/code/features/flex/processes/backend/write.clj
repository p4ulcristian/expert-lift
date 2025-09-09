(ns features.flex.processes.backend.write
  (:require
   [features.flex.processes.backend.db :as db]))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workspace-id params)
      (:workspace/id context)))

(defn create-process
  "Create a new process"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          name (:process/name params)
          description (:process/description params)
          id (java.util.UUID/randomUUID)]
      (db/create-process {:process/id id
                          :process/name name
                          :process/description description
                          :process/workspace-id workspace-id}))
    (catch Exception e
      (println "Error creating process:" (.getMessage e))
      {:error (.getMessage e)})))

(defn edit-process
  "Edit an existing process"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:process/id params)
          name (:process/name params)
          description (:process/description params)]
      (db/edit-process {:process/id id :process/name name :process/description description})
      {:process/id id :process/name name :process/description description})
    (catch Exception e
      (println "Error editing process:" (.getMessage e))
      {:error (.getMessage e)})))

(defn cleanup-process-associations
  "Remove all associations before deleting process"
  [process-id]
  (try
    (db/delete-recipe-process-associations {:process/id process-id})
    (db/delete-workstation-process-associations {:process/id process-id})
    (catch Exception e
      (println "Error cleaning up process associations:" (.getMessage e)))))

(defn delete-process
  "Delete a process and its associations"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:process/id params)]
      (cleanup-process-associations id)
      (db/delete-process {:process/id id})
      {:process/id id})
    (catch Exception e
      (println "Error deleting process:" (.getMessage e))
      {:error (.getMessage e)})))