
(ns features.customizer.panel.frontend.blocks.canvas.stage
  (:require
    ["react"              :as react]
    ["@react-three/drei"  :as r3d]
    ["@react-three/fiber" :as r3f]))

(def half-pi (/ js/Math.PI 2))



(defn showroom []
  (let [texture (r3d/useTexture "/img/showroom1.jpg")]
    [:> r3d/Sphere {"scale" [10 10 10]
                    "geometry" [20 32]}
      [:meshStandardMaterial {"map" texture
                              "side" 2}]]))

(defn environment []
  
    [:<> 
      [:> r3d/Environment {"frames"     js/Infinity
                           "preset"  "warehouse"}]])
                           ;apartment, city, dawn, forest, lobby, night, park, studio, sunset, warehouse}]])
                           ;"files" "/hdr/env.hdr"}]])
                           ;"resolution" 256}]]) 
                           ;"background" true}]])
                           ;"blur"       0.8}