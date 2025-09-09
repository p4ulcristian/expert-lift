(ns features.flex.machines.routes
  #?(:cljs (:require [features.flex.machines.frontend.machine :as machine]
                     [features.flex.machines.frontend.machines :as machines])
     :clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def machines-path "/flex/ws/:workspace-id/machines")
(def machine-path "/flex/ws/:workspace-id/machines/:machine-id")
(def machines-dashboard-path "/flex/ws/:workspace-id/machines-dashboard")

(def routes
  #?(:cljs [{:path "/flex/ws/:workspace-id/machines"
             :view machines/view}
            {:path "/flex/ws/:workspace-id/machines/:machine-id"
             :view machine/view}]
     :clj  [{:path machines-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path machines-dashboard-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path machine-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 