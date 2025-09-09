
(ns features.common.form.frontend.inputs.digit
  (:require 
    [re-frame.core        :as r]
    [ui.button            :as button]
    ["react"              :as react]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn- value! [{:keys [type value]} {:keys [formdata-path]}]
  (case type
    "num"       value
    "subscribe" (:qty @(r/subscribe [:db/get-in (concat formdata-path ["formdata"] value)]))
    0))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Subscriptions ----

(r/reg-sub
  :form.digit-input/value
  (fn [db [_ {:keys [value-path min value]}]]
    (get-in db value-path)))

;; ---- Subscriptions ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-fx
  :form.digit-input/dec!
  (fn [{:keys [db]} [_ current-value {:as   input-props
                                      :keys [prefix value value-path service-id]}]]
    (let [current-qty (:qty current-value)]
      (if (= 1 current-qty)
        {:dispatch   [:db/dissoc-in value-path]}
        {:dispatch-n [[:db/assoc-in value-path {:qty    (dec current-qty)
                                                :prefix prefix
                                                :value  (- (:value current-value) value)}]
                      (when service-id [:db/update-in value-path assoc :service-id service-id])]}))))


(r/reg-event-fx
 :form.digit-input/inc!
 (fn [{:keys [db]} [_ max-value current-value {:as   input-props
                                               :keys [prefix value value-path service-id]}]]
   (let [current-qty (:qty current-value)]
     
     {:dispatch-n [[:db/assoc-in value-path {:qty    (inc current-qty)
                                             :prefix prefix
                                             :value  (+ (:value current-value) value)}]
                   (when service-id [:db/update-in value-path assoc :service-id service-id])]})))

(r/reg-event-fx
  :form.digit-input/on-change!
  (fn [{:keys [db]} [_ value-path value service-id]]
    (if (zero? (:qty value))
      {:dispatch   [:db/dissoc-in value-path]}
      {:dispatch-n [[:db/assoc-in value-path value]
                    (when service-id [:db/update-in value-path assoc :service-id service-id])]})))

;; ---- Effects ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn- dec-button [value min-value input-props]
  (let [disable? (>= min-value (:qty value))]
    [button/view {:disabled disable?
                  :on-click #(r/dispatch [:form.digit-input/dec! value input-props])}
      [:i {:class "fa-solid fa-minus"}]]))    
      
(defn- input-field [value-path value min-value max-value
                    {:as   input-props
                     :keys [prefix service-id]}]

  (let [qty (:qty value min-value)]
    ;; ;; Update max-value
    (react/useEffect
      (fn []
        (when (< max-value qty)
          (r/dispatch [:db/assoc-in value-path {:qty (if (nil? max-value) 0 max-value) :value (* (:value value 0) max-value)}]))
        (fn []))
      #js[max-value])

    [:input {:value      qty
             :type       "number"
             :inputMode  "numeric"
             :class      "digit-input--field"
             :min        min-value
             :max        max-value
             :on-change  #(let [input-value (-> % .-target .-value int)]
                            (cond 
                              (< max-value input-value)
                              (r/dispatch [:form.digit-input/on-change! value-path
                                            {:qty    max-value 
                                             :prefix prefix
                                             :value  (* (:value input-props) max-value)}
                                           service-id])
                              (> min-value input-value)
                              (r/dispatch [:form.digit-input/on-change! value-path
                                           {:qty    min-value 
                                            :prefix prefix
                                            :value  (* (:value input-props) min-value)}
                                           service-id])
                              :else
                              (r/dispatch [:form.digit-input/on-change! value-path
                                           {:qty    input-value
                                            :prefix prefix
                                            :value  (* (:value input-props) input-value)}
                                           service-id])))}]))
                           
(defn- inc-button [value max-value input-props]
  (let [disable? (<= max-value (:qty value))]
    [button/view {:disabled disable?
                  :on-click #(r/dispatch [:form.digit-input/inc! max-value value input-props])}
      [:i {:class "fa-solid fa-plus"}]]))

(defn- digit-input [{:keys [title value-path default min max] :as input-props}]
  (let [min-value  (value! min input-props)
        max-value  (value! max input-props)
        value      @(r/subscribe [:form.digit-input/value input-props])]

     (react/useEffect
       (fn []
         (when (and default (not value))
           (r/dispatch [:db/assoc-in value-path {:qty   default
                                                 :value (* (:value input-props) default)}]))
         (fn []))
       #js[])

     [:<>
       (when title 
         title)  
       [:div {:class (str "digit-input--container " (:class input-props))}
         [dec-button  value min-value input-props]
         [input-field value-path value min-value max-value input-props]
         [inc-button  value max-value input-props]]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [config]
  [digit-input config])