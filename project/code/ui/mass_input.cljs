
(ns ui.mass-input
  (:require 
    [re-frame.api :as r]))
   
;; -----------------------------------------------------------------------------
;; ---- Utils ----

(def DEFAULT_VALUE {:unit "g" :display-value 0 :stored-value 0})

(def to-grams-ratios
  {"kg"    #(* % 1000) ;; Kilograms to grams
   "lbs"   #(* % 453.592)
   "lb/oz" #(let [[lb oz] %
                  total-ounces (+ (* lb 16) oz)]
              (* total-ounces 28.3495)) ;; Pounds/Ounces to grams
   "oz"    #(* % 28.3495) ;; Ounces to grams
   "g"     identity}) ;; Grams to grams (no conversion needed)

(def from-grams-ratios
  {"kg"    #(/ % 1000) ;; Grams to kilograms
   "lbs"   #(/ % 453.592) ;; Grams to pounds
   "lb/oz" #(let [total-ounces (/ % 28.3495)
                  lb           (int (/ total-ounces 16))
                  remaining-oz (mod total-ounces 16)]
              [lb remaining-oz]) ;; Grams to pounds and ounces
   "oz"    #(/ % 28.3495) ;; Grams to ounces
   "g"     identity}) ;; Grams to grams (no conversion needed)


(defn convert-to-mm [value unit]
  (let [convert-fn (get to-grams-ratios unit)]
    (if convert-fn
      (convert-fn value)
      value)))

(defn convert-from-mm [value unit]
  (let [convert-fn (get from-grams-ratios unit)]
    (if convert-fn
      (convert-fn value)
      value)))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-db
  :mass-input.value/on-change!
  (fn [db [_ {:keys [value-path]} new-display-value]]
    (let [input-data       (get-in db value-path)
          unit             (:unit input-data "g")
          new-stored-value (convert-to-mm new-display-value unit)]
      
      (assoc-in db value-path {:display-value new-display-value
                               :stored-value  new-stored-value
                               :unit          unit}))))

(r/reg-event-db
 :mass-input.unit/on-change!
 (fn [db [_ {:keys [value-path]} new-unit]]
   (let [input-data        (get-in db value-path)
         stored-value      (:stored-value input-data)
         new-display-value (convert-from-mm stored-value new-unit)]
     (assoc-in db value-path {:unit          new-unit
                              :stored-value  stored-value
                              :display-value new-display-value}))))

(r/reg-event-db
 :mass-input.feet-inch/on-change!
 (fn [db [_ {:keys [display-value value-path]} key new-value]]
  (let [input-data        (get-in db value-path)
        [lbs oz]          display-value
        new-display-value (if (= "lbs" key)
                            [new-value oz]
                            [lbs new-value])
        unit              (:unit input-data "g")
        new-stored-value  (convert-to-mm new-display-value unit)]
    (assoc-in db value-path {:unit          unit
                             :stored-value  new-stored-value
                             :display-value new-display-value}))))
  
;; ---- Effects ----
;; -----------------------------------------------------------------------------

(defn handle-input-change [event input-props]
  (let [value             (-> ^js event .-target .-value js/parseFloat)
        new-display-value (if (js/Number.isNaN value) 0 value)]
    (r/dispatch-sync [:mass-input.value/on-change! input-props new-display-value])))

(defn handle-input-feet-inch-change [input-props key event]
  (let [value               (-> ^js event .-target .-value js/parseFloat)
        new-displayed-value (if (js/Number.isNaN value) 0 value)]
    (r/dispatch-sync [:mass-input.feet-inch/on-change! input-props key new-displayed-value])))

(defn handle-unit-change [event input-props]
  (let [new-unit (-> ^js event .-target .-value)]
    (r/dispatch [:x.db/set-item! [::selected-unit] new-unit])
    (r/dispatch-sync [:mass-input.unit/on-change! input-props new-unit])))
 
(defn feet-inch-input [input-props input-value]
  (let [[feet-value inch-value] (:display-value input-value)]
    [:div {:class "e-feet-inch-input-container"}
      [:input {:type      "number"
               :value     feet-value
               :on-change #(handle-input-feet-inch-change input-props "lbs" %)}]
   
      [:p {:style {:align-self "center"}} "|"]
   
      [:input {:type      "number"
               :value     inch-value
               :on-change #(handle-input-feet-inch-change input-props "oz" %)}]]))     

(defn- default-input [input-props input-value]
  [:input {:type      "number"
           :value     (:display-value input-value)
           :on-change #(handle-input-change % input-props)}])

(defn- input-handler [{:keys [unit] :as input-value} input-props]
  [:div {:class "e-element-input"
         :style {:border-radius "6px"}}
    (case unit
      "lb/oz" [feet-inch-input input-props input-value]
      [default-input input-props input-value])])

(defn- unit-selector [{:keys [unit]} input-props]
  [:select {:class     "e-element-input"
            :value     unit
            :style     {:border-radius "6px"
                        :cursor        "pointer"}
            :on-change #(handle-unit-change % input-props)}
    [:option {:value "g"}     "g"]
    [:option {:value "kg"}    "kg"]
    [:option {:value "oz"}    "oz"]
    [:option {:value "lbs"}    "lbs"]
    [:option {:value "lb/oz"} "lbs/oz"]])
   

(defn view [{:keys [value-path] :as input-props}]
  (let [input-value   @(r/subscribe [:x.db/get-item value-path DEFAULT_VALUE])]
    
    [:div {:class "e-distance-input"}
     [input-handler input-value input-props]
     [unit-selector input-value input-props]])) 