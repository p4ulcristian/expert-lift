(ns features.flex.batches.frontend.batch-editor.components.process
  (:require [features.flex.batches.frontend.batch-editor.components.shared :as shared]
            [features.flex.batches.frontend.batch-editor.state :as state]
            [features.flex.batches.frontend.batch-editor.timing :as timing]))

;; -----------------------------------------------------------------------------
;; ---- Process Component Helper Functions ----

(defn- compute-process-component-state [process index batch-id batches]
  "Computes the state information needed for process component rendering"
  (let [current-batch (when batch-id (first (filter #(= (:batch/id %) batch-id) batches)))
        workflow-state (or (:batch/workflow-state current-batch) "to-do")
        step-info (shared/get-current-step-info batch-id batches index)
        is-selected (state/is-selected? (:process/id process))
        selected-count (count @state/selection-state)]
    {:current-batch current-batch
     :workflow-state workflow-state
     :step-info step-info
     :is-selected is-selected
     :selected-count selected-count}))

(defn- process-css-class [step-info workflow-state is-selected]
  "Generates CSS class string for process component"
  (str "draggable-item process-item"
       (cond
         (:is-current-step step-info) (str " current-step " workflow-state)
         (:is-completed-step step-info) " completed-step"
         :else "")
       (when is-selected " selected")))

(defn- process-container-style [step-info process is-selected]
  "Generates style map for process container"
  {:border (when-not (:is-current-step step-info) 
            (str "2px solid " (:process/color process)))
   :background-color (if is-selected "#e3f2fd" "#fff")
   :padding "8px"
   :margin "4px"
   :border-radius "4px"
   :display "flex"
   :flex-direction "column"
   :gap "4px"
   :cursor "grab"
   :position "relative"
   :box-shadow (when is-selected "0 0 0 2px #1976d2")
   :opacity (when (:is-completed-step step-info) "0.7")})

(defn- multi-drag-counter-badge [is-selected selected-count]
  "Renders multi-drag counter badge when multiple items are selected"
  (when (and is-selected (> selected-count 1))
    [:div {:style {:position "absolute"
                   :top "-8px"
                   :right "-8px"
                   :background "#ff4444"
                   :color "white"
                   :border-radius "50%"
                   :width "20px"
                   :height "20px"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :font-size "12px"
                   :font-weight "600"
                   :z-index "15"}
           :class "multi-drag-counter"}
     selected-count]))

(defn process-component [process index batch-id batches on-batches-change available-processes]
  (let [state (compute-process-component-state process index batch-id batches)
        css-class (process-css-class (:step-info state) (:workflow-state state) (:is-selected state))
        container-style (process-container-style (:step-info state) process (:is-selected state))]
    [:div {:data-id (:process/id process)
           :style container-style
           :class css-class}
     [multi-drag-counter-badge (:is-selected state) (:selected-count state)]
     [shared/workflow-state-badge (:workflow-state state) (:is-current-step (:step-info state))]
     [shared/circular-remove-button process batch-id batches on-batches-change]
     [shared/process-name-section process batch-id batches on-batches-change available-processes]
     [shared/workstations-section process]
     (when (:current-batch state)
       [timing/timing-display process (:current-batch state)])]))