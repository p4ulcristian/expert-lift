(ns features.customizer.test-3d.frontend.views
  (:require 
    [features.customizer.test-3d.frontend.blocks.canvas :as canvas]))

(defn main-view []
  [:div {:class "test-3d-container"
         :style {:width "100vw" 
                 :height "100vh"
                 :background "#1a1a1a"}}
   
   ;; Header
   [:div {:class "test-3d-header"
          :style {:position "absolute"
                  :top 0
                  :left 0
                  :right 0
                  :z-index 10
                  :background "rgba(255,255,255,0.1)"
                  :padding "1rem"
                  :backdrop-filter "blur(10px)"}}
    [:h1 {:style {:margin 0 :color "#fff"}} "3D Test Lab"]]
   
   ;; 3D Canvas
   [canvas/view]])

(defn view []
  [main-view])