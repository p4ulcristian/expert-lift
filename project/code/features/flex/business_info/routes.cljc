(ns features.flex.business-info.routes
  #?(:cljs (:require [features.flex.business-info.frontend.view :as business-info]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def business-info-path "/flex/ws/:workspace-id/business-info")

(def routes
  #?(:cljs [{:path business-info-path
             :view #'business-info/view
             :title "Business Info"}]
     :clj  [{:path business-info-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}]))