(ns features.flex.locations.backend.write
  (:require
   [features.flex.locations.backend.db :as db]
   [cheshire.core :as json]))

(defn parse-json-field
  "Parse JSON field from string or return empty vector"
  [field]
  (try
    (cond
      (string? field) (json/parse-string field true)
      (nil? field) []
      :else field)
    (catch Exception e
      (println "Error parsing JSON field:" (.getMessage e))
      [])))

(defn prepare-json-field
  "Convert field to JSON string format for database storage"
  [field]
  (try
    (if (or (nil? field) (and (coll? field) (empty? field)))
      "[]"
      (json/generate-string field))
    (catch Exception e
      (println "Error preparing JSON field:" (.getMessage e))
      "[]")))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workspace-id params)
      (:workspace/id context)))

(defn prepare-location-data
  "Prepare location data for database operations"
  [params workspace-id]
  {:name (:name params)
   :description (or (:description params) "")
   :type (or (:type params) "workstation")
   :status (or (:status params) "active")
   :capacity (:capacity params)
   :tags (prepare-json-field (:tags params))
   :linked_operators (prepare-json-field (:linked_operators params))
   :workstation_processes (prepare-json-field (:workstation_processes params))
   :is_partner_location (or (:is_partner_location params) false)
   :geo_info (:geo_info params)
   :notes (or (:notes params) "")
   :workspace_id workspace-id})

(defn format-location-response
  "Format location data for response with parsed JSON fields"
  [location-data]
  (assoc location-data
         :tags (parse-json-field (:tags location-data))
         :linked_operators (parse-json-field (:linked_operators location-data))
         :workstation_processes (parse-json-field (:workstation_processes location-data))))

(defn create-location
  "Create a new location"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          id (java.util.UUID/randomUUID)
          location-data (assoc (prepare-location-data params workspace-id) :id id)]
      (db/create-location location-data)
      (format-location-response location-data))
    (catch Exception e
      (println "Error creating location:" (.getMessage e))
      {:error (.getMessage e)})))

(defn edit-location
  "Edit an existing location"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:id params)
          workspace-id (get-workspace-id params context)
          location-data (assoc (prepare-location-data params workspace-id) :id id)]
      (db/edit-location location-data)
      (format-location-response location-data))
    (catch Exception e
      (println "Error editing location:" (.getMessage e))
      {:error (.getMessage e)})))

(defn delete-location
  "Delete a location by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:id params)]
      (db/delete-location {:id id})
      {:id id})
    (catch Exception e
      (println "Error deleting location:" (.getMessage e))
      {:error (.getMessage e)})))