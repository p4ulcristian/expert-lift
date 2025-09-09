(ns features.flex.batches.frontend.batch-editor.components.recipe
  (:require [clojure.string]))

;; -----------------------------------------------------------------------------
;; ---- Recipe Component Helper Functions ----

(defn- recipe-container-style [recipe]
  "Style map for recipe component container"
  {:border (str "2px solid " (:recipe/color recipe))
   :background-color "#fff"
   :padding "12px"
   :margin "4px"
   :border-radius "8px"
   :display "flex"
   :flex-direction "column"
   :gap "8px"
   :cursor "grab"
   :position "relative"
   :box-shadow "0 2px 4px rgba(0,0,0,0.1)"
   :transition "all 0.2s ease"})

(defn- recipe-header-section [recipe]
  "Recipe header with drag handle and color indicator"
  [:div {:style {:display "flex" :align-items "center" :gap "8px" :margin-bottom "4px"}}
   [:span {:style {:color "#666" :font-size "12px" :cursor "grab"}} "⋮⋮"]
   [:div {:style {:flex 1 :display "flex" :align-items "center" :gap "8px"}}
    [:div {:style {:width "12px"
                   :height "12px"
                   :border-radius "2px"
                   :background-color (:recipe/color recipe)
                   :flex-shrink "0"}}]
    [:div {:style {:font-weight "600" :font-size "14px" :color "#333"}} (:recipe/name recipe)]]])

(defn- recipe-details-section [recipe]
  "Recipe details with step count and badge"
  [:div {:style {:display "flex" :align-items "center" :justify-content "space-between"}}
   [:div {:style {:font-size "12px" :color "#666"}} 
    (str (count (:recipe/processes recipe)) " step" (when (not= (count (:recipe/processes recipe)) 1) "s"))]
   [:div {:style {:background-color (:recipe/color recipe)
                  :color "white"
                  :padding "3px 8px"
                  :border-radius "12px"
                  :font-size "10px"
                  :font-weight "600"
                  :text-transform "uppercase"
                  :letter-spacing "0.5px"}}
    "Recipe"]])

(defn- recipe-process-preview [recipe]
  "Process preview for recipes with 3 or fewer steps"
  (when (<= (count (:recipe/processes recipe)) 3)
    [:div {:style {:font-size "11px" :color "#888" :line-height "1.3"}}
     (clojure.string/join " → " (map :process/name (:recipe/processes recipe)))]))

(defn recipe-component [recipe index]
  [:div {:data-id (:recipe/id recipe)
         :style (recipe-container-style recipe)
         :class "draggable-item recipe-item"
         :on-mouse-enter #(-> ^js % .-target .-style (.setProperty "transform" "translateY(-2px)"))
         :on-mouse-leave #(-> ^js % .-target .-style (.setProperty "transform" "translateY(0)"))}
   [recipe-header-section recipe]
   [recipe-details-section recipe]
   [recipe-process-preview recipe]])