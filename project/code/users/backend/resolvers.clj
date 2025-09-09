(ns users.backend.resolvers
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [users.backend.db :as user-db]
   [features.flex.workspaces.backend.db :as workspace-db]))


(defn get-user-full-name [user]
  (when user
    (str (:first_name user) " " (:last_name user))))

;; Helper functions for direct database access
(defn get-user-by-id-fn [id]
  (first (user-db/get-user-by-id id)))

(defn get-user-by-oauth-id-fn [oauth-id]
  (first (user-db/get-user-by-oauth-id oauth-id)))

(defn get-user-by-email-fn [email]
  (first (user-db/get-user-by-email email)))

(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))


;; Pathom resolvers
(pco/defresolver get-current-user-res 
  "Get current user data from session"
  [{:keys [request] :as _env} {:workspace/keys [id]}]
  {::pco/output [:current-user/data]}
  {:current-user/data 
   (let [user-id (get-user-id-from-request request)]
     (if user-id
       (try
         (let [user (get-user-by-id-fn user-id)
               workspace-role (when id (workspace-db/get-user-workspace-role user-id id))]
           (when user
             {:user/id (:id user)
              :user/first-name (:first_name user)
              :user/last-name (:last_name user)
              :user/email (:email user)
              :user/picture-url (:picture_url user)
              :user/full-name (get-user-full-name user)
              :user/workspace-role workspace-role}))
         (catch Exception e
           (println "Error fetching current user:" (.getMessage e))
           nil))
       nil))})

(pco/defresolver get-id-by-oauth-id 
  [{:user/keys [oauth-id]}] 
  {::pco/output [:user/id]}
  {:user/id 
   (let [user (get-user-by-oauth-id-fn oauth-id)]
     (:id user))})

(pco/defresolver get-username-by-id 
  [{:user/keys [id]}]
  {::pco/output [:user/name]}
  {:user/name 
   (let [user (get-user-by-id-fn id)]
     (get-user-full-name user))})

(pco/defresolver get-user-email-by-id 
  [{:user/keys [id]}]
  {::pco/output [:user/email]}
  {:user/email 
   (let [user (get-user-by-id-fn id)]
     (:email user))})

(pco/defresolver get-users-list-res
  "Get all users with their roles"
  [_env]
  {::pco/output [:users/list]}
  {:users/list 
   (let [users (user-db/get-all-users)]
     (mapv (fn [user]
            (let [roles (user-db/get-user-roles (:id user))]
              (assoc user :roles (mapv :role roles))))
          users))})

(pco/defresolver get-user-workspace-role-res 
  [{:user/keys [id] :as user} {:workspace/keys [id] :as workspace}]
  {::pco/output [:user/workspace-role]}
  {:user/workspace-role 
   (let [user-id (:user/id user)
         workspace-id (:workspace/id workspace)]
     (workspace-db/get-user-workspace-role user-id workspace-id))})

(pco/defresolver get-current-user-basic-res 
  "Get current user data without workspace context"
  [{:keys [request] :as _env} _resolver-props]
  {::pco/output [:current-user/basic-data]}
  {:current-user/basic-data 
   (let [user-id (get-user-id-from-request request)]
     (if user-id
       (try
         (let [user (get-user-by-id-fn user-id)]
           (when user
             {:user/id (:id user)
              :user/first-name (:first_name user)
              :user/last-name (:last_name user)
              :user/email (:email user)
              :user/picture-url (:picture_url user)
              :user/full-name (get-user-full-name user)}))
         (catch Exception e
           (println "Error fetching current user basic data:" (.getMessage e))
           nil))
       nil))})

(def resolvers [get-id-by-oauth-id
                get-username-by-id
                get-user-email-by-id
                get-users-list-res
                get-current-user-res
                get-current-user-basic-res
                get-user-workspace-role-res])
