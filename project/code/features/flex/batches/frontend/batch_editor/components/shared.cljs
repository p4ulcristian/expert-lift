(ns features.flex.batches.frontend.batch-editor.components.shared
  (:require [router.frontend.zero :as router]
            [zero.frontend.re-frame :as rf]
            [features.flex.batches.frontend.batch-editor.state :as state]
            [features.flex.batches.frontend.batch-editor.utils :as utils]))

;; -----------------------------------------------------------------------------
;; ---- Shared UI Components ----

(defn get-current-step-info [batch-id batches index]
  (if batch-id
    (let [current-batch (first (filter #(= (:batch/id %) batch-id) batches))
          current-step (:batch/current-step current-batch)
          process-position (inc index)
          is-current-step (= process-position current-step)
          is-completed-step (< process-position current-step)]
      {:current-step current-step
       :is-current-step is-current-step
       :is-completed-step is-completed-step})
    ; If no batch-id, no special status
    {:current-step 0
     :is-current-step false
     :is-completed-step false}))

(defn get-process-styling [process is-current-step snapshot]
  (let [background-color (cond
                          (.-isDragging snapshot) "#f0f0f0"
                          :else "#fff")
        border-color (if (not is-current-step) (:process/color process) "#28a745")]
    {:background-color background-color
     :border-color border-color}))

(defn process-drag-handle [] 
  [:span {:style {:cursor "grab"} :class "drag-handle"} 
   "⋮⋮"])

(defn process-checkbox [process-id is-selected available-processes] 
  (when available-processes  ; Only show checkbox if this is an available process
    [:input {:type "checkbox"
             :checked is-selected
             :on-change #(state/toggle-selection process-id available-processes)
             :on-click #(.stopPropagation ^js %)  ; Prevent triggering drag
             :style {:margin-right "8px"
                     :cursor "pointer"
                     :transform "scale(1.2)"}}]))

(defn workflow-state-badge [workflow-state is-current-step]
  (when (and is-current-step workflow-state)
    [:div {:style {:position "absolute"
                   :top "8px"
                   :right "8px"
                   :background (case workflow-state
                                 "to-do" "#6366f1"
                                 "doing" "#f59e0b"
                                 "#6b7280")
                   :color "white"
                   :padding "2px 6px"
                   :border-radius "4px"
                   :font-size "9px"
                   :font-weight "600"
                   :text-transform "uppercase"
                   :letter-spacing "0.5px"
                   :z-index "10"}}
     workflow-state]))

(defn process-name-section [process batch-id batches on-batches-change available-processes] 
  (let [is-selected (state/is-selected? (:process/id process))]
    [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
     [process-checkbox (:process/id process) is-selected available-processes]
     [process-drag-handle]
     [:span {:style {:flex 1} :class "process-name"} (:process/name process)]]))

(defn circular-remove-button [process batch-id batches on-batches-change] 
  (when batch-id
    [:button {:on-click #(utils/remove-process-from-batch! batches batch-id (:process/id process) on-batches-change)
              :style {:position "absolute"
                      :top "-10px"
                      :right "-10px"
                      :width "20px"
                      :height "20px"
                      :border-radius "50%"
                      :background "#ef4444"
                      :border "2px solid #ffffff"
                      :color "white"
                      :cursor "pointer"
                      :font-size "12px"
                      :line-height "1"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :transition "all 0.2s ease"
                      :opacity "1"
                      :transform "scale(1)"
                      :z-index "20"
                      :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}
              :on-mouse-enter #(-> ^js % .-target .-style (.setProperty "background" "#dc2626"))
              :on-mouse-leave #(-> ^js % .-target .-style (.setProperty "background" "#ef4444"))
              :class "circular-remove-btn"}
     "×"]))

(defn navigate-to-workstation [workstation-id]
  (let [wsid @(rf/subscribe [:workspace/get-id])]
    (when (and wsid workstation-id)
      (router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" workstation-id "/task-board")}))))

(defn workstation-link [workstation index]
  [:span
   (when (> index 0) ", ")
   [:a {:href "#"
        :on-click (fn [e]
                    (.preventDefault ^js e)
                    (navigate-to-workstation (:workstation_id workstation)))
        :class "workstation-link"}
    (:workstation_name workstation)]])

(defn workstations-section [process] 
  (js/console.log "🏭 Workstations check for process:" 
                  "id:" (:process/id process)
                  "original-id:" (:process/original-id process)
                  "name:" (:process/name process)
                  "has-workstations:" (boolean (:workstations process))
                  "workstations:" (:workstations process))
  (when (and (:workstations process) (seq (:workstations process)))
    [:div {:class "workstations"}
     "Workstations: "
     (doall
      (map-indexed
       (fn [index workstation]
         ^{:key (:workstation_id workstation)}
         [workstation-link workstation index])
       (:workstations process)))]))

(defn step-progress-indicator [batch]
  (let [current-step (:batch/current-step batch)
        total-steps (count (:batch/processes batch))]
    (when (> total-steps 0)
      [:div {:class "batch-step-indicator"}
       [:span "Step"]
       [:span {:class "step-number"} 
        (str current-step "/" total-steps)]])))