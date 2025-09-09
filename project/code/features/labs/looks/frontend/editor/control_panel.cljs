(ns features.labs.looks.frontend.editor.control-panel
  (:require
   [features.labs.looks.frontend.editor.controls :as controls]))

;; Helper function to flatten material groups
(defn flatten-material-groups [groups]
  (reduce-kv (fn [acc _category props]
               (merge acc (reduce-kv (fn [acc prop value]
                                       (assoc acc prop value))
                                   {}
                                   props)))
             {}
             groups))

;; Material configuration groups
(def material-groups
  {"Basic Properties"
   {"color" {:type :color :initial-value "#ffffff"}
    "opacity" {:type :number :min 0 :max 1 :default 1}
    "transparent" {:type :boolean :default false}
    "wireframe" {:type :boolean :default false}
    "map" {:type :image}
    "alphaMap" {:type :image}
    "aoMap" {:type :image}
    "aoMapIntensity" {:type :number :min 0 :max 1 :default 1}
    "displacementMap" {:type :image}
    "displacementScale" {:type :number :min 0 :max 10 :default 1}
    "displacementBias" {:type :number :min -1 :max 1 :default 0}
    "emissive" {:type :color :initial-value "#000000"}
    "emissiveMap" {:type :image}
    "emissiveIntensity" {:type :number :min 0 :max 1 :default 1}
    "envMap" {:type :image}
    "envMapIntensity" {:type :number :min 0 :max 1 :default 1}
    "lightMap" {:type :image}
    "lightMapIntensity" {:type :number :min 0 :max 1 :default 1}
    "normalMap" {:type :image}
    "normalScale" {:type :vector :default [1 1]}}

   "Physical Properties"
   {"metalness" {:type :number :min 0 :max 1 :default 0}
    "metalnessMap" {:type :image}
    "roughness" {:type :number :min 0 :max 1 :default 1}
    "roughnessMap" {:type :image}
    "reflectivity" {:type :number :min 0 :max 1 :default 0.5}
    "ior" {:type :number :min 1 :max 2.333 :default 1.5}
    "iridescence" {:type :number :min 0 :max 1 :default 0}
    "iridescenceIOR" {:type :number :min 1 :max 2.333 :default 1.3}
    "iridescenceThicknessRange" {:type :vector :default [100 400]}
    "iridescenceThicknessMap" {:type :image}
    "transmission" {:type :number :min 0 :max 1 :default 0}
    "transmissionMap" {:type :image}
    "thickness" {:type :number :min 0 :max 1 :default 0.5}
    "attenuationDistance" {:type :number :min 0 :max 1 :default 0}
    "attenuationColor" {:type :color :initial-value "#ffffff"}}

   "Clearcoat"
   {"clearcoat" {:type :number :min 0 :max 1 :default 0}
    "clearcoatMap" {:type :image}
    "clearcoatRoughness" {:type :number :min 0 :max 1 :default 0}
    "clearcoatRoughnessMap" {:type :image}
    "clearcoatNormalMap" {:type :image}
    "clearcoatNormalScale" {:type :vector :default [1 1]}}

   "Sheen"
   {"sheen" {:type :number :min 0 :max 1 :default 0}
    "sheenColor" {:type :color :initial-value "#ffffff"}
    "sheenColorMap" {:type :image}
    "sheenRoughness" {:type :number :min 0 :max 1 :default 1}
    "sheenRoughnessMap" {:type :image}}

   "Specular"
   {"specularIntensity" {:type :number :min 0 :max 1 :default 1}
    "specularIntensityMap" {:type :image}
    "specularColor" {:type :color :initial-value "#ffffff"}
    "specularColorMap" {:type :image}}

   "Advanced"
   {"bumpMap" {:type :image}
    "bumpScale" {:type :number :min 0 :max 1 :default 1}
    "gradientMap" {:type :image}
    "repeat" {"x" {:type :number :min 0 :max 100 :default 20}
              "y" {:type :number :min 0 :max 100 :default 20}}}})

;; UI control inputs with metadata
(def control-inputs
  {"Basic Properties" (with-meta (get material-groups "Basic Properties") {:state false})
   "Physical Properties" (with-meta (get material-groups "Physical Properties") {:state false})
   "Clearcoat" (with-meta (get material-groups "Clearcoat") {:state false})
   "Sheen" (with-meta (get material-groups "Sheen") {:state false})
   "Specular" (with-meta (get material-groups "Specular") {:state false})
   "Advanced" (with-meta (get material-groups "Advanced") {:state false})})

;; Function to get material props in a flat structure
(defn get-material-props [groups]
  (flatten-material-groups groups))


(defn texture-maps []
  [controls/group "Texture Maps" {:state false}
    [:<>
      ;; Basic Maps
      [controls/group "Basic Maps" {:state false}
        [:<>
          [controls/image "map"               {:path ["maps" "map"]}]
          [controls/image "alphaMap"          {:path ["maps" "alphaMap"]}]
          [controls/image "aoMap"             {:path ["maps" "aoMap"]}]
          [controls/image "displacementMap"   {:path ["maps" "displacementMap"]}]
          [controls/image "emissiveMap"       {:path ["maps" "emissiveMap"]}]
          [controls/image "envMap"            {:path ["maps" "envMap"]}]
          [controls/image "lightMap"          {:path ["maps" "lightMap"]}]
          [controls/image "normalMap"         {:path ["maps" "normalMap"]}]]]

      ;; Clearcoat Maps
      [controls/group "Clearcoat Maps" {:state false}
        [:<>
          [controls/image "clearcoatMap"          {:path ["maps" "clearcoatMap"]}]
          [controls/image "clearcoatNormalMap"    {:path ["maps" "clearcoatNormalMap"]}]
          [controls/image "clearcoatRoughnessMap" {:path ["maps" "clearcoatRoughnessMap"]}]]]

      ;; Physical Maps
      [controls/group "Physical Maps" {:state false}
        [:<>
          [controls/image "metalnessMap"      {:path ["maps" "metalnessMap"]}]
          [controls/image "roughnessMap"      {:path ["maps" "roughnessMap"]}]]]

      ;; Specular Maps
      [controls/group "Specular Maps" {:state false}
        [:<>
          [controls/image "specularColorMap"     {:path ["maps" "specularColorMap"]}]
          [controls/image "specularIntensityMap" {:path ["maps" "specularIntensityMap"]}]]]

      ;; Sheen Maps
      [controls/group "Sheen Maps" {:state false}
        [:<>
          [controls/image "sheenColorMap"     {:path ["maps" "sheenColorMap"]}]
          [controls/image "sheenRoughnessMap" {:path ["maps" "sheenRoughnessMap"]}]]]

      ;; Other Maps
      [controls/group "Other Maps" {:state false}
        [:<>
          [controls/image "transmissionMap"   {:path ["maps" "transmissionMap"]}]
          [controls/image "bumpMap"           {:path ["maps" "bumpMap"]}]
          [controls/image "gradientMap"       {:path ["maps" "gradientMap"]}]]]]])

(defn basic-properties []
  [controls/group "Basic Properties" {:state true}
    [:<>
      [controls/number "repeatX" {:min 0 :max 100 :default 20 :path ["settings" "repeat" "x"]}]
      [controls/number "repeatY" {:min 0 :max 100 :default 20 :path ["settings" "repeat" "y"]}]
      [controls/color        "color"              {:initial-value "#ffffff" 
                                                   :path          ["material" "color"]}]
   
      [controls/number       "opacity"            {:min     0 
                                                   :max     1 
                                                   :default 1 
                                                   :path    ["material" "opacity"]}]
   
      [controls/bool         "transparent"        {:default false 
                                                   :path    ["material" "transparent"]}]
   
      [controls/bool         "wireframe"          {:default false 
                                                   :path    ["material" "wireframe"]}]
   
      [controls/number       "aoMapIntensity"    {:min     0 
                                                  :max     1 
                                                  :default 1 
                                                  :path    ["material" "aoMapIntensity"]}]
  
      [controls/number       "displacementScale" {:min     0 
                                                  :max     10 
                                                  :default 1 
                                                  :path    ["material" "displacementScale"]}]
  
      [controls/number       "displacementBias"  {:min     -1 
                                                  :max     1 
                                                  :default 0 
                                                  :path    ["material" "displacementBias"]}]
   
      [controls/color        "emissive"          {:initial-value "#000000" 
                                                  :path          ["material" "emissive"]}]
   
      [controls/number       "emissiveIntensity" {:min     0 
                                                  :max     1 
                                                  :default 1 
                                                  :path    ["material" "emissiveIntensity"]}]
   
      [controls/number       "envMapIntensity"   {:min     0 
                                                  :max     1 
                                                  :default 1 
                                                  :path    ["material" "envMapIntensity"]}]
   
      [controls/number       "lightMapIntensity" {:min     0 
                                                  :max     1 
                                                  :default 1 
                                                  :path    ["material" "lightMapIntensity"]}]
   
      [controls/vector-input "normalScale"       {:default [1 1] 
                                                  :path    ["material" "normalScale"]}]]])

(defn physical-properties []
  [controls/group "Physical Properties" {:state false}
    [:<>
      [controls/number "metalness" {:min 0 :max 1 :default 0 :path ["material" "metalness"]}]
      [controls/number "roughness" {:min 0 :max 1 :default 1 :path ["material" "roughness"]}]
      [controls/number "reflectivity" {:min 0 :max 1 :default 0.5 :path ["material" "reflectivity"]}]
      [controls/number "ior" {:min 1 :max 2.333 :default 1.5 :path ["material" "ior"]}]
      [controls/number "iridescence" {:min 0 :max 1 :default 0 :path ["material" "iridescence"]}]
      [controls/number "iridescenceIOR" {:min 1 :max 2.333 :default 1.3 :path ["material" "iridescenceIOR"]}]
      [controls/vector-input "iridescenceThicknessRange" {:default [100 400] :path ["material" "iridescenceThicknessRange"]}]
      [controls/number "transmission" {:min 0 :max 1 :default 0 :path ["material" "transmission"]}]
      [controls/number "thickness" {:min 0 :max 1 :default 0.5 :path ["material" "thickness"]}]
      [controls/number "attenuationDistance" {:min 0 :max 1 :default 0 :path ["material" "attenuationDistance"]}]
      [controls/color "attenuationColor" {:initial-value "#ffffff" :path ["material" "attenuationColor"]}]]])

(defn clearcoat-properties []
  [controls/group "Clearcoat" {:state false}
    [:<>
      [controls/number "clearcoat" {:min 0 :max 1 :default 0 :path ["material" "clearcoat"]}]
      [controls/number "clearcoatRoughness" {:min 0 :max 1 :default 0 :path ["material" "clearcoatRoughness"]}]
      [controls/vector-input "clearcoatNormalScale" {:default [1 1] :path ["material" "clearcoatNormalScale"]}]]])

(defn sheen-properties []
  [controls/group "Sheen" {:state false}
    [:<>
      [controls/number "sheen" {:min 0 :max 1 :default 0 :path ["material" "sheen"]}]
      [controls/color "sheenColor" {:initial-value "#ffffff" :path ["material" "sheenColor"]}]
      [controls/number "sheenRoughness" {:min 0 :max 1 :default 1 :path ["material" "sheenRoughness"]}]]])

(defn specular-properties []
  [controls/group "Specular" {:state false}
    [:<>
      [controls/number "specularIntensity" {:min 0 :max 1 :default 1 :path ["material" "specularIntensity"]}]
      [controls/color "specularColor" {:initial-value "#ffffff" :path ["material" "specularColor"]}]]])

(defn advanced-properties []
  [controls/group "Advanced" {:state false}
    [:<>
      [controls/number "bumpScale" {:min 0 :max 1 :default 1 :path ["material" "bumpScale"]}]]])

(defn view []
  [:div {:class "hide-scroll"
         :style {:overflow   "auto"
                 :padding    "0 0 15px"
                 :background "var(--ir-secondary)"}}
    [basic-properties]
    [texture-maps]
    [physical-properties]
    [clearcoat-properties]
    [sheen-properties]
    [specular-properties]
    [advanced-properties]])