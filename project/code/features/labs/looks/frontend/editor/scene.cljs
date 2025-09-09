(ns features.labs.looks.frontend.editor.scene
  (:require
   ["@react-three/drei"  :as r3d]
   ["@react-three/fiber" :as r3f]
   ["react" :as react]))

(def half-pi (/ js/Math.PI 2))

(defn- Lightformers []
  (let [positions  [2 0 2 0 2 0]
        group-ref  (react/useRef nil)]
    (r3f/useFrame (fn [_state delta]
                    (let [g-pos (-> group-ref .-current .-position)
                          z     (.-z g-pos)]
                      (set! (.-z g-pos) (+ z (* delta 1)))
                      (when (> (+ z (* delta 1)) 5)
                          (set! (.-z g-pos) -10)))))
    [:<> 
      [:> r3d/Lightformer {"intensity"  0.75
                           "rotation-x" half-pi
                           "position"   [0 5 -9]
                           "scale"      [10 10 1]}]
      [:group {"rotation" [0 0.5 0]}
        [:group {"ref" group-ref}
          (doall
            (map-indexed (fn [x i]
                           ^{:key x}
                           [:> r3d/Lightformer {"form"      "circle"
                                                "intensity" 0.5
                                                "rotation"  [half-pi 0 0]
                                                "position"  [x 4 (* i 4)]
                                                "scale"     [3 1 1]}])
                         positions))]]
                
      [:> r3d/Float {"speed"             5
                     "floatIntensity"    2
                     "rotationIntensity" 2}
        [:> r3d/Lightformer {"form"      "ring"
                             "color"     "white"
                             "intensity" 0.7
                             "scale"     10
                             "position"  [-15 4 -18]
                             "target"    [0 0 0]}]]]))

      ;; [:mesh {"scale" 100}
      ;;     [:sphereGeometry {"args" [1 64 64]}]
      ;;     [:> lamina/LayerMaterial {"side" 1}
      ;;       [:> lamina/Color {"color" "#444"
      ;;                         "alpha" 1
      ;;                         "mode"  "normal"}]
      ;;       [:> lamina/Depth {"colorA" "#777"
      ;;                         "colorB" "black"
      ;;                         "alpha"  0.5
      ;;                         "mode"   "normal"
      ;;                         "near"   0
      ;;                         "far"    300
      ;;                         "origin" [100 100 100]}]]]]))


(defn environment []
  [:<> 
    [:ambientLight     {"intensity" 0.6}]
    [:> r3d/Environment {"files"  "/labs/env.hdr"
                         "frames" js/Infinity}
      [Lightformers]]])

(defn camera-control []
  (let [ref (react/useRef nil)]
    [:<>
      [:> r3d/OrbitControls 
         {"enableZoom"  true
          "onChange" (fn [e]
                         (.set (.-position (.-current ref))
                               0 0 1)
                         (.add (.-position (.-current ref))
                               (-> e .-target .-object .-position)))

          "zoomSpeed"    0.8
          "rotateSpeed" 1}]
          ;; "minDistance" 1
          ;; "maxDistance" 10}]
  
      [:spotLight {"ref"       ref
                   "intensity" 3
                   "penumbra"  1
                   "power"     5
                   "color"     "white"
                   "angle"     1.5
                   "shadow"    true}]]))