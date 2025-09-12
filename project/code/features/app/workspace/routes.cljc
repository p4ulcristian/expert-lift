(ns features.app.workspace.routes
  #?(:cljs (:require [features.app.workspace.frontend.view :as workspace]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view]
                     [router.backend.middleware :refer [wrap-require-authentication]])))

(def workspace-path "/app/:workspace-id")

(def routes
  #?(:cljs [{:path workspace-path
             :view #'workspace/view
             :title "Workspace"}]
     :clj  [{:path workspace-path
             :get #'backend-view/response
             :middleware [wrap-require-authentication]}]))