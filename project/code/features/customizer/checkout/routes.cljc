
(ns features.customizer.checkout.routes
  #?(:clj  (:require
             [features.customizer.zero.backend.view               :as backend-view]
             [authentication.middlewares.customizer       :as customizer-middleware]))
  #?(:cljs (:require 
             [features.customizer.checkout.frontend.views :as checkout])))
             

(def path "/checkout")

(def routes
  #?(:clj  [{:path       path
             :get        #'backend-view/response}]
            ;;  :middleware [customizer-middleware/require-customizer-role]}]
     :cljs [{:path path
             :view #'checkout/view}]))