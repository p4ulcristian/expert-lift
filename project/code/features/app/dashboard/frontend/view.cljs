(ns features.app.dashboard.frontend.view
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]))

(defn- check-auth-and-redirect [auth-user auth-loading?]
  "Check authentication and redirect based on user workspace status"
  (reset! auth-loading? true)
  (parquery/send-queries
    {:queries {:user/current {}}
     :parquery/context {}
     :callback (fn [response]
                 (let [user (:user/current response)]
                   (reset! auth-user user)
                   (reset! auth-loading? false)
                   (when-not user
                     (set! (.-location js/window) "/login"))
                   (when (:user/workspace-id user)
                     (set! (.-location js/window) (str "/app/" (:user/workspace-id user))))))}))

(defn- handle-logout []
  "Handle user logout"
  (parquery/send-queries
    {:queries {:users/logout {}}
     :parquery/context {}
     :callback (fn [response]
                 (when (:success (:users/logout response))
                   (set! (.-location js/window) "/login")))}))

(defn- loading-screen []
  "Loading state component"
  [:div {:style {:min-height "100vh"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :background "#f5f5f5"}}
   [:div {:style {:text-align "center"}}
    "Loading..."]])

(defn- header-section [auth-user]
  "Header with title and user info"
  [:div {:style {:display "flex" :justify-content "space-between" :align-items "center" :margin-bottom "3rem"}}
   [:h1 {:style {:color "#333" :margin "0"}}
    "Expert Lift"]
   [:div {:style {:display "flex" :gap "1rem" :align-items "center"}}
    [:span {:style {:color "#666"}}
     (str "Welcome, " (:user/full-name auth-user))]
    [:button {:on-click handle-logout
              :style {:padding "0.5rem 1rem" :background "#dc3545" :color "white" :border "none" :border-radius "4px" :cursor "pointer"}}
     "Logout"]]])

(defn- not-invited-message []
  "Message for users not invited to any workspaces"
  [:div {:style {:margin-bottom "2rem"}}
   [:div {:style {:font-size "4rem" :margin-bottom "1rem"}} "🚫"]
   [:h2 {:style {:color "#333" :margin-bottom "1rem"}} "Not Invited to Any Workspaces"]
   [:p {:style {:color "#666" :font-size "1.1rem" :line-height "1.6" :max-width "500px" :margin "0 auto"}}
    "You haven't been invited to any workspaces yet. Please contact your administrator to be added to a workspace to access the application features."]])

(defn- help-section []
  "Help and support information"
  [:div {:style {:border-top "1px solid #eee" :padding-top "2rem" :margin-top "2rem"}}
   [:p {:style {:color "#888" :font-size "0.9rem"}}
    "Need help? Contact your system administrator or reach out to support."]])

(defn- main-content [auth-user]
  "Main dashboard content for users without workspace"
  [:div {:style {:min-height "100vh"
                 :background "#f5f5f5"
                 :padding "2rem"}}
   [:div {:style {:max-width "800px"
                  :margin "0 auto"}}
    [header-section auth-user]
    [:div {:style {:background "white" :border-radius "8px" :padding "3rem" :box-shadow "0 2px 4px rgba(0,0,0,0.1)" :text-align "center"}}
     [not-invited-message]
     [help-section]]]])

(defn- auth-required-screen []
  "Screen shown when authentication is required"
  [:div {:style {:min-height "100vh"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :background "#f5f5f5"}}
   [:div {:style {:text-align "center"}}
    [:h2 "Authentication Required"]
    [:p "Please log in to access this page."]
    [:button {:on-click #(set! (.-location js/window) "/login")
              :style {:padding "0.5rem 1rem" :background "#007bff" :color "white" :border "none" :border-radius "4px" :cursor "pointer"}}
     "Go to Login"]]])

(defn view []
  "Main dashboard view for users not invited to workspaces"
  (let [auth-user (r/atom nil)
        auth-loading? (r/atom true)]
    
    ;; Check auth on component mount
    (check-auth-and-redirect auth-user auth-loading?)
    
    (fn []
      (cond
        @auth-loading? [loading-screen]
        @auth-user [main-content @auth-user]
        :else [auth-required-screen]))))