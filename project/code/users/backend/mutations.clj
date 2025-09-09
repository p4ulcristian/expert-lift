(ns users.backend.mutations
  (:require
   [com.wsscode.pathom3.connect.operation :as pathom.co :refer [defmutation]]
   [users.backend.db :as user-db]))


(defn add-user-fn [_ {:user/keys [oauth-id first-name last-name email picture-url]
                      :as _props}]
  (try
    (let [user-id (java.util.UUID/randomUUID)]
      (user-db/create-user user-id first-name last-name email picture-url)
      {:user/id user-id
       :user/oauth-id oauth-id
       :user/first-name first-name
       :user/last-name last-name
       :user/email email
       :user/picture-url picture-url})
    (catch Exception e
      (println "Error creating user:" (.getMessage e))
      nil)))

(defmutation add-user [env mutation-props]
  {::pathom.co/op-name 'users/add-user!}
  (add-user-fn env mutation-props))

(def mutations [add-user])