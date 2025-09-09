(ns features.flex.inventory.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-inventory
  "Get all inventory items ordered by name"
  [{:keys [workspace_id]}]
  (postgres/execute-sql
    "SELECT 
      inventory.id,
      inventory.name,
      inventory.description,
      inventory.category,
      inventory.type,
      inventory.quantity,
      inventory.min_qty,
      inventory.unit,
      inventory.supplier,
      inventory.item_category,
      inventory.picture_url,
      inventory.workspace_id,
      to_char(inventory.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(inventory.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM inventory
    WHERE inventory.workspace_id = $1
    ORDER BY inventory.name"
    {:params [workspace_id]}))

(defn get-inventory-item
  "Get a single inventory item by id"
  [{:keys [id]}]
  (first
    (postgres/execute-sql
      "SELECT 
        inventory.id,
        inventory.name,
        inventory.description,
        inventory.category,
        inventory.type,
        inventory.quantity,
        inventory.min_qty,
        inventory.unit,
        inventory.supplier,
        inventory.item_category,
        inventory.picture_url,
        inventory.workspace_id,
        to_char(inventory.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(inventory.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
      FROM inventory
      WHERE inventory.id = $1"
      {:params [id]})))

(defn create-inventory-item
  "Create a new inventory item"
  [{:keys [id name description category type quantity min_qty unit supplier item_category picture_url workspace_id]}]
  (first
    (postgres/execute-sql
      "INSERT INTO inventory (id, name, description, category, type, quantity, min_qty, unit, supplier, item_category, picture_url, workspace_id)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
      RETURNING 
        id,
        name,
        description,
        category,
        type,
        quantity,
        min_qty,
        unit,
        supplier,
        item_category,
        picture_url,
        workspace_id,
        to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at"
      {:params [id name description category type quantity min_qty unit supplier item_category picture_url workspace_id]})))

(defn edit-inventory-item
  "Edit an inventory item by id"
  [{:keys [id name description category type quantity min_qty unit supplier item_category picture_url]}]
  (postgres/execute-sql
    "UPDATE inventory
    SET name = $2,
        description = $3,
        category = $4,
        type = $5,
        quantity = $6,
        min_qty = $7,
        unit = $8,
        supplier = $9,
        item_category = $10,
        picture_url = $11,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $1"
    {:params [id name description category type quantity min_qty unit supplier item_category picture_url]}))

(defn delete-inventory-item
  "Delete an inventory item by id"
  [{:keys [id]}]
  (postgres/execute-sql
    "DELETE FROM inventory WHERE id = $1"
    {:params [id]}))

(defn get-low-stock-items
  "Get inventory items where quantity is at or below minimum quantity"
  [{:keys [workspace_id]}]
  (postgres/execute-sql
    "SELECT 
      inventory.id,
      inventory.name,
      inventory.description,
      inventory.category,
      inventory.type,
      inventory.quantity,
      inventory.min_qty,
      inventory.unit,
      inventory.supplier,
      inventory.item_category,
      inventory.picture_url,
      inventory.workspace_id,
      to_char(inventory.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(inventory.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM inventory
    WHERE inventory.workspace_id = $1
      AND inventory.quantity <= inventory.min_qty
    ORDER BY inventory.name"
    {:params [workspace_id]}))