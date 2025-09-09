(ns ui.distance-input
  (:require 
    [re-frame.api :as r]))
   
;; -----------------------------------------------------------------------------
;; ---- Utils ----

(def DEFAULT_VALUE {:unit "cm" :display-value 0 :stored-value 0})

(def to-mm-ratios
  {"cm"    #(* % 10)
   "m"     #(* % 1000)
   "inch"  #(* % 25.4)
   "ft/in" #(let [[feet inches] %
                  total-inches  (+ (* feet 12) inches)]
              (* total-inches 25.4))})

(def from-mm-ratios
  {"cm"    #(/ % 10)
   "m"     #(/ % 1000)
   "inch"  #(/ % 25.4)
   "ft/in" #(let [inches           (/ % 25.4)
                  feet             (int (/ inches 12))
                  remaining-inches (mod inches 12)]
              [feet remaining-inches])})

(defn convert-to-mm [value unit]
  (let [convert-fn (get to-mm-ratios unit)]
    (if convert-fn
      (convert-fn value)
      value)))

(defn convert-from-mm [value unit]
  (let [convert-fn (get from-mm-ratios unit)]
    (if convert-fn
      (convert-fn value)
      value)))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-db
  :distance-input.value/on-change!
  (fn [db [_ {:keys [value-path]} new-display-value]]
    (let [input-data       (get-in db value-path)
          unit             (:unit input-data "cm")
          new-stored-value (convert-to-mm new-display-value unit)]
      
      (assoc-in db value-path {:display-value new-display-value
                               :stored-value  new-stored-value
                               :unit          unit}))))

(r/reg-event-db
 :distance-input.unit/on-change!
 (fn [db [_ {:keys [value-path]} new-unit]]
   (let [input-data        (get-in db value-path)
         stored-value      (:stored-value input-data)
         new-display-value (convert-from-mm stored-value new-unit)]
     (assoc-in db value-path {:unit          new-unit
                              :stored-value  stored-value
                              :display-value new-display-value}))))

(r/reg-event-db
 :distance-input.feet-inch/on-change!
 (fn [db [_ {:keys [value-path]} key new-value]]
  (let [input-data        (get-in db value-path)
        [feet inch]       (:display-value input-data)
        new-display-value (if (= "feet" key)
                            [new-value inch]
                            [feet new-value])
        unit              (:unit input-data "cm")
        new-stored-value  (convert-to-mm new-display-value unit)]
    (assoc-in db value-path {:unit          unit
                             :stored-value  new-stored-value
                             :display-value new-display-value}))))
  
;; ---- Effects ----
;; -----------------------------------------------------------------------------

(defn handle-input-change [event input-props]
  (let [value             (-> ^js event .-target .-value js/parseFloat)
        new-display-value (if (js/Number.isNaN value) 0 value)]
    (r/dispatch-sync [:distance-input.value/on-change! input-props new-display-value])))

(defn handle-input-feet-inch-change [input-props key event]
  (let [value               (-> ^js event .-target .-value js/parseFloat)
        new-displayed-value (if (js/Number.isNaN value) 0 value)]
    (r/dispatch-sync [:distance-input.feet-inch/on-change! input-props key new-displayed-value])))

(defn handle-unit-change [event input-props]
  (let [new-unit (-> ^js event .-target .-value)]
    (r/dispatch [:x.db/set-item! [::selected-unit] new-unit])
    (r/dispatch-sync [:distance-input.unit/on-change! input-props new-unit])))
 
(defn feet-inch-input [input-props input-value]
  (let [[feet-value inch-value] (:display-value input-value)]
    [:div {:class "e-feet-inch-input-container"}
      [:input {:type      "number"
               :value     feet-value
               :on-change #(handle-input-feet-inch-change input-props "feet" %)}]
   
      [:p {:style {:align-self "center"}} "|"]
   
      [:input {:type      "number"
               :value     inch-value
               :on-change #(handle-input-feet-inch-change input-props "inch" %)}]]))     

(defn- default-input [input-props {:keys [display-value]}]
  [:input {:type      "number"
           :value     display-value
           :on-change #(handle-input-change % input-props)}])

(defn- input-handler [{:keys [unit] :as input-value} input-props]
  [:div {:class "e-element-input"
         :style {:width         "100px"
                 :border-radius "6px"}}
    (case unit
      "ft/in" [feet-inch-input input-props input-value]
      [default-input input-props input-value])])

(defn- unit-selector [{:keys [unit]} input-props]
  [:select {:class     "e-element-input"
            :value     unit
            :style     {:border-radius "6px"
                        :cursor        "pointer"}
            :on-change #(handle-unit-change % input-props)}
    [:option {:value "cm"}    "cm"]
    [:option {:value "m"}     "m"]
    [:option {:value "inch"}  "inch"]
    [:option {:value "ft/in"} "ft/in"]])
   

(defn view [{:keys [value-path] :as input-props}]
  (let [input-value   @(r/subscribe [:x.db/get-item value-path DEFAULT_VALUE])]
    
    [:div {:class "e-distance-input"}
     [input-handler input-value input-props]
     [unit-selector input-value input-props]])) 