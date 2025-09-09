
(ns ui.popover
  (:require
   ["framer-motion" :refer [motion]]
   ["react" :as react]
   [re-frame.core :as r]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn get-bound-rect [element]
  (try 
    (.getBoundingClientRect element)
    (catch :default e
      (.log js/console e))))

(defn get-y [target-rect y]
  (case y
    :center (+ (.-top target-rect)
               (/ (.-height target-rect) 2))
               
    :bottom (+ (.-top target-rect)
               (.-height target-rect))
    (.-top target-rect)))
 
(defn get-x [target-rect x]
  (case x
    :center (+ (.-left target-rect)
               (/ (.-width target-rect ) 2))
               
    :right  (+ (.-left target-rect)
               (.-width target-rect))
    (.-left target-rect)))

(defn get-position [target-rect [y x]]
  [(+ (get-y target-rect y) (.-scrollY js/window))
   (+ (get-x target-rect x) (.-scrollX js/window))])

(defn anchor->translate [[y x]]
  (str
    (case x
      :center "-50%"
      :right  "-100%"
      "0%")
    " "
    (case y
      :center "-50%"
      :bottom "-100%"
      "0%")))

(defn update-props [target-rect props]
  ;; Check if popover displayed out screen
  (let [popover-element (.getElementById js/document (name (:id props)))
        out-bottom? (<= (+ (.-pageYOffset js/window) (.-innerHeight js/window))
                        (+ (.-top target-rect)
                           (.-scrollY js/window)
                           (.-offsetHeight popover-element)))
        out-top?    (>= (.-pageYOffset js/window) (- (.-top target-rect) (.-offsetHeight popover-element)))]

    (cond out-top?    (assoc props :align  [:bottom (last (:align props))]
                                   :anchor [:top (last (:anchor props))])
          out-bottom? (assoc props :align  [:top (last (:align props))]
                                   :anchor [:bottom (last (:anchor props))])
          
          :else props)))

(defn get-rect [target-rect {:keys [align anchor width style] :as _props}]
  (let [[top left] (get-position target-rect align)]
    {:top   top
     :left  left
     :width (if (= width :inherit)
               (.-width target-rect)
               (:width style))
     :translate (anchor->translate anchor)}))

(defn pos-check-and-get-rect [props]
  (try 
    (let [target-rect (get-bound-rect (:target props))] 
      (get-rect target-rect (update-props target-rect props)))
    (catch :default e
      (.log js/console e))))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Hooks ----

(defn on-mount [{:keys [id on-open on-close]}]
  (react/useEffect
   (fn []
     (when on-open (on-open))
     (fn []
       (if on-close (on-close)
           (r/dispatch [:popover/remove id]))))
   #js[]))

(defn use-click-away [on-click-away]
  (let [ref (react/useRef nil)]
    (react/useEffect
      (fn []
        (let [listener (fn [event]
                         (when (and ref.current
                                    (not (.contains ref.current (.-target event))))
                           (on-click-away event)))]
          
          (.addEventListener js/document "click" listener)
          (.addEventListener js/document "touchstart" listener)
          #(do
             (.removeEventListener js/document "click" listener)
             (.removeEventListener js/document "touchstart" listener))))
      #js [])
    ref))

(defn resize-hook [_mount [_ set-style] props]
  (react/useLayoutEffect
    (let [resize-listener (fn [_]
                            (set-style 
                              (get-rect (get-bound-rect (:target props)) props)))
          scroll-listener (fn [_]
                            (set-style (pos-check-and-get-rect props)))]
      (fn []
        (set-style (pos-check-and-get-rect props))
        (.addEventListener js/window "resize" resize-listener)
        (.addEventListener (:scroll-ref props) "scroll" scroll-listener)
        (fn []
          (.removeEventListener js/window "resize" resize-listener)
          (.removeEventListener (:scroll-ref props) "scroll" scroll-listener))))
   #js[props]))

;; ---- Hooks ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn popover [{:as props
                :keys [id state on-click-away]
                :or {state false}}
               content]
  
  (let [[mount set-mount] (react/useState false)
        [style _ :as style-state] (react/useState (:style props))
        click-away-ref (when on-click-away (use-click-away on-click-away))]
    
    (on-mount props)
    (when (and (:target props) (:scroll-ref props)) 
      (resize-hook mount style-state props))

    (when (or state mount)
      [:> (.-div motion)
       {
        :initial    {:y -10 :opacity 0}
        :animate    (when state {:y 0 :opacity 1})
        :exit       {:opacity 0}
        :transition {:duration 0.2}
        
        :onAnimationComplete (fn []
                               (if state
                                 (set-mount true)
                                 (do
                                   (r/dispatch [:popover/remove (:id props)])
                                   (set-mount false))))
        :className "popover"
        :id id
        :ref click-away-ref
        :data-state state
        :style (merge style (:style props))}
       content])))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [props content]
  [popover props content])