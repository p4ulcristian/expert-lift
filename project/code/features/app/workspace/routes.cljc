(ns features.app.workspace.routes
  #?(:cljs (:require [features.app.workspace.frontend.view :as workspace]
                     [features.app.material-templates.frontend.view :as material-templates]
                     [features.app.addresses.frontend.view :as addresses]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view])))

(def workspace-path "/app/:workspace-id")
(def material-templates-path "/app/:workspace-id/material-templates")
(def addresses-path "/app/:workspace-id/addresses")

(def routes
  #?(:cljs [{:path workspace-path
             :view #'workspace/view
             :title "Workspace"}
            {:path material-templates-path
             :view #'material-templates/view
             :title "Material Templates"}
            {:path addresses-path
             :view #'addresses/view
             :title "Addresses"}]
     :clj  [{:path workspace-path
             :get #'backend-view/response}
            {:path material-templates-path
             :get #'backend-view/response}
            {:path addresses-path
             :get #'backend-view/response}]))