(ns features.flex.workstations.backend.write
  (:require
   [features.flex.workstations.backend.db :as db]))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workstation/workspace-id params)
      (:workspace/id context)))

(defn create-workstation
  "Create a new workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          id (:workstation/id params)
          name (:workstation/name params)
          description (:workstation/description params)]
      (db/add-workstation! {:workstation/id id
                           :workstation/name name
                           :workstation/description description
                           :workstation/workspace-id workspace-id})
      {:workstation/id id
       :workstation/name name
       :workstation/description description
       :workstation/workspace-id workspace-id})
    (catch Exception e
      (println "Error creating workstation:" (.getMessage e))
      {:error (.getMessage e)})))

(defn edit-workstation
  "Edit an existing workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          id (:workstation/id params)
          name (:workstation/name params)
          description (:workstation/description params)]
      (db/edit-workstation {:workstation/id id
                           :workstation/name name
                           :workstation/description description
                           :workstation/workspace-id workspace-id})
      {:workstation/id id
       :workstation/name name
       :workstation/description description
       :workstation/workspace-id workspace-id})
    (catch Exception e
      (println "Error editing workstation:" (.getMessage e))
      {:error (.getMessage e)})))

(defn cleanup-workstation-associations
  "Remove all associations before deleting workstation"
  [workstation-id]
  (try
    (db/unassign-all-machines-from-workstation {:workstation_id workstation-id})
    (db/delete-all-workstation-processes {:workstation_id workstation-id})
    (catch Exception e
      (println "Error cleaning up workstation associations:" (.getMessage e)))))

(defn delete-workstation
  "Delete a workstation and its associations"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          id (:workstation/id params)]
      (cleanup-workstation-associations id)
      (db/delete-workstation {:workstation/id id :workspace-id workspace-id})
      {:workstation/id id})
    (catch Exception e
      (println "Error deleting workstation:" (.getMessage e))
      {:error (.getMessage e)})))

(defn assign-machine
  "Assign a machine to a workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation/workstation-id params)
          machine-id (:machine/machine-id params)]
      (db/assign-machine-to-workstation {:workstation/id workstation-id
                                         :machine/id machine-id})
      {:machine/id machine-id
       :workstation/workstation-id workstation-id})
    (catch Exception e
      (println "Error assigning machine:" (.getMessage e))
      {:error (.getMessage e)})))

(defn unassign-machine
  "Unassign a machine from its workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [machine-id (:machine/machine-id params)]
      (db/unassign-machine-from-workstation {:machine/id machine-id})
      {:machine/id machine-id
       :workstation/workstation-id nil})
    (catch Exception e
      (println "Error unassigning machine:" (.getMessage e))
      {:error (.getMessage e)})))

(defn assign-process
  "Assign a process to a workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation-id params)
          process-id (:process-id params)]
      (db/assign-process-to-workstation {:workstation_id workstation-id
                                         :process_id process-id})
      {:workstation_id workstation-id
       :process_id process-id})
    (catch Exception e
      (println "Error assigning process:" (.getMessage e))
      {:error (.getMessage e)})))

(defn unassign-process
  "Unassign a process from a workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation-id params)
          process-id (:process-id params)]
      (db/unassign-process-from-workstation {:workstation_id workstation-id
                                             :process_id process-id})
      {:workstation_id workstation-id
       :process_id process-id})
    (catch Exception e
      (println "Error unassigning process:" (.getMessage e))
      {:error (.getMessage e)})))