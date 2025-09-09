(ns features.flex.shared.frontend.components.info-tooltip
  (:require ["react" :as react]))

;; -----------------------------------------------------------------------------
;; ---- Helper Functions ----

(defn calculate-bubble-position
  "Calculate bubble position next to trigger element"
  [trigger-rect]
  (let [bubble-width 280
        bubble-height 80
        spacing 12
        arrow-size 6]
    {:left (+ (.-right trigger-rect) spacing)
     :top (+ (.-top trigger-rect) 
             (/ (- (.-height trigger-rect) bubble-height) 2))
     :arrow-left (- spacing arrow-size)
     :arrow-top (/ bubble-height 2)}))

(defn create-hover-handlers
  "Create mouse enter/leave handlers for tooltip"
  [show-fn hide-fn hover-delay]
  (let [[hover-timeout set-hover-timeout] (react/useState nil)]
    {:show-tooltip (fn []
                    (when hover-timeout
                      (js/clearTimeout hover-timeout)
                      (set-hover-timeout nil))
                    (show-fn))
     
     :hide-tooltip (fn []
                    (let [timeout (js/setTimeout hide-fn hover-delay)]
                      (set-hover-timeout timeout)))
     
     :cancel-hide (fn []
                   (when hover-timeout
                     (js/clearTimeout hover-timeout)
                     (set-hover-timeout nil)))
     
     :cleanup (fn []
               (when hover-timeout
                 (js/clearTimeout hover-timeout)))}))

;; ---- Helper Functions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- UI Components ----

(defn bubble-arrow
  "Small arrow pointing to trigger element"
  [arrow-styles]
  [:div {:style (merge {:position "absolute"
                        :width 0
                        :height 0
                        :border-style "solid"
                        :border-width "6px 6px 6px 0"
                        :border-color "transparent #ffffff transparent transparent"
                        :filter "drop-shadow(-1px 0px 1px rgba(0,0,0,0.05))"}
                       arrow-styles)}])

(defn bubble-content
  "Info bubble with subtle styling"
  [content position-style arrow-position on-mouse-enter on-mouse-leave]
  [:div {:style (merge {:position "fixed"
                        :z-index 1000
                        :background "#ffffff"
                        :border "1px solid #e2e8f0"
                        :border-radius "6px"
                        :padding "12px 16px"
                        :box-shadow "0 2px 8px rgba(0,0,0,0.08)"
                        :max-width "280px"
                        :font-size "13px"
                        :line-height "1.4"
                        :color "#475569"
                        :pointer-events "auto"}
                       position-style)
         :on-mouse-enter on-mouse-enter
         :on-mouse-leave on-mouse-leave}
   [bubble-arrow {:left (:arrow-left arrow-position)
                  :top (:arrow-top arrow-position)}]
   [:div {:style {:margin 0}} content]])

(defn trigger-wrapper
  "Wrapper for trigger element with hover handlers"
  [trigger-element trigger-ref on-mouse-enter on-mouse-leave]
  [:div {:ref trigger-ref
         :style {:display "inline-flex"
                 :align-items "center"}
         :on-mouse-enter on-mouse-enter
         :on-mouse-leave on-mouse-leave}
   trigger-element])

;; ---- UI Components ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Main Component ----

(defn view
  "Subtle info tooltip that appears as a bubble next to trigger element"
  [{:keys [content hover-delay]
    :or {hover-delay 150}}
   trigger-element]
  (let [[visible? set-visible] (react/useState false)
        [position-style set-position-style] (react/useState {})
        [arrow-position set-arrow-position] (react/useState {})
        trigger-ref (react/useRef nil)
        
        show-tooltip (fn []
                      (when-let [trigger-elem (.-current trigger-ref)]
                        (let [rect (.getBoundingClientRect trigger-elem)
                              pos-data (calculate-bubble-position rect)]
                          (set-position-style (select-keys pos-data [:left :top]))
                          (set-arrow-position (select-keys pos-data [:arrow-left :arrow-top]))
                          (set-visible true))))
        
        hide-tooltip #(set-visible false)
        
        handlers (create-hover-handlers show-tooltip hide-tooltip hover-delay)]

    ;; Cleanup on unmount
    (react/useEffect
     (fn [] (:cleanup handlers))
     #js [])

    [:<>
     [trigger-wrapper 
      trigger-element 
      trigger-ref 
      (:show-tooltip handlers)
      (:hide-tooltip handlers)]
     
     (when visible?
       [bubble-content 
        content 
        position-style 
        arrow-position
        (:cancel-hide handlers)
        (:hide-tooltip handlers)])]))



;; ---- Main Component ----
;; -----------------------------------------------------------------------------