
(ns features.customizer.panel.frontend.blocks.filters
  (:require
    [re-frame.core :as r]
    ["react" :as react]
    ["framer-motion" :refer [motion]]
    [ui.select  :as select]
    [ui.button  :as button]
    [ui.floater :as floater]))

(def COLOR-FAMILY-OPTIONS
  [{:label "All Colors" :value nil :color "#666"}
   {:label "Red" :value "red" :color "#ff4444"}
   {:label "Blue" :value "blue" :color "#4488ff"} 
   {:label "Purple" :value "purple" :color "#aa44ff"}
   {:label "Green" :value "green" :color "#44ff88"}
   {:label "Orange" :value "orange" :color "#ff8844"}
   {:label "Yellow" :value "yellow" :color "#ffdd44"}
   {:label "Pink" :value "pink" :color "#ff44aa"}
   {:label "Black" :value "black" :color "#333"}
   {:label "White" :value "white" :color "#fff"}])

(def PRICE-GROUP-OPTIONS
  [{:label "All Prices" :value nil :stars 0}
   {:label "$Group 1" :value "basic" :stars 1}
   {:label "$Group 2" :value "premium" :stars 2}
   {:label "$Group 3" :value "premium-plus" :stars 3}
   {:label "$Group 4" :value "luxury" :stars 4}])

(def SURFACE-OPTIONS
  [{:label "All Surfaces" :value nil :pattern "none" :letter "A"}
   {:label "Crocodile" :value "crocodile" :pattern "crocodile" :color "#4a5568" :letter "C"}
   {:label "Chameleon" :value "chameleon" :pattern "chameleon" :color "#38a169" :letter "H"}
   {:label "Vein" :value "vein" :pattern "vein" :color "#805ad5" :letter "V"}
   {:label "Hammer" :value "hammer" :pattern "hammer" :color "#d69e2e" :letter "M"}
   {:label "Smooth" :value "smooth" :pattern "smooth" :color "#718096" :letter "S"}])

(def COAT-OPTIONS
  [{:label "All Coats" :value nil :finish "none" :letter "A"}
   {:label "Clear Coat" :value "clear-coat" :finish "glossy" :color "#e2e8f0" :letter "C"}
   {:label "Matte" :value "matte" :finish "matte" :color "#4a5568" :letter "M"}
   {:label "Satin" :value "satin" :finish "satin" :color "#718096" :letter "S"}])

;; -----------------------------------------------------------------------------
;; ---- Drawer Events ----

(r/reg-event-fx
  :customizer.filters/open!
  (fn [{:keys [db]} [_]]
    {:db (assoc-in db [:customizer.filters/drawer] true)
     :dispatch [:customizer.menu/close!]}))

(r/reg-event-db
  :customizer.filters/close!
  (fn [db [_]]
    (assoc-in db [:customizer.filters/drawer] false)))

;; ---- Drawer Events ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Fetch ----

(defn customizer-looks-filters-callback [response]
  (let [data (get-in response [:customizer.looks/filters!])]
    (r/dispatch [:customizer.looks/init! data])))

(r/reg-event-fx
  :customizer.looks.filters/select!
  (fn [{:keys [db]} [_ key value]]
    (let [filters (reduce (fn [acc [k v]] (assoc acc k (:value v [(:value (first v))]))) 
                         {}
                         (assoc (get db :customizer/filters) key value))]
      {:dispatch       [:db/assoc-in [:customizer/filters key] value]
       :pathom/request {:query    [`(:customizer.looks/filters! ~filters)]
                        :callback customizer-looks-filters-callback}})))

;; ---- Fetch ----
;; -----------------------------------------------------------------------------

;; ---- Surface Picker ----

(defn get-surface-pattern [pattern]
  (case pattern
    "crocodile" "radial-gradient(circle at 30% 40%, #666 20%, transparent 50%), radial-gradient(circle at 70% 80%, #444 15%, transparent 40%)"
    "chameleon" "linear-gradient(45deg, #00ff88 25%, transparent 25%), linear-gradient(-45deg, #88ff00 25%, transparent 25%), linear-gradient(45deg, transparent 75%, #00ff88 75%), linear-gradient(-45deg, transparent 75%, #88ff00 75%)"
    "vein" "linear-gradient(90deg, transparent 70%, #999 70%, #999 85%, transparent 85%)"
    "hammer" "radial-gradient(circle at 20% 50%, #ccc 10%, transparent 20%), radial-gradient(circle at 80% 20%, #aaa 8%, transparent 18%)"
    "smooth" "linear-gradient(180deg, #f1f5f9 0%, #cbd5e1 100%)"
    "none"))

(defn surface-circle [{:keys [label value pattern color letter]} selected? on-select]
  (let [background-style (if (= pattern "none")
                          "#666"
                          (str color ", " (get-surface-pattern pattern)))
        border-color (if selected? "var(--irb-clr)" "rgba(255,255,255,0.3)")]
    [:button {:class "surface-circle"
              :title label
              :data-selected selected?
              :style {:width "36px"
                      :height "36px"
                      :border-radius "8px"
                      :background background-style
                      :background-size "8px 8px"
                      :border (str (if selected? "3px" "2px") " solid " border-color)
                      :cursor "pointer"
                      :transition "all 0.2s ease"
                      :box-shadow (if selected? "0 0 0 2px rgba(255,215,0,0.3)" "none")
                      :transform (if selected? "scale(1.1)" "scale(1)")
                      :position "relative"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"}
              :on-click #(on-select value)
              :on-mouse-enter #(set! (.-transform (.-style (.-target %))) "scale(1.05)")
              :on-mouse-leave #(set! (.-transform (.-style (.-target %))) (if selected? "scale(1.1)" "scale(1)"))}
     [:span {:style {:color "white"
                     :font-size "16px"
                     :font-weight "700"
                     :text-shadow "1px 1px 2px rgba(0,0,0,0.8)"
                     :z-index "2"}}
      letter]]))

(defn surface-picker [{:keys [selected-value on-select]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "12px"}}
   [:h4 {:style {:margin "0"
                 :font-size "14px"
                 :font-weight "600"
                 :color "rgba(255,255,255,0.9)"}}
    "Surface"]
   [:div {:style {:display "grid"
                  :grid-template-columns "repeat(3, 1fr)"
                  :gap "8px"
                  :justify-items "center"}}
    (for [surface-option SURFACE-OPTIONS]
      ^{:key (str "surface-" (:value surface-option))}
      [surface-circle 
       surface-option 
       (= (:value surface-option) selected-value) 
       on-select])]])

;; ---- Top Coat Picker ----

(defn coat-circle [{:keys [label value finish color letter]} selected? on-select]
  (let [border-color (if selected? "var(--irb-clr)" "rgba(255,255,255,0.3)")
        finish-style (case finish
                       "glossy" {:background (str "linear-gradient(135deg, " color " 0%, #ffffff 50%, " color " 100%)")
                                 :box-shadow "inset 0 2px 4px rgba(255,255,255,0.6)"}
                       "matte" {:background color}
                       "satin" {:background (str "linear-gradient(45deg, " color " 0%, #ffffff 30%, " color " 100%)")
                                :box-shadow "inset 0 1px 2px rgba(255,255,255,0.3)"}
                       "none" {:background "#666"})]
    [:button {:class "coat-circle"
              :title label
              :data-selected selected?
              :style (merge {:width "36px"
                            :height "36px"
                            :border-radius "50%"
                            :border (str (if selected? "3px" "2px") " solid " border-color)
                            :cursor "pointer"
                            :transition "all 0.2s ease"
                            :box-shadow (if selected? "0 0 0 2px rgba(255,215,0,0.3)" "none")
                            :transform (if selected? "scale(1.1)" "scale(1)")
                            :position "relative"
                            :display "flex"
                            :align-items "center"
                            :justify-content "center"}
                           finish-style)
              :on-click #(on-select value)
              :on-mouse-enter #(set! (.-transform (.-style (.-target %))) "scale(1.05)")
              :on-mouse-leave #(set! (.-transform (.-style (.-target %))) (if selected? "scale(1.1)" "scale(1)"))}
     [:span {:style {:color "white"
                     :font-size "16px"
                     :font-weight "700"
                     :text-shadow "1px 1px 2px rgba(0,0,0,0.8)"
                     :z-index "2"}}
      letter]]))

(defn coat-picker [{:keys [selected-value on-select]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "12px"}}
   [:h4 {:style {:margin "0"
                 :font-size "14px"
                 :font-weight "600"
                 :color "rgba(255,255,255,0.9)"}}
    "Top Coat"]
   [:div {:style {:display "grid"
                  :grid-template-columns "repeat(4, 1fr)"
                  :gap "8px"
                  :justify-items "center"}}
    (for [coat-option COAT-OPTIONS]
      ^{:key (str "coat-" (:value coat-option))}
      [coat-circle 
       coat-option 
       (= (:value coat-option) selected-value) 
       on-select])]])

;; ---- Price Picker ----

(defn price-rectangle [{:keys [label value stars]} selected? on-select]
  (let [border-color (if selected? "var(--irb-clr)" "rgba(255,255,255,0.3)")]
    [:button {:class "price-rectangle"
              :title label
              :data-selected selected?
              :style {:width "60px"
                      :height "24px"
                      :border-radius "8px"
                      :background (if (= stars 0) "#666" "linear-gradient(135deg, #2d3748 0%, #4a5568 100%)")
                      :border (str (if selected? "3px" "2px") " solid " border-color)
                      :cursor "pointer"
                      :transition "all 0.2s ease"
                      :box-shadow (if selected? "0 0 0 2px rgba(255,215,0,0.3)" "none")
                      :transform (if selected? "scale(1.1)" "scale(1)")
                      :position "relative"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"}
              :on-click #(on-select value)
              :on-mouse-enter #(set! (.-transform (.-style (.-target %))) "scale(1.05)")
              :on-mouse-leave #(set! (.-transform (.-style (.-target %))) (if selected? "scale(1.1)" "scale(1)"))}
     (if (= stars 0)
       [:span {:style {:color "white"
                       :font-size "14px"
                       :font-weight "700"
                       :text-shadow "1px 1px 2px rgba(0,0,0,0.8)"}}
        "A"]
       [:div {:style {:display "flex"
                      :gap "2px"
                      :justify-content "center"
                      :align-items "center"}}
        (for [i (range stars)]
          ^{:key i}
          [:i {:class "fa-solid fa-star"
               :style {:color "var(--irb-clr)"
                       :font-size "10px"
                       :text-shadow "1px 1px 1px rgba(0,0,0,0.8)"}}])])]))

(defn price-picker [{:keys [selected-value on-select]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "12px"}}
   [:h4 {:style {:margin "0"
                 :font-size "14px"
                 :font-weight "600"
                 :color "rgba(255,255,255,0.9)"}}
    "Price"]
   [:div {:style {:display "flex"
                  :flex-direction "column"
                  :gap "6px"
                  :align-items "center"}}
    ;; First row - "All Prices" full width
    (let [all-option (first PRICE-GROUP-OPTIONS)]
      [:div {:style {:width "100%"
                     :display "flex"
                     :justify-content "center"}}
       [price-rectangle 
        all-option 
        (= (:value all-option) selected-value) 
        on-select]])
    
    ;; Second row - Groups 1 & 2
    [:div {:style {:display "flex"
                   :gap "8px"
                   :justify-content "center"}}
     (for [price-option (take 2 (drop 1 PRICE-GROUP-OPTIONS))]
       ^{:key (str "price-" (:value price-option))}
       [price-rectangle 
        price-option 
        (= (:value price-option) selected-value) 
        on-select])]
    
    ;; Third row - Groups 3 & 4
    [:div {:style {:display "flex"
                   :gap "8px"
                   :justify-content "center"}}
     (for [price-option (drop 3 PRICE-GROUP-OPTIONS)]
       ^{:key (str "price-" (:value price-option))}
       [price-rectangle 
        price-option 
        (= (:value price-option) selected-value) 
        on-select])]]])

;; ---- Color Picker ----

(defn color-circle [{:keys [label value color]} selected? on-select]
  (let [is-white (= color "#fff")
        border-color (cond
                       selected? "var(--irb-clr)"
                       is-white "rgba(255,255,255,0.8)"
                       :else "rgba(255,255,255,0.3)")]
    [:button {:class "color-circle"
              :title label
              :data-selected selected?
              :style {:width "36px"
                      :height "36px"
                      :border-radius "50%"
                      :background color
                      :border (str (if selected? "3px" "2px") " solid " border-color)
                      :cursor "pointer"
                      :transition "all 0.2s ease"
                      :box-shadow (if selected? "0 0 0 2px rgba(255,215,0,0.3)" "none")
                      :transform (if selected? "scale(1.1)" "scale(1)")
                      :position "relative"}
              :on-click #(on-select value)
              :on-mouse-enter #(set! (.-transform (.-style (.-target %))) "scale(1.05)")
              :on-mouse-leave #(set! (.-transform (.-style (.-target %))) (if selected? "scale(1.1)" "scale(1)"))}
     (when (= value nil)
       [:i {:class "fa-solid fa-palette"
            :style {:color "white"
                    :font-size "14px"
                    :position "absolute"
                    :top "50%"
                    :left "50%"
                    :transform "translate(-50%, -50%)"}}])]))

(defn color-picker [{:keys [selected-value on-select]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "12px"}}
   [:h4 {:style {:margin "0"
                 :font-size "14px"
                 :font-weight "600"
                 :color "rgba(255,255,255,0.9)"}}
    "Main Color"]
   [:div {:style {:display "grid"
                  :grid-template-columns "repeat(5, 1fr)"
                  :gap "8px"
                  :justify-items "center"}}
    (for [color-option COLOR-FAMILY-OPTIONS]
      ^{:key (str "color-" (:value color-option))}
      [color-circle 
       color-option 
       (= (:value color-option) selected-value) 
       on-select])]])

;; ---- Color Picker ----

;; ---- Header ----

(defn header [] 
  [:div  {:style {:border-bottom "1px solid"
                  :padding-bottom "6px"
                  :margin-bottom "6px"}}
    [:p {:style {:text-align "center" :font-size "16px" :font-weight "600"}} "Filters"] 
    [:button {:style  {:position "absolute"
                       :right "0"
                       :top   "0"}
              :on-click #()}
      [:i {:class ""}]]])

;; ---- Header ----

(defn filters []
  (let [filters @(r/subscribe [:db/get-in [:customizer/filters]])]
    [:div {:id    "part-configurator--filters"
           :style {:display         "flex"
                   :flex-direction  "column"
                   :gap             "25px"
                   :padding-bottom  "25px"}}
      [:div {:id "part-configurator--filters-color"}
        [color-picker {:selected-value (:color_family filters nil)
                       :on-select (fn [value]
                                    (r/dispatch [:customizer.looks.filters/select! :color_family value]))}]]


      [:div {:id "part-configurator--filters-surface"}
        [surface-picker {:selected-value (:surface filters nil)
                         :on-select (fn [value]
                                      (r/dispatch [:customizer.looks.filters/select! :surface value]))}]]

      [:div {:id "part-configurator--filters-coat"}
        [coat-picker {:selected-value (:coat filters nil)
                      :on-select (fn [value]
                                   (r/dispatch [:customizer.looks.filters/select! :coat value]))}]]

      [:div {:id "part-configurator--filters-price"}
        [price-picker {:selected-value (:price_group_key filters)
                       :on-select (fn [value]
                                    (r/dispatch [:customizer.looks.filters/select! :price_group_key value]))}]]]))

;; ---- Drawer ----

(defn desktop-open-button []
  (let [drawer-open? @(r/subscribe [:db/get-in [:customizer.filters/drawer]])]
    [button/view {:id       "customizer--sidebar--filters-btn"
                  :class    "header--grow-button"
                  :color    (if drawer-open? "var(--irb-clr)" "rgba(255, 255, 255, 0.1)")
                  :style    {:color (when drawer-open? "black")}
                  :on-click #(if drawer-open?
                               (r/dispatch [:customizer.filters/close!])
                               (r/dispatch [:customizer.filters/open!]))}
      [:i {:class "fa-solid fa-filter"}]
      [:p "Filters"]]))

(defn floater []
  [floater/view {:orientation :left
                 :class       "customizer--floater-ui"
                 :state       @(r/subscribe [:db/get-in [:customizer.filters/drawer]])
                 :config      {:bg false}}
    [:div {:style {:height         "100%"
                   :display        "flex"
                   :flex-direction "column"}}
      [header]
      [filters]]])

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn desktop-view []
  [:<>
    [desktop-open-button]
    [floater]])
