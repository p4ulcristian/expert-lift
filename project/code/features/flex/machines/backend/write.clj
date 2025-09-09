(ns features.flex.machines.backend.write
  (:require
   [pathom.backend.utils :as utils]
   [features.flex.machines.backend.db :as db]
   [cheshire.core :as json])
  (:import [java.time.format DateTimeFormatter]))

(def date-formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd"))

(defn format-date
  "Format date for database storage"
  [date]
  (when date
    (if (instance? java.time.LocalDate date)
      (.format date-formatter date)
      date)))

(defn get-workspace-id-from-context
  "Extract workspace ID from context or request"
  [env]
  (utils/get-entity-from-mutation env :workspace/id))

(defn set-machine-defaults
  "Set default values for machine fields"
  [machine-data]
  (merge machine-data
         {:category (or (:category machine-data) "Custom")
          :status (or (:status machine-data) "Idle")
          :location (or (:location machine-data) nil)
          :energy_type (or (:energy_type machine-data) "Electrical")
          :energy_consumption (or (:energy_consumption machine-data) 0)
          :consumption_mode (or (:consumption_mode machine-data) "manual")
          :sensor_id (or (:sensor_id machine-data) nil)
          :amortization_type (or (:amortization_type machine-data) "time-based")
          :amortization_rate (or (:amortization_rate machine-data) 0)
          :usage_unit (or (:usage_unit machine-data) nil)
          :production_rate (or (:production_rate machine-data) 0)
          :oee_target (or (:oee_target machine-data) 85)
          :operating_temp (or (:operating_temp machine-data) 0)
          :operating_pressure (or (:operating_pressure machine-data) 0)
          :maintenance_interval_days (or (:maintenance_interval_days machine-data) 30)
          :workstation_id (or (:workstation_id machine-data) nil)}))

(defn prepare-machine-data
  "Prepare machine data for database"
  [machine-data workspace-id]
  (let [wear-parts-json (json/generate-string (:wear_parts machine-data []))
        defaults (set-machine-defaults machine-data)]
    (merge defaults
           {:workspace_id workspace-id
            :wear_parts wear-parts-json
            :last_maintenance (format-date (:last_maintenance machine-data))
            :maintenance_due (format-date (:maintenance_due machine-data))})))

(defn create-machine
  "Create new machine"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id-from-context {:request request})
          machine-data (dissoc params :parquery/keys)
          id (java.util.UUID/randomUUID)
          full-data (prepare-machine-data (assoc machine-data :id id) workspace-id)]
      (db/create-machine full-data)
      (assoc machine-data :id id :wear_parts (:wear_parts machine-data [])))
    (catch Exception e
      (println "Error creating machine:" (.getMessage e))
      (println "Stack trace:" (.printStackTrace e))
      nil)))

(defn edit-machine
  "Edit existing machine"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [machine-data (dissoc params :parquery/keys)
          full-data (prepare-machine-data machine-data nil)]
      (db/edit-machine full-data)
      (assoc machine-data :wear_parts (:wear_parts machine-data [])))
    (catch Exception e
      (println "Error editing machine:" (.getMessage e))
      (println "Stack trace:" (.printStackTrace e))
      nil)))

(defn delete-machine
  "Delete machine by ID"
  [{:parquery/keys [context] :as params}]
  (try
    (let [id (:id params)]
      (db/delete-machine {:id id})
      {:id id})
    (catch Exception e
      (println "Error deleting machine:" (.getMessage e))
      nil)))

(defn update-machine-usage
  "Update machine usage"
  [{:parquery/keys [context] :as params}]
  (try
    (let [id (:id params)]
      (db/update-machine-usage {:id id})
      {:id id :status "updated"})
    (catch Exception e
      (println "Error updating machine usage:" (.getMessage e))
      nil)))