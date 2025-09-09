(ns features.customizer.panel.frontend.blocks.looks.utils
  (:require [re-frame.core :as r]))

(defn get-active-index! [^js slider-props]
  (-> slider-props .-track .-details .-rel))

(defn move-to! [^js slider-props index]
  (.moveToIdx slider-props index true))

(defn next! [^js slider-props]
  (.next slider-props))

(defn prev! [^js slider-props]
  (.prev slider-props))

(defn update! [^js slider-props]
  (.update slider-props))

;; -----------------------------------------------------------------------------
;; ---- Keyboard controls ----

(defn prev-handler [^js slider-props]
  (let [{:keys [index count]} @(r/subscribe [:db/get-in [:customizer/looks]])
        prev-index   (if (zero? index) (dec count) (dec index))]

    (r/dispatch [:looks/select-by-index! prev-index])
    (move-to! slider-props index)))

(defn next-handler [^js slider-props]
  (let [{:keys [index count]} @(r/subscribe [:db/get-in [:customizer/looks]])
        next-index   (if (= index (dec count)) 0 (inc index))]
    (r/dispatch [:looks/select-by-index! next-index])
    (move-to! slider-props next-index)))
      

(defn- keyboard-handler [^js slider-props ^js event]
  ;; (.preventDefault ^js event)
  (case (.-keyCode event) 
    39 (next-handler slider-props)
    37 (prev-handler slider-props)
    nil))

(defn keyboard-controls [^js slider]
  (.on slider "created" 
    (fn [^js slider-props]
      (.addEventListener (.-container slider) "keydown"
                         (fn [event]
                           (keyboard-handler slider-props event))))))

;; ---- Keyboard controls ----
;; -----------------------------------------------------------------------------

(defn wheel-controls [^js slider]
  (let [touch-timeout (atom nil)
        position      (atom {:x 0 :y 0})
        wheel-active  (atom false)
        
        dispatch (fn [^js e name]
                   (swap! position update :x - (.-deltaX e))
                   (swap! position update :y - (.-deltaY e))
                   (-> slider .-container
                       (.dispatchEvent (js/CustomEvent. name
                                                        #js{:detail #js{:x (:x @position)
                                                                        :y (:y @position)}}))))
        wheel-start (fn [^js e] 
                      (reset! position {:x (.-pageX e)
                                        :y (.-pageY e)})
                      (dispatch e "ksDragStart"))
        
        wheel (fn [^js e]
                (dispatch e "ksDrag"))
                
        wheel-end (fn [^js e]
                    (dispatch e "ksDragEnd"))
        
        event-wheel (fn [^js e]
                      (.preventDefault ^js e)
                      
                      (when-not @wheel-active
                        (wheel-start e)
                        (reset! wheel-active true))
                      
                      (wheel e)
                      (js/clearTimeout @touch-timeout)
                      (reset! touch-timeout (js/setTimeout #(do
                                                              (reset! wheel-active false)
                                                              (wheel-end e))
                                                           100)))]
    
    (.on slider "created" #(let [container (.-container slider)]
                             (.addEventListener container "wheel" event-wheel #js{:passive false})))))