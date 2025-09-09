(ns ui.floater
  (:require
   ["framer-motion" :refer [motion]]
   ["react" :as react]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn vw->px [value] (-> (.-innerWidth js/window)
                         (*  value)
                         (/  100)))

(defn vh->px [value] (-> (.-innerHeight js/window)
                         (*  value)
                         (/  100)))

(defn- orientation->x [orientation]
  (case orientation
    :left  (vw->px 0)
    :right (vw->px 0)
    :top   (vh->px 0)
    :bottom   (vh->px 0)))

(defn- x-start [orientation]
  (case orientation
    :left  (vw->px -100)
    :right (vw->px -100)
    :top   (vh->px -100)
    :bottom   (vh->px -100)))

(defn- disable-pull-to-refresh! []
  (.setAttribute (.querySelector js/document "html") 
                 "style" 
                 "overscroll-behavior: none;overflow: hidden;"))

(defn- enable-pull-to-refresh! []
  (.setAttribute (.querySelector js/document "html")
                 "style"
                 ""))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn bgStyle [state]
  (if state
    {:opacity "0.5" :display "block"}
    {:opacity "0" :display "none"}))

(defn- background-cover [{:keys [state on-close config]}]
  (when-let [bg (:bg config {})]
    [:div {:on-click #(on-close)
           :class "floater-bg-cover"
           :style (merge (bgStyle state)
                        (:style bg))}]))

(defn floater [{:keys [state orientation class style config] 
                :or {state false
                     orientation "right"}
                :as props} content]
  
  (let [[mount set-mount] (react/useState false)]
    
    ;; Handle scroll lock
    (react/useEffect
     (fn []
       (if state
         (when (:scroll-lock config true) (disable-pull-to-refresh!))
         (when (:scroll-lock config true) (enable-pull-to-refresh!)))
       ;; Return cleanup function
       (fn []))
     #js [state])

    [:<>
      [background-cover props]  
      [:> (.-div motion)
         {:animate             {(keyword orientation) (if state "0px" "-100%")}
          :transition          {:type "spring" :stiffness 120 :damping 15}

          :initial    {(keyword orientation) "-100%"}
          :exit       {(keyword orientation) "-100%"}
          ;; :transition {:duration 0.2}

          :className           (str "floater " class)
          :data-state          state
          :data-orientation    orientation
          :style               (merge {(keyword orientation) "-100%"} style)
          :onAnimationComplete (fn [])}
                                 
        content]]))
      

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [props content]
  [floater props content])

