(ns features.app.teams.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame :as rf]
            [zero.frontend.react :as zero-react]
            [ui.modal :as modal]
            [ui.form-field :as form-field]
            [ui.data-table :as data-table]
            [ui.enhanced-button :as enhanced-button]
            [ui.page-header :as page-header]
            [translations.core :as tr]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    workspace-id))

(defn- load-teams-query
  "Execute ParQuery to load team members with pagination"
  [workspace-id params]
  (rf/dispatch [:teams/set-loading true])
  (parquery/send-queries
   {:queries {:workspace-teams/get-paginated params}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (let [result (:workspace-teams/get-paginated response)]
                 (rf/dispatch [:teams/load-success result])))}))

(defn- get-query-type
  "Get appropriate query type for save operation"
  [is-new?]
  (if @is-new? 
    :workspace-teams/create 
    :workspace-teams/update))

(defn- prepare-team-data
  "Prepare team member data for save"
  [team is-new?]
  (if @is-new?
    (dissoc team :user/id)
    team))

(defn- handle-save-response
  "Handle save response and update UI"
  [response query-type callback load-teams]
  (callback)
  (if (:success (get response query-type))
    (do (rf/dispatch [:teams/close-modal])
        (load-teams))
    (js/alert (str "Error: " (:error (get response query-type))))))

(defn- save-team-query
  "Execute ParQuery to save team member"
  [team workspace-id modal-is-new? callback load-teams]
  (let [query-type (get-query-type modal-is-new?)
        team-data (prepare-team-data team modal-is-new?)
        context {:workspace-id workspace-id}]
    (parquery/send-queries
     {:queries {query-type team-data}
      :parquery/context context
      :callback (fn [response]
                 (handle-save-response response query-type callback load-teams))})))

(defn- delete-team-query
  "Execute ParQuery to delete team member"
  [user-id workspace-id load-teams]
  (parquery/send-queries
   {:queries {:workspace-teams/delete {:user/id user-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (if (:success (:workspace-teams/delete response))
                 (load-teams)
                 (js/alert "Error deleting team member")))}))

(defn- validate-username
  "Validate username"
  [username]
  (< (count (str/trim (str username))) 2))

(defn- validate-full-name
  "Validate full name"
  [full-name]
  (< (count (str/trim (str full-name))) 2))

(defn- validate-email
  "Validate email"
  [email]
  (let [email-str (str/trim (str email))]
    (or (< (count email-str) 3)
        (not (re-matches #"^[^\s@]+@[^\s@]+\.[^\s@]+$" email-str)))))

(defn- validate-password
  "Validate password (only for new users)"
  [password is-new?]
  (when is-new?
    (< (count (str/trim (str password))) 6)))

(defn validate-team-member
  "Validates team member data and returns map of field errors"
  [team is-new?]
  (let [errors {}
        username (:user/username team)
        full-name (:user/full-name team)
        email (:user/email team)
        password (:user/password team)]
    (cond-> errors
      (validate-username username) (assoc :user/username (tr/tr :teams/error-username))
      (validate-full-name full-name) (assoc :user/full-name (tr/tr :teams/error-full-name))
      (validate-email email) (assoc :user/email (tr/tr :teams/error-email))
      (validate-password password is-new?) (assoc :user/password (tr/tr :teams/error-password)))))

;; Re-frame events and subscriptions
(rf/reg-sub
  :teams/data
  (fn [db _]
    (get-in db [:teams :data] {:users [] :pagination {}})))

(rf/reg-sub
  :teams/loading?
  (fn [db _]
    (get-in db [:teams :loading?] false)))

(rf/reg-sub
  :teams/modal-team
  (fn [db _]
    (get-in db [:teams :modal-team] nil)))

(rf/reg-sub
  :teams/modal-is-new?
  (fn [db _]
    (get-in db [:teams :modal-is-new?] false)))

(rf/reg-sub
  :teams/modal-form
  (fn [db _]
    (get-in db [:teams :modal-form] {})))

(rf/reg-sub
  :teams/modal-errors
  (fn [db _]
    (get-in db [:teams :modal-errors] {})))

(rf/reg-sub
  :teams/modal-loading?
  (fn [db _]
    (get-in db [:teams :modal-loading?] false)))


(rf/reg-event-db
  :teams/set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:teams :loading?] loading?)))

(rf/reg-event-db
  :teams/load-success
  (fn [db [_ data]]
    (let [updated-db (-> db
                         (assoc-in [:teams :data] data)
                         (assoc-in [:teams :loading?] false))]
      updated-db)))


(rf/reg-event-db
  :teams/open-modal
  (fn [db [_ team is-new?]]  ; Standard re-frame pattern, no trim-v
    (-> db
        (assoc-in [:teams :modal-team] team)
        (assoc-in [:teams :modal-is-new?] is-new?))))

(rf/reg-event-db
  :teams/close-modal
  (fn [db _]
    (-> db
        (assoc-in [:teams :modal-team] nil)
        (assoc-in [:teams :modal-is-new?] false)
        (assoc-in [:teams :modal-form] {})
        (assoc-in [:teams :modal-errors] {}))))

(rf/reg-event-db
  :teams/update-form-field
  (fn [db [_ field-key value]]
    (assoc-in db [:teams :modal-form field-key] value)))

(rf/reg-event-db
  :teams/set-form-errors
  (fn [db [_ errors]]
    (assoc-in db [:teams :modal-errors] errors)))

(rf/reg-event-db
  :teams/init-form
  (fn [db [_ team-data]]
    (assoc-in db [:teams :modal-form] (or team-data {}))))

(rf/reg-event-db
  :teams/set-modal-loading
  (fn [db [_ loading?]]
    (assoc-in db [:teams :modal-loading?] loading?)))

(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                   :font-size "0.875rem" :letter-spacing "0.025em"
                   :color (if has-error? "#dc3545" "#374151")}}
   label 
   (when (#{:user/username :user/full-name :user/email} field-key) 
     [:span {:style {:color "#ef4444" :margin-left "0.25rem"}} "*"])])

(defn- input-base-props
  "Base properties for input fields"
  [field-key team has-error? attrs]
  {:value (str (get team field-key ""))
   :on-change #(rf/dispatch [:teams/update-form-field field-key (.. % -target -value)])
   :style (merge {:width "100%"
                  :padding "0.75rem 1rem"
                  :border (if has-error? "2px solid #dc3545" "1px solid #d1d5db")
                  :border-radius "8px"
                  :font-size "1rem"
                  :line-height "1.5"
                  :transition "border-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out"
                  :box-shadow (if has-error? 
                                "0 0 0 3px rgba(220, 53, 69, 0.1)" 
                                "0 1px 2px 0 rgba(0, 0, 0, 0.05)")
                  :outline "none"}
                 (:style attrs)
                 {:focus {:border-color (if has-error? "#dc3545" "#3b82f6")
                         :box-shadow (if has-error? 
                                       "0 0 0 3px rgba(220, 53, 69, 0.1)"
                                       "0 0 0 3px rgba(59, 130, 246, 0.1)")}})})

(defn- render-select-input
  "Render select dropdown input"
  [field-key form-data has-error? attrs options]
  [:select (merge (dissoc attrs :type :options) (input-base-props field-key form-data has-error? attrs))
   (for [option options]
     ^{:key (:value option)}
     [:option {:value (:value option)} (:label option)])])

(defn- render-text-input
  "Render text input"
  [field-key form-data has-error? attrs]
  [:input (merge attrs (input-base-props field-key form-data has-error? attrs))])

(defn- field-input
  "Render appropriate input type"
  [field-key form-data has-error? attrs]
  (if (:options attrs)
    (render-select-input field-key form-data has-error? attrs (:options attrs))
    (render-text-input field-key form-data has-error? attrs)))

(defn- field-error [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- form-field
  "Complete form field with label, input and error"
  [label field-key form-data errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key form-data has-error? attrs]
     [field-error (get errors field-key)]]))

(defn- form-fields
  "All form input fields"
  [form-data errors is-new?]
  [:div
   [form-field (tr/tr :teams/username) :user/username form-data errors
    {:type "text" :placeholder (tr/tr :teams/username-placeholder)}]
   [form-field (tr/tr :teams/full-name) :user/full-name form-data errors
    {:type "text" :placeholder (tr/tr :teams/full-name-placeholder)}]
   [form-field (tr/tr :teams/email) :user/email form-data errors
    {:type "email" :placeholder (tr/tr :teams/email-placeholder)}]
   [form-field (tr/tr :teams/phone) :user/phone form-data errors
    {:type "tel" :placeholder (tr/tr :teams/phone-placeholder)}]
   [form-field (tr/tr :teams/role) :user/role form-data errors
    {:type "select" 
     :options [{:value "employee" :label (tr/tr :teams/employee)}
               {:value "admin" :label (tr/tr :teams/admin)}]}]
   (when is-new?
     [form-field (tr/tr :teams/password) :user/password form-data errors
      {:type "password" :placeholder (tr/tr :teams/password-placeholder)}])
   (when-not is-new?
     [form-field (tr/tr :teams/active-field) :user/active form-data errors
      {:type "select"
       :options [{:value true :label (tr/tr :teams/active)}
                 {:value false :label (tr/tr :teams/inactive)}]}])])

(defn- handle-save-click
  "Handle save button click with validation"
  [form-data is-new? on-save]
  (let [validation-errors (validate-team-member form-data is-new?)]
    (if (empty? validation-errors)
      (do (rf/dispatch [:teams/set-form-errors {}])
          (rf/dispatch [:teams/set-modal-loading true])
          (on-save form-data (fn [] (rf/dispatch [:teams/set-modal-loading false]))))
      (rf/dispatch [:teams/set-form-errors validation-errors]))))

(defn team-modal
  "Modal for creating/editing team members using new UI components"
  [team-data is-new? on-save on-cancel]
  (let [form-data (rf/subscribe [:teams/modal-form])
        errors (rf/subscribe [:teams/modal-errors])
        loading? (rf/subscribe [:teams/modal-loading?])]
    ;; Initialize form when modal opens
    (rf/dispatch [:teams/init-form team-data])
    (fn [team-data is-new? on-save on-cancel]
      [modal/modal {:on-close on-cancel :close-on-backdrop? true}
       ^{:key "header"} [modal/modal-header
        {:title (if is-new? (tr/tr :teams/modal-add-title) (tr/tr :teams/modal-edit-title))
         :subtitle (if is-new? 
                     (tr/tr :teams/modal-add-subtitle)
                     (tr/tr :teams/modal-edit-subtitle))}]
       ^{:key "form"} [form-fields @form-data @errors is-new?]
       ^{:key "footer"} [modal/modal-footer
        ^{:key "cancel"} [enhanced-button/enhanced-button
         {:variant :secondary
          :on-click on-cancel
          :text (tr/tr :teams/cancel)}]
        ^{:key "save"} [enhanced-button/enhanced-button
         {:variant :primary
          :loading? @loading?
          :on-click #(handle-save-click @form-data is-new? on-save)
          :text (if @loading? (tr/tr :teams/saving) (tr/tr :teams/save-member))}]]])))

(defn- user-name-render
  "Custom render function for user name column with username and email"
  [full-name row]
  [:div 
   [:div {:style {:font-weight "600" :color "#111827" :font-size "0.875rem"}}
    full-name]
   [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem"}}
    (str "@" (:user/username row))]
   [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
    (:user/email row)]])

(defn- role-render
  "Custom render function for role column"
  [role row]
  [:span {:style {:display "inline-block" :padding "0.25rem 0.75rem"
                  :background (case role
                                "superadmin" "#dc2626"
                                "admin" "#ea580c" 
                                "employee" "#059669"
                                "#6b7280")
                  :color "white"
                  :border-radius "12px" :font-size "0.75rem" :font-weight "500"}}
   (str/capitalize (str role))])

(defn- status-render
  "Custom render function for active status"
  [active row]
  [:span {:style {:display "inline-block" :padding "0.25rem 0.75rem"
                  :background (if active "#10b981" "#ef4444")
                  :color "white"
                  :border-radius "12px" :font-size "0.75rem" :font-weight "500"}}
   (if active (tr/tr :teams/active) (tr/tr :teams/inactive))])

(defn- contact-render
  "Custom render function for contact info"
  [phone row]
  [:div
   (when phone
     [:div {:style {:color "#374151" :font-size "0.875rem"}}
      phone])])

(defn teams-table
  "Teams table using server-side data-table component with search, sorting, and pagination"
  [teams loading? on-edit on-delete query-fn]
  [data-table/server-side-data-table
   {:headers [{:key :user/full-name :label (tr/tr :teams/table-header-member) :render user-name-render :sortable? true}
              {:key :user/role :label (tr/tr :teams/table-header-role) :render role-render :sortable? true}
              {:key :user/active :label (tr/tr :teams/table-header-status) :render status-render :sortable? true}
              {:key :user/phone :label (tr/tr :teams/table-header-phone) :render contact-render :sortable? false}]
    :data-source @teams
    :data-key :users
      :loading? @loading?
      :empty-message (tr/tr :teams/no-members-found)
      :id-key :user/id
      :table-id :teams-table
      :show-search? true
      :show-pagination? true
      :query-fn query-fn
      :actions [{:key :edit :label (tr/tr :teams/action-edit) :variant :primary :on-click on-edit}
                {:key :delete :label (tr/tr :teams/action-delete) :variant :danger 
                 :on-click (fn [row] 
                            (when (js/confirm (tr/tr :teams/confirm-delete))
                              (on-delete (:user/id row))))}]}])

(defn- teams-page-header
  "Page header with title and add button using new UI component"
  []
  [page-header/page-header
   {:title (tr/tr :teams/page-title)
    :description (tr/tr :teams/page-description)
    :action-button [enhanced-button/enhanced-button
                    {:variant :success
                     :on-click (fn [] 
                                (rf/dispatch [:teams/open-modal {:user/role "employee"
                                                                :user/active true} true]))
                     :text (tr/tr :teams/add-new-member)}]}])

(defn- teams-content
  "Main content area with server-side data table"
  [teams-data loading? delete-team query-fn]
  [teams-table 
   teams-data 
   loading?
   (fn [team]
     (let [event-vec [:teams/open-modal team false]]
       (rf/dispatch event-vec)))
   delete-team
   query-fn])

(defn- modal-when-open
  "Render modal when team member is selected"
  [save-team]
  (let [modal-team (rf/subscribe [:teams/modal-team])
        modal-is-new? (rf/subscribe [:teams/modal-is-new?])]
    (when @modal-team
      [team-modal @modal-team @modal-is-new? save-team
       (fn [] (rf/dispatch [:teams/close-modal]))])))

(defn view []
  (let [workspace-id (get-workspace-id)
        teams-data (rf/subscribe [:teams/data])
        loading? (rf/subscribe [:teams/loading?])
        modal-team (rf/subscribe [:teams/modal-team])
        modal-is-new? (rf/subscribe [:teams/modal-is-new?])
        
        load-teams (fn [params]
                     (load-teams-query workspace-id (or params {})))
        
        save-team (fn [team callback]
                    (save-team-query team workspace-id modal-is-new? 
                                     callback (fn [] (load-teams {}))))
        
        delete-team (fn [user-id]
                      (delete-team-query user-id workspace-id (fn [] (load-teams {}))))]
    
    
    ;; Load teams on component mount (authentication is handled by backend)
    (zero-react/use-effect
     {;:mount (fn [] (load-teams {}))
      :params #js[]})
    
    [:div {:style {:min-height "100vh" :background "#f9fafb"}}
     [:div {:style {:max-width "1200px" :margin "0 auto" :padding "2rem"}} 
      [teams-page-header]
      [teams-content teams-data loading? delete-team load-teams]
      [modal-when-open save-team]]]))