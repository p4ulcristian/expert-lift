(ns features.app.workspace.routes
  #?(:cljs (:require [features.app.workspace.frontend.view :as workspace]
                     [features.app.material-templates.frontend.view :as material-templates]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view])))

(def workspace-path "/app/:workspace-id")
(def material-templates-path "/app/:workspace-id/material-templates")

(def routes
  #?(:cljs [{:path workspace-path
             :view #'workspace/view
             :title "Workspace"}
            {:path material-templates-path
             :view #'material-templates/view
             :title "Material Templates"}]
     :clj  [{:path workspace-path
             :get #'backend-view/response}
            {:path material-templates-path
             :get #'backend-view/response}]))