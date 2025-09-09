(ns features.flex.inventory.backend.read
  (:require
   [features.flex.inventory.backend.db :as db]))

(defn get-inventory
  "Get all inventory items for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (db/get-inventory {:workspace_id workspace-id})))
    (catch Exception e
      (println "Error fetching inventory:" (.getMessage e))
      [])))

(defn get-inventory-item
  "Get a single inventory item by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:id params)]
      (when id
        (db/get-inventory-item {:id id})))
    (catch Exception e
      (println "Error fetching inventory item:" (.getMessage e))
      nil)))

(defn get-low-stock-items
  "Get inventory items with low stock for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (db/get-low-stock-items {:workspace_id workspace-id})))
    (catch Exception e
      (println "Error fetching low stock items:" (.getMessage e))
      [])))