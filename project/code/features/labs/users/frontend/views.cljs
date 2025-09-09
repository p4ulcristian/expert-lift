(ns features.labs.users.frontend.views
  (:require
   [features.labs.shared.frontend.components.header :as header]
   [app.frontend.request :as request]
   [ui.table.zero :as table]
   [zero.frontend.react :as zero-react]))

(defn get-users [set-users! set-loading!]
  (request/pathom-with-workspace-id
   {:query '[:users/list]
    :callback (fn [response]
                (println "response" response)
                (set-loading! false)
                (set-users! (:users/list response)))}))

(defn users-header []
  [:div {:class "users-header"}
   [:h1 "Users Management"]
   [:p {:style {:color "var(--ir-text-secondary)"
                :margin-top "8px"}}
    "Manage user accounts and permissions"]])

(defn roles-cell [_item user]
  (let [roles (:roles user)]
    (if (seq roles)
      [:div {:style {:display "flex"
                     :flex-wrap "wrap"
                     :gap "4px"}}
       (for [role roles]
         ^{:key role}
         [:span {:style {:background "#e5e7eb"
                        :color "#374151"
                        :padding "2px 8px"
                        :border-radius "4px"
                        :font-size "0.8em"
                        :text-transform "capitalize"}}
          role])]
      [:span {:style {:font-style "italic"
                     :color "#999"
                     :font-size "0.85em"}}
       "No roles"])))

(defn user-table-row-element [style content]
  [:div {:style (merge {:display "grid"
                        :gridTemplateColumns "2fr 2fr 1fr 2fr"
                        :alignItems "center"
                        :gap "8px"
                        :padding "8px 0"
                        :color "#000000"} style)}
   content])

(defn user-table [users]
  [table/view
   {:rows users
    :columns [:first_name :last_name :email :roles]
    :labels {:first_name "First Name"
             :last_name "Last Name"
             :email "Email"
             :roles "Roles"}
    :column-elements {:roles (fn [_item row] (roles-cell _item row))
                     :first_name (fn [_item row] [:span {:style {:color "#000000"}} (:first_name row)])
                     :last_name (fn [_item row] [:span {:style {:color "#000000"}} (:last_name row)])
                     :email (fn [_item row] [:span {:style {:color "#000000"}} (:email row)])}
    :row-element user-table-row-element}])

(defn users-content []
  (let [[users set-users!] (zero-react/use-state nil)
        [loading set-loading!] (zero-react/use-state true)]
    
    (zero-react/use-effect
     {:mount (fn []
               (get-users set-users! set-loading!))})
    
    [:div {:class "users-content"}
     (if loading
       [:div "Loading users..."]
       (if (seq users)
         [user-table users]
         [:div "No users found"]))]))

(defn view []
  [:div {:class "users-container"}
   [header/view]
   [:div {:style {:padding "40px"
                  :background "var(--ir-primary)"
                  :min-height "calc(100vh - 60px)"}}
    [users-header]
    [users-content]]])