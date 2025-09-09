(ns features.labs.looks.frontend.editor.model
  (:require
    ["react"             :as react]
    ["@react-three/drei" :as r3d]
    [re-frame.core :as r]))
    
;; -----------------------------------------------------------------------------
;; ---- MODEL ----

(def REPEATWRAPPING 1000)

(defn- hydrate-texture! [^js maps {:strs [x y]}]
  (doseq [texture-map-key (.keys js/Object maps)]
    (doto (aget maps texture-map-key)
      (set! -wrapS REPEATWRAPPING)
      (set! -wrapT  REPEATWRAPPING)
      (set! -repeat #js{"x" x "y" y}))))

(defn remove-nil-maps [texture-maps]
  (->> texture-maps
       (remove (fn [[_ v]] (nil? v)))
       (into {})
       clj->js))

(defn mesh-physical-material [{:strs [material maps settings] :as _texture-props}] 
  (let [texture-maps  (-> maps
                          remove-nil-maps
                          r3d/useTexture)]
                          
    (react/useLayoutEffect
      (fn []
        (hydrate-texture! texture-maps (get settings "repeat"))
        (fn []))
      #js[texture-maps])
    
    [:meshPhysicalMaterial
      (merge {"attach" "material"}
             (js->clj texture-maps)
             material)]))
     
(defn- mesh [[_mesh-id ^js mesh-data] & [{:keys [_rotation texture-props]}]]
  (when (.-isMesh mesh-data)
    [:mesh {"geometry"      (.-geometry mesh-data)
            "rotation"      (.-rotation mesh-data)
            "scale"         (.-scale mesh-data)
            "material"      (.-material mesh-data)
            "position"      (.-position mesh-data)
            "opacity"       0
            "castShadow"    true
            "recieveShadow" true}
      [mesh-physical-material texture-props]]))

(defn glb-model [texture-props model]
  (when-let [model-data    (try (r3d/useGLTF (:src model)) 
                                (catch js/Error _e nil))]
    
    (let [nodes (js->clj (.-nodes model-data))]
      [:<>
        (doall 
          (map (fn [data]
                 ^{:key (str (random-uuid))}
                 [mesh data {:texture-props texture-props
                             :rotation      (:rotation model)}])
              nodes))])))

(defn basic-shape [model-key texture-props]
  (case model-key
    "sphere"   [:> r3d/Sphere {"scale" [10 10 10] "args" [8 256 256 16]}
                   [mesh-physical-material texture-props]]
    "cube"     [:> r3d/Box {"scale" [10 10 10]}
                   [mesh-physical-material texture-props]]
    "cylinder" [:> r3d/Cylinder {"scale" [10 10 10]}
                   [mesh-physical-material texture-props]]
    "cone"     [:> r3d/Cone {"scale" [10 10 10] "args" [8 12 256 16]}
                   [mesh-physical-material texture-props]]
    "torus"    [:> r3d/TorusKnot {"scale" [1 1 1] "args" [8 3 256 64]}
                   [mesh-physical-material texture-props]]))

(defn view [{:keys [texture-props]}]
  (let [model-key @(r/subscribe [:db/get-in [:labs :model :value] "three_piece_rim"])]
  
    ^{:key model-key}
    [:> r3d/Resize {"scale" 5}
      [:> r3d/Center
        [:> r3d/Bvh
          [:group {"rotation" [0 -2 0]
                   "scale" 1}
            (if (= model-key "three_piece_rim") 
              [glb-model texture-props {:src "/labs/three_piece_rim.gltf"
                                        :rotation [(/ js/Math.PI 2) 0 0.8]}]
              [basic-shape model-key texture-props])]]]]))        
