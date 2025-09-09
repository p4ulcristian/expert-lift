(ns features.flex.orders.frontend.job-components
  (:require
   ["react" :as react]
   [clojure.string :as clojure.string]
   [cljs.pprint]
   [features.flex.orders.frontend.request :as orders-request]
   [router.frontend.zero :as router]
   [zero.frontend.re-frame :refer [subscribe]]))

;; ============================================================================
;; UTILITY FUNCTIONS
;; ============================================================================

(defn job-to-string [job]
  (str job))

(defn show-job-string [job]
  (job-to-string job))

(defn truncate-id [id]
  (if id (subs id 0 8) "Unknown"))

(defn get-status-color [status]
  (case status
    "awaiting-inspection" "#f0ad4e"  ; warning yellow
    "inspected" "#17a2b8"            ; info blue
    "in-progress" "#5bc0de"          ; light blue
    "paused" "#fd7e14"               ; warning orange
    "job-complete" "#5cb85c"         ; success green
    "#666"))                         ; default gray

;; ============================================================================
;; BASIC UI COMPONENTS
;; ============================================================================

(defn expand-collapse-icon [expanded?]
  [:i {:class (str "fas " (if expanded? "fa-chevron-up" "fa-chevron-down"))
       :style {:color "#666"
               :font-size "12px"}}])

;; ============================================================================
;; PART IMAGE COMPONENT
;; ============================================================================

(defn part-image [part]
  [:img {:src (:picture_url part)
         :alt (:name part)
         :style {:width "32px"
                 :height "32px"
                 :border-radius "4px"
                 :object-fit "cover"
                 :border "1px solid #dee2e6"}}])

(defn part-name [job]
  [:span {:style {:font-weight 600
                  :color "#212529"
                  :font-size "16px"}}
   (get-in job [:part :name])])

(defn part-info-section [job]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "12px"}}
   [:div {:style {:display "flex"
                  :flex-direction "column"}}
    [:span {:style {:font-weight 600
                    :color "#212529"
                    :font-size "16px"}}
     (or (:job/package-name job) (str "Job #" (truncate-id (:job/id job))))]
    (when (and (not (:job/package-name job)) (:job/id job))
      [:span {:style {:font-size "12px"
                      :color "#6c757d"}}
       (str "ID: " (truncate-id (:job/id job)))])]])

;; ============================================================================
;; COLOR INDICATOR COMPONENT
;; ============================================================================

(defn color-swatch [color]
  [:div {:style {:width "20px"
                 :height "20px"
                 :border-radius "4px"
                 :background-color (:basecolor color)
                 :border "1px solid #dee2e6"}}])

(defn color-name [color]
  [:span {:style {:color "#495057"
                  :font-size "14px"}}
   (:name color)])

(defn color-indicator [color]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "6px"}}
   [color-swatch color]
   [color-name color]])

;; ============================================================================
;; QUANTITY HEADER DISPLAY COMPONENT
;; ============================================================================

(defn quantity-header-display [quantity]
  [:div {:style {:font-size "14px"
                 :color "#495057"
                 :font-weight 500
                 :background-color "#f8f9fa"
                 :padding "4px 8px"
                 :border-radius "4px"
                 :border "1px solid #dee2e6"}}
   (str quantity " pcs")])

;; ============================================================================
;; JOB STATUS COMPONENT
;; ============================================================================

(defn job-status-label [status]
  [:div {:style {:background-color (get-status-color status)
                 :padding "4px 8px"
                 :border-radius "4px"
                 :color "#333"
                 :font-size "12px"
                 :font-weight 600
                 :text-transform "capitalize"}}
   (clojure.string/replace status "-" " ")])

;; ============================================================================
;; JOB STATUS PROGRESS BAR COMPONENT
;; ============================================================================

(def job-status-flow
  ["awaiting-inspection"
   "inspected"
   "in-progress"
   "paused"])

(defn get-status-index [status]
  (.indexOf job-status-flow status))

(defn timeline-step-icon [status is-active?]
  [:div {:style {:width "24px"
                 :height "24px"
                 :border-radius "12px"
                 :background-color (if is-active? (get-status-color status) "#e0e0e0")
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :margin-bottom "8px"
                 :z-index 2}}
   (when (= status "inspected")
     [:i {:class "fas fa-check"
          :style {:color "white"
                  :font-size "12px"}}])])

(defn timeline-connector [is-last? is-active? status]
  (when-not is-last?
    [:div {:style {:position "absolute"
                   :top "12px"
                   :left "50%"
                   :right "-50%"
                   :height "2px"
                   :background-color (if is-active?
                                      (get-status-color status)
                                      "#e0e0e0")
                   :z-index 1}}]))

(defn timeline-step-label [label is-active?]
  [:div {:style {:font-size "12px"
                 :color (if is-active? "#333" "#999")
                 :text-align "center"
                 :white-space "nowrap"
                 :overflow "hidden"
                 :text-overflow "ellipsis"
                 :width "100%"}}
   label])

(defn timeline-step [label status is-last? is-active?]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :position "relative"
                 :flex 1
                 :min-width "120px"}}
   [timeline-step-icon status is-active?]
   [timeline-connector is-last? is-active? status]
   [timeline-step-label label is-active?]])

(defn job-status-progress-bar [current-status]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :align-items "center"
                 :margin "12px 0"
                 :position "relative"
                 :padding "0 12px"}}
   (for [[index status] (map-indexed vector job-status-flow)]
     ^{:key status}
     [timeline-step 
      (clojure.string/replace status "-" " ")
      status
      (= index (dec (count job-status-flow)))
      (<= (get-status-index status) (get-status-index current-status))])])

;; ============================================================================
;; JOB HEADER COMPONENT
;; ============================================================================

(defn job-header [job]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :width "100%"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :gap "12px"}}
    [part-info-section job]
    (when-let [status (:job/status job)]
      [job-status-label status])]
   (when-let [status (:job/status job)]
     [job-status-progress-bar status])])

;; ============================================================================
;; FORM FIELD COMPONENTS
;; ============================================================================

(defn field-label [text]
  [:div {:style {:font-size "10px"
                 :color "#6c757d"
                 :font-weight 600
                 :text-transform "uppercase"
                 :margin-bottom "4px"}}
   text])

(defn field-container [content]
  [:div {:style {:background-color "#ffffff"
                 :padding "12px"
                 :border-radius "6px"
                 :border "1px solid #dee2e6"}}
   content])

(defn select-input [value on-change options]
  [:select {:style {:width "100%"
                    :padding "8px"
                    :border "1px solid #dee2e6"
                    :border-radius "4px"
                    :font-size "14px"
                    :color "#212529"
                    :background-color "#ffffff"}
            :value value
            :on-change on-change}
   (for [option options]
     ^{:key option}
     [:option {:value option} 
      (clojure.string/replace option "-" " ")])])

(defn format-option-text [option]
  (clojure.string/replace option "-" " "))

;; ============================================================================
;; MATERIAL FIELD COMPONENT
;; ============================================================================

(defn get-material-options []
  ["aluminum" "steel" "stainless-steel" "brass" "copper" "titanium" "plastic" "carbon-fiber"])

(defn material-field [job material set-material]
  (when (:material job)
    [field-container
     [:<>
      [field-label "Material"]
      [select-input material 
                    #(set-material (.-value (.-target %)))
                    (get-material-options)]]]))

;; ============================================================================
;; QUANTITY FIELD COMPONENTS
;; ============================================================================

(defn quantity-button [text color enabled? on-click]
  [:button {:style {:background color
                    :border "none"
                    :color "white"
                    :width "24px"
                    :height "24px"
                    :border-radius "4px"
                    :font-size "14px"
                    :font-weight "bold"
                    :cursor (if enabled? "pointer" "not-allowed")
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"}
            :disabled (not enabled?)
            :on-click on-click}
   text])

(defn decrease-quantity-button [quantity set-quantity]
  [quantity-button "-" 
                   (if (> quantity 1) "#dc3545" "#ccc")
                   (> quantity 1)
                   #(when (> quantity 1)
                      (set-quantity (dec quantity)))])

(defn increase-quantity-button [quantity set-quantity]
  [quantity-button "+" 
                   "#28a745"
                   true
                   #(set-quantity (inc quantity))])

(defn quantity-display [quantity]
  [:div {:style {:font-size "14px"
                 :color "#212529"
                 :font-weight 500
                 :min-width "60px"
                 :text-align "center"}}
   (str quantity " pieces")])

(defn quantity-controls [quantity set-quantity]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"}}
   [decrease-quantity-button quantity set-quantity]
   [quantity-display quantity]
   [increase-quantity-button quantity set-quantity]])

(defn quantity-field [job quantity set-quantity]
  (when (:quantity job)
    [field-container
     [:<>
      [field-label "Quantity"]
      [quantity-controls quantity set-quantity]]]))

;; ============================================================================
;; SURFACE FIELD COMPONENT
;; ============================================================================

(defn get-surface-options []
  ["raw" "machined" "painted" "anodized" "powder-coated" "rusty" "oxidized" "polished"])

(defn surface-field [job current-surface set-current-surface]
  (when (:current_surface job)
    [field-container
     [:<>
      [field-label "Current Surface"]
      [select-input current-surface
                    #(set-current-surface (.-value (.-target %)))
                    (get-surface-options)]]]))

;; ============================================================================
;; JOB TYPE FIELD COMPONENT
;; ============================================================================

(defn job-type-display [job-type]
  [:div {:style {:font-size "14px"
                 :color "#212529"
                 :font-weight 500}}
   job-type])

(defn job-type-field [job]
  (when (:type job)
    [field-container
     [:<>
      [field-label "Job Type"]
      [job-type-display (:type job)]]]))

;; ============================================================================
;; LOCATION FIELD COMPONENT
;; ============================================================================

(defn location-field [job]
  (when (:location job)
    [field-container
     [:<>
      [field-label "Location"]
      [:div {:style {:font-size "14px"
                     :color "#212529"
                     :font-weight 500}}
       (:location job)]]]))

;; ============================================================================
;; JOB DETAILS GRID COMPONENT
;; ============================================================================

(defn job-details-grid [job quantity set-quantity material set-material current-surface set-current-surface]
  [:div {:style {:display "grid"
                 :grid-template-columns "1fr 1fr"
                 :gap "12px"}}
   [material-field job material set-material]
   [quantity-field job quantity set-quantity]
   [surface-field job current-surface set-current-surface]
   [job-type-field job]
   [location-field job]])

(defn job-details-section [job quantity set-quantity material set-material current-surface set-current-surface]
  [:div {:style {:margin-bottom "20px"}}
   [:h4 {:style {:margin "0 0 12px 0"
                 :font-size "14px"
                 :font-weight 600
                 :color "#495057"}}
    "Job Details"]
   [job-details-grid job quantity set-quantity material set-material current-surface set-current-surface]])

;; ============================================================================
;; DESCRIPTION SECTION COMPONENT
;; ============================================================================

(defn section-title [title]
  [:h4 {:style {:margin "0 0 8px 0"
                :font-size "14px"
                :font-weight 600
                :color "#495057"}}
   title])

(defn description-textarea [description set-description]
  [:textarea {:style {:width "100%"
                      :min-height "80px"
                      :padding "12px"
                      :border "1px solid #dee2e6"
                      :border-radius "6px"
                      :font-size "14px"
                      :color "#495057"
                      :background-color "#ffffff"
                      :resize "vertical"
                      :font-family "inherit"}
              :value description
              :on-change #(set-description (.-value (.-target %)))
              :placeholder "Enter job description..."}])

(defn description-section [job description set-description]
  [:div {:style {:margin-bottom "20px"}}
   [section-title "Description"]
   [description-textarea description set-description]])

;; ============================================================================
;; CHANGE DETECTION
;; ============================================================================

(defn has-changes? [job quantity material current-surface description]
  (or (not= quantity (:quantity job))
      (not= material (:material job))
      (not= current-surface (:current_surface job))
      (not= description (:description job))))

;; ============================================================================
;; CONFIRM BUTTON COMPONENT
;; ============================================================================

(defn confirm-button-style [has-changes? saving?]
  (merge {:border "none"
          :color "white"
          :padding "10px 20px"
          :border-radius "6px"
          :font-size "14px"
          :font-weight 600
          :cursor (if (and has-changes? (not saving?)) "pointer" "not-allowed")
          :transition "background-color 0.2s"}
         (if has-changes?
           {:background "#28a745"}
           {:background "#6c757d"})))

(defn handle-confirm-click [job quantity material current-surface description set-saving refresh-fn]
  (let [job-id (:job/id job)
        ;; Merge original job data with updated values
        updated-job (merge job
                          {:job/id job-id
                           :job/material material
                           :job/current-surface current-surface
                           :job/quantity quantity
                           :job/description description
                           ;; Set status to inspected when confirming job changes
                           :job/status "inspected"
                           :job/type (:type job)
                           ;; Extract IDs from nested objects
                           :job/look-id (get-in job [:color :id])
                           :job/part-id (get-in job [:part :id])})]
    (set-saving true)
    (orders-request/update-job
     (:job/id updated-job)
     updated-job
     (fn [response]
       (set-saving false)
       ;; Refresh the order data to show updated job status
       (when refresh-fn
         (refresh-fn))))))

(defn confirm-button [job quantity material current-surface description saving? set-saving refresh-fn]
  (let [has-changes? (has-changes? job quantity material current-surface description)]
    [:button {:style (confirm-button-style has-changes? saving?)
              :disabled (or saving? (not has-changes?))
              :on-mouse-over (when (and has-changes? (not saving?)) 
                               #(set! (.-style.backgroundColor (.-target %)) "#218838"))
              :on-mouse-leave (when (and has-changes? (not saving?)) 
                                #(set! (.-style.backgroundColor (.-target %)) "#28a745"))
              :on-click #(when (and has-changes? (not saving?))
                           (handle-confirm-click job quantity material current-surface description set-saving refresh-fn))}
     (cond
       saving? "Saving..."
       has-changes? "Confirm"
       :else "No Changes")]))

;; ============================================================================
;; BATCH NAMING UTILITIES
;; ============================================================================

(defn generate-batch-name [part-name color-name quantity]
  (let [safe-part-name (or part-name "Unknown Part")
        safe-color-name (or color-name "Unknown Color")
        safe-quantity (or quantity 1)]
    (str safe-part-name " - " safe-color-name " - " safe-quantity)))

;; ============================================================================
;; BATCH BUTTON COMPONENT
;; ============================================================================

(defn batch-button-style []
  {:border "none"
   :color "white"
   :padding "10px 20px"
   :border-radius "6px"
   :font-size "14px"
   :font-weight 600
   :cursor "pointer"
   :background "#007bff"
   :transition "background-color 0.2s"})

(defn handle-batch-click [job quantity material current-surface description set-saving]
  (let [wsid @(subscribe [:workspace/get-id])
        job-id (:job/id job)
        url (str "/flex/ws/" wsid "/batches/" job-id)
        part-name (get-in job [:part :name])
        color-name (get-in job [:color :name])
        batch-name (generate-batch-name part-name color-name quantity)
        batch-data {:job/id job-id
                   :job/description batch-name
                   :job/quantity quantity}]
    (set-saving true)
    (orders-request/create-batch
     wsid
     batch-data
     (fn [batch-response]
       (set-saving false)
       ;; Navigate after successful batch creation
       (router/navigate! {:path url})))))

(defn batch-button [job quantity material current-surface description saving? set-saving]
  [:button {:style (batch-button-style)
            :disabled saving?
            :on-mouse-over (when (not saving?) 
                             #(set! (.-style.backgroundColor (.-target %)) "#0056b3"))
            :on-mouse-leave (when (not saving?) 
                              #(set! (.-style.backgroundColor (.-target %)) "#007bff"))
            :on-click #(when (not saving?)
                         (handle-batch-click job quantity material current-surface description set-saving))}
   (if saving? "Creating Batch..." "Batch")])

(defn confirm-section [job quantity material current-surface description saving? set-saving refresh-fn]
  [:div {:style {:display "flex"
                 :justify-content "flex-end"
                 :gap "12px"
                 :margin-top "20px"}}
   [batch-button job quantity material current-surface description saving? set-saving]
   [confirm-button job quantity material current-surface description saving? set-saving refresh-fn]])

;; ============================================================================
;; BATCHES SECTION COMPONENT
;; ============================================================================

(defn batch-card-style []
  {:background-color "#ffffff"
   :border "1px solid #dee2e6"
   :border-radius "8px"
   :padding "12px"
   :margin-bottom "8px"
   :cursor "pointer"
   :transition "all 0.2s ease"})

(defn batch-status-badge [status]
  [:div {:style {:font-size "12px"
                 :font-weight "600"
                 :color "#fff"
                 :background-color (case status
                                    "awaiting" "#f0ad4e"
                                    "in-progress" "#5bc0de"
                                    "done" "#5cb85c"
                                    "#6c757d")
                 :padding "4px 8px"
                 :border-radius "4px"
                 :display "inline-block"}}
   status])

(defn process-step-vertical [process current-step is-last?]
  "Renders a single process step in vertical layout"
  (let [step-num (:step_order process)
        is-current (= step-num current-step)
        is-completed (< step-num current-step)]
    [:div {:style {:display "flex"
                   :align-items "flex-start"
                   :position "relative"
                   :padding "1px 0"}}
     [:div {:style {:display "flex"
                    :flex-direction "column" 
                    :align-items "center"
                    :margin-right "6px"
                    :flex-shrink 0}}
      [:div {:style {:width "16px"
                     :height "16px"
                     :border-radius "50%"
                     :background-color (cond
                                       is-completed "#5cb85c"
                                       is-current "#5bc0de"
                                       :else "#e9ecef")
                     :display "flex"
                     :align-items "center"
                     :justify-content "center"
                     :color "#fff"
                     :font-size "9px"
                     :font-weight "bold"
                     :z-index 2}}
       step-num]
      (when-not is-last?
        [:div {:style {:width "1px"
                       :height "12px"
                       :background-color (if is-completed "#5cb85c" "#e9ecef")
                       :margin-top "1px"}}])]
     [:div {:style {:flex 1
                    :padding-top "1px"}}
      [:div {:style {:font-size "11px"
                     :font-weight "600"
                     :color "#212529"
                     :margin-bottom "0px"}}
       (:name process)]
      (when (:description process)
        [:div {:style {:font-size "9px"
                       :color "#6c757d"
                       :line-height "1.2"}}
         (:description process)])]]))

(defn process-step [process current-step]
  "Legacy horizontal process step - kept for backward compatibility"
  (let [step-num (:step_order process)
        is-current (= step-num current-step)
        is-completed (< step-num current-step)]
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :align-items "center"
                   :position "relative"
                   :min-width "100px"}}
     [:div {:style {:width "24px"
                    :height "24px"
                    :border-radius "50%"
                    :background-color (cond
                                      is-completed "#5cb85c"
                                      is-current "#5bc0de"
                                      :else "#e9ecef")
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :color "#fff"
                    :font-size "12px"
                    :font-weight "bold"
                    :margin-bottom "8px"
                    :z-index 2}}
      step-num]
     [:div {:style {:text-align "center"}}
      [:div {:style {:font-size "12px"
                     :font-weight "600"
                     :color "#212529"
                     :margin-bottom "2px"}}
       (:name process)]
      [:div {:style {:font-size "11px"
                     :color "#6c757d"
                     :white-space "nowrap"
                     :overflow "hidden"
                     :text-overflow "ellipsis"
                     :max-width "100px"}}
       (:description process)]]
     [:div {:style {:position "absolute"
                    :top "12px"
                    :right "-50px"
                    :width "50px"
                    :height "2px"
                    :background-color (if is-completed "#5cb85c" "#e9ecef")
                    :z-index 1}}]]))

(defn batch-card-vertical [batch job-id job]
  "Renders batch card with vertical process list for order inspection"
  (let [wsid @(subscribe [:workspace/get-id])
        job-url (str "/flex/ws/" wsid "/batches/" job-id)
        processes (:batch/processes batch)]
    [:div {:style (batch-card-style)
           :on-click #(router/navigate! {:path job-url})}
     [:div {:style {:display "flex"
                    :justify-content "space-between"
                    :align-items "flex-start"
                    :margin-bottom "16px"}}
      [:div
       [:div {:style {:display "flex"
                      :align-items "center"
                      :gap "8px"
                      :margin-bottom "4px"}}
        (when (:batch/part-picture-url batch)
          [:img {:src (:batch/part-picture-url batch)
                 :style {:width "24px"
                         :height "24px"
                         :border-radius "4px"
                         :object-fit "cover"}}])
        [:div {:style {:font-size "14px"
                       :font-weight "600"
                       :color "#212529"}}
         (or (:batch/part-name batch) (:batch/description batch) "Unnamed Batch")]]
       [:div {:style {:display "flex"
                      :align-items "center"
                      :gap "6px"}}
        (when (:batch/color-basecolor batch)
          [:div {:style {:width "16px"
                         :height "16px"
                         :border-radius "3px"
                         :background-color (:batch/color-basecolor batch)
                         :border "1px solid #dee2e6"}}])
        [:div {:style {:font-size "12px"
                       :color "#6c757d"}}
         (:batch/color-name batch)]]]
      [:div {:style {:display "flex"
                     :flex-direction "column"
                     :align-items "flex-end"
                     :gap "4px"}}
       [batch-status-badge (:batch/status batch)]
       [:div {:style {:font-size "12px"
                      :color "#495057"
                      :background-color "#e9ecef"
                      :padding "2px 6px"
                      :border-radius "4px"}}
        "Qty: " (:batch/quantity batch)]]]
     (when (seq processes)
       [:div {:style {:border-top "1px solid #e9ecef"
                      :padding-top "4px"
                      :display "grid"
                      :grid-template-columns "1fr 1fr"
                      :gap "12px"}}
        [:div
         [:h5 {:style {:margin "0 0 6px 0"
                       :font-size "10px"
                       :font-weight "600"
                       :color "#6c757d"
                       :text-transform "uppercase"}}
          "Properties"]
         [:pre {:style {:font-size "8px"
                        :color "#6c757d"
                        :background "#f8f9fa"
                        :padding "4px"
                        :border-radius "2px"
                        :overflow "auto"
                        :white-space "pre-wrap"
                        :max-height "200px"
                        :font-family "monospace"
                        :line-height "1.2"}}
          (with-out-str (cljs.pprint/pprint (or (:batch/form-data batch) 
                                               (:job/form-data job)
                                               "No form data found")))]]
        [:div
         [:h5 {:style {:margin "0 0 6px 0"
                       :font-size "10px"
                       :font-weight "600"
                       :color "#6c757d"
                       :text-transform "uppercase"}}
          "Steps"]
         [:div {:style {:display "flex"
                        :flex-direction "column"}}
          (map-indexed
           (fn [idx process]
             ^{:key (str (:id process) "-" (:step_order process))}
             [process-step-vertical process (:batch/current-step batch) (= idx (dec (count processes)))])
           processes)]]])]))

(defn batch-card [batch job-id]
  "Original horizontal batch card - kept for backward compatibility"
  (let [wsid @(subscribe [:workspace/get-id])
        job-url (str "/flex/ws/" wsid "/batches/" job-id)]
    [:div {:style (batch-card-style)
           :on-click #(router/navigate! {:path job-url})}
     [:div {:style {:display "flex"
                    :justify-content "space-between"
                    :align-items "flex-start"
                    :margin-bottom "12px"}}
      [:div
       [:div {:style {:display "flex"
                      :align-items "center"
                      :gap "8px"
                      :margin-bottom "4px"}}
        (when (:batch/part-picture-url batch)
          [:img {:src (:batch/part-picture-url batch)
                 :style {:width "24px"
                         :height "24px"
                         :border-radius "4px"
                         :object-fit "cover"}}])
        [:div {:style {:font-size "14px"
                       :font-weight "600"
                       :color "#212529"}}
         (or (:batch/part-name batch) (:batch/description batch) "Unnamed Batch")]]
       [:div {:style {:display "flex"
                      :align-items "center"
                      :gap "6px"}}
        (when (:batch/color-basecolor batch)
          [:div {:style {:width "16px"
                         :height "16px"
                         :border-radius "3px"
                         :background-color (:batch/color-basecolor batch)
                         :border "1px solid #dee2e6"}}])
        [:div {:style {:font-size "12px"
                       :color "#6c757d"}}
         (:batch/color-name batch)]]]
      [:div {:style {:display "flex"
                     :flex-direction "column"
                     :align-items "flex-end"
                     :gap "4px"}}
       [batch-status-badge (:batch/status batch)]
       [:div {:style {:font-size "12px"
                      :color "#495057"
                      :background-color "#e9ecef"
                      :padding "2px 6px"
                      :border-radius "4px"}}
        "Qty: " (:batch/quantity batch)]]]
     [:div {:style {:margin-top "12px"
                    :padding "0 25px"
                    :display "flex"
                    :align-items "flex-start"
                    :overflow-x "auto"
                    :gap "0"}}
      (for [process (:batch/processes batch)]
        ^{:key (str (:id process) "-" (:step_order process))}
        [process-step process (:batch/current-step batch)])]]))

(defn batches-section-inspection [job]
  "Renders batches section with vertical process lists for inspection"
  (let [batches (:job/batches job)]
    [:div {:style {:margin-bottom "12px"}}
     [:h4 {:style {:margin "0 0 6px 0"
                   :font-size "13px"
                   :font-weight 600
                   :color "#212529"}}
      (str "Job Batches" (when batches (str " (" (count batches) ")")))]
     (if (seq batches)
       [:div {:style {:display "flex"
                      :flex-direction "column"
                      :gap "6px"}}
        (for [batch batches]
          ^{:key (:batch/id batch)}
          [batch-card-vertical batch (:job/id job) job])]
       [:div {:style {:padding "12px"
                      :background-color "#f8f9fa"
                      :border-radius "4px"
                      :border "1px solid #dee2e6"
                      :text-align "center"
                      :color "#6c757d"
                      :font-size "12px"}}
        [:i {:class "fas fa-info-circle"
             :style {:margin-right "6px"}}]
        "No batches have been created for this job yet."])]))

(defn batches-section [job]
  "Original batches section - kept for backward compatibility"
  (let [batches (:job/batches job)]
    [:div {:style {:margin-bottom "20px"}}
     [:h4 {:style {:margin "0 0 12px 0"
                   :font-size "16px"
                   :font-weight 600
                   :color "#212529"}}
      (str "Batches" (when batches (str " (" (count batches) ")")))]
     (if (seq batches)
       [:div {:style {:display "flex"
                      :flex-direction "column"
                      :gap "8px"}}
        (for [batch batches]
          ^{:key (:batch/id batch)}
          [batch-card batch (:job/id job)])]
       [:div {:style {:padding "16px"
                      :background-color "#f8f9fa"
                      :border-radius "6px"
                      :border "1px solid #dee2e6"
                      :text-align "center"
                      :color "#6c757d"
                      :font-size "14px"}}
        "No batches have been created for this job yet."])]))

;; ============================================================================
;; JOB EXPANDED CONTENT COMPONENT
;; ============================================================================

(defn expanded-content-container [content]
  [:div {:style {:border-top "1px solid #dee2e6"
                 :padding "20px"
                 :background-color "#f8f9fa"}}
   content])

(defn job-expanded-content-inspection [job refresh-fn]
  "Renders expanded job content optimized for order inspection"
  [expanded-content-container
   [:<>
    [:div {:style {:margin-bottom "16px"}}
     [:h4 {:style {:margin "0 0 8px 0"
                   :font-size "14px"
                   :font-weight 600
                   :color "#495057"}}
      "Job Form Data"]
     [:pre {:style {:font-size "9px"
                    :color "#495057"
                    :background "#ffffff"
                    :border "1px solid #dee2e6"
                    :border-radius "6px"
                    :padding "12px"
                    :overflow "auto"
                    :white-space "pre-wrap"
                    :max-height "300px"
                    :font-family "monospace"
                    :line-height "1.3"}}
      (with-out-str (cljs.pprint/pprint (or (:job/form-data job) 
                                           "No job form data found")))]]
    [batches-section-inspection job]]])

(defn job-expanded-content [job refresh-fn]
  "Original expanded content - kept for backward compatibility"
  [expanded-content-container
   [:<>
    [batches-section job]
    (when (:description job)
      [:div {:style {:margin-top "16px"}}
       [:h4 {:style {:margin "0 0 8px 0"
                     :font-size "14px"
                     :font-weight 600
                     :color "#495057"}}
        "Job Description"]
       [:div {:style {:padding "12px"
                      :background-color "#ffffff"
                      :border "1px solid #dee2e6"
                      :border-radius "6px"
                      :color "#495057"
                      :font-size "14px"}}
        (:description job)]])]])

;; ============================================================================
;; EXPANDABLE JOB ITEM COMPONENTS
;; ============================================================================

(defn job-header-style [expanded?]
  {:padding "20px"
   :cursor "pointer"
   :background-color (if expanded? "#f8f9fa" "#ffffff")
   :display "flex"
   :justify-content "space-between"
   :align-items "center"
   :user-select "none"
   :-webkit-user-select "none"
   :-moz-user-select "none"
   :-ms-user-select "none"})

(defn handle-job-click [job set-expanded event]
  (.preventDefault ^js event)
  (.stopPropagation ^js event)
  (show-job-string job)
  (set-expanded (fn [prev] (not prev))))

(defn job-header-section [job expanded set-expanded]
  [:div {:style (job-header-style expanded)
         :on-click #(handle-job-click job set-expanded %)}
   [job-header job]
   [expand-collapse-icon expanded]])

(defn job-item-container [content]
  [:div {:style {:border "2px solid #dee2e6"
                 :border-radius "12px"
                 :margin-bottom "16px"
                 :background-color "#ffffff"
                 :box-shadow "0 4px 6px rgba(0,0,0,0.07)"
                 :overflow "hidden"}}
   content])

(defn expandable-job-item-inspection [job refresh-fn]
  "Expandable job item optimized for order inspection with vertical process lists"
  (let [[expanded set-expanded] (react/useState true)] ; Start expanded for inspection
    [job-item-container
     [:<> 
      [job-header-section job expanded set-expanded]
      (when expanded
        [job-expanded-content-inspection job refresh-fn])]]))

(defn expandable-job-item [job refresh-fn]
  "Original expandable job item - kept for backward compatibility"
  (let [[expanded set-expanded] (react/useState false)]
    [job-item-container
     [:<> 
      [job-header-section job expanded set-expanded]
      (when expanded
        [job-expanded-content job refresh-fn])]]))

;; ============================================================================
;; JOBS LIST COMPONENTS
;; ============================================================================

(defn jobs-list-container [content]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "10px"}}
   content])

(defn render-job-item [job refresh-fn]
  ^{:key (or (:job/id job) (str "job-" (hash job)))}
  [expandable-job-item job refresh-fn])

(defn jobs-list-inspection [jobs refresh-fn]
  "Jobs list optimized for order inspection with vertical process layouts"
  [jobs-list-container
   (for [job jobs]
     ^{:key (or (:job/id job) (str "job-" (hash job)))}
     [expandable-job-item-inspection job refresh-fn])])

(defn jobs-list [jobs refresh-fn]
  "Original jobs list - kept for backward compatibility"
  [jobs-list-container
   (for [job jobs]
     ^{:key (or (:job/id job) (str "job-" (hash job)))}
     [render-job-item job refresh-fn])])

;; ============================================================================
;; JOBS OVERVIEW CARD COMPONENT
;; ============================================================================

(defn get-jobs-to-show [order]
  (let [jobs (:order/jobs order)]
    (if (and jobs (seq jobs)) 
      jobs
      [])))

(defn create-jobs-button [order refresh-fn]
  [:button {:style {:background "#007bff"
                    :color "white"
                    :border "none"
                    :padding "12px 24px"
                    :border-radius "8px"
                    :font-size "16px"
                    :font-weight "600"
                    :cursor "pointer"
                    :transition "background-color 0.2s"
                    :margin-top "16px"}
            :on-mouse-over #(set! (.-style.backgroundColor (.-target %)) "#0056b3")
            :on-mouse-leave #(set! (.-style.backgroundColor (.-target %)) "#007bff")
            :on-click (fn []
                        (let [wsid @(subscribe [:workspace/get-id])]
                          (orders-request/create-jobs-from-configuration
                           wsid
                           (:order/id order)
                           (fn [response]
                             (when refresh-fn (refresh-fn))))))}
   [:i {:class "fas fa-plus"
        :style {:margin-right "8px"}}]
   "Create Jobs from Configuration"])

(defn jobs-empty-state [order refresh-fn]
  [:div {:style {:text-align "center"
                 :padding "40px"
                 :color "#666"
                 :background-color "#f8f9fa"
                 :border-radius "8px"
                 :border "1px solid #dee2e6"}}
   [:i {:class "fas fa-briefcase"
        :style {:font-size "48px"
                :color "#dee2e6"
                :margin-bottom "16px"}}]
   [:h3 {:style {:margin "0 0 8px 0"
                 :color "#666"}}
    "No Jobs Found"]
   [:p {:style {:margin "0 0 16px 0"
                :font-size "14px"}}
    "This order doesn't have any jobs yet."]
   (when (:data order)
     [create-jobs-button order refresh-fn])])

(defn jobs-overview-card-inspection [order refresh-fn]
  "Jobs overview card optimized for order inspection with vertical process displays"
  (let [jobs-to-show (get-jobs-to-show order)]
    (if (seq jobs-to-show)
      [jobs-list-inspection jobs-to-show refresh-fn]
      [jobs-empty-state order refresh-fn])))

(defn jobs-overview-card [order refresh-fn]
  (let [jobs-to-show (get-jobs-to-show order)]
    (if (seq jobs-to-show)
      [jobs-list-inspection jobs-to-show refresh-fn]
      [jobs-empty-state order refresh-fn]))) 