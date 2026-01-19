(ns ui.header
  (:require
   [reagent.core :as r]
   [ui.button :as button]
   [zero.frontend.re-frame :as rf]
   [parquery.frontend.request :as parquery]
   [router.frontend.zero :as router]
   [translations.core :as tr]))

;; Re-frame events and subscriptions
(rf/reg-event-db
 :header/set-language
 (fn [db [_ language]]
   (println "DEBUG: re-frame event :header/set-language called with:" language)
   (let [updated-db (assoc-in db [:header :language] language)]
     (println "DEBUG: Updated db header section:" (get-in updated-db [:header]))
     updated-db)))

(rf/reg-sub
 :header/current-language
 (fn [db _]
   (let [current-lang (get-in db [:header :language] :hu)]
     (println "DEBUG: subscription :header/current-language returning:" current-lang)
     current-lang)))



;; Initialize default language
(rf/dispatch [:header/set-language :hu])

;; Navigation helpers
(defn- get-workspace-id []
  "Get workspace ID from router parameters"
  (get-in @router/state [:parameters :path :workspace-id]))

(defn handle-logo-click []
  "Navigate to workspace dashboard when logo/title is clicked"
  (when-let [workspace-id (get-workspace-id)]
    (router/navigate! {:path (str "/app/" workspace-id)})))

;; Event handlers
(defn handle-logout []
  "Handle user logout"
  (parquery/send-queries
    {:queries {:users/logout {}}
     :callback (fn [response]
                 (when (:success (:users/logout response))
                   (set! (.-location js/window) "/login")))}))

;; Role translation helper
(defn- translate-role
  "Convert role string to translated label"
  [role]
  (case role
    "employee" (tr/tr :header/role-employee)
    "admin" (tr/tr :header/role-admin)
    "superadmin" (tr/tr :header/role-superadmin)
    role))

;; Data loading
(defn- load-header-data
  "Load user and workspace data for the header"
  [user-data workspace-data]
  (let [workspace-id (get-workspace-id)]
    (parquery/send-queries
     {:queries {:user/current {}}
      :parquery/context {}
      :callback (fn [response]
                  (when-let [user (:user/current response)]
                    (reset! user-data user)))}))
  (when-let [workspace-id (get-workspace-id)]
    (parquery/send-queries
     {:queries {:workspaces/get-by-id {:workspace/id workspace-id}}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (when-let [workspace (:workspaces/get-by-id response)]
                    (reset! workspace-data workspace)))})))

(defn header
  "Main application header with logo, user info, and logout button"
  []
  (let [user-data (r/atom nil)
        workspace-data (r/atom nil)]
    ;; Load data on mount
    (load-header-data user-data workspace-data)

    (fn []
      [:header.app-header
       [:div.header-content
        [:div.header-left
         {:on-click handle-logo-click
          :style {:cursor "pointer" :display "flex" :align-items "center"}}
         [:img.logo {:src "/logo/logo-256.webp" :alt "Logo"}]
         [:span.brand-name (tr/tr :header/brand)]]
        [:div.header-right
         ;; User info section
         (when (or @workspace-data @user-data)
           [:div.user-info-section
            (when @workspace-data
              [:span.workspace-name (:workspace/name @workspace-data)])
            (when @user-data
              [:div.user-info
               [:span.user-name (:user/full-name @user-data)]
               [:span.user-role (translate-role (:user/role @user-data))]])])
         ;; Logout button
         [button/view
          {:type :secondary
           :on-click handle-logout
           :class "logout-btn"}
          [:i {:class "fa-solid fa-right-from-bracket"}]]]]])))

(defn view
  "Header component view function"
  []
  [header])