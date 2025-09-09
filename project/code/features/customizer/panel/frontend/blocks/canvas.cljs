
(ns features.customizer.panel.frontend.blocks.canvas
  (:require 
    [reagent.core :as reagent]
    [re-frame.core :as r]
    ["react"              :as react]
    ["@react-three/fiber" :as r3f]
    ["@react-three/drei" :as r3d]
    ["@react-three/postprocessing" :as pp :refer [Selection Select EffectComposer Outline]]
 
    [features.customizer.panel.frontend.blocks.canvas.model       :as model]
    [features.customizer.panel.frontend.blocks.canvas.stage       :as stage]
    [features.customizer.panel.frontend.blocks.canvas.controls    :as controls]
    [features.customizer.panel.frontend.blocks.canvas.placeholder :as placeholder]
    [features.customizer.panel.frontend.blocks.canvas.loader      :as loader]))


(def tutorial-flag
  [:div {:id    "customizer--tutorial-flag--model"
         :style {:position  "fixed"
                 :top       "50%"
                 :left      "50%"
                 :transform "translate(-50%,-50%)"
                 :width     "47vw" 
                 :height    "75vh"
                 :margin    "auto"}}])

(defn render [edited-item]
  (let [selected-item @(r/subscribe [:db/get-in [:customizer/menu :selected]])
        selected-part-id (:id selected-item)
        ;; Get selection counter from re-frame to detect re-selections
        selection-counter @(r/subscribe [:db/get-in [:customizer :selection-counter] 0])
        ;; Get hover counter from re-frame to detect hover events
        hover-counter @(r/subscribe [:db/get-in [:customizer :hover-counter] 0])
        canvas-key (str "canvas-" (get edited-item :model_url))
        ;; Animated outline strength that fades out over 2 seconds after selection
        [outline-strength set-outline-strength] (react/useState 0)]
    
    ;; Animate outline strength when a part is selected
    (react/useEffect
      (fn []
        (if selected-part-id
          ;; Start at full strength, then fade out over 2 seconds
          (do
            (set-outline-strength 6.0)
            (let [start-time (js/performance.now)
                  animation-id (atom nil)
                  animate (fn animate [current-time]
                            (let [elapsed (- current-time start-time)
                                  progress (min (/ elapsed 2000) 1)
                                  eased-progress (* progress progress (- 3 (* 2 progress)))
                                  current-strength (* 6.0 (- 1 eased-progress))]
                              (set-outline-strength current-strength)
                              (when (< progress 1)
                                (reset! animation-id (js/requestAnimationFrame animate)))))]
              (reset! animation-id (js/requestAnimationFrame animate))
              ;; Return cleanup function to cancel animation
              (fn []
                (when @animation-id
                  (js/cancelAnimationFrame @animation-id)))))
          ;; Reset to 0 when no part is selected  
          (set-outline-strength 0))
        ;; Cleanup function
        (fn []))
      #js[selected-part-id selection-counter hover-counter])
    [:div {:class "canvasContainer" :style {:position "fixed" :inset 0}}
      tutorial-flag
      

      [:> r3f/Canvas {"key"         canvas-key
                      "shadows"     true 
                      "performance" {"min" 0.1
                                     "max" 1
                                     "regress" (fn [])}
                      "gl"          {"preserveDrawingBuffer" true
                                     "powerPreference" "high-performance"}
                      "dpr"         [1 2]
                      "frameloop"   "always"}
      ;; [:> r3d/AdaptiveEvents]
      ;; [:> r3d/AdaptiveDpr {"pixelated" true}]
      ;; [:> r3d/Stats {"showPanel" 0}]
      ;; [:> r3d/StatsGl {"showPanel" 0}]
      [:> r3d/Sphere {"scale" [13 13 13]}
        [:meshBasicMaterial {"side" 2 "color" "#cfd2db"}]]

      [controls/camera-control]
     
      [:> react/Suspense {:fallback nil} 
        [stage/environment]]

      [:> Selection
       [:> EffectComposer {"multisampling" 8
                           "autoClear" false
                           "stencilBuffer" false  
                           "depthBuffer" true}
        ;; Dynamic outline: cyan for selection (fading), green for hover
        [:> Outline {"blur" true               
                     "visibleEdgeColor" (if selected-part-id "#00ffff" "#00ff00")
                     "hiddenEdgeColor" (if selected-part-id "#00cccc" "#00cc00")
                     "edgeStrength" (if selected-part-id outline-strength 4.0)
                     "pulseSpeed" 0.0
                     "width" 1024
                     "height" 1024
                     "kernelSize" 2
                     "xRay" true}]]
       
       [:> react/Suspense {:key      (get edited-item :model_url)
                           :fallback (reagent/as-element [loader/view])}

         (if (get edited-item :model_url)
           [model/view edited-item selected-part-id]
           [placeholder/view edited-item])]]]]))
      
(defn view [props]
  [render props])
