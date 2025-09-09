
(ns features.customizer.checkout.frontend.views
  (:require 
   ["react" :as react]
   [re-frame.core :as r]

   [router.frontend.zero :as router]

   [ui.link :as link]
   [ui.popup :as popup]
   [ui.button :as button]
   [ui.stepper :as stepper]

   [features.customizer.checkout.frontend.effects]
   [features.common.stripe.frontend.views :as stripe]
   [features.customizer.blocks.local-storage :as local-storage]

   [features.customizer.checkout.frontend.header :as header]
   [features.customizer.checkout.frontend.blocks.details :as details]
   [features.customizer.checkout.frontend.blocks.cart-items :as cart-items]
   [features.customizer.checkout.frontend.blocks.contact :as contact]
   [features.customizer.checkout.frontend.blocks.delivery :as delivery]
   [features.customizer.checkout.frontend.blocks.payment :as payment]))
   
(defn STEPPER-CONFIG [processing]
  {:label  {:color "var(--irb-clr)"
            :disabled-color "gray"
            :selected-color "var(--irb-clr)"
            :style {:color "black"}}
   :back   {:color "#b4b4b4"
            :style {:color "black"}}
   :next   {:color "var(--irb-clr)"
            :style {:color "black"}}
   :finish {:color "var(--irb-clr)"
            :style {:color "black"}}
   
   :finish-step-label (if processing "Processing..." "Pay Securely")})            

(defn- checkout-steps [cart]
  (let [[processing set-processing] (react/useState false)]
    [:<>
      [stepper/view (STEPPER-CONFIG processing)
       "Summary" {:content [:<>
                             [cart-items/view cart]
                             [details/view cart]]
                  :valid?  @(r/subscribe [:checkout.summary/valid?])}
       "Contact" {:content [:<>
                             [contact/view]
                             [details/view cart]]
                  :valid?  @(r/subscribe [:checkout.contact/valid?])} 
       "Delivery" {:content [:<>
                              [delivery/view] 
                              [details/view cart]]
                   :valid?  @(r/subscribe [:checkout.delivery/valid?])}
       "Payment" {:content  [:<>
                              [payment/view set-processing]
                              [details/view cart]]
                  :valid?   (and (not processing) @(r/subscribe [:checkout.payment/valid?]))
                  :on-click #(when-not processing 
                               (stripe/submit))}]]))

          ;:on-click #(r/dispatch [:checkout.order/send!])
          ;:valid?   @(r/subscribe [:checkout.payment/valid?])}])

(defn dollar-to-cents [num]
  (when (number? num)
    (* num 100)))

(defn create-payment-intent [cart]
  ;; (.setItem js/localStorage "customizer-cart-content" cart)
  ;; (r/dispatch [:customizer.checkout/initialize-auto-save])
  (when-let [zip @(r/subscribe [:db/get-in [:postal-code] "98104"])]
    (when (not @(r/subscribe [:db/get-in [:stripe :client-secret]]))
      (let [total-price (reduce + (map :price (vals cart)))]
        (r/dispatch [:customizer.checkout/create-payment-intent
                     {:amount  (if (zero? total-price)
                                 1000
                                 (dollar-to-cents total-price))
                      :address {:line1 "920 5th Ave"
                                :city "Seattle"
                                :state "WA"
                                :postal-code "98104"
                                :country "US"}}])))))

(defn checkout-popup []
  [popup/view {:state @(r/subscribe [:db/get-in [:checkout :alert-popup]])
               :style {:background "#1a1a1a"
                       :border "1px solid #333"
                       :border-radius "16px"
                       :padding "32px"
                       :max-width "420px"
                       :width "90%"
                       :box-shadow "0 20px 40px rgba(0, 0, 0, 0.4)"
                       :text-align "center"}}
               
    [:<>
    
      [:div {:style {:margin-bottom "24px"}}
        [:img {:src "/logo/logo-good-size.png"
               :alt "Iron Rainbow Logo"
               :style {:width "140px"
                       :height "140px"
                       :margin "0 auto"}}]]
    
      [:h2 {:style {:margin "0 0 16px 0"
                    :font-size "24px"
                    :font-weight "600"
                    :color "#ffffff"
                    :line-height "1.2"}}
        "Secure Checkout Required"]
    
     ;; Description
      [:p {:style {:margin "0 0 24px 0"
                   :font-size "16px"
                   :color "#cccccc"
                   :line-height "1.5"
                   :max-width "400px"
                   :margin-left "auto"
                   :margin-right "auto"}}
        "To complete your order securely and provide you with order tracking, we need you to sign in to your account."]
    
     ;; Benefits list
      [:div {:style {:background "#2a2a2a"
                     :border "1px solid #444"
                     :border-radius "12px"
                     :padding "20px"
                     :margin "0 0 32px 0"
                     :text-align "left"}}
        [:h4 {:style {:margin "0 0 16px 0"
                      :font-size "16px"
                      :font-weight "600"
                      :color "#ffffff"}}
         "Why we need your account:"]
        [:ul {:style {:margin "0"
                      :padding-left "20px"
                      :color "#cccccc"}}
          [:li {:style {:margin-bottom "8px"
                        :font-size "14px"
                        :line-height "1.4"}}
           "📦 Track your order status in real-time"]
          [:li {:style {:margin-bottom "8px"
                        :font-size "14px"
                        :line-height "1.4"}}
           "💳 Secure payment processing"]
          [:li {:style {:margin-bottom "8px"
                        :font-size "14px"
                        :line-height "1.4"}}
           "📧 Receive order confirmations and updates"]
          [:li {:style {:margin-bottom "8px"
                        :font-size "14px"
                        :line-height "1.4"}}
           "🔄 Easy reordering and order history"]
          [:li {:style {:margin-bottom "0"
                        :font-size "14px"
                        :line-height "1.4"}}
           "🛡️ Protect your personal information"]]]
    
     ;; Buttons
      [:div {:style {:display "grid"
                     :gap     "12px"}}
        [link/view {:color    "var(--irb-clr)"
                    :mode     :primary
                    :href     "/login/customizer?redirect-url=/checkout"
                    
                    :style    {:min-width "120px"
                               :color "black"
                               :padding "12px 24px"}}
          "Sign in"]
        [button/view {:mode     :outlined
                      :type     :secondary
                      :disabled true
                      :on-click #(r/dispatch [:db/assoc-in [:checkout :alert-popup] false])
                      :style    {:min-width "120px"
                                 :padding "12px 24px"}}
          "Continue As Guest"]]]])

(defn view []  
  (let [cart        @(r/subscribe [:db/get-in [:cart :content]])
        checkout    @(r/subscribe [:db/get-in [:checkout]])]
    
    (react/useEffect (fn []
                       (create-payment-intent cart)
                       (when-not @(r/subscribe [:db/get-in [:user-profile]])
                         (r/dispatch [:db/assoc-in [:checkout :alert-popup] true]))
                       (when (nil? cart)
                         (r/dispatch [:local-storage/load-item [:cart :content] "customizer-cart-content"]))
                       (fn []))
                     #js[])
    ;; (watch-checkout checkout)
    [:<>
      [:div {:id "checkout"}
        [header/view]
        [checkout-steps cart]]
      [checkout-popup]]))