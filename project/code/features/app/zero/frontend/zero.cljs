(ns features.app.zero.frontend.zero
  (:require
   [features.app.homepage.routes :as homepage-routes]
   [features.app.superadmin.routes :as superadmin-routes]
   [features.app.login.routes :as login-routes]
   [features.app.dashboard.routes :as dashboard-routes]
   [features.app.workspace.routes :as workspace-routes]
   [router.frontend.zero :as router]))

(def routes (concat homepage-routes/routes
                    superadmin-routes/routes
                    login-routes/routes
                    dashboard-routes/routes
                    workspace-routes/routes))

(defn view []
  (let [router-data (:data @router/state)]
    [:div
     [(:view router-data)]]))
