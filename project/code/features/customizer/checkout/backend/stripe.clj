
(ns features.customizer.checkout.backend.stripe
  (:require
    [clojure.java.io :as io]
    [features.common.stripe.backend.payment-intent :as stripe.pi]
    [features.common.stripe.backend.calculation    :as stripe.calc]
    [features.common.stripe.backend.webhook        :as stripe.webhook]
    [zero.backend.state.postgres                   :as postgres]
    [features.customizer.checkout.backend.db       :as db]))

(defn update-payment-intent [payment-intent-id order-id user-id]
  (println "update-payment-intent" payment-intent-id order-id user-id)
  (stripe.pi/update-payment-intent-by-id payment-intent-id
    (fn [builder]
      (-> builder
          (stripe.pi/put-meta "order_id" (str order-id))
          (stripe.pi/put-meta "user_id" user-id)))))

(defn create-payment-intent-with-tax [options]
  (println "create-payment-intent-with-tax" options)
  (let [tax-calculation (stripe.calc/create-calculation options)
        total-amount    (.getAmountTotal tax-calculation)
        payment-intent  (stripe.pi/create-payment-intent
                          {:amount total-amount
                           :tax_calculation_id (.getId tax-calculation)})] 

    {:tax-calculation   (stripe.calc/get-basic-tax-info tax-calculation)
     :total-amount      total-amount
     :payment-intent-id (.getId payment-intent)
     :client-secret     (.getClientSecret payment-intent)}))

(defn payment-intent-succeeded [event]
  (let [payment-intent (stripe.webhook/get-payment-intent event)]
    
    (if (= "succeeded" (.getStatus payment-intent))
      (let [metadata (.getMetadata payment-intent)
            order-id (get metadata "order_id")
            amount   (.getAmount payment-intent)]
        (println "amount " (type amount))
        (println "dollar amount " (/ amount 100.00))
        
        (if order-id
          (try
            ;; Update order status to paid
            (db/order-payment-success "paid"
                                      (.getId payment-intent)
                                      (/ amount 100.00)
                                      order-id)
            (println "Checkout: Order" order-id "updated to paid status")
            (catch Exception e
              (println "Checkout: Failed to update order" order-id ":" (.getMessage e))))
         (println "Checkout: Order id not found in metadata: " metadata)))
      (println "Checkout: Payment intent failed" payment-intent))))
    
    

;; Register checkout webhook handlers
(stripe.webhook/register-event-handlers! 
  {"payment_intent.succeeded"       #'payment-intent-succeeded
   "payment_intent.payment_failed"  (fn [_] (println "Payment failed"))
   "payment_intent.processing"      (fn [_] (println "Payment processing"))
   "payment_intent.canceled"        (fn [_] (println "Payment canceled"))
   "charge.succeeded"               (fn [_] (println "Charge succeeded"))
   "charge.failed"                  (fn [_] (println "Charge failed"))
   "charge.refunded"                (fn [_] (println "Charge refunded"))
   "charge.dispute.created"         (fn [_] (println "Charge dispute created"))
   "charge.dispute.closed"          (fn [_] (println "Charge dispute closed"))})

;; -----------------------------------------------------------------------------
;; ---- Test ----

;; (def data (atom nil))

;; (reset! data (create-payment-intent-with-tax {:amount 1000
;;                                               :address {:line1 "920 5th Ave"
;;                                                            :city "Seattle"
;;                                                            :state "WA"
;;                                                            :zip "98104"
;;                                                            :country "US"}}))

;; (.toJson @data)

;; (println (calculation/get-basic-tax-info (calculation/create-calculation {:amount 1000
;;                                                                             :address {:line1 "920 5th Ave"
;;                                                                                       :city "Seattle"
;;                                                                                       :state "WA"
;;                                                                                       :zip "98104"
;;                                                                                       :country "US"}})))