(ns features.flex.teams.frontend.team
  (:require
   [app.frontend.request :as request]
   [features.flex.shared.frontend.components.body :as body]
   [features.flex.teams.frontend.request :as teams-request]
   [clojure.string :as str]
   [reagent.core :refer [atom]]
   [ui.button :as button]
   [ui.text-field :as text-field]
   [zero.frontend.react :as zero-react]
   [zero.frontend.re-frame :refer [subscribe dispatch]]))

(defonce invitations-atom (atom []))
(defonce users-atom (atom []))
(defonce email-atom (atom ""))
(defonce role-atom (atom "employee"))
(defonce sending-invitation? (atom false))

;; Shared table components
(defn table-header-cell [text]
  [:th {:style {:padding "10px"
                :text-align "left"
                :border-bottom "2px solid #ddd"}} text])

(defn table-cell [content]
  [:td {:style {:padding "10px" :font-size "14px"}} content])

(defn human-readable-expiration [expires-at]
  (let [now (js/Date.)
        expiry-date (js/Date. expires-at)
        diff-ms (- (.getTime expiry-date) (.getTime now))
        diff-days (Math/ceil (/ diff-ms (* 1000 60 60 24)))]
    (cond
      (< diff-days 0) "Expired"
      (= diff-days 0) "Expires today"
      (= diff-days 1) "Expires tomorrow"
      :else (str "Expires in " diff-days " days"))))

(defn get-invitations [workspace-id]
  (teams-request/get-workspace-invitations
   workspace-id
   (fn [invitations]
     (reset! invitations-atom (or invitations [])))))


(defn get-users [workspace-id]
  (teams-request/get-workspace-users 
   workspace-id
   (fn [users]
     (reset! users-atom (or users [])))))

(defn remove-invitation [invitation-id workspace-id]
  (teams-request/remove-invitation 
   invitation-id 
   (fn [response]
     (if (:error response)
       (println "Error removing invitation:" (:error response))
       (get-invitations workspace-id)))))

(defn get-error-message [error response]
  (case error
    :insufficient-permissions "Only workspace owners can remove team members"
    :cannot-remove-self "You cannot remove yourself from the workspace"
    :user-not-member "User is not a member of this workspace"
    :unauthorized "You must be logged in to remove team members"
    :database-error "Database error occurred while removing member"
    (or (:message response) "Failed to remove team member")))

(defn handle-remove-success [workspace-id]
  (dispatch [:notifications/success! "remove-member" "Team member removed successfully"])
  (get-users workspace-id))

(defn handle-remove-error [response]
  (let [error-msg (get-error-message (:error response) response)]
    (dispatch [:notifications/error! "remove-member" error-msg])))

(defn remove-team-member [user-id workspace-id]
  (when (js/confirm "Are you sure you want to remove this team member? This action cannot be undone.")
    (teams-request/remove-team-member 
     user-id 
     workspace-id
     (fn [response]
       (if (:error response)
         (handle-remove-error response)
         (handle-remove-success workspace-id))))))

(defn invite-member [workspace-id]
  (when-not @sending-invitation?
    (reset! sending-invitation? true)
    (dispatch [:notifications/loading! "invite-member" "Sending invitation..."])
    (teams-request/invite-team-member 
     workspace-id 
     @email-atom 
     @role-atom 
     (fn [response]
       (reset! sending-invitation? false)
       (if (:error response)
         (dispatch [:notifications/error! "invite-member" "Failed to send invitation"])
         (do
           (reset! email-atom "")
           (reset! role-atom "employee")
           (dispatch [:notifications/success! "invite-member" "Invitation sent successfully!"])))
       (get-invitations workspace-id)
       (get-users workspace-id)))))

(defn form-label [text]
  [:label {:style {:display "block"
                   :margin-bottom "5px"
                   :font-size "14px"}}
   text])

(defn email-input-field []
  [:div {:style {:flex "1"}}
   [form-label "Email"]
   [text-field/view
    {:value @email-atom
     :on-change #(reset! email-atom %)
     :placeholder "team@example.com"}]])

(defn role-select-field []
  [:div
   [form-label "Role"]
   [:select {:value @role-atom
             :on-change #(reset! role-atom (.. ^js % -target -value))
             :style {:padding "8px"
                     :border "1px solid #ddd"
                     :border-radius "4px"}}
    [:option {:value "employee"} "Employee"]
    [:option {:value "owner"} "Owner"]]])

(defn invite-button [workspace-id]
  [:div
   [button/view
    {:on-click #(invite-member workspace-id)
     :disabled @sending-invitation?}
    (if @sending-invitation? "Sending..." "Send Invitation")]])

(defn invitation-form-fields [workspace-id]
  [:div {:style {:display "flex"
                 :gap "10px"
                 :align-items "flex-end"}}
   [email-input-field]
   [role-select-field]
   [invite-button workspace-id]])

(defn invitation-form [workspace-id]
  [:div {:style {:margin-bottom "30px"
                 :padding "20px"
                 :background "#f5f5f5"
                 :border-radius "8px"}}
   [:h2 {:style {:font-size "18px"
                 :margin-bottom "15px"}}
    "Invite Team Member"]
   [invitation-form-fields workspace-id]])

(defn invitation-remove-button [invitation-id workspace-id]
  [:button
   {:on-click #(remove-invitation invitation-id workspace-id)
    :style {:font-size "12px" 
            :padding "4px 8px"
            :background-color "#ffc107"
            :color "white"
            :border "none"
            :border-radius "4px"
            :cursor "pointer"}}
   "Remove"])

(defn invitation-table-row [invitation index workspace-id]
  [:tr {:style {:border-bottom "1px solid #eee"
                :background-color (if (even? index) "#f9f9f9" "#ffffff")}}
   [table-cell (:invitation/email invitation)]
   [table-cell (:invitation/role invitation)]
   [table-cell (:invitation/status invitation)]
   [table-cell (:invitation/inviter-name invitation)]
   [table-cell (human-readable-expiration (:invitation/expires-at invitation))]
   [:td {:style {:padding "10px"}}
    [invitation-remove-button (:invitation/id invitation) workspace-id]]])

(defn invitations-table-header []
  [:thead
   [:tr {:style {:background "#f0f0f0"}}
    [table-header-cell "Email"]
    [table-header-cell "Role"]
    [table-header-cell "Status"]
    [table-header-cell "Invited By"]
    [table-header-cell "Expires"]
    [table-header-cell "Actions"]]])

(defn invitations-table-body [invitations workspace-id]
  [:tbody
   (map-indexed
    (fn [index invitation]
      ^{:key (:invitation/id invitation)}
      [invitation-table-row invitation index workspace-id])
    invitations)])

(defn invitations-table-content [invitations workspace-id]
  [:table {:style {:width "100%"
                   :border-collapse "collapse"}}
   [invitations-table-header]
   [invitations-table-body invitations workspace-id]])

(defn invitations-table [invitations workspace-id]
  [:div
   [:h2 {:style {:font-size "18px"
                 :margin-bottom "15px"}}
    "Pending Invitations"]
   (if (empty? invitations)
     [:p {:style {:color "#666"}} "No pending invitations"]
     [invitations-table-content invitations workspace-id])])

(defn format-joined-date [joined-at]
  (when joined-at
    (subs joined-at 0 10)))

(defn format-user-role [role]
  (when role 
    (str/capitalize role)))

(defn user-action-cell [user workspace-id]
  [:button
   {:on-click #(remove-team-member (:user/id user) workspace-id)
    :style {:font-size "12px" 
            :padding "4px 8px"
            :background-color "#dc3545"
            :color "white"
            :border "none"
            :border-radius "4px"
            :cursor "pointer"}}
   "Remove"])

(defn user-table-row [user index is-owner? workspace-id]
  [:tr {:style {:border-bottom "1px solid #eee"
                :background-color (if (even? index) "#f9f9f9" "#ffffff")}}
   [table-cell (:user/display-name user)]
   [table-cell (:user/email user)]
   [table-cell (format-user-role (:user/role user))]
   [table-cell (format-joined-date (:user/joined-at user))]
   (when is-owner?
     [:td {:style {:padding "10px"}}
      [user-action-cell user workspace-id]])])

(defn users-table-header [is-owner?]
  [:thead
   [:tr {:style {:background "#f0f0f0"}}
    [table-header-cell "Name"]
    [table-header-cell "Email"]
    [table-header-cell "Role"]
    [table-header-cell "Joined"]
    (when is-owner?
      [table-header-cell "Actions"])]])

(defn users-table-body [users is-owner? workspace-id]
  [:tbody
   (map-indexed
    (fn [index user]
      ^{:key (:user/id user)}
      [user-table-row user index is-owner? workspace-id])
    users)])

(defn users-table-content [users is-owner? workspace-id]
  [:table {:style {:width "100%"
                   :border-collapse "collapse"}}
   [users-table-header is-owner?]
   [users-table-body users is-owner? workspace-id]])

(defn users-table [users workspace-id]
  (let [is-owner? (subscribe [:workspace/am-i-owner?])]
    [:div {:style {:margin-top "30px"}}
     [:h2 {:style {:font-size "18px"
                   :margin-bottom "15px"}}
      "Team Members"]
     (if (empty? users)
       [:p {:style {:color "#666"}} "No team members found"]
       [users-table-content users @is-owner? workspace-id])]))


(defn teams-content [workspace-id]
  [:div
   [invitation-form workspace-id]
   [invitations-table @invitations-atom workspace-id]
   [users-table @users-atom workspace-id]])

(defn view []
  (let [workspace-id @(subscribe [:workspace/get-id])]
    (zero-react/use-effect
     {:mount (fn []
               (when workspace-id
                 (get-invitations workspace-id)
                 (get-users workspace-id)))
      :params #js [workspace-id]})
    [body/view
     {:title "Team & Roles"
      :description "Manage team members and assign user roles."
      :body [teams-content workspace-id]}]))