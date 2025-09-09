(ns features.common.stripe.backend.webhook
  (:require
    [cheshire.core :as json]
    [zero.backend.state.env :as env])
  (:import
     [com.stripe.net Webhook]
     [com.stripe.model Event]))

;; -----------------------------------------------------------------------------
;; ---- Constants ----

(defn webhook-secret []
  (or @env/stripe-webhook-secret
      ;; TEST-SECRET
      "whsec_45b6555dc65888efe25e0456b2720316c9de3d2d420485b3393748fa4bcb5064"))

(def EVENT_HANDLERS (atom {}))

;; ---- Constants ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn register-event-handlers! [handlers]
  (when (not= @EVENT_HANDLERS handlers)
    (reset! EVENT_HANDLERS handlers)
    (println "Stripe: Event handlers already registered")))

(defn get-payload [request]
  (let [body (:body request)]
    (if (string? body)
      body
      (slurp body))))

(defn get-payment-intent [event]
  (-> event .getData .getObject))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn verify-and-parse-webhook [request]
  (let [signature      (get-in request [:headers "stripe-signature"])
        webhook-secret (webhook-secret)]

    (if (or signature webhook-secret)
      (try
        (let [payload (get-payload request)]
           (Webhook/constructEvent payload signature webhook-secret))
        (catch Exception e
          (println "Webhook construction failed:" (.getMessage e))))

      (println "Missing signature or webhook secret"))))

(defn handle-event [event]
  (if-let [handler (get @EVENT_HANDLERS (.getType event))]
    (try
      (handler event)
      (catch Exception e
        (println "Error in event handler for" (.getType event) ":" (.getMessage e))))
    (println "No event handler found for event type:" (.getType event))))


(defn handler [request]
  (let [event (verify-and-parse-webhook request)]
    (if event
      (do 
        (handle-event event)
        {:status 200 :body {:received true}})
      (do 
        (println "Webhook error:" event)
        {:status 400 :body (str "Webhook error: " event)}))))
  