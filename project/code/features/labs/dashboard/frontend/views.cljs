(ns features.labs.dashboard.frontend.views
  (:require
   [features.labs.shared.frontend.components.header :as header]
   [router.frontend.zero :as router]))

(defn dashboard-card [title icon path]
  [:div {:class "dashboard-card"
         :on-click #(router/navigate! {:path path})}
   [:div {:class "dashboard-card-content"}
    [:i {:class (str "fa-solid " icon " dashboard-card-icon")}]
    [:div {:class "dashboard-card-title"}
     title]]])

(defn view []
  [:div {:style {:display "flex"
                 :flex-direction "column"}}
   [header/view]
   [:div {:class "dashboard-container"}
    [:div {:class "dashboard-background"}]
    [:div {:class "dashboard-header"}
     [:h1 {:class "dashboard-welcome"}
      "Welcome, Ananda"]]
    [dashboard-card "Looks" "fa-palette" "/irunrainbow/looks"]
    [dashboard-card "Parts" "fa-puzzle-piece" "/irunrainbow/parts"]
    [dashboard-card "Forms" "fa-file-lines" "/irunrainbow/forms"]
    [dashboard-card "Services" "fa-cogs" "/irunrainbow/services"]
    [dashboard-card "Users" "fa-users" "/irunrainbow/users"] 
    [dashboard-card "Storage" "fa-folder" "/irunrainbow/storage"]
    [dashboard-card "Zip Codes" "fa-map-pin" "/irunrainbow/zip_codes"]
    [dashboard-card "Coating Partners" "fa-building" "/irunrainbow/workspaces"]
]]) 