(ns features.app.material-templates.frontend.view
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

(defn- is-admin? [user]
  "Check if user has admin or superadmin role"
  (let [role (:user/role user)]
    (or (= role "admin") (= role "superadmin"))))

(defn- load-current-user [user-atom]
  "Load current user data"
  (parquery/send-queries
   {:queries {:user/current {}}
    :parquery/context {}
    :callback (fn [response]
                (reset! user-atom (:user/current response)))}))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    workspace-id))

(defn- load-templates-query
  "Execute ParQuery to load material templates with pagination"
  [workspace-id params templates-atom pagination-atom loading-atom]
  (reset! loading-atom true)
  (parquery/send-queries
   {:queries {:workspace-material-templates/get-paginated params}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (reset! loading-atom false)
               (let [result (:workspace-material-templates/get-paginated response)
                     items (:material-templates result [])
                     pag (:pagination result)]
                 (reset! templates-atom items)
                 (when pag
                   (reset! pagination-atom pag))))}))

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
    (parquery/send-queries
     {:queries {query-type template-data}
      :parquery/context context
      :callback (fn [response]
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
      (validate-name name) (assoc :material-template/name (tr/tr :material-templates/error-name))
      (validate-unit unit) (assoc :material-template/unit (tr/tr :material-templates/error-unit)))))

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
  (let [has-error? (contains? errors field-key)
        testid (str "material-template-" (name field-key) "-input")]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key template has-error? (assoc attrs :data-testid testid)]
     [field-error (get errors field-key)]]))

(defn- form-fields
  "All form input fields"
  [template errors]
  [:div
   [form-field (tr/tr :material-templates/name) :material-template/name template errors
    {:type "text" :placeholder (tr/tr :material-templates/name-placeholder)}]
   [form-field (tr/tr :material-templates/unit) :material-template/unit template errors
    {:type "text" :placeholder (tr/tr :material-templates/unit-placeholder)}]
   [form-field (tr/tr :material-templates/category) :material-template/category template errors
    {:type "text" :placeholder (tr/tr :material-templates/category-placeholder)}]
   [form-field (tr/tr :material-templates/description) :material-template/description template errors
    {:type "textarea" :placeholder (tr/tr :material-templates/description-placeholder) :rows 3}]])


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
        {:title (if is-new? (tr/tr :material-templates/modal-add-title) (tr/tr :material-templates/modal-edit-title))
         :subtitle (if is-new?
                     (tr/tr :material-templates/modal-add-subtitle)
                     (tr/tr :material-templates/modal-edit-subtitle))}]
       ^{:key "form"} [form-fields template @errors]
       ^{:key "footer"} [modal/modal-footer
        ^{:key "cancel"} [enhanced-button/enhanced-button
         {:variant :secondary
          :data-testid "material-template-cancel-button"
          :on-click on-cancel
          :text (tr/tr :material-templates/cancel)}]
        ^{:key "save"} [enhanced-button/enhanced-button
         {:variant :primary
          :data-testid "material-template-submit-button"
          :loading? @loading?
          :on-click #(handle-save-click template loading? errors on-save)
          :text (if @loading? (tr/tr :material-templates/saving) (tr/tr :material-templates/save-template))}]]])))

;; =============================================================================
;; Column Renderers for react-data-table-component
;; =============================================================================

(defn- template-name-cell
  "Custom cell for template name column with description"
  [row]
  [:div
   [:div {:style {:font-weight "600" :color "#111827" :font-size "0.875rem"}}
    (:material-template/name row)]
   (when (:material-template/description row)
     [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem" :line-height "1.4"}}
      (:material-template/description row)])])

(defn- category-cell
  "Custom cell for category with fallback text"
  [row]
  (let [category (:material-template/category row)]
    (if category
      [:span {:style {:color "#374151" :font-size "0.875rem"}} category]
      [:span {:style {:color "#9ca3af" :font-style "italic"}} (tr/tr :material-templates/no-category)])))

(defn- get-columns
  "Get column configuration for material templates table (react-data-table format)"
  []
  [{:name      (tr/tr :material-templates/table-header-material)
    :selector  :material-template/name
    :sortField :material-template/name
    :sortable  true
    :cell      template-name-cell
    :width     "300px"}
   {:name      (tr/tr :material-templates/table-header-unit)
    :selector  :material-template/unit
    :sortField :material-template/unit
    :sortable  true
    :width     "100px"}
   {:name      (tr/tr :material-templates/table-header-category)
    :selector  :material-template/category
    :sortField :material-template/category
    :sortable  true
    :cell      category-cell
    :width     "150px"}])

(defn- templates-subheader
  "Subheader with title and add button"
  [modal-template modal-is-new?]
  [subheader/subheader
   {:title (tr/tr :material-templates/page-title)
    :description (tr/tr :material-templates/page-description)
    :action-button [enhanced-button/enhanced-button
                    {:variant :success
                     :data-testid "add-material-template-button"
                     :on-click (fn []
                                (reset! modal-template {})
                                (reset! modal-is-new? true))
                     :text (tr/tr :material-templates/add-new-template)}]}])

(defn- modal-when-open
  "Render modal when template is selected"
  [modal-template modal-is-new? save-template]
  (when @modal-template
    [material-template-modal @modal-template @modal-is-new? save-template
     (fn [] (reset! modal-template nil))]))

(defn view []
  (let [workspace-id (get-workspace-id)
        ;; Local state
        templates (r/atom [])
        pagination (r/atom {:total-count 0 :page 0 :page-size 10})
        loading? (r/atom false)
        current-user (r/atom nil)
        search-term (r/atom "")
        sort-field (r/atom :material-template/name)
        sort-direction (r/atom "asc")
        modal-template (r/atom nil)
        modal-is-new? (r/atom false)

        ;; Load function
        load-templates (fn []
                         (load-templates-query
                          workspace-id
                          {:search @search-term
                           :sort-by @sort-field
                           :sort-direction @sort-direction
                           :page (:page @pagination)
                           :page-size (:page-size @pagination)}
                          templates
                          pagination
                          loading?))

        save-template (fn [template callback]
                        (save-template-query template workspace-id modal-is-new?
                                             callback modal-template load-templates))

        delete-template (fn [template-id]
                          (delete-template-query template-id workspace-id load-templates))

        on-edit (fn [row]
                  (reset! modal-template row)
                  (reset! modal-is-new? false))

        on-delete (fn [row]
                    (when (js/confirm (tr/tr :material-templates/confirm-delete))
                      (delete-template (:material-template/id row))))]

    ;; Load current user on mount
    (load-current-user current-user)

    (fn []
      (let [admin? (is-admin? @current-user)]
        ;; Load initial data
        (when (and (empty? @templates) (not @loading?))
          (load-templates))

        [:<>
         ;; Only show add button for admins
         (when admin?
           [templates-subheader modal-template modal-is-new?])
         ;; Show simple header for non-admins
         (when-not admin?
           [subheader/subheader
            {:title (tr/tr :material-templates/page-title)
             :description (tr/tr :material-templates/page-description)}])
         [content-section/content-section
          ;; Search bar
          [:div {:style {:margin-bottom "1rem"}}
           [data-table-search/view
            {:search-term @search-term
             :placeholder (tr/tr :material-templates/search-placeholder)
             :data-testid "material-templates-search"
             :on-search-change (fn [value]
                                 (reset! search-term value))
             :on-search (fn [value]
                          (reset! search-term value)
                          (swap! pagination assoc :page 0)
                          (load-templates))}]]

          ;; Material templates table - only pass edit/delete handlers for admins
          [data-table/view
           (merge
            {:columns (get-columns)
             :data @templates
             :loading? @loading?
             :pagination @pagination
             :entity {:name "material-template" :name-plural "material templates"}
             :data-testid "material-templates-table"
             ;; Pagination handler
             :on-page-change (fn [page _total-rows]
                               (swap! pagination assoc :page (dec page))
                               (load-templates))
             :on-page-size-change (fn [new-size]
                                    (swap! pagination assoc :page-size new-size :page 0)
                                    (load-templates))
             ;; Sort handler
             :on-sort (fn [field direction _sorted-rows]
                        (reset! sort-field field)
                        (reset! sort-direction direction)
                        (load-templates))}
            ;; Only add edit/delete for admins
            (when admin?
              {:on-edit on-edit
               :on-delete on-delete}))]

          [modal-when-open modal-template modal-is-new? save-template]]]))))
