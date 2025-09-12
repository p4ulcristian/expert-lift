(ns features.app.zero.frontend.zero
  (:require
   [features.app.homepage.routes :as homepage-routes]
   [features.app.superadmin.routes :as superadmin-routes]
   [features.app.login.routes :as login-routes]
   [features.app.dashboard.routes :as dashboard-routes]
   [features.app.workspace.routes :as workspace-routes]
   [router.frontend.zero :as router]
   [zero.frontend.re-frame-viewer.view :as re-frame-viewer]
   [ui.header :as header]))

(def routes (concat homepage-routes/routes
                    superadmin-routes/routes
                    login-routes/routes
                    dashboard-routes/routes
                    workspace-routes/routes))

(defn view []
  (let [router-data (:data @router/state)
        current-path (:path @router/state)
        show-header? (not= current-path "/login")]
    [:div
     (when show-header?
       [header/view])
     [(:view router-data)]
     [re-frame-viewer/re-frame-viewer]
     [re-frame-viewer/keyboard-listener]]))
