(ns features.flex.dashboard.routes
  #?(:cljs (:require [features.flex.dashboard.frontend.view :as home]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def path "/flex/ws/:workspace-id")

(def routes
  #?(:cljs [{:path path
             :view #'home/view
             :title "Dashboard"}]
     :clj  [{:path path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}]))