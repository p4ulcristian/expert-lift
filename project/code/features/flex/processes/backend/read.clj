(ns features.flex.processes.backend.read
  (:require
   [features.flex.processes.backend.db :as db]))

(defn get-process-workstations
  "Get workstations associated with a process"
  [process-id]
  (try
    (db/get-process-workstations {:process/id process-id})
    (catch Exception e
      (println "Error fetching process workstations:" (.getMessage e))
      [])))

(defn enrich-process-with-workstations
  "Add workstation data to a process"
  [process]
  (let [workstations (get-process-workstations (:process/id process))]
    (assoc process :process/workstations workstations)))

(defn get-processes
  "Get all processes for a workspace with their workstations"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace/id params)]
      (when workspace-id
        (->> (db/get-processes {:workspace/id workspace-id})
             (mapv enrich-process-with-workstations))))
    (catch Exception e
      (println "Error fetching processes:" (.getMessage e))
      [])))

(defn get-process
  "Get a single process by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:process/id params)]
      (when id
        (db/get-process {:process/id id})))
    (catch Exception e
      (println "Error fetching process:" (.getMessage e))
      nil)))