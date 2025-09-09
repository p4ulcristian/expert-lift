(ns features.flex.services-pricing.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn get-services
  "Get all services ordered by name"
  []
  (postgres/execute-sql
   "SELECT 
      id,
      name,
      description
    FROM services
    ORDER BY name"
   {:params []}))

(defn get-services-with-pricing
  "Get all services with their pricing info for a workspace"
  [workspace-id]
  (postgres/execute-sql
   "SELECT 
      s.id,
      s.name,
      s.description,
      COALESCE(sp.price, 0) as price,
      COALESCE(sp.is_active, false) as is_active,
      sp.id as pricing_id
    FROM services s
    LEFT JOIN services_pricing sp ON s.id = sp.service_id AND sp.workspace_id = $1
    ORDER BY s.name"
   {:params [workspace-id]}))

(defn upsert-services-pricing
  "Insert or update services pricing for a workspace"
  [workspace-id service-id price is-active]
  (postgres/execute-sql
   "INSERT INTO services_pricing (id, workspace_id, service_id, price, is_active, updated_at)
    VALUES (gen_random_uuid(), $1, $2, $3, $4, CURRENT_TIMESTAMP)
    ON CONFLICT (workspace_id, service_id)
    DO UPDATE SET 
      price = EXCLUDED.price,
      is_active = EXCLUDED.is_active,
      updated_at = CURRENT_TIMESTAMP"
   {:params [workspace-id service-id price is-active]}))

(defn get-services-pricing
  "Get specific pricing record for a service in a workspace"
  [workspace-id service-id]
  (first
   (postgres/execute-sql
    "SELECT 
       id,
       workspace_id,
       service_id,
       price,
       is_active,
       to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
       to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
     FROM services_pricing 
     WHERE workspace_id = $1 AND service_id = $2"
    {:params [workspace-id service-id]})))

(defn get-service-by-id
  "Get a specific service with its pricing info for a workspace"
  [workspace-id service-id]
  (first
   (postgres/execute-sql
    "SELECT 
       s.id,
       s.name,
       s.description,
       COALESCE(sp.price, 0) as price,
       COALESCE(sp.is_active, false) as is_active,
       sp.id as pricing_id
     FROM services s
     LEFT JOIN services_pricing sp ON s.id = sp.service_id AND sp.workspace_id = $1
     WHERE s.id = $2"
    {:params [workspace-id service-id]})))