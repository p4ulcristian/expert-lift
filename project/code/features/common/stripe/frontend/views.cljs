
(ns features.common.stripe.frontend.views
  (:require
    ["react" :as react]
    [ui.popup :as popup]
    [cljs.core.async :refer [go]]
    [cljs.core.async.interop :refer-macros [<p!]]
    ["@stripe/stripe-js" :as stripe-js]
    ["@stripe/react-stripe-js" :as react-stripe-js]
    [features.flex.processes.frontend.process :as process]))

;; Initialize Stripe
(def stripe-promise (stripe-js/loadStripe "pk_test_51RZmyT02Xidq6KZyzrB08vwwJhSTZZhCifBRMhoQCRCDdp5YObskhpVLB9fkiqiHigW1mVjc5bQpbEnBp2C4PTy200AsvTrE2y"))

(def FORM-ID (str "payment-form-" (random-uuid)))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(def IRB-COLOR "hsl(50, 100%, 52%)")
(def PRIM-COLOR "#fff")
(def BG-COLOR "rgb(0, 0, 0, 0.3)")

(def stripe-appearance
  #js{"theme"     "night"    
      "variables" #js{"colorPrimary"    PRIM-COLOR
                      "colorBackground" BG-COLOR
                      "borderColor"     IRB-COLOR
                      "colorText"       "#fff"

                      "borderRadius" "12px"
                      "colorDanger"  "#df1b41"
                      "colorSuccess" "#0A7B83"
                      "colorWarning" "#ffd144"}
      "rules" #js{".Input" #js{:backgroundColor BG-COLOR}
                  ".Error" #js{:color "#df1b41"}
                 
                  ".Input:focus" #js{:borderColor IRB-COLOR
                                     :boxShadow (str "0px 0px 4px " IRB-COLOR)}}})
                                                    
(defn get-elements-options [{:keys [client-secret]}]
  #js{:clientSecret client-secret
      :appearance   stripe-appearance})

(defn submit []
  (let [form (js/document.getElementById FORM-ID)]
    (.requestSubmit form)))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn confirm-payment [stripe elements config]
  (.confirmPayment stripe
      #js{:elements      elements
          :clientSecret  (:client-secret config)
          :confirmParams #js{:return_url (str js/window.location.origin "/orders")}}))

;; Form Submission Handler
(defn handle-form-submit [event stripe elements set-processing config]
  ;; Prevent default form submission
  (.preventDefault ^js event)
  
  ;; Don't submit if already processing
  (if (or (not stripe) (not elements))
    (js/console.log "Cannot submit - processing or missing stripe/elements")
    
    (do
      ;; Validate the form
      (.submit elements)
      (set-processing true)
      (when-let [on-start (:on-start config)]
        (on-start))
      
      (go
        (try
          ;; Confirm the payment
          (let [result (<p! (confirm-payment stripe elements config))]

            ;; Check for errors
            (if-let [error (.-error result)]
              ;; Handle error case
              (when-let [on-error (:on-error config)]
                (on-error error))
              ;; If no error property, the customer will be redirected
              (when-let [on-success (:on-success config)]
                (set-processing "done")
                (on-success result))))
          
          (catch js/Error err
            (when-let [on-error (:on-error config)]
              (on-error err)))
          
          (finally
            (when-let [on-end (:on-end config)]
              (on-end))
            (set-processing false)))))))

;; Payment Form Component
(defn payment-form [payment-config processing set-processing components]
  (let [stripe   (react-stripe-js/useStripe)
        elements (react-stripe-js/useElements)]
        

    [:form {:id        FORM-ID
            :on-submit #(when-not processing
                          (handle-form-submit % stripe elements set-processing payment-config))}
            
      [:> react-stripe-js/PaymentElement]
      
      (into [:<>] components)]))

(defn processing-popup [processing]
  [popup/view {:state processing
               :style {:background    "rgba(0, 0, 0, 0.6)"
                       :border        "1px solid var(--irb-clr)"
                       :border-radius "12px"
                       :padding       "15px"}}

    [:div {:style {:display         "flex"
                   :gap             "15px"
                   :flex-direction  "column"
                   :align-items     "center"
                   :justify-content "center"}}
      [:<>
        [:i {:class "fa-solid fa-spinner"
             :style {:font-size "36px"
                     :color     "var(--irb-clr)"
                     :animation "spin 1s linear infinite"}}]
        [:p "Waiting for confirmation..."]
        [:p "Please wait while we process your payment..."]]]])

(defn view [stripe-payment-config & components]
  ;; stripe-payment-config
  ;; {:client-secret "acscdsa"
  ;;  :on-success    fn
  ;;  :on-error      fn
  ;;  :on-start      fn
  ;;  :on-end        fn
  ;;  }
  ;; components
  (let [[processing set-processing] (react/useState false)]
    [:<> 
      [:> react-stripe-js/Elements {:stripe  stripe-promise
                                    :options (get-elements-options stripe-payment-config)}
        [payment-form stripe-payment-config processing set-processing components]]
      [processing-popup processing]]))
      

