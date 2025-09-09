(ns features.site.orders.backend.mutations
  (:require
  ;;  [app.backend.stripe :as stripe]
   [com.wsscode.pathom3.connect.operation :as pco]))

(defn create-payment-intent-fn [{:keys [_request] :as _env} {:keys [amount currency order-id]}]
  (try 
    ;; (let [metadata (when order-id {"order_id" (str order-id)})
    ;;       intent (stripe/create-payment-intent
    ;;               {:amount amount
    ;;                :currency currency
    ;;                :metadata metadata
    ;;                :automatic_payment_methods {:enabled true}})]
    ;;   (println "intent" intent)
    ;;   intent)
    {}
    (catch Exception e
      {:error (str "Failed to create payment intent: " (.getMessage e))})))

(pco/defmutation create-payment-intent [env {:keys [amount currency order-id]}]
  {::pco/op-name 'site/create-payment-intent
   ::pco/output [:client_secret :id :error]}
  (create-payment-intent-fn env {:amount amount :currency currency :order-id order-id}))

(def mutations [create-payment-intent])