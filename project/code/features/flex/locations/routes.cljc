(ns features.flex.locations.routes
  #?(:cljs (:require [features.flex.locations.frontend.location :as  location]
                     [features.flex.locations.frontend.locations :as locations])
     :clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def locations-path "/flex/ws/:workspace-id/locations")
(def location-path "/flex/ws/:workspace-id/locations/:location-id")

(def routes
  #?(:cljs [{:path "/flex/ws/:workspace-id/locations"
             :view locations/view}
            {:path "/flex/ws/:workspace-id/locations/:location-id"
             :view location/view}]
     :clj  [{:path locations-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path location-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 