(ns features.app.zero.backend.zero
  (:require [features.app.homepage.routes :as homepage-routes]
            [features.app.superadmin.routes :as superadmin-routes]))

(def routes
  (concat homepage-routes/routes
          superadmin-routes/routes))
