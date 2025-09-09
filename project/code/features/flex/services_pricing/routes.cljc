(ns features.flex.services-pricing.routes
  #?(:cljs (:require [features.flex.services-pricing.frontend.view :as services]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def services-path "/flex/ws/:workspace-id/services")

(def routes
  #?(:cljs [{:path services-path
             :view #'services/view
             :title "Services"}]
     :clj  [{:path services-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 