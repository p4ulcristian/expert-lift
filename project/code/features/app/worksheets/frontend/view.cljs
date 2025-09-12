(ns features.app.worksheets.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame]
            [zero.frontend.react :as zero-react]
            [ui.modal :as modal]
            [ui.form-field :as form-field]
            [ui.data-table :as data-table]
            [ui.enhanced-button :as enhanced-button]
            [ui.page-header :as page-header]))

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
  [workspace-id loading? worksheets params]
  (println "DEBUG load-worksheets-query called with params:" params)
  (reset! loading? true)
  (parquery/send-queries
   {:queries {:workspace-worksheets/get-paginated params}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (println "DEBUG load-worksheets-query response:" response)
               (reset! loading? false)
               (let [result (:workspace-worksheets/get-paginated response)]
                 (reset! worksheets result)))}))

(defn- get-query-type
  "Get appropriate query type for save operation"
  [is-new?]
  (if @is-new? 
    :workspace-worksheets/create 
    :workspace-worksheets/update))

(defn- prepare-worksheet-data
  "Prepare worksheet data for save"
  [worksheet is-new?]
  (if @is-new?
    (dissoc worksheet :worksheet/id)
    worksheet))

(defn- handle-save-response
  "Handle save response and update UI"
  [response query-type callback modal-worksheet load-worksheets]
  (callback)
  (if (:success (get response query-type))
    (do (reset! modal-worksheet nil)
        (load-worksheets))
    (js/alert (str "Error: " (:error (get response query-type))))))

(defn- save-worksheet-query
  "Execute ParQuery to save worksheet"
  [worksheet workspace-id modal-is-new? callback modal-worksheet load-worksheets]
  (let [query-type (get-query-type modal-is-new?)
        worksheet-data (prepare-worksheet-data worksheet modal-is-new?)
        context {:workspace-id workspace-id}]
    (println "DEBUG: save-worksheet-query called")
    (println "  Workspace ID:" workspace-id)
    (println "  Query type:" query-type)
    (println "  Worksheet data:" worksheet-data)
    (println "  Context being sent:" context)
    (parquery/send-queries
     {:queries {query-type worksheet-data}
      :parquery/context context
      :callback (fn [response]
                 (println "DEBUG: save-worksheet-query response:" response)
                 (handle-save-response response query-type callback modal-worksheet load-worksheets))})))

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
      (empty? status) (assoc :worksheet/status "Status is required"))))

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
            ;; Round up to nearest quarter hour
            (Math/ceil (* diff-hours 4)) / 4)))
      (catch js/Error e
        (println "Error calculating duration:" e)
        nil))))

(defn- update-duration-if-needed
  "Update work duration when arrival or departure times change"
  [worksheet]
  (let [arrival (:worksheet/arrival-time @worksheet)
        departure (:worksheet/departure-time @worksheet)
        calculated-duration (calculate-work-duration arrival departure)]
    (when calculated-duration
      (swap! worksheet assoc :worksheet/work-duration-hours calculated-duration))))

(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                   :font-size "0.875rem" :letter-spacing "0.025em"
                   :color (if has-error? "#dc3545" "#374151")}}
   label 
   (when (#{:worksheet/serial-number :worksheet/work-description :worksheet/work-type :worksheet/service-type :worksheet/status} field-key) 
     [:span {:style {:color "#ef4444" :margin-left "0.25rem"}} "*"])])

(defn- input-base-props
  "Base properties for input fields"
  [field-key worksheet has-error? attrs]
  (let [is-time-field? (#{:worksheet/arrival-time :worksheet/departure-time} field-key)
        base-change-handler (fn [e] 
                             (swap! worksheet assoc field-key (.. e -target -value))
                             (when is-time-field?
                               (update-duration-if-needed worksheet)))]
    {:value (str (get @worksheet field-key ""))
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
  [field-key worksheet has-error? attrs]
  [:textarea (merge (input-base-props field-key worksheet has-error? attrs) (dissoc attrs :type))])

(defn- render-text-input
  "Render text input"
  [field-key worksheet has-error? attrs]
  [:input (merge (input-base-props field-key worksheet has-error? attrs) attrs)])

(defn- render-select
  "Render select input"
  [field-key worksheet has-error? attrs options]
  [:select (merge (input-base-props field-key worksheet has-error? attrs) (dissoc attrs :options))
   [:option {:value ""} "Select..."]
   (for [[value label] options]
     ^{:key value}
     [:option {:value value} label])])

(defn- field-input
  "Render appropriate input type"
  [field-key worksheet has-error? attrs]
  (cond
    (= (:type attrs) "textarea") (render-textarea field-key worksheet has-error? attrs)
    (= (:type attrs) "select") (render-select field-key worksheet has-error? attrs (:options attrs))
    :else (render-text-input field-key worksheet has-error? attrs)))

(defn- field-error [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- form-field
  "Complete form field with label, input and error"
  [label field-key worksheet errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key worksheet has-error? attrs]
     [field-error (get errors field-key)]]))

(defn- form-fields
  "All form input fields"
  [worksheet errors]
  [:div
   [form-field "Serial Number" :worksheet/serial-number worksheet errors
    {:type "text" :placeholder "e.g. 2024-01-15/001"}]
   [form-field "Creation Date" :worksheet/creation-date worksheet errors
    {:type "date"}]
   [form-field "Work Type" :worksheet/work-type worksheet errors
    {:type "select" :options [["repair" "Repair"] ["maintenance" "Maintenance"] ["other" "Other"]]}]
   [form-field "Service Type" :worksheet/service-type worksheet errors
    {:type "select" :options [["normal" "Normal"] ["night" "Night"] ["weekend" "Weekend"] ["holiday" "Holiday"]]}]
   [form-field "Work Description" :worksheet/work-description worksheet errors
    {:type "textarea" :placeholder "Describe the work to be performed..." :rows 4}]
   [form-field "Status" :worksheet/status worksheet errors
    {:type "select" :options [["draft" "Draft"] ["in_progress" "In Progress"] ["completed" "Completed"] ["cancelled" "Cancelled"]]}]
   [form-field "Arrival Time" :worksheet/arrival-time worksheet errors
    {:type "datetime-local"}]
   [form-field "Departure Time" :worksheet/departure-time worksheet errors
    {:type "datetime-local"}]
   [form-field "Work Duration (Hours)" :worksheet/work-duration-hours worksheet errors
    {:type "number" :step "0.25" :placeholder "Auto-calculated from arrival/departure" :disabled true}]
   [:div {:style {:margin-bottom "1.5rem" :font-size "0.75rem" :color "#6b7280"}}
    "💡 Work duration is automatically calculated from arrival and departure times (rounded up to nearest quarter hour)"]
   [form-field "Notes" :worksheet/notes worksheet errors
    {:type "textarea" :placeholder "Optional notes..." :rows 3}]])

(defn- handle-save-click
  "Handle save button click with validation"
  [worksheet loading? errors on-save]
  (let [validation-errors (validate-worksheet @worksheet)]
    (if (empty? validation-errors)
      (do (reset! loading? true)
          (reset! errors {})
          (on-save @worksheet (fn [] (reset! loading? false))))
      (reset! errors validation-errors))))

(defn worksheet-modal
  "Modal for creating/editing worksheets using new UI components"
  [worksheet-data is-new? on-save on-cancel]
  (let [loading? (r/atom false)
        errors (r/atom {})
        worksheet (r/atom worksheet-data)]
    (fn [worksheet-data is-new? on-save on-cancel]
      (reset! worksheet worksheet-data)
      ;; Calculate duration when modal opens with existing data
      (update-duration-if-needed worksheet)
      [modal/modal {:on-close on-cancel :close-on-backdrop? true}
       ^{:key "header"} [modal/modal-header
        {:title (if is-new? "Add New Worksheet" "Edit Worksheet")
         :subtitle (if is-new? 
                     "Create a new worksheet for your workspace"
                     "Update the details of this worksheet")}]
       ^{:key "form"} [form-fields worksheet @errors]
       ^{:key "footer"} [modal/modal-footer
        ^{:key "cancel"} [enhanced-button/enhanced-button
         {:variant :secondary
          :on-click on-cancel
          :text "Cancel"}]
        ^{:key "save"} [enhanced-button/enhanced-button
         {:variant :primary
          :loading? @loading?
          :on-click #(handle-save-click worksheet loading? errors on-save)
          :text (if @loading? "Saving..." "Save Worksheet")}]]])))

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

(defn worksheets-table
  "Worksheets table using server-side data-table component with search, sorting, and pagination"
  [worksheets loading? on-edit on-delete query-fn]
  [data-table/server-side-data-table
   {:headers [{:key :worksheet/serial-number :label "Serial Number" :render worksheet-serial-render :sortable? true}
              {:key :worksheet/work-type :label "Work Type" :render work-type-render :sortable? true}
              {:key :worksheet/status :label "Status" :render status-render :sortable? true}
              {:key :worksheet/address-name :label "Address" :render address-render :sortable? true}
              {:key :worksheet/assigned-to-name :label "Assigned To" :render assigned-to-render :sortable? true}]
    :data-source @worksheets
    :loading? @loading?
    :empty-message "No worksheets found"
    :id-key :worksheet/id
    :table-id :worksheets-table
    :show-search? true
    :show-pagination? true
    :query-fn query-fn
    :on-data-change (fn [result] (reset! worksheets result))
    :actions [{:key :edit :label "Edit" :variant :primary :on-click on-edit}
              {:key :delete :label "Delete" :variant :danger 
               :on-click (fn [row] 
                          (when (js/confirm "Are you sure you want to delete this worksheet?")
                            (on-delete (:worksheet/id row))))}]}])

(defn- worksheets-page-header
  "Page header with title and add button using new UI component"
  [modal-worksheet modal-is-new?]
  [page-header/page-header
   {:title "Worksheets"
    :description "Manage worksheets for this workspace"
    :action-button [enhanced-button/enhanced-button
                    {:variant :success
                     :on-click (fn [] 
                                (reset! modal-worksheet {:worksheet/status "draft"
                                                        :worksheet/work-type ""
                                                        :worksheet/service-type ""
                                                        :worksheet/creation-date (.toISOString (js/Date.))})
                                (reset! modal-is-new? true))
                     :text "+ Add New Worksheet"}]}])

(defn- worksheets-content
  "Main content area with server-side data table"
  [worksheets loading? modal-worksheet modal-is-new? delete-worksheet query-fn]
  [worksheets-table 
   worksheets 
   loading?
   (fn [worksheet]
     (reset! modal-worksheet worksheet)
     (reset! modal-is-new? false))
   delete-worksheet
   query-fn])

(defn- modal-when-open
  "Render modal when worksheet is selected"
  [modal-worksheet modal-is-new? save-worksheet]
  (when @modal-worksheet
    [worksheet-modal @modal-worksheet @modal-is-new? save-worksheet
     (fn [] (reset! modal-worksheet nil))]))

(defn view []
  (let [authenticated? (r/atom nil)  ; nil = checking, true = authenticated, false = not authenticated
        workspace-id (get-workspace-id)
        worksheets (r/atom [])
        loading? (r/atom false)
        modal-worksheet (r/atom nil)
        modal-is-new? (r/atom false)
        
        load-worksheets (fn [params]
                        (load-worksheets-query workspace-id loading? worksheets (or params {})))
        
        save-worksheet (fn [worksheet callback]
                       (save-worksheet-query worksheet workspace-id modal-is-new? 
                                          callback modal-worksheet (fn [] (load-worksheets {}))))
        
        delete-worksheet (fn [worksheet-id]
                         (delete-worksheet-query worksheet-id workspace-id (fn [] (load-worksheets {}))))]
    
    (fn []
      ;; Call useEffect hook inside the render function
      (zero-react/use-effect
        {:mount (fn [] 
                  ;; Check authentication first
                  (parquery/send-queries
                   {:queries {:user/current {}}
                    :parquery/context {}
                    :callback (fn [response]
                               (let [user (:user/current response)]
                                 (if (and user (:user/id user))
                                   (do 
                                     (reset! authenticated? true)
                                     ;; Load initial worksheets after authentication is confirmed
                                     (when (empty? (:worksheets @worksheets [])) (load-worksheets {})))
                                   (reset! authenticated? false))))}))
         :params #js[]})
      
      (cond
        (nil? @authenticated?)
        [:div {:style {:padding "2rem" :text-align "center"}}
         [:div "Checking authentication..."]]
        
        (false? @authenticated?)
        (do 
          (println "User not authenticated, redirecting to login")
          (set! (.-location js/window) "/login")
          [:div])
        
        :else
        [:div {:style {:min-height "100vh" :background "#f9fafb"}}
         [:div {:style {:max-width "1200px" :margin "0 auto" :padding "2rem"}}
          [worksheets-page-header modal-worksheet modal-is-new?]
          [worksheets-content worksheets loading? modal-worksheet modal-is-new? delete-worksheet load-worksheets]
          [modal-when-open modal-worksheet modal-is-new? save-worksheet]]]))))