(ns features.common.stripe.backend.utils
  (:require
     [zero.backend.state.env :as env])
  (:import
   [com.stripe Stripe]))

(defn init-stripe! []
  (let [api-key @env/stripe-secret-key]
    (println "Initializing Stripe with key:" api-key)
    (set! Stripe/apiKey api-key)))