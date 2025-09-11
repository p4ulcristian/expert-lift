(ns features.app.workspace.material-templates.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]))

(defn- get-workspace-id []
  "Get workspace ID from router parameters"
  (get-in @router/state [:parameters :path :workspace-id]))

(defn validate-material-template
  "Validates material template data and returns map of field errors"
  [template]
  (let [errors {}
        name (str (:material-template/name template))
        unit (str (:material-template/unit template))]
    (cond-> errors
      (< (count (str/trim name)) 2)
      (assoc :material-template/name "Name is required")
      
      (< (count (str/trim unit)) 1)
      (assoc :material-template/unit "Unit is required"))))

(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "bold"
                   :color (if has-error? "#dc3545" "inherit")}}
   label (when (#{:material-template/name :material-template/unit} field-key) " *")])

(defn- field-input [field-key template has-error? attrs]
  (if (= (:type attrs) "textarea")
    [:textarea (merge (dissoc attrs :type)
                      {:value (str (get @template field-key ""))
                       :on-change #(swap! template assoc field-key (.. % -target -value))
                       :style (merge (:style attrs)
                                     (when has-error?
                                       {:border "2px solid #dc3545"}))})]
    [:input (merge attrs
                   {:value (str (get @template field-key ""))
                    :on-change #(swap! template assoc field-key (.. % -target -value))
                    :style (merge (:style attrs)
                                  (when has-error?
                                    {:border "2px solid #dc3545"}))})]))

(defn- field-error [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- form-field [label field-key template errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1rem"}}
     [field-label label field-key has-error?]
     [field-input field-key template has-error? attrs]
     [field-error (get errors field-key)]]))

(defn material-template-modal
  "Modal for creating/editing material templates"
  [template is-new? on-save on-cancel]
  (let [loading? (r/atom false)
        errors (r/atom {})]
    (fn [template is-new? on-save on-cancel]
      [:div {:style {:position "fixed" :top 0 :left 0 :right 0 :bottom 0
                     :background "rgba(0,0,0,0.5)" :z-index 1000
                     :display "flex" :align-items "center" :justify-content "center"}}
       [:div {:style {:background "white" :padding "2rem" :border-radius "8px"
                      :min-width "400px" :max-width "600px" :max-height "80vh" :overflow "auto"}}
        [:h3 (if is-new? "Add New Material Template" "Edit Material Template")]
        
        [form-field "Name" :material-template/name template @errors
         {:type "text" :placeholder "e.g. Steel Cable"}]
        
        [form-field "Unit" :material-template/unit template @errors
         {:type "text" :placeholder "e.g. m, kg, pcs"}]
        
        [form-field "Category" :material-template/category template @errors
         {:type "text" :placeholder "e.g. Cables, Hardware"}]
        
        [form-field "Description" :material-template/description template @errors
         {:type "textarea" :placeholder "Optional description" :rows 3}]
        
        (when-not is-new?
          [:div {:style {:margin-bottom "1rem"}}
           [:label {:style {:display "flex" :align-items "center" :font-weight "bold"}}
            [:input {:type "checkbox" 
                     :checked (boolean (:material-template/active @template))
                     :on-change #(swap! template assoc :material-template/active (.. % -target -checked))
                     :style {:margin-right "0.5rem"}}]
            "Active"]])
        
        [:div {:style {:display "flex" :gap "1rem" :margin-top "2rem"}}
         [:button {:type "button"
                   :disabled @loading?
                   :on-click (fn []
                               (let [validation-errors (validate-material-template @template)]
                                 (if (empty? validation-errors)
                                   (do
                                     (reset! loading? true)
                                     (reset! errors {})
                                     (on-save @template (fn [] (reset! loading? false))))
                                   (reset! errors validation-errors))))
                   :style {:padding "0.5rem 1rem" :background "#007bff" :color "white"
                           :border "none" :border-radius "4px" :cursor "pointer"}}
          (if @loading? "Saving..." "Save")]
         
         [:button {:type "button"
                   :on-click (fn [] (on-cancel))
                   :style {:padding "0.5rem 1rem" :background "#6c757d" :color "white"
                           :border "none" :border-radius "4px" :cursor "pointer"}}
          "Cancel"]]]])))

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
        [:td {:style {:padding "0.75rem" :border "1px solid #dee2e6"}}
         [:div [:strong (:material-template/name template)]
          (when (:material-template/description template)
            [:div {:style {:color "#6c757d" :font-size "0.875rem"}}
             (:material-template/description template)])]]
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

(defn view []
  (let [workspace-id (get-workspace-id)
        templates (r/atom [])
        loading? (r/atom false)
        modal-template (r/atom nil)
        modal-is-new? (r/atom false)
        
        load-templates (fn []
                        (reset! loading? true)
                        (parquery/send-queries
                         {:queries {:workspace-material-templates/get-all {}}
                          :parquery/context {:workspace-id workspace-id}
                          :callback (fn [response]
                                     (reset! loading? false)
                                     (let [result (:workspace-material-templates/get-all response)]
                                       (reset! templates (or result []))))}))
        
        save-template (fn [template callback]
                       (let [query-type (if @modal-is-new? 
                                         :workspace-material-templates/create 
                                         :workspace-material-templates/update)
                             template-data (if @modal-is-new?
                                            (dissoc template :material-template/id)
                                            template)]
                         (parquery/send-queries
                          {:queries {query-type template-data}
                           :parquery/context {:workspace-id workspace-id}
                           :callback (fn [response]
                                      (callback)
                                      (if (:success (get response query-type))
                                        (do
                                          (reset! modal-template nil)
                                          (load-templates))
                                        (js/alert (str "Error: " (:error (get response query-type))))))})))
        
        delete-template (fn [template-id]
                         (parquery/send-queries
                          {:queries {:workspace-material-templates/delete {:material-template/id template-id}}
                           :parquery/context {:workspace-id workspace-id}
                           :callback (fn [response]
                                      (if (:success (:workspace-material-templates/delete response))
                                        (load-templates)
                                        (js/alert "Error deleting template")))}))]
    
    (fn []
      ;; Load templates on mount
      (when (empty? @templates)
        (load-templates))
      
      [:div {:style {:padding "2rem"}}
       [:div {:style {:display "flex" :justify-content "space-between" :align-items "center"
                      :margin-bottom "2rem"}}
        [:h1 "Material Templates"]
        [:button {:type "button"
                  :on-click (fn [] 
                              (reset! modal-template (r/atom {:material-template/active true}))
                              (reset! modal-is-new? true))
                  :style {:padding "0.5rem 1rem" :background "#28a745" :color "white"
                          :border "none" :border-radius "4px" :cursor "pointer"}}
         "Add Template"]]
       
       (if @loading?
         [:div "Loading..."]
         [material-template-table @templates
          (fn [template]
            (reset! modal-template (r/atom template))
            (reset! modal-is-new? false))
          delete-template])
       
       (when @modal-template
         [material-template-modal @modal-template @modal-is-new? save-template
          (fn [] (reset! modal-template nil))])])))