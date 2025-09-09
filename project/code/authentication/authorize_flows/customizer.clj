(ns authentication.authorize-flows.customizer
  (:require
   [authentication.helpers :as helpers]
   [clojure.string :as str]
   [users.backend.resolvers :as user-resolvers]
   [users.backend.db :as user-db]))


(defn merge-to-session 
  "Merge new session data with existing session"
  [req new-session]
  (let [old-session (:session req)]
    (assoc req :session (merge old-session new-session))))

(defn create-user-from-token 
  "Create a new user from Auth0 token data and OAuth provider entry"
  [auth0-token]
  (when auth0-token
    (let [sub        (:sub auth0-token)
          email      (:email auth0-token)
          name       (:name auth0-token)
          picture    (:picture auth0-token)
          provider   (if sub (first (str/split sub #"\|")) "unknown")
          [first-name last-name] (if name 
                                   (str/split name #" " 2)
                                   ["" ""])
          user-id    (str (java.util.UUID/randomUUID))]
      ;; Create user without oauth_id
      (user-db/create-user user-id (or first-name "") (or last-name "") (or email "") picture)
      ;; Create OAuth provider entry
      (user-db/create-oauth-provider (str (java.util.UUID/randomUUID)) user-id sub provider)
      user-id)))

(defn ensure-customizer-role 
  "Ensure user has customizer role, assign if missing"
  [user-id]
  (let [user-roles (mapv :role (user-db/get-user-roles user-id))
        has-customizer? (some #(= "customizer" %) user-roles)]
    (when-not has-customizer?
      (user-db/add-user-role (str (java.util.UUID/randomUUID)) user-id "customizer"))))

(defn handle-customizer-authorization 
  "Handle customizer authorization flow - automatically creates users as customizers"
  [auth0-token redirect-url]
  (if (nil? auth0-token)
    (helpers/redirect-handler "/login/customizer")
    (let [sub         (:sub auth0-token)
          email       (:email auth0-token)
          user        (when sub (user-resolvers/get-user-by-oauth-id-fn sub))
          existing-user-by-email (when (and (not user) email) 
                                   (user-resolvers/get-user-by-email-fn email))
          user-id     (cond 
                        user (str (:id user))
                        existing-user-by-email 
                        ;; Link OAuth to existing user
                        (when sub
                          (user-db/create-oauth-provider (str (java.util.UUID/randomUUID)) 
                                                         (:id existing-user-by-email) 
                                                         sub 
                                                         (if sub (first (str/split sub #"\|")) "unknown"))
                          (str (:id existing-user-by-email)))
                        :else (create-user-from-token auth0-token))
          user-roles  (when user-id
                        (mapv :role (user-db/get-user-roles user-id)))]
      (cond 
        user-id
        (do 
          (ensure-customizer-role user-id)
          (merge-to-session
           (helpers/redirect-handler (or redirect-url "/"))
           {:user-roles user-roles
            :user-id   user-id
            :token     auth0-token}))
        
        :else 
        {:status  500
         :body    "Failed to create or retrieve user"
         :headers {"Content-type" "text/plain"}}))))