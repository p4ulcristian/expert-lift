(ns features.flex.teams.routes
  #?(:cljs (:require [features.flex.teams.frontend.team :as teams]
                     [features.flex.teams.frontend.invitation :as invitation]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def teams-path "/flex/ws/:workspace-id/team-roles")
(def invitation-path "/accept-invitation")

(def routes
  #?(:cljs [{:path teams-path
             :view #'teams/view
             :title "Team & Roles"}
            {:path invitation-path
             :view #'invitation/invitation-page
             :title "Accept Team Invitation"}]
     :clj  [{:path teams-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path invitation-path
             :get #'backend-view/response}]))