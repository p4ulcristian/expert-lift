(ns features.app.worksheets.frontend.components
  "UI components for worksheets feature - forms, signatures, materials"
  (:require
   [clojure.string :as str]
   [reagent.core :as r]
   [zero.frontend.re-frame :as rf]
   [features.app.worksheets.frontend.utils :as utils]
   [features.app.worksheets.frontend.queries :as queries]
   [ui.modal :as modal]
   [ui.enhanced-button :as enhanced-button]
   [ui.address-search :as address-search]
   [translations.core :as tr]
   ["react-signature-canvas" :default SignatureCanvas]))

;; =============================================================================
;; Form Field Components
;; =============================================================================

(defn field-label
  "Render form field label with optional required indicator"
  [label field-key has-error?]
  [:label {:style (merge utils/label-style
                         {:color (if has-error? "#dc3545" "#374151")})}
   label
   (when (utils/required-fields field-key)
     [:span {:style {:color "#ef4444" :margin-left "0.25rem"}} "*"])])

(defn field-error
  "Render field error message"
  [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- input-base-props
  "Base properties for input fields"
  [field-key has-error? attrs]
  (let [is-time-field? (#{:worksheet/arrival-time :worksheet/departure-time} field-key)
        form-data-sub @(rf/subscribe [:worksheets/modal-form-data])
        form-data (or form-data-sub {})
        field-value (get form-data field-key "")
        display-value (if (and is-time-field? (= (:type attrs) "datetime-local"))
                        (utils/format-datetime-for-input field-value)
                        (str field-value))
        base-change-handler (fn [e]
                              (let [value (.. e -target -value)]
                                (rf/dispatch [:worksheets/update-modal-form-field field-key value])
                                (when is-time-field?
                                  ;; Auto-calculate duration for time fields
                                  (let [updated-data (assoc form-data field-key value)
                                        arrival (:worksheet/arrival-time updated-data)
                                        departure (:worksheet/departure-time updated-data)
                                        calculated-duration (utils/calculate-work-duration arrival departure)]
                                    (when calculated-duration
                                      (rf/dispatch [:worksheets/update-modal-form-field :worksheet/work-duration-hours calculated-duration]))))))]
    {:value display-value
     :on-change base-change-handler
     :style (merge utils/input-base-style
                   {:border (if has-error? utils/input-error-border utils/input-normal-border)
                    :box-shadow (if has-error?
                                  "0 0 0 3px rgba(220, 53, 69, 0.1)"
                                  "0 1px 2px 0 rgba(0, 0, 0, 0.05)")
                    :background (when (:disabled attrs) "#f9fafb")
                    :cursor (when (:disabled attrs) "not-allowed")}
                   (:style attrs))}))

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

(defn- render-address-select
  "Render address dropdown that loads addresses from workspace"
  [field-key has-error? attrs]
  (let [workspace-id (:workspace-id attrs)
        addresses (r/atom [])
        loading? (r/atom false)]
    ;; Load addresses when component mounts
    (queries/load-addresses workspace-id addresses loading?)

    (fn [field-key has-error? attrs]
      [:select (merge (input-base-props field-key has-error? attrs)
                      (dissoc attrs :workspace-id :current-value))
       [:option {:value ""}
        (if @loading? "Loading addresses..." "Select address...")]
       (for [address @addresses]
         ^{:key (:address/id address)}
         [:option {:value (:address/id address)}
          (:address/name address)])])))

(defn- field-input
  "Render appropriate input type based on attrs"
  [field-key has-error? attrs]
  (cond
    (= (:type attrs) "textarea") (render-textarea field-key has-error? attrs)
    (= (:type attrs) "select") (render-select field-key has-error? attrs (:options attrs))
    (= (:type attrs) "address-select") [render-address-select field-key has-error? attrs]
    :else (render-text-input field-key has-error? attrs)))

(defn form-field
  "Complete form field with label, input and error"
  [label field-key errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key has-error? attrs]
     [field-error (get errors field-key)]]))

;; =============================================================================
;; Form Sections
;; =============================================================================

(defn basic-form-fields
  "Render basic worksheet form fields"
  [errors]
  [:div
   [form-field "Serial Number" :worksheet/serial-number errors
    {:type "text" :placeholder "Auto-generated" :disabled true}]
   [form-field "Creation Date" :worksheet/creation-date errors
    {:type "date"}]])

(defn address-form-field
  "Render address autocomplete field"
  [errors workspace-id]
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        has-error? (contains? errors :worksheet/address-id)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label "Address" :worksheet/address-id has-error?]
     [address-search/address-search-dropdown
      {:component-id :worksheet-address
       :workspace-id workspace-id
       :value {:address/id (:worksheet/address-id form-data)
               :address/name (:worksheet/address-name form-data)}
       :on-select (fn [address]
                    (rf/dispatch [:worksheets/update-modal-form-field :worksheet/address-id (:address/id address)])
                    (rf/dispatch [:worksheets/update-modal-form-field :worksheet/address-name (:address/name address)]))}]
     [field-error (get errors :worksheet/address-id)]]))

(defn work-info-form-fields
  "Render work information form fields"
  [errors]
  [:div
   [form-field "Work Type" :worksheet/work-type errors
    {:type "select" :options utils/work-type-options}]
   [form-field "Service Type" :worksheet/service-type errors
    {:type "select" :options utils/service-type-options}]
   [form-field "Work Description" :worksheet/work-description errors
    {:type "textarea" :placeholder "Describe the work to be performed..." :rows 4}]
   [form-field "Status" :worksheet/status errors
    {:type "select" :options utils/status-options}]])

(defn time-tracking-form-fields
  "Render time tracking form fields"
  [errors]
  [:div
   [form-field "Arrival Time" :worksheet/arrival-time errors
    {:type "datetime-local"}]
   [form-field "Departure Time" :worksheet/departure-time errors
    {:type "datetime-local"}]
   [form-field "Work Duration (Hours)" :worksheet/work-duration-hours errors
    {:type "number" :step "1" :placeholder "Auto-calculated from arrival/departure" :disabled true}]
   [:div {:style {:margin-bottom "1.5rem" :font-size "0.75rem" :color "#6b7280"}}
    "Work duration is automatically calculated from arrival and departure times (rounded up to nearest full hour)"]])

(defn notes-form-field
  "Render notes form field"
  [errors]
  [form-field "Notes" :worksheet/notes errors
   {:type "textarea" :placeholder "Optional notes..." :rows 3}])

;; =============================================================================
;; Material Components
;; =============================================================================

(defn- material-item-display
  "Render single material item with remove button"
  [idx material]
  [:div {:key idx
         :style {:display "flex" :justify-content "space-between" :align-items "center"
                 :padding "0.25rem 0" :border-bottom "1px solid #f3f4f6"}}
   [:span {:style {:font-size "0.875rem"}}
    (str (:name material) " - " (:quantity material) " " (:unit material))]
   [:button {:type "button"
             :on-click #(rf/dispatch [:worksheets/remove-material idx])
             :style {:background "#ef4444" :color "white" :border "none"
                     :border-radius "4px" :padding "0.25rem 0.5rem"
                     :font-size "0.75rem" :cursor "pointer"}}
    "Remove"]])

(defn- existing-materials-list
  "Render list of existing materials"
  [materials]
  (when (seq materials)
    [:div {:style {:margin-bottom "1rem"}}
     [:h4 {:style {:margin-bottom "0.5rem" :font-size "0.9rem" :font-weight "500" :color "#374151"}}
      "Added Materials:"]
     [:div {:style {:max-height "150px" :overflow-y "auto"
                    :border "1px solid #d1d5db" :border-radius "6px" :padding "0.5rem"}}
      (map-indexed material-item-display materials)]]))

(defn- template-material-selector
  "Render material template selection form"
  [material-templates selected-template-id]
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        quantity-value (get form-data :worksheet/new-material-quantity "")
        disabled? (or (empty? selected-template-id) (empty? (str quantity-value)))]
    [:div {:style {:display "grid" :grid-template-columns "2fr 1fr auto" :gap "0.5rem" :align-items "end"}}
     [:div
      [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                       :font-size "0.75rem" :color "#374151"}}
       "Select Material"]
      [:select {:value selected-template-id
                :on-change #(rf/dispatch [:worksheets/select-material-template (.. % -target -value)])
                :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                        :border-radius "6px" :font-size "0.875rem"}}
       [:option {:value ""} "Choose material..."]
       (map (fn [template]
              [:option {:key (:material-template/id template) :value (:material-template/id template)}
               (str (:material-template/name template) " (" (:material-template/unit template) ")")])
            (sort-by :material-template/name material-templates))]]
     [:div
      [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                       :font-size "0.75rem" :color "#374151"}}
       "Quantity"]
      [:input {:type "number"
               :value quantity-value
               :on-change #(rf/dispatch [:worksheets/update-form-field :worksheet/new-material-quantity (.. % -target -value)])
               :placeholder "5"
               :disabled (empty? selected-template-id)
               :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                       :border-radius "6px" :font-size "0.875rem"
                       :opacity (if (empty? selected-template-id) 0.5 1)}}]]
     [:button {:type "button"
               :on-click #(rf/dispatch [:worksheets/add-selected-material])
               :disabled disabled?
               :style {:background (if disabled? "#9ca3af" "#10b981")
                       :color "white" :border "none" :border-radius "6px"
                       :padding "0.5rem 1rem" :font-size "0.875rem"
                       :cursor (if disabled? "not-allowed" "pointer")
                       :font-weight "500"}}
      "Add"]]))

(defn- custom-material-inputs
  "Render custom material input form"
  []
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        custom-name (get form-data :worksheet/custom-material-name "")
        custom-unit (get form-data :worksheet/custom-material-unit "")
        custom-quantity (get form-data :worksheet/custom-material-quantity "")
        disabled? (or (empty? (str custom-name))
                      (empty? (str custom-unit))
                      (empty? (str custom-quantity)))]
    [:div {:style {:margin-top "1rem" :padding-top "1rem" :border-top "1px solid #e5e7eb"}}
     [:h4 {:style {:margin-bottom "0.5rem" :font-size "0.9rem" :font-weight "500" :color "#374151"}}
      "Add Custom Material:"]
     [:div {:style {:display "grid" :grid-template-columns "2fr 1fr 1fr auto" :gap "0.5rem" :align-items "end"}}
      [:div
       [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                        :font-size "0.75rem" :color "#374151"}}
        "Custom Material Name"]
       [:input {:type "text"
                :value custom-name
                :on-change #(rf/dispatch [:worksheets/update-form-field :worksheet/custom-material-name (.. % -target -value)])
                :placeholder "Enter material name..."
                :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                        :border-radius "6px" :font-size "0.875rem"}}]]
      [:div
       [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                        :font-size "0.75rem" :color "#374151"}}
        "Unit"]
       [:input {:type "text"
                :value custom-unit
                :on-change #(rf/dispatch [:worksheets/update-form-field :worksheet/custom-material-unit (.. % -target -value)])
                :placeholder "pcs, kg, m..."
                :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                        :border-radius "6px" :font-size "0.875rem"}}]]
      [:div
       [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                        :font-size "0.75rem" :color "#374151"}}
        "Quantity"]
       [:input {:type "number"
                :value custom-quantity
                :on-change #(rf/dispatch [:worksheets/update-form-field :worksheet/custom-material-quantity (.. % -target -value)])
                :placeholder "5"
                :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                        :border-radius "6px" :font-size "0.875rem"}}]]
      [:button {:type "button"
                :on-click #(rf/dispatch [:worksheets/add-custom-material])
                :disabled disabled?
                :style {:background (if disabled? "#9ca3af" "#2563eb")
                        :color "white" :border "none" :border-radius "6px"
                        :padding "0.5rem 1rem" :font-size "0.875rem"
                        :cursor (if disabled? "not-allowed" "pointer")
                        :font-weight "500"}}
       "Add"]]]))

(defn materials-section
  "Render complete materials section"
  []
  (let [materials (get @(rf/subscribe [:worksheets/modal-form-data]) :worksheet/material-usage [])
        material-templates @(rf/subscribe [:material-templates/all])
        selected-template-id (get @(rf/subscribe [:worksheets/modal-form-data]) :worksheet/selected-material-template "")]
    [:div {:style {:margin-bottom "1.5rem"}}
     [:h3 {:style {:margin-bottom "1rem" :font-size "1.125rem" :font-weight "600" :color "#374151"}}
      "Materials Used"]
     [:div
      [existing-materials-list materials]
      [template-material-selector material-templates selected-template-id]
      [custom-material-inputs]]]))

;; =============================================================================
;; Signature Components
;; =============================================================================

(defn- render-signature-content
  "Render signature - handles both old PNG (base64) and new SVG formats"
  [signature-data]
  (cond
    ;; No signature
    (or (nil? signature-data) (empty? signature-data))
    [:div {:style {:color "#9ca3af" :text-align "center" :font-size "0.875rem"}}
     "Click to sign"]

    ;; Old format: base64 PNG/JPEG data URL
    (str/starts-with? signature-data "data:image")
    [:img {:src signature-data
           :style {:max-width "100%"
                   :max-height "100%"
                   :object-fit "contain"}}]

    ;; New format: SVG string
    (str/starts-with? signature-data "<svg")
    [:div {:style {:max-width "100%"
                   :max-height "100%"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"}
           :dangerouslySetInnerHTML {:__html signature-data}}]

    ;; Fallback - try as image
    :else
    [:img {:src signature-data
           :style {:max-width "100%"
                   :max-height "100%"
                   :object-fit "contain"}}]))

(defn- signature-display
  "Render signature as clickable display (non-active)"
  [ref-dispatch-key label]
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        signature-data (if (= ref-dispatch-key :worksheets/set-maintainer-signature-ref)
                         (:worksheet/maintainer-signature form-data)
                         (:worksheet/customer-signature form-data))]
    [:div
     [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                      :font-size "0.875rem" :color "#374151"}}
      label]
     [:div {:style {:border "1px solid #d1d5db"
                    :border-radius "8px"
                    :background "#ffffff"
                    :width "100%"
                    :max-width "300px"
                    :height "150px"
                    :cursor "pointer"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :position "relative"
                    :overflow "hidden"}
            :on-click #(rf/dispatch [:worksheets/open-signature-zoom label])}
      [render-signature-content signature-data]
      [:div {:style {:position "absolute"
                     :top "5px"
                     :right "5px"
                     :background "rgba(0,0,0,0.5)"
                     :color "white"
                     :border-radius "3px"
                     :padding "2px 6px"
                     :font-size "0.75rem"}}
       "zoom"]]]))

(defn- signature-zoom-overlay
  "Full-screen overlay for signature editing with mobile-optimized canvas"
  []
  (let [zoom-data @(rf/subscribe [:worksheets/signature-zoom-data])
        ;; Use same size for canvas and CSS to avoid coordinate offset
        base-width (if (< js/window.innerWidth 768)
                     (- js/window.innerWidth 80)
                     500)
        base-height (if (< js/window.innerWidth 768) 250 300)]
    (when zoom-data
      [:div {:style {:position "fixed"
                     :top 0
                     :left 0
                     :width "100vw"
                     :height "100vh"
                     :background "rgba(0, 0, 0, 0.8)"
                     :display "flex"
                     :align-items "center"
                     :justify-content "center"
                     :z-index 9999}
             :on-click #(rf/dispatch [:worksheets/close-signature-zoom])}
       [:div {:style {:background "white"
                      :border-radius "12px"
                      :padding "2rem"
                      :width "90vw"
                      :max-width "600px"
                      :height "auto"
                      :max-height "80vh"
                      :position "relative"
                      :overflow "auto"}
              :on-click (fn [e] (.stopPropagation e))}
        ;; Close button
        [:button {:style {:position "absolute"
                          :top "10px"
                          :right "10px"
                          :background "transparent"
                          :border "none"
                          :font-size "1.5rem"
                          :cursor "pointer"
                          :color "#6b7280"
                          :width "30px"
                          :height "30px"
                          :display "flex"
                          :align-items "center"
                          :justify-content "center"}
                  :on-click #(rf/dispatch [:worksheets/close-signature-zoom])}
         "x"]

        ;; Content
        [:h2 {:style {:margin "0 0 1.5rem 0" :font-size "1.5rem" :font-weight "600" :color "#374151"}}
         (:label zoom-data)]

        ;; Signature canvas - mobile optimized with touch-action fix
        [:div {:style {:margin-bottom "1.5rem"}}
         [:> SignatureCanvas
          {:penColor "black"
           :canvasProps {:width base-width
                         :height base-height
                         :style {:border "2px solid #d1d5db"
                                 :border-radius "8px"
                                 :background "#ffffff"
                                 :display "block"
                                 ;; Prevent page scroll while signing on mobile
                                 :touch-action "none"}}
           :ref (fn [ref]
                  (rf/dispatch [:worksheets/set-zoom-signature-ref ref])
                  ;; Load existing signature data into zoom canvas
                  ;; Note: fromDataURL only works with base64 images, not SVG
                  (when ref
                    (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
                          signature-data (if (= (:ref-dispatch-key zoom-data) :worksheets/set-maintainer-signature-ref)
                                           (:worksheet/maintainer-signature form-data)
                                           (:worksheet/customer-signature form-data))]
                      ;; Only load if it's a base64 image (old format)
                      ;; SVG signatures start fresh (can't load into canvas)
                      (when (and signature-data
                                 (str/starts-with? signature-data "data:image"))
                        (.fromDataURL ^js ref signature-data)))))}]]

        ;; Action buttons
        [:div {:style {:display "flex" :justify-content "space-between" :gap "1rem"}}
         [:button {:type "button"
                   :on-click (fn []
                               (when-let [^js ref @(rf/subscribe [:worksheets/zoom-signature-ref])]
                                 (.clear ref)))
                   :style {:padding "0.75rem 1.5rem" :font-size "0.875rem" :color "#6b7280"
                           :background "transparent" :border "1px solid #d1d5db" :border-radius "6px"
                           :cursor "pointer" :font-weight "500"}}
          "Clear"]
         [:button {:type "button"
                   :on-click #(rf/dispatch [:worksheets/close-signature-zoom])
                   :style {:padding "0.75rem 1.5rem" :font-size "0.875rem" :color "white"
                           :background "#3b82f6" :border "none" :border-radius "6px"
                           :cursor "pointer" :font-weight "500"}}
          "Done"]]]])))

(defn signatures-section
  "Render complete signatures section"
  []
  [:div {:style {:margin-bottom "1.5rem"}}
   [:h3 {:style {:margin-bottom "1rem" :font-size "1.125rem" :font-weight "600" :color "#374151"}}
    "Signatures"]
   [:div {:style {:display "grid" :grid-template-columns "1fr 1fr" :gap "1rem"}}
    [signature-display :worksheets/set-maintainer-signature-ref "Maintainer Signature"]
    [signature-display :worksheets/set-customer-signature-ref "Customer Signature"]]])

;; =============================================================================
;; Modal Components
;; =============================================================================

(defn- modal-header-config
  "Generate modal header configuration"
  [is-new?]
  {:title (if is-new? (tr/tr :worksheets/modal-add-title) (tr/tr :worksheets/modal-edit-title))
   :subtitle (if is-new? (tr/tr :worksheets/modal-add-subtitle) (tr/tr :worksheets/modal-edit-subtitle))})

(defn- save-button-click-handler
  "Handle save button click with validation and signature capture"
  [on-save]
  (fn []
    (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
          validation-errors (utils/validate-worksheet form-data)]
      (if (empty? validation-errors)
        (do
          (rf/dispatch [:worksheets/set-modal-form-loading true])
          (rf/dispatch [:worksheets/set-modal-form-errors {}])
          (on-save form-data (fn [] (rf/dispatch [:worksheets/set-modal-form-loading false]))))
        (rf/dispatch [:worksheets/set-modal-form-errors validation-errors])))))

(defn- modal-close-handler
  "Handle modal close event"
  [on-cancel]
  (fn []
    (rf/dispatch [:worksheets/clear-modal-form])
    (when on-cancel (on-cancel))))

(defn modal-footer-buttons
  "Render modal footer buttons"
  [on-cancel on-save]
  [modal/modal-footer
   [enhanced-button/enhanced-button
    {:variant :secondary
     :on-click (fn []
                 (rf/dispatch [:worksheets/clear-modal-form])
                 (when on-cancel (on-cancel)))
     :text (tr/tr :worksheets/cancel)}]
   [enhanced-button/enhanced-button
    {:variant :primary
     :loading? @(rf/subscribe [:worksheets/modal-form-loading?])
     :on-click (save-button-click-handler on-save)
     :text (if @(rf/subscribe [:worksheets/modal-form-loading?])
             (tr/tr :worksheets/saving)
             (tr/tr :worksheets/save-worksheet))}]])

(defn worksheet-modal
  "Modal for creating/editing worksheets"
  [worksheet-data is-new? on-save on-cancel workspace-id]
  ;; Initialize form state in re-frame
  (rf/dispatch [:worksheets/set-modal-form-data worksheet-data])
  (rf/dispatch [:worksheets/set-modal-form-errors {}])
  (rf/dispatch [:worksheets/set-modal-form-loading false])
  (rf/dispatch [:material-templates/load workspace-id])

  (fn [_worksheet-data is-new? on-save on-cancel workspace-id]
    [:div
     [modal/modal {:on-close (modal-close-handler on-cancel)
                   :close-on-backdrop? true}
      [modal/modal-header (modal-header-config is-new?)]
      (let [errors @(rf/subscribe [:worksheets/modal-form-errors])]
        [:div {:style {:padding "20px"}}
         [basic-form-fields errors]
         [address-form-field errors workspace-id]
         [work-info-form-fields errors]
         [time-tracking-form-fields errors]
         [notes-form-field errors]
         [materials-section]
         [signatures-section]])
      [modal-footer-buttons on-cancel on-save]]
     [signature-zoom-overlay]]))
