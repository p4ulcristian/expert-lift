(ns features.customizer.checkout.frontend.blocks.details
  (:require 
    ["react" :as react]
    [re-frame.core :as r]
    [ui.tooltip :as tooltip]
    [ui.text-field :as text-field]))

(defn cart->all-qty [{:keys [content]}]
  (reduce #(+ %1 (get-in %2 ["formdata" "quantity" :qty]))
          0
          content))

(defn to-fixed [num]
  (when (number? num)
    (.toFixed num 2)))

(defn cart-details [cart-data]
  [:div {:style {:margin                "15px 0px"
                 :display               "grid"
                 :grid-template-columns "1fr auto"}}
    [:span "Paint job:"]     [:b (:count cart-data)]
    [:span "Item Quantity:"] [:b (cart->all-qty cart-data)]])

(defn coupon-field []
  [text-field/view {:placeholder    "Coupon code"
                    :right-adornments [:button {:on-click #(.alert js/window "Coupon!🎉")}
                                        "Apply"]}])

(defn job-rows [cart-items]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "8px"}}
    (for [[job-id job-data] cart-items]
      ^{:key job-id}
      [:div {:style {:display "flex"
                     :justify-content "space-between"}}
       [:span {:style {:color "rgba(255, 255, 255, 0.8)"
                       :font-size "14px"}}
         (str (:name job-data) " (x" (get-in job-data ["formdata" "quantity" "qty"] 1) ")")]
       
       [:span {:style {:color "rgba(255, 255, 255, 0.9)"
                       :font-size "14px"}}
         (str "$"  (to-fixed (:price job-data)))]])])

(defn subtotal-row [{:keys [subtotal]}]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :margin-bottom "16px"}}
    [:span {:style {:color "rgba(255, 255, 255, 0.7)"
                    :font-size "14px"}}
     "Subtotal"]
    [:span {:style {:color "rgba(255, 255, 255, 0.9)"
                    :font-size "14px"}}
      (if subtotal 
       (str "$" (to-fixed subtotal))
       "Loading...")]])

(defn tax-row [{:keys [tax-percentage tax]}]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :margin-bottom "16px"}}
     (if tax-percentage
       [:span {:style {:color "rgba(255, 255, 255, 0.7)"
                       :font-size "14px"}}
    
         (str "Tax (" (to-fixed tax-percentage) "%)")]
       [:span {:style {:color "rgba(255, 255, 255, 0.7)"
                       :font-size "14px"}}
        "Tax (-%)"])
       
    [:span {:style {:color "rgba(255, 255, 255, 0.9)"
                    :font-size "14px"}}
     (if tax
       (str "$" (to-fixed tax))
       "Loading...")]])

(defn shipping-row [shipping]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :margin-bottom "16px"}}
    [:span {:style {:color "rgba(255, 255, 255, 0.7)"
                    :font-size "14px"}}
     "Shipping"]
    [:span {:style {:color "rgba(255, 255, 255, 0.9)"
                    :font-size "14px"}}
     (str "$" (to-fixed shipping))]])

(defn total-row [{:keys [total]}]
  [:div {:style {:border-top "1px solid rgba(255, 255, 255, 0.2)"
                 :padding-top "16px"
                 :display "flex"
                 :justify-content "space-between"}}
    [:span {:style {:color "rgba(255, 255, 255, 0.9)"
                    :font-size "18px"
                    :font-weight "600"}}
      "Total"]
    [:span {:style {:color "var(--irb-clr)"
                      :font-size "20px"
                      :font-weight "700"}}
     (if total
       (str "$" (to-fixed total))
       "Loading...")]])

(defn order-summary [cart-content]
  (let [;cart @(r/subscribe [:db/get-in [:cart]])
        stripe-data @(r/subscribe [:db/get-in [:stripe]])
        
        shipping 15.00] ; Fixed shipping
    
    [:div {:style {:background "rgba(0, 0, 0, 0.3)"
                   :border "1px solid rgba(255, 255, 255, 0.1)"
                   :border-radius "12px"
                   :padding "20px"
                   :margin-bottom "20px"}}
     
     [:h3 {:style {:color "rgba(255, 255, 255, 0.9)"
                   :font-size "16px"
                   :font-weight "600"
                   :margin-bottom "16px"
                   :display       "flex"
                   :justify-content "space-between"
                   :align-items   "center"
                   :gap           "8px"}}
      "Order Summary"
      [tooltip/view {:id     "tax-tooltip"
                     :align  [:top :center]
                     :anchor [:bottom :center]
                     :tooltip "In order to calculate the tax, we need to know your shipping address. Please enter it in the shipping address field."
                     :style {:width "200px"
                             :font-size "12px"
                             :margin-bottom "10px"
                             :box-shadow "0 0 10px 0 rgba(0, 0, 0, 0.1)"}}
           [:i {:class "fa-solid fa-circle-info"}]]]
     
     [job-rows cart-content]
     
     [:div {:style {:border-top "1px solid rgba(255, 255, 255, 0.2)"
                    :padding-top "16px"
                    :margin-top "16px"}}
      
      [subtotal-row (:tax-calculation stripe-data)]
      [tax-row (:tax-calculation stripe-data)]
      [shipping-row shipping]
      [total-row (:tax-calculation stripe-data)]]]))

(defn view [cart]
  [:div {:id    "checkout--order-summary"
         :class "checkout--bg-box"
         :data-force-blur true}
    [order-summary cart]])
     