(ns features.app.superadmin.frontend.view
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]))

(defn user-modal [user modal-open? on-save on-cancel]
  (when @modal-open?
    [:div {:style {:position "fixed"
                   :top "0"
                   :left "0"
                   :width "100%"
                   :height "100%"
                   :background "rgba(0, 0, 0, 0.5)"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :z-index "1000"}}
     [:div {:style {:background "#fff"
                    :border-radius "8px"
                    :padding "2rem"
                    :width "500px"
                    :max-width "90vw"}}
      [:h2 {:style {:margin-bottom "1.5rem"}}
       (if (:user/id @user) "Edit User" "Add New User")]
      
      [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (on-save))}
       [:div {:style {:margin-bottom "1rem"}}
        [:label {:style {:display "block"
                         :margin-bottom "0.5rem"
                         :font-weight "bold"}}
         "Username"]
        [:input {:type "text"
                 :value (or (:user/username @user) "")
                 :on-change #(swap! user assoc :user/username (.. % -target -value))
                 :style {:width "100%"
                         :padding "0.5rem"
                         :border "1px solid #ccc"
                         :border-radius "4px"}}]]
       
       [:div {:style {:margin-bottom "1rem"}}
        [:label {:style {:display "block"
                         :margin-bottom "0.5rem"
                         :font-weight "bold"}}
         "Full Name"]
        [:input {:type "text"
                 :value (or (:user/full-name @user) "")
                 :on-change #(swap! user assoc :user/full-name (.. % -target -value))
                 :style {:width "100%"
                         :padding "0.5rem"
                         :border "1px solid #ccc"
                         :border-radius "4px"}}]]
       
       [:div {:style {:margin-bottom "1rem"}}
        [:label {:style {:display "block"
                         :margin-bottom "0.5rem"
                         :font-weight "bold"}}
         "Email"]
        [:input {:type "email"
                 :value (or (:user/email @user) "")
                 :on-change #(swap! user assoc :user/email (.. % -target -value))
                 :style {:width "100%"
                         :padding "0.5rem"
                         :border "1px solid #ccc"
                         :border-radius "4px"}}]]
       
       [:div {:style {:margin-bottom "1rem"}}
        [:label {:style {:display "block"
                         :margin-bottom "0.5rem"
                         :font-weight "bold"}}
         "Phone"]
        [:input {:type "tel"
                 :value (or (:user/phone @user) "")
                 :on-change #(swap! user assoc :user/phone (.. % -target -value))
                 :style {:width "100%"
                         :padding "0.5rem"
                         :border "1px solid #ccc"
                         :border-radius "4px"}}]]
       
       [:div {:style {:margin-bottom "1rem"}}
        [:label {:style {:display "block"
                         :margin-bottom "0.5rem"
                         :font-weight "bold"}}
         "Role"]
        [:select {:value (or (:user/role @user) "employee")
                  :on-change #(swap! user assoc :user/role (.. % -target -value))
                  :style {:width "100%"
                          :padding "0.5rem"
                          :border "1px solid #ccc"
                          :border-radius "4px"}}
         [:option {:value "employee"} "Employee"]
         [:option {:value "admin"} "Admin"]
         [:option {:value "superadmin"} "Super Admin"]]]
       
       (when-not (:user/id @user)
         [:div {:style {:margin-bottom "1rem"}}
          [:label {:style {:display "block"
                           :margin-bottom "0.5rem"
                           :font-weight "bold"}}
           "Password"]
          [:input {:type "password"
                   :value (or (:user/password @user) "")
                   :on-change #(swap! user assoc :user/password (.. % -target -value))
                   :style {:width "100%"
                           :padding "0.5rem"
                           :border "1px solid #ccc"
                           :border-radius "4px"}}]])
       
       [:div {:style {:display "flex"
                      :gap "1rem"
                      :justify-content "flex-end"
                      :margin-top "2rem"}}
        [:button {:type "button"
                  :on-click on-cancel
                  :style {:padding "0.5rem 1rem"
                          :border "1px solid #ccc"
                          :background "#f5f5f5"
                          :border-radius "4px"
                          :cursor "pointer"}}
         "Cancel"]
        [:button {:type "submit"
                  :style {:padding "0.5rem 1rem"
                          :border "none"
                          :background "#007bff"
                          :color "white"
                          :border-radius "4px"
                          :cursor "pointer"}}
         "Save"]]]]]))

(defn user-table [users on-add on-edit on-delete]
  [:div {:style {:background "white"
                 :border-radius "8px"
                 :padding "1.5rem"
                 :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}}
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "center"
                  :margin-bottom "1.5rem"}}
    [:h2 "Users"]
    [:button {:on-click on-add
              :style {:padding "0.5rem 1rem"
                      :background "#28a745"
                      :color "white"
                      :border "none"
                      :border-radius "4px"
                      :cursor "pointer"}}
     "Add User"]]
   
   [:table {:style {:width "100%"
                    :border-collapse "collapse"}}
    [:thead
     [:tr {:style {:background "#f8f9fa"}}
      [:th {:style {:padding "0.75rem"
                    :text-align "left"
                    :border-bottom "1px solid #ddd"}}
       "Name"]
      [:th {:style {:padding "0.75rem"
                    :text-align "left"
                    :border-bottom "1px solid #ddd"}}
       "Email"]
      [:th {:style {:padding "0.75rem"
                    :text-align "left"
                    :border-bottom "1px solid #ddd"}}
       "Role"]
      [:th {:style {:padding "0.75rem"
                    :text-align "left"
                    :border-bottom "1px solid #ddd"}}
       "Status"]
      [:th {:style {:padding "0.75rem"
                    :text-align "center"
                    :border-bottom "1px solid #ddd"}}
       "Actions"]]]
    
    [:tbody
     (for [user users]
       ^{:key (:user/id user)}
       [:tr {:style {:border-bottom "1px solid #eee"}}
        [:td {:style {:padding "0.75rem"}} 
         [:div (:user/full-name user)]
         [:div {:style {:font-size "0.8rem" :color "#666"}} (:user/username user)]]
        [:td {:style {:padding "0.75rem"}} (:user/email user)]
        [:td {:style {:padding "0.75rem"}} 
         [:span {:style {:padding "0.25rem 0.5rem"
                         :border-radius "4px"
                         :font-size "0.875rem"
                         :background (case (:user/role user)
                                      "superadmin" "#dc3545"
                                      "admin" "#fd7e14"
                                      "#28a745")
                         :color "white"}}
          (:user/role user)]]
        [:td {:style {:padding "0.75rem"}}
         [:span {:style {:padding "0.25rem 0.5rem"
                         :border-radius "4px"
                         :font-size "0.875rem"
                         :background (if (:user/active user) "#28a745" "#6c757d")
                         :color "white"}}
          (if (:user/active user) "Active" "Inactive")]]
        [:td {:style {:padding "0.75rem"
                      :text-align "center"}}
         [:button {:on-click #(on-edit user)
                   :style {:padding "0.25rem 0.5rem"
                           :margin-right "0.5rem"
                           :background "#007bff"
                           :color "white"
                           :border "none"
                           :border-radius "4px"
                           :cursor "pointer"
                           :font-size "0.875rem"}}
          "Edit"]
         [:button {:on-click #(on-delete user)
                   :style {:padding "0.25rem 0.5rem"
                           :background "#dc3545"
                           :color "white"
                           :border "none"
                           :border-radius "4px"
                           :cursor "pointer"
                           :font-size "0.875rem"}}
          "Delete"]]])]]])

(defn view []
  (let [users (r/atom [])
        current-user (r/atom {})
        modal-open? (r/atom false)
        loading? (r/atom true)
        
        load-users (fn []
                     (reset! loading? true)
                     (parquery/send-queries
                       {:queries {:users/get-all {}}
                        :parquery/context {}
                        :callback (fn [response]
                                    (println "Users loaded:" response)
                                    (reset! users (:users/get-all response))
                                    (reset! loading? false))}))
        
        save-user (fn [user-data is-new?]
                    (let [query-key (if is-new? :users/create :users/update)]
                      (parquery/send-queries
                        {:queries {query-key user-data}
                         :parquery/context {}
                         :callback (fn [response]
                                     (println "User save response:" response)
                                     (if (:success (get response query-key))
                                       (do
                                         (load-users)
                                         (reset! modal-open? false))
                                       (js/alert (str "Error: " (:error (get response query-key))))))})))
        
        delete-user-fn (fn [user]
                         (when (js/confirm (str "Delete user " (:user/full-name user) "?"))
                           (parquery/send-queries
                             {:queries {:users/delete {:user/id (:user/id user)}}
                              :parquery/context {}
                              :callback (fn [response]
                                          (println "Delete response:" response)
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
         (fn [] ; on-add
           (reset! current-user {})
           (reset! modal-open? true))
         (fn [user] ; on-edit
           (reset! current-user user)
           (reset! modal-open? true))
         delete-user-fn])
        
        [user-modal current-user modal-open?
         (fn [] ; on-save
           (let [is-new? (not (:user/id @current-user))
                 user-data (if is-new?
                            {:user/username (:user/username @current-user)
                             :user/full-name (:user/full-name @current-user)
                             :user/password (:user/password @current-user)
                             :user/email (:user/email @current-user)
                             :user/phone (:user/phone @current-user)
                             :user/role (:user/role @current-user)}
                            {:user/id (:user/id @current-user)
                             :user/username (:user/username @current-user)
                             :user/full-name (:user/full-name @current-user)
                             :user/email (:user/email @current-user)
                             :user/phone (:user/phone @current-user)
                             :user/role (:user/role @current-user)
                             :user/active (:user/active @current-user)})]
             (save-user user-data is-new?)))
         (fn [] ; on-cancel
           (reset! modal-open? false))]]])))