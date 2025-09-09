(ns features.customizer.checkout.frontend.blocks.stripe
  (:require
   ["react" :as react]
   [re-frame.core :as r]
   [app.frontend.request :as request]
   [router.frontend.zero :as router]
   [features.common.stripe.frontend.views :as stripe-views]

   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   ["@stripe/stripe-js" :as stripe-js]
   ["@stripe/react-stripe-js" :as react-stripe-js]
   [ui.button :as button]))

;; Initialize Stripe
(def stripe-promise (stripe-js/loadStripe "pk_test_51RZmyT02Xidq6KZyzrB08vwwJhSTZZhCifBRMhoQCRCDdp5YObskhpVLB9fkiqiHigW1mVjc5bQpbEnBp2C4PTy200AsvTrE2y"))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn error-notification [message]
  (r/dispatch [:notifications/error! "payment-failed-error" message 2500]))

(def IRB-COLOR "hsl(50, 100%, 52%)")
(def PRIM-COLOR "#fff")
(def BG-COLOR "rgb(0, 0, 0, 0.3)")

(def stripe-appearance
  #js{"theme"     "night"    
      "variables" #js{"colorPrimary"   PRIM-COLOR
                      "colorBackground" BG-COLOR
                      "borderColor" IRB-COLOR
                      "colorText"       "#fff"

                      "borderRadius" "12px"
                      "colorDanger" "#df1b41"
                      "colorSuccess" "#0A7B83"
                      "colorWarning" "#ffd144"}
      "rules" #js{".Input" #js{:backgroundColor BG-COLOR}
                  ".Error" #js{:color "#df1b41"}
                 
                  ".Input:focus" #js{:borderColor IRB-COLOR
                                     :boxShadow (str "0px 0px 4px " IRB-COLOR)}}})
                                                    
  

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; Form Submission Handler
(defn handle-form-submit [event stripe elements order-data on-success on-error set-error set-loading]
  ;; Prevent default form submission
  (.preventDefault ^js event)
  
  ;; Don't submit if already processing
  (if (or (not stripe) (not elements))
    (do
      (error-notification "Cannot submit - processing or missing stripe/elements")
      (js/console.log "Cannot submit - processing or missing stripe/elements"))
  
    (do
      (.submit elements)
      (set-loading true)
      (set-error nil)
  
      (go
        (try
          ;; Confirm the payment
          (let [result (<p! (.confirmPayment stripe
                              #js{:elements      elements
                                  :clientSecret @(r/subscribe [:db/get-in [:stripe :client-secret]])
                                  ;; :redirect "if_required"
                                  :confirmParams #js{:return_url (str js/window.location.origin "/orders")}}))]

            ;; Check for errors
            (if-let [error (.-error result)]
              ;; Handle error case
              (do
                (set-error (.-message error))
                (error-notification (.-message error))
                (js/console.error "Payment failed:" error))
          
              ;; If no error property, the customer will be redirected
              (do 
                (on-success result)
                (js/console.log "Payment processed - redirecting in 3 seconds..."))))
      
          (catch js/Error err
            (.log js/console "err" err)
            (error-notification (str "An unexpected error occurred: " (.-message err)))
            (set-error (str "An unexpected error occurred: " (.-message err))))
      
          (finally
            (set-loading false)))))))

;; Elements Provider Options
(defn get-elements-options [{:keys [client-secret]}]
  #js{:clientSecret client-secret
      :appearance   stripe-appearance})

(defn error-message [error]
  (when error
    [:div {:style {:color "#ef4444"
                   :margin-top "12px"
                   :font-size "14px"
                   :text-align "center"}}
     error]))

;; Payment Form Component
(defn payment-form [{:keys [order-data line-items on-success on-error]}]
  (let [stripe   (react-stripe-js/useStripe)
        elements (react-stripe-js/useElements)
        [error set-error] (react/useState nil)
        [loading set-loading] (react/useState false)]
    
    [:form {:id        "payment-form"
            :on-submit #(handle-form-submit % stripe elements order-data on-success on-error set-error set-loading)
            :style     {:margin-top "20px"}}
         
      [:> react-stripe-js/PaymentElement]
     
      [error-message error]]))

(defn header []
  [:div {:style {:text-align "center"
                 :margin-bottom "24px"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :justify-content "center"
                  :gap "8px"
                  :margin-bottom "12px"}}
    [:span {:style {:color "rgba(255, 255, 255, 0.8)"
                    :font-size "1rem"
                    :font-weight "500"}}
     "Payment options"]]
    
   [:p {:style {:font-size "0.9rem"
                :color "rgba(255, 255, 255, 0.6)"}}
    "Complete your order securely"]])

;; Inline Payment Component
(defn payment-section [{:keys [on-success on-error on-start on-end]}]
  (let [[loading set-loading] (react/useState false)]
    [:div {:id    "payment-container"
           :style {:background    "rgba(0, 0, 0, 0.3)"
                   :border        "1px solid rgba(255, 255, 255, 0.1)"
                   :border-radius "12px"
                   :padding       "15px"
                   :margin-top    "20px"}}
         
       [header]
   
       [:> react/Suspense {:fallback [:div "Loading..."]}
         (when-let [stripe-data @(r/subscribe [:db/get-in [:stripe]])]
           [stripe-views/view {:client-secret (:client-secret stripe-data)
                               :on-success    on-success
                               :on-error      #(r/dispatch [:notifications/error! "payment-failed-error" (.-message %) 2500])
                               :on-start      on-start
                               :on-end        on-end}])]]))
              