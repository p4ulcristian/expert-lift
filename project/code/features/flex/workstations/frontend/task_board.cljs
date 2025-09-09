(ns features.flex.workstations.frontend.task-board
  (:require
   [features.flex.workstations.frontend.request :as workstations-request]
   [clojure.string]
   [features.flex.shared.frontend.components.body :as edit-page]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [zero.frontend.react :as zero-react]
   ["react-sortablejs" :refer [ReactSortable]]))

;; CSS Styles for drag and drop
(defn task-board-styles []
  [:style "
    .task-board-ghost {
      opacity: 0.5;
      background: #e3f2fd !important;
      border: 2px dashed #1976d2 !important;
      transform: rotate(3deg);
    }
    
    .task-board-chosen {
      box-shadow: 0 4px 12px rgba(0,0,0,0.3) !important;
      transform: scale(1.05);
    }
    
    .task-board-drag {
      opacity: 1 !important;
      background: #fff !important;
      box-shadow: 0 8px 24px rgba(0,0,0,0.4) !important;
      transform: rotate(5deg) scale(1.1);
      z-index: 9999 !important;
    }
    
    .batch-card:hover {
      box-shadow: 0 4px 8px rgba(0,0,0,0.2) !important;
      transform: translateY(-2px);
    }
    
    .batch-card:active {
      cursor: grabbing !important;
    }
  "])

;; Load batches for workstation
(defn get-workstation-current-batches [workstation-id]
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (workstations-request/get-batches-with-current-step-on-workstation
     workspace-id workstation-id
     (fn [batches]
       (when batches
         (r/dispatch [:db/assoc-in [:workstations :current-batches workstation-id] batches]))))))

;; Batch workflow state update
(defn update-batch-workflow-state! [batch-id new-state]
  (println "🔄 Updating batch" batch-id "to state:" new-state)
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (workstations-request/update-batch-workflow-state
     workspace-id batch-id new-state
     (fn [response]
       (println "✅ Batch workflow state updated:" response)
       ;; Reload batches to reflect the change
       (when-let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])]
         (get-workstation-current-batches workstation-id))))))

;; Drag and drop handler for task board
(defn handle-batch-move [from-status to-status batch-id]
  (when (not= from-status to-status)
    (update-batch-workflow-state! batch-id to-status)))

;; Draggable batch card
(defn batch-card [batch index workflow-state]
  (let [expanded-column @(r/subscribe [:db/get-in [:workstation :expanded-column]])
        is-expanded (= expanded-column workflow-state)
        is-last-step (not (:next_process_name batch))
        is-confirmed (= (:status batch) "complete")
        status-color (case workflow-state
                      "to-do" "#6c757d"
                      "doing" "#fd7e14" 
                      "done" "#198754"
                      "#6c757d")]
    [:div {:style {:background "white"
                   :border (str "2px solid " status-color)
                   :border-radius "8px"
                   :padding "16px"
                   :margin "8px 0"
                   :box-shadow "0 2px 4px rgba(0,0,0,0.1)"
                   :min-height "80px"
                   :cursor "grab"
                   :transition "all 0.2s ease"}
           :class "batch-card"
           :data-batch-id (:batch_id batch)
           :data-workflow-state workflow-state
           :data-confirmed is-confirmed}
     [:div {:style {:font-weight "bold" 
                    :margin-bottom "8px"
                    :font-size "16px"
                    :color "#333"}}
      (:part_name batch)]
     [:div {:style {:font-size "14px" 
                    :color "#666" 
                    :margin-bottom "4px"}}
      (str "Process: " (:process_name batch))]
     [:div {:style {:display "flex"
                    :justify-content "space-between"
                    :align-items "center"}}
      [:div {:style {:font-size "12px" 
                     :color "#999"}}
       (str "ID: " (subs (str (:batch_id batch)) 0 8) "...")]
      [:div {:style {:font-size "12px"
                     :padding "2px 8px"
                     :background status-color
                     :color "white"
                     :border-radius "12px"
                     :font-weight "600"}}
       (clojure.string/upper-case workflow-state)]]]))

(defn filter-batches-by-state [batches state]
  (filter #(= (:workflow_state %) state) batches))

(defn task-column [title status batches]
  (let [filtered-batches (filter-batches-by-state batches status)]
    [:div {:style {:flex "1"
                   :background "#e9ecef"
                   :border-radius "8px"
                   :padding "16px"
                   :min-height "400px"
                   :box-shadow "0 2px 8px rgba(0,0,0,0.1)"}}
     [:h3 {:style {:margin "0 0 16px 0"
                   :text-align "center"}}
      (str title " (" (count filtered-batches) ")")]
     [:> ReactSortable
      {:list (clj->js (if (empty? filtered-batches)
                        [{:id "empty-placeholder"}]
                        (mapv #(hash-map :id (:batch_id %)) filtered-batches)))
       :setList (fn [new-list] nil)
       :group "task-board"
       :animation 150
       :ghostClass "task-board-ghost"
       :chosenClass "task-board-chosen"
       :dragClass "task-board-drag"
       :onStart (fn [evt]
                  (let [item (.-item evt)
                        batch-id (.getAttribute item "data-batch-id")]
                    (println "🚀 Started dragging batch:" batch-id)))
       :onMove (fn [evt]
                 ;; Allow dropping everywhere including empty columns
                 true)
       :onEnd (fn [evt]
                (let [from-container (.-from evt)
                      to-container (.-to evt)
                      from-status (or (.getAttribute from-container "data-status")
                                      (cond
                                        (clojure.string/includes? (.-className from-container) "task-column-to-do") "to-do"
                                        (clojure.string/includes? (.-className from-container) "task-column-doing") "doing"
                                        (clojure.string/includes? (.-className from-container) "task-column-done") "done"
                                        :else nil))
                      to-status (or (.getAttribute to-container "data-status")
                                    (cond
                                      (clojure.string/includes? (.-className to-container) "task-column-to-do") "to-do"
                                      (clojure.string/includes? (.-className to-container) "task-column-doing") "doing"
                                      (clojure.string/includes? (.-className to-container) "task-column-done") "done"
                                      :else nil))]
                  (println "🔚 Drag ended - from:" from-status "to:" to-status)
                  (when (not= from-status to-status)
                    (let [item (.-item evt)
                          batch-id (.getAttribute item "data-batch-id")]
                      (println "🎯 Moving batch" batch-id "from" from-status "to" to-status)
                      (when (and batch-id from-status to-status (not= from-status to-status))
                        (handle-batch-move from-status to-status batch-id))))))
       :style {:min-height "200px"}
       :data-status status
       :className (str "task-column-" status)}
      (if (empty? filtered-batches)
        [:div {:key "empty-placeholder" 
                :data-id "empty-placeholder"
                :style {:text-align "center" :padding "20px" :color "#666"}}
         "Drop batches here"]
        (doall
         (map-indexed 
          (fn [index batch]
            ^{:key (:batch_id batch)}
            [:div {:data-id (:batch_id batch)
                   :data-batch-id (:batch_id batch)
                   :data-workflow-state status}
             [batch-card batch index status]])
          filtered-batches)))]]))

(defn task-board []
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])
        batches @(r/subscribe [:db/get-in [:workstations :current-batches workstation-id] []])]
    
    ;; Load batches when component mounts or workstation-id changes
    (zero-react/use-effect
     {:mount (fn []
               (when workstation-id
                 (get-workstation-current-batches workstation-id)))
      :deps [workstation-id]})
    
    [:div {:style {:display "grid"
                   :grid-template-columns "1fr 1fr 1fr"
                   :gap "16px"}}
     [task-column "To-do" "to-do" (or batches [])]
     [task-column "Doing" "doing" (or batches [])]
     [task-column "Done" "done" (or batches [])]]))

(defn task-board-content []
  [task-board])

(defn view []  
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])
        wsid @(r/subscribe [:workspace/get-id])
        workstation-name (or @(r/subscribe [:db/get-in [:workstation :name]]) "Workstation")]
    [:div
     [task-board-styles]
     [edit-page/view
      {:title (str workstation-name " - Task Board")
       :description (str "Manage workflow states for batches on " workstation-name)
       :title-buttons (list
                      ^{:key "operator"}
                      [button/view {:mode :filled
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 600 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" workstation-id "/operator")})}
                       "Operator"]
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" workstation-id)})}
                       "Back"])
       :body [task-board-content]}]]))