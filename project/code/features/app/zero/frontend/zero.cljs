(ns features.app.zero.frontend.zero
  (:require
   [features.app.homepage.routes :as homepage-routes]
   [features.app.superadmin.routes :as superadmin-routes]
   [features.app.login.routes :as login-routes]
   [router.frontend.zero :as router]))

(def routes (concat homepage-routes/routes
                    superadmin-routes/routes
                    login-routes/routes))

(defn view []
  (let [router-data (:data @router/state)]
    [:div
     [(:view router-data)]]))
