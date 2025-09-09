(ns features.flex.workspaces.routes
  #?(:cljs (:require [features.flex.workspaces.frontend.view :as workspaces]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def path "/flex")

(def routes
  #?(:cljs [{:path path
             :view #'workspaces/view
             :title "Workspaces"}]
     :clj  [{:path path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}]))