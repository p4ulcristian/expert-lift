(ns features.app.workspace.frontend.view
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]))

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
                   (when-not user
                     (set! (.-location js/window) "/login"))
                   (when-not (:user/workspace-id user)
                     (println "No workspace-id, redirecting to /app")
                     (set! (.-location js/window) "/app"))
                   (when (and (:user/workspace-id user) 
                             (not= (:user/workspace-id user) workspace-id))
                     (set! (.-location js/window) (str "/app/" (:user/workspace-id user))))))}))

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
                 (when (:success (:users/logout response))
                   (set! (.-location js/window) "/login")))}))

(defn- loading-screen []
  "Loading state component"
  [:div {:style {:min-height "100vh"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :background "#c9ddd8"}}
   [:div {:class "loading-spinner"}]])

(defn- workspace-header [workspace auth-user]
  "Workspace header with title and user info"
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :margin-bottom "3rem"}}
   [:div {:style {:text-align "center"}}
    [:h1 {:style {:color "#333" :margin "0" :margin-bottom "0.5rem"}}
     (:workspace/name workspace)]
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
   [feature-card "🏗️" "Material Templates" "Manage standard materials and supplies" (str "/app/" workspace-id "/material-templates")]
   [feature-card "📍" "Addresses" "Manage workspace addresses and locations" (str "/app/" workspace-id "/addresses")]
   [feature-card "📋" "Worksheets" "Manage work orders and service reports" (str "/app/" workspace-id "/worksheets")]
   [feature-card "👥" "Team" "Collaborate with your service team"]])

(defn- workspace-footer [workspace-id]
  "Footer with workspace ID"
  [:div {:style {:border-top "1px solid #eee" :padding-top "2rem" :margin-top "3rem"}}
   [:p {:style {:color "#888" :font-size "0.9rem"}}
    (str "Workspace ID: " workspace-id)]])

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
                 :background "#c9ddd8"}}
   [:div {:style {:text-align "center"}}
    [:h2 "Access Denied"]
    [:p "You don't have access to this workspace or it doesn't exist."]
    [:button {:on-click #(set! (.-location js/window) "/app")
              :style {:padding "0.5rem 1rem" :background "#72a9bf" :color "white" :border "none" :border-radius "4px" :cursor "pointer"}}
     "Go to Dashboard"]]])

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