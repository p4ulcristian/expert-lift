(ns features.flex.dashboard.backend.read
  (:require
   [users.backend.resolvers :as users]
   [features.flex.workspaces.backend.db :as workspace-db]
   [features.flex.orders.backend.db :as orders-db]))

(defn get-user-id-from-context
  "Extract user ID from context or request"
  [context request]
  (or (:user-id context) 
      (get-in request [:session :user-id])))

(defn get-recent-orders
  "Get recent orders for workspace"
  [{:parquery/keys [context] :as params}]
  (let [workspace-id (:workspace-id params)]
    (orders-db/get-recent-orders {:workspace_id workspace-id})))

(defn get-order-stats
  "Get order statistics for workspace"
  [{:parquery/keys [context] :as params}]
  {:total-orders 120
   :pending 10 
   :completed 110})

(defn get-workspace-name
  "Get workspace name by ID"
  [{:parquery/keys [context] :as params}]
  (let [workspace-id (:workspace-id params)
        workspace (workspace-db/get-workspace-by-id workspace-id)]
    (:name workspace)))

(defn get-client-name
  "Get current user's full name"
  [{:parquery/keys [context request] :as params}]
  (let [user-id (get-user-id-from-context context request)
        user (users/get-user-by-id-fn user-id)]
    (users/get-user-full-name user)))