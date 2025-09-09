(ns features.site.orders.routes
  #?(:cljs (:require [features.site.orders.frontend.orders :as orders]
                     [features.site.orders.frontend.order :as order]))
  #?(:clj  (:require [features.site.zero.backend.view :as backend-view]
                     [authentication.middlewares.customizer :as customizer-middleware]
                     [features.site.orders.backend.resolvers :as orders-resolvers])))

(def orders-path "/orders")
(def order-path "/orders/:order-id")
(def stripe-checkout-path "/stripe-checkout")

(def routes
  #?(:cljs [{:path orders-path
             :view #'orders/view
             :title "Orders"}
            {:path order-path
             :view #'order/view
             :title "Order"}]
     :clj  [{:path orders-path
             :get #'backend-view/response
             :middleware [customizer-middleware/require-customizer-role]
             :resolvers orders-resolvers/resolvers}
            {:path order-path
             :get #'backend-view/response
             :middleware [customizer-middleware/require-customizer-role]
             :resolvers orders-resolvers/resolvers}
            {:path stripe-checkout-path
             :post #'orders-resolvers/handle-stripe-webhook}]))
