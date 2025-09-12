(ns features.app.workspace.routes
  #?(:cljs (:require [features.app.workspace.frontend.view :as workspace]
                     [features.app.material-templates.frontend.view :as material-templates]
                     [features.app.addresses.frontend.view :as addresses]
                     [features.app.teams.frontend.view :as teams]
                     [features.app.worksheets.frontend.view :as worksheets]
                     [features.app.settings.frontend.view :as settings]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view])))

(def workspace-path "/app/:workspace-id")
(def material-templates-path "/app/:workspace-id/material-templates")
(def addresses-path "/app/:workspace-id/addresses")
(def teams-path "/app/:workspace-id/teams")
(def worksheets-path "/app/:workspace-id/worksheets")
(def settings-path "/app/:workspace-id/settings")

(def routes
  #?(:cljs [{:path workspace-path
             :view #'workspace/view
             :title "Workspace"}
            {:path material-templates-path
             :view #'material-templates/view
             :title "Material Templates"}
            {:path addresses-path
             :view #'addresses/view
             :title "Addresses"}
            {:path teams-path
             :view #'teams/view
             :title "Teams"}
            {:path worksheets-path
             :view #'worksheets/view
             :title "Worksheets"}
            {:path settings-path
             :view #'settings/settings-page
             :title "Settings"}]
     :clj  [{:path workspace-path
             :get #'backend-view/response}
            {:path material-templates-path
             :get #'backend-view/response}
            {:path addresses-path
             :get #'backend-view/response}
            {:path teams-path
             :get #'backend-view/response}
            {:path worksheets-path
             :get #'backend-view/response}
            {:path settings-path
             :get #'backend-view/response}]))