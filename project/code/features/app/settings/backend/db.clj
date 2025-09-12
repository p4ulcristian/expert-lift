(ns features.app.settings.backend.db
  "Settings backend handlers for workspace configuration"
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-workspace-settings
  "Get settings for workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)]
    (when workspace-id
      ;; Return basic settings structure for now
      {:workspace-id workspace-id
       :settings/general {:workspace/name "My Workspace"
                         :workspace/timezone "UTC"
                         :workspace/language "en"}
       :settings/notifications {:email-notifications true
                               :push-notifications false}
       :settings/security {:two-factor-enabled false
                          :session-timeout 8}})))

(defn update-workspace-settings
  "Update workspace settings"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        settings (:settings params)]
    (when (and workspace-id settings)
      ;; For now, just return the updated settings
      ;; In the future, this would save to database
      (println "Updating settings for workspace" workspace-id "with:" settings)
      {:success true
       :settings settings})))