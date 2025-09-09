(ns features.flex.parts-pricing.frontend.pricing
  (:require
   [features.flex.parts-pricing.frontend.request :as parts-request]
   [clojure.string :as str]
   [reagent.core :as r]
   [ui.button :as button]
   [ui.text-field :as text-field]))

;; Remove global state - each component will have its own
;; State for pricing form - now created per component
(defn create-pricing-state []
  (r/atom {:basic nil
           :basic+ nil
           :pro nil
           :pro+ nil
           :loading false
           :error nil
           :success nil}))

;; Utils
(defn format-currency [value]
  (if (and value (not (js/isNaN value)))
    (str "$" (.toFixed (js/parseFloat value) 2))
    "$0.00"))

(defn parse-currency [value]
  (when value
    (let [cleaned (-> value
                     (str/replace #"[$,]" "")
                     (str/trim))]
      (when-not (empty? cleaned)
        (js/parseFloat cleaned)))))

;; Save pricing function
(defn save-pricing! [pricing-state part-id workspace-id is-active on-success]
  (swap! pricing-state assoc :loading true :error nil :success nil)
  
  (let [pricing-data {:part-id part-id
                     :workspace-id workspace-id
                     :price-basic (parse-currency (:basic @pricing-state))
                     :price-basic-plus (parse-currency (:basic+ @pricing-state))
                     :price-pro (parse-currency (:pro @pricing-state))
                     :price-pro-plus (parse-currency (:pro+ @pricing-state))
                     :is-active (boolean is-active)}]
    
    (parts-request/save-part-pricing
     workspace-id
     pricing-data
     (fn [response]
       (swap! pricing-state assoc :loading false)
       (let [result (:workspace-parts/save-pricing response)]
         (if (:success result)
           (do
             (swap! pricing-state assoc :success (:message result))
             (when on-success (on-success response))
             ;; Clear success message after 3 seconds
             (js/setTimeout #(swap! pricing-state assoc :success nil) 3000))
           (swap! pricing-state assoc :error (or (:error result) "Failed to save pricing"))))))))

;; Auto-save function that can be called externally
(defn auto-save-pricing! [pricing-state part-id workspace-id is-active on-success]
  (save-pricing! pricing-state part-id workspace-id is-active on-success))

;; Load existing pricing from part data (no separate query needed)
(defn load-pricing-from-part! [pricing-state part-data]
  (when-let [pricing (:pricing part-data)]
    (swap! pricing-state merge {:basic (when (> (:basic pricing) 0) (str (:basic pricing)))
                               :basic+ (when (> (:basic-plus pricing) 0) (str (:basic-plus pricing)))
                               :pro (when (> (:pro pricing) 0) (str (:pro pricing)))
                               :pro+ (when (> (:pro-plus pricing) 0) (str (:pro-plus pricing)))})))

;; Price input component - compact version
(defn price-input [{:keys [label value on-change placeholder]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "4px"}}
   [:label {:style {:font-weight "400"
                   :color "var(--muted-clr)"
                   :font-size "0.75rem"}}
    label]
   [text-field/view {:value value
                    :placeholder (or placeholder "0.00")
                    :type "number"
                    :style {:width "100%"
                           :font-size "0.85rem"}
                    :on-change on-change
                    :left-adornment [:span {:style {:color "var(--muted-clr)"
                                                   :font-weight "400"
                                                   :font-size "0.8rem"}} "$"]
                    :override {:style {:padding "6px 8px"
                                     :border "1px solid #e5e7eb"
                                     :border-radius "4px"}}}]])

;; Compact pricing form component
(defn pricing-form [{:keys [part-id workspace-id  is-active on-active-change on-success pricing-state]}]
  (let [state @pricing-state]
    [:div {:style {:padding "12px"
                   :background-color "#f9fafb"
                   :border-radius "6px"
                   :border "1px solid #e5e7eb"}}
     [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :align-items "center"
                   :margin-bottom "12px"}}
      [:div
       [:h4 {:style {:margin "0"
                    :color "var(--text-clr)"
                    :font-size "0.9rem"
                    :font-weight "500"}}
        "Price Groups"]
       [:p {:style {:margin "0"
                   :color "var(--muted-clr)"
                   :font-size "0.75rem"}}
        "Set pricing for customer tiers"]]
      
      ;; Compact action buttons
      [:div {:style {:display "flex"
                    :gap "8px"}}
       [button/view {:mode :clear
                    :type :secondary
                    :style {:font-size "0.8rem"
                           :padding "4px 8px"}
                    :on-click #(do
                                (swap! pricing-state assoc 
                                      :basic nil :basic+ nil :pro nil :pro+ nil
                                      :error nil :success nil))
                    :disabled (:loading state)}
        "Reset"]
       
       [button/view {:mode :filled
                    :type :primary
                    :style {:font-size "0.8rem"
                           :padding "4px 12px"}
                    :disabled (or (:loading state)
                                 (not (some identity [(:basic state) (:basic+ state) 
                                                     (:pro state) (:pro+ state)])))
                    :on-click #(save-pricing! pricing-state part-id workspace-id is-active 
                                             (fn [response] 
                                               (when on-success (on-success response))
                                               ;; Update the active state in parent component
                                               (when on-active-change (on-active-change is-active))))}
        (if (:loading state) "Saving..." "Save")]]]
     
     ;; Compact price inputs - horizontal layout
     [:div {:style {:display "grid"
                   :grid-template-columns "1fr 1fr 1fr 1fr"
                   :gap "12px"
                   :margin-bottom "8px"}}
      [price-input {:label "Basic"
                   :value (:basic state)
                   :placeholder "0"
                   :on-change #(swap! pricing-state assoc :basic %)}]
      
      [price-input {:label "Basic+"
                   :value (:basic+ state)
                   :placeholder "0"
                   :on-change #(swap! pricing-state assoc :basic+ %)}]
      
      [price-input {:label "Pro"
                   :value (:pro state)
                   :placeholder "0"
                   :on-change #(swap! pricing-state assoc :pro %)}]
      
      [price-input {:label "Pro+"
                   :value (:pro+ state)
                   :placeholder "0"
                   :on-change #(swap! pricing-state assoc :pro+ %)}]]
     
     ;; Compact messages
     (when (:error state)
       [:div {:style {:padding "6px 8px"
                     :background-color "#fee2e2"
                     :border "1px solid #fca5a5"
                     :border-radius "4px"
                     :color "#dc2626"
                     :font-size "0.75rem"
                     :margin-top "8px"}}
        (:error state)])
     
     (when (:success state)
       [:div {:style {:padding "6px 8px"
                     :background-color "#d1fae5"
                     :border "1px solid #86efac"
                     :border-radius "4px"
                     :color "#065f46"
                     :font-size "0.75rem"
                     :margin-top "8px"}}
        (:success state)])]))

;; Component to be used in parts view - no extra wrapper
(defn pricing-section [{:keys [part-id workspace-id part-data  on-auto-save]}]
  (let [pricing-state (create-pricing-state)]
    (r/create-class
     {:component-did-mount
      (fn []
        (when part-data
          (load-pricing-from-part! pricing-state part-data))
        ;; Register auto-save function with parent component
        (when on-auto-save
          (on-auto-save (partial auto-save-pricing! pricing-state part-id workspace-id))))
      
      :component-did-update
      (fn [_ [_ prev-props]]
        (when (not= (:part-data prev-props) part-data)
          (load-pricing-from-part! pricing-state part-data))
        ;; Re-register if part-id changed
        (when (and on-auto-save (not= (:part-id prev-props) part-id))
          (on-auto-save (partial auto-save-pricing! pricing-state part-id workspace-id))))
      
      :reagent-render
      (fn [{:keys [part-id workspace-id part-data is-active on-active-change on-success]}]
        [pricing-form {:part-id part-id
                      :workspace-id workspace-id
                      :part-data part-data
                      :is-active is-active
                      :on-active-change on-active-change
                      :on-success on-success
                      :pricing-state pricing-state}])})))