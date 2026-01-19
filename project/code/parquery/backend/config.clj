(ns parquery.backend.config
  (:require
   [users.backend.resolvers :as users]
   [users.backend.db :as user-db]
   [workspaces.backend.db :as workspace-db]
   [features.app.material-templates.backend.db :as material-templates-db]
   [features.app.addresses.backend.db :as addresses-db]
   [features.app.teams.backend.db :as teams-db]
   [features.app.worksheets.backend.db :as worksheets-db]
   [features.app.settings.backend.db :as settings-db]
   [features.app.pdf-generator.backend.handlers :as pdf-handlers]
   [cheshire.core]))

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

(defn can-edit-worksheet?
  "Check if user can edit a worksheet.
   - Admins/superadmins can edit any worksheet
   - Employees can only edit worksheets assigned to them"
  [request worksheet-id workspace-id]
  (let [user-roles (get-in request [:session :user-roles])
        user-id (get-in request [:session :user-id])]
    (println "DEBUG: can-edit-worksheet? check")
    (println "  User roles:" user-roles)
    (println "  User ID:" user-id)
    (println "  Worksheet ID:" worksheet-id)
    (cond
      ;; Admins and superadmins can edit any worksheet
      (some #{"admin" "superadmin"} user-roles)
      (do
        (println "  -> Admin/superadmin - allowed")
        true)

      ;; Employees can only edit their assigned worksheets
      :else
      (let [worksheet (first (worksheets-db/get-worksheet-by-id worksheet-id workspace-id))
            assigned-to (some-> worksheet :assigned_to_user_id str)]
        (println "  Worksheet assigned_to_user_id:" assigned-to)
        (println "  User ID (str):" (str user-id))
        (if (= assigned-to (str user-id))
          (do
            (println "  -> Employee assigned to worksheet - allowed")
            true)
          (do
            (println "  -> Employee NOT assigned - denied")
            false))))))

;; User Management Handlers for Expert Lift
;; Workspace Management Handlers
(defn get-all-workspaces
  "Get all workspaces"
  [{:parquery/keys [context request] :as params}]
  (try
    (workspace-db/get-all-workspaces)
    (catch Exception e
      (println "ERROR: get-all-workspaces failed:" (.getMessage e))
      [])))

(defn get-workspace-by-id
  "Get workspace by ID"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace/id params)]
    (try
      (first (workspace-db/get-workspace-by-id workspace-id))
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
    (users/get-all-users-fn)
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
  (let [{:user/keys [id username full-name email phone role active workspace-id password]} params]
    (try
      ;; Update user fields first
      (let [result (first (user-db/update-user id username full-name email phone role active workspace-id))]
        ;; If password is provided, update it separately
        (when (and password (seq (str password)))
          (user-db/update-user-password id password))
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
    (when user-id
      (try
        (users/get-user-by-id-fn user-id)
        (catch Exception e
          (println "Error fetching current user:" (.getMessage e))
          nil)))))

;; Workspace Material Templates Handlers
(defn get-workspace-material-templates
  "Get all active material templates for workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (when workspace-id
      (try
        (material-templates-db/get-material-templates-by-workspace workspace-id)
        (catch Exception e
          (println "ERROR: get-workspace-material-templates failed:" (.getMessage e))
          [])))))

(defn get-workspace-material-templates-paginated
  "Get material templates with pagination and filtering for workspace"
  [{:parquery/keys [context request] :as params}]
  (println "DEBUG: get-workspace-material-templates-paginated called")
  (println "  Context:" context)
  (println "  Query params:" (dissoc params :parquery/context :parquery/request))
  (let [workspace-id (:workspace-id context)
        query-params (dissoc params :parquery/context :parquery/request)]
    (if workspace-id
      (try
        (let [result (material-templates-db/get-material-templates-paginated workspace-id query-params)]
          (println "DEBUG: material-templates query result:" result)
          result)
        (catch Exception e
          (println "ERROR: get-workspace-material-templates-paginated failed:" (.getMessage e))
          (.printStackTrace e)
          {:material-templates [] :pagination {:total-count 0 :page 0 :page-size 10 :total-pages 0}}))
      {:material-templates [] :pagination {:total-count 0 :page 0 :page-size 10 :total-pages 0}})))

(defn get-workspace-material-template-by-id
  "Get material template by ID within workspace"
  [{:parquery/keys [context request] :as params}]
  (let [template-id (:material-template/id params)
        workspace-id (:workspace-id context)]
    (when (and template-id workspace-id)
      (try
        (first (material-templates-db/get-material-template-by-id template-id workspace-id))
        (catch Exception e
          (println "ERROR: get-workspace-material-template-by-id failed:" (.getMessage e))
          nil)))))

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

;; Helper functions for addresses
(defn parse-elevators-json
  "Parse elevators JSON string to Clojure data, return empty vector if invalid"
  [elevators-json]
  (try
    (if (and elevators-json (not= elevators-json "null"))
      (cheshire.core/parse-string elevators-json true)
      [])
    (catch Exception e
      (println "WARNING: Failed to parse elevators JSON:" elevators-json "Error:" (.getMessage e))
      [])))

(defn format-elevators-for-db
  "Format elevators vector as JSON string for database storage"
  [elevators]
  (if (and elevators (seq elevators))
    (cheshire.core/generate-string elevators)
    "[]"))

;; Workspace Addresses Handlers
(defn get-workspace-addresses
  "Get all addresses for workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (when workspace-id
      (try
        (addresses-db/get-addresses-by-workspace workspace-id)
        (catch Exception e
          (println "ERROR: get-workspace-addresses failed:" (.getMessage e))
          [])))))

(defn get-workspace-addresses-paginated
  "Get addresses with server-side filtering, sorting, and pagination"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        page-size (:page-size params 10)]
    (if workspace-id
      (try
        (addresses-db/get-addresses-paginated workspace-id
                                              {:search (:search params)
                                               :sort-by (:sort-by params)
                                               :sort-direction (:sort-direction params)
                                               :page (:page params 0)
                                               :page-size page-size})
        (catch Exception e
          (println "ERROR: get-workspace-addresses-paginated failed:" (.getMessage e))
          {:addresses []
           :pagination {:total-count 0 :page 0 :page-size page-size :total-pages 0}}))
      {:addresses []
       :pagination {:total-count 0 :page 0 :page-size page-size :total-pages 0}})))

(defn get-workspace-address-by-id
  "Get single address by ID within workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        address-id (:address/id params)]
    (when (and address-id workspace-id)
      (try
        (first (addresses-db/get-address-by-id address-id workspace-id))
        (catch Exception e
          (println "ERROR: get-workspace-address-by-id failed:" (.getMessage e))
          nil)))))

(defn search-workspace-addresses
  "Search addresses for dropdown - simplified data for UI"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (when workspace-id
      (try
        (addresses-db/search-addresses-for-dropdown workspace-id
                                                    (:search params "")
                                                    (:limit params 20))
        (catch Exception e
          (println "ERROR: search-workspace-addresses failed:" (.getMessage e))
          [])))))

;; Worksheet handlers
(defn get-workspace-worksheets
  "Get all worksheets for workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (when workspace-id
      (try
        (worksheets-db/get-worksheets-by-workspace workspace-id)
        (catch Exception e
          (println "ERROR: get-workspace-worksheets failed:" (.getMessage e))
          [])))))

(defn get-workspace-worksheets-paginated
  "Get worksheets with server-side filtering, sorting, and pagination"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        page-size (:page-size params 10)]
    (if workspace-id
      (try
        (worksheets-db/get-worksheets-paginated workspace-id
                                                {:search (:search params)
                                                 :sort-by (:sort-by params)
                                                 :sort-direction (:sort-direction params)
                                                 :page (:page params 0)
                                                 :page-size page-size})
        (catch Exception e
          (println "ERROR: get-workspace-worksheets-paginated failed:" (.getMessage e))
          {:worksheets []
           :pagination {:total-count 0 :page 0 :page-size page-size :total-pages 0}}))
      {:worksheets []
       :pagination {:total-count 0 :page 0 :page-size page-size :total-pages 0}})))

(defn get-workspace-worksheet-by-id
  "Get single worksheet by ID within workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        worksheet-id (:worksheet/id params)]
    (when (and worksheet-id workspace-id)
      (try
        (first (worksheets-db/get-worksheet-by-id worksheet-id workspace-id))
        (catch Exception e
          (println "ERROR: get-workspace-worksheet-by-id failed:" (.getMessage e))
          nil)))))

(defn create-workspace-worksheet
  "Create new worksheet in workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          user-id (get-in request [:session :user-id])
          creation-date (:worksheet/creation-date params)
          work-type (:worksheet/work-type params)
          service-type (:worksheet/service-type params)
          work-description (:worksheet/work-description params)
          notes (:worksheet/notes params)
          status (:worksheet/status params)
          address-id (:worksheet/address-id params)
          elevator-id (:worksheet/elevator-id params)
          assigned-to-user-id (:worksheet/assigned-to-user-id params)
          arrival-time (:worksheet/arrival-time params)
          departure-time (:worksheet/departure-time params)
          work-duration-hours (:worksheet/work-duration-hours params)
          maintainer-signature (:worksheet/maintainer-signature params)
          customer-signature (:worksheet/customer-signature params)]
      (if workspace-id
        (try
          (println "DEBUG: Attempting to create worksheet with workspace-id:" workspace-id)
          (let [material-usage (when-let [materials (:worksheet/material-usage params)]
                                  (cheshire.core/generate-string materials))
                result (first (worksheets-db/create-worksheet workspace-id creation-date 
                                                             work-type service-type work-description
                                                             material-usage notes status address-id elevator-id 
                                                             user-id assigned-to-user-id
                                                             arrival-time departure-time work-duration-hours
                                                             maintainer-signature customer-signature))]
            (println "DEBUG: Worksheet created successfully:" result)
            {:worksheet/id (str (:id result))
             :worksheet/serial-number (:serial_number result)
             :worksheet/creation-date (str (:creation_date result))
             :worksheet/work-type (:work_type result)
             :worksheet/service-type (:service_type result)
             :worksheet/work-description (:work_description result)
             :worksheet/notes (:notes result)
             :worksheet/status (:status result)
             :worksheet/arrival-time (str (:arrival_time result))
             :worksheet/departure-time (str (:departure_time result))
             :worksheet/work-duration-hours (:work_duration_hours result)
             :success true})
          (catch Exception e
            (println "Error creating workspace worksheet:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

(defn update-workspace-worksheet
  "Update existing worksheet in workspace.
   - Admins/superadmins can update any worksheet
   - Employees can only update worksheets assigned to them"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        id (:worksheet/id params)]
    (if (not workspace-id)
      {:success false :error "No workspace context"}
      (if (not (can-edit-worksheet? request id workspace-id))
        {:success false :error "Nincs jogosultságod szerkeszteni ezt a munkalapot"}
        (let [serial-number (:worksheet/serial-number params)
              creation-date (:worksheet/creation-date params)
              work-type (:worksheet/work-type params)
              service-type (:worksheet/service-type params)
              work-description (:worksheet/work-description params)
              notes (:worksheet/notes params)
              status (:worksheet/status params)
              address-id (:worksheet/address-id params)
              elevator-id (:worksheet/elevator-id params)
              assigned-to-user-id (:worksheet/assigned-to-user-id params)
              arrival-time (:worksheet/arrival-time params)
              departure-time (:worksheet/departure-time params)
              work-duration-hours (:worksheet/work-duration-hours params)
              maintainer-signature (:worksheet/maintainer-signature params)
              customer-signature (:worksheet/customer-signature params)]
          (try
            (let [result (first (worksheets-db/update-worksheet id workspace-id serial-number creation-date
                                                               work-type service-type work-description
                                                               nil notes status address-id elevator-id
                                                               assigned-to-user-id
                                                               arrival-time departure-time work-duration-hours
                                                               maintainer-signature customer-signature))]
              {:worksheet/id (str (:id result))
               :worksheet/serial-number (:serial_number result)
               :worksheet/creation-date (str (:creation_date result))
               :worksheet/work-type (:work_type result)
               :worksheet/service-type (:service_type result)
               :worksheet/work-description (:work_description result)
               :worksheet/notes (:notes result)
               :worksheet/status (:status result)
               :worksheet/arrival-time (str (:arrival_time result))
               :worksheet/departure-time (str (:departure_time result))
               :worksheet/work-duration-hours (:work_duration_hours result)
               :success true})
            (catch Exception e
              (println "Error updating workspace worksheet:" (.getMessage e))
              {:success false :error (parse-db-error (.getMessage e))})))))))

(defn delete-workspace-worksheet
  "Delete worksheet from workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          worksheet-id (:worksheet/id params)]
      (if workspace-id
        (try
          (worksheets-db/delete-worksheet worksheet-id workspace-id)
          {:success true :worksheet/id worksheet-id}
          (catch Exception e
            (println "Error deleting workspace worksheet:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

(defn create-workspace-address
  "Create new address in workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          name (:address/name params)
          address-line1 (:address/address-line1 params)
          address-line2 (:address/address-line2 params)
          city (:address/city params)
          postal-code (:address/postal-code params)
          country (:address/country params)
          contact-person (:address/contact-person params)
          contact-phone (:address/contact-phone params)
          contact-email (:address/contact-email params)
          elevators (:address/elevators params)
          elevators-json (format-elevators-for-db elevators)]
      (if workspace-id
        (try
          (println "DEBUG: Attempting to create address with workspace-id:" workspace-id)
          (let [result (first (addresses-db/create-address workspace-id name address-line1 address-line2 city postal-code country contact-person contact-phone contact-email elevators-json))]
            (println "DEBUG: Address created successfully:" result)
            {:address/id (str (:id result))
             :address/name (:name result)
             :address/address-line1 (:address_line1 result)
             :address/address-line2 (:address_line2 result)
             :address/city (:city result)
             :address/postal-code (:postal_code result)
             :address/country (:country result)
             :address/contact-person (:contact_person result)
             :address/contact-phone (:contact_phone result)
             :address/contact-email (:contact_email result)
             :address/elevators (parse-elevators-json (:elevators_json result))
             :address/workspace-id (str (:workspace_id result))
             :address/created-at (str (:created_at result))
             :address/updated-at (str (:updated_at result))
             :success true})
          (catch Exception e
            (println "Error creating workspace address:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

(defn update-workspace-address
  "Update existing address in workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          id (:address/id params)
          name (:address/name params)
          address-line1 (:address/address-line1 params)
          address-line2 (:address/address-line2 params)
          city (:address/city params)
          postal-code (:address/postal-code params)
          country (:address/country params)
          contact-person (:address/contact-person params)
          contact-phone (:address/contact-phone params)
          contact-email (:address/contact-email params)
          elevators (:address/elevators params)
          elevators-json (format-elevators-for-db elevators)]
      (if workspace-id
        (try
          (let [result (first (addresses-db/update-address id workspace-id name address-line1 address-line2 city postal-code country contact-person contact-phone contact-email elevators-json))]
            {:address/id (str (:id result))
             :address/name (:name result)
             :address/address-line1 (:address_line1 result)
             :address/address-line2 (:address_line2 result)
             :address/city (:city result)
             :address/postal-code (:postal_code result)
             :address/country (:country result)
             :address/contact-person (:contact_person result)
             :address/contact-phone (:contact_phone result)
             :address/contact-email (:contact_email result)
             :address/elevators (parse-elevators-json (:elevators_json result))
             :address/workspace-id (str (:workspace_id result))
             :address/created-at (str (:created_at result))
             :address/updated-at (str (:updated_at result))
             :success true})
          (catch Exception e
            (println "Error updating workspace address:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

(defn delete-workspace-address
  "Delete address from workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          address-id (:address/id params)]
      (if workspace-id
        (try
          (addresses-db/delete-address address-id workspace-id)
          {:success true :address/id address-id}
          (catch Exception e
            (println "Error deleting workspace address:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

;; Workspace Teams Handlers
(defn get-workspace-teams
  "Get all teams (users) for workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (when workspace-id
      (try
        (teams-db/get-users-by-workspace workspace-id)
        (catch Exception e
          (println "ERROR: get-workspace-teams failed:" (.getMessage e))
          [])))))

(defn get-workspace-teams-paginated
  "Get teams (users) with server-side filtering, sorting, and pagination"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        page-size (:page-size params 10)]
    (if workspace-id
      (try
        (teams-db/get-users-paginated workspace-id
                                      {:search (:search params)
                                       :sort-by (:sort-by params)
                                       :sort-direction (:sort-direction params)
                                       :page (:page params 0)
                                       :page-size page-size})
        (catch Exception e
          (println "ERROR: get-workspace-teams-paginated failed:" (.getMessage e))
          {:users []
           :pagination {:total-count 0 :page 0 :page-size page-size :total-pages 0}}))
      {:users []
       :pagination {:total-count 0 :page 0 :page-size page-size :total-pages 0}})))

(defn get-workspace-team-by-id
  "Get single team member (user) by ID within workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        user-id (:user/id params)]
    (when (and user-id workspace-id)
      (try
        (first (teams-db/get-user-by-id user-id workspace-id))
        (catch Exception e
          (println "ERROR: get-workspace-team-by-id failed:" (.getMessage e))
          nil)))))

(defn search-workspace-teams
  "Search teams (users) for dropdown - returns simplified data"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (when workspace-id
      (try
        (teams-db/search-users-for-dropdown workspace-id
                                            (:search params "")
                                            (:limit params 20))
        (catch Exception e
          (println "ERROR: search-workspace-teams failed:" (.getMessage e))
          [])))))

(defn create-workspace-team
  "Create new team member (user) in workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          username (:user/username params)
          full-name (:user/full-name params)
          email (:user/email params)
          phone (:user/phone params)
          role (:user/role params)
          password (:user/password params)]
      (if workspace-id
        (try
          (println "DEBUG: Attempting to create user with workspace-id:" workspace-id)
          (let [result (first (teams-db/create-user workspace-id username full-name email phone role password))]
            (println "DEBUG: User created successfully:" result)
            {:user/id (str (:id result))
             :user/username (:username result)
             :user/full-name (:full_name result)
             :user/email (:email result)
             :user/phone (:phone result)
             :user/role (:role result)
             :user/active (:active result)
             :user/created-at (str (:created_at result))
             :user/updated-at (str (:updated_at result))
             :success true})
          (catch Exception e
            (println "Error creating workspace team member:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

(defn update-workspace-team
  "Update existing team member (user) in workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          id (:user/id params)
          username (:user/username params)
          full-name (:user/full-name params)
          email (:user/email params)
          phone (:user/phone params)
          role (:user/role params)
          active (:user/active params)]
      (if workspace-id
        (try
          (let [result (first (teams-db/update-user id workspace-id username full-name email phone role active))]
            {:user/id (str (:id result))
             :user/username (:username result)
             :user/full-name (:full_name result)
             :user/email (:email result)
             :user/phone (:phone result)
             :user/role (:role result)
             :user/active (:active result)
             :user/created-at (str (:created_at result))
             :user/updated-at (str (:updated_at result))
             :success true})
          (catch Exception e
            (println "Error updating workspace team member:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

(defn delete-workspace-team
  "Delete team member (user) from workspace (admin+ only)"
  [{:parquery/keys [context request] :as params}]
  (if (has-admin-role? request)
    (let [workspace-id (:workspace-id context)
          user-id (:user/id params)]
      (if workspace-id
        (try
          (teams-db/delete-user user-id workspace-id)
          {:success true :user/id user-id}
          (catch Exception e
            (println "Error deleting workspace team member:" (.getMessage e))
            {:success false :error (parse-db-error (.getMessage e))}))
        {:success false :error "No workspace context"}))
    {:success false :error "Insufficient permissions"}))

;; Entity namespace mapping for wire format transformation
(def query->entity-ns
  "Maps query keys to entity namespaces for automatic wire->keys conversion.
   When a query key is in this map, the response will be automatically transformed
   from {:serial_number ...} to {:worksheet/serial-number ...}"
  {;; Worksheets
   :workspace-worksheets/get-all        "worksheet"
   :workspace-worksheets/get-paginated  "worksheet"
   :workspace-worksheets/get-by-id      "worksheet"
   ;; Addresses
   :workspace-addresses/get-all         "address"
   :workspace-addresses/get-paginated   "address"
   :workspace-addresses/get-by-id       "address"
   :workspace-addresses/search          "address"
   ;; Teams (users within workspace)
   :workspace-teams/get-all             "user"
   :workspace-teams/get-paginated       "user"
   :workspace-teams/get-by-id           "user"
   :workspace-teams/search              "user"
   ;; Material Templates
   :workspace-material-templates/get-all       "material-template"
   :workspace-material-templates/get-paginated "material-template"
   :workspace-material-templates/get-by-id     "material-template"
   ;; Workspaces
   :workspaces/get-all                  "workspace"
   :workspaces/get-by-id                "workspace"
   ;; Users (admin management)
   :user/current                        "user"
   :users/get-all                       "user"
   :current-user/basic-data             "user"})

(defn get-entity-ns
  "Returns the entity namespace for a query key, or nil if not mapped.
   Used by query-engine to determine if wire->keys transformation should be applied."
  [query-key]
  (get query->entity-ns query-key))

;; Query mappings to functions
(def read-queries
  "Read operations - mapped to handler functions"
  {:user/current #'get-current-user
   :users/get-all #'get-all-users
   :workspaces/get-all #'get-all-workspaces
   :workspaces/get-by-id #'get-workspace-by-id
   :current-user/basic-data #'get-current-user
   :workspace-material-templates/get-all #'get-workspace-material-templates
   :workspace-material-templates/get-paginated #'get-workspace-material-templates-paginated
   :workspace-material-templates/get-by-id #'get-workspace-material-template-by-id
   :workspace-addresses/get-all #'get-workspace-addresses
   :workspace-addresses/get-paginated #'get-workspace-addresses-paginated
   :workspace-addresses/get-by-id #'get-workspace-address-by-id
   :workspace-addresses/search #'search-workspace-addresses
   :workspace-teams/get-all #'get-workspace-teams
   :workspace-teams/get-paginated #'get-workspace-teams-paginated
   :workspace-teams/get-by-id #'get-workspace-team-by-id
   :workspace-teams/search #'search-workspace-teams
   :workspace-worksheets/get-all #'get-workspace-worksheets
   :workspace-worksheets/get-paginated #'get-workspace-worksheets-paginated
   :workspace-worksheets/get-by-id #'get-workspace-worksheet-by-id
   :workspace-worksheets/generate-pdf #'pdf-handlers/generate-worksheet-pdf
   :workspace-settings/get #'settings-db/get-workspace-settings})

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
   :workspace-material-templates/delete #'delete-workspace-material-template
   :workspace-addresses/create #'create-workspace-address
   :workspace-addresses/update #'update-workspace-address
   :workspace-addresses/delete #'delete-workspace-address
   :workspace-teams/create #'create-workspace-team
   :workspace-teams/update #'update-workspace-team
   :workspace-teams/delete #'delete-workspace-team
   :workspace-worksheets/create #'create-workspace-worksheet
   :workspace-worksheets/update #'update-workspace-worksheet
   :workspace-worksheets/delete #'delete-workspace-worksheet
   :workspace-settings/update #'settings-db/update-workspace-settings})

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