
(ns features.customizer.checkout.backend.mutations
  (:require
    [clojure.java.io :as io]
    [com.wsscode.pathom3.connect.operation :as pathom.co :refer [defmutation]]
    [zero.backend.state.postgres :as postgres]
   
    [features.customizer.checkout.backend.stripe :as stripe]
    [features.customizer.checkout.backend.db :as db]))

;; -----------------------------------------------------------------------------
;; ---- Create Order ----

(defn create-order [{:keys [data workspace-id user-id payment-intent-id]}] 
  (try
    (let [order-id (java.util.UUID/randomUUID)]
      (db/checkout-create-order order-id
                                workspace-id
                                user-id
                                "order-submitted"
                                "rush"
                                "iron-rainbow"
                                (java.time.Instant/now)
                                payment-intent-id)
      (stripe/update-payment-intent payment-intent-id order-id user-id)
      order-id)
    (catch Exception e
      (println "create-order! error" e)
      (throw e)))) 

(defn prepare-jobs-data [order-id workspace-id data]
  (for [[_ package-data] data]
    (let [package-id (if (string? (:id package-data))
                       (java.util.UUID/fromString (:id package-data))
                       (:id package-data))]
      [(java.util.UUID/randomUUID)    ;; id
       workspace-id                   ;; workspace-id
       order-id                       ;; order-id
       package-id                     ;; package-id
       (:description package-data)    ;; description
       (get package-data "formdata")  ;; form-data
       "pending"])))                  ;; status

(defn prepare-batches-data [order-id workspace-id data jobs-with-packages]
  (mapcat (fn [[job-id _ package-data]]
            (for [[_ part-data] (:parts package-data)]
              (let [quantity (get-in package-data ["formdata" "quantity" :qty] 1)]
                [(java.util.UUID/randomUUID)      ;; id
                 workspace-id                     ;; workspace-id
                 order-id                         ;; order-id
                 job-id                           ;; job-id
                 (:id part-data)                  ;; part-id
                 (get-in part-data [:look :id])   ;; look-id
                 (get part-data "formdata")       ;; form-data
                 (:description part-data)         ;; description
                 quantity                         ;; quantity
                 "awaiting"                   ;; status
                 (java.time.Instant/now)])))  ;; updated-at
          jobs-with-packages))

(defn create-order-fn [_ {:keys [data workspace-id] :as mutation-props}]
  (time
    (postgres/with-pool
      (fn [conn]
        (let [order-id           (create-order mutation-props)
              jobs-data          (prepare-jobs-data order-id workspace-id data)
              job-ids            (map first jobs-data)
              jobs-with-packages (map vector job-ids (map second data) (map second data))
              batches-data       (prepare-batches-data order-id workspace-id data jobs-with-packages)]
          
          ;; Bulk insert all jobs at once
          (when (seq jobs-data)
            (db/create-jobs-bulk jobs-data))
          
          ;; Bulk insert all batches at once  
          (when (seq batches-data)
            (db/create-batches-bulk batches-data))
          
          order-id)))))
    
(defmutation create-order-m [env mutation-props]
  {::pathom.co/op-name 'customizer.checkout/create-order!}
  (create-order-fn env mutation-props))

;; ---- Create Order ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Successfully Paid Order ----

(defn order-payment-success-fn [{:keys [request] :as env} mutation-props]
  (println "add-order-payment-success-fn" mutation-props)
  (db/order-payment-success "paid"
                             (:payment-intent-id mutation-props)
                             (:total-amount mutation-props)
                             (:order-id mutation-props)))
                  
(defmutation order-payment-success-m [env mutation-props]
  {::pathom.co/op-name 'customizer.checkout/order-payment-success!}
  (order-payment-success-fn env mutation-props))

;; ---- Successfully Paid Order ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Create Payment Intent ----

(defn create-payment-intent-fn [{:keys [request] :as env} mutation-props]
  ;; (stripe/create-payment-intent mutation-props)ú
  (println "create-payment-intent-fn" mutation-props)
  (stripe/create-payment-intent-with-tax mutation-props))

(defmutation create-payment-intent-m [env mutation-props]
  {::pathom.co/op-name 'customizer.checkout/create-payment-intent}
  (create-payment-intent-fn env mutation-props))

;; ---- Create Payment Intent ----
;; -----------------------------------------------------------------------------

(def mutations [create-order-m
                create-payment-intent-m
                order-payment-success-m])