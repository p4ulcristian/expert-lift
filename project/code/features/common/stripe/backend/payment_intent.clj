(ns features.common.stripe.backend.payment-intent
  (:import
    [com.stripe Stripe]
    [com.stripe.model PaymentIntent]
    [com.stripe.param PaymentIntentCreateParams
                      PaymentIntentUpdateParams
                      PaymentIntentCreateParams$AutomaticPaymentMethods]))
 
;; -----------------------------------------------------------------------------
;; ---- Create Payment Intent ----

(defn automatic-payment-methods-builder [options]
  (-> (PaymentIntentCreateParams$AutomaticPaymentMethods/builder)
      (.setEnabled true)
      .build))

(defn create-payment-intent-params [options]
  (try
    (-> (PaymentIntentCreateParams/builder)
        (.setAmount (:amount options))
        (.setCurrency (:currency options "usd"))
        (.setAutomaticPaymentMethods (automatic-payment-methods-builder options))
        (.putMetadata "tax_calculation_id" (:tax_calculation_id options))
        .build)
    (catch Exception e
      (println "Error creating payment intent:" e)
      nil)))

(defn create-payment-intent [options]
  (try
    (PaymentIntent/create (create-payment-intent-params options))
    (catch Exception e
      (println "Error creating payment intent:" e)
      nil)))

;; ---- Create Payment Intent ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Retrieve Payment Intent ----

(defn retrieve-payment-intent [payment-intent-id]
  (try
    (when payment-intent-id
      (PaymentIntent/retrieve payment-intent-id))
    (catch Exception e
      (println "Error retrieving payment intent:" e)
      nil)))

;; (retrieve-payment-intent "pi_3S0BHh02Xidq6KZy178rMwlw")

;; ---- Retrieve Payment Intent ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Update Payment Intent ----

(defn put-meta [builder key value]
  (.putMetadata builder key value))

(defn update-payment-intent-params [_fn]
  (try
    (-> (PaymentIntentUpdateParams/builder)
        _fn
        .build)
    (catch Exception e
      (println "Error updating payment intent:" e)
      nil)))

(defn update-payment-intent [payment-intent _fn]
  (try
    (when payment-intent
      (when-let [params (update-payment-intent-params _fn)]
        (.update payment-intent params)))
    (catch Exception e
      (println "Error updating payment intent:" e)
      nil)))

(defn update-payment-intent-by-id [payment-intent-id _fn]
  (try
    (when payment-intent-id
      (let [payment-intent (retrieve-payment-intent payment-intent-id)]
        (update-payment-intent payment-intent _fn)))
    (catch Exception e
      (println "Error updating payment intent by id:" e)
      nil)))

;; ---- Update Payment Intent ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Testing ----

;; (def data (atom nil))

;; (reset! data (PaymentIntent/create
;;                (create-payment-intent-params {:amount 1000})))

;; (println @data)
