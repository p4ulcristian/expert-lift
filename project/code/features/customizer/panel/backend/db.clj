(ns features.customizer.panel.backend.db
  (:require 
    [zero.backend.state.postgres :as postgres]))

(defn list-looks
  "List all looks with optional filtering"
  []
  (postgres/execute-sql 
   "SELECT 
      id,
      name,
      basecolor,
      color_family,
      tags,
      thumbnail,
      texture,
      price_group_key
    FROM looks
    ORDER BY created_at DESC"))

(defn list-looks-filters
  "List all looks with optional filtering based on schema"
  [color-family price-group-key tags]
  (postgres/execute-sql 
   "SELECT 
      id,
      name,
      basecolor,
      color_family,
      tags,
      thumbnail,
      texture,
      price_group_key
    FROM looks
    WHERE ($1::varchar IS NULL OR color_family = $1::varchar)
      AND ($2::varchar IS NULL OR price_group_key = $2::varchar)
      AND (COALESCE($3::varchar[], '{}') = '{}' OR tags @> $3::varchar[])"
   {:params [color-family price-group-key tags]}))

(defn get-popular-parts
  "Get all parts where popular is true"
  []
  (postgres/execute-sql 
   "SELECT 
      id,
      name,
      description,
      picture_url,
      mesh_id,
      package_id,
      form_id
    FROM parts
    WHERE popular = true
    ORDER BY name"))