(ns parquery.backend.config
  (:require
   [users.backend.resolvers :as users]
   [users.backend.db :as user-db]
   [workspaces.backend.db :as workspace-db]
   [features.app.material-templates.backend.db :as material-templates-db]))

;; Error handling helpers
(defn parse-db-error
  "Convert database error messages to user-friendly messages"
  [error-message]
  (cond
    (and error-message (.contains error-message "users_username_idx"))
    "Username already exists. Please choose a different username."
    
    (and error-message (.contains error-message "users_email_idx"))
    "Email address already exists. Please use a different email."
    
    (and error-message (.contains error-message "unique constraint"))
    "This value already exists. Please use a different value."
    
    :else
    "An error occurred. Please try again or contact support."))

;; Authorization helpers
(defn has-admin-role? [request]
  (let [user-roles (get-in request [:session :user-roles])
        session-data (:session request)]
    (println "DEBUG: has-admin-role? check")
    (println "  Session data:" session-data)
    (println "  User roles:" user-roles)
    (println "  Has admin/superadmin?" (some #{"admin" "superadmin"} user-roles))
    (some #{"admin" "superadmin"} user-roles)))

(defn has-superadmin-role? [request]
  (let [user-roles (get-in request [:session :user-roles])]
    (println "DEBUG: has-superadmin-role? check")
    (println "  User roles:" user-roles)
    (some #{"superadmin"} user-roles)))

;; User Management Handlers for Expert Lift
;; Workspace Management Handlers
(defn get-all-workspaces
  "Get all workspaces"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspaces (workspace-db/get-all-workspaces)]
      (mapv (fn [workspace]
             {:workspace/id (str (:id workspace))
              :workspace/name (:name workspace)
              :workspace/description (:description workspace)
              :workspace/active (:active workspace)
              :workspace/created-at (str (:created_at workspace))
              :workspace/updated-at (str (:updated_at workspace))})
           workspaces))
    (catch Exception e
      (println "ERROR: get-all-workspaces failed:" (.getMessage e))
      [])))

(defn get-workspace-by-id
  "Get workspace by ID"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace/id params)]
    (try
      (let [workspace (first (workspace-db/get-workspace-by-id workspace-id))]
        (when workspace
          {:workspace/id (str (:id workspace))
           :workspace/name (:name workspace)
           :workspace/description (:description workspace)
           :workspace/active (:active workspace)
           :workspace/created-at (str (:created_at workspace))
           :workspace/updated-at (str (:updated_at workspace))}))
      (catch Exception e
        (println "ERROR: get-workspace-by-id failed:" (.getMessage e))
        nil))))

(defn create-workspace
  "Create new workspace"
  [{:parquery/keys [context request] :as params}]
  (let [{:workspace/keys [name description]} params]
    (try
      (let [result (first (workspace-db/create-workspace name description))]
        {:workspace/id (str (:id result))
         :workspace/name (:name result)
         :workspace/description (:description result)
         :workspace/active (:active result)
         :workspace/created-at (str (:created_at result))
         :workspace/updated-at (str (:updated_at result))
         :success true})
      (catch Exception e
        (println "Error creating workspace:" (.getMessage e))
        {:success false :error (.getMessage e)}))))

(defn update-workspace
  "Update existing workspace"
  [{:parquery/keys [context request] :as params}]
  (let [{:workspace/keys [id name description active]} params]
    (try
      (let [result (first (workspace-db/update-workspace id name description active))]
        {:workspace/id (str (:id result))
         :workspace/name (:name result)
         :workspace/description (:description result)
         :workspace/active (:active result)
         :workspace/created-at (str (:created_at result))
         :workspace/updated-at (str (:updated_at result))
         :success true})
      (catch Exception e
        (println "Error updating workspace:" (.getMessage e))
        {:success false :error (.getMessage e)}))))

(defn delete-workspace
  "Delete workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace/id params)]
    (try
      (workspace-db/delete-workspace workspace-id)
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
              :user/workspace-name (:workspace_name user)
              :user/created-at (str (:created_at user))
              :user/updated-at (str (:updated_at user))})
           users))
    (catch Exception e
      (println "ERROR: get-all-users failed:" (.getMessage e))
      [])))

(defn create-user
  "Create new user"
  [{:parquery/keys [context request] :as params}]
  (let [{:user/keys [username full-name password email phone role workspace-id]} params]
    (try
      (let [result (first (user-db/create-user username full-name password email phone role workspace-id))]
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
        (let [error-msg (.getMessage e)]
          (println "Error creating user:" error-msg)
          {:success false :error (parse-db-error error-msg)})))))

(defn update-user
  "Update existing user"
  [{:parquery/keys [context request] :as params}]
  (let [{:user/keys [id username full-name email phone role active workspace-id]} params]
    (try
      (let [result (first (user-db/update-user id username full-name email phone role active workspace-id))]
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
    (println "DEBUG: login-user called")
    (println "  Username:" username)
    (println "  Password length:" (when password (count password)))
    (try
      (if-let [user (user-db/verify-password username password)]
        (let [session-data {:user-id (str (:id user))
                           :user-roles [(:role user)]
                           :workspace-id (when (:workspace_id user) (str (:workspace_id user)))}
              response {:success true
                       :user/id (:id user)
                       :user/username (:username user)
                       :user/full-name (:full_name user)
                       :user/role (:role user)
                       :user/workspace-id (when (:workspace_id user) (str (:workspace_id user)))
                       :session-data session-data}]
          (println "DEBUG: Login successful for user:" (:username user))
          (println "  User ID:" (:id user))
          (println "  User role:" (:role user))
          (println "  Workspace ID:" (:workspace_id user))
          (println "  Session data being set:" session-data)
          response)
        (do
          (println "DEBUG: Login failed - invalid credentials for username:" username)
          {:success false :error "Invalid username or password"}))
      (catch Exception e
        (println "Error during login:" (.getMessage e))
        (.printStackTrace e)
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

;; Workspace Material Templates Handlers
(defn get-workspace-material-templates
  "Get all active material templates for workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (if workspace-id
      (try
        (let [templates (material-templates-db/get-material-templates-by-workspace workspace-id)]
          (mapv (fn [template]
                 {:material-template/id (str (:id template))
                  :material-template/name (:name template)
                  :material-template/unit (:unit template)
                  :material-template/category (:category template)
                  :material-template/description (:description template)
                  :material-template/active (:active template)
                  :material-template/workspace-id (str (:workspace_id template))
                  :material-template/created-at (str (:created_at template))
                  :material-template/updated-at (str (:updated_at template))})
               templates))
        (catch Exception e
          (println "ERROR: get-workspace-material-templates failed:" (.getMessage e))
          []))
      (do
        (println "ERROR: No workspace-id in context")
        []))))

(defn get-workspace-material-template-by-id
  "Get material template by ID within workspace"
  [{:parquery/keys [context request] :as params}]
  (let [template-id (:material-template/id params)
        workspace-id (:workspace-id context)]
    (if (and template-id workspace-id)
      (try
        (let [template (first (material-templates-db/get-material-template-by-id template-id workspace-id))]
          (when template
            {:material-template/id (str (:id template))
             :material-template/name (:name template)
             :material-template/unit (:unit template)
             :material-template/category (:category template)
             :material-template/description (:description template)
             :material-template/active (:active template)
             :material-template/workspace-id (str (:workspace_id template))
             :material-template/created-at (str (:created_at template))
             :material-template/updated-at (str (:updated_at template))}))
        (catch Exception e
          (println "ERROR: get-workspace-material-template-by-id failed:" (.getMessage e))
          nil))
      nil)))

(defn create-workspace-material-template
  "Create new material template in workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (println "DEBUG: create-workspace-material-template called")
  (println "  Context:" context)
  (println "  Request keys:" (keys request))
  (println "  Params:" (dissoc params :parquery/request))
  (if (has-admin-role? request)
    (let [workspace-id (get-in request [:session :workspace-id])
          {:material-template/keys [name unit category description]} params]
      (println "DEBUG: Admin role check passed")
      (if workspace-id
        (try
          (println "DEBUG: Attempting to create material template with workspace-id:" workspace-id)
          (let [result (first (material-templates-db/create-material-template workspace-id name unit category description))]
            (println "DEBUG: Material template created successfully:" result)
            {:material-template/id (str (:id result))
             :material-template/name (:name result)
             :material-template/unit (:unit result)
             :material-template/category (:category result)
             :material-template/description (:description result)
             :material-template/active (:active result)
             :material-template/workspace-id (str (:workspace_id result))
             :material-template/created-at (str (:created_at result))
             :material-template/updated-at (str (:updated_at result))
             :success true})
          (catch Exception e
            (println "Error creating workspace material template:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        (do
          (println "DEBUG: No workspace-id in context - failing")
          {:success false :error "No workspace context"})))
    (do
      (println "DEBUG: Admin role check failed - insufficient permissions")
      {:success false :error "Insufficient permissions"})))

(defn update-workspace-material-template
  "Update existing material template in workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          {:material-template/keys [id name unit category description active]} params]
      (if workspace-id
        (try
          (let [result (first (material-templates-db/update-material-template id workspace-id name unit category description active))]
            {:material-template/id (str (:id result))
             :material-template/name (:name result)
             :material-template/unit (:unit result)
             :material-template/category (:category result)
             :material-template/description (:description result)
             :material-template/active (:active result)
             :material-template/workspace-id (str (:workspace_id result))
             :material-template/created-at (str (:created_at result))
             :material-template/updated-at (str (:updated_at result))
             :success true})
          (catch Exception e
            (println "Error updating workspace material template:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

(defn delete-workspace-material-template
  "Delete material template from workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          template-id (:material-template/id params)]
      (if workspace-id
        (try
          (material-templates-db/delete-material-template template-id workspace-id)
          {:success true :material-template/id template-id}
          (catch Exception e
            (println "Error deleting workspace material template:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

;; Query mappings to functions
(def read-queries
  "Read operations - mapped to handler functions"
  {:user/current #'get-current-user
   :users/get-all #'get-all-users
   :workspaces/get-all #'get-all-workspaces
   :workspaces/get-by-id #'get-workspace-by-id
   :current-user/basic-data #'get-current-user
   :workspace-material-templates/get-all #'get-workspace-material-templates
   :workspace-material-templates/get-by-id #'get-workspace-material-template-by-id})

(def write-queries
  "Write operations - mapped to handler functions"  
  {:users/create #'create-user
   :users/update #'update-user
   :users/delete #'delete-user
   :users/login #'login-user
   :users/logout #'logout-user
   :workspaces/create #'create-workspace
   :workspaces/update #'update-workspace
   :workspaces/delete #'delete-workspace
   :workspace-material-templates/create #'create-workspace-material-template
   :workspace-material-templates/update #'update-workspace-material-template
   :workspace-material-templates/delete #'delete-workspace-material-template})

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