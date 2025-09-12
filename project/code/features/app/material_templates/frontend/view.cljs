(ns features.app.material-templates.frontend.view
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
            [ui.page-header :as page-header]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    workspace-id))


(defn- get-query-type
  "Get appropriate query type for save operation"
  [is-new?]
  (if @is-new? 
    :workspace-material-templates/create 
    :workspace-material-templates/update))

(defn- prepare-template-data
  "Prepare template data for save"
  [template is-new?]
  (if @is-new?
    (dissoc template :material-template/id)
    template))

(defn- handle-save-response
  "Handle save response and update UI"
  [response query-type callback modal-template load-templates]
  (callback)
  (if (:success (get response query-type))
    (do (reset! modal-template nil)
        (load-templates))
    (js/alert (str "Error: " (:error (get response query-type))))))

(defn- save-template-query
  "Execute ParQuery to save template"
  [template workspace-id modal-is-new? callback modal-template load-templates]
  (let [query-type (get-query-type modal-is-new?)
        template-data (prepare-template-data template modal-is-new?)
        context {:workspace-id workspace-id}]
    (println "DEBUG: save-template-query called")
    (println "  Workspace ID:" workspace-id)
    (println "  Query type:" query-type)
    (println "  Template data:" template-data)
    (println "  Context being sent:" context)
    (parquery/send-queries
     {:queries {query-type template-data}
      :parquery/context context
      :callback (fn [response]
                 (println "DEBUG: save-template-query response:" response)
                 (handle-save-response response query-type callback modal-template load-templates))})))

(defn- delete-template-query
  "Execute ParQuery to delete template"
  [template-id workspace-id load-templates]
  (parquery/send-queries
   {:queries {:workspace-material-templates/delete {:material-template/id template-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (if (:success (:workspace-material-templates/delete response))
                 (load-templates)
                 (js/alert "Error deleting template")))}))

(defn- validate-name
  "Validate template name"
  [name]
  (< (count (str/trim (str name))) 2))

(defn- validate-unit
  "Validate template unit"
  [unit]
  (< (count (str/trim (str unit))) 1))

(defn validate-material-template
  "Validates material template data and returns map of field errors"
  [template]
  (let [errors {}
        name (:material-template/name template)
        unit (:material-template/unit template)]
    (cond-> errors
      (validate-name name) (assoc :material-template/name "Name is required")
      (validate-unit unit) (assoc :material-template/unit "Unit is required"))))

(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                   :font-size "0.875rem" :letter-spacing "0.025em"
                   :color (if has-error? "#dc3545" "#374151")}}
   label 
   (when (#{:material-template/name :material-template/unit} field-key) 
     [:span {:style {:color "#ef4444" :margin-left "0.25rem"}} "*"])])

(defn- input-base-props
  "Base properties for input fields"
  [field-key template has-error? attrs]
  {:value (str (get @template field-key ""))
   :on-change #(swap! template assoc field-key (.. % -target -value))
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

(defn- render-textarea
  "Render textarea input"
  [field-key template has-error? attrs]
  [:textarea (merge (dissoc attrs :type) (input-base-props field-key template has-error? attrs))])

(defn- render-text-input
  "Render text input"
  [field-key template has-error? attrs]
  [:input (merge attrs (input-base-props field-key template has-error? attrs))])

(defn- field-input
  "Render appropriate input type"
  [field-key template has-error? attrs]
  (if (= (:type attrs) "textarea")
    (render-textarea field-key template has-error? attrs)
    (render-text-input field-key template has-error? attrs)))

(defn- field-error [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- form-field
  "Complete form field with label, input and error"
  [label field-key template errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key template has-error? attrs]
     [field-error (get errors field-key)]]))

(defn- form-fields
  "All form input fields"
  [template errors]
  [:div
   [form-field "Name" :material-template/name template errors
    {:type "text" :placeholder "e.g. Steel Cable"}]
   [form-field "Unit" :material-template/unit template errors
    {:type "text" :placeholder "e.g. m, kg, pcs"}]
   [form-field "Category" :material-template/category template errors
    {:type "text" :placeholder "e.g. Cables, Hardware"}]
   [form-field "Description" :material-template/description template errors
    {:type "textarea" :placeholder "Optional description" :rows 3}]])

(defn- active-checkbox
  "Active status checkbox for existing templates"
  [template is-new?]
  (when-not is-new?
    [:div {:style {:margin-bottom "1.5rem" :padding "1rem" :background "#f9fafb" 
                   :border "1px solid #e5e7eb" :border-radius "8px"}}
     [:label {:style {:display "flex" :align-items "center" :font-weight "600" 
                      :color "#374151" :cursor "pointer"}}
      [:input {:type "checkbox"
               :checked (boolean (:material-template/active @template))
               :on-change #(swap! template assoc :material-template/active (.. % -target -checked))
               :style {:margin-right "0.75rem" :width "1rem" :height "1rem" 
                       :accent-color "#3b82f6" :cursor "pointer"}}]
      "Active Template"
      [:span {:style {:color "#6b7280" :font-weight "normal" :margin-left "0.5rem"}}
       "(Uncheck to disable this template)"]]]))

(defn- handle-save-click
  "Handle save button click with validation"
  [template loading? errors on-save]
  (let [validation-errors (validate-material-template @template)]
    (if (empty? validation-errors)
      (do (reset! loading? true)
          (reset! errors {})
          (on-save @template (fn [] (reset! loading? false))))
      (reset! errors validation-errors))))


(defn material-template-modal
  "Modal for creating/editing material templates using new UI components"
  [template-data is-new? on-save on-cancel]
  (let [loading? (r/atom false)
        errors (r/atom {})
        template (r/atom template-data)]
    (fn [template-data is-new? on-save on-cancel]
      (reset! template template-data)
      [modal/modal {:on-close on-cancel :close-on-backdrop? true}
       ^{:key "header"} [modal/modal-header
        {:title (if is-new? "Add New Material Template" "Edit Material Template")
         :subtitle (if is-new? 
                     "Create a new material template for your workspace"
                     "Update the details of this material template")}]
       ^{:key "form"} [form-fields template @errors]
       ^{:key "checkbox"} [active-checkbox template is-new?]
       ^{:key "footer"} [modal/modal-footer
        ^{:key "cancel"} [enhanced-button/enhanced-button
         {:variant :secondary
          :on-click on-cancel
          :text "Cancel"}]
        ^{:key "save"} [enhanced-button/enhanced-button
         {:variant :primary
          :loading? @loading?
          :on-click #(handle-save-click template loading? errors on-save)
          :text (if @loading? "Saving..." "Save Template")}]]])))

(defn- template-name-render
  "Custom render function for template name column with description"
  [name row]
  [:div 
   [:div {:style {:font-weight "600" :color "#111827" :font-size "0.875rem"}}
    name]
   (when (:material-template/description row)
     [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem" :line-height "1.4"}}
      (:material-template/description row)])])

(defn- category-render
  "Custom render function for category with fallback text"
  [category row]
  (or category 
      [:span {:style {:color "#9ca3af" :font-style "italic"}} "No category"]))

(defn material-templates-table
  "Material templates table using server-side data-table component with search, sorting, and pagination"  
  [templates-data loading? on-edit on-delete query-fn]
  (println "DEBUG: material-templates-table called")
  (println "DEBUG: templates-data:" templates-data)
  (println "DEBUG: loading?:" loading?)
  (println "DEBUG: query-fn:" query-fn)
  [data-table/server-side-data-table
   {:headers [{:key :material-template/name :label "Material" :render template-name-render :sortable? true}
              {:key :material-template/unit :label "Unit" :sortable? true
               :cell-style {:color "#374151" :font-weight "500" :font-size "0.875rem"}}
              {:key :material-template/category :label "Category" :render category-render :sortable? true
               :cell-style {:color "#6b7280" :font-size "0.875rem"}}
              {:key :material-template/active :label "Status" :sortable? true
               :render (fn [active? _] [data-table/status-badge active?])}]
    :data-source templates-data
    :data-key :material-templates
    :loading? loading?
    :empty-message "No material templates found"
    :id-key :material-template/id
    :table-id :material-templates-table
    :show-search? true
    :show-pagination? true
    :query-fn query-fn
    :actions [{:key :edit :label "Edit" :variant :primary :on-click on-edit}
              {:key :delete :label "Delete" :variant :danger 
               :on-click (fn [row] 
                          (when (js/confirm "Are you sure you want to delete this template?")
                            (on-delete (:material-template/id row))))}]}])

(defn- templates-page-header
  "Page header with title and add button using new UI component"
  [modal-template modal-is-new?]
  [page-header/page-header
   {:title "Material Templates"
    :description "Manage your material templates for this workspace"
    :action-button [enhanced-button/enhanced-button
                    {:variant :success
                     :on-click (fn [] 
                                (reset! modal-template {:material-template/active true})
                                (reset! modal-is-new? true))
                     :text "+ Add New Template"}]}])

(defn- templates-content
  "Main content area with server-side data table"
  [templates-data-atom loading?-atom modal-template modal-is-new? delete-template query-fn]
  [material-templates-table 
   @templates-data-atom
   @loading?-atom
   (fn [template]
     (reset! modal-template template)
     (reset! modal-is-new? false))
   delete-template
   query-fn])

(defn- modal-when-open
  "Render modal when template is selected"
  [modal-template modal-is-new? save-template]
  (when @modal-template
    [material-template-modal @modal-template @modal-is-new? save-template
     (fn [] (reset! modal-template nil))]))

;; Re-frame subscriptions and events
(rf/reg-sub
  :material-templates/data
  (fn [db _]
    (let [raw-data (get-in db [:material-templates :data])
          _ (println "DEBUG: subscription raw-data:" raw-data)]
      (if (and raw-data (:material-templates raw-data))
        ;; If we have the old structure, use it as is
        raw-data
        ;; Otherwise return default structure
        {:material-templates [] :pagination {}}))))

(rf/reg-sub
  :material-templates/loading?
  (fn [db _]
    (get-in db [:material-templates :loading?] false)))

(rf/reg-event-db
  :material-templates/set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:material-templates :loading?] loading?)))

(rf/reg-event-db
  :material-templates/load-success
  (fn [db [_ data]]
    (-> db
        (assoc-in [:material-templates :data] data)
        (assoc-in [:material-templates :loading?] false))))


(defn view []
  (let [workspace-id (get-workspace-id)
        loading? (r/atom false)
        modal-template (r/atom nil)
        modal-is-new? (r/atom false)
        
        templates-data-atom (r/atom {:material-templates [] :pagination {}})
        
        load-templates (fn [params]
                         (println "DEBUG: load-templates called with params:" params)
                         (println "DEBUG: workspace-id:" workspace-id)
                         (reset! loading? true)
                         (parquery/send-queries
                          {:queries {:workspace-material-templates/get-paginated (or params {})}
                           :parquery/context {:workspace-id workspace-id}
                           :callback (fn [response]
                                      (println "DEBUG: load-templates response:" response)
                                      (reset! loading? false)
                                      (let [result (:workspace-material-templates/get-paginated response)
                                            formatted-result {:material-templates (:material-templates result)
                                                            :pagination {:total-count (:total-count result)
                                                                       :page (:page result) 
                                                                       :page-size (:page-size result)
                                                                       :total-pages (:total-pages result)}}]
                                        (println "DEBUG: formatted result:" formatted-result)
                                        (reset! templates-data-atom formatted-result)))}))
        
        save-template (fn [template callback]
                        (save-template-query template workspace-id modal-is-new? 
                                             callback modal-template (fn [] (load-templates {}))))
        
        delete-template (fn [template-id]
                          (delete-template-query template-id workspace-id (fn [] (load-templates {}))))]
    
    
    ;; Initial load
    (println "DEBUG: Material templates view function called - NEW VERSION")
    (zero-react/use-effect
      {:mount (fn []
                (println "DEBUG: Initial load triggered")
                (load-templates {}))
       :params #js []})
    
    [:div {:style {:min-height "100vh" :background "#f9fafb"}}
     [:div {:style {:max-width "1200px" :margin "0 auto" :padding "2rem"}} 
      [templates-page-header modal-template modal-is-new?]
      [templates-content templates-data-atom loading? modal-template modal-is-new? delete-template load-templates]
      [modal-when-open modal-template modal-is-new? save-template]]]))