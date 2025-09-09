(ns app.backend.stripe
  (:require
   [zero.backend.state.env :as env])
  (:import
   [com.stripe Stripe]
   [com.stripe.model PaymentIntent]
   [com.stripe.param PaymentIntentCreateParams]
   [com.stripe.param PaymentIntentCreateParams$AutomaticPaymentMethods]))

(defn init-stripe! []
  (let [api-key @env/stripe-secret-key]
    (println "Initializing Stripe with key:" api-key)
    (set!  Stripe/apiKey api-key)))

(defn create-payment-intent [{:keys [amount currency metadata]}]
  (try
    ;; Ensure API key is set before each operation
    (init-stripe!)
    (let [params-builder (-> (PaymentIntentCreateParams/builder)
                             (.setAmount amount)
                             (.setCurrency currency)
                             (.setAutomaticPaymentMethods
                               (-> (PaymentIntentCreateParams$AutomaticPaymentMethods/builder)
                                   (.setEnabled true)
                                   (.build))))
          params-builder (if metadata
                           (.putAllMetadata params-builder metadata)
                           params-builder)
          intent (PaymentIntent/create (.build params-builder))]
      {:id (.getId intent)
       :client_secret (.getClientSecret intent)
       :status (.getStatus intent)})
    (catch Exception e
      (println "Error creating payment intent:" (.getMessage e))
      {:error (.getMessage e)})))