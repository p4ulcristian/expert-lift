(ns features.app.workspace.material-templates.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame]
            [zero.frontend.react :as zero-react]))

(defn- get-workspace-id []
  "Get workspace ID from router parameters"
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    (println "DEBUG: get-workspace-id called")
    (println "  Router state:" router-state)
    (println "  Extracted workspace-id:" workspace-id)
    workspace-id))

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
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                   :font-size "0.875rem" :letter-spacing "0.025em"
                   :color (if has-error? "#dc3545" "#374151")}}
   label 
   (when (#{:material-template/name :material-template/unit} field-key) 
     [:span {:style {:color "#ef4444" :margin-left "0.25rem"}} "*"])])

(defn- input-base-props [field-key template has-error? attrs]
  "Base properties for input fields"
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
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key template has-error? attrs]
     [field-error (get errors field-key)]]))

(defn- modal-overlay []
  "Modal overlay background"
  {:position "fixed" :top 0 :left 0 :right 0 :bottom 0
   :background "rgba(0, 0, 0, 0.6)" :z-index 1000
   :display "flex" :align-items "center" :justify-content "center"
   :backdrop-filter "blur(4px)"
   :animation "fadeIn 0.2s ease-out"})

(defn- modal-content []
  "Modal content container styles"
  {:background "white" :padding "2rem" :border-radius "16px"
   :min-width "480px" :max-width "640px" :max-height "90vh" :overflow "auto"
   :box-shadow "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)"
   :transform "scale(1)" :animation "slideIn 0.2s ease-out"})

(defn- modal-title [is-new?]
  "Modal title based on create/edit mode"
  [:div {:style {:margin-bottom "2rem" :padding-bottom "1rem" :border-bottom "1px solid #e5e7eb"}}
   [:h3 {:style {:font-size "1.5rem" :font-weight "600" :color "#111827" :margin "0"}}
    (if is-new? "Add New Material Template" "Edit Material Template")]
   [:p {:style {:color "#6b7280" :font-size "0.875rem" :margin "0.5rem 0 0 0"}}
    (if is-new? 
      "Create a new material template for your workspace"
      "Update the details of this material template")]])

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
  [:div {:style {:display "flex" :gap "0.75rem" :margin-top "2.5rem" :padding-top "2rem"
                 :border-top "1px solid #e5e7eb" :justify-content "flex-end"}}
   [:button {:type "button" :on-click on-cancel
             :style {:padding "0.75rem 1.5rem" :background "white" :color "#374151"
                     :border "1px solid #d1d5db" :border-radius "8px" :cursor "pointer"
                     :font-weight "500" :transition "all 0.2s ease-in-out"
                     :hover {:background "#f9fafb" :border-color "#9ca3af"}}}
    "Cancel"]
   [:button {:type "button" :disabled @loading?
             :on-click #(handle-save-click template loading? errors on-save)
             :style {:padding "0.75rem 1.5rem" 
                     :background (if @loading? "#9ca3af" "#3b82f6") 
                     :color "white" :border "none" :border-radius "8px" 
                     :cursor (if @loading? "not-allowed" "pointer")
                     :font-weight "500" :transition "all 0.2s ease-in-out"
                     :opacity (if @loading? 0.7 1)
                     :hover (when-not @loading? {:background "#2563eb"})}}
    (if @loading? "Saving..." "Save Template")]])

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
                 :margin-bottom "2rem" :padding-bottom "1.5rem" :border-bottom "1px solid #e5e7eb"}}
   [:div
    [:h1 {:style {:font-size "1.875rem" :font-weight "700" :color "#111827" :margin "0"}}
     "Material Templates"]
    [:p {:style {:color "#6b7280" :font-size "0.875rem" :margin "0.5rem 0 0 0"}}
     "Manage your material templates for this workspace"]]
   [:button {:type "button"
             :on-click (fn [] 
                        (reset! modal-template {:material-template/active true})
                        (reset! modal-is-new? true))
             :style {:padding "0.75rem 1.25rem" :background "#10b981" :color "white"
                     :border "none" :border-radius "8px" :cursor "pointer"
                     :font-weight "600" :font-size "0.875rem" :letter-spacing "0.025em"
                     :transition "all 0.2s ease-in-out" :box-shadow "0 1px 2px 0 rgba(0, 0, 0, 0.05)"
                     :hover {:background "#059669" :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"}}}
    "+ Add New Template"]])

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

(defn view []
  (let [authenticated? (r/atom nil)  ; nil = checking, true = authenticated, false = not authenticated
        workspace-id (get-workspace-id)
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
                                     ;; Load templates after authentication is confirmed
                                     (when (empty? @templates) (load-templates)))
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
        [:div {:style {:padding "2rem"}}
         [page-header modal-template modal-is-new?]
         [templates-content templates loading? modal-template modal-is-new? delete-template]
         [modal-when-open modal-template modal-is-new? save-template]]))))