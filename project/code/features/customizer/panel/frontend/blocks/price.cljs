
(ns features.customizer.panel.frontend.blocks.price
  (:require
    [re-frame.core :as r]
    ["react"       :as react]
    [ui.button     :as button]
    [ui.popup      :as popup]
   
    [features.customizer.panel.frontend.blocks.price.subs]
    [features.customizer.panel.frontend.blocks.price.effects]))
    

;; (defn- calc-total-cost! [formdata]
;;   (react/useEffect
;;     (fn []
;;       (try
;;         (when formdata 
;;           (time (r/dispatch [:customizer.part.price/calc! formdata])))
;;         (catch :default e
;;           (.log js/console "formula not ready")))
;;       (fn []))
;;     #js[formdata]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn- field-value [value]
  (cond
    (contains? value :label) (get value :label)
    (contains? value :qty)   (get value :value)
    (or (string? value)
        (int? value)) value
    :else (str value)))
  
(defn- render-formdata [data]
  [:<>
    (doall 
      (map (fn [[k {:keys [value] :as v}]]
             (when-not (= 0 value)
               ^{:key (str (random-uuid))}
               [:<>
                ;;  [:p (if-let [field @(r/subscribe [:x.dictionary/look-up k])]
                ;;         field k) ":"]
                 (if (and (map? v)
                          (not (contains? v :value)))
                   [:div {:style {:display               "grid"
                                  :gap                   "8px"
                                  :grid-template-columns "auto 1fr"}}
                     [render-formdata v]]
                   [:b (:prefix v) (field-value v) (:suffix v)])]))
           (dissoc data :look-cost :quantity :condition)))])

(defn- price-details-popup [part formdata]
  [popup/view 
     {:state    @(r/subscribe [:db/get-in [::aa]])
      :on-close (fn []
                  (r/dispatch [:db/dissoc-in [::aa]]))
      :style    {:z-index 2}
      :cover    {:z-index 1}}
   [:div
     [:div 
       [:p {:class "popup-desktop--title"} "Job Details"]
       [:button {:on-click #(r/dispatch [:db/dissoc-in [::aa]])
                 :style    {:position "absolute"
                            :top 0 :right 0}}
         [:i {:class "fa-solid fa-xmark"}]]]
     [:div {:style {:margin-bottom "15px"}}
       [:p {:style {:font-size  "1.1rem"
                    :text-align "center"}}
         (str (-> formdata :quantity :qty) " x " (:precursor part) " " (:name part))]
        
       [:span {:style {:color "#c8c8c8" :font-size "0.9rem"}} "Price contains surface preparation and powder coating"]]
     
     [:div {:style {:max-height            "50vh"
                    :display               "grid"
                    :gap                   "8px 30px"
                    :grid-template-columns "auto 1fr"}}
       [:p "Current surface: "] [:b (str (get-in formdata [:condition :label] "-"))]
       [:p "New look: "]        [:b (str (get-in part [:color :name]))]
       [render-formdata formdata]]]])

(defn- part-total-cost [edited-item]
  (let [price            @(r/subscribe [:customizer.package/get-total])
        formdata         (get edited-item "formdata")]
    ;; (calc-total-cost! formdata)

    [:<> 
      ;; [price-details-popup edited-item formdata]
      ;; [:div {:id    "customizer-total-price-btn"
      ;;        :style {:background "rgba(0, 0, 0, 0.35)" :color "white"}}]
      [button/view {:id    "customizer-total-price-btn"
                    :color "rgba(0, 0, 0, 0.35)"
                    :on-click #(r/dispatch [:db/assoc-in [::aa] true])
                    :style    {:text-align "center"
                               :color      "white"
                               :display    "flex"
                               :align-items "center"
                               :gap         "12px"}}
        [:i {:class "fa-solid fa-info"}]
        [:p "Total: " [:b "$" price]]]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [edited-item]
  [part-total-cost edited-item])