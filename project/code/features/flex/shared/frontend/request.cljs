(ns features.flex.shared.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-user-data
  "Get user data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:user/get {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [user (:user/get response)]
                  (callback user)))}))

(defn fetch-generic-data
  "Fetch generic data using ParQuery"
  [workspace-id queries callback]
  (parquery/send-queries
   {:queries queries
    :parquery/context {:workspace-id workspace-id}
    :callback callback}))

(defn get-current-user
  "Get current user data using ParQuery (no workspace needed)"
  [callback]
  (parquery/send-queries
   {:queries {:user/current {}}
    :callback (fn [response]
                (let [user-data (:user/current response)]
                  (callback {:current-user/basic-data user-data})))}))