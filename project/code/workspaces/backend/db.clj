(ns workspaces.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

;; Expert Lift Workspace Database Functions

(defn get-all-workspaces []
  "Get all workspaces"
  (postgres/execute-sql
   "SELECT id, name, description, active, created_at, updated_at
    FROM expert_lift.workspaces
    ORDER BY name"
   {:params []}))

(defn get-workspace-by-id [id]
  "Get workspace by ID"
  (postgres/execute-sql
   "SELECT id, name, description, active, created_at, updated_at
    FROM expert_lift.workspaces
    WHERE id = $1"
   {:params [id]}))

(defn get-workspace-by-name [name]
  "Get workspace by name"
  (postgres/execute-sql
   "SELECT id, name, description, active, created_at, updated_at
    FROM expert_lift.workspaces
    WHERE name = $1"
   {:params [name]}))

(defn create-workspace [name description]
  "Create new workspace"
  (postgres/execute-sql
   "INSERT INTO expert_lift.workspaces (name, description, active, created_at, updated_at)
    VALUES ($1, $2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id, name, description, active, created_at, updated_at"
   {:params [name description]}))

(defn update-workspace [id name description active]
  "Update workspace"
  (postgres/execute-sql
   "UPDATE expert_lift.workspaces
    SET name = $1,
        description = $2,
        active = $3,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $4
    RETURNING id, name, description, active, created_at, updated_at"
   {:params [name description active id]}))

(defn delete-workspace [id]
  "Delete workspace"
  (postgres/execute-sql
   "DELETE FROM expert_lift.workspaces
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn activate-workspace [id]
  "Activate workspace"
  (postgres/execute-sql
   "UPDATE expert_lift.workspaces
    SET active = true, updated_at = CURRENT_TIMESTAMP
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn deactivate-workspace [id]
  "Deactivate workspace"
  (postgres/execute-sql
   "UPDATE expert_lift.workspaces
    SET active = false, updated_at = CURRENT_TIMESTAMP
    WHERE id = $1
    RETURNING id"
   {:params [id]}))