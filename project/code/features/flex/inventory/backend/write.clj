(ns features.flex.inventory.backend.write
  (:require
   [features.flex.inventory.backend.db :as db]))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workspace-id params)
      (:workspace/id context)))

(defn prepare-inventory-item-data
  "Prepare inventory item data for database operations"
  [params workspace-id]
  {:name (:name params)
   :description (:description params)
   :category (:category params)
   :type (:type params)
   :quantity (or (:quantity params) 0)
   :min_qty (or (:min_qty params) 0)
   :unit (or (:unit params) "pcs")
   :supplier (:supplier params)
   :item_category (:item_category params)
   :picture_url (:picture_url params)
   :workspace_id workspace-id})

(defn format-inventory-item-response
  "Format inventory item data for response"
  [item-data]
  (assoc item-data
         :quantity (or (:quantity item-data) 0)
         :min_qty (or (:min_qty item-data) 0)
         :unit (or (:unit item-data) "pcs")))

(defn create-inventory-item
  "Create a new inventory item"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          id (java.util.UUID/randomUUID)
          item-data (assoc (prepare-inventory-item-data params workspace-id) :id id)]
      (db/create-inventory-item item-data)
      (format-inventory-item-response item-data))
    (catch Exception e
      (println "Error creating inventory item:" (.getMessage e))
      {:error (.getMessage e)})))

(defn edit-inventory-item
  "Edit an existing inventory item"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:id params)
          workspace-id (get-workspace-id params context)
          item-data (assoc (prepare-inventory-item-data params workspace-id) :id id)]
      (db/edit-inventory-item item-data)
      (format-inventory-item-response item-data))
    (catch Exception e
      (println "Error editing inventory item:" (.getMessage e))
      {:error (.getMessage e)})))

(defn delete-inventory-item
  "Delete an inventory item by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:id params)]
      (db/delete-inventory-item {:id id})
      {:id id})
    (catch Exception e
      (println "Error deleting inventory item:" (.getMessage e))
      {:error (.getMessage e)})))