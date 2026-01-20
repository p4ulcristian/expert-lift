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
                              (let [value (.. e -target -value)
                                    ;; Get fresh form data inside handler to avoid stale closure
                                    current-form-data (or @(rf/subscribe [:worksheets/modal-form-data]) {})]
                                (rf/dispatch [:worksheets/update-modal-form-field field-key value])
                                (when is-time-field?
                                  ;; Auto-calculate duration for time fields
                                  (let [updated-data (assoc current-form-data field-key value)
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
   [:option {:value ""} (tr/tr :worksheets/select)]
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
        (if @loading? (tr/tr :worksheets/loading-addresses) (tr/tr :worksheets/select-address))]
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
  (let [has-error? (contains? errors field-key)
        testid (str "worksheet-" (name field-key) "-input")]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key has-error? (assoc attrs :data-testid testid)]
     [field-error (get errors field-key)]]))

;; =============================================================================
;; Form Sections
;; =============================================================================

(defn basic-form-fields
  "Render basic worksheet form fields"
  [errors]
  [:div
   [form-field (tr/tr :worksheets/serial-number) :worksheet/serial-number errors
    {:type "text" :placeholder (tr/tr :worksheets/auto-generated) :disabled true}]
   [form-field (tr/tr :worksheets/creation-date) :worksheet/creation-date errors
    {:type "date"}]])

(defn address-form-field
  "Render address autocomplete field"
  [errors workspace-id]
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        has-error? (contains? errors :worksheet/address-id)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label (tr/tr :worksheets/address) :worksheet/address-id has-error?]
     [address-search/address-search-dropdown
      {:component-id :worksheet-address
       :workspace-id workspace-id
       :value {:address/id (:worksheet/address-id form-data)
               :address/name (:worksheet/address-name form-data)}
       :on-select (fn [address]
                    (rf/dispatch [:worksheets/update-modal-form-field :worksheet/address-id (:address/id address)])
                    (rf/dispatch [:worksheets/update-modal-form-field :worksheet/address-name (:address/name address)])
                    ;; Store elevators as name strings and clear previous selection
                    (let [elevators (:elevators address)
                          names (mapv (fn [e] (if (map? e) (or (:name e) (str e)) (str e)))
                                      (if (sequential? elevators) elevators []))]
                      (rf/dispatch [:worksheets/update-modal-form-field :worksheet/address-elevators names]))
                    (rf/dispatch [:worksheets/update-modal-form-field :worksheet/elevator-identifier nil]))}]
     [field-error (get errors :worksheet/address-id)]]))

(defn elevator-form-field
  "Render elevator dropdown based on selected address"
  [errors]
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        elevators (:worksheet/address-elevators form-data)
        selected-elevator (:worksheet/elevator-identifier form-data)
        has-error? (contains? errors :worksheet/elevator-identifier)
        has-elevators? (and elevators (seq elevators))]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label (tr/tr :worksheets/elevator) :worksheet/elevator-identifier has-error?]
     [:select {:value (or selected-elevator "")
               :disabled (not has-elevators?)
               :on-change #(rf/dispatch [:worksheets/update-modal-form-field
                                         :worksheet/elevator-identifier
                                         (let [v (.. % -target -value)]
                                           (when (seq v) v))])
               :style (merge utils/input-base-style
                             {:border (if has-error? utils/input-error-border utils/input-normal-border)
                              :background (if has-elevators? "#fff" "#f9fafb")
                              :cursor (if has-elevators? "pointer" "not-allowed")})}
      [:option {:value ""} (if has-elevators? (tr/tr :worksheets/select-elevator) (tr/tr :worksheets/select-address-first))]
      (for [elevator-name elevators]
        ^{:key elevator-name}
        [:option {:value elevator-name} elevator-name])]
     [field-error (get errors :worksheet/elevator-identifier)]]))

(defn work-info-form-fields
  "Render work information form fields"
  [errors]
  [:div
   [form-field (tr/tr :worksheets/work-type) :worksheet/work-type errors
    {:type "select" :options (utils/work-type-options)}]
   [form-field (tr/tr :worksheets/service-type) :worksheet/service-type errors
    {:type "select" :options (utils/service-type-options)}]
   [form-field (tr/tr :worksheets/work-description) :worksheet/work-description errors
    {:type "textarea" :placeholder (tr/tr :worksheets/work-description-placeholder) :rows 4}]
   [form-field (tr/tr :worksheets/status) :worksheet/status errors
    {:type "select" :options (utils/status-options)}]])

(defn time-tracking-form-fields
  "Render time tracking form fields"
  [errors]
  [:div
   [form-field (tr/tr :worksheets/arrival-time) :worksheet/arrival-time errors
    {:type "datetime-local"}]
   [form-field (tr/tr :worksheets/departure-time) :worksheet/departure-time errors
    {:type "datetime-local"}]
   [form-field (tr/tr :worksheets/work-duration) :worksheet/work-duration-hours errors
    {:type "number" :step "1" :placeholder (tr/tr :worksheets/work-duration-placeholder) :disabled true}]
   [:div {:style {:margin-bottom "1.5rem" :font-size "0.75rem" :color "#6b7280"}}
    (tr/tr :worksheets/work-duration-note)]])

(defn notes-form-field
  "Render notes form field"
  [errors]
  [form-field (tr/tr :worksheets/notes) :worksheet/notes errors
   {:type "textarea" :placeholder (tr/tr :worksheets/notes-placeholder) :rows 3}])

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
    (tr/tr :worksheets/remove)]])

(defn- existing-materials-list
  "Render list of existing materials"
  [materials]
  (when (seq materials)
    [:div {:style {:margin-bottom "1rem"}}
     [:h4 {:style {:margin-bottom "0.5rem" :font-size "0.9rem" :font-weight "500" :color "#374151"}}
      (tr/tr :worksheets/added-materials)]
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
       (tr/tr :worksheets/select-material)]
      [:select {:value selected-template-id
                :on-change #(rf/dispatch [:worksheets/select-material-template (.. % -target -value)])
                :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                        :border-radius "6px" :font-size "0.875rem"}}
       [:option {:value ""} (tr/tr :worksheets/choose-material)]
       (map (fn [template]
              [:option {:key (:material-template/id template) :value (:material-template/id template)}
               (str (:material-template/name template) " (" (:material-template/unit template) ")")])
            (sort-by :material-template/name material-templates))]]
     [:div
      [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                       :font-size "0.75rem" :color "#374151"}}
       (tr/tr :worksheets/quantity)]
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
      (tr/tr :worksheets/add)]]))

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
      (tr/tr :worksheets/add-custom-material)]
     [:div {:style {:display "grid" :grid-template-columns "2fr 1fr 1fr auto" :gap "0.5rem" :align-items "end"}}
      [:div
       [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                        :font-size "0.75rem" :color "#374151"}}
        (tr/tr :worksheets/custom-material-name)]
       [:input {:type "text"
                :value custom-name
                :on-change #(rf/dispatch [:worksheets/update-form-field :worksheet/custom-material-name (.. % -target -value)])
                :placeholder (tr/tr :worksheets/enter-material-name)
                :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                        :border-radius "6px" :font-size "0.875rem"}}]]
      [:div
       [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                        :font-size "0.75rem" :color "#374151"}}
        (tr/tr :worksheets/unit)]
       [:input {:type "text"
                :value custom-unit
                :on-change #(rf/dispatch [:worksheets/update-form-field :worksheet/custom-material-unit (.. % -target -value)])
                :placeholder "pcs, kg, m..."
                :style {:width "100%" :padding "0.5rem" :border "1px solid #d1d5db"
                        :border-radius "6px" :font-size "0.875rem"}}]]
      [:div
       [:label {:style {:display "block" :margin-bottom "0.25rem" :font-weight "500"
                        :font-size "0.75rem" :color "#374151"}}
        (tr/tr :worksheets/quantity)]
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
       (tr/tr :worksheets/add)]]]))

(defn materials-section
  "Render complete materials section"
  []
  (let [materials (get @(rf/subscribe [:worksheets/modal-form-data]) :worksheet/material-usage [])
        material-templates @(rf/subscribe [:material-templates/all])
        selected-template-id (get @(rf/subscribe [:worksheets/modal-form-data]) :worksheet/selected-material-template "")]
    [:div {:style {:margin-bottom "1.5rem"}}
     [:h3 {:style {:margin-bottom "1rem" :font-size "1.125rem" :font-weight "600" :color "#374151"}}
      (tr/tr :worksheets/materials-used)]
     [:div
      [existing-materials-list materials]
      [template-material-selector material-templates selected-template-id]
      [custom-material-inputs]]]))

;; =============================================================================
;; Signature Components
;; =============================================================================

(defn- signature-canvas-preview
  "Small read-only SignatureCanvas for displaying points data.
   Uses the same rendering as the zoom canvas - guarantees identical display."
  [signature-data]
  (let [canvas-ref (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [_]
        (when-let [ref @canvas-ref]
          (when (and signature-data (str/starts-with? signature-data "points:"))
            (let [json-str (subs signature-data 7)
                  points-array (try (js/JSON.parse json-str) (catch :default _ nil))]
              (when points-array
                (.fromData ref points-array))))))

      :component-did-update
      (fn [this old-argv]
        (let [[_ old-sig] old-argv
              [_ new-sig] (r/argv this)]
          (when (not= new-sig old-sig)
            (when-let [ref @canvas-ref]
              (.clear ref)
              (when (and new-sig (str/starts-with? new-sig "points:"))
                (let [json-str (subs new-sig 7)
                      points-array (try (js/JSON.parse json-str) (catch :default _ nil))]
                  (when points-array
                    (.fromData ref points-array))))))))

      :reagent-render
      (fn [_signature-data]
        ;; Use same internal dimensions as zoom canvas (360x120, 3:1 ratio)
        ;; Scale and center with CSS for the preview
        [:div {:style {:width "100%"
                       :height "100%"
                       :display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :overflow "hidden"}}
         [:> SignatureCanvas
          {:penColor "black"
           :backgroundColor "white"
           :canvasProps {:width 360
                         :height 120
                         :style {:pointer-events "none"}}
           :ref (fn [ref]
                  (when ref
                    (reset! canvas-ref ref)))}]])})))

(defn- render-signature-content
  "Render signature - handles points (JSON), base64 PNG, and SVG formats"
  [signature-data]
  (cond
    ;; No signature
    (or (nil? signature-data) (empty? signature-data))
    [:div {:style {:color "#9ca3af" :text-align "center" :font-size "0.875rem"}}
     (tr/tr :worksheets/click-to-sign)]

    ;; New format: points data (JSON) - use SignatureCanvas for display
    (str/starts-with? signature-data "points:")
    [signature-canvas-preview signature-data]

    ;; Old format: base64 PNG/JPEG data URL (backward compatibility)
    (str/starts-with? signature-data "data:image")
    [:img {:src signature-data
           :style {:max-width "100%"
                   :max-height "100%"
                   :object-fit "contain"}}]

    ;; SVG string format (backward compatibility)
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
                    :aspect-ratio "3 / 1"
                    :cursor "pointer"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :position "relative"
                    :overflow "hidden"}
            :on-click #(rf/dispatch [:worksheets/open-signature-zoom ref-dispatch-key label])}
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

(defn- signature-zoom-canvas
  "Inner component for signature canvas - Form-2 with proper lifecycle"
  [zoom-data]
  (let [sig-ref (r/atom nil)
        ;; Fixed dimensions that fit on mobile - NO CSS SCALING ALLOWED
        canvas-width 360
        canvas-height 120]
    (r/create-class
     {:component-did-mount
      (fn [_this]
        ;; Load existing signature after mount
        (when-let [ref @sig-ref]
          (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
                signature-data (if (= (:ref-dispatch-key zoom-data) :worksheets/set-maintainer-signature-ref)
                                 (:worksheet/maintainer-signature form-data)
                                 (:worksheet/customer-signature form-data))]
            (cond
              ;; New format: points data - use fromData()
              (and signature-data (str/starts-with? signature-data "points:"))
              (let [json-str (subs signature-data 7)
                    points-array (try (js/JSON.parse json-str) (catch :default _ nil))]
                (when points-array
                  (.fromData ^js ref points-array)))

              ;; Old format: base64 image - use fromDataURL() for backward compatibility
              (and signature-data (str/starts-with? signature-data "data:image"))
              (.fromDataURL ^js ref signature-data)))))

      :reagent-render
      (fn [_zoom-data]
        [:div {:style {:margin-bottom "1.5rem"
                       :display "flex"
                       :justify-content "center"}}
         [:> SignatureCanvas
          {:penColor "black"
           :backgroundColor "white"
           :canvasProps {:width canvas-width
                         :height canvas-height
                         :style {:border "2px solid #d1d5db"
                                 :border-radius "8px"
                                 :background "#ffffff"
                                 ;; Prevent page scroll while signing on mobile
                                 :touch-action "none"}}
           :ref (fn [ref]
                  (when ref
                    (reset! sig-ref ref)
                    (rf/dispatch [:worksheets/set-zoom-signature-ref ref])))}]])})))

(defn- signature-zoom-overlay
  "Full-screen overlay for signature editing with mobile-optimized canvas"
  []
  (let [zoom-data @(rf/subscribe [:worksheets/signature-zoom-data])]
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

        ;; Signature canvas - separate component to ensure fresh dimension calculation
        [signature-zoom-canvas zoom-data]

        ;; Action buttons
        [:div {:style {:display "flex" :justify-content "space-between" :gap "1rem"}}
         [:button {:type "button"
                   :on-click (fn []
                               (when-let [^js ref @(rf/subscribe [:worksheets/zoom-signature-ref])]
                                 (.clear ref)))
                   :style {:padding "0.75rem 1.5rem" :font-size "0.875rem" :color "#6b7280"
                           :background "transparent" :border "1px solid #d1d5db" :border-radius "6px"
                           :cursor "pointer" :font-weight "500"}}
          (tr/tr :worksheets/clear)]
         [:button {:type "button"
                   :on-click #(rf/dispatch [:worksheets/close-signature-zoom])
                   :style {:padding "0.75rem 1.5rem" :font-size "0.875rem" :color "white"
                           :background "#3b82f6" :border "none" :border-radius "6px"
                           :cursor "pointer" :font-weight "500"}}
          (tr/tr :worksheets/done)]]]])))

(defn signatures-section
  "Render complete signatures section"
  []
  (let [form-data @(rf/subscribe [:worksheets/modal-form-data])
        ;; Use signature data as keys to force re-render when signatures change
        maintainer-sig (:worksheet/maintainer-signature form-data)
        customer-sig (:worksheet/customer-signature form-data)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [:h3 {:style {:margin-bottom "1rem" :font-size "1.125rem" :font-weight "600" :color "#374151"}}
      (tr/tr :worksheets/signatures)]
     [:div {:style {:display "flex" :flex-direction "column" :gap "1rem"}}
      ^{:key (str "maintainer-" (hash maintainer-sig))}
      [signature-display :worksheets/set-maintainer-signature-ref (tr/tr :worksheets/maintainer-signature)]
      ^{:key (str "customer-" (hash customer-sig))}
      [signature-display :worksheets/set-customer-signature-ref (tr/tr :worksheets/customer-signature)]]]))

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
     :data-testid "worksheet-cancel-button"
     :on-click (fn []
                 (rf/dispatch [:worksheets/clear-modal-form])
                 (when on-cancel (on-cancel)))
     :text (tr/tr :worksheets/cancel)}]
   [enhanced-button/enhanced-button
    {:variant :primary
     :data-testid "worksheet-submit-button"
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
         [elevator-form-field errors]
         [work-info-form-fields errors]
         [time-tracking-form-fields errors]
         [notes-form-field errors]
         [materials-section]
         [signatures-section]])
      [modal-footer-buttons on-cancel on-save]]
     [signature-zoom-overlay]]))
