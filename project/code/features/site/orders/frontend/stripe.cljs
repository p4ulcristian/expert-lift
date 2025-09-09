(ns features.site.orders.frontend.stripe
  (:require
   [zero.frontend.react :as react]
   [app.frontend.request :as request]
   ["@stripe/stripe-js" :as stripe-js]
   ["@stripe/react-stripe-js" :as react-stripe-js]))

;; Initialize Stripe
(def stripe-promise (stripe-js/loadStripe "pk_test_51RZmyT02Xidq6KZyzrB08vwwJhSTZZhCifBRMhoQCRCDdp5YObskhpVLB9fkiqiHigW1mVjc5bQpbEnBp2C4PTy200AsvTrE2y"))

;; Error Handling
(defn handle-payment-error [error on-error set-error-message]
  (let [error-message (.-message error)]
    (set-error-message error-message)
    (when on-error
      (on-error {:message error-message}))))

;; Payment Confirmation
(defn confirm-payment [stripe elements client-secret order-id on-success on-error set-error-message]
  (-> (.submit elements)
      (.then (fn []
              (.confirmPayment stripe
                             #js {:elements elements
                                  :clientSecret client-secret
                                  :confirmParams #js {:return_url (str "http://" js/window.location.host "/orders/" order-id)}})))
      (.then (fn [result]
              (if (.-error result)
                (handle-payment-error (.-error result) on-error set-error-message)
                (when on-success
                  (on-success {:id (.-id result)})))))))

;; Payment Processing
(defn process-payment [stripe elements order-id on-success on-error set-error-message set-loading]
  (set-loading true)
  (request/pathom
   {:query `[(site/create-payment-intent {:amount 2999 :currency "usd" :order-id ~order-id})]
    :callback (fn [response]
                (println "response" response)
                (let [client-secret (get-in response [:site/create-payment-intent :client_secret])]
                  (if client-secret
                    (confirm-payment stripe elements client-secret order-id on-success on-error set-error-message)
                    (do
                      (set-error-message "Failed to create payment intent")
                      (when on-error
                        (on-error {:message "Failed to create payment intent"}))
                      (set-loading false)))))}))

;; Form Submission Handler
(defn handle-form-submit [event stripe elements order-id on-success on-error set-error-message set-loading]
  (when event
    (.preventDefault ^js event))
  
  (when elements
    (process-payment stripe elements order-id on-success on-error set-error-message set-loading)))

;; Button Styles
(defn get-button-styles [loading]
  {:background "linear-gradient(135deg, #10b981 0%, #059669 100%)"
   :color "white"
   :border "none"
   :border-radius "8px"
   :padding "12px 24px"
   :font-weight "600"
   :font-size "0.9rem"
   :cursor (if loading "not-allowed" "pointer")
   :opacity (if loading 0.6 1)
   :transition "all 0.2s ease"
   :box-shadow "0 4px 12px rgba(16, 185, 129, 0.3)"
   :width "100%"})

;; Form Styles
(defn get-form-styles []
  {:max-width "500px"
   :margin "0 auto"
   :padding "20px"})

;; Error Message Styles
(defn get-error-styles []
  {:color "#ef4444"
   :margin-top "12px"
   :font-size "0.9rem"})

;; Payment Element Options
(defn get-payment-element-options []
  #js {:layout #js {:type "tabs"
                    :defaultCollapsed false}})

;; Elements Provider Options
(defn get-elements-options []
  #js {:mode "payment"
       :amount 2999
       :currency "usd"
       :appearance #js {:theme "stripe"
                        :variables #js {:colorPrimary "#10b981"
                                        :colorBackground "#ffffff"
                                        :colorText "#1f2937"
                                        :colorDanger "#ef4444"
                                        :fontFamily "system-ui, -apple-system, sans-serif"
                                        :spacingUnit "4px"
                                        :borderRadius "8px"}}})

;; Modal Styles
(defn get-modal-styles []
  {:position "fixed"
   :top 0
   :left 0
   :right 0
   :bottom 0
   :background "rgba(0,0,0,0.5)"
   :display "flex"
   :align-items "center"
   :justify-content "center"
   :z-index 1000})

;; Modal Content Styles
(defn get-modal-content-styles []
  {:background "white"
   :border-radius "12px"
   :padding "32px"
   :max-width "400px"
   :width "90%"
   :box-shadow "0 20px 40px rgba(0,0,0,0.1)"})

;; Order Summary Styles
(defn get-order-summary-styles []
  {:background "#f8fafc"
   :border "1px solid #e2e8f0"
   :border-radius "8px"
   :padding "16px"
   :margin-bottom "20px"})

;; Checkout Form Component
(defn checkout-form [{:keys [order-id on-success on-error]}]
  (let [stripe (react-stripe-js/useStripe)
        elements (react-stripe-js/useElements)
        [error-message set-error-message] (react/use-state nil)
        [loading set-loading] (react/use-state false)]
    
    [:form {:on-submit #(handle-form-submit % stripe elements order-id on-success on-error set-error-message set-loading)
            :style (get-form-styles)}
     [:div {:style {:margin-bottom "20px"}}
      [:> react-stripe-js/PaymentElement
       {:options (get-payment-element-options)}]]
     
     [:button {:type "submit"
               :disabled (or (not stripe) (not elements) loading)
               :style (get-button-styles loading)}
      (if loading
        "Processing..."
        "Pay Now")]
     
     (when error-message
       [:div {:style (get-error-styles)}
        error-message])]))

;; Payment Modal Component
(defn payment-modal [{:keys [order visible? on-close on-success on-error]}]
  (when visible?
    [:div {:style (get-modal-styles)}
     [:div {:style (get-modal-content-styles)}
      [:div {:style {:text-align "center"
                     :margin-bottom "24px"}}
       [:h2 {:style {:font-size "1.5rem"
                     :font-weight "700"
                     :color "#1f2937"
                     :margin-bottom "8px"}}
        "Payment"]]
      
      [:div {:style (get-order-summary-styles)}
       [:div {:style {:display "flex"
                      :justify-content "space-between"
                      :margin-bottom "8px"}}
        [:span "Order Total:"]
        [:span {:style {:font-weight "600"}} "$29.99"]]
       [:div {:style {:display "flex"
                      :justify-content "space-between"
                      :font-size "0.9rem"
                      :color "#6b7280"}}
        [:span "Currency:"]
        [:span "USD"]]]
      
      [:> react-stripe-js/Elements {:stripe stripe-promise
                                    :options (get-elements-options)}
       [checkout-form {:order-id (:id order)
                       :on-success on-success
                       :on-error on-error}]]]]))