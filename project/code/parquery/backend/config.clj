(ns parquery.backend.config
  (:require
   [users.backend.resolvers :as users]
   [users.backend.db :as user-db]))

;; User Management Handlers for Expert Lift
(defn get-all-users
  "Get all users for admin management"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [users (users/get-all-users-fn)]
      (mapv (fn [user]
             {:user/id (str (:id user))
              :user/username (:username user)  
              :user/full-name (:full_name user)
              :user/email (:email user)
              :user/phone (:phone user)
              :user/role (str (:role user))
              :user/active (:active user)
              :user/created-at (str (:created_at user))
              :user/updated-at (str (:updated_at user))})
           users))
    (catch Exception e
      (println "ERROR: get-all-users failed:" (.getMessage e))
      [])))

(defn create-user
  "Create new user"
  [{:parquery/keys [context request] :as params}]
  (let [{:user/keys [username full-name password email phone role]} params]
    (try
      (let [result (first (user-db/create-user username full-name password email phone role))]
        {:user/id (:id result)
         :user/username (:username result)
         :user/full-name (:full_name result)
         :user/email (:email result)
         :user/phone (:phone result)
         :user/role (:role result)
         :user/active (:active result)
         :success true})
      (catch Exception e
        (println "Error creating user:" (.getMessage e))
        {:success false :error (.getMessage e)}))))

(defn update-user
  "Update existing user"
  [{:parquery/keys [context request] :as params}]
  (let [{:user/keys [id username full-name email phone role active]} params]
    (try
      (let [result (first (user-db/update-user id username full-name email phone role active))]
        {:user/id (:id result)
         :user/username (:username result)
         :user/full-name (:full_name result)
         :user/email (:email result)
         :user/phone (:phone result)
         :user/role (:role result)
         :user/active (:active result)
         :success true})
      (catch Exception e
        (println "Error updating user:" (.getMessage e))
        {:success false :error (.getMessage e)}))))

(defn delete-user
  "Delete user"
  [{:parquery/keys [context request] :as params}]
  (let [user-id (:user/id params)]
    (try
      (user-db/delete-user user-id)
      {:success true :user/id user-id}
      (catch Exception e
        (println "Error deleting user:" (.getMessage e))
        {:success false :error (.getMessage e)}))))

(defn login-user
  "Authenticate user with username/password and create session"
  [{:parquery/keys [context request] :as params}]
  (let [{:user/keys [username password]} params]
    (try
      (if-let [user (user-db/verify-password username password)]
        {:success true
         :user/id (:id user)
         :user/username (:username user)
         :user/full-name (:full_name user)
         :user/role (:role user)
         :session-data {:user-id (str (:id user))
                        :user-roles [(:role user)]}}
        {:success false :error "Invalid username or password"})
      (catch Exception e
        (println "Error during login:" (.getMessage e))
        {:success false :error "Login failed"}))))

(defn logout-user
  "Clear user session"
  [{:parquery/keys [context request] :as params}]
  {:success true
   :session-data nil})

(defn get-current-user
  "Get current logged-in user data"
  [{:parquery/keys [context request] :as params}]
  (let [user-id (get-in request [:session :user-id])]
    (if user-id
      (try
        (let [user (users/get-user-by-id-fn user-id)]
          (when user
            {:user/id (:id user)
             :user/username (:username user)
             :user/full-name (:full_name user)
             :user/email (:email user)
             :user/phone (:phone user)
             :user/role (:role user)
             :user/active (:active user)}))
        (catch Exception e
          (println "Error fetching current user:" (.getMessage e))
          nil))
      nil)))

;; Query mappings to functions
(def read-queries
  "Read operations - mapped to handler functions"
  {:user/current #'get-current-user
   :users/get-all #'get-all-users
   :current-user/basic-data #'get-current-user})

(def write-queries
  "Write operations - mapped to handler functions"  
  {:users/create #'create-user
   :users/update #'update-user
   :users/delete #'delete-user
   :users/login #'login-user
   :users/logout #'logout-user})

(defn get-query-type
  "Returns query type based on config"
  [query-key]
  (cond
    (contains? read-queries query-key) :read
    (contains? write-queries query-key) :write
    (= "parquery" (namespace query-key)) :parquery
    :else :unknown))

(defn get-query-handler
  "Returns the handler function for a query"
  [query-key]
  (or (get read-queries query-key)
      (get write-queries query-key)))