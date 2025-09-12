(ns features.app.teams.backend.db
  (:require [zero.backend.state.postgres :as postgres]
            [clojure.string :as str]))

(defn- normalize-hungarian
  "Normalize Hungarian characters for search - convert accented chars to base chars"
  [text]
  (when text
    (-> text
        (str/replace #"[áÁ]" "a")
        (str/replace #"[éÉ]" "e") 
        (str/replace #"[íÍ]" "i")
        (str/replace #"[óÓöÖőŐ]" "o")
        (str/replace #"[úÚüÜűŰ]" "u")
        (str/lower-case))))

(defn get-users-by-workspace
  "Get all users for a workspace"
  [workspace-id]
  (postgres/execute-sql 
   "SELECT id, username, full_name, email, phone, role, active, created_at, updated_at 
    FROM expert_lift.users 
    WHERE workspace_id = $1 
    ORDER BY full_name"
   {:params [workspace-id]}))

(defn get-users-paginated
  "Get users with server-side filtering, sorting, and pagination"
  [workspace-id {:keys [search sort-by sort-direction page page-size]
                 :or {sort-by "full_name" sort-direction "asc" page 0 page-size 10}}]
  (let [offset (* page page-size)
        has-search? (and search (not (str/blank? search)))
        search-condition (if has-search?
                          "AND (LOWER(full_name) LIKE $2 OR LOWER(username) LIKE $2 OR LOWER(email) LIKE $2)"
                          "")
        search-param (when has-search? (str "%" (str/lower-case search) "%"))
        order-direction (if (= sort-direction "desc") "DESC" "ASC")
        
        ;; Map frontend column names to database columns
        db-column (case sort-by
                    "user/full-name" "full_name"
                    "user/username" "username" 
                    "user/email" "email"
                    "user/role" "role"
                    "user/active" "active"
                    "full_name")
        
        ;; Build the query parameters correctly
        params (if has-search? 
                [workspace-id search-param page-size offset]
                [workspace-id page-size offset])
        
        query (str "SELECT id, username, full_name, email, phone, role, active, created_at, updated_at 
                   FROM expert_lift.users 
                   WHERE workspace_id = $1 " 
                   search-condition
                   " ORDER BY " db-column " " order-direction
                   " LIMIT $" (if has-search? "3" "2")
                   " OFFSET $" (if has-search? "4" "3"))
        
        count-query (str "SELECT COUNT(*) as total FROM expert_lift.users 
                         WHERE workspace_id = $1 " 
                         search-condition)
        count-params (if has-search? [workspace-id search-param] [workspace-id])]
    
    (let [users (postgres/execute-sql query {:params params})
          total-count (:total (first (postgres/execute-sql count-query {:params count-params})))]
      
      {:users users
       :total-count total-count
       :page page
       :page-size page-size
       :total-pages (Math/ceil (/ total-count page-size))})))

(defn get-user-by-id
  "Get user by ID (within workspace)"
  [user-id workspace-id]
  (postgres/execute-sql 
   "SELECT id, username, full_name, email, phone, role, active, created_at, updated_at 
    FROM expert_lift.users 
    WHERE id = $1 AND workspace_id = $2"
   {:params [user-id workspace-id]}))

(defn create-user
  "Create new user in workspace"
  [workspace-id username full-name email phone role password]
  (postgres/execute-sql 
   "INSERT INTO expert_lift.users (workspace_id, username, full_name, email, phone, role, password_hash, active, created_at, updated_at) 
    VALUES ($1, $2, $3, $4, $5, $6::expert_lift.user_role, $7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) 
    RETURNING id, username, full_name, email, phone, role, active, created_at, updated_at"
   {:params [workspace-id username full-name email phone role password]}))

(defn update-user
  "Update existing user (within workspace)"
  [user-id workspace-id username full-name email phone role active]
  (postgres/execute-sql 
   "UPDATE expert_lift.users 
    SET username = $1, full_name = $2, email = $3, phone = $4, role = $5::expert_lift.user_role, active = $6, updated_at = CURRENT_TIMESTAMP
    WHERE id = $7 AND workspace_id = $8
    RETURNING id, username, full_name, email, phone, role, active, created_at, updated_at"
   {:params [username full-name email phone role active user-id workspace-id]}))

(defn delete-user
  "Delete user (within workspace)"
  [user-id workspace-id]
  (postgres/execute-sql 
   "DELETE FROM expert_lift.users 
    WHERE id = $1 AND workspace_id = $2
    RETURNING id"
   {:params [user-id workspace-id]}))

(defn search-users-for-dropdown
  "Search users for dropdown - returns simplified data for UI"
  [workspace-id search-term limit]
  (let [has-search? (and search-term (not (str/blank? search-term)))
        search-condition (if has-search?
                          "AND (LOWER(full_name) LIKE $2 OR LOWER(username) LIKE $2)"
                          "")
        search-param (when has-search? (str "%" (str/lower-case search-term) "%"))
        params (if has-search? 
                [workspace-id search-param limit]
                [workspace-id limit])
        query (str "SELECT id, username, full_name, email, role 
                   FROM expert_lift.users 
                   WHERE workspace_id = $1 AND active = true " 
                   search-condition
                   " ORDER BY full_name 
                   LIMIT $" (if has-search? "3" "2"))]
    (postgres/execute-sql query {:params params})))