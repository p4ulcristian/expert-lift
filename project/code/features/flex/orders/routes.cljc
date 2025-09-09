(ns features.flex.orders.routes
  #?(:cljs (:require [features.flex.orders.frontend.view :as orders]
                     [features.flex.orders.frontend.order :as order]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def orders-path "/flex/ws/:workspace-id/orders")
(def order-path "/flex/ws/:workspace-id/orders/:order-id")

(def routes
  #?(:cljs [{:path orders-path
             :view #'orders/view
             :title "Orders"}
            {:path order-path
             :view #'order/view
             :title "Order"}]
     :clj  [{:path orders-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path order-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}]))