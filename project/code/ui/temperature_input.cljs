(ns ui.temperature-input
  (:require 
    [re-frame.api :as r]))

(defonce default-value {:display-value 0  
                        :unit          "C"
                        :stored-value  0})
;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn celsius-to-fahrenheit [c]
  (+ (* c 1.8) 32))

(defn fahrenheit-to-celsius [f]
  (/ (- f 32) 1.8))

(defn convert-to-stored [value unit]
  (case unit
    "C" value
    "F" (fahrenheit-to-celsius value)
    value))

(defn convert-from-stored [value unit]
  (case unit
    "C" value
    "F" (celsius-to-fahrenheit value)
    value))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-db
 :temperature-input.value/on-change!
 (fn [db [_ {:keys [value-path]} new-display-value]]
   (let [input-value (get-in db value-path)
         unit             (:unit input-value "C")
         new-stored-value (convert-to-stored new-display-value unit)]
     (assoc-in db value-path {:display-value new-display-value
                              :unit          unit
                              :stored-value  new-stored-value}))))


(r/reg-event-db
 :temperature-input.unit/on-change!
 (fn [db [_ {:keys [value-path]} new-unit]]
   (let [input-data         (get-in db value-path)
         stored-value      (:stored-value input-data)
         new-display-value (convert-from-stored stored-value new-unit)]
    
     (assoc-in db value-path {:display-value new-display-value
                              :unit          new-unit
                              :stored-value  stored-value}))))


;; ---- Effects ----
;; -----------------------------------------------------------------------------

(defn handle-input-change [input-props e]
  (let [new-display-value (js/parseFloat (.-value (.-target ^js e)))]
    (r/dispatch-sync [:temperature-input.value/on-change! input-props new-display-value])))

(defn handle-unit-change [input-props e]
  (let [new-unit (.-value (.-target ^js e))]
    (r/dispatch-sync [:temperature-input.unit/on-change! input-props new-unit])))

(defn view [{:keys [value-path] :as input-props}]
  (let [input-data @(r/subscribe [:x.db/get-item value-path default-value])]
   [:div {:class "e-distance-input"
          :style {:display "grid" :grid-template-columns "50px 50px"}}
     [:input {:type      "number"
              :class     "e-element-input"
              :style     {:border-radius "6px"}
              :value     (:display-value input-data)
              :on-change #(handle-input-change input-props %)}]
     [:select {:class     "e-element-input"
               :value     (:unit input-data)
               :style     {:border-radius "6px"
                           :cursor "pointer"}
               :on-change #(handle-unit-change input-props %)}
       [:option {:value "C"} "°C"]
       [:option {:value "F"} "°F"]]]))
      