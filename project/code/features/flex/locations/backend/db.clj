(ns features.flex.locations.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-locations
  "Get all locations ordered by name"
  [{:keys [workspace_id]}]
  (postgres/execute-sql
    "SELECT 
      locations.id,
      locations.name,
      locations.description,
      locations.type,
      locations.status,
      locations.capacity,
      locations.tags,
      locations.linked_operators,
      locations.workstation_processes,
      locations.is_partner_location,
      locations.geo_info,
      locations.notes,
      locations.workspace_id,
      to_char(locations.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(locations.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM locations
    WHERE locations.workspace_id = $1
    ORDER BY locations.name"
    {:params [workspace_id]}))

(defn get-location
  "Get a single location by id"
  [{:keys [id]}]
  (first
    (postgres/execute-sql
      "SELECT 
        locations.id,
        locations.name,
        locations.description,
        locations.type,
        locations.status,
        locations.capacity,
        locations.tags,
        locations.linked_operators,
        locations.workstation_processes,
        locations.is_partner_location,
        locations.geo_info,
        locations.notes,
        locations.workspace_id,
        to_char(locations.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(locations.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
      FROM locations
      WHERE locations.id = $1"
      {:params [id]})))

(defn create-location
  "Create a new location with all fields"
  [{:keys [id name description type status capacity tags linked_operators 
           workstation_processes is_partner_location geo_info notes workspace_id]}]
  (first
    (postgres/execute-sql
      "INSERT INTO locations (
        id, name, description, type, status, capacity, tags, 
        linked_operators, workstation_processes, is_partner_location, 
        geo_info, notes, workspace_id
      )
      VALUES (
        $1, $2, $3, $4, $5, $6, $7::jsonb,
        $8::jsonb, $9::jsonb, $10,
        $11, $12, $13
      )
      RETURNING 
        id, name, description, type, status, capacity, tags, 
        linked_operators, workstation_processes, is_partner_location,
        geo_info, notes, workspace_id,
        to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at"
      {:params [id name description type status capacity tags
                linked_operators workstation_processes is_partner_location
                geo_info notes workspace_id]})))

(defn edit-location
  "Edit a location with all fields"
  [{:keys [id name description type status capacity tags linked_operators 
           workstation_processes is_partner_location geo_info notes]}]
  (postgres/execute-sql
    "UPDATE locations
    SET name = $2,
        description = $3,
        type = $4,
        status = $5,
        capacity = $6,
        tags = $7::jsonb,
        linked_operators = $8::jsonb,
        workstation_processes = $9::jsonb,
        is_partner_location = $10,
        geo_info = $11,
        notes = $12,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $1"
    {:params [id name description type status capacity tags
              linked_operators workstation_processes is_partner_location
              geo_info notes]}))

(defn delete-location
  "Delete a location by id"
  [{:keys [id]}]
  (postgres/execute-sql
    "DELETE FROM locations WHERE id = $1"
    {:params [id]}))

(defn get-locations-by-type
  "Get locations filtered by type"
  [{:keys [workspace_id type]}]
  (postgres/execute-sql
    "SELECT 
      locations.id,
      locations.name,
      locations.description,
      locations.type,
      locations.status,
      locations.capacity,
      locations.tags,
      locations.linked_operators,
      locations.workstation_processes,
      locations.is_partner_location,
      locations.geo_info,
      locations.notes,
      locations.workspace_id,
      to_char(locations.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(locations.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM locations
    WHERE locations.workspace_id = $1
      AND locations.type = $2
    ORDER BY locations.name"
    {:params [workspace_id type]}))

(defn get-locations-with-tags
  "Get locations that contain any of the specified tags"
  [{:keys [workspace_id tags]}]
  (postgres/execute-sql
    "SELECT 
      locations.id,
      locations.name,
      locations.description,
      locations.type,
      locations.status,
      locations.capacity,
      locations.tags,
      locations.linked_operators,
      locations.workstation_processes,
      locations.is_partner_location,
      locations.geo_info,
      locations.notes,
      locations.workspace_id,
      to_char(locations.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(locations.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM locations
    WHERE locations.workspace_id = $1
      AND locations.tags ?| $2
    ORDER BY locations.name"
    {:params [workspace_id tags]}))