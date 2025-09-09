(ns features.labs.looks.frontend.editor.controls
  (:require
   ["react"       :as react]
   [features.common.storage.frontend.picker :as storage.picker]
   [reagent.core  :as reagent]
   [ui.button :as button]
   [ui.notification :as notification]
   [ui.text-field :as text-field]))
   

(defn- calculate-percentage [value min max]
  (let [range (- max min)
        normalized-value (- value min)]
    (* 100 (/ normalized-value range))))

(def STATE (reagent/atom nil))

(defn input-label [input-id label]
  (if (empty? label)
    [:p {:style {:font-size "0.8rem" :margin-bottom "3px" :color "white"}} (str (name input-id))]
    [:p {:style {:font-size "0.8rem" :margin-bottom "3px" :color "white"}} label]))

(defn number [input-id {:keys [path label default min max] :or {min 0 max 1}}]
  
  (let [value (get-in @STATE path)]
    (react/useEffect
      (fn []
        (swap! STATE assoc-in path (or value default min))
        (fn []))
      #js[])

    [:div
      [input-label input-id label]
      [:div {:class "input--number-container"
             :style {:padding-right "8px"}}
        [:input {:type      "range"
                 :min       min 
                 :max       max 
                 :step      0.001
                 :value     value
                 :on-click  #(.focus (.-nextElementSibling (.-target %)))
                 :on-change #(swap! STATE assoc-in path (-> % .-target .-valueAsNumber))
                 :class     "modern-range"
                 :style     {:background (str "linear-gradient(to right, var(--ir-primary) " (calculate-percentage value min max) "% " 
                                                                      ", var(--ir-border-light) " (calculate-percentage value min max) "%)")}}]
        [:input {:type      "number"
                 :value     value
                 :min       min
                 :max       max 
                 :step      0.001
                 :on-change #(let [_value (-> % .-target .-valueAsNumber)]
                               (cond (or (>= min _value) (js/Number.isNaN _value)) (swap! STATE assoc-in path min)
                                     (<= max _value) (swap! STATE assoc-in path max)
                                     :else (swap! STATE assoc-in path _value)))
                 :style     {:color "white"
                                  :position "absolute"
                                  :pointer-events "none"
                                  :user-select "none"
                                  :width "80px"
                                  :text-align "center"}}]]]))
                             
      
                  


;; -----------------------------------------------------------------------------
;; ---- Vector ----

(defn vector-input [input-id {:keys [path label default _min _max] :as _input-props}]
  (let [value (get-in @STATE path)]
    
    (react/useEffect
      (fn []
        (swap! STATE assoc-in path (or value default [0 0]))
        (fn []))
      #js[])
  
    [:<>
      [input-label input-id label]
      [:div {:style {:display "flex"
                     :align-items "center"
                     :gap         "8px"
                     :color       "white"
                     :padding     "0 8px"}}
        [:input {:type      "number"
                 :value     (first value)
                 :step      0.001
                 :on-change #(reset! STATE 
                                (update-in @STATE path assoc 0 (-> % .-target .-valueAsNumber)))
                 :style     {:color "white"
                             :background "var(--ir-primary)"
                             :border "1px solid var(--ir-border-light)"
                             :border-radius "6px"
                             :width         "70px"
                             :padding      "4px 6px"}}]
        [:input {:type      "number"
                 :value     (last value)
                 :step      0.001
                 :on-change #(reset! STATE
                                (update-in @STATE path assoc 1 (-> % .-target .-valueAsNumber)))
                 :style     {:color "white"
                             :background "var(--ir-primary)"
                             :border "1px solid var(--ir-border-light)"
                             :border-radius "6px"
                             :width         "70px"
                             :padding      "4px 6px"}}]]]))

;; ---- Vector ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Text ----

(defn text [input-id {:keys [path label] :as _input-props}]
  (let [value (get-in @STATE path)]
    (react/useEffect
     (fn []
       (swap! STATE assoc-in path "")
       (fn []))
     #js[])
  
    [:<>
      [input-label input-id label]
      [:input {:type      "text"
               :value     value
               :on-change #(swap! STATE assoc-in path (-> % .-target .-value))
               :style     {:color "white"
                           :border "1px solid gray"
                           :border-radius "6px"}}]]))

;; ---- Text ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Color ----

(defn copy-to-clipboard-button [value]
  [button/view {:on-click (fn []
                            (js/navigator.clipboard.writeText value)
                            (notification/toast! :blank "Copied to clipboard" 
                                                 #js{"icon" (reagent/as-element [:i {:class ["fa-solid" "fa-copy"]}])}))
                :style    {:box-shadow "none"
                           :padding "6px 8px"}}
    [:i {:class ["fa-solid" "fa-copy"]
         :style {:color "var(--ir-text-secondary)"}}]])

(defn color-input-field [value path]
  [text-field/view {:type            "text"
                    :value           value
                    :on-change       #(swap! STATE assoc-in path %)
                    :right-adornment [copy-to-clipboard-button value]
                    :style           {:width         "100%"
                                      :border        "none"
                                      :box-shadow    "none"
                                      :padding       "0px 8px"
                                      :border-radius "6px"}}])

(defn color-input-picker [value path]
  [:input {:type      "color"
           :class     "input--color"
           :value     value
           :on-change #(swap! STATE assoc-in path (-> % .-target .-value))
           :style     {:height "28px"
                       :width "28px"
                       :border-radius "6px"}}])

(defn color [input-id {:keys [initial-value path label]
                       :or   {initial-value "#000000"}}]
  (let [value (get-in @STATE path)]
    (react/useEffect
      (fn []
        (swap! STATE assoc-in path (or value initial-value))
        (fn []))
      #js[])
  
    [:div
      [input-label input-id label]
      [:div {:style {:display               "grid"
                     :align-items           "center"
                     :grid-template-columns "auto 1fr"
                     :gap                   "8px"
                     :padding-right         "8px"}}
        
        [color-input-picker value path]
        [color-input-field value path]]]))
                                                   
      

;; ---- Color ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Image ----

(defn image [input-id {:keys [path label] :as _input-props}]
  (let [value (get-in @STATE path)]
    
    (react/useEffect
      (fn []
        (swap! STATE assoc-in path value)
        (fn []))
      #js[])
  
    [:div {:class "state-manager--img-input"}
      [input-label input-id label]
      [storage.picker/view
        {:accept        ["image/png" "image/svg+xml"]
         :value         value
         :on-select     #(swap! STATE assoc-in path (:url %))}]]))

;; ---- Image ----
;; -----------------------------------------------------------------------------

(defn bool [input-id {:keys [path label] :as _input-props}]
  (let [value (get-in @STATE path false)]
    (react/useEffect
      (fn []
        (swap! STATE assoc-in path (or value false))
        (fn []))
      #js[])
  
    [:<>
      [:div {:style {:display "flex"
                     :align-items "center"
                     :gap         "6px"
                     :color      "white"}}
        [:input {:id        path
                 :type      :checkbox
                 :checked   value
                 :on-change #(swap! STATE assoc-in path (not value))}]
                            
        [:label {:for path 
                 :style {:color "white"}} 
          (str (or label input-id))]]]))

(defn input-broker [key {:keys [type] :as input-props}]
  (cond
    (= type :number)  [number key input-props]
    (= type :vector)  [vector-input key input-props]
    (= type :text)    [text key input-props]
    (= type :color)   [color key input-props]
    (= type :image)   [image key input-props]
    (= type :boolean) [bool key input-props]
    :else nil))

(defn group [group-name meta items]
  (let [[o? set-o] (react/useState (:state meta false))]
    [:div {:class "controls--group"
           :style {}}
     
     [button/view {:mode     :clear_2
                   :type     :secondary
                    :on-click #(set-o not)
                    :style    {:width           "100%"
                               :padding         "12px 6px"
                               :margin-bottom   "12px"
                               :display         "flex"
                               :gap             "6px"
                               :align-items     "center"
                               :border-radius   "0"
                               :color           "white"}} 
       (if o?
         [:i {:class ["fa-solid" "fa-caret-down"]}]
         [:i {:class ["fa-solid" "fa-caret-right"]}])
       [:b {:style {:color "white"}} (str group-name)]]

     [:div {:class "controls--group-body"
            :style {:margin-left "10px"
                    :padding-left "6px"
                    :display     "grid"
                    :gap         "16px"
                    :border-left "1px solid var(--ir-border-light)" 
                    :height      (if o? "auto" "0px")
                    :overflow    "hidden"}}
                     
       items]]))

(defn render-inputs [structure & [path]]
  (reduce
    (fn [fragment [key inner-structure]]
      (let [path (conj (vec path) key)]
        (if-let [input (input-broker key (assoc inner-structure :path path))]
          (conj fragment input)
          (conj fragment
                [group key (meta inner-structure) (render-inputs inner-structure path)]))))
    [:<>]
    (sort structure)))

(defn panel [structure _state]
  [render-inputs structure])

(defn state-manager [structure]
  [[panel structure STATE] @STATE])
