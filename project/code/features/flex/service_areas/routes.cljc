(ns features.flex.service-areas.routes
  #?(:cljs (:require [features.flex.service-areas.frontend.service-areas :as service-areas])
     :clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def service-area-path "/flex/ws/:workspace-id/service-area")

(def routes
  #?(:cljs [{:path service-area-path
             :view service-areas/view}]
     :clj  [{:path service-area-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}]))