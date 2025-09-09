(ns features.customizer.panel.frontend.blocks.canvas.controls
  (:require 
    ["@react-three/drei"  :as r3d]))

(defn camera-control []
  [:> r3d/OrbitControls 
    {"enableZoom"      true
     "autoRotateSpeed" 3.5
     "zoomSpeed"       0.8
     "rotateSpeed"     1
     "minDistance"     1
     "maxDistance"     10}])

                  
