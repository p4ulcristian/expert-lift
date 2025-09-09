(ns features.flex.inventory.routes
  #?(:cljs (:require [features.flex.inventory.frontend.inventory-item :as inventory-item]
                     [features.flex.inventory.frontend.inventory :as inventory])
     :clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def inventory-path "/flex/ws/:workspace-id/inventory")
(def inventory-item-path "/flex/ws/:workspace-id/inventory/:inventory-item-id")

(def routes
  #?(:cljs [{:path "/flex/ws/:workspace-id/inventory"
             :view inventory/view}
            {:path "/flex/ws/:workspace-id/inventory/:inventory-item-id"
             :view inventory-item/view}]
     :clj  [{:path inventory-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path inventory-item-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 