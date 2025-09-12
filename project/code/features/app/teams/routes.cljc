(ns features.app.teams.routes
  #?(:cljs (:require [features.app.teams.frontend.view :as teams]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view]
                     [router.backend.middleware :refer [wrap-require-authentication]])))

(def teams-path "/app/:workspace-id/teams")

(def routes
  #?(:cljs [{:path teams-path
             :view #'teams/view
             :title "Teams"}]
     :clj  [{:path teams-path
             :get #'backend-view/response
             :middleware [wrap-require-authentication]}]))