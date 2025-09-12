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
      (assoc :user/password "Password must be at least 6 characters")
      
      (and (not is-new?) (seq (str/trim password)) (< (count (str/trim password)) 6))
      (assoc :user/password "New password must be at least 6 characters"))))

(defn validate-workspace
  "Validates workspace data and returns map of field errors"
  [workspace]
  (let [errors {}
        name (str (:workspace/name workspace))]
    (cond-> errors
      (< (count (str/trim name)) 2)
      (assoc :workspace/name "Workspace name is required"))))

(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "bold"
                   :color (if has-error? "#dc3545" "inherit")}}
   label (when (#{:user/username :user/full-name :user/password} field-key) " *")])

(defn- field-input [field-key user has-error? attrs]
  (if (= (:type attrs) "textarea")
    [:textarea (merge (dissoc attrs :type)
                      {:value (str (get @user field-key ""))
                       :on-change #(swap! user assoc field-key (.. % -target -value))
                       :style (merge {:width "100%" :padding "0.5rem" :border-radius "4px"
                                      :border (str "1px solid " (if has-error? "#dc3545" "#ccc"))
                                      :min-height "80px" :resize "vertical"}
                                     (:style attrs))})]
    [:input (merge attrs
                   {:value (str (get @user field-key ""))
                    :on-change #(swap! user assoc field-key (.. % -target -value))
                    :style (merge {:width "100%" :padding "0.5rem" :border-radius "4px"
                                   :border (str "1px solid " (if has-error? "#dc3545" "#ccc"))}
                                  (:style attrs))})]))

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

(defn workspace-select [user errors workspaces]
  (let [has-error? (contains? errors :user/workspace-id)]
    [:div {:style {:margin-bottom "1rem"}}
     [field-label "Workspace" :user/workspace-id has-error?]
     [:select {:value (or (:user/workspace-id @user) "")
               :on-change #(swap! user assoc :user/workspace-id (.. % -target -value))
               :style {:width "100%" :padding "0.5rem" :border-radius "4px"
                       :border (str "1px solid " (if has-error? "#dc3545" "#ccc"))}}
      [:option {:value ""} "-- No Workspace --"]
      (for [workspace workspaces]
        [:option {:key (:workspace/id workspace) :value (:workspace/id workspace)} 
         (:workspace/name workspace)])]
     [field-error :user/workspace-id errors]]))

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

(defn modal-form [user is-new? errors is-valid? on-save workspaces]
  [:form {:on-submit (fn [e] (.preventDefault e) (when is-valid? (on-save)))}
   [input-field "Username" :user/username user errors {:type "text"}]
   [input-field "Full Name" :user/full-name user errors {:type "text"}]
   [input-field "Email" :user/email user errors {:type "email"}]
   [input-field "Phone" :user/phone user errors {:type "tel"}]
   [role-select user errors]
   [workspace-select user errors workspaces]
   [input-field (if is-new? "Password" "New Password (leave empty to keep current)") 
                :user/password user errors {:type "password" :placeholder (when-not is-new? "Leave empty to keep current password")}]
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

(defn user-modal [user modal-open? on-save on-cancel workspaces]
  (when @modal-open?
    (let [is-new? (not (:user/id @user))
          current-errors (validate-user @user is-new?)
          is-valid? (empty? current-errors)]
      [modal-backdrop
       [:<>
        [modal-header is-new?]
        [modal-form user is-new? current-errors is-valid? on-save workspaces]
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
  [:tr {:key (:user/id user)
        :style {:border-bottom "1px solid #eee"}}
   [:td {:style {:padding "0.75rem"}}
    [:div (:user/full-name user)]
    [:div {:style {:font-size "0.8rem" :color "#666"}} (:user/username user)]]
   [:td {:style {:padding "0.75rem"}} (:user/email user)]
   [:td {:style {:padding "0.75rem"}} [role-badge (:user/role user)]]
   [:td {:style {:padding "0.75rem"}} 
    (if (:user/workspace-name user)
      [:span {:style {:font-size "0.875rem" :color "#666"}} 
       (:user/workspace-name user)]
      [:span {:style {:font-size "0.875rem" :color "#999"}} "None"])]
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
      [table-th "Workspace" nil]
      [table-th "Status" nil]
      [table-th "Actions" "center"]]]
    [:tbody
     (for [user users]
       [user-row user on-edit on-delete])]]])

;; Workspace Components
(defn workspace-modal-form [workspace errors is-valid? on-save]
  [:form {:on-submit (fn [e] (.preventDefault e) (when is-valid? (on-save)))}
   [input-field "Name" :workspace/name workspace errors {:type "text"}]
   [input-field "Description" :workspace/description workspace errors {:type "textarea" :rows "3"}]
   [validation-summary errors]])

(defn workspace-modal [workspace modal-open? on-save on-cancel]
  (when @modal-open?
    (let [is-new? (not (:workspace/id @workspace))
          current-errors (validate-workspace @workspace)
          is-valid? (empty? current-errors)]
      [modal-backdrop
       [:<>
        [:h2 {:style {:margin-bottom "1.5rem"}}
         (if is-new? "Add New Workspace" "Edit Workspace")]
        [workspace-modal-form workspace current-errors is-valid? on-save]
        [modal-buttons is-valid? on-cancel on-save]]])))

(defn workspace-row [workspace on-edit on-delete]
  [:tr {:key (:workspace/id workspace)
        :style {:border-bottom "1px solid #eee"}}
   [:td {:style {:padding "0.75rem"}}
    [:div (:workspace/name workspace)]
    [:div {:style {:font-size "0.8rem" :color "#666"}} 
     (when (:workspace/description workspace) (:workspace/description workspace))]]
   [:td {:style {:padding "0.75rem"}} [status-badge (:workspace/active workspace)]]
   [:td {:style {:padding "0.75rem"}} 
    [:div {:style {:font-size "0.8rem" :color "#999"}}
     (:workspace/created-at workspace)]]
   [:td {:style {:padding "0.75rem" :text-align "center"}}
    [action-button "Edit" "#007bff" #(on-edit workspace) true]
    [action-button "Delete" "#dc3545" #(on-delete workspace) false]]])

(defn workspace-table [workspaces on-add on-edit on-delete]
  [:div {:style {:background "white" :border-radius "8px" :padding "1.5rem" :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}}
   [:div {:style {:display "flex" :justify-content "space-between" :align-items "center" :margin-bottom "1.5rem"}}
    [:h2 "Workspaces"]
    [:button {:on-click on-add :style {:padding "0.5rem 1rem" :background "#28a745" :color "white" :border "none" :border-radius "4px" :cursor "pointer"}} "Add Workspace"]]
   [:table {:style {:width "100%" :border-collapse "collapse"}}
    [:thead
     [:tr {:style {:background "#f8f9fa"}}
      [table-th "Name" nil]
      [table-th "Status" nil]
      [table-th "Created" nil]
      [table-th "Actions" "center"]]]
    [:tbody
     (for [workspace workspaces]
       [workspace-row workspace on-edit on-delete])]]])

(defn view []
  (let [users (r/atom [])
        current-user (r/atom {})
        modal-open? (r/atom false)
        loading? (r/atom true)
        workspaces (r/atom [])
        current-workspace (r/atom {})
        workspace-modal-open? (r/atom false)
        workspaces-loading? (r/atom true)
        auth-user (r/atom nil)
        auth-loading? (r/atom true)
        
        check-auth #(do (reset! auth-loading? true)
                         (parquery/send-queries
                           {:queries {:user/current {}}
                            :parquery/context {}
                            :callback (fn [response]
                                        (let [user (:user/current response)]
                                          (reset! auth-user user)
                                          (reset! auth-loading? false)
))}))

        load-users #(do (reset! loading? true)
                         (parquery/send-queries
                           {:queries {:users/get-all {}}
                            :parquery/context {}
                            :callback (fn [response]
                                        (let [users-data (:users/get-all response)]
                                          (if (:error users-data)
                                            (do 
                                              (reset! users []))
                                            (reset! users (or users-data [])))
                                          (reset! loading? false)))}))
        
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
                                            (js/alert (str "Error: " (:error (:users/delete response))))))})))

        load-workspaces #(do (reset! workspaces-loading? true)
                             (parquery/send-queries
                               {:queries {:workspaces/get-all {}}
                                :parquery/context {}
                                :callback (fn [response]
                                            (let [workspaces-data (:workspaces/get-all response)]
                                              (if (:error workspaces-data)
                                                (do 
                                                  (reset! workspaces []))
                                                (reset! workspaces (or workspaces-data [])))
                                              (reset! workspaces-loading? false)))}))
        
        save-workspace (fn [workspace-data is-new?]
                         (let [query-key (if is-new? :workspaces/create :workspaces/update)]
                           (parquery/send-queries
                             {:queries {query-key workspace-data}
                              :parquery/context {}
                              :callback (fn [response]
                                          (if (:success (get response query-key))
                                            (do (load-workspaces) (reset! workspace-modal-open? false))
                                            (js/alert (str "Error: " (:error (get response query-key))))))})))
        
        delete-workspace-fn (fn [workspace]
                              (when (js/confirm (str "Delete workspace " (:workspace/name workspace) "?"))
                                (parquery/send-queries
                                  {:queries {:workspaces/delete {:workspace/id (:workspace/id workspace)}}
                                   :parquery/context {}
                                   :callback (fn [response]
                                               (if (:success (:workspaces/delete response))
                                                 (load-workspaces)
                                                 (js/alert (str "Error: " (:error (:workspaces/delete response))))))})))]
    
    ;; Check auth and load data on component mount
    (check-auth)
    (load-users)
    (load-workspaces)
    
(fn []
      (if @auth-loading?
        [:div {:style {:min-height "100vh"
                       :display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :background "#f5f5f5"}}
         [:div {:style {:text-align "center"}}
          "Checking authentication..."]]
        (if (and @auth-user (= "superadmin" (:user/role @auth-user)))
          [:div {:style {:min-height "100vh"
                         :background "#f5f5f5"
                         :padding "2rem"}}
           [:div {:style {:max-width "1200px"
                          :margin "0 auto"}}
            [:div {:style {:display "flex" :justify-content "space-between" :align-items "center" :margin-bottom "2rem"}}
             [:h1 {:style {:color "#333" :margin "0"}}
              "Super Admin - User Management"]
             [:div {:style {:display "flex" :gap "1rem" :align-items "center"}}
              [:span {:style {:color "#666"}}
               (str "Welcome, " (:user/full-name @auth-user))]
              [:button {:on-click #(parquery/send-queries
                                                  {:queries {:users/logout {}}
                                                   :parquery/context {}
                                                   :callback (fn [response]
                                                               (:success (:users/logout response)))})
                        :style {:padding "0.5rem 1rem" :background "#dc3545" :color "white" :border "none" :border-radius "4px" :cursor "pointer"}}
               "Logout"]]]
            
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
                               :user/role (:user/role user)
                               :user/workspace-id (when (not (empty? (:user/workspace-id user))) 
                                                     (:user/workspace-id user))}
                    user-data (if is-new?
                                (assoc base-data :user/password (:user/password user))
                                (let [update-data (assoc base-data :user/id (:user/id user) :user/active (:user/active user))]
                                  (if (and (:user/password user) (seq (str/trim (:user/password user))))
                                    (assoc update-data :user/password (:user/password user))
                                    update-data)))]
                (save-user user-data is-new?))
             #(reset! modal-open? false)
             @workspaces]
            
            ;; Workspaces Section
            [:div {:style {:margin-top "3rem"}}
             (if @workspaces-loading?
               [:div {:style {:text-align "center" :padding "2rem"}}
                "Loading workspaces..."]
               [workspace-table @workspaces
                #(do (reset! current-workspace {}) (reset! workspace-modal-open? true))
                #(do (reset! current-workspace %) (reset! workspace-modal-open? true))
                delete-workspace-fn])
             
             [workspace-modal current-workspace workspace-modal-open?
              #(let [is-new? (not (:workspace/id @current-workspace))
                     workspace @current-workspace
                     workspace-data {:workspace/name (:workspace/name workspace)
                                   :workspace/description (:workspace/description workspace)}
                     workspace-data (if is-new?
                                      workspace-data
                                      (assoc workspace-data 
                                             :workspace/id (:workspace/id workspace)
                                             :workspace/active (:workspace/active workspace)))]
                 (save-workspace workspace-data is-new?))
              #(reset! workspace-modal-open? false)]]]]
          [:div {:style {:min-height "100vh"
                         :display "flex"
                         :align-items "center"
                         :justify-content "center"
                         :background "#f5f5f5"}}
           [:div {:style {:text-align "center"}}
            [:h2 "Access Denied"]
            [:p "You need superadmin privileges to access this page."]
            [:div {:style {:color "#666"}} "Access denied."]]])))))