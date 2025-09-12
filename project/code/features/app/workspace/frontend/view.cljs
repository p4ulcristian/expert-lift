(ns features.app.workspace.frontend.view
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [translations.core :as tr]
            [zero.frontend.re-frame :as rf]))

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

(defn- workspace-header [workspace auth-user]
  "Workspace header with title and user info"
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :margin-bottom "3rem"}}
   [:div {:style {:text-align "center"}}
    [:h1 {:style {:color "#333" :margin "0" :margin-bottom "0.5rem"}}
     (:workspace/name workspace)]
    [:p {:style {:color "#72a9bf" :margin "0" :font-size "1rem" :font-style "italic" :margin-bottom "0.5rem"}}
     (get-random-welcome-message (:user/username auth-user))]
    [:p {:style {:color "#666" :margin "0" :font-size "1.1rem"}}
     (:workspace/description workspace)]]])


(defn- feature-card 
  ([icon title description]
   [feature-card icon title description nil])
  ([icon title description link-url]
   "Individual feature card component"
   (let [card-content [:div {:style {:border "1px solid #e0e0e0" :border-radius "8px" :padding "2rem" :text-align "center"
                                     :cursor (when link-url "pointer")
                                     :transition "box-shadow 0.2s"
                                     :box-shadow (when link-url "0 2px 4px rgba(0,0,0,0.1)")}}
                       [:div {:style {:font-size "2rem" :margin-bottom "1rem"}} icon]
                       [:h3 {:style {:color "#333" :margin-bottom "0.5rem"}} title]
                       [:p {:style {:color "#666" :font-size "0.9rem"}} description]]]
     (if link-url
       [:a {:href link-url
            :style {:text-decoration "none"}}
        card-content]
       card-content))))

(defn- features-grid [workspace-id]
  "Grid of feature cards"
  [:div {:style {:display "grid" :grid-template-columns "repeat(auto-fit, minmax(250px, 1fr))" :gap "2rem" :margin-top "3rem"}}
   [feature-card "🏗️" (tr/tr :features/material-templates) (tr/tr :features/material-templates-desc) (str "/app/" workspace-id "/material-templates")]
   [feature-card "📍" (tr/tr :features/addresses) (tr/tr :features/addresses-desc) (str "/app/" workspace-id "/addresses")]
   [feature-card "📋" (tr/tr :features/worksheets) (tr/tr :features/worksheets-desc) (str "/app/" workspace-id "/worksheets")]
   [feature-card "⚙️" (tr/tr :features/settings) (tr/tr :features/settings-desc) (str "/app/" workspace-id "/settings")]
   [feature-card "👥" (tr/tr :features/teams) (tr/tr :features/teams-desc) (str "/app/" workspace-id "/teams")]])

(defn- workspace-footer [workspace-id]
  "Footer with workspace ID"
  [:div {:style {:border-top "1px solid #eee" :padding-top "2rem" :margin-top "3rem"}}
   [:p {:style {:color "#888" :font-size "0.9rem"}}
    (str (tr/tr :dashboard/workspace-id) ": " workspace-id)]])

(defn- workspace-content [auth-user workspace workspace-id]
  "Main workspace dashboard content"
  [:div {:style {:min-height "100vh"
                 :background "#c9ddd8"
                 :padding "2rem"}}
   [:div {:style {:max-width "1200px"
                  :margin "0 auto"}}
    [workspace-header workspace auth-user]
    [:div {:style {:background "white" :border-radius "8px" :padding "3rem" :box-shadow "0 2px 4px rgba(0,0,0,0.1)" :text-align "center"}}
     [features-grid workspace-id]
     [workspace-footer workspace-id]]]])

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