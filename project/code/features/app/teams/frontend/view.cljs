(ns features.app.teams.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame :as rf]
            [zero.frontend.react :as zero-react]
            [ui.modal :as modal]
            [ui.form-field :as form-field]
            [ui.data-table.core :as data-table]
            [ui.data-table.search :as data-table-search]
            [ui.enhanced-button :as enhanced-button]
            [ui.subheader :as subheader]
            [ui.content-section :as content-section]
            [translations.core :as tr]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    workspace-id))

(defn- load-teams-query
  "Execute ParQuery to load team members with pagination"
  [workspace-id params users-atom pagination-atom loading-atom]
  (reset! loading-atom true)
  (parquery/send-queries
   {:queries {:workspace-teams/get-paginated params}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (reset! loading-atom false)
               (let [result (:workspace-teams/get-paginated response)
                     items (:users result [])
                     pag (:pagination result)]
                 (reset! users-atom items)
                 (when pag
                   (reset! pagination-atom pag))))}))

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

(defn- fetch-user-for-edit
  "Fetch user by ID with password for editing"
  [user-id workspace-id]
  (parquery/send-queries
   {:queries {:workspace-teams/get-by-id {:user/id user-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (let [user (:workspace-teams/get-by-id response)]
                 (when user
                   (rf/dispatch [:teams/open-modal user false]))))}))

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
  "Validate password - required for new users, optional but min 6 chars if provided for existing"
  [password is-new?]
  (let [pwd-str (str/trim (str password))]
    (if is-new?
      (< (count pwd-str) 6)
      (and (seq pwd-str) (< (count pwd-str) 6)))))

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

(rf/reg-sub
  :teams/password-visible?
  (fn [db _]
    (get-in db [:teams :password-visible?] false)))

(rf/reg-event-db
  :teams/toggle-password-visibility
  (fn [db _]
    (update-in db [:teams :password-visible?] not)))

(rf/reg-event-db
  :teams/reset-password-visibility
  (fn [db _]
    (assoc-in db [:teams :password-visible?] false)))


(rf/reg-event-db
  :teams/open-modal
  (fn [db [_ team is-new?]]
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
        (assoc-in [:teams :modal-errors] {})
        (assoc-in [:teams :password-visible?] false))))

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

(defn- generate-random-password
  "Generate a random 8-character password"
  []
  (let [chars "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"]
    (apply str (repeatedly 8 #(nth chars (rand-int (count chars)))))))

(defn- eye-icon
  "SVG eye icon for showing password"
  []
  [:svg {:width "20" :height "20" :viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2"}
   [:path {:d "M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"}]
   [:circle {:cx "12" :cy "12" :r "3"}]])

(defn- eye-off-icon
  "SVG eye-off icon for hiding password"
  []
  [:svg {:width "20" :height "20" :viewBox "0 0 24 24" :fill "none" :stroke "currentColor" :stroke-width "2"}
   [:path {:d "M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"}]
   [:line {:x1 "1" :y1 "1" :x2 "23" :y2 "23"}]])

(defn- password-field-with-toggle
  "Password field with visibility toggle and generate button"
  [form-data errors is-new?]
  (let [password-visible? (rf/subscribe [:teams/password-visible?])
        has-error? (contains? errors :user/password)
        password-value (str (get form-data :user/password ""))]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label (tr/tr :teams/password) :user/password has-error?]
     [:div {:style {:display "flex" :gap "0.5rem" :align-items "stretch"}}
      [:div {:style {:position "relative" :flex "1"}}
       [:input {:type (if @password-visible? "text" "password")
                :value password-value
                :placeholder (tr/tr :teams/password-placeholder)
                :on-change #(rf/dispatch [:teams/update-form-field :user/password (.. % -target -value)])
                :style {:width "100%"
                        :padding "0.75rem 2.5rem 0.75rem 1rem"
                        :border (if has-error? "2px solid #dc3545" "1px solid #d1d5db")
                        :border-radius "8px"
                        :font-size "1rem"
                        :line-height "1.5"
                        :box-shadow "0 1px 2px 0 rgba(0, 0, 0, 0.05)"
                        :outline "none"}}]
       [:button {:type "button"
                 :on-click #(rf/dispatch [:teams/toggle-password-visibility])
                 :title (if @password-visible?
                          (tr/tr :teams/hide-password)
                          (tr/tr :teams/show-password))
                 :style {:position "absolute"
                         :right "0.5rem"
                         :top "50%"
                         :transform "translateY(-50%)"
                         :background "transparent"
                         :border "none"
                         :cursor "pointer"
                         :color "#6b7280"
                         :padding "0.25rem"
                         :display "flex"
                         :align-items "center"
                         :justify-content "center"}}
        (if @password-visible? [eye-off-icon] [eye-icon])]]
      [:button {:type "button"
                :on-click #(rf/dispatch [:teams/update-form-field :user/password (generate-random-password)])
                :style {:padding "0.75rem 1rem"
                        :background "#3b82f6"
                        :color "white"
                        :border "none"
                        :border-radius "8px"
                        :font-size "0.875rem"
                        :font-weight "500"
                        :cursor "pointer"
                        :white-space "nowrap"}}
       (tr/tr :teams/generate-new-password)]]
     [field-error (get errors :user/password)]]))

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
     :options [{:value "employee" :label (tr/tr :header/role-employee)}
               {:value "admin" :label (tr/tr :header/role-admin)}]}]
   [password-field-with-toggle form-data errors is-new?]
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

;; =============================================================================
;; Column Renderers for react-data-table-component
;; =============================================================================

(defn- user-name-cell
  "Custom cell for user name column with username and email"
  [row]
  [:div
   [:div {:style {:font-weight "600" :color "#111827" :font-size "0.875rem"}}
    (:user/full-name row)]
   [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem"}}
    (str "@" (:user/username row))]
   [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
    (:user/email row)]])

(defn- role-cell
  "Custom cell for role column"
  [row]
  (let [role (:user/role row)]
    [:span {:style {:display "inline-block" :padding "0.25rem 0.75rem"
                    :background (case role
                                  "superadmin" "#dc2626"
                                  "admin" "#ea580c"
                                  "employee" "#059669"
                                  "#6b7280")
                    :color "white"
                    :border-radius "12px" :font-size "0.75rem" :font-weight "500"}}
     (str/capitalize (str role))]))

(defn- status-cell
  "Custom cell for active status"
  [row]
  (let [active (:user/active row)]
    [:span {:style {:display "inline-block" :padding "0.25rem 0.75rem"
                    :background (if active "#10b981" "#ef4444")
                    :color "white"
                    :border-radius "12px" :font-size "0.75rem" :font-weight "500"}}
     (if active (tr/tr :teams/active) (tr/tr :teams/inactive))]))

(defn- contact-cell
  "Custom cell for contact info"
  [row]
  (let [phone (:user/phone row)]
    [:div
     (when phone
       [:div {:style {:color "#374151" :font-size "0.875rem"}}
        phone])]))

(defn- get-columns
  "Get column configuration for teams table (react-data-table format)"
  []
  [{:name      (tr/tr :teams/table-header-member)
    :selector  :user/full-name
    :sortField :user/full-name
    :sortable  true
    :cell      user-name-cell
    :width     "250px"}
   {:name      (tr/tr :teams/table-header-role)
    :selector  :user/role
    :sortField :user/role
    :sortable  true
    :cell      role-cell
    :width     "120px"}
   {:name      (tr/tr :teams/table-header-status)
    :selector  :user/active
    :sortField :user/active
    :sortable  true
    :cell      status-cell
    :width     "120px"}
   {:name      (tr/tr :teams/table-header-phone)
    :selector  :user/phone
    :sortable  false
    :cell      contact-cell
    :width     "160px"}])

(defn- teams-subheader
  "Subheader with title and add button"
  []
  [subheader/subheader
   {:title (tr/tr :teams/page-title)
    :description (tr/tr :teams/page-description)
    :action-button [enhanced-button/enhanced-button
                    {:variant :success
                     :on-click (fn []
                                (rf/dispatch [:teams/open-modal {:user/role "employee"
                                                                :user/active true} true]))
                     :text (tr/tr :teams/add-new-member)}]}])

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
        ;; Local state
        users (r/atom [])
        pagination (r/atom {:total-count 0 :page 0 :page-size 10})
        loading? (r/atom false)
        search-term (r/atom "")
        sort-field (r/atom :user/full-name)
        sort-direction (r/atom "asc")

        ;; Re-frame subscriptions for modal
        modal-is-new? (rf/subscribe [:teams/modal-is-new?])

        ;; Load function
        load-teams (fn []
                     (load-teams-query
                      workspace-id
                      {:search @search-term
                       :sort-by @sort-field
                       :sort-direction @sort-direction
                       :page (:page @pagination)
                       :page-size (:page-size @pagination)}
                      users
                      pagination
                      loading?))

        save-team (fn [team callback]
                    (save-team-query team workspace-id modal-is-new?
                                     callback load-teams))

        delete-team (fn [user-id]
                      (delete-team-query user-id workspace-id load-teams))

        on-edit (fn [row]
                  (fetch-user-for-edit (:user/id row) workspace-id))

        on-delete (fn [row]
                    (when (js/confirm (tr/tr :teams/confirm-delete))
                      (delete-team (:user/id row))))]

    (fn []
      ;; Load initial data
      (when (and (empty? @users) (not @loading?))
        (load-teams))

      [:<>
       [teams-subheader]
       [content-section/content-section
        ;; Search bar
        [:div {:style {:margin-bottom "1rem"}}
        [data-table-search/view
         {:search-term @search-term
          :placeholder (tr/tr :teams/search-placeholder)
          :on-search-change (fn [value]
                              (reset! search-term value))
          :on-search (fn [value]
                       (reset! search-term value)
                       (swap! pagination assoc :page 0)
                       (load-teams))}]]

       ;; Teams table
       [data-table/view
        {:columns (get-columns)
         :data @users
         :loading? @loading?
         :pagination @pagination
         :entity {:name "user" :name-plural "team members"}
         :on-edit on-edit
         :on-delete on-delete
         ;; Pagination handler
         :on-page-change (fn [page _total-rows]
                           (swap! pagination assoc :page (dec page))
                           (load-teams))
         :on-page-size-change (fn [new-size]
                                (swap! pagination assoc :page-size new-size :page 0)
                                (load-teams))
         ;; Sort handler
         :on-sort (fn [field direction _sorted-rows]
                    (reset! sort-field field)
                    (reset! sort-direction direction)
                    (load-teams))}]

       [modal-when-open save-team]]])))
