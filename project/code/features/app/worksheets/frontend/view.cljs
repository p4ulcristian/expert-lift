(ns features.app.worksheets.frontend.view
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
            [ui.address-search :as address-search]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    (println "DEBUG: get-workspace-id called")
    (println "  Router state:" router-state)
    (println "  Extracted workspace-id:" workspace-id)
    workspace-id))

(defn- load-worksheets-query
  "Execute ParQuery to load worksheets with pagination"
  [workspace-id params]
  (println "DEBUG load-worksheets-query called with params:" params)
  (rf/dispatch [:worksheets/set-loading true])
  (parquery/send-queries
   {:queries {:workspace-worksheets/get-paginated params}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (println "DEBUG load-worksheets-query response:" response)
               (let [result (:workspace-worksheets/get-paginated response)]
                 (println "DEBUG: ParQuery result structure:" result)
                 (println "DEBUG: Worksheets array:" (:worksheets result))
                 (rf/dispatch [:worksheets/set-data result])))}))


(defn- save-worksheet-query
  "Execute ParQuery to save worksheet"
  [worksheet workspace-id modal-is-new? callback modal-worksheet load-worksheets]
  (let [is-new? (if (satisfies? IDeref modal-is-new?) @modal-is-new? modal-is-new?)
        query-type (if is-new? :workspace-worksheets/create :workspace-worksheets/update)
        worksheet-data (if is-new? (dissoc worksheet :worksheet/id) worksheet)
        context {:workspace-id workspace-id}]
    (println "DEBUG: save-worksheet-query called")
    (println "  Worksheet input:" worksheet)
    (println "  Workspace ID:" workspace-id)
    (println "  Is new?:" is-new?)
    (println "  Query type:" query-type)
    (println "  Prepared worksheet data:" worksheet-data)
    (println "  Context being sent:" context)
    (parquery/send-queries
     {:queries {query-type worksheet-data}
      :parquery/context context
      :callback (fn [response]
                 (println "DEBUG: save-worksheet-query response:" response)
                 (callback)
                 (if (:success (get response query-type))
                   (do (rf/dispatch [:worksheets/close-modal])
                       (load-worksheets))
                   (js/alert (str "Error: " (:error (get response query-type))))))})))

(defn- delete-worksheet-query
  "Execute ParQuery to delete worksheet"
  [worksheet-id workspace-id load-worksheets]
  (parquery/send-queries
   {:queries {:workspace-worksheets/delete {:worksheet/id worksheet-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (if (:success (:workspace-worksheets/delete response))
                 (load-worksheets)
                 (js/alert "Error deleting worksheet")))}))

(defn- validate-serial-number
  "Validate worksheet serial number"
  [serial-number]
  (< (count (str/trim (str serial-number))) 2))

(defn- validate-work-description
  "Validate work description"
  [work-description]
  (< (count (str/trim (str work-description))) 5))

(defn validate-worksheet
  "Validates worksheet data and returns map of field errors"
  [worksheet]
  (let [errors {}
        serial-number (:worksheet/serial-number worksheet)
        work-description (:worksheet/work-description worksheet)
        work-type (:worksheet/work-type worksheet)
        service-type (:worksheet/service-type worksheet)
        status (:worksheet/status worksheet)]
    (cond-> errors
      (validate-serial-number serial-number) (assoc :worksheet/serial-number "Serial number is required")
      (validate-work-description work-description) (assoc :worksheet/work-description "Work description is required (min 5 characters)")
      (empty? work-type) (assoc :worksheet/work-type "Work type is required")
      (empty? service-type) (assoc :worksheet/service-type "Service type is required") 
      (empty? status) (assoc :worksheet/status "Status is required")
      (empty? (:worksheet/address-id worksheet)) (assoc :worksheet/address-id "Address is required"))))

(defn- calculate-work-duration
  "Calculate work duration in hours from arrival and departure times"
  [arrival-time departure-time]
  (when (and arrival-time departure-time 
             (not (empty? arrival-time)) 
             (not (empty? departure-time)))
    (try
      (let [arrival (js/Date. arrival-time)
            departure (js/Date. departure-time)]
        (when (> (.getTime departure) (.getTime arrival))
          (let [diff-ms (- (.getTime departure) (.getTime arrival))
                diff-hours (/ diff-ms 1000 60 60)]
            ;; Round up to nearest full hour
            (Math/ceil diff-hours))))
      (catch js/Error e
        (println "Error calculating duration:" e)
        nil))))


(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                   :font-size "0.875rem" :letter-spacing "0.025em"
                   :color (if has-error? "#dc3545" "#374151")}}
   label 
   (when (#{:worksheet/serial-number :worksheet/work-description :worksheet/work-type :worksheet/service-type :worksheet/status} field-key) 
     [:span {:style {:color "#ef4444" :margin-left "0.25rem"}} "*"])])

(defn- format-datetime-for-input
  "Convert ISO datetime string to datetime-local format (YYYY-MM-DDTHH:mm)"
  [iso-datetime]
  (when (and iso-datetime (not (empty? iso-datetime)))
    (try
      (let [date (js/Date. iso-datetime)
            year (.getFullYear date)
            month (str (.padStart (str (inc (.getMonth date))) 2 "0"))
            day (str (.padStart (str (.getDate date)) 2 "0"))
            hours (str (.padStart (str (.getHours date)) 2 "0"))
            minutes (str (.padStart (str (.getMinutes date)) 2 "0"))]
        (str year "-" month "-" day "T" hours ":" minutes))
      (catch js/Error e
        (println "Error formatting datetime:" e)
        ""))))

(defn- input-base-props
  "Base properties for input fields"
  [field-key has-error? attrs]
  (let [is-time-field? (#{:worksheet/arrival-time :worksheet/departure-time} field-key)
        form-data @(rf/subscribe [:worksheets/modal-form-data])
        field-value (get form-data field-key "")
        display-value (if (and is-time-field? (= (:type attrs) "datetime-local"))
                        (format-datetime-for-input field-value)
                        (str field-value))
        base-change-handler (fn [e] 
                             (let [value (.. e -target -value)]
                               (rf/dispatch [:worksheets/update-modal-form-field field-key value])
                               (when is-time-field?
                                 ;; Auto-calculate duration for time fields
                                 (let [updated-data (assoc form-data field-key value)
                                       arrival (:worksheet/arrival-time updated-data)
                                       departure (:worksheet/departure-time updated-data)
                                       calculated-duration (calculate-work-duration arrival departure)]
                                   (when calculated-duration
                                     (rf/dispatch [:worksheets/update-modal-form-field :worksheet/work-duration-hours calculated-duration]))))))]
    {:value display-value
     :on-change base-change-handler
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
                    :outline "none"
                    :background (when (:disabled attrs) "#f9fafb")
                    :cursor (when (:disabled attrs) "not-allowed")}
                   (:style attrs)
                   {:focus {:border-color (if has-error? "#dc3545" "#3b82f6")
                           :box-shadow (if has-error? 
                                         "0 0 0 3px rgba(220, 53, 69, 0.1)"
                                         "0 0 0 3px rgba(59, 130, 246, 0.1)")}})}))

(defn- render-textarea
  "Render textarea input"
  [field-key has-error? attrs]
  [:textarea (merge (input-base-props field-key has-error? attrs) (dissoc attrs :type))])

(defn- render-text-input
  "Render text input"
  [field-key has-error? attrs]
  [:input (merge (input-base-props field-key has-error? attrs) attrs)])

(defn- render-select
  "Render select input"
  [field-key has-error? attrs options]
  [:select (merge (input-base-props field-key has-error? attrs) (dissoc attrs :options))
   [:option {:value ""} "Select..."]
   (for [[value label] options]
     ^{:key value}
     [:option {:value value} label])])

(defn- field-input
  "Render appropriate input type"
  [field-key has-error? attrs]
  (cond
    (= (:type attrs) "textarea") (render-textarea field-key has-error? attrs)
    (= (:type attrs) "select") (render-select field-key has-error? attrs (:options attrs))
    :else (render-text-input field-key has-error? attrs)))

(defn- field-error [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- form-field
  "Complete form field with label, input and error"
  [label field-key errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key has-error? attrs]
     [field-error (get errors field-key)]]))

(defn- form-fields
  "All form input fields"
  []
  (let [errors @(rf/subscribe [:worksheets/modal-form-errors])]
    [:div
     [form-field "Serial Number" :worksheet/serial-number errors
      {:type "text" :placeholder "Auto-generated" :disabled true}]
     [form-field "Creation Date" :worksheet/creation-date errors
      {:type "date"}]
     
     ;; Address search field
     [:div {:style {:margin-bottom "1.5rem"}}
      [field-label "Address" :worksheet/address-id (contains? errors :worksheet/address-id)]
      [address-search/address-search-dropdown
       {:component-id :worksheet-form-address
        :workspace-id (get-workspace-id)
        :value @(rf/subscribe [:worksheets/modal-form-field :worksheet/address])
        :on-select (fn [address]
                     (rf/dispatch [:worksheets/modal-form-set-field :worksheet/address address])
                     (rf/dispatch [:worksheets/modal-form-set-field :worksheet/address-id (:address/id address)]))
        :placeholder "Type to search addresses..."
        :disabled false}]
      (when-let [error (:worksheet/address-id errors)]
        [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
         error])]
         
     [form-field "Work Type" :worksheet/work-type errors
      {:type "select" :options [["repair" "Repair"] ["maintenance" "Maintenance"] ["other" "Other"]]}]
     [form-field "Service Type" :worksheet/service-type errors
      {:type "select" :options [["normal" "Normal"] ["night" "Night"] ["weekend" "Weekend"] ["holiday" "Holiday"]]}]
     [form-field "Work Description" :worksheet/work-description errors
      {:type "textarea" :placeholder "Describe the work to be performed..." :rows 4}]
     [form-field "Status" :worksheet/status errors
      {:type "select" :options [["draft" "Draft"] ["in_progress" "In Progress"] ["completed" "Completed"] ["cancelled" "Cancelled"]]}]
     [form-field "Arrival Time" :worksheet/arrival-time errors
      {:type "datetime-local"}]
     [form-field "Departure Time" :worksheet/departure-time errors
      {:type "datetime-local"}]
     [form-field "Work Duration (Hours)" :worksheet/work-duration-hours errors
      {:type "number" :step "1" :placeholder "Auto-calculated from arrival/departure" :disabled true}]
     [:div {:style {:margin-bottom "1.5rem" :font-size "0.75rem" :color "#6b7280"}}
      "💡 Work duration is automatically calculated from arrival and departure times (rounded up to nearest full hour)"]
     [form-field "Notes" :worksheet/notes errors
      {:type "textarea" :placeholder "Optional notes..." :rows 3}]]))

(defn- handle-save-click
  "Handle save button click with validation"
  [on-save]
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        validation-errors (validate-worksheet form-data)]
    (println "DEBUG: handle-save-click called")
    (println "  Form data:" form-data)
    (println "  Validation errors:" validation-errors)
    (if (empty? validation-errors)
      (do (println "  Validation passed, starting save...")
          (rf/dispatch [:worksheets/set-modal-form-loading true])
          (rf/dispatch [:worksheets/set-modal-form-errors {}])
          (on-save form-data (fn [] 
                              (println "  Save callback called")
                              (rf/dispatch [:worksheets/set-modal-form-loading false]))))
      (do (println "  Validation failed, setting errors")
          (rf/dispatch [:worksheets/set-modal-form-errors validation-errors])))))

(defn worksheet-modal
  "Modal for creating/editing worksheets using new UI components"
  [worksheet-data is-new? on-save on-cancel]
  (let [loading? @(rf/subscribe [:worksheets/modal-form-loading?])]
    ;; Initialize form data when modal opens
    (zero-react/use-effect
      {:mount (fn []
                (rf/dispatch [:worksheets/set-modal-form-data worksheet-data])
                ;; Calculate duration for existing data
                (let [arrival (:worksheet/arrival-time worksheet-data)
                      departure (:worksheet/departure-time worksheet-data)
                      calculated-duration (calculate-work-duration arrival departure)]
                  (when calculated-duration
                    (rf/dispatch [:worksheets/update-modal-form-field :worksheet/work-duration-hours calculated-duration]))))
       :params #js [worksheet-data]})
    
    [modal/modal {:on-close (fn []
                             (rf/dispatch [:worksheets/clear-modal-form])
                             (on-cancel)) 
                  :close-on-backdrop? true}
     ^{:key "header"} [modal/modal-header
      {:title (if is-new? "Add New Worksheet" "Edit Worksheet")
       :subtitle (if is-new? 
                   "Create a new worksheet for your workspace"
                   "Update the details of this worksheet")}]
     ^{:key "form"} [form-fields]
     ^{:key "footer"} [modal/modal-footer
      ^{:key "cancel"} [enhanced-button/enhanced-button
       {:variant :secondary
        :on-click (fn []
                   (rf/dispatch [:worksheets/clear-modal-form])
                   (on-cancel))
        :text "Cancel"}]
      ^{:key "save"} [enhanced-button/enhanced-button
       {:variant :primary
        :loading? loading?
        :on-click #(handle-save-click on-save)
        :text (if loading? "Saving..." "Save Worksheet")}]]]))

(defn- worksheet-serial-render
  "Custom render function for worksheet serial number column"
  [serial-number row]
  [:div 
   [:div {:style {:font-weight "600" :color "#111827" :font-size "0.875rem"}}
    serial-number]
   [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem"}}
    (str "Created: " (:worksheet/creation-date row))]])

(defn- work-type-render
  "Custom render function for work type column with service type"
  [work-type row]
  [:div
   [:div {:style {:font-weight "500" :color "#111827" :font-size "0.875rem"
                  :text-transform "capitalize"}}
    work-type]
   [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem"
                  :text-transform "capitalize"}}
    (str "Service: " (:worksheet/service-type row))]])

(defn- status-render
  "Custom render function for status column with colored badges"
  [status row]
  (let [status-colors {"draft" {:bg "#fef3c7" :color "#92400e"}
                      "in_progress" {:bg "#dbeafe" :color "#1e40af"}
                      "completed" {:bg "#d1fae5" :color "#065f46"}
                      "cancelled" {:bg "#fee2e2" :color "#991b1b"}}
        colors (get status-colors status {:bg "#f3f4f6" :color "#374151"})]
    [:span {:style {:display "inline-block" :padding "0.25rem 0.75rem"
                    :background (:bg colors) :color (:color colors)
                    :border-radius "12px" :font-size "0.75rem" :font-weight "500"
                    :text-transform "capitalize"}}
     (str/replace status "_" " ")]))

(defn- address-render
  "Custom render function for address column"
  [address-name row]
  [:div
   [:div {:style {:font-weight "500" :color "#111827" :font-size "0.875rem"}}
    address-name]
   (when (:worksheet/address-city row)
     [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem"}}
      (:worksheet/address-city row)])])

(defn- assigned-to-render
  "Custom render function for assigned to column"
  [assigned-to-name row]
  [:div
   (if assigned-to-name
     [:div {:style {:color "#374151" :font-size "0.875rem" :font-weight "500"}}
      assigned-to-name]
     [:span {:style {:color "#9ca3af" :font-style "italic" :font-size "0.75rem"}}
      "Unassigned"])])



;; Re-frame events and subscriptions
(rf/reg-sub
  :worksheets/data
  (fn [db _]
    (get-in db [:worksheets :data] {:worksheets [] :pagination {}})))

(rf/reg-sub
  :worksheets/loading?
  (fn [db _]
    (get-in db [:worksheets :loading?] false)))

(rf/reg-sub
  :worksheets/modal-worksheet
  (fn [db _]
    (get-in db [:worksheets :modal-worksheet] nil)))

(rf/reg-sub
  :worksheets/modal-is-new?
  (fn [db _]
    (get-in db [:worksheets :modal-is-new?] false)))

(rf/reg-sub
  :worksheets/authenticated?
  (fn [db _]
    (get-in db [:worksheets :authenticated?] nil)))

(rf/reg-event-db
  :worksheets/set-loading
  (fn [db [loading?]]
    (assoc-in db [:worksheets :loading?] loading?)))

(rf/reg-event-db
  :worksheets/set-data
  (fn [db [data]]
    (-> db
        (assoc-in [:worksheets :data] data)
        (assoc-in [:worksheets :loading?] false))))

(rf/reg-event-db
  :worksheets/set-modal-worksheet
  (fn [db [worksheet]]
    (assoc-in db [:worksheets :modal-worksheet] worksheet)))

(rf/reg-event-db
  :worksheets/set-modal-is-new
  (fn [db [is-new?]]
    (assoc-in db [:worksheets :modal-is-new?] is-new?)))

(rf/reg-event-db
  :worksheets/set-authenticated
  (fn [db [authenticated?]]
    (assoc-in db [:worksheets :authenticated?] authenticated?)))

(rf/reg-event-db
  :worksheets/close-modal
  (fn [db _]
    (assoc-in db [:worksheets :modal-worksheet] nil)))

;; Modal form state management
(rf/reg-sub
  :worksheets/modal-form-data
  (fn [db _]
    (get-in db [:worksheets :modal-form-data] {})))

(rf/reg-sub
  :worksheets/modal-form-errors
  (fn [db _]
    (get-in db [:worksheets :modal-form-errors] {})))

(rf/reg-sub
  :worksheets/modal-form-loading?
  (fn [db _]
    (get-in db [:worksheets :modal-form-loading?] false)))

(rf/reg-event-db
  :worksheets/set-modal-form-data
  (fn [db [data]]
    ;; Construct address object from address data
    (let [enhanced-data (if (and (:worksheet/address-id data) 
                                 (:worksheet/address-name data))
                          (assoc data :worksheet/address
                                 {:address/id (:worksheet/address-id data)
                                  :address/name (:worksheet/address-name data)
                                  :address/display (str (:worksheet/address-name data) 
                                                        " - " (:worksheet/address-city data))})
                          data)]
      (assoc-in db [:worksheets :modal-form-data] enhanced-data))))

(rf/reg-event-db
  :worksheets/update-modal-form-field
  (fn [db [field-key value]]
    (assoc-in db [:worksheets :modal-form-data field-key] value)))

(rf/reg-event-db
  :worksheets/set-modal-form-errors
  (fn [db [errors]]
    (assoc-in db [:worksheets :modal-form-errors] errors)))

(rf/reg-event-db
  :worksheets/set-modal-form-loading
  (fn [db [loading?]]
    (assoc-in db [:worksheets :modal-form-loading?] loading?)))

(rf/reg-event-db
  :worksheets/modal-form-set-field
  (fn [db [field-key value]]
    (assoc-in db [:worksheets :modal-form-data field-key] value)))

(rf/reg-sub
  :worksheets/modal-form-field
  (fn [db [_ field-key]]
    (get-in db [:worksheets :modal-form-data field-key])))

(rf/reg-event-db
  :worksheets/clear-modal-form
  (fn [db _]
    (-> db
        (assoc-in [:worksheets :modal-form-data] {})
        (assoc-in [:worksheets :modal-form-errors] {})
        (assoc-in [:worksheets :modal-form-loading?] false))))

;; Event to load worksheets
(rf/reg-event-db
  :worksheets/load-data
  (fn [db [params]]
    (let [workspace-id (get-workspace-id)]
      (load-worksheets-query workspace-id (or params {}))
      db)))

;; Event to handle authentication check
(rf/reg-event-db
  :worksheets/check-authentication
  (fn [db _]
    (parquery/send-queries
     {:queries {:user/current {}}
      :parquery/context {}
      :callback (fn [response]
                 (let [user (:user/current response)]
                   (if (and user (:user/id user))
                     (do 
                       (rf/dispatch [:worksheets/set-authenticated true])
                       (when (empty? (:worksheets (get-in db [:worksheets :data]) []))
                         (rf/dispatch [:worksheets/load-data {}])))
                     (rf/dispatch [:worksheets/set-authenticated false]))))})
    db))

(defn view []
  (let [authenticated? @(rf/subscribe [:worksheets/authenticated?])
        worksheets-data @(rf/subscribe [:worksheets/data])
        loading? @(rf/subscribe [:worksheets/loading?])
        modal-worksheet @(rf/subscribe [:worksheets/modal-worksheet])
        modal-is-new? @(rf/subscribe [:worksheets/modal-is-new?])
        
        workspace-id (get-workspace-id)
        
        load-worksheets (fn [params]
                        (rf/dispatch [:worksheets/load-data params]))
        
        save-worksheet (fn [worksheet callback]
                       (println "DEBUG: save-worksheet wrapper called")
                       (println "  Worksheet data received:" worksheet)
                       (println "  Modal is new?:" modal-is-new?)
                       (save-worksheet-query worksheet workspace-id modal-is-new? callback modal-worksheet (fn [] (load-worksheets {}))))
        
        delete-worksheet (fn [worksheet-id]
                         (delete-worksheet-query worksheet-id workspace-id (fn [] (load-worksheets {}))))]
    
    ;; Initialize authentication check on mount
    (zero-react/use-effect
      {:mount (fn []
                (rf/dispatch [:worksheets/check-authentication]))
       :params #js[]})
    
    (cond
      (nil? authenticated?)
      [:div {:style {:padding "2rem" :text-align "center"}}
       [:div "Checking authentication..."]]
      
      (false? authenticated?)
      (do 
        (println "User not authenticated, redirecting to login")
        (set! (.-location js/window) "/login")
        [:div])
      
      :else
      [:div {:style {:min-height "100vh" :background "#f9fafb"}}
       [:div {:style {:max-width "1200px" :margin "0 auto" :padding "2rem"}}
        ;; Page header with modal controls
        [page-header/page-header
         {:title "Worksheets"
          :description "Manage worksheets for this workspace"
          :action-button [enhanced-button/enhanced-button
                          {:variant :success
                           :on-click (fn [] 
                                      (rf/dispatch [:worksheets/set-modal-worksheet {:worksheet/status "draft"
                                                                                    :worksheet/work-type ""
                                                                                    :worksheet/service-type ""
                                                                                    :worksheet/creation-date (.toISOString (js/Date.))}])
                                      (rf/dispatch [:worksheets/set-modal-is-new true]))
                           :text "+ Add New Worksheet"}]}]
        
        ;; Worksheets table
        [data-table/server-side-data-table
         {:headers [{:key :worksheet/serial-number :label "Serial Number" :render worksheet-serial-render :sortable? true}
                    {:key :worksheet/work-type :label "Work Type" :render work-type-render :sortable? true}
                    {:key :worksheet/status :label "Status" :render status-render :sortable? true}
                    {:key :worksheet/address-name :label "Address" :render address-render :sortable? true}
                    {:key :worksheet/assigned-to-name :label "Assigned To" :render assigned-to-render :sortable? true}]
          :data-source worksheets-data
          :loading? loading?
          :empty-message "No worksheets found"
          :id-key :worksheet/id
          :table-id :worksheets-table
          :show-search? true
          :show-pagination? true
          :query-fn load-worksheets
          :actions [{:key :edit :label "Edit" :variant :primary 
                     :on-click (fn [worksheet]
                                (rf/dispatch [:worksheets/set-modal-worksheet worksheet])
                                (rf/dispatch [:worksheets/set-modal-is-new false]))}
                    {:key :pdf :label "PDF" :variant :secondary
                     :on-click (fn [worksheet]
                                (let [pdf-url (str "/pdf-generator/worksheet/" (:worksheet/id worksheet))]
                                  (js/window.open pdf-url "_blank")))}
                    {:key :delete :label "Delete" :variant :danger 
                     :on-click (fn [row] 
                                (when (js/confirm "Are you sure you want to delete this worksheet?")
                                  (delete-worksheet (:worksheet/id row))))}]}]
        
        ;; Modal when open
        (when modal-worksheet
          [worksheet-modal modal-worksheet modal-is-new? save-worksheet
           (fn [] (rf/dispatch [:worksheets/close-modal]))])]])))