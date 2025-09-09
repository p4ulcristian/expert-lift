
(ns features.labs.looks.frontend.editor.thumbnail
  (:require 
    [re-frame.core        :as r]
    ["react"              :as react]

    ["@react-three/drei"  :as r3d]
    ["@react-three/fiber" :as r3f]))

(defn- render-thumbnail! [ref ^js props [_ set-render]]  
  (let [canvas-element (.-current ref)
        gl             (.-gl props)
        scene          (.-scene props)
        camera         (.-camera props)]

    (.setSize gl 64 64)
    (aset (.-style canvas-element) "width" "64px")
    (aset (.-style canvas-element) "height" "64px")
    (.render gl scene camera)

    (let [screenshot (.toDataURL canvas-element "image/png")]
      (r/dispatch [:db/assoc-in [:labs :base64] screenshot]))
    
    (set-render false)))

;; -----------------------------------------------------------------------------
;; ---- Lighting ----

(defn lighting [texture-props]
  [:<> 
    (when (< 0.5 (get-in texture-props ["material-props" "metalness"])) 
      [:ambientLight {:intensity 4}])
    [:pointLight {"intensity" 0.4
                  "position"  [0 0 5]}]
        
    [:spotLight {"intensity"   1
                 "attenuation" 1
                 "anglePower"  50
                 "distance"    10
                 "position"    [0 1 3]}]

    [:directionalLight {"intensity" 1
                        "position"  [0 -1 0.5]}]])

(defn environment [texture-props]
  ;; (when (< 0.5 (get-in texture-props ["material" "metalness"]))
    [:> react/Suspense {:fallback nil}
      [:> r3d/Environment {"files" "/labs/env.hdr"}]])

;; ---- Lighting ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Thumbnail ----

(defn thumbnail [set-render]
  
    [:button {:on-click #(set-render true)
              :style    {:padding 0}}
      [:div {:style {:background    "gray"
                     :cursor        "pointer"
                     :padding       "8px"
                     :border-radius "6px"
                     :display       "grid"
                     :height        "120px"
                     :place-items   "center"}}
       (when-let [src @(r/subscribe [:db/get-in [:labs :base64]])]
         [:img {:src src
                :style {:height "100px"
                        :width  "100px"
                        :margin "auto"}}])]])

;; ---- Thumbnail ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Material ----

(def REPEATWRAPPING 1000)
   
(defn- hydrate-texture! [^js maps {:strs [x y]}]
  (doseq [texture-map-key (.keys js/Object maps)]
    (doto (aget maps texture-map-key)
      (set! -wrapS REPEATWRAPPING)
      (set! -wrapT  REPEATWRAPPING)
      (set! -repeat #js{"x" x "y" y}))))

(defn clone-textures [textures]
  (clj->js 
    (reduce (fn [_texture-maps [_key texture-map]]
              (assoc _texture-maps _key (.clone texture-map))) 
            {}
            (js->clj textures))))

(defn remove-nil-maps [texture-maps]
  (->> texture-maps
       (remove (fn [[_ v]] (nil? v)))
       (into {})
       clj->js))
               
(defn mesh-physical-material [{:strs [material maps settings] :as _texture-props}] 
  (let [texture-maps  (-> maps
                          remove-nil-maps
                          r3d/useTexture
                          clone-textures)]
    
    (react/useLayoutEffect
      (fn []
        (hydrate-texture! texture-maps (get settings "repeat"))
        (fn []))
      #js[texture-maps])
    
    [:meshPhysicalMaterial
      (merge {"attach" "material"}
             (js->clj texture-maps)
             material)]))

;; ---- Material ----
;; -----------------------------------------------------------------------------

(defn- thumbnail-generator [texture-props]
  (let [ref           (react/useRef nil)
        [render? set-render :as render-state] (react/useState false)]
    
    (react/useEffect
      (fn []
        ;; Generate thumbnail after 500ms of texture-props change
        (let [timeout-id (js/setTimeout
                           #(set-render true)
                           500)]
          (fn []
            (js/clearTimeout timeout-id))))
      #js[texture-props])
    
    [:<> 
      [thumbnail set-render]
      (when render?
        [:div {:style {:position "fixed" :bottom 0 :left 0 :z-index -100 :visibility "hidden"}}
          [:> r3f/Canvas {"id"        "thumbnail-render-canvas"
                          "ref"       ref
                          "style"     {:width "64px" :height "64px"}
                          "shadows"   true
                          "onCreated" (fn [props] (render-thumbnail! ref props render-state))
                          "gl"        {"preserveDrawingBuffer" true}}

            [lighting texture-props]
            [environment texture-props]

            [:> react/Suspense {:fallback nil}
              [:> r3d/Resize {"scale" 6}
                [:> r3d/Center
                  [:> r3d/Sphere
                    [mesh-physical-material
                     (-> texture-props
                         (assoc-in ["settings" "repeat"] {"x" 1 "y" 1}))]]]]]]])]))           

(defn view [props]
  [thumbnail-generator props])
