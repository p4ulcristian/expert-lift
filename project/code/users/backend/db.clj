(ns users.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

;; User Global Roles Queries

(defn get-user-role [user-id]
  "Get single user role by user ID"
  (postgres/execute-sql
   "SELECT id, user_id, role, created_at, updated_at
    FROM user_global_roles
    WHERE user_id = $1"
   {:params [user-id]}))

(defn get-user-roles [user-id]
  "Get all user roles by user ID"
  (postgres/execute-sql
   "SELECT id, user_id, role, created_at, updated_at
    FROM user_global_roles
    WHERE user_id = $1"
   {:params [user-id]}))

(defn check-user-has-role [user-id role]
  "Check if user has specific role"
  (postgres/execute-sql
   "SELECT id, user_id, role, created_at, updated_at
    FROM user_global_roles
    WHERE user_id = $1 AND role = $2"
   {:params [user-id role]}))

(defn add-user-role [id user-id role]
  "Add role to user"
  (postgres/execute-sql
   "INSERT INTO user_global_roles (id, user_id, role, created_at, updated_at)
    VALUES ($1, $2, $3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id, user_id, role, created_at, updated_at"
   {:params [id user-id role]}))

(defn update-user-role [user-id role]
  "Update user role"
  (postgres/execute-sql
   "UPDATE user_global_roles
    SET role = $1, updated_at = CURRENT_TIMESTAMP
    WHERE user_id = $2
    RETURNING id, user_id, role, created_at, updated_at"
   {:params [role user-id]}))

(defn delete-user-role [user-id]
  "Delete user role"
  (postgres/execute-sql
   "DELETE FROM user_global_roles
    WHERE user_id = $1
    RETURNING id"
   {:params [user-id]}))

;; User OAuth Providers Queries

(defn create-oauth-provider [id user-id oauth-id provider]
  "Create OAuth provider record"
  (postgres/execute-sql
   "INSERT INTO user_oauth_providers (id, user_id, oauth_id, provider, created_at)
    VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP)
    RETURNING id"
   {:params [id user-id oauth-id provider]}))

(defn get-user-by-oauth-id [oauth-id]
  "Get user by OAuth ID"
  (postgres/execute-sql
   "SELECT u.id, u.first_name, u.last_name, u.email, u.picture_url, u.created_at, u.updated_at,
           uop.oauth_id, uop.provider
    FROM users u
    INNER JOIN user_oauth_providers uop ON u.id = uop.user_id
    WHERE uop.oauth_id = $1"
   {:params [oauth-id]}))

(defn get-user-by-email [email]
  "Get user by email"
  (postgres/execute-sql
   "SELECT id, first_name, last_name, email, picture_url, created_at, updated_at
    FROM users
    WHERE email = $1"
   {:params [email]}))

(defn get-user-oauth-providers [user-id]
  "Get all OAuth providers for user"
  (postgres/execute-sql
   "SELECT id, oauth_id, provider, created_at
    FROM user_oauth_providers
    WHERE user_id = $1
    ORDER BY created_at ASC"
   {:params [user-id]}))

(defn delete-oauth-provider [user-id oauth-id]
  "Delete OAuth provider"
  (postgres/execute-sql
   "DELETE FROM user_oauth_providers
    WHERE user_id = $1 AND oauth_id = $2
    RETURNING id"
   {:params [user-id oauth-id]}))

(defn update-oauth-provider [user-id oauth-id provider]
  "Update OAuth provider"
  (postgres/execute-sql
   "UPDATE user_oauth_providers
    SET provider = $1
    WHERE user_id = $2 AND oauth_id = $3
    RETURNING id"
   {:params [provider user-id oauth-id]}))

;; User Queries

(defn create-user [id first-name last-name email picture-url]
  "Create new user"
  (postgres/execute-sql
   "INSERT INTO users (id, first_name, last_name, email, picture_url, created_at, updated_at)
    VALUES ($1, $2, $3, $4, $5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    RETURNING id"
   {:params [id first-name last-name email picture-url]}))

(defn get-user-by-id [id]
  "Get user by ID"
  (postgres/execute-sql
   "SELECT id, first_name, last_name, email, picture_url, created_at, updated_at
    FROM users
    WHERE id = $1"
   {:params [id]}))

(defn get-all-users []
  "Get all users"
  (postgres/execute-sql
   "SELECT id, first_name, last_name, email, picture_url, created_at, updated_at
    FROM users
    ORDER BY id"
   {:params []}))

(defn update-user [id first-name last-name email picture-url]
  "Update user"
  (postgres/execute-sql
   "UPDATE users
    SET first_name = $1,
        last_name = $2,
        email = $3,
        picture_url = $4,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $5
    RETURNING id"
   {:params [first-name last-name email picture-url id]}))

(defn delete-user [id]
  "Delete user"
  (postgres/execute-sql
   "DELETE FROM users
    WHERE id = $1
    RETURNING id"
   {:params [id]}))