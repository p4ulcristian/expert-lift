(ns features.labs.looks.backend.draft.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn create-look-draft
  "Save a new look draft to the database"
  [{:keys [name price-group-key tags texture basecolor color-family layers layers-count]}]
  (postgres/execute-sql
   "INSERT INTO looks_draft (
        name,
        price_group_key,
        tags,
        texture,
        basecolor,
        color_family,
        layers,
        layers_count
    ) VALUES (
        $1,
        $2,
        $3::varchar(255)[],
        $4::jsonb,
        $5,
        $6,
        $7::jsonb[],
        $8
    )"
   {:params [name price-group-key tags texture basecolor color-family layers layers-count]}))

(defn list-look-drafts
  "List all look drafts with optional filtering"
  [{:keys [search]}]
  (postgres/execute-sql
   "SELECT 
        id,
        name,
        basecolor,
        color_family,
        tags,
        texture,
        layers,
        layers_count,
        price_group_key
    FROM looks_draft
    WHERE ($1::text IS NULL OR 
           name ILIKE $1 || '%' OR 
           similarity(name, $1) > 0.3)
    ORDER BY 
      CASE WHEN $1::text IS NOT NULL THEN 
        CASE 
          WHEN name ILIKE $1 || '%' THEN 0
          WHEN similarity(name, $1) > 0.3 THEN 1
          ELSE 2
        END
      END,
      CASE WHEN $1::text IS NOT NULL THEN similarity(name, $1) END DESC,
      name ASC
    LIMIT 10"
   {:params [search]}))

(defn get-look-drafts-suggestions
  "List all look drafts with optional filtering"
  [{:keys [search]}]
  (postgres/execute-sql
   "SELECT 
        id,
        name
    FROM looks_draft
    WHERE ($1::text IS NULL OR 
           name ILIKE $1 || '%' OR 
           similarity(name, $1) > 0.3)
    ORDER BY 
      CASE WHEN $1::text IS NOT NULL THEN 
        CASE 
          WHEN name ILIKE $1 || '%' THEN 0
          WHEN similarity(name, $1) > 0.3 THEN 1
          ELSE 2
        END
      END,
      CASE WHEN $1::text IS NOT NULL THEN similarity(name, $1) END DESC,
      name ASC"
   {:params [search]}))

(defn get-look-draft
  "Get a look draft by its ID"
  [{:keys [id]}]
  (postgres/execute-sql
   "SELECT 
        id,
        name,
        basecolor,
        color_family,
        tags,
        texture,
        layers,
        layers_count,
        price_group_key
    FROM looks_draft
    WHERE id = $1"
   {:params [id]}))

(defn update-look-draft
  "Update an existing look draft"
  [{:keys [id name price-group-key tags texture basecolor color-family layers layers-count]}]
  (postgres/execute-sql
   "UPDATE looks_draft
    SET name = COALESCE($2, name),
        price_group_key = COALESCE($3, price_group_key),
        tags = COALESCE($4::varchar(255)[], tags),
        texture = COALESCE($5::jsonb, texture),
        basecolor = COALESCE($6, basecolor),
        color_family = COALESCE($7, color_family),
        layers = COALESCE($8::jsonb[], layers),
        layers_count = COALESCE($9, layers_count)
    WHERE id = $1"
   {:params [id name price-group-key tags texture basecolor color-family layers layers-count]}))

(defn delete-look-draft
  "Delete a look draft by its ID"
  [{:keys [id]}]
  (postgres/execute-sql
   "DELETE FROM looks_draft
    WHERE id = $1"
   {:params [id]}))