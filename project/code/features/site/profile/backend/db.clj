(ns features.site.profile.backend.db
  (:require 
    [zero.backend.state.postgres :as postgres]))

(defn get-user-profile-by-id
  "Get user profile information"
  [user-id]
  (first 
    (postgres/execute-sql 
     "SELECT 
        id,
        email,
        CONCAT(first_name, ' ', last_name) as name,
        picture_url,
        to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
      FROM users
      WHERE id = $1"
     {:params [user-id]})))

(defn get-user-oauth-providers-by-id
  "Get user's OAuth providers"
  [user-id]
  (postgres/execute-sql 
   "SELECT 
      id,
      oauth_id,
      provider,
      to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at
    FROM user_oauth_providers
    WHERE user_id = $1
    ORDER BY created_at ASC"
   {:params [user-id]}))