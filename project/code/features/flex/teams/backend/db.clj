(ns features.flex.teams.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn remove-invitation
  "Remove/delete a workspace invitation"
  [id]
  (let [result (first (postgres/execute-sql
                       "DELETE FROM workspace_invitations
                        WHERE id = $1
                        RETURNING id"
                       {:params [id]}))]
    (if result 1 0)))

(defn create-invitation
  "Create a new workspace invitation"
  [id workspace-id email role invited-by expires-at]
  (first (postgres/execute-sql
          "INSERT INTO workspace_invitations (id, workspace_id, email, role, invited_by, expires_at, created_at, updated_at)
           VALUES ($1, $2, $3, $4, $5, $6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
           RETURNING id, email, role, status, expires_at, created_at"
          {:params [id workspace-id email role invited-by expires-at]})))

(defn get-workspace-invitations
  "Get all workspace invitations with inviter information"
  [workspace-id]
  (postgres/execute-sql
   "SELECT wi.*, u.first_name, u.last_name, u.email as inviter_email
    FROM workspace_invitations wi
    INNER JOIN users u ON wi.invited_by = u.id
    WHERE wi.workspace_id = $1
    ORDER BY wi.created_at DESC"
   {:params [workspace-id]}))

(defn get-workspace-users
  "Get all users in a workspace with their roles"
  [workspace-id]
  (postgres/execute-sql
   "SELECT ws.*, u.first_name, u.last_name, u.email, u.created_at as user_created_at
    FROM workspace_shares ws
    INNER JOIN users u ON ws.user_id = u.id
    WHERE ws.workspace_id = $1
    ORDER BY u.first_name, u.last_name"
   {:params [workspace-id]}))

(defn is-workspace-owner?
  "Check if user is owner of workspace"
  [user-id workspace-id]
  (let [result (first (postgres/execute-sql
                       "SELECT role FROM workspace_shares 
                        WHERE user_id = $1 AND workspace_id = $2"
                       {:params [user-id workspace-id]}))]
    (= "owner" (:role result))))

(defn is-workspace-member?
  "Check if user is already a member of workspace (any role)"
  [user-id workspace-id]
  (let [result (first (postgres/execute-sql
                       "SELECT id FROM workspace_shares 
                        WHERE user_id = $1 AND workspace_id = $2"
                       {:params [user-id workspace-id]}))]
    (not (nil? result))))

(defn get-invitation-details
  "Get invitation details by ID with workspace and inviter information"
  [invitation-id]
  (try
    (first (postgres/execute-sql
            "SELECT wi.email as invitee_email,
                    w.name as workspace_name,
                    u.email as inviter_email,
                    u.first_name as inviter_first_name,
                    u.last_name as inviter_last_name
             FROM workspace_invitations wi
             INNER JOIN workspaces w ON wi.workspace_id = w.id
             INNER JOIN users u ON wi.invited_by = u.id
             WHERE wi.id = $1"
            {:params [invitation-id]}))
    (catch Exception e
      (println "Error in get-invitation-details:" (.getMessage e))
      nil)))

(defn get-invitation-full-details
  "Get full invitation details including workspace_id, role, status, expires_at"
  [invitation-id]
  (try
    (first (postgres/execute-sql
            "SELECT wi.*, w.name as workspace_name
             FROM workspace_invitations wi
             INNER JOIN workspaces w ON wi.workspace_id = w.id
             WHERE wi.id = $1"
            {:params [invitation-id]}))
    (catch Exception e
      (println "Error in get-invitation-full-details:" (.getMessage e))
      nil)))

(defn add-user-to-workspace
  "Add a user to a workspace with specified role"
  [user-id workspace-id role]
  (try
    (first (postgres/execute-sql
            "INSERT INTO workspace_shares (id, workspace_id, user_id, role, created_at, updated_at)
             VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
             RETURNING id, workspace_id, user_id, role"
            {:params [(str (java.util.UUID/randomUUID)) workspace-id user-id role]}))
    (catch Exception e
      (println "Error in add-user-to-workspace:" (.getMessage e))
      nil)))

(defn remove-user-from-workspace
  "Remove a user from a workspace"
  [user-id workspace-id]
  (try
    (println "🔍 DB DEBUG - remove-user-from-workspace called with user-id:" user-id "workspace-id:" workspace-id)
    (let [result (first (postgres/execute-sql
                         "DELETE FROM workspace_shares 
                          WHERE user_id = $1 AND workspace_id = $2
                          RETURNING id"
                         {:params [user-id workspace-id]}))]
      (println "🔍 DB DEBUG - SQL result:" result)
      (if result 
        (do
          (println "🔍 DB DEBUG - Success: Returning 1")
          1)
        (do
          (println "🔍 DB DEBUG - No rows affected: Returning 0")
          0)))
    (catch Exception e
      (println "🔍 DB DEBUG - Exception in remove-user-from-workspace:" (.getMessage e))
      (println "Error in remove-user-from-workspace:" (.getMessage e))
      0)))