(ns features.app.settings.backend.db
  "Settings backend handlers for workspace configuration"
  (:require [zero.backend.state.postgres :as postgres]
            [workspaces.backend.db :as workspace-db]))

(defn get-workspace-settings
  "Get settings for workspace"
  [{:parquery/keys [context request] :as params}]
  (println "DEBUG: get-workspace-settings called with context:" context)
  (let [workspace-id (:workspace-id context)]
    (println "DEBUG: workspace-id from context:" workspace-id)
    (when workspace-id
      (try
        (let [workspace (first (workspace-db/get-workspace-by-id (java.util.UUID/fromString workspace-id)))]
          (println "DEBUG: workspace found:" workspace)
          {:workspace-id workspace-id
           :settings/general {:workspace/name (or (:name workspace) "My Workspace")
                             :workspace/timezone "UTC"
                             :workspace/language "en"}
           :settings/notifications {:email-notifications true
                                   :push-notifications false}
           :settings/security {:two-factor-enabled false
                              :session-timeout 8}})
        (catch Exception e
          (println "ERROR in get-workspace-settings:" (.getMessage e))
          (.printStackTrace e)
          {:workspace-id workspace-id
           :settings/general {:workspace/name "My Workspace"
                             :workspace/timezone "UTC"
                             :workspace/language "en"}
           :settings/notifications {:email-notifications true
                                   :push-notifications false}
           :settings/security {:two-factor-enabled false
                              :session-timeout 8}})))))

(defn update-workspace-settings
  "Update workspace settings"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        settings (:settings params)]
    (when (and workspace-id settings)
      (try
        ;; Update workspace name in database
        (let [workspace-name (get-in settings [:settings/general :workspace/name])
              current-workspace (first (workspace-db/get-workspace-by-id (Integer/parseInt workspace-id)))]
          (when (and workspace-name current-workspace)
            (workspace-db/update-workspace 
             (Integer/parseInt workspace-id)
             workspace-name
             (:description current-workspace)
             (:active current-workspace)))
          {:success true
           :settings settings})
        (catch Exception e
          (println "Error updating workspace settings:" (.getMessage e))
          {:success false
           :error (.getMessage e)})))))

(defn upload-workspace-logo
  "Upload workspace logo file"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        file-data (:file-data params)
        file-extension (:file-extension params)]
    (when (and workspace-id file-data file-extension)
      (try
        ;; Create uploads directory if it doesn't exist
        (let [upload-dir "uploads/logos"
              filename (str workspace-id "." file-extension)
              file-path (str upload-dir "/" filename)]
          ;; TODO: Implement actual file saving to filesystem
          ;; This would typically use Java file I/O to save the base64 data
          (println "Would save file to:" file-path)
          {:success true
           :file-path file-path
           :filename filename})
        (catch Exception e
          (println "Error uploading logo:" (.getMessage e))
          {:success false
           :error (.getMessage e)})))))