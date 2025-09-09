(ns features.app.superadmin.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]))

(defn validate-user
  "Validates user data and returns map of field errors"
  [user is-new?]
  (let [errors {}
        username (str (:user/username user))
        full-name (str (:user/full-name user))
        email (str (:user/email user))
        password (str (:user/password user))
        role (:user/role user)]
    (cond-> errors
      (< (count (str/trim username)) 3)
      (assoc :user/username "Username must be at least 3 characters")
      
      (< (count (str/trim full-name)) 2)
      (assoc :user/full-name "Full name is required")
      
      (or (nil? role) (empty? (str role)))
      (assoc :user/role "Role is required")
      
      (and (not (empty? email))
           (not (re-matches #".+@.+\..+" email)))
      (assoc :user/email "Please enter a valid email address")
      
      (and is-new? (< (count (str/trim password)) 6))
      (assoc :user/password "Password must be at least 6 characters"))))

(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "bold"
                   :color (if has-error? "#dc3545" "inherit")}}
   label (when (#{:user/username :user/full-name :user/password} field-key) " *")])

(defn- field-input [field-key user has-error? attrs]
  [:input (merge attrs
                 {:value (str (get @user field-key ""))
                  :on-change #(swap! user assoc field-key (.. % -target -value))
                  :style (merge {:width "100%" :padding "0.5rem" :border-radius "4px"
                                 :border (str "1px solid " (if has-error? "#dc3545" "#ccc"))}
                                (:style attrs))})])

(defn- field-error [field-key errors]
  (when-let [error (get errors field-key)]
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error]))

(defn input-field [label field-key user errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1rem"}}
     [field-label label field-key has-error?]
     [field-input field-key user has-error? attrs]
     [field-error field-key errors]]))

(defn role-select [user errors]
  (let [has-error? (contains? errors :user/role)]
    [:div {:style {:margin-bottom "1rem"}}
     [field-label "Role" :user/role has-error?]
     [:select {:value (or (:user/role @user) "")
               :on-change #(swap! user assoc :user/role (.. % -target -value))
               :style {:width "100%" :padding "0.5rem" :border-radius "4px"
                       :border (str "1px solid " (if has-error? "#dc3545" "#ccc"))}}
      [:option {:value ""} "-- Select Role --"]
      [:option {:value "employee"} "Employee"]
      [:option {:value "admin"} "Admin"]
      [:option {:value "superadmin"} "Super Admin"]]
     [field-error :user/role errors]]))

(defn validation-summary [errors]
  (when (seq errors)
    [:div {:style {:background "#f8d7da" :border "1px solid #f5c6cb" :color "#721c24"
                   :padding "0.75rem" :border-radius "4px" :margin-bottom "1rem"}}
     "Please fix the errors above before saving."]))

(defn modal-buttons [is-valid? on-cancel on-save]
  [:div {:style {:display "flex" :gap "1rem" :justify-content "flex-end" :margin-top "2rem"}}
   [:button {:type "button" :on-click on-cancel
             :style {:padding "0.5rem 1rem" :border "1px solid #ccc" :background "#f5f5f5"
                     :border-radius "4px" :cursor "pointer"}}
    "Cancel"]
   [:button {:type "submit" :disabled (not is-valid?)
             :on-click (fn [e] (.preventDefault e) (when is-valid? (on-save)))
             :style {:padding "0.5rem 1rem" :border "none" :color "white" :border-radius "4px"
                     :background (if is-valid? "#007bff" "#6c757d")
                     :cursor (if is-valid? "pointer" "not-allowed")
                     :opacity (if is-valid? 1 0.6)}}
    "Save"]])

(defn modal-form [user is-new? errors is-valid? on-save]
  [:form {:on-submit (fn [e] (.preventDefault e) (when is-valid? (on-save)))}
   [input-field "Username" :user/username user errors {:type "text"}]
   [input-field "Full Name" :user/full-name user errors {:type "text"}]
   [input-field "Email" :user/email user errors {:type "email"}]
   [input-field "Phone" :user/phone user errors {:type "tel"}]
   [role-select user errors]
   (when is-new?
     [input-field "Password" :user/password user errors {:type "password"}])
   [validation-summary errors]])

(defn modal-header [is-new?]
  [:h2 {:style {:margin-bottom "1.5rem"}}
   (if is-new? "Add New User" "Edit User")])

(defn modal-backdrop [children]
  [:div {:style {:position "fixed" :top "0" :left "0" :width "100%" :height "100%"
                 :background "rgba(0, 0, 0, 0.5)" :display "flex" :align-items "center"
                 :justify-content "center" :z-index "1000"}}
   [:div {:style {:background "#fff" :border-radius "8px" :padding "2rem"
                  :width "500px" :max-width "90vw"}}
    children]])

(defn user-modal [user modal-open? on-save on-cancel]
  (when @modal-open?
    (let [is-new? (not (:user/id @user))
          current-errors (validate-user @user is-new?)
          is-valid? (empty? current-errors)]
      [modal-backdrop
       [:<>
        [modal-header is-new?]
        [modal-form user is-new? current-errors is-valid? on-save]
        [modal-buttons is-valid? on-cancel on-save]]])))

(defn- table-header [on-add]
  [:div {:style {:display "flex" :justify-content "space-between" :align-items "center" :margin-bottom "1.5rem"}}
   [:h2 "Users"]
   [:button {:on-click on-add :style {:padding "0.5rem 1rem" :background "#28a745" :color "white" :border "none" :border-radius "4px" :cursor "pointer"}} "Add User"]])

(defn- table-th [text align]
  [:th {:style {:padding "0.75rem" :text-align (or align "left") :border-bottom "1px solid #ddd"}} text])

(defn- role-badge [role]
  [:span {:style {:padding "0.25rem 0.5rem" :border-radius "4px" :font-size "0.875rem" :color "white"
                  :background (case role "superadmin" "#dc3545" "admin" "#fd7e14" "#28a745")}}
   role])

(defn- status-badge [active?]
  [:span {:style {:padding "0.25rem 0.5rem" :border-radius "4px" :font-size "0.875rem" :color "white"
                  :background (if active? "#28a745" "#6c757d")}}
   (if active? "Active" "Inactive")])

(defn- action-button [text color on-click margin?]
  [:button {:on-click on-click
            :style {:padding "0.25rem 0.5rem" :background color :color "white" :border "none"
                    :border-radius "4px" :cursor "pointer" :font-size "0.875rem"
                    :margin-right (when margin? "0.5rem")}}
   text])

(defn- user-row [user on-edit on-delete]
  ^{:key (:user/id user)}
  [:tr {:style {:border-bottom "1px solid #eee"}}
   [:td {:style {:padding "0.75rem"}}
    [:div (:user/full-name user)]
    [:div {:style {:font-size "0.8rem" :color "#666"}} (:user/username user)]]
   [:td {:style {:padding "0.75rem"}} (:user/email user)]
   [:td {:style {:padding "0.75rem"}} [role-badge (:user/role user)]]
   [:td {:style {:padding "0.75rem"}} [status-badge (:user/active user)]]
   [:td {:style {:padding "0.75rem" :text-align "center"}}
    [action-button "Edit" "#007bff" #(on-edit user) true]
    [action-button "Delete" "#dc3545" #(on-delete user) false]]])

(defn user-table [users on-add on-edit on-delete]
  [:div {:style {:background "white" :border-radius "8px" :padding "1.5rem" :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}}
   [table-header on-add]
   [:table {:style {:width "100%" :border-collapse "collapse"}}
    [:thead
     [:tr {:style {:background "#f8f9fa"}}
      [table-th "Name" nil]
      [table-th "Email" nil]
      [table-th "Role" nil]
      [table-th "Status" nil]
      [table-th "Actions" "center"]]]
    [:tbody
     (for [user users]
       [user-row user on-edit on-delete])]]])

(defn view []
  (let [users (r/atom [])
        current-user (r/atom {})
        modal-open? (r/atom false)
        loading? (r/atom true)
        
        load-users #(do (reset! loading? true)
                         (parquery/send-queries
                           {:queries {:users/get-all {}}
                            :parquery/context {}
                            :callback (fn [response]
                                        (reset! users (:users/get-all response))
                                        (reset! loading? false))}))
        
        save-user (fn [user-data is-new?]
                    (let [query-key (if is-new? :users/create :users/update)]
                      (parquery/send-queries
                        {:queries {query-key user-data}
                         :parquery/context {}
                         :callback (fn [response]
                                     (if (:success (get response query-key))
                                       (do (load-users) (reset! modal-open? false))
                                       (js/alert (str "Error: " (:error (get response query-key))))))})))
        
        delete-user-fn (fn [user]
                         (when (js/confirm (str "Delete user " (:user/full-name user) "?"))
                           (parquery/send-queries
                             {:queries {:users/delete {:user/id (:user/id user)}}
                              :parquery/context {}
                              :callback (fn [response]
                                          (if (:success (:users/delete response))
                                            (load-users)
                                            (js/alert (str "Error: " (:error (:users/delete response))))))})))]
    
    ;; Load users on component mount
    (load-users)
    
    (fn []
      [:div {:style {:min-height "100vh"
                     :background "#f5f5f5"
                     :padding "2rem"}}
       [:div {:style {:max-width "1200px"
                      :margin "0 auto"}}
        [:h1 {:style {:color "#333"
                      :margin-bottom "2rem"}}
         "Super Admin - User Management"]
        
        (if @loading?
          [:div {:style {:text-align "center" :padding "2rem"}}
           "Loading users..."]
          [user-table @users
           #(do (reset! current-user {:user/role "employee"}) (reset! modal-open? true))
           #(do (reset! current-user %) (reset! modal-open? true))
           delete-user-fn])
        
        [user-modal current-user modal-open?
         #(let [is-new? (not (:user/id @current-user))
                user @current-user
                base-data {:user/username (:user/username user)
                           :user/full-name (:user/full-name user)
                           :user/email (:user/email user)
                           :user/phone (:user/phone user)
                           :user/role (:user/role user)}
                user-data (if is-new?
                            (assoc base-data :user/password (:user/password user))
                            (assoc base-data :user/id (:user/id user) :user/active (:user/active user)))]
            (save-user user-data is-new?))
         #(reset! modal-open? false)]]])))