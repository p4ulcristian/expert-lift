(ns features.site.zero.backend.zero
  (:require [features.site.homepage.routes :as homepage-routes]
            [features.site.orders.routes :as orders-routes]
            [features.site.profile.routes :as profile-routes]))

(def routes
  (concat homepage-routes/routes
          orders-routes/routes
          profile-routes/routes))
