
(ns ui.select 
  (:require
    ["react" :as react]
    [re-frame.core :as r]
    [cljs.reader :as reader]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn focusable? [el]
  (and el (.-focus el) (.-tabIndex el)))

(defn mobile-device? []
  (try (-> js/navigator .-userAgentData .-mobile)
       (catch :default _
         (re-matches #"(?i).*(Mobi|Android|iPhone|iPad|iPod).*" js/navigator.userAgent))))

(defn on-down [event]
  (try
    (let [next-option (.-nextSibling (.-target ^js event))]
      (when (focusable? next-option)
        (.focus next-option)))
    (catch :default _))
  (.preventDefault ^js event))

(defn on-up [event]
  (try 
    (let [next-option (.-previousSibling (.-target ^js event))]
      (when (focusable? next-option)
        (.focus next-option)))
    (catch :default _))
  (.preventDefault ^js event))
    

(defn keydown-listener [event _props]
  (case (.-key ^js event)
    "ArrowDown" (on-down event)
    "ArrowUp"   (on-up   event)
    nil))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn option [index option-props {:keys [popover-id value option option-element on-select _multiple] :as select-props}]
  ^{:key (:value option-props option-props)}
  (let [option-id    (str "opt-id-" index)
        option-value (:value option-props option-props)
        option-label (:label option-props option-props)
        selected     (and (= option-value (:value value value))
                          (= option-label (:label value value)))]

    [:button (merge {:key           option-value
                     :id            option-id
                     :class         "input-select--option"
                     :data-selected selected
                     :on-click      (fn [_]
                                      (when on-select
                                        (on-select option-props))
                                        
                                      (r/dispatch [:popover/close popover-id]))}
                    (:override option))
      (if option-element
        [option-element option-props select-props]
        [:<>
          option-label
          (when selected [:i {:class ["fa-solid" "fa-check"] :style {:margin-left "auto"}}])])]))

(defn multi-option [[state set-state] index option-props {:keys [_value option option-element on-select] :as select-props}]
  (let [option-id     (str "opt-id-" index)
        optione-value (:value option-props option-props)
        option-label  (:label option-props option-props)

        _selected-options  (:value select-props [])
        selected          (contains? (set state) optione-value)]

    [:button (merge {:key           optione-value
                     :id            option-id
                     :class         "input-select--option"
                     :data-selected selected
                     :on-click      (fn [_]
                                      (let [_value (if selected
                                                     (-> state set (disj optione-value) vec)
                                                     (-> state set (conj optione-value) vec))]
                                        (println _value)
                                        (set-state _value)                        
                                        (on-select _value)))}
                    (:override option)) 
      (if option-element
         [option-element option-props selected select-props]
         [:<>
           [:div {:style {:display "grid"
                          :aspect-ratio 1
                          :place-items "center"
                          :width "16px"
                          :border "1px solid"}}
             (if selected 
               [:i {:class ["fa-regular" "fa-square-check"]}]
               [:i {:class ["fa-solid" "fa-square"]}])]
           option-label])]))

(defn options-render [state {:keys [_value multiple options] :as select-props}]
  [:<>
    (if multiple
      
      (map-indexed 
        (fn [index option-props]
          ^{:key (hash option-props)}
          [multi-option state index option-props select-props])
        options)
      
      (map-indexed
        (fn [index option-props]
          ^{:key (hash option-props)}
          [option index option-props select-props])
        options))])


(defn dropdown-popover [{:keys [value multiple dropdown-override] :as select-props}]
  (let [state (react/useState value)]
    (react/useEffect
      (fn []
        (let [container (if (empty? value)
                          (.querySelector js/document "div.input-select--dropdown button.input-select--option")
                          (.querySelector js/document "div.input-select--dropdown button.input-select--option[data-selected=true]"))
              listener  (fn [event] (if-let [on-key-down (:on-key-down select-props)]
                                      (on-key-down event select-props)
                                      (keydown-listener event select-props)))]            
          (.setTimeout js/window (fn [_] (.focus container)) 50)
          (.addEventListener js/document "keydown" listener)

          (fn []
            (.removeEventListener js/document "keydown" listener))))

      #js[])

    [:div (merge {:class         "input-select--dropdown"
                  :data-multiple multiple}
                 dropdown-override)
      [options-render state select-props]]))
    

(defn select-label [{:keys [value placeholder label-element] :as props}]
  (let [_value (:label value value)
        label-value (if (empty? _value)
                      (or placeholder "-")
                      (or _value placeholder "-"))]
    
    (if label-element
      [label-element props label-value]
      [:div label-value])))

(defn multi-select-label [{:keys [value placeholder _multiple]}]
  [:div {:style {:display "flex"
                 :gap "8px"
                 :align-items "center"}}
   (if (empty? value)
     (or placeholder "-")
     (map (fn [a]
            [:p  (:label a a)])
          value))])  


(defn select [{:keys [id title multiple override] :as props}]
  (let [popover-id (keyword "select.popover" (or id (random-uuid)))
        props      (assoc props :popover-id popover-id)]
        
    [:<> 
      (when title
        title)
      [:button (merge {:class    "input-select"
                       :on-click (fn [event]
                                   (when-not @(r/subscribe [:db/get-in [:popovers popover-id :state] false])
                                     (r/dispatch [:popover/open popover-id
                                                   {:content [dropdown-popover props]
                                                    :target  (.-target ^js event)
                                                    :align   [:bottom :left]
                                                    :anchor  [:top :left]
                                                    :width   :inherit}])))}
                                                                        
                      override)

         (if multiple 
           [multi-select-label props]
           [select-label props])
         [:i {:class ["fa-solid" "fa-caret-down"]}]]]))

(defn native-select [{:keys [id _value multiple placeholder on-select options] :as props}]
  (let [native-select-id (str "native.select-" (or id (random-uuid)))]
    [:div {:class "input-native-select"}
      [:select (merge {:id native-select-id
                       :multiple  multiple
                       :on-change (fn [event]
                                    (when on-select
                                      (on-select (-> ^js event .-target .-value reader/read-string))))}
                      (:override props))
       
        [:option {:value nil} (or placeholder "-")]
        (map (fn [option-props]
               [:option {:key   (:value option-props option-props)
                         :value (str option-props)
                         :class "input-select--option"}
                 (:label option-props option-props)])
             options)]
      [:i {:class ["fa-solid" "fa-caret-down"]}]]))
      
(defn view [props]
  (if (:mobile? props (mobile-device?))
    [native-select props]
    [select props]))
