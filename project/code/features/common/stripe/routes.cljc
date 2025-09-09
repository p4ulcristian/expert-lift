(ns features.common.stripe.routes
  #?(:clj  (:require
            [features.common.stripe.backend.webhook :as stripe.webhook])))
  
(def path "/stripe-webhook")

(def routes
  #?(:clj  [{:path path
             :post #'stripe.webhook/handler}]
     :cljs []))