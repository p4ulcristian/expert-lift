(ns features.site.profile.routes
  #?(:cljs (:require [features.site.profile.frontend.profile :as profile]))
  #?(:clj  (:require [features.site.zero.backend.view :as backend-view]
                     [authentication.middlewares.customizer :as customizer-middleware]
                     [features.site.profile.backend.resolvers :as profile-resolvers])))

(def profile-path "/profile")

(def routes
  #?(:cljs [{:path profile-path
             :view #'profile/view
             :title "Profile"}]
     :clj  [{:path profile-path
             :get #'backend-view/response
             :middleware [customizer-middleware/require-customizer-role]
             :resolvers profile-resolvers/resolvers}]))