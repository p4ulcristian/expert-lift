(ns features.labs.parts.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

;; -----------------------------------------------------------------------------
;; ---- Categories ----

(defn create-category
  "Save a new category to the database"
  [{:keys [name description picture_url category_id order_position]}]
  (postgres/execute-sql
   "INSERT INTO categories (
        name,
        description,
        picture_url,
        category_id,
        order_position
    ) VALUES (
        $1,
        $2,
        $3,
        $4,
        $5
    )
    RETURNING id, name, description, picture_url, category_id, created_at, order_position"
   {:params [name description picture_url category_id order_position]}))

(defn update-category
  "Update an existing category"
  [{:keys [id name description picture_url]}]
  (postgres/execute-sql
   "UPDATE categories
    SET name = COALESCE($2, name),
        description = COALESCE($3, description),
        picture_url = COALESCE($4, picture_url)
    WHERE id = $1
    RETURNING id, name, description, picture_url, category_id, created_at"
   {:params [id name description picture_url]}))

(defn delete-category
  "Delete a category by its ID"
  [{:keys [id]}]
  (postgres/execute-sql
   "DELETE FROM categories
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn update-category-order-position
  "Update order position for a single category"
  [{:keys [id order_position]}]
  (postgres/execute-sql
   "UPDATE categories
    SET order_position = $2
    WHERE id = $1
    RETURNING id, order_position"
   {:params [id order_position]}))

(defn initialize-category-order-positions
  "Set initial order positions for existing categories"
  []
  (postgres/execute-sql
   "UPDATE categories 
    SET order_position = subq.row_num
    FROM (
      SELECT c.id, ROW_NUMBER() OVER (PARTITION BY c.category_id ORDER BY c.created_at) - 1 as row_num
      FROM categories c
    ) subq
    WHERE categories.id = subq.id
    RETURNING categories.id, categories.order_position"
   {:params []}))

(defn get-next-category-order-position
  "Get the next order position for a category"
  [{:keys [category_id]}]
  (postgres/execute-sql
   "SELECT COALESCE(MAX(order_position), -1) + 1 as next_order_position
    FROM categories 
    WHERE ($1::uuid IS NULL AND category_id IS NULL) OR (category_id = $1::uuid)"
   {:params [category_id]}))

;; -----------------------------------------------------------------------------
;; ---- Packages ----

(defn create-package
  "Save a new package to the database"
  [{:keys [name description picture_url prefix category_id model_url popular form_id order_position]}]
  (postgres/execute-sql
   "INSERT INTO packages (
        name,
        description,
        picture_url,
        prefix,
        category_id,
        model_url,
        popular,
        form_id,
        created_at,
        updated_at,
        order_position
    ) VALUES (
        $1,
        $2,
        $3,
        $4,
        $5,
        $6,
        $7,
        $8,
        NOW(),
        NOW(),
        $9
    )
    RETURNING id, name, description, picture_url, prefix, category_id, model_url, popular, form_id, created_at, updated_at, order_position"
   {:params [name description picture_url prefix category_id model_url popular form_id order_position]}))

(defn update-package
  "Update an existing package"
  [{:keys [id name description picture_url prefix category_id model_url popular form_id]}]
  (postgres/execute-sql
   "UPDATE packages
    SET name = COALESCE($2, name),
        description = COALESCE($3, description),
        picture_url = COALESCE($4, picture_url),
        prefix = COALESCE($5, prefix),
        category_id = COALESCE($6, category_id),
        model_url = COALESCE($7, model_url),
        popular = COALESCE($8, popular),
        form_id = $9,
        updated_at = NOW()
    WHERE id = $1
    RETURNING id, name, description, picture_url, prefix, category_id, model_url, popular, form_id, created_at, updated_at"
   {:params [id name description picture_url prefix category_id model_url popular form_id]}))

(defn delete-package
  "Delete a package by its ID"
  [{:keys [id]}]
  (postgres/execute-sql
   "DELETE FROM packages
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn update-package-order-position
  "Update order position for a single package"
  [{:keys [id order_position]}]
  (postgres/execute-sql
   "UPDATE packages
    SET order_position = $2
    WHERE id = $1
    RETURNING id, order_position"
   {:params [id order_position]}))

(defn initialize-package-order-positions
  "Set initial order positions for existing packages"
  []
  (postgres/execute-sql
   "UPDATE packages 
    SET order_position = subq.row_num
    FROM (
      SELECT p.id, ROW_NUMBER() OVER (PARTITION BY p.category_id ORDER BY p.created_at) - 1 as row_num
      FROM packages p
    ) subq
    WHERE packages.id = subq.id
    RETURNING packages.id, packages.order_position"
   {:params []}))

(defn get-next-package-order-position
  "Get the next order position for a package"
  [{:keys [category_id]}]
  (postgres/execute-sql
   "SELECT COALESCE(MAX(order_position), -1) + 1 as next_order_position
    FROM packages 
    WHERE ($1::uuid IS NULL AND category_id IS NULL) OR (category_id = $1::uuid)"
   {:params [category_id]}))

;; -----------------------------------------------------------------------------
;; ---- Parts ----

(defn create-part
  "Save a new part to the database"
  [{:keys [name description picture_url package_id mesh_id form_id popular order_position]}]
  (postgres/execute-sql
   "INSERT INTO parts (
        name,
        description,
        picture_url,
        package_id,
        mesh_id,
        form_id,
        created_at,
        updated_at,
        popular,
        order_position
    ) VALUES (
        $1,
        $2,
        $3,
        $4,
        $5,
        $6,
        NOW(),
        NOW(),
        $7,
        $8
    )
    RETURNING id, name, description, picture_url, package_id, mesh_id, form_id, created_at, updated_at, popular, order_position"
   {:params [name description picture_url package_id mesh_id form_id popular order_position]}))

(defn update-part
  "Update an existing part"
  [{:keys [id name description picture_url popular mesh_id package_id form_id]}]
  (postgres/execute-sql
   "UPDATE parts
    SET name = COALESCE($2, name),
        description = COALESCE($3, description),
        picture_url = COALESCE($4, picture_url),
        popular = COALESCE($5, popular),
        mesh_id = COALESCE($6, mesh_id),
        package_id = COALESCE($7, package_id),
        form_id = $8,
        updated_at = NOW()
    WHERE id = $1
    RETURNING id, name, description, picture_url, package_id, mesh_id, form_id, created_at, updated_at, popular"
   {:params [id name description picture_url popular mesh_id package_id form_id]}))

(defn delete-part
  "Delete a part by its ID"
  [{:keys [id]}]
  (postgres/execute-sql
   "DELETE FROM parts
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn update-part-order-position
  "Update order position for a single part"
  [{:keys [id order_position]}]
  (postgres/execute-sql
   "UPDATE parts
    SET order_position = $2
    WHERE id = $1
    RETURNING id, order_position"
   {:params [id order_position]}))

(defn initialize-part-order-positions
  "Set initial order positions for existing parts"
  []
  (postgres/execute-sql
   "UPDATE parts 
    SET order_position = subq.row_num
    FROM (
      SELECT p.id, ROW_NUMBER() OVER (PARTITION BY p.package_id ORDER BY p.created_at) - 1 as row_num
      FROM parts p
    ) subq
    WHERE parts.id = subq.id
    RETURNING parts.id, parts.order_position"
   {:params []}))

(defn get-next-part-order-position
  "Get the next order position for a part"
  [{:keys [package_id]}]
  (postgres/execute-sql
   "SELECT COALESCE(MAX(order_position), -1) + 1 as next_order_position
    FROM parts 
    WHERE ($1::uuid IS NULL AND package_id IS NULL) OR (package_id = $1::uuid)"
   {:params [package_id]}))