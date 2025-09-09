(ns features.customizer.test-3d.frontend.blocks.canvas
  (:require 
    ["react" :as react]
    ["@react-three/fiber" :as r3f :refer [useFrame]]
    ["@react-three/drei" :as r3d]
    ["@react-three/postprocessing" :as pp :refer [Selection Select EffectComposer Outline]]))

(defn make-mesh-interactive [mesh]
  (let [[hovered set-hovered] (react/useState false)
        on-pointer-over (react/useCallback #(set-hovered true) #js [])
        on-pointer-out (react/useCallback #(set-hovered false) #js [])]
    [:> Select {"enabled" hovered}
     [:primitive {"object" mesh
                  "onPointerOver" on-pointer-over
                  "onPointerOut" on-pointer-out
                  "dispose" nil}]]))

(def model-urls
  ["https://bucket-production-f0da.up.railway.app/ironrainbow/aef63f96-8e0d-49da-99f8-e86723c33fae.glb"
   "https://bucket-production-f0da.up.railway.app/ironrainbow/5f8a2dae-8b1b-4611-ba7f-1d1f33ac615a.glb"
   "https://bucket-production-f0da.up.railway.app/ironrainbow/22c9855c-85ba-42a8-b629-99c0e2209d95.glb"])

(defn interactive-model [props current-url]
  (let [ref (react/useRef)
        gltf (r3d/useGLTF current-url)
        [meshes set-meshes] (react/useState [])]
    
    ;; Slower rotation for better performance
    (useFrame (fn [state delta]
                (when (.-current ref)
                  (set! (.. ref -current -rotation -y) (+ (.. ref -current -rotation -y) (* delta 0.3))))))
    
    ;; Extract meshes once when GLTF loads
    (react/useEffect 
      (fn []
        (when gltf
          (let [scene (.-scene gltf)
                found-meshes (atom [])]
            (.traverse scene 
              (fn [child]
                (when (.-isMesh child)
                  (swap! found-meshes conj child))))
            (set-meshes @found-meshes))))
      #js [gltf])
    
    [:group (merge {"ref" ref} props)
     ;; Render each mesh in its own Select for individual highlighting
     (map-indexed 
       (fn [idx mesh]
         ^{:key (str current-url "-" idx)} [make-mesh-interactive mesh])
       meshes)]))


(defn scene []
  (let [[current-url-index set-current-url-index] (react/useState 2)
        current-url (nth model-urls current-url-index)]
    
    [:group
     [:> r3d/OrbitControls {"enablePan" true
                            "enableZoom" true
                            "enableRotate" true}] 
     [:ambientLight {"intensity" 0.5}]
     [:spotLight {"position" [10 10 10] 
                  "angle" 0.15 
                  "penumbra" 1}]
     [:pointLight {"position" [-10 -10 -10]}]
     
     [:> r3d/Grid {"args" [20 20] 
                   "position" [0 -1 0]}]
     
     [:> Selection
      [:> EffectComposer {"multisampling" 2     ; Minimal multisampling for visibility
                          "autoClear" false
                          "stencilBuffer" false  
                          "depthBuffer" true}
       [:> Outline {"blur" false               
                    "visibleEdgeColor" "cyan"  ; More visible color
                    "edgeStrength" 40          ; Increased for visibility
                    "width" 512                ; Balanced resolution
                    "kernelSize" 1}]]          ; Small kernel for sharp outline
      
      [:> react/Suspense {"fallback" nil}
       [interactive-model {"position" [0 0 0]} current-url]]]
     
     ;; Toggle button
     [:> r3d/Html {"position" [-4 3 0]}
      [:button {:style {:padding "10px 20px"
                        :background "#333"
                        :color "white"
                        :border "none"
                        :border-radius "8px"
                        :cursor "pointer"
                        :font-size "14px"}
                :on-click #(set-current-url-index (mod (inc current-url-index) (count model-urls)))}
       (str "Model " (inc current-url-index) "/" (count model-urls))]]]))

(defn view []
  [:div {:class "test-3d-canvas" 
         :style {:position "absolute"
                 :inset 0
                 :z-index 1}}
   [:> r3f/Canvas {"dpr" [1 1]                       ; Minimum DPR for maximum performance
                   "frameloop" "demand"              ; Only render when needed
                   "performance" {"min" 0.1 "max" 1.0 "debounce" 200}  ; Performance scaling
                   "style" {:background "#1a1a1a"}
                   "gl" {"antialias" false           
                         "alpha" false               
                         "preserveDrawingBuffer" false 
                         "powerPreference" "high-performance"
                         "precision" "lowp"          ; Lower precision for better performance
                         "logarithmicDepthBuffer" false}}
    [scene]
    
    ;; Performance monitor - remove in production
    [:> r3d/Stats]]])