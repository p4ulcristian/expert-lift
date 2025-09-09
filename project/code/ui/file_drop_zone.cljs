
(ns ui.file-drop-zone
  (:require
    ["react" :as react]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn event->data-id [e]
  (let [target (.-target ^js e)]
    (try (.getAttribute ^js target "data-id")
         (catch :default _e))))

(defn drag-over-f [e]
  (.preventDefault ^js e)
  ;; Set cursor style
  (set! (-> ^js e .-dataTransfer .-dropEffect) "copy")
  (event->data-id e))

(defn on-drag-leave [e]
  ;; Ensure it's leaving the window
  (when (= (.-target ^js e) js/document)
    (.log js/console "drage leave")))

(defn drop-f [e]
  (.preventDefault ^js e)
 
  ;; Handle dropped files
  (.log js/console (.getAttribute ^js (.-target ^js e) "data-id"))
  (array-seq (.-files (.-dataTransfer ^js e))))

(defn cursor-inside-element? [^js e ^js el]
  (let [rect (.getBoundingClientRect el)
        x (.-clientX e)
        y (.-clientY e)]
    (and (>= x (.-left rect))
         (<= x (.-right rect))
         (>= y (.-top rect))
         (<= y (.-bottom rect)))))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Hooks ----

(defn drop-zone-ref [[state set-state] props]
  (let [ref (react/useRef nil)]
    (react/useLayoutEffect
      (let [drop-listener      (fn [e]
                                 (let [files   (drop-f e)
                                       data-id (event->data-id e)]
                                   (when-let [on-drop (:on-drop props)]
                                     (on-drop files data-id))
                                   (set-state {})))
            
            drag-over-listener (fn [e]
                                 (let [target (drag-over-f e)]
                                   (set-state {:data-id target})
                                   (when-let [on-drop-over (:on-drag-over props)]
                                     (on-drop-over e state))))
            
            drag-leave-listener (fn [e]
                                  
                                  (set-state {})
                                  (when-let [on-drag-leave (:on-drag-leave props)]
                                    (on-drag-leave e)))]

         (fn []
           (when (.-current ref)
             (.addEventListener ^js (.-parentElement (.-current ^js ref)) "drop" drop-listener)
             (.addEventListener ^js (.-parentElement (.-current ^js ref)) "dragover" drag-over-listener)
             (.addEventListener ^js (.-parentElement (.-current ^js ref)) "dragleave" drag-leave-listener))
           (fn []
             (.removeEventListener ^js (.-parentElement (.-current ^js ref)) "drop" drop-listener)
             (.removeEventListener ^js (.-parentElement (.-current ^js ref)) "dragover" drag-over-listener)
             (.removeEventListener ^js (.-parentElement (.-current ^js ref)) "dragleave" drag-leave-listener))))
      #js[])
    ref))  

;; ---- Hooks ----
;; -----------------------------------------------------------------------------

(defn drop-zone [{:keys [content-f] :as props}]
  (let [[state _ :as _state] (react/useState {})]
    [:div {:id    "dropzone"
           :ref   (drop-zone-ref _state props)
           :style {:position       "absolute"
                   :inset          "0"
                   :background     (when-not (empty? state) "rgb(93 167 217 / 10%)")
                   :border         (when-not (empty? state) "3px dashed var(--prim-clr)")
                   :pointer-events "none"
                   :z-index        20
                   :display        "grid"
                   :place-items    "end center"}}
      (when-not (empty? state) [content-f state])]))

(defn view [props _content]
  [drop-zone props])  
   


  