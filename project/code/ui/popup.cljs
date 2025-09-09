
(ns ui.popup
  (:require
   ["framer-motion" :refer [motion]]
   ["react" :as react]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn vh->px [value] (-> (.-innerHeight js/window)
                         (*  value)
                         (/  100)))

(defn vw->px [value] (-> (.-innerWidth js/window)
                         (*  value)
                         (/  100)
                         (str "px")))

(defn- disable-pull-to-refresh! []
  (let [scroll-bar-width (- (.-innerWidth js/window)
                            (-> js/document .-documentElement .-clientWidth))]
    (.setAttribute (.querySelector js/document "html")
                   "style"
                   (str "overscroll-behavior: none;overflow: hidden;margin-right:" scroll-bar-width"px;"))))

(defn- enable-pull-to-refresh! []
  (.setAttribute (.querySelector js/document "html")
                 "style"
                 ""))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn popup [{:as props
              :keys [state on-close on-open] 
              :or {state false}}
             content]
  (let [[mount set-mount] (react/useState false)]
    
    ;; Handle scroll lock and callbacks
    (react/useEffect
     (fn []
       (if state
         (do
           (when (:scroll-lock props true) (disable-pull-to-refresh!))
           (when on-open (on-open)))
         (when (:scroll-lock props true) (enable-pull-to-refresh!)))
       ;; Cleanup function
       (fn []
         (enable-pull-to-refresh!)))
     #js [state])

    (when (or state mount)
      [:> (.-div motion)
       {:initial {:opacity 0}
        :animate {:opacity (if state 1 0)}
        :onAnimationComplete (fn []
                              (if state
                                (set-mount true)
                                (set-mount false)))
        :style (merge {:position "fixed"
                       :top   0
                       :left  0
                       :width "100%"
                       :height "100%"
                       :z-index 1}
                      (:container-style props {}))}
                
       
       [:<>
         (when-let [cover (:cover props {})]
           [:div {:on-click #(on-close)
                  :class "popup-cover"
                  :style cover}])
         [:div {:class "popup"
                :data-state state
                :data-position (:position props :center)
                :style (:style props)}
           content]]])))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [props content]
  [popup props content])