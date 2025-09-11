(ns parquery.backend.config
  (:require
   [users.backend.resolvers :as users]
   [users.backend.db :as user-db]))

;; User Management Handlers for Expert Lift
;; Workspace Management Handlers
(defn get-all-workspaces
  "Get all workspaces"
  [{:parquery/keys [context request] :as params}]
  (try
    ;; TODO: Replace with actual database query
    [{:workspace/id "123e4567-e89b-12d3-a456-426614174000"
      :workspace/name "Main Office"
      :workspace/description "Primary workspace for main office operations"
      :workspace/active true
      :workspace/created-at "2024-01-15T10:30:00Z"
      :workspace/updated-at "2024-01-15T10:30:00Z"}
     {:workspace/id "456e7890-e89b-12d3-a456-426614174001"
      :workspace/name "Remote Team"
      :workspace/description "Workspace for remote team members"
      :workspace/active true
      :workspace/created-at "2024-01-20T14:15:00Z"
      :workspace/updated-at "2024-01-20T14:15:00Z"}]
    (catch Exception e
      (println "ERROR: get-all-workspaces failed:" (.getMessage e))
      [])))

(defn create-workspace
  "Create new workspace"
  [{:parquery/keys [context request] :as params}]
  (let [{:workspace/keys [name description]} params]
    (try
      ;; TODO: Replace with actual database insert
      (let [new-workspace {:workspace/id (str (java.util.UUID/randomUUID))
                          :workspace/name name
                          :workspace/description description
                          :workspace/active true
                          :workspace/created-at (str (java.time.Instant/now))
                          :workspace/updated-at (str (java.time.Instant/now))}]
        (assoc new-workspace :success true))
      (catch Exception e
        (println "Error creating workspace:" (.getMessage e))
        {:success false :error (.getMessage e)}))))

(defn update-workspace
  "Update existing workspace"
  [{:parquery/keys [context request] :as params}]
  (let [{:workspace/keys [id name description active]} params]
    (try
      ;; TODO: Replace with actual database update
      (let [updated-workspace {:workspace/id id
                              :workspace/name name
                              :workspace/description description
                              :workspace/active active
                              :workspace/updated-at (str (java.time.Instant/now))}]
        (assoc updated-workspace :success true))
      (catch Exception e
        (println "Error updating workspace:" (.getMessage e))
        {:success false :error (.getMessage e)}))))

(defn delete-workspace
  "Delete workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace/id params)]
    (try
      ;; TODO: Replace with actual database delete
      {:success true :workspace/id workspace-id}
      (catch Exception e
        (println "Error deleting workspace:" (.getMessage e))
        {:success false :error (.getMessage e)}))))
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
              :user/workspace-id (when (:workspace_id user) (str (:workspace_id user)))
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
         :user/workspace-id (when (:workspace_id result) (str (:workspace_id result)))
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
         :user/workspace-id (when (:workspace_id result) (str (:workspace_id result)))
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
         :user/workspace-id (when (:workspace_id user) (str (:workspace_id user)))
         :session-data {:user-id (str (:id user))
                        :user-roles [(:role user)]
                        :workspace-id (when (:workspace_id user) (str (:workspace_id user)))}}
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
             :user/active (:active user)
             :user/workspace-id (when (:workspace_id user) (str (:workspace_id user)))}))
        (catch Exception e
          (println "Error fetching current user:" (.getMessage e))
          nil))
      nil)))

;; Query mappings to functions
(def read-queries
  "Read operations - mapped to handler functions"
  {:user/current #'get-current-user
   :users/get-all #'get-all-users
   :workspaces/get-all #'get-all-workspaces
   :current-user/basic-data #'get-current-user})

(def write-queries
  "Write operations - mapped to handler functions"  
  {:users/create #'create-user
   :users/update #'update-user
   :users/delete #'delete-user
   :users/login #'login-user
   :users/logout #'logout-user
   :workspaces/create #'create-workspace
   :workspaces/update #'update-workspace
   :workspaces/delete #'delete-workspace})

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