(ns features.site.orders.routes
  #?(:cljs (:require [features.site.orders.frontend.orders :as orders]
                     [features.site.orders.frontend.order :as order]))
  #?(:clj  (:require [features.site.zero.backend.view :as backend-view]
                     [features.site.orders.backend.resolvers :as orders-resolvers])))

(def orders-path "/orders")
(def order-path "/orders/:order-id")

(def routes
  #?(:cljs [{:path orders-path
             :view #'orders/view
             :title "Orders"}
            {:path order-path
             :view #'order/view
             :title "Order"}]
     :clj  [{:path orders-path
             :get #'backend-view/response
             :resolvers orders-resolvers/resolvers}
            {:path order-path
             :get #'backend-view/response
             :resolvers orders-resolvers/resolvers}
]))
