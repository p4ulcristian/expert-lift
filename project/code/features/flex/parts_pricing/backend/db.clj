(ns features.flex.parts-pricing.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn get-parts
  "Get all parts with basic information"
  []
  (postgres/execute-sql 
   "SELECT 
      id,
      name,
      description,
      picture_url,
      to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM parts
    ORDER BY created_at DESC"
   {}))

(defn get-parts-with-pricing
  "Get parts with pricing information for a workspace"
  [workspace-id]
  (postgres/execute-sql 
   "SELECT 
      p.id,
      p.name,
      p.description,
      p.picture_url,
      p.package_id,
      to_char(p.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(p.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at,
      COALESCE(pp.price_basic, 0) as price_basic,
      COALESCE(pp.price_basic_plus, 0) as price_basic_plus,
      COALESCE(pp.price_pro, 0) as price_pro,
      COALESCE(pp.price_pro_plus, 0) as price_pro_plus,
      COALESCE(pp.is_active, false) as is_active,
      pp.id as pricing_id
    FROM parts p
    LEFT JOIN parts_pricing pp ON p.id = pp.part_id AND pp.workspace_id = $1
    ORDER BY p.created_at DESC"
   {:params [workspace-id]}))

(defn get-packages-with-categories
  "Get packages with their category information"
  []
  (postgres/execute-sql 
   "SELECT 
      p.id,
      p.name,
      p.description,
      p.picture_url,
      p.category_id,
      c.name as category_name
    FROM packages p
    LEFT JOIN categories c ON p.category_id = c.id
    ORDER BY c.name, p.name"
   {}))

(defn get-categories-hierarchy
  "Get categories hierarchy structure"
  []
  (postgres/execute-sql 
   "SELECT 
      c.id,
      c.name,
      c.description,
      c.picture_url,
      c.category_id as parent_id
    FROM categories c
    ORDER BY c.name"
   {}))

(defn upsert-parts-pricing
  "Insert or update parts pricing for a workspace and part"
  [workspace-id part-id price-basic price-basic-plus price-pro price-pro-plus is-active]
  (postgres/execute-sql 
   "INSERT INTO parts_pricing (workspace_id, part_id, price_basic, price_basic_plus, price_pro, price_pro_plus, is_active, updated_at)
    VALUES ($1, $2, $3, $4, $5, $6, $7, CURRENT_TIMESTAMP)
    ON CONFLICT (workspace_id, part_id)
    DO UPDATE SET 
      price_basic = EXCLUDED.price_basic,
      price_basic_plus = EXCLUDED.price_basic_plus,
      price_pro = EXCLUDED.price_pro,
      price_pro_plus = EXCLUDED.price_pro_plus,
      is_active = EXCLUDED.is_active,
      updated_at = CURRENT_TIMESTAMP"
   {:params [workspace-id part-id price-basic price-basic-plus price-pro price-pro-plus is-active]}))

(defn get-parts-pricing
  "Get specific parts pricing for workspace and part"
  [workspace-id part-id]
  (first 
   (postgres/execute-sql 
    "SELECT 
       id,
       workspace_id,
       part_id,
       price_basic,
       price_basic_plus,
       price_pro,
       price_pro_plus,
       is_active,
       to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
       to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
     FROM parts_pricing 
     WHERE workspace_id = $1 AND part_id = $2"
    {:params [workspace-id part-id]})))