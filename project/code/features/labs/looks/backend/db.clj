(ns features.labs.looks.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn create-look
  "Save a new look to the database"
  [{:keys [id price-group-key thumbnail tags texture name basecolor color-family layers layers-count]}]
  (postgres/execute-sql
   "INSERT INTO looks (
        id,
        price_group_key,
        thumbnail,
        tags,
        texture,
        name,
        basecolor,
        color_family,
        layers,
        layers_count,
        created_at
    ) VALUES (
        $1,
        $2,
        $3,
        $4,
        $5,
        $6,
        $7,
        $8,
        $9,
        $10,
        NOW()
    )"
   {:params [id price-group-key thumbnail tags texture name basecolor color-family layers layers-count]}))

(defn get-look
  "Get a look by its ID"
  [{:keys [id]}]
  (postgres/execute-sql
   "SELECT 
        id,
        name,
        basecolor,
        color_family,
        thumbnail,
        price_group_key,
        tags,
        texture,
        layers,
        layers_count,
        to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at
    FROM looks
    WHERE id = $1"
   {:params [id]}))

(defn list-looks
  "List all looks with optional filtering"
  [{:keys [price-group-key tags color-family search]}]
  (postgres/execute-sql
   "SELECT 
        id,
        name,
        basecolor,
        color_family,
        tags,
        thumbnail,
        texture,
        layers,
        layers_count,
        price_group_key,
        to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at
    FROM looks
    WHERE ($1::text IS NULL OR price_group_key = $1)
      AND ($2::text[] IS NULL OR tags && $2)
      AND ($3::text IS NULL OR color_family = $3)
      AND ($4::text IS NULL OR similarity(name, $4) > 0.3)
    ORDER BY 
      CASE WHEN $4::text IS NOT NULL THEN similarity(name, $4) END DESC,
      created_at DESC"
   {:params [price-group-key tags color-family search]}))

(defn update-look
  "Update an existing look"
  [{:keys [id price-group-key thumbnail tags texture name basecolor color-family layers layers-count]}]
  (postgres/execute-sql
   "UPDATE looks
    SET price_group_key = COALESCE($2, price_group_key),
        thumbnail = COALESCE($3, thumbnail),
        tags = COALESCE($4, tags),
        texture = COALESCE($5, texture),
        name = COALESCE($6, name),
        basecolor = COALESCE($7, basecolor),
        color_family = COALESCE($8, color_family),
        layers = COALESCE($9, layers),
        layers_count = COALESCE($10, layers_count)
    WHERE id = $1"
   {:params [id price-group-key thumbnail tags texture name basecolor color-family layers layers-count]}))

(defn get-name-suggestions
  "List all look names with optional filtering"
  [{:keys [search]}]
  (postgres/execute-sql
   "SELECT 
        name
    FROM looks
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

(defn get-layers-suggestions
  "Get all distinct layer variations from looks"
  []
  (postgres/execute-sql
   "SELECT DISTINCT layers
    FROM looks
    WHERE layers IS NOT NULL"
   {:params []}))

(defn delete-look
  "Delete a look by its ID"
  [{:keys [id]}]
  (postgres/execute-sql
   "DELETE FROM looks
    WHERE id = $1"
   {:params [id]}))