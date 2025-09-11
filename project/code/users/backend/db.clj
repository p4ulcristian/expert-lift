(ns users.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

;; Expert Lift User Queries - Using new schema with username, full_name, role enum

(defn get-user-by-id [id]
  "Get user by ID"
  (postgres/execute-sql
   "SELECT id, username, full_name, email, phone, role, active, workspace_id, created_at, updated_at
    FROM expert_lift.users
    WHERE id = $1"
   {:params [id]}))

(defn get-user-by-username [username]
  "Get user by username"
  (postgres/execute-sql
   "SELECT id, username, full_name, password_hash, email, phone, role, active, workspace_id, created_at, updated_at
    FROM expert_lift.users
    WHERE username = $1"
   {:params [username]}))

(defn get-user-by-email [email]
  "Get user by email"
  (postgres/execute-sql
   "SELECT id, username, full_name, email, phone, role, active, workspace_id, created_at, updated_at
    FROM expert_lift.users
    WHERE email = $1"
   {:params [email]}))

(defn get-all-users []
  "Get all users with workspace names"
  (postgres/execute-sql
   "SELECT u.id, u.username, u.full_name, u.email, u.phone, u.role, u.workspace_id, u.active, u.created_at, u.updated_at,
           w.name as workspace_name
    FROM expert_lift.users u
    LEFT JOIN expert_lift.workspaces w ON u.workspace_id = w.id
    ORDER BY u.full_name"
   {:params []}))

(defn create-user [username full-name password email phone role workspace-id]
  "Create new user with plain password (TODO: add proper hashing)"
  (postgres/execute-sql
   "INSERT INTO expert_lift.users (username, full_name, password_hash, email, phone, role, workspace_id, active, created_at, updated_at)
    VALUES ($1, $2, $3, $4, $5, $6::expert_lift.user_role, $7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id, username, full_name, email, phone, role, workspace_id, active, created_at, updated_at"
   {:params [username full-name password email phone role workspace-id]}))

(defn update-user [id username full-name email phone role active workspace-id]
  "Update user (without password)"
  (postgres/execute-sql
   "UPDATE expert_lift.users
    SET username = $1,
        full_name = $2,
        email = $3,
        phone = $4,
        role = $5::expert_lift.user_role,
        active = $6,
        workspace_id = $7,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $8
    RETURNING id, username, full_name, email, phone, role, workspace_id, active, created_at, updated_at"
   {:params [username full-name email phone role active workspace-id id]}))

(defn update-user-password [id password]
  "Update user password (TODO: add proper hashing)"
  (postgres/execute-sql
   "UPDATE expert_lift.users
    SET password_hash = $1,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $2
    RETURNING id"
   {:params [password id]}))

(defn delete-user [id]
  "Delete user"
  (postgres/execute-sql
   "DELETE FROM expert_lift.users
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn activate-user [id]
  "Activate user"
  (postgres/execute-sql
   "UPDATE expert_lift.users
    SET active = true, updated_at = CURRENT_TIMESTAMP
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn deactivate-user [id]
  "Deactivate user"
  (postgres/execute-sql
   "UPDATE expert_lift.users
    SET active = false, updated_at = CURRENT_TIMESTAMP
    WHERE id = $1
    RETURNING id"
   {:params [id]}))

(defn verify-password [username password]
  "Verify user password for login (TODO: add proper hashing)"
  (let [user (first (get-user-by-username username))]
    (when (and user (:active user))
      (when (= password (:password_hash user))
        (dissoc user :password_hash)))))