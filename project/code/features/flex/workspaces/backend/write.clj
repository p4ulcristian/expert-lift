(ns features.flex.workspaces.backend.write
  (:require
   [features.flex.workspaces.backend.db :as workspace-db]))

(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

(defn remove-workspace
  "Remove a workspace by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [user-id (or (:user-id context)
                      (get-user-id-from-request request))
          workspace-id (:workspace/id params)]
      (when-not user-id
        (throw (ex-info "User not authenticated" {:error :unauthorized})))
      (when-not workspace-id
        (throw (ex-info "Workspace ID is required" {:error :invalid-input})))

      (let [result (workspace-db/delete-workspace workspace-id)]
        (if result
          {:workspace/id workspace-id :success true}
          {:error "Workspace not found"})))
    (catch Exception e
      (println "Error deleting workspace:" (.getMessage e))
      {:error (.getMessage e)})))

(defn add-workspace 
  "Create a new workspace and assign the creator as owner"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (java.util.UUID/randomUUID)
          user-id (or (:user-id context)
                      (get-user-id-from-request request))
          workspace-name (:workspace/name params)]
      (when-not user-id
        (throw (ex-info "User not authenticated" {:error :unauthorized})))
      (when-not workspace-name
        (throw (ex-info "Workspace name is required" {:error :invalid-input})))
      
      ;; Create the workspace
      (workspace-db/create-workspace workspace-id workspace-name)
      
      ;; Create workspace share with owner role
      (let [share-id (java.util.UUID/randomUUID)]
        (workspace-db/share-workspace share-id workspace-id user-id "owner"))
      
      {:workspace/id workspace-id
       :workspace/name workspace-name})
    (catch Exception e
      (println "Error creating workspace:" (.getMessage e))
      {:error (.getMessage e)})))