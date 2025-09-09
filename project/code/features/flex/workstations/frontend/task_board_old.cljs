(ns features.flex.workstations.frontend.task-board
  (:require
   [features.flex.workstations.frontend.request :as workstations-request]
   [re-frame.core :as r]
   [reagent.core :as reagent]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [zero.frontend.react :as zero-react]
   ["react-sortablejs" :as ReactSortable]))

(defn workstation-header []
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])
        wsid @(r/subscribe [:workspace/get-id])]
    [:div {:style {:background "linear-gradient(135deg, #2c3e50 0%, #34495e 50%, #2c3e50 100%)"
                   :color "white"
                   :padding "24px 32px"
                   :border-radius "12px"
                   :margin-bottom "32px"
                   :box-shadow "0 8px 32px rgba(0,0,0,0.12)"}}
     [:div {:style {:display "flex"
                    :justify-content "space-between"
                    :align-items "center"}}
      [:div
       [:h1 {:style {:font-size "2rem" 
                     :font-weight 700 
                     :color "white" 
                     :margin "0 0 8px 0"
                     :text-shadow "0 2px 4px rgba(0,0,0,0.2)"}} 
        "Task Board"]
       [:p {:style {:font-size "1.1rem"
                    :margin 0
                    :opacity 0.9
                    :font-weight 400}}
        (or @(r/subscribe [:db/get-in [:workstation :name]]) "Workstation")]]
      [:div {:style {:display "flex" :gap "12px"}}
       [button/view {:mode :filled
                     :color "rgba(255,255,255,0.2)"
                     :style {:color "white"
                             :border "2px solid rgba(255,255,255,0.3)"
                             :fontWeight 600
                             :padding "12px 24px"
                             :borderRadius "8px"
                             :backdropFilter "blur(10px)"}
                     :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" workstation-id "/operator")})}
        "Operator"]
       [button/view {:mode :outlined
                     :style {:color "white"
                             :border "2px solid rgba(255,255,255,0.3)"
                             :fontWeight 500
                             :padding "12px 24px"
                             :borderRadius "8px"
                             :backdropFilter "blur(10px)"}
                     :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations")})}
        "Back to Workstations"]]]
     (when-let [description @(r/subscribe [:db/get-in [:workstation :description]])]
       [:div {:style {:margin-top "16px"
                      :padding-top "16px"
                      :border-top "1px solid rgba(255,255,255,0.2)"}}
        [:p {:style {:font-size "1rem"
                     :margin 0
                     :opacity 0.8
                     :font-weight 300
                     :font-style "italic"}}
         description]])]))

(defn get-workstation-batches [workstation-id]
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (workstations-request/get-workstation-batches
     workspace-id workstation-id
     (fn [batches]
       (when batches
         (r/dispatch [:db/assoc-in [:workstation :task-board-batches] batches]))))))

(defn update-batch-workflow-state! [batch-id new-state]
  (println "🚀 Updating batch" batch-id "to state" new-state)
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (workstations-request/update-batch-workflow-state
     workspace-id batch-id new-state
     (fn [response]
       (println "✅ Batch workflow state mutation response:" (clj->js response))
       (if (:error response)
         (println "❌ Error updating batch:" (:error response))
         (println "✅ Successfully updated batch" batch-id "to" new-state))
       ;; Refresh the batches data
       (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])]
         (when workstation-id 
           (println "🔄 Refreshing batches for workstation" workstation-id)
           (get-workstation-batches workstation-id))))))))

(defn handle-drag-end [result batches]
  (println "🎯 Drag ended with result:" (clj->js result))
  (let [source (:source result)
        destination (:destination result)]
    (println "🎯 Source:" (clj->js source))
    (println "🎯 Destination:" (clj->js destination))
    (if-not destination
      (println "🚫 No destination - drag cancelled")
      (if (= (:droppableId source) (:droppableId destination))
        (println "🔄 Same column drag - ignoring")
        (let [draggable-id (:draggableId result)
              source-state (:droppableId source)
              dest-state (:droppableId destination)]
          (println "🔄 Moving batch" draggable-id "from" source-state "to" dest-state)
          (println "🔍 Available batches:" (map :batch_id batches))
          (update-batch-workflow-state! draggable-id dest-state))))))

;; Small batch card components
(defn batch-drag-handle []
  [:span {:style {:cursor "grab" :margin-right "12px" :margin-left "4px" :color "#9ca3af" :font-size "1rem"}} 
   "⋮⋮"])

(defn batch-title [batch]
  [:div {:style {:font-weight "600"
                 :color "#1e293b"
                 :font-size "0.75rem"
                 :flex "1"
                 :white-space "nowrap"
                 :overflow "hidden"
                 :text-overflow "ellipsis"}}
   (:batch_name batch)])

(defn batch-title-row [batch]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :margin-bottom "12px"
                 :cursor "grab"}}
   [batch-drag-handle]
   [batch-title batch]])

(defn batch-part-image [batch]
  (when (:part_picture_url batch)
    [:img {:src (:part_picture_url batch)
           :alt (:part_name batch)
           :style {:width "40px"
                   :height "40px"
                   :border-radius "6px"
                   :border "1px solid #e2e8f0"
                   :margin-right "12px"
                   :object-fit "cover"}}]))

(defn batch-part-name [batch]
  [:div {:style {:font-size "0.85rem"
                 :color "#475569"
                 :font-weight "500"
                 :margin-bottom "2px"}}
   (:part_name batch)])

(defn batch-color-info [batch]
  (when (and (:color_name batch) (:color_basecolor batch))
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "6px"
                   :margin-bottom "4px"}}
     [:div {:style {:width "14px"
                    :height "14px"
                    :border-radius "50%"
                    :background (:color_basecolor batch)
                    :border "1px solid #d1d5db"
                    :flex-shrink "0"}}]
     [:span {:style {:font-size "0.8rem"
                     :color "#64748b"}}
      (:color_name batch)]]))

(defn batch-quantity [batch]
  [:div {:style {:font-size "0.7rem"
                 :color "#64748b"
                 :font-weight "500"}}
   (str "Qty: " (:quantity batch))])

(defn format-date-with-month-name 
   "Convert 2024-01-15T14:30:25.123Z to Jan 15, 2024 14:30"
  [date-string]
  (when date-string
    (let [months {1 "Jan" 2 "Feb" 3 "Mar" 4 "Apr" 5 "May" 6 "Jun"
                  7 "Jul" 8 "Aug" 9 "Sep" 10 "Oct" 11 "Nov" 12 "Dec"}
          year (subs date-string 0 4)
          month-num (js/parseInt (subs date-string 5 7))
          day (subs date-string 8 10)
          time (subs date-string 11 16)
          month-name (get months month-num)]
      (str month-name " " day ", " year " " time))))

(defn batch-creation-date [batch]
  (when (:created_at batch)
    [:div {:style {:font-size "0.7rem"
                   :color "#9ca3af"
                   :margin-top "4px"}}
     (format-date-with-month-name (:created_at batch))]))

(defn batch-details-row [batch]
  [:div {:style {:display "flex"
                 :align-items "flex-start"
                 :margin-bottom "12px"
                 :margin-left "4px"
                 :gap "0"}}
   [batch-part-image batch]
       [:div {:style {:flex "1"}}
     [batch-part-name batch]
     [batch-color-info batch]
     [batch-quantity batch]
     [batch-creation-date batch]]])

(defn parse-iso-date [date-string] 
  (when date-string
    (js/Date. date-string)))

(defn calculate-elapsed-time [start-time end-time]
 
    (let [start (parse-iso-date start-time)
          end (parse-iso-date end-time)]
      (/ (- (.getTime end) (.getTime start)) 1000)))

(defn calculate-current-duration [start-time]
 
  (when start-time
    (let [start (parse-iso-date start-time)
          now (js/Date.)]
      (/ (- (.getTime now) (.getTime start)) 1000))))

(defn format-duration [seconds]
  
  (when seconds
    (let [hours (js/Math.floor (/ seconds 3600))
          minutes (js/Math.floor (/ (mod seconds 3600) 60))
          secs (js/Math.floor (mod seconds 60))]
      (str (when (> hours 0) (str hours "h "))
           (when (> minutes 0) (str minutes "m "))
           secs "s"))))

(defn live-timer [start-time]
  (let [[current-time set-current-time] (zero-react/use-state (js/Date.now))]
    
    (zero-react/use-effect
     {:mount (fn []
               ;; Set up interval to update timer every second
               (let [interval (js/setInterval
                               (fn []
                                 (set-current-time (js/Date.now)))
                               1000)]
                 ;; Return cleanup function
                 (fn []
                   (js/clearInterval interval))))
      :params [start-time]})  ; Re-run effect if start-time changes
    
    (let [current-duration (calculate-current-duration start-time)]
      [:span {:style {:color "#f59e0b"
                      :font-weight "600"
                      :font-size "0.7rem"}}
       "⏱️ " (format-duration current-duration)])))

(defn timing-display [batch workflow-state] 
  (let [start-time (:start_time batch)
        finish-time (:finish_time batch)]
    (cond
      ;; Show live timer for "doing" state with start time
      (and (= workflow-state "doing") start-time)
      [live-timer start-time]
      
      ;; Show elapsed time for "done" state with both times
      (and (= workflow-state "done") start-time finish-time)
      (let [elapsed (calculate-elapsed-time start-time finish-time)]
        [:span {:style {:color "#10b981"
                        :font-weight "600"
                        :font-size "0.7rem"}}
         "✓ " (format-duration elapsed)])
      
      ;; Show "Ready" for to-do state
      (= workflow-state "to-do")
      [:span {:style {:color "#6366f1"
                      :font-weight "500"
                      :font-size "0.7rem"}}
       "📋 Ready"]
      
      ;; Fallback
      :else nil)))

(defn batch-process-row [batch workflow-state]
  [:div {:style {:font-size "0.75rem"
                 :color "#64748b"
                 :padding "8px 12px"
                 :margin "4px 0"
                 :background "#f8fafc"
                 :border-radius "4px"
                 :border "1px solid #e2e8f0"
                 :position "relative"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :justify-content "space-between"
                  :gap "8px"}}
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "8px"}}
     ;; Small colored indicator dot
     [:div {:style {:width "8px"
                    :height "8px"
                    :border-radius "50%"
                    :background (case workflow-state
                                  "to-do" "#6366f1"
                                  "doing" "#f59e0b"
                                  "done" "#10b981"
                                  "#64748b")
                    :flex-shrink "0"}}]
     [:span {:style {:font-weight "500"}}
      (:process_name batch)]]
    
    ;; Timing display
    [timing-display batch workflow-state]]
   
   (when (= (:batch_location batch) "previous")
     [:span {:style {:font-size "0.65rem"
                     :color "#10b981"
                     :font-weight "700"
                     :background "#d1fae5"
                     :padding "2px 6px"
                     :border-radius "3px"
                     :margin-left "16px"
                     :border "1px solid #10b981"}}
      "✓ COMPLETED"])])

(defn process-progress-bar [batch]
  (let [current-step (:current_step batch)
        process-name (:process_name batch)
        previous-process (when (> current-step 1)
                          (:previous_process_name batch))
        next-process (:next_process_name batch)]
    [:div {:style {:margin-top "12px"
                   :padding "8px 12px"
                   :background "#f8fafc"
                   :border-radius "6px"
                   :border "1px solid #e2e8f0"}}
     [:div {:style {:display "flex"
                    :justify-content "space-between"
                    :align-items "center"
                    :gap "8px"}}
      ;; Previous process
      [:div {:style {:flex "1"
                     :text-align "left"
                     :font-size "0.7rem"
                     :color "#94a3b8"
                     :opacity (if previous-process 0.7 0.3)
                     :white-space "nowrap"
                     :overflow "hidden"
                     :text-overflow "ellipsis"}}
       (or previous-process "—")]
      
      ;; Current process
      [:div {:style {:flex "1"
                     :text-align "center"
                     :font-size "0.75rem"
                     :font-weight "600"
                     :color "#1e293b"
                     :background "#e0f2fe"
                     :padding "4px 8px"
                     :border-radius "4px"
                     :border "1px solid #bae6fd"
                     :white-space "nowrap"
                     :overflow "hidden"
                     :text-overflow "ellipsis"}}
       process-name]
      
      ;; Next process
      [:div {:style {:flex "1"
                     :text-align "right"
                     :font-size "0.7rem"
                     :color "#94a3b8"
                     :opacity 0.7
                     :white-space "nowrap"
                     :overflow "hidden"
                     :text-overflow "ellipsis"}}
       (or next-process "—")]]]))

(defn get-column-styling [status is-dragging-over is-expanded]
  (let [base-style {:flex (if is-expanded "2" "1")
                    :border-radius "12px"
                    :padding "24px"
                    :margin "0 12px"
                    :min-height "500px"
                    :border "2px solid"
                    :transition "all 0.3s ease"
                    :position "relative"
                    :overflow "hidden"}]
    (case status
      "to-do" (merge base-style
                     {:background (if is-dragging-over 
                                    "linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%)"
                                    "linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%)")
                      :border-color (if is-dragging-over "#4f46e5" "#6366f1")
                      :box-shadow (if is-dragging-over
                                    "0 8px 24px rgba(99, 102, 241, 0.25)"
                                    "0 4px 12px rgba(99, 102, 241, 0.1)")})
      "doing" (merge base-style
                     {:background (if is-dragging-over
                                    "linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)"
                                    "linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%)")
                      :border-color (if is-dragging-over "#d97706" "#f59e0b")
                      :box-shadow (if is-dragging-over
                                    "0 8px 24px rgba(245, 158, 11, 0.25)"
                                    "0 4px 12px rgba(245, 158, 11, 0.1)")})
      "done" (merge base-style
                    {:background (if is-dragging-over
                                   "linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)"
                                   "linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%)")
                     :border-color (if is-dragging-over "#059669" "#10b981")
                     :box-shadow (if is-dragging-over
                                   "0 8px 24px rgba(16, 185, 129, 0.25)"
                                   "0 4px 12px rgba(16, 185, 129, 0.1)")})
      ;; Default fallback
      base-style)))

(defn get-column-header-styling [status]
  (case status
    "to-do" {:margin "0 0 20px 0"
             :color "#312e81"
             :font-size "1.2rem"
             :font-weight "700"
             :text-align "center"
             :padding "16px 12px"
             :border-radius "8px"
             :background "linear-gradient(135deg, #dbeafe 0%, #c7d2fe 100%)"
             :border "2px solid #6366f1"
             :box-shadow "0 2px 8px rgba(99, 102, 241, 0.15)"
             :position "relative"
             :overflow "hidden"
             :display "flex"
             :justify-content "space-between"
             :align-items "center"}
    "doing" {:margin "0 0 20px 0"
             :color "#92400e"
             :font-size "1.2rem"
             :font-weight "700"
             :text-align "center"
             :padding "16px 12px"
             :border-radius "8px"
             :background "linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)"
             :border "2px solid #f59e0b"
             :box-shadow "0 2px 8px rgba(245, 158, 11, 0.15)"
             :position "relative"
             :overflow "hidden"
             :display "flex"
             :justify-content "space-between"
             :align-items "center"}
    "done" {:margin "0 0 20px 0"
            :color "#065f46"
            :font-size "1.2rem"
            :font-weight "700"
            :text-align "center"
            :padding "16px 12px"
            :border-radius "8px"
            :background "linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)"
            :border "2px solid #10b981"
            :box-shadow "0 2px 8px rgba(16, 185, 129, 0.15)"
            :position "relative"
            :overflow "hidden"
            :display "flex"
            :justify-content "space-between"
            :align-items "center"}
    ;; Default
    {:margin "0 0 20px 0"
     :color "#374151"
     :font-size "1.2rem"
     :font-weight "600"
     :text-align "center"
     :padding-bottom "12px"
     :border-bottom "2px solid #e2e8f0"
     :display "flex"
     :justify-content "space-between"
     :align-items "center"}))

(defn confirm-batch! [batch-id]
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (workstations-request/confirm-batch
     workspace-id batch-id
     (fn [response]
       (if (:error response)
         (println "❌ Error confirming batch:" (:error response))
         (do
           (println "✅ Successfully confirmed batch" batch-id)
           ;; Refresh the batches data
           (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])]
             (when workstation-id 
               (get-workstation-batches workstation-id)))))))))

(defn batch-card-styling [snapshot batch workflow-state is-confirmed]
  (let [is-previous-workstation (= (:batch_location batch) "previous")
        is-dragging (.-isDragging snapshot)
        accent-color (case workflow-state
                       "to-do" "#6366f1"
                       "doing" "#f59e0b" 
                       "done" (if is-confirmed "#059669" "#10b981")
                       "#64748b")]
    {:background (if is-confirmed "#f0fdf4" "#ffffff")
     :border (cond
               is-previous-workstation "2px solid #10b981"
               is-dragging "2px solid #3b82f6"
               :else "2px solid #e2e8f0")
     :border-left (if is-previous-workstation
                    "2px solid #10b981"
                    (str "4px solid " accent-color))
     :border-radius "8px"
     :padding "16px"
     :margin-bottom "12px"
     :min-width "280px"
     :width "100%"
     :box-shadow (cond
                   is-dragging "0 12px 24px rgba(0,0,0,0.2)"
                   is-previous-workstation "0 4px 12px rgba(16, 185, 129, 0.15)"
                   is-confirmed "0 4px 12px rgba(5, 150, 105, 0.15)"
                   :else "0 2px 8px rgba(0,0,0,0.08)")
     :cursor "grab"
     :transition "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)"
     :transform (if is-dragging "rotate(2deg) scale(1.02)" "rotate(0deg) scale(1)")
     :opacity (if is-dragging 0.9 1)
     :position "relative"}))

;; Location Assignment Components
(defn location-info [{:keys [label value]}]
  [:div {:style {:flex "1"
                 :background "#f8fafc"
                 :padding "16px"
                 :border-radius "8px"
                 :border "1px solid #e2e8f0"}}
   [:div {:style {:font-size "0.75rem"
                  :color "#64748b"
                  :margin-bottom "8px"}}
    label]
   [:div {:style {:font-size "0.9rem"
                  :color "#1e293b"
                  :font-weight "500"}}
    (or value "Not assigned")]])

(defn location-assignment-section [batch]
  [:div {:style {:margin-top "24px"
                 :padding-top "24px"
                 :border-top "1px solid #e2e8f0"}}
   [:div {:style {:font-size "0.9rem"
                  :color "#475569"
                  :margin-bottom "16px"}}
    "Location Assignment"]
   [:div {:style {:display "flex"
                  :gap "12px"
                  :align-items "center"}}
    [location-info {:label "Current Location" :value (:location batch)}]
    [location-info {:label "Next Location" :value (:next_location batch)}]]])

;; Confirmation Button Component
(defn confirm-button [batch-id]
  [:div {:style {:margin-top "16px"
                 :display "flex"
                 :justify-content "center"}}
   [button/view {:mode :filled
                 :color "var(--seco-clr)"
                 :style {:fontWeight 500 
                         :padding "8px 20px"}
                 :on-click #(confirm-batch! batch-id)}
    "Confirm Completion"]])

;; Main Batch Card Component
(defn batch-card [batch index workflow-state]
  (let [expanded-column @(r/subscribe [:db/get-in [:workstation :expanded-column]])
        is-expanded (= expanded-column workflow-state)
        is-last-step (not (:next_process_name batch))
        is-confirmed (= (:status batch) "complete")]
    [:> Draggable {:key (:batch_id batch) 
                   :draggableId (:batch_id batch) 
                   :index index}
     (fn [provided snapshot]
       (reagent/as-element
        [:div (merge {:ref (.-innerRef provided)
                      :style (merge (js->clj (.. provided -draggableProps -style) :keywordize-keys true)
                                    (batch-card-styling snapshot batch workflow-state is-confirmed))
                      :class "batch-card"
                      :data-confirmed is-confirmed}
                     (js->clj (.-draggableProps provided) :keywordize-keys true)
                     (js->clj (.-dragHandleProps provided) :keywordize-keys true))
         [batch-title-row batch]
         [batch-details-row batch]
         [batch-process-row batch workflow-state]
         [process-progress-bar batch]
         (when (and (= workflow-state "done") is-last-step (not is-confirmed))
           [confirm-button (:batch_id batch)])
         (when (and is-expanded (not= workflow-state "doing"))
           [location-assignment-section batch])]))]))

(defn filter-batches-by-state [batches state]
  (cond
    (= state "done")
    ;; Show batches that are either:
    ;; 1. Actually in "done" state at current workstation
    ;; 2. Completed at previous workstation but still visible (batch_location = "previous")
    (filter (fn [batch]
              (or (and (= (:workflow_state batch) "done") (= (:batch_location batch) "current"))
                  (= (:batch_location batch) "previous")))
            batches)
    
    :else
    ;; For to-do and doing, only show current workstation batches
    (filter (fn [batch]
              (and (= (:workflow_state batch) state)
                   (= (:batch_location batch) "current")))
            batches)))

(defn task-column [title status batches]
  (let [filtered-batches (filter-batches-by-state batches status)
        [expanded-column set-expanded-column] (zero-react/use-state nil)
        is-expanded (= expanded-column status)]
    (r/dispatch [:db/assoc-in [:workstation :expanded-column] expanded-column])
    [:> Droppable {:droppableId status :type "BATCH"}
     (fn [provided snapshot]
       (reagent/as-element
        [:div (merge {:ref (.-innerRef provided)
                      :style (get-column-styling status (.-isDraggingOver snapshot) is-expanded)}
                     (js->clj (.-droppableProps provided) :keywordize-keys true))
         [:h3 {:style (get-column-header-styling status)}
          [:div {:style {:flex "1"
                         :text-align "center"}}
           (str title " (" (count filtered-batches) ")")]
          [:button {:on-click #(set-expanded-column (if is-expanded nil status))
                    :style {:background "none"
                            :border "none"
                            :cursor "pointer"
                            :padding "4px"
                            :display "flex"
                            :align-items "center"
                            :justify-content "center"
                            :color (case status
                                     "to-do" "#312e81"
                                     "doing" "#92400e"
                                     "done" "#065f46"
                                     "#374151")}}
           [:i {:class (if is-expanded "fas fa-compress" "fas fa-expand")
                :style {:font-size "16px"}}]]
          ;; Add top bar accent
          [:div {:style {:position "absolute"
                         :top "0"
                         :left "0"
                         :right "0"
                         :height "4px"
                         :background (case status
                                       "to-do" "linear-gradient(90deg, #6366f1, #4f46e5)"
                                       "doing" "linear-gradient(90deg, #f59e0b, #d97706)"
                                       "done" "linear-gradient(90deg, #10b981, #059669)"
                                       "#6b7280")
                         :border-radius "8px 8px 0 0"}}]]
         [:div {:style {:min-height "400px"}}
          (if (empty? filtered-batches)
            [:div {:style {:text-align "center"
                           :color (case status
                                    "to-do" "#6366f1"
                                    "doing" "#f59e0b"
                                    "done" "#10b981"
                                    "#64748b")
                           :padding "48px 16px"
                           :font-style "italic"
                           :font-weight "500"
                           :font-size "1.1rem"
                           :opacity "0.7"}}
             "No batches"]
            (doall
             (map-indexed (fn [index batch]
                            ^{:key (:batch_id batch)}
                            [batch-card batch index status])
                          filtered-batches)))]
         (.-placeholder provided)]))]))

(defn task-board-content []
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])
        batches @(r/subscribe [:db/get-in [:workstation :task-board-batches]])
        _ (when workstation-id (get-workstation-batches workstation-id))
        _ (js/console.log "Task board batches:" (clj->js batches))
        _ (js/console.log "Workstation ID:" workstation-id)]
    (if (nil? batches)
      [:div {:style {:padding "20px" :text-align "center"}}
       [:p "Loading batches..."]
       [:p (str "Workstation: " workstation-id)]]
      [:> DragDropContext {:onDragEnd #(handle-drag-end (js->clj % :keywordize-keys true) (or batches []))}
       [:div {:style {:display "flex"
                      :gap "0"
                      :margin-top "0"
                      :background "white"
                      :border-radius "12px"
                      :box-shadow "0 4px 16px rgba(0,0,0,0.06)"
                      :padding "32px"}}
        [task-column "To-do" "to-do" (or batches [])]
        [task-column "Doing" "doing" (or batches [])]
        [task-column "Done" "done" (or batches [])]]])))

(defn view []  
  [:div {:style {:max-width "1200px"
                 :margin "20px auto"
                 :padding "20px"}}
   [workstation-header]
   [task-board-content]])