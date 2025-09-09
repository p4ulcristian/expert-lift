(ns users.backend.resolvers
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [users.backend.db :as user-db]))

(defn get-user-full-name [user]
  (when user
    (:full_name user)))

;; Helper functions for direct database access
(defn get-user-by-id-fn [id]
  (first (user-db/get-user-by-id id)))

(defn get-user-by-email-fn [email]
  (first (user-db/get-user-by-email email)))

(defn get-all-users-fn []
  (user-db/get-all-users))

(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

;; Updated Pathom resolvers for Expert Lift schema
(pco/defresolver get-current-user-res 
  "Get current user data from session"
  [{:keys [request] :as _env} _input]
  {::pco/output [:current-user/data]}
  {:current-user/data 
   (let [user-id (get-user-id-from-request request)]
     (if user-id
       (try
         (let [user (get-user-by-id-fn user-id)]
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
       nil))})

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
  "Get all users for admin interface"
  [_env]
  {::pco/output [:users/list]}
  {:users/list 
   (let [users (get-all-users-fn)]
     (mapv (fn [user]
            {:user/id (:id user)
             :user/username (:username user)
             :user/full-name (:full_name user)
             :user/email (:email user)
             :user/phone (:phone user)
             :user/role (:role user)
             :user/active (:active user)
             :user/created-at (:created_at user)
             :user/updated-at (:updated_at user)})
          users))})

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
              :user/username (:username user)
              :user/full-name (:full_name user)
              :user/email (:email user)
              :user/phone (:phone user)
              :user/role (:role user)
              :user/active (:active user)}))
         (catch Exception e
           (println "Error fetching current user basic data:" (.getMessage e))
           nil))
       nil))})

(def resolvers [get-username-by-id
                get-user-email-by-id
                get-users-list-res
                get-current-user-res
                get-current-user-basic-res])