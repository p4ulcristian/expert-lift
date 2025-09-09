(ns features.site.profile.backend.resolvers
  (:require
   [clojure.java.io :as io]
   [com.wsscode.pathom3.connect.operation :as pco]
   [zero.backend.state.postgres :as postgres]
   [features.site.profile.backend.db :as db]))

;; SQL functions are now in db.clj

(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

(pco/defresolver get-user-profile-res
  [{:keys [request] :as _env} _input]
  {::pco/output [{:site/user-profile [:id :email :name :picture_url :created_at :updated_at]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/user-profile (db/get-user-profile-by-id user-id)}
      {:site/user-profile nil})))

(pco/defresolver get-user-oauth-providers-res
  [{:keys [request] :as _env} _input]
  {::pco/output [{:site/user-oauth-providers [:id :oauth_id :provider :created_at]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/user-oauth-providers (db/get-user-oauth-providers-by-id user-id)}
      {:site/user-oauth-providers []})))

(def resolvers [get-user-profile-res get-user-oauth-providers-res])