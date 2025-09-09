(ns features.customizer.panel.frontend.blocks.canvas.placeholder
  (:require 
    [re-frame.core :as r]
    ["react"             :as react]
    ["three"             :as three]
    ["@react-three/drei" :as r3d]))

(defn- update-texture-map-props [^js texture-map {:keys [x y]}]
  (doto texture-map
    (set! -wrapS  three/RepeatWrapping)
    (set! -wrapT  three/RepeatWrapping)
    (set! -repeat #js{"x" 5 "y" 5})))

(defn- hydrate-texture! [{:keys [^js maps settings] :as texture-props}]
  (doseq [texture-map-key (.keys js/Object maps)]
    (update-texture-map-props (aget maps texture-map-key) (get settings :repeat))))

(defn material [{:keys [material] :as texture-props}]
  (let [texture-maps   (try (r3d/useTexture (clj->js (get texture-props :maps))) 
                            (catch js/Error e nil))
        texture-props  (assoc texture-props :maps texture-maps)]

    (react/useLayoutEffect
      (fn []
        (when texture-maps 
          (hydrate-texture! texture-props))
        (fn []))
      #js[texture-props])
    
    [:meshPhysicalMaterial 
      (merge {"attach" "material"}
             (js->clj (:maps texture-props))
             material)]))

(defn- placeholder [part-data]
  (let [scale (get part-data :scale 5)
        gltf  (try (r3d/useGLTF "/models/default_customizer_model.glb")
                   (catch js/Error _e nil))]
    [:> r3d/Resize {"scale" scale}
      [:> r3d/Center
        (if gltf
          (let [nodes (-> gltf .-scene .-children)]
            [:group {"rotation" [0 -2 0]}
              (doall
                (for [^js node nodes]
                  ^{:key (.-name node)}
                  [:mesh {"geometry"   (.-geometry node)
                          "rotation"   (.-rotation node)
                          "scale"      (.-scale node)
                          "position"   (.-position node)
                          "castShadow" true
                          "receiveShadow" true}
                    (if-let [texture @(r/subscribe [:db/get-in [:customizer :selected-look :texture]])]
                      ^{:key (:id texture)}[material texture]
                      [:meshPhysicalMaterial {:metalness 0.85
                                              :roughness 0.25
                                              :reflectivity 0.8
                                              :color "#C0C0C0"}])]))])
          [:mesh {"rotation" [0.1 0.1 0.1]}
            [:sphereGeometry {"args" [1 64 64]}]
            (if-let [texture @(r/subscribe [:db/get-in [:customizer :selected-look :texture]])]
              ^{:key (:id texture)}[material texture]
              [:meshPhysicalMaterial {:metalness 0.85
                                      :roughness 0.25
                                      :reflectivity 0.8
                                      :color "#C0C0C0"}])])]]))      
    
(defn view [part-data]
  [placeholder part-data])