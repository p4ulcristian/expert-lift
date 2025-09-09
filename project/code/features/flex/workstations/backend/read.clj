(ns features.flex.workstations.backend.read
  (:require
   [features.flex.workstations.backend.db :as db]))

(defn get-workstation-machines
  "Get machines assigned to a workstation"
  [workstation-id]
  (try
    (db/get-workstation-machines {:workstation_id workstation-id})
    (catch Exception e
      (println "Error getting workstation machines:" (.getMessage e))
      [])))

(defn get-workstation-processes
  "Get processes assigned to a workstation"
  [workstation-id]
  (try
    (db/get-workstation-processes {:workstation_id workstation-id})
    (catch Exception e
      (println "Error getting workstation processes:" (.getMessage e))
      [])))

(defn enrich-workstation-with-data
  "Add machines and processes data to a workstation"
  [workstation]
  (assoc workstation
         :machines (get-workstation-machines (:workstation/id workstation))
         :processes (get-workstation-processes (:workstation/id workstation))))

(defn get-workstations
  "Get all workstations for a workspace with their machines and processes"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (let [workstations (db/get-workstations {:workspace_id workspace-id})]
          (map enrich-workstation-with-data workstations))))
    (catch Exception e
      (println "Error getting workstations:" (.getMessage e))
      [])))

(defn get-workstation
  "Get a single workstation by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation-id params)]
      (when workstation-id
        (db/get-workstation {:workstation/id workstation-id})))
    (catch Exception e
      (println "Error getting workstation:" (.getMessage e))
      nil)))

(defn get-available-machines
  "Get machines available for assignment in a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (db/get-available-machines {:workspace_id workspace-id})))
    (catch Exception e
      (println "Error getting available machines:" (.getMessage e))
      [])))

(defn get-available-processes
  "Get processes available for assignment in a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (db/get-available-processes {:workspace_id workspace-id})))
    (catch Exception e
      (println "Error getting available processes:" (.getMessage e))
      [])))

(defn get-batches-with-current-step-on-workstation
  "Get batches with current step on a specific workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation-id params)
          workspace-id (:workspace-id params)]
      (when (and workstation-id workspace-id)
        (db/get-batches-with-current-step-on-workstation 
         {:workstation_id workstation-id :workspace_id workspace-id})))
    (catch Exception e
      (println "Error getting batches with current step on workstation:" (.getMessage e))
      [])))

(defn get-workstation-batches
  "Get all batches associated with a workstation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation-id params)
          workspace-id (:workspace-id params)]
      (when (and workstation-id workspace-id)
        (db/get-workstation-batches 
         {:workstation_id workstation-id :workspace_id workspace-id})))
    (catch Exception e
      (println "Error getting workstation batches:" (.getMessage e))
      [])))

(defn get-workstation-machines-query
  "Get workstation machines for ParQuery"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation-id params)]
      (when workstation-id
        (get-workstation-machines workstation-id)))
    (catch Exception e
      (println "Error getting workstation machines:" (.getMessage e))
      [])))

(defn get-workstation-processes-query
  "Get workstation processes for ParQuery"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workstation-id (:workstation-id params)]
      (when workstation-id
        (get-workstation-processes workstation-id)))
    (catch Exception e
      (println "Error getting workstation processes:" (.getMessage e))
      [])))