(ns features.app.workspace.material-templates.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame]
            [zero.frontend.react :as zero-react]))

(defn- get-workspace-id []
  "Get workspace ID from router parameters"
  (get-in @router/state [:parameters :path :workspace-id]))

(defn- load-templates-query [workspace-id loading? templates]
  "Execute ParQuery to load templates"
  (parquery/send-queries
   {:queries {:workspace-material-templates/get-all {}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (reset! loading? false)
               (let [result (:workspace-material-templates/get-all response)]
                 (reset! templates (or result []))))}))

(defn- get-query-type [is-new?]
  "Get appropriate query type for save operation"
  (if @is-new? 
    :workspace-material-templates/create 
    :workspace-material-templates/update))

(defn- prepare-template-data [template is-new?]
  "Prepare template data for save"
  (if @is-new?
    (dissoc template :material-template/id)
    template))

(defn- handle-save-response [response query-type callback modal-template load-templates]
  "Handle save response and update UI"
  (callback)
  (if (:success (get response query-type))
    (do (reset! modal-template nil)
        (load-templates))
    (js/alert (str "Error: " (:error (get response query-type))))))

(defn- save-template-query [template workspace-id modal-is-new? callback modal-template load-templates]
  "Execute ParQuery to save template"
  (let [query-type (get-query-type modal-is-new?)
        template-data (prepare-template-data template modal-is-new?)]
    (parquery/send-queries
     {:queries {query-type template-data}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                 (handle-save-response response query-type callback modal-template load-templates))})))

(defn- delete-template-query [template-id workspace-id load-templates]
  "Execute ParQuery to delete template"
  (parquery/send-queries
   {:queries {:workspace-material-templates/delete {:material-template/id template-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (if (:success (:workspace-material-templates/delete response))
                 (load-templates)
                 (js/alert "Error deleting template")))}))

(defn- validate-name [name]
  "Validate template name"
  (< (count (str/trim (str name))) 2))

(defn- validate-unit [unit]
  "Validate template unit"
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
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "bold"
                   :color (if has-error? "#dc3545" "inherit")}}
   label (when (#{:material-template/name :material-template/unit} field-key) " *")])

(defn- input-base-props [field-key template has-error? attrs]
  "Base properties for input fields"
  {:value (str (get @template field-key ""))
   :on-change #(swap! template assoc field-key (.. % -target -value))
   :style (merge (:style attrs) (when has-error? {:border "2px solid #dc3545"}))})

(defn- render-textarea [field-key template has-error? attrs]
  "Render textarea input"
  [:textarea (merge (dissoc attrs :type) (input-base-props field-key template has-error? attrs))])

(defn- render-text-input [field-key template has-error? attrs]
  "Render text input"
  [:input (merge attrs (input-base-props field-key template has-error? attrs))])

(defn- field-input [field-key template has-error? attrs]
  "Render appropriate input type"
  (if (= (:type attrs) "textarea")
    (render-textarea field-key template has-error? attrs)
    (render-text-input field-key template has-error? attrs)))

(defn- field-error [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- form-field [label field-key template errors attrs]
  "Complete form field with label, input and error"
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1rem"}}
     [field-label label field-key has-error?]
     [field-input field-key template has-error? attrs]
     [field-error (get errors field-key)]]))

(defn- modal-overlay []
  "Modal overlay background"
  {:position "fixed" :top 0 :left 0 :right 0 :bottom 0
   :background "rgba(0,0,0,0.5)" :z-index 1000
   :display "flex" :align-items "center" :justify-content "center"})

(defn- modal-content []
  "Modal content container styles"
  {:background "white" :padding "2rem" :border-radius "8px"
   :min-width "400px" :max-width "600px" :max-height "80vh" :overflow "auto"})

(defn- modal-title [is-new?]
  "Modal title based on create/edit mode"
  [:h3 (if is-new? "Add New Material Template" "Edit Material Template")])

(defn- form-fields [template errors]
  "All form input fields"
  [:div
   [form-field "Name" :material-template/name template errors
    {:type "text" :placeholder "e.g. Steel Cable"}]
   [form-field "Unit" :material-template/unit template errors
    {:type "text" :placeholder "e.g. m, kg, pcs"}]
   [form-field "Category" :material-template/category template errors
    {:type "text" :placeholder "e.g. Cables, Hardware"}]
   [form-field "Description" :material-template/description template errors
    {:type "textarea" :placeholder "Optional description" :rows 3}]])

(defn- active-checkbox [template is-new?]
  "Active status checkbox for existing templates"
  (when-not is-new?
    [:div {:style {:margin-bottom "1rem"}}
     [:label {:style {:display "flex" :align-items "center" :font-weight "bold"}}
      [:input {:type "checkbox"
               :checked (boolean (:material-template/active @template))
               :on-change #(swap! template assoc :material-template/active (.. % -target -checked))
               :style {:margin-right "0.5rem"}}]
      "Active"]]))

(defn- handle-save-click [template loading? errors on-save]
  "Handle save button click with validation"
  (let [validation-errors (validate-material-template @template)]
    (if (empty? validation-errors)
      (do (reset! loading? true)
          (reset! errors {})
          (on-save @template (fn [] (reset! loading? false))))
      (reset! errors validation-errors))))

(defn- modal-buttons [template loading? errors on-save on-cancel]
  "Save and Cancel buttons"
  [:div {:style {:display "flex" :gap "1rem" :margin-top "2rem"}}
   [:button {:type "button" :disabled @loading?
             :on-click #(handle-save-click template loading? errors on-save)
             :style {:padding "0.5rem 1rem" :background "#007bff" :color "white"
                     :border "none" :border-radius "4px" :cursor "pointer"}}
    (if @loading? "Saving..." "Save")]
   [:button {:type "button" :on-click on-cancel
             :style {:padding "0.5rem 1rem" :background "#6c757d" :color "white"
                     :border "none" :border-radius "4px" :cursor "pointer"}}
    "Cancel"]])

(defn material-template-modal
  "Modal for creating/editing material templates"
  [template-data is-new? on-save on-cancel]
  (let [loading? (r/atom false)
        errors (r/atom {})
        template (r/atom template-data)]
    (fn [template-data is-new? on-save on-cancel]
      (reset! template template-data)
      [:div {:style (modal-overlay)}
       [:div {:style (modal-content)}
        [modal-title is-new?]
        [form-fields template @errors]
        [active-checkbox template is-new?]
        [modal-buttons template loading? errors on-save on-cancel]]])))

(defn- table-header-style []
  {:padding "0.75rem" :text-align "left" :border "1px solid #dee2e6"})

(defn- table-cell-style []
  {:padding "0.75rem" :border "1px solid #dee2e6"})

(defn- template-name-cell [template]
  [:td {:style (table-cell-style)}
   [:div [:strong (:material-template/name template)]
    (when (:material-template/description template)
      [:div {:style {:color "#6c757d" :font-size "0.875rem"}}
       (:material-template/description template)])]])

(defn material-template-table
  "Table displaying material templates"
  [templates on-edit on-delete]
  [:div
   [:table {:style {:width "100%" :border-collapse "collapse"}}
    [:thead
     [:tr {:style {:background "#f8f9fa"}}
      [:th {:style {:padding "0.75rem" :text-align "left" :border "1px solid #dee2e6"}} "Name"]
      [:th {:style {:padding "0.75rem" :text-align "left" :border "1px solid #dee2e6"}} "Unit"]
      [:th {:style {:padding "0.75rem" :text-align "left" :border "1px solid #dee2e6"}} "Category"]
      [:th {:style {:padding "0.75rem" :text-align "left" :border "1px solid #dee2e6"}} "Active"]
      [:th {:style {:padding "0.75rem" :text-align "center" :border "1px solid #dee2e6"}} "Actions"]]]
    [:tbody
     (for [template templates]
       ^{:key (:material-template/id template)}
       [:tr
        [template-name-cell template]
        [:td {:style {:padding "0.75rem" :border "1px solid #dee2e6"}}
         (:material-template/unit template)]
        [:td {:style {:padding "0.75rem" :border "1px solid #dee2e6"}}
         (or (:material-template/category template) "-")]
        [:td {:style {:padding "0.75rem" :border "1px solid #dee2e6"}}
         (if (:material-template/active template) "Yes" "No")]
        [:td {:style {:padding "0.75rem" :border "1px solid #dee2e6" :text-align "center"}}
         [:button {:type "button"
                   :on-click #(on-edit template)
                   :style {:padding "0.25rem 0.5rem" :margin-right "0.5rem"
                           :background "#ffc107" :color "black" :border "none"
                           :border-radius "4px" :cursor "pointer"}}
          "Edit"]
         [:button {:type "button"
                   :on-click (fn [] (when (js/confirm "Are you sure you want to delete this template?")
                                     (on-delete (:material-template/id template))))
                   :style {:padding "0.25rem 0.5rem"
                           :background "#dc3545" :color "white" :border "none"
                           :border-radius "4px" :cursor "pointer"}}
          "Delete"]]])]]])

(defn- page-header [modal-template modal-is-new?]
  "Page header with title and add button"
  [:div {:style {:display "flex" :justify-content "space-between" :align-items "center"
                 :margin-bottom "2rem"}}
   [:h1 "Material Templates"]
   [:button {:type "button"
             :on-click (fn [] 
                        (reset! modal-template {:material-template/active true})
                        (reset! modal-is-new? true))
             :style {:padding "0.5rem 1rem" :background "#28a745" :color "white"
                     :border "none" :border-radius "4px" :cursor "pointer"}}
    "Add Template"]])

(defn- templates-content [templates loading? modal-template modal-is-new? delete-template]
  "Main content area with loading or table"
  (if @loading?
    [:div "Loading..."]
    [material-template-table @templates
     (fn [template]
       (reset! modal-template template)
       (reset! modal-is-new? false))
     delete-template]))

(defn- modal-when-open [modal-template modal-is-new? save-template]
  "Render modal when template is selected"
  (when @modal-template
    [material-template-modal @modal-template @modal-is-new? save-template
     (fn [] (reset! modal-template nil))]))

(defn- render-templates-view [templates loading? modal-template modal-is-new? 
                             load-templates delete-template save-template]
  "Main render function for templates view"
  (zero-react/use-effect
    {:mount (fn [] (when (empty? @templates) (load-templates)))
     :params #js[]})
  [:div {:style {:padding "2rem"}}
   [page-header modal-template modal-is-new?]
   [templates-content templates loading? modal-template modal-is-new? delete-template]
   [modal-when-open modal-template modal-is-new? save-template]])

(defn view []
  (let [workspace-id (get-workspace-id)
        templates (r/atom [])
        loading? (r/atom false)
        modal-template (r/atom nil)
        modal-is-new? (r/atom false)
        
        load-templates (fn []
                        (reset! loading? true)
                        (load-templates-query workspace-id loading? templates))
        
        save-template (fn [template callback]
                       (save-template-query template workspace-id modal-is-new? 
                                          callback modal-template load-templates))
        
        delete-template (fn [template-id]
                         (delete-template-query template-id workspace-id load-templates))]
    
    (fn []
      (render-templates-view templates loading? modal-template modal-is-new? 
                           load-templates delete-template save-template))))