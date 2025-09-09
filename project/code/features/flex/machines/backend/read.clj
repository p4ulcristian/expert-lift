(ns features.flex.machines.backend.read
  (:require
   [features.flex.machines.backend.db :as db]
   [cheshire.core :as json]))

(defn parse-wear-parts
  "Parse wear parts JSON string"
  [wear-parts-json]
  (try
    (if (string? wear-parts-json)
      (json/parse-string wear-parts-json true)
      wear-parts-json)
    (catch Exception e
      (println "Error parsing wear parts JSON:" (.getMessage e))
      [])))

(defn parse-energy-profiles
  "Parse energy profiles JSON string"
  [energy-profiles-json]
  (try
    (if (string? energy-profiles-json)
      (json/parse-string energy-profiles-json true)
      energy-profiles-json)
    (catch Exception e
      (println "Error parsing energy profiles JSON:" (.getMessage e))
      [])))

(defn process-machine-data
  "Process machine data with parsed JSON fields"
  [machine]
  (-> machine
      (assoc :wear_parts (parse-wear-parts (:wear_parts machine)))
      (assoc :energy_profiles (parse-energy-profiles (:energy_profiles machine)))))

(defn get-machines
  "Get machines for workspace"
  [{:parquery/keys [context] :as params}]
  (try
    (let [workspace-id (:workspace-id params)
          machines (db/get-machines {:workspace_id workspace-id})]
      (map process-machine-data machines))
    (catch Exception e
      (println "Error fetching machines:" (.getMessage e))
      [])))

(defn get-machine
  "Get single machine by ID"
  [{:parquery/keys [context] :as params}]
  (try
    (let [id (:id params)]
      (when-let [machine (db/get-machine {:id id})]
        (process-machine-data machine)))
    (catch Exception e
      (println "Error fetching machine:" (.getMessage e))
      nil)))

(defn get-machines-needing-maintenance
  "Get machines needing maintenance for workspace"
  [{:parquery/keys [context] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (db/get-machines-needing-maintenance {:workspace_id workspace-id}))
    (catch Exception e
      (println "Error fetching machines needing maintenance:" (.getMessage e))
      [])))

(defn get-machine-stats
  "Get machine statistics for workspace"
  [{:parquery/keys [context] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (db/get-machine-stats {:workspace_id workspace-id}))
    (catch Exception e
      (println "Error fetching machine stats:" (.getMessage e))
      {:total_machines 0
       :active_machines 0
       :idle_machines 0
       :maintenance_machines 0
       :down_machines 0
       :overdue_maintenance 0
       :upcoming_maintenance 0})))