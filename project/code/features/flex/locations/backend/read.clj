(ns features.flex.locations.backend.read
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

(defn process-location
  "Process single location by parsing JSON fields"
  [location]
  (when location
    (assoc location
           :tags (parse-json-field (:tags location))
           :linked_operators (parse-json-field (:linked_operators location))
           :workstation_processes (parse-json-field (:workstation_processes location)))))

(defn get-locations
  "Get all locations for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (let [locations (db/get-locations {:workspace_id workspace-id})]
          (map process-location locations))))
    (catch Exception e
      (println "Error fetching locations:" (.getMessage e))
      [])))

(defn get-location
  "Get single location by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:id params)]
      (when id
        (when-let [location (db/get-location {:id id})]
          (process-location location))))
    (catch Exception e
      (println "Error fetching location:" (.getMessage e))
      nil)))

(defn get-locations-by-type
  "Get locations filtered by type for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)
          type (:type params)]
      (when (and workspace-id type)
        (let [locations (db/get-locations-by-type {:workspace_id workspace-id :type type})]
          (map process-location locations))))
    (catch Exception e
      (println "Error fetching locations by type:" (.getMessage e))
      [])))

(defn get-locations-with-tags
  "Get locations that have specific tags for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)
          tags (:tags params)]
      (when (and workspace-id tags)
        (let [tag-array (if (vector? tags) tags [tags])
              locations (db/get-locations-with-tags {:workspace_id workspace-id :tags tag-array})]
          (map process-location locations))))
    (catch Exception e
      (println "Error fetching locations with tags:" (.getMessage e))
      [])))