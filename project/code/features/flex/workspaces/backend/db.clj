(ns features.flex.workspaces.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn get-workspace-by-id
  "Get workspace by ID using postgres/execute-sql"
  [id]
  (first (postgres/execute-sql 
          "SELECT id, name, created_at, updated_at
           FROM workspaces
           WHERE id = $1"
          {:params [id]})))

(defn create-workspace
  "Create a new workspace using postgres/execute-sql"
  [id name]
  (first (postgres/execute-sql
          "INSERT INTO workspaces (id, name, created_at, updated_at)
           VALUES ($1, $2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
           RETURNING id, name, created_at, updated_at"
          {:params [id name]})))

(defn share-workspace
  "Share workspace with user using postgres/execute-sql"
  [id workspace-id user-id role]
  (first (postgres/execute-sql
          "INSERT INTO workspace_shares (id, workspace_id, user_id, role, created_at, updated_at)
           VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
           RETURNING id, workspace_id, user_id, role, created_at, updated_at"
          {:params [id workspace-id user-id role]})))

(defn delete-workspace
  "Delete workspace using postgres/execute-sql"
  [id]
  (first (postgres/execute-sql
          "DELETE FROM workspaces WHERE id = $1 RETURNING id"
          {:params [id]})))

(defn get-my-workspaces
  "Get all workspaces shared with user using postgres/execute-sql"
  [user-id]
  (postgres/execute-sql 
   "SELECT w.id, w.name, w.created_at, w.updated_at, ws.role,
           bi.facility_state, bi.facility_city
    FROM workspaces w
    INNER JOIN workspace_shares ws ON w.id = ws.workspace_id
    LEFT JOIN business_info bi ON w.id = bi.workspace_id
    WHERE ws.user_id = $1
    ORDER BY w.created_at DESC"
   {:params [user-id]}))

(defn get-user-workspace-role
  "Get user role in a specific workspace"
  [user-id workspace-id]
  (when (and user-id workspace-id)
    (let [result (first (postgres/execute-sql 
                         "SELECT role FROM workspace_shares WHERE user_id = $1 AND workspace_id = $2"
                         {:params [user-id workspace-id]}))]
      (:role result))))