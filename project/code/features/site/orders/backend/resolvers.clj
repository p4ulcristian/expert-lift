(ns features.site.orders.backend.resolvers
  (:require
   [clojure.java.io :as io]
   [com.wsscode.pathom3.connect.operation :as pco]
   [features.site.orders.backend.mutations :as mutations]
   [zero.backend.state.env :as env]
   [zero.backend.state.postgres :as postgres]
   [features.site.orders.backend.db :as db])
  (:import
   [com.stripe.net Webhook]))

;; SQL functions are now in db.clj

(defn parse-uuid [uuid-string]
  "Parse a string UUID, handling both string and UUID inputs"
  (if (string? uuid-string)
    (java.util.UUID/fromString uuid-string)
    uuid-string))

(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

(pco/defresolver get-user-id-res
  [{:keys [request] :as _env} _input]
  {::pco/output [:site/user-id]}
  {:site/user-id (get-user-id-from-request request)})

(pco/defresolver get-user-orders-res
  [{:keys [request] :as _env} _input]
  {::pco/output [{:site/user-orders [:id :status :payment_status :payment_intent_id :urgency :source :created_at :due_date :updated_at]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/user-orders (db/get-orders-by-user-id user-id)}
      {:site/user-orders []})))

(pco/defresolver get-user-orders-with-jobs-res
  [{:keys [request] :as _env} _input]
  {::pco/output [{:site/user-orders-with-jobs [:id :status :payment_status :payment_intent_id :urgency :source :created_at :due_date :updated_at :jobs]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/user-orders-with-jobs (db/get-orders-with-jobs-by-user-id user-id)}
      {:site/user-orders-with-jobs []})))


(pco/defresolver get-order-res
  [{:keys [request] :as _env} {:order/keys [id]}]
  {::pco/output [{:site/order [:id :status :payment_status :payment_intent_id :urgency :source :created_at :due_date :updated_at]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/order (db/get-order-by-id id user-id)}
      {:site/order nil})))

(pco/defresolver get-order-jobs-res
  [_env {:order/keys [id]}]
  {::pco/output [{:site/order-jobs [:job_id :quantity :material :current_surface :job_description 
                                    :part_id :part_name :part_picture_url :part_description
                                    :look_id :look_name :basecolor :color_family :look_thumbnail]}]}
  {:site/order-jobs (db/get-order-jobs-by-order-id id)})

(defn get-order-by-payment-intent-id-fn [payment-intent-id]
  (db/get-order-by-payment-intent-id payment-intent-id))

(pco/defresolver get-order-by-payment-intent-id-res
  [_env {:order/keys [payment-intent-id]}]
  {::pco/output [{:site/order [:id :status :payment_status :payment_intent_id :urgency :source :created_at :due_date :updated_at]}]}
  {:site/order (get-order-by-payment-intent-id-fn payment-intent-id)})

(defn handle-stripe-webhook [request]
  (try
    ;; Check if this is already parsed webhook data (for testing)
    (if (and (map? (:body request)) (get-in request [:body :type]))
      ;; Handle pre-parsed webhook data
      (let [event-data (:body request)
            event-type (:type event-data)]
        
        (println "Handling pre-parsed webhook event type:" event-type)
        
        (case event-type
          "payment_intent.succeeded" 
          (let [payment-intent (get-in event-data [:data :object])
                payment-intent-id (:id payment-intent)
                metadata (:metadata payment-intent)
                order-id (get metadata :order_id)]
            
            (println "Payment succeeded for order:" order-id)
            (println "Payment intent ID:" payment-intent-id)
            
            (when order-id
              (try
                (db/update-order-payment-status! "paid"
                                                  payment-intent-id
                                                  (parse-uuid order-id))
                (println "Order" order-id "updated to paid payment status")
                (catch Exception e
                  (println "Failed to update order:" (.getMessage e)))))
            
            {:status 200 :body "OK"})
          
          "payment_intent.payment_failed"
          (let [payment-intent (get-in event-data [:data :object])
                metadata (:metadata payment-intent)
                order-id (get metadata :order_id)]
            
            (println "Payment failed for order:" order-id)
            
            (when order-id
              (try
                (db/update-order-payment-status! "failed"
                                                  (:id payment-intent)
                                                  (parse-uuid order-id))
                (println "Order" order-id "updated to failed payment status")
                (catch Exception e
                  (println "Failed to update order:" (.getMessage e)))))
            
            {:status 200 :body "OK"})
          
          ;; Default case - acknowledge receipt
          (do
            (println "Unhandled event type:" event-type)
            {:status 200 :body "OK"})))
      
      ;; Handle raw Stripe webhook
      (let [payload (if (string? (:body request))
                      (:body request)
                      (slurp (:body request)))
            sig-header (get-in request [:headers "stripe-signature"])
            endpoint-secret @env/stripe-webhook-secret]
        
        (println "Webhook payload:" payload)
        (println "Signature header:" sig-header)
        
        ;; Verify webhook signature
        (let [event (Webhook/constructEvent payload sig-header endpoint-secret)
              event-type (.getType event)
              event-data (.getData event)]
          
          (println "Event type:" event-type)
          
          (case event-type
            "payment_intent.succeeded" 
            (let [payment-intent (.getObject event-data)
                  payment-intent-id (.getId payment-intent)
                  metadata (.getMetadata payment-intent)
                  order-id (get metadata "order_id")]
              
              (println "Payment succeeded for order:" order-id)
              (println "Payment intent ID:" payment-intent-id)
              
              (when order-id
                (try
                  (db/update-order-payment-status! "paid"
                                                   payment-intent-id
                                                   (parse-uuid order-id))
                  (println "Order" order-id "updated to paid payment status")
                  (catch Exception e
                    (println "Failed to update order:" (.getMessage e)))))
              
              {:status 200 :body "OK"})
            
            "payment_intent.payment_failed"
            (let [payment-intent (.getObject event-data)
                  metadata (.getMetadata payment-intent)
                  order-id (get metadata "order_id")]
              
              (println "Payment failed for order:" order-id)
              
              (when order-id
                (try
                  (db/update-order-payment-status! "failed"
                                                   (.getId payment-intent)
                                                   (parse-uuid order-id))
                  (println "Order" order-id "updated to failed payment status")
                  (catch Exception e
                    (println "Failed to update order:" (.getMessage e)))))
              
              {:status 200 :body "OK"})
            
            ;; Default case - acknowledge receipt
            (do
              (println "Unhandled event type:" event-type)
              {:status 200 :body "OK"})))))
    
    (catch Exception e
      (println "Webhook error:" (.getMessage e))
      {:status 400 :body (str "Webhook error: " (.getMessage e))})))

(def resolvers [get-user-orders-res get-user-orders-with-jobs-res get-user-id-res get-order-res get-order-jobs-res])