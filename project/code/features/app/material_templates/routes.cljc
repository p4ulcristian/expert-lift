(ns features.app.material-templates.routes
  #?(:cljs (:require [features.app.material-templates.frontend.view :as material-templates]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view]
                     [router.backend.middleware :refer [wrap-require-authentication]])))

(def material-templates-path "/app/:workspace-id/material-templates")

(def routes
  #?(:cljs [{:path material-templates-path
             :view #'material-templates/view
             :title "Material Templates"}]
     :clj  [{:path material-templates-path
             :get #'backend-view/response
             :middleware [wrap-require-authentication]}]))