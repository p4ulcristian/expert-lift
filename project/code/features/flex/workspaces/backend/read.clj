(ns features.flex.workspaces.backend.read
  (:require
   [features.flex.workspaces.backend.db :as workspace-db]))

(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

(defn timestamp-to-string 
  "Convert timestamp to ISO string for JSON serialization"
  [timestamp]
  (when timestamp
    (str timestamp)))

(defn get-my-workspaces
  "Get all workspaces for the current user"
  [{:parquery/keys [context request] :as params}]
  (let [user-id (or (:user-id context)
                    (get-user-id-from-request request))]
    (when user-id
      (mapv (fn [workspace]
              {:workspace/id (:id workspace)
               :workspace/name (:name workspace)
               :workspace/created-at (timestamp-to-string (:created_at workspace))
               :workspace/updated-at (timestamp-to-string (:updated_at workspace))
               :workspace/role (:role workspace)
               :workspace/facility-state (:facility_state workspace)
               :workspace/facility-city (:facility_city workspace)})
            (workspace-db/get-my-workspaces user-id)))))

(defn get-workspace-name
  "Get workspace name by ID"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace/id params)
        workspace (workspace-db/get-workspace-by-id workspace-id)]
    (:name workspace)))

(defn get-workspace
  "Get complete workspace data including name, owner status, and user role"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id params)
        user-id (or (:user-id context)
                    (get-user-id-from-request request))
        workspace (workspace-db/get-workspace-by-id workspace-id)
        user-role (workspace-db/get-user-workspace-role user-id workspace-id)]
    (when workspace
      {:workspace/id workspace-id
       :workspace/name (:name workspace)
       :workspace/owner? (= user-role "owner")
       :workspace/role user-role})))