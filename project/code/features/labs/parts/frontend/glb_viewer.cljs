(ns features.labs.parts.frontend.glb-viewer
  (:require
   [reagent.core :as reagent] 
   ["react" :as react]
   ["@react-three/drei" :as r3d]
   ["@react-three/fiber" :as r3f]))

;; NOTE: You must have /public/draco/ with draco_decoder.js, draco_wasm_wrapper.js, draco_decoder.wasm

(defn loading-spinner []
  [:> r3d/Html {"center" true}
    [:div {:style {:display "flex"
                   :justify-content "center"
                   :align-items "center"
                   :padding "20px"
                   :color "#666"}}
     [:div {:style {:width "20px"
                    :height "20px"
                    :border "2px solid #f3f3f3"
                    :border-top "2px solid #3498db"
                    :border-radius "50%"
                    :animation "spin 1s linear infinite"}}
      [:style "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); }}"]]]])

(defn broken-model-component [{:keys [url]}]
  [:> r3d/Html {"center" true
                :style {:position "relative"}}
                        
    [:div {:style {:text-align "center"}}
     ;; Error title
      [:h3 {:style {:margin      "0 0 12px 0"
                    :font-size   "18px"
                    :font-weight "600"
                    :opacity     "0.95"}}
        "Model Loading Failed"]
      [:p {:style {:margin      "0 0 20px 0"
                   :font-size   "14px"
                   :opacity     "0.8"
                   :line-height "1.4"}}
        "The Link is broken or not found"]]])

(defn glb-model-component [{:keys [url]}]
  (let [gltf (try (r3d/useGLTF url) (catch js/Error _ nil))]
    (if gltf
      [:> r3d/Center
        [:primitive {:object   (.-scene gltf)
                     :position [0 0 0]
                     :scale    4}]]
      [broken-model-component {:url url}])))

(defn glb-model [{:keys [url]}]
  [:> react/Suspense {:fallback (reagent/as-element [loading-spinner])}
    [glb-model-component {:url url}]])

(defn glb-viewer [{:keys [url width height]}]
  (let [w (or width 200)
        h (or height 200)]
    (if url
      [:> r3f/Canvas {:shadows true
                      :style {:width (str w "px")
                              :height (str h "px")
                              :background "#f5f5f5"
                              :border-radius "8px"
                              :overflow "hidden"}}
        [:> r3d/OrbitControls {:enableZoom true 
                               :makeDefault true
                               :target [0 0 0]}]
        [:ambientLight {:intensity 1.0}]
        [:directionalLight {:position [2 4 2] :intensity 1.2 :castShadow true}]
        [:pointLight {:position [0 2 2] :intensity 0.7}]
       [glb-model {:url url}]]
      [:div {:style {:width (str w "px")
                     :height (str h "px")
                     :background "#f5f5f5"
                     :border-radius "8px"
                     :overflow "hidden"
                     :display "flex"
                     :align-items "center"
                     :justify-content "center"}}
       [:i {:class "fa-solid fa-cube"
            :style {:font-size "32px" :color "#666"}}]])))

;; Optionally, for performance, you can call (r3d/useGLTF.preload url) elsewhere if you want to preload models.