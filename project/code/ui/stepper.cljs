(ns ui.stepper
  (:require 
    [re-frame.core :as r]
    [ui.button :as button]
    ["react" :as react]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn window-resize-hook []
  (let [[window-size set-window-size] (react/useState (.-innerWidth js/window))
        handle-resize (fn [e]
                       (set-window-size (.-innerWidth js/window)))]
    (react/useEffect
      (fn []
        (.addEventListener js/window "resize" handle-resize)
        (fn []
          (.removeEventListener js/window "resize" handle-resize)))
      #js[])
    window-size))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-db
 :step-next
 (fn [db [_]]
   (update-in db [:stepper] inc)))

(r/reg-event-fx
 :stepper/next!
 (fn [_ [_]]
   {:dispatch [:step-next]}))

(r/reg-event-db
 :step-back
 (fn [db [_]]
   (update-in db [:stepper] dec)))

(r/reg-event-fx
 :stepper/back!
 (fn [_ [_]]
   {:dispatch [:step-back]}))

(r/reg-event-fx
 :stepper/select!
 (fn [_ [_ step-id]]
   {:dispatch [:db/assoc-in [:stepper] step-id]}))

;; ---- Effects ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ----- Header -----

(defn done-icon-element [icon]
  [:i {:class (or icon "fa-solid fa-check")
       :style {:font-size "18px" :width "18px"}}])

(defn selected-icon-element [icon]
  [:i {:class (or icon "fa-solid fa-brush")
       :style {:font-size "18px" :width "18px"}}])

(defn step-label-icon [selected-icon done-icon selected? done? index]
  (cond
    selected? [selected-icon-element selected-icon]
    done?     [done-icon-element     done-icon]
    :else [:span {:style {:font-size "16px" :width "18px"}} (inc index)]))

(defn step-label [stepper-props index current-step [step-id {:keys [label-fn valid? selected-icon done-icon]}]]
  (let [selected? (= index current-step)]
    [button/view {:key        step-id
                  :class      "stepper--step-label"
                  :data-done  valid?
                  :color      (cond 
                                selected? (get-in stepper-props [:config :label :selected-color])
                                (not valid?) (get-in stepper-props [:config :label :disabled-color] "gray")
                                :else (get-in stepper-props [:config :label :color]))
                  :style      (get-in stepper-props [:config :label :style])
                  :disabled   (not valid?)
                  :on-click   #(r/dispatch [:stepper/select! index])}
      [step-label-icon selected-icon done-icon selected? valid? index]
      [:span (if (string? step-id)
               step-id
               (label-fn step-id))]]))

(defn step-labels [stepper-props current-step steps]
  [:<> 
    (map-indexed
      (fn [index [step-id step-data :as a-data]]
        [:<> {:key step-id}
          [step-label stepper-props index current-step a-data]
          [:div {:class     "stepper--line"
                 :style     {"--clr" (cond 
                                      ;;  (= index current-step) (get-in stepper-props [:config :label :selected-color])
                                       (not (:valid? step-data)) (get-in stepper-props [:config :label :disabled-color] "gray")
                                       :else (get-in stepper-props [:config :label :color]))}
                 :data-done (:valid? step-data false)}]])
     (butlast steps))])

(defn last-step-label [stepper-props current-step steps]
  (let [all-valid?                    (every? (fn [[_ {:keys [valid?]}]] (true? valid?)) (butlast steps))
        [last-step-id last-step-data] (last steps)
        last-step                     [last-step-id (assoc last-step-data :valid? (and (:valid? last-step-data) all-valid?))]]
    [step-label stepper-props (dec (count steps)) current-step last-step]))

(defn header [{:keys [index steps] :as stepper-props}]
  [:div {:id "stepper--header"}
    [step-labels stepper-props index steps]
    [last-step-label stepper-props index steps]])

;; ----- Header -----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ----- Body -----

(defn step [[_ {:keys [content]}]]
  [:div {:id "stepper--step-container"}
   content])

(defn body [{:keys [current-step]}]
  [step current-step])

;; ----- Body -----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ----- Footer -----

(defn back-button [{:keys [index current-step config]}]
  (when (< 0 index)
    [button/view (merge {:class    "stepper--back-button"
                         :on-click #(do
                                      (when-let [on-back (-> current-step last :on-back)] (on-back))
                                      (r/dispatch [:stepper/back!]))}
                        (get-in config [:back]))            
     "Back"]))

(defn scroll-container-top! []
  (.scroll (.getElementById js/document "stepper--container") 0 0))

(defn next-button [{:keys [config] 
                    [_ {:keys [valid? on-click next-label]}] :current-step} breakpoint]
  [button/view (merge {:on-click  #(do
                                     (when on-click (on-click))
                                     (r/dispatch [:stepper/next!])
                                     (when breakpoint (scroll-container-top!)))
                       :disabled (not valid?)
                       :class     "stepper--next-button"}
                      (get-in config [:next]))
   (or next-label "Next")])

(defn finish-button [{:keys [steps current-step config]}]
  (let [[_ {:keys [valid? on-click]}] current-step
        disable? (if-not (nil? valid?)
                   (not valid?)
                   (not (every? (fn [[_ {:keys [valid?]}]] (true? valid?)) (butlast steps))))]
    [button/view (merge {:disabled disable?
                         :class    "stepper--finish-button"
                         :on-click #(on-click)}
                        (get-in config [:finish]))
     (get config :finish-step-label "Finish")]))

(defn footer [{:keys [index steps] :as view-props} breakpoint]
  (let [last-step? (= (inc index) (count steps))]
    [:div {:id "stepper--footer"}
     [back-button view-props]
     (if last-step?
       [finish-button view-props]
       [next-button view-props breakpoint])]))

;; ----- Footer -----
;; -----------------------------------------------------------------------------

(defn stepper [config args]
  (let [window-size (window-resize-hook)
        step-index @(r/subscribe [:db/get-in [:stepper] (get config :initial-step 0)])
        steps      (partition 2 args)
        breakpoint (>= (:breakpoint config 600) window-size)
        view-props {:index        step-index
                    :steps        steps
                    :config       config
                    :current-step (nth steps step-index)
                    :on-finish    (:on-finish config)}]
    
    (react/useEffect
      (fn []
        (when-let [on-open (-> view-props :current-step second :on-open)]
          (on-open view-props))
        (fn []
          (when-let [on-leave (-> view-props :current-step second :on-leave)]
            (on-leave view-props))))
      #js[view-props])

    [:div {:id              "stepper--container"
           :style           (:container-style config)
           :data-breakpoint breakpoint}
      [header view-props]
      [body view-props]
      [footer view-props breakpoint]]))

(defn view [config & steps]
  (assert (even? (count steps))
          (str "\n\n COMPONENTS/STEPPER FAILED!\n steps must be even key value pair!
               \n Given steps: \n" steps "
               \n"))
  
  (react/useEffect
    (fn []
      (r/dispatch [:db/assoc-in [:stepper] (get config :initial-step 0)])
      (fn []))
    #js[])
  
  [stepper config steps])
