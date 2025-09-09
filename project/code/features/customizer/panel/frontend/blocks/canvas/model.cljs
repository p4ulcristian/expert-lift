(ns features.customizer.panel.frontend.blocks.canvas.model
  (:require 
    [re-frame.core        :as r]
    [reagent.core         :as reagent]
    ["react"              :as react]
    ["three"              :as three]
    ["@react-three/drei"  :as r3d]
    ["@react-three/postprocessing" :refer [Select]]
    [ui.button :as button]))

(defn get-part-by-id [parts id]
  (first (filter #(= (:id %) id) parts)))


;; -----------------------------------------------------------------------------
;; ---- FAILED TO LOAD MODEL ----

(defn- failed-to-load-model [{:keys [model scale]
                              :or   {scale 5} :as package-data}]
  [:> r3d/Resize {"scale" scale}
    [:> r3d/Center
      [:group {"rotation" [0 0 0]
               "scale"    scale}
       ;; Error card using Html component
        [:> r3d/Html {"position" [0 0 0]
                      "center" true
                      "distanceFactor" 7}
          [:div {:style {:padding       "36px"
                         :border-radius "18px"
                         :text-align    "center"
                         :min-width     "420px"
                         :box-shadow    "0 12px 48px rgba(0,0,0,0.3)"
                         :border        "1px solid rgba(255,255,255,0.2)"
                         :background    "linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%)"}}
           ;; Error icon
            [:div {:style {:font-size     "72px"
                           :margin-bottom "24px"}}
              "⚠️"]
           ;; Error title
            [:h2 {:style {:font-weight "600"}}
              "Model Loading Failed"]
           ;; Error description
            [:p {:style {:margin      "0 0 30px 0"
                         :font-size   "21px"
                         :opacity     "0.9"
                         :line-height "1.4"}}
             "The customizer couldn't load the 3D model. This might be due to a network issue or an invalid file."]
           ;; Refresh button
            [button/view {:color    "#5D6D7E"
                          :on-click #(r/dispatch [:customizer.model/refresh-model])
                          :style    {:padding       "15px 30px"
                                     :font-size     "21px"
                                     :color         "#fff"
                                     :border-radius "12px"
                                     :font-weight   "600"}}
              "🔄 Try Again"]]]]]])

;; ---- FAILED TO LOAD MODEL ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- MODEL ----

(defn- update-texture-map [^js texture-map {:keys [x y]}]
  (doto texture-map
    (set! -wrapS  three/RepeatWrapping)
    (set! -wrapT  three/RepeatWrapping)
    (set! -repeat #js{"x" x "y" y})))

(defn- hydrate-texture! [{:keys [^js maps settings]}]
  (doseq [texture-map-key (.keys js/Object maps)]
    (update-texture-map (aget maps texture-map-key) (get settings :repeat)))) 
       
(defn remove-nil-maps [texture-maps]
  (if (and texture-maps (or (map? texture-maps) (coll? texture-maps)))
    (->> texture-maps
         (remove (fn [[_ v]] (or (nil? v) 
                                 (= v "")
                                 (= v "null")
                                 (not (string? v)))))
         (into {})
         clj->js)
    #js{}))

(defn- textured-material [mesh-data {:keys [material] :as a}]
  "Component that loads textures - will suspend until loaded"
  (let [maps (assoc a :maps (r3d/useTexture (remove-nil-maps (get a :maps))))]
    
    (react/useLayoutEffect
      (fn []
        (hydrate-texture! maps)
        (fn []))
      #js[(:maps maps)])
    
    [:meshPhysicalMaterial
      (merge {"attach" "material"}
             ;; Texture Maps
             (js->clj (:maps maps))
             material)]))

(defn- color-fallback-material [{:keys [material]} basecolor]
  "Fallback material using the look's base color while textures load"
  [:meshPhysicalMaterial
    (merge {"attach" "material"
            "color" (or basecolor "#C0C0C0")
            "roughness" 0.25
            "metalness" 0.85}
           material)])

(defn material [mesh-data {:keys [material basecolor] :as a}]
  "Smart material component that shows color immediately, then loads textures"
  (let [texture-maps (get a :maps)
        cleaned-maps (when (and texture-maps 
                                (or (map? texture-maps) (coll? texture-maps)))
                       (try 
                         (remove-nil-maps texture-maps)
                         (catch js/Error _e #js{})))
        has-textures (and cleaned-maps 
                          (> (.-length (.keys js/Object cleaned-maps)) 0))]
    
    
    (if has-textures
      ;; Has textures - show color fallback while loading
      [:> react/Suspense 
        {:fallback (reagent/as-element [color-fallback-material a basecolor])}
        [textured-material mesh-data a]]
      
      ;; No textures - show material with base color
      [color-fallback-material a basecolor])))

(defn- default-material [^js mesh-data]
  [:meshPhysicalMaterial 
    {"attach"       "material"
     "roughness"    0.25
     "metalness"    0.85
     "reflectivity" 0.8
     "color"        "#C0C0C0"}])

(defn- mesh [mesh-id ^js mesh-data & [{:keys [parts mesh->part selected-part-id]}]]
  (let [part-id (get mesh->part mesh-id)
        is-selected (= part-id selected-part-id)
        [hovered set-hovered] (react/useState false)
        on-pointer-over (react/useCallback #(do (set-hovered true)
                                                 (r/dispatch [:db/update-in [:customizer :hover-counter] inc])) #js [])
        on-pointer-out (react/useCallback #(set-hovered false) #js [])]
    [:> Select {"enabled" (or hovered is-selected)}
     [:mesh {"geometry"      (.-geometry mesh-data)
             "rotation"      (.-rotation mesh-data)
             "scale"         (.-scale mesh-data)
             "material"      (.-material mesh-data)
             "position"      (.-position mesh-data)
             "castShadow"    true
             "recieveShadow" true
             "onPointerOver" on-pointer-over
             "onPointerOut"  on-pointer-out
             "onClick"        (fn [e]
                                  (.stopPropagation ^js e)
                                  (r/dispatch [:customizer.model/select-part! part-id])
                                  (r/dispatch [:db/update-in [:customizer :selection-counter] inc]))
             "dispose"       nil}

       (if-let [texture-data (get-in parts [part-id :look :texture])]
         [material mesh-data {:maps texture-data 
                              :material (:material texture-data)
                              :basecolor (get-in parts [part-id :look :basecolor])}]
         [default-material mesh-data])]]))

(defn- group-mesh [mesh-id ^js mesh-data & [texture-map selected-part-id]]
  (let [is-selected (= mesh-id selected-part-id)
        [hovered set-hovered] (react/useState false)
        on-pointer-over (react/useCallback #(do (set-hovered true)
                                                 (r/dispatch [:db/update-in [:customizer :hover-counter] inc])) #js [])
        on-pointer-out (react/useCallback #(set-hovered false) #js [])]
    [:> Select {"enabled" (or hovered is-selected)}
     [:mesh {"geometry"      (.-geometry mesh-data)
             "rotation"      (.-rotation mesh-data)
             "scale"         (.-scale mesh-data)
             "material"      (.-material mesh-data)
             "position"      (.-position mesh-data)
             "castShadow"    true
             "recieveShadow" true
             "onPointerOver" on-pointer-over
             "onPointerOut"  on-pointer-out
             "dispose"       nil}

       (if texture-map
         [material mesh-data {:maps texture-map}]
         [default-material mesh-data])]]))

(defn- group [mesh-id ^js node-data {:keys [parts mesh->part selected-part-id] :as config}]
  (let [part-id     (get mesh->part mesh-id)
        texture-map (get-in parts [part-id :look :texture])]
    
    [:group {"rotation"      (.-rotation node-data)
             "scale"         (.-scale node-data)
             "position"      (.-position node-data)
             "onClick"       (fn [e]
                                 (.stopPropagation ^js e)
                                 (when part-id
                                   (r/dispatch [:customizer.model/select-part! part-id])
                                   (r/dispatch [:db/update-in [:customizer :selection-counter] inc])))}
      (for [data (.-children node-data)]
        ^{:key (.-id data)}
        [group-mesh (.-id data) data texture-map selected-part-id])]))

(defn- node-handler [^js node-data config]
  (cond
    (.-isMesh node-data)  [mesh  (.-name node-data) node-data config]
    (.-isGroup node-data) [group (.-name node-data) node-data config]
    :else nil))

(defn model [{:keys [model_url scale package_id]
              :or   {scale 5} :as edited-item} selected-part-id]

  (let [^js gltf  (try (r3d/useGLTF model_url)
                       (catch js/Error e (println e) false))
        ^js nodes (try (-> gltf .-scene .-children)
                       (catch js/Error e (println e) false))]

    (if (false? gltf)
      [failed-to-load-model edited-item]
      (let [package-id @(r/subscribe [:db/get-in [:customizer :package-id]])
            parts      @(r/subscribe [:db/get-in [:customizer :parts]])
            mesh->part @(r/subscribe [:db/get-in [:customizer :mesh->part package-id]])]
        
        [:> r3d/Resize {"scale" scale}
          [:> r3d/Center
            [:> r3d/Bvh
              [:group {"rotation" [0 -2 0]
                       "scale"    scale}
                (doall
                  (for [^js node nodes]
                    (let [id (.-name node)]
                      ^{:key (str id)}
                      [node-handler node {:parts           parts
                                          :mesh->part      mesh->part
                                          :selected-part-id selected-part-id
                                          :rotation        (:rotation model)}])))]]]]))))

;; ---- MODEL ----
;; -----------------------------------------------------------------------------

(defn view [props selected-part-id]
  [model props selected-part-id]) 
 