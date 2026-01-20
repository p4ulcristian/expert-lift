(ns features.app.workspace.frontend.view
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [translations.core :as tr]
            [zero.frontend.re-frame :as rf]
            [ui.content-section :as content-section]))

(defn- get-workspace-id []
  "Get workspace ID from router parameters"
  (get-in @router/state [:parameters :path :workspace-id]))

(defn- check-auth-and-workspace-access [auth-user auth-loading? workspace-id]
  "Check authentication and workspace access permissions"
  (reset! auth-loading? true)
  (parquery/send-queries
    {:queries {:user/current {}}
     :parquery/context {}
     :callback (fn [response]
                 (println "Workspace auth check response:" response)
                 (let [user (:user/current response)]
                   (println "Current user in workspace:" user)
                   (println "User workspace-id:" (:user/workspace-id user))
                   (println "Expected workspace-id:" workspace-id)
                   (reset! auth-user user)
                   (reset! auth-loading? false)
))}))

(defn- load-workspace-data [workspace workspace-loading? workspace-id]
  "Load workspace information from the server"
  (when workspace-id
    (reset! workspace-loading? true)
    (parquery/send-queries
      {:queries {:workspaces/get-by-id {:workspace/id workspace-id}}
       :parquery/context {}
       :callback (fn [response]
                   (let [workspace-data (:workspaces/get-by-id response)]
                     (reset! workspace workspace-data)
                     (reset! workspace-loading? false)))})))

(defn- handle-logout []
  "Handle user logout"
  (parquery/send-queries
    {:queries {:users/logout {}}
     :parquery/context {}
     :callback (fn [response]
                 (:success (:users/logout response)))}))

(defn- loading-screen []
  "Loading state component"
  [:div {:style {:min-height "100vh"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :background "#c9ddd8"}}
   [:div {:class "loading-spinner"}]])

(defn- get-random-welcome-message [username]
  "Returns a random welcoming message personalized with username and elevator maintenance themes"
  (let [name (or username "there")
        welcome-keys [:welcome/ready-to-keep-running
                      :welcome/ensure-safe-comfortable
                      :welcome/elevate-service
                      :welcome/expertise-keeps-moving
                      :welcome/lift-standards
                      :welcome/vertical-excellence
                      :welcome/skilled-hands
                      :welcome/perfect-journey
                      :welcome/safety-reliability
                      :welcome/rise-to-challenges
                      :welcome/dedication-connects
                      :welcome/smooth-rides]
        prefix-keys [:welcome-prefix/welcome-back
                     :welcome-prefix/hello  
                     :welcome-prefix/good-morning
                     :welcome-prefix/great-to-see
                     :welcome-prefix/welcome
                     :welcome-prefix/good-to-see]
        random-prefix-key (rand-nth prefix-keys)
        welcome-prefix (str (tr/tr random-prefix-key) ", " name "! ")
        random-key (rand-nth welcome-keys)
        message-text (tr/tr random-key)]
    (str welcome-prefix message-text)))

(defn- feature-card
  "Individual feature card component with stats"
  [{:keys [icon title link-url color stats]}]
  (let [card-content
        [:div {:style {:border "none"
                       :border-radius "12px"
                       :padding "1.25rem"
                       :cursor (when link-url "pointer")
                       :transition "box-shadow 0.2s, transform 0.2s"
                       :box-shadow "0 4px 12px rgba(0,0,0,0.08)"
                       :background color
                       :width "100%"
                       :height "120px"
                       :display "flex"
                       :gap "1rem"
                       :align-items "flex-start"
                       :box-sizing "border-box"}}
         ;; Icon container
         [:div {:style {:background "rgba(255,255,255,0.2)"
                        :border-radius "10px"
                        :padding "0.75rem"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"}}
          [:i {:class icon
               :style {:font-size "1.5rem" :color "white"}}]]
         ;; Content
         [:div {:style {:flex 1}}
          [:h3 {:style {:color "white"
                        :margin "0 0 0.5rem 0"
                        :font-size "1rem"
                        :font-weight "600"
                        :text-transform "uppercase"
                        :letter-spacing "0.5px"}}
           title]
          (when stats
            [:div {:style {:display "flex" :flex-direction "column" :gap "0.25rem"}}
             (for [[label value] stats]
               ^{:key label}
               [:div {:style {:color "rgba(255,255,255,0.9)"
                              :font-size "0.85rem"
                              :display "flex"
                              :align-items "center"
                              :gap "0.5rem"}}
                [:span {:style {:font-weight "600"}} value]
                [:span {:style {:opacity "0.8"}} label]])])]]]
    (if link-url
      [:a {:href link-url
           :style {:text-decoration "none"
                   :display "block"
                   :height "100%"
                   :touch-action "manipulation"
                   :-webkit-tap-highlight-color "transparent"}}
       card-content]
      card-content)))

(defn- is-admin? [user]
  "Check if user has admin or superadmin role"
  (let [role (:user/role user)]
    (or (= role "admin") (= role "superadmin"))))

(defn- load-dashboard-stats [workspace-id stats-atom]
  "Load dashboard statistics"
  (parquery/send-queries
   {:queries {:dashboard/stats {}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (reset! stats-atom (:dashboard/stats response)))}))

(defn- features-grid [workspace-id user]
  "Grid of feature cards - shows admin features only for admins"
  (let [admin? (is-admin? user)
        stats (r/atom nil)]
    (load-dashboard-stats workspace-id stats)
    (fn []
      (let [s @stats]
        [:div {:style {:display "grid"
                       :grid-template-columns "repeat(auto-fit, minmax(280px, 1fr))"
                       :gap "1rem"
                       :margin-top "2rem"
                       :align-items "stretch"}}
         [feature-card {:icon "fa-solid fa-clipboard-list"
                        :title (tr/tr :features/worksheets)
                        :link-url (str "/app/" workspace-id "/worksheets")
                        :color "#6b8e9b"
                        :stats [[(tr/tr :dashboard/stat-in-progress) (:worksheets-in-progress s 0)]
                                [(tr/tr :dashboard/stat-draft) (:worksheets-draft s 0)]
                                [(tr/tr :dashboard/stat-completed) (:worksheets-completed s 0)]]}]
         [feature-card {:icon "fa-solid fa-location-dot"
                        :title (tr/tr :features/addresses)
                        :link-url (str "/app/" workspace-id "/addresses")
                        :color "#6b8e9b"
                        :stats [[(tr/tr :dashboard/stat-addresses) (:addresses-count s 0)]]}]
         [feature-card {:icon "fa-solid fa-cubes"
                        :title (tr/tr :features/material-templates)
                        :link-url (str "/app/" workspace-id "/material-templates")
                        :color "#6b8e9b"
                        :stats [[(tr/tr :dashboard/stat-templates) (:templates-count s 0)]]}]
         (when admin?
           [feature-card {:icon "fa-solid fa-users"
                          :title (tr/tr :features/teams)
                          :link-url (str "/app/" workspace-id "/teams")
                          :color "#6b8e9b"
                          :stats [[(tr/tr :dashboard/stat-members) (:team-members-count s 0)]]}])
         (when admin?
           [feature-card {:icon "fa-solid fa-gear"
                          :title (tr/tr :features/settings)
                          :link-url (str "/app/" workspace-id "/settings")
                          :color "#6b8e9b"
                          :stats nil}])
         [feature-card {:icon "fa-solid fa-comment"
                        :title (tr/tr :features/feedback)
                        :link-url (str "/app/" workspace-id "/feedback")
                        :color "#6b8e9b"
                        :stats nil}]]))))

(defn- workspace-content [auth-user _workspace workspace-id]
  "Main workspace dashboard content"
  [content-section/content-section
   [features-grid workspace-id auth-user]])

(defn- access-denied-screen []
  "Screen shown when access is denied"
  [:div {:style {:min-height "100vh"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :background "#c9ddd8"
                 :padding "2rem"}}
   [:div {:style {:background "white"
                  :border-radius "12px"
                  :padding "3rem 2.5rem"
                  :box-shadow "0 8px 24px rgba(0,0,0,0.15)"
                  :text-align "center"
                  :max-width "500px"
                  :width "100%"}}
    [:div {:style {:color "#dc3545" :font-size "4rem" :margin-bottom "1.5rem"}} "🚫"]
    [:h1 {:style {:color "#333" 
                  :margin "0 0 1rem 0" 
                  :font-size "2rem"
                  :font-weight "600"}} 
     (tr/tr :dashboard/access-denied)]
    [:p {:style {:color "#666" 
                 :margin "0 0 2rem 0" 
                 :font-size "1.1rem"
                 :line-height "1.5"}} 
     (tr/tr :dashboard/access-denied-message)]
    [:button {:on-click #(set! (.-location js/window) "/login")
              :style {:padding "0.75rem 2rem" 
                      :background "#72a9bf" 
                      :color "white" 
                      :border "none" 
                      :border-radius "8px" 
                      :cursor "pointer"
                      :font-size "1rem"
                      :font-weight "500"
                      :transition "background-color 0.2s"
                      :box-shadow "0 2px 4px rgba(114, 169, 191, 0.3)"}}
     (tr/tr :dashboard/go-to-login)]]])

(defn view []
  "Main workspace view component"
  (let [auth-user (r/atom nil)
        workspace (r/atom nil)
        auth-loading? (r/atom true)
        workspace-loading? (r/atom true)
        workspace-id (get-workspace-id)]
    
    ;; Check auth and load workspace on component mount
    (check-auth-and-workspace-access auth-user auth-loading? workspace-id)
    (load-workspace-data workspace workspace-loading? workspace-id)
    
    (fn []
      (cond
        (or @auth-loading? @workspace-loading?) [loading-screen]
        (and @auth-user @workspace) [workspace-content @auth-user @workspace workspace-id]
        :else [access-denied-screen]))))