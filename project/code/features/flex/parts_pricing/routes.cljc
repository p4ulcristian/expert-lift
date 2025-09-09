(ns features.flex.parts-pricing.routes
  #?(:cljs (:require [features.flex.parts-pricing.frontend.parts :as parts]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def parts-path "/flex/ws/:workspace-id/parts")

(def routes
  #?(:cljs [{:path parts-path
             :view #'parts/view
             :title "Parts"}]
     :clj  [{:path parts-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 