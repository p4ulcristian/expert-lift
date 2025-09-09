(ns features.customizer.panel.frontend.blocks.canvas.simple-model
  (:require 
    ["react"                       :as react]
    ["@react-three/drei"           :as r3d]))
    
(def REPEATWRAPPING 1000)

;; -----------------------------------------------------------------------------
;; ---- MODEL ----

(defn- update-texture-map-props [texture-map {:keys [x y]}] 
  (set! (.-wrapS texture-map)  REPEATWRAPPING)
  (set! (.-wrapT texture-map)  REPEATWRAPPING)
  (set! (.-repeat texture-map) #js{"x" x "y" y}))

(defn- hydrate-texture! [{:keys [maps settings] :as a}]
  (mapv (fn [texture-map-key]
          (update-texture-map-props (aget maps texture-map-key) (get settings :repeat))) 
       (.keys js/Object maps)))

(defn material [mesh-data {:keys [material-props] :as a}] 
  (let [maps (assoc a :maps (r3d/useTexture (clj->js (get a :maps))))]

    (react/useLayoutEffect
      (fn []
        (hydrate-texture! maps)
        (fn []))
      #js[maps])
    
    [:meshPhysicalMaterial
      (merge {"attach" "material"
              "side" 2}
             (js->clj (:maps maps))
             material-props)]))

(defn- default-material [mesh-data]
  [:meshPhysicalMaterial 
    {"attach"             "material"
     "map"                (-> mesh-data .-material .-map)
     "normalMap"          (-> mesh-data .-material .-normalMap)
     "normalScale"        (-> mesh-data .-material .-normalScale)
     "roughness"          (-> mesh-data .-material .-roughness)
     "metalness"          (-> mesh-data .-material .-metalness)
     "reflectivity"       (-> mesh-data .-material .-reflectivity)
     "color"              (-> mesh-data .-material .-color)
     "clearcoat"          1.0
     "clearcoatRoughness" 0
     "emissiveIntensity"  0.04
     "side" 2

     "emissive"           "rgba(0,0,0,1)"
     "transparent"        true}])

(defn- mesh [[mesh-id ^js mesh-data] & [{:keys [textures texture] :as config}]]
  (when (.-isMesh mesh-data)
    [:mesh {"geometry"      (.-geometry mesh-data)
            "rotation"      (.-rotation mesh-data)
            "scale"         (.-scale mesh-data)
            "material"      (.-material mesh-data)
            "position"      (.-position mesh-data)
            "castShadow"    true
            "recieveShadow" true}
         
      [default-material mesh-data]

      (map (fn [texture-map]
              ^{:key (random-uuid)}
              [material mesh-data texture-map])
          textures)]))
             
(defn model [{:keys [model color scale]
              :or   {scale 5}}]
  (let [model-data    (r3d/useGLTF (:src model))
        nodes         (js->clj (.-nodes model-data))]
    
    [:> r3d/Resize {"scale" scale}
      [:> r3d/Center
         [:> r3d/Bvh
          [:group {"rotation" (get model :rotation [0 -2 0])
                   "scale"    scale}
            (doall 
              (map (fn [[mesh-id _ :as data]]
                     ^{:key (str mesh-id)}
                     [mesh data 
                           {:textures (:texture color)
                            :rotation (:rotation model)}])
                   nodes))]]]]))

;; ---- MODEL ----
;; -----------------------------------------------------------------------------

(defn view [props]
  [model props]) 
 