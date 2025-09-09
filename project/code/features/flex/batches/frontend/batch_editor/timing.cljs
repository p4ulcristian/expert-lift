(ns features.flex.batches.frontend.batch-editor.timing
  (:require [zero.frontend.react :as zero-react]))

;; -----------------------------------------------------------------------------
;; ---- Timing Functions ----

(defn parse-iso-date [iso-string] 
  (when iso-string
    (js/Date. iso-string)))

(defn calculate-elapsed-time [start-time end-time] 
  (when (and start-time end-time)
    (- (.getTime (parse-iso-date end-time))
       (.getTime (parse-iso-date start-time)))))

(defn calculate-current-duration [start-time] 
  (when start-time
    (- (js/Date.now)
       (.getTime (parse-iso-date start-time)))))

(defn format-duration [duration-ms] 
  (when duration-ms
    (let [seconds (Math/floor (/ duration-ms 1000))
          minutes (Math/floor (/ seconds 60))
          hours (Math/floor (/ minutes 60))
          remaining-minutes (mod minutes 60)
          remaining-seconds (mod seconds 60)]
      (cond
        (>= hours 1) (str hours "h " remaining-minutes "m")
        (>= minutes 1) (str minutes "m " remaining-seconds "s")
        :else (str remaining-seconds "s")))))

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

(defn get-waiting-time [process batch batch-processes]
  (let [process-order (:step_order process)
        batch-processes-sorted (sort-by :step_order batch-processes)
        previous-step-process (first (filter #(= (:step_order %) (dec process-order)) batch-processes-sorted))
        batch-created-time (or (:created_at batch) (:created-at batch))]
    (cond
      ;; If there's a previous step and it's finished, calculate from its finish time
      (and previous-step-process (:finish_time previous-step-process))
      (calculate-current-duration (:finish_time previous-step-process))
      
      ;; If this is the first step or no previous step finished, calculate from batch creation
      batch-created-time
      (calculate-current-duration batch-created-time)
      
      ;; Fallback
      :else 0)))

(defn static-completed-time [start-time finish-time]
  (let [static-duration (calculate-elapsed-time start-time finish-time)]
    [:span {:class "timing-complete"
            :style {:font-weight "600"
                    :font-size "0.7rem"}}
     "✓ " (format-duration static-duration)]))

(defn live-waiting-timer [process batch batch-processes]
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
      :params [process batch]})  ; Re-run effect if process or batch changes
    
    (let [waiting-duration (get-waiting-time process batch batch-processes)]
      [:span {:style {:font-weight "600"
                      :font-size "0.7rem"}}
       "⏳ " (format-duration waiting-duration)])))

(defn timing-display [process batch]
  (let [process-order (:step_order process)
        current-step (or (:current_step batch) (:current-step batch) 1)
        start-time (:start_time process)
        finish-time (:finish_time process)
        batch-processes (:processes batch)
        workflow-state (or (:workflow_state batch) (:workflow-state batch) "to-do")]
    [:div {:class "process-timing"}
     (cond
       ;; Process is done - show static elapsed time
       (and finish-time start-time)
       [static-completed-time start-time finish-time]
       
       ;; Process is currently running - show live timer
       (and start-time (not finish-time) (= process-order current-step))
       [:div {:class "timing-active"}
        [live-timer start-time]]
       
       ;; Process is waiting to start but only show elapsed time if in "doing" state
       (and (not start-time) (= process-order current-step) (= workflow-state "doing"))
       [:div {:class "timing-ready"}
        [live-waiting-timer process batch batch-processes]]
       
       ;; Future process or to-do state
       :else nil)]))