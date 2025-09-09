(ns features.customizer.checkout.backend.db
  (:require 
    [zero.backend.state.postgres :as postgres]))

(defn checkout-create-order
  "Insert a new order and return id"
  [id workspace-id user-id status urgency source due-date payment-intent-id]
  (postgres/execute-sql 
   "INSERT INTO orders (id, workspace_id, user_id, status, urgency, source, due_date, payment_intent_id)
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
    RETURNING id"
   {:params [id workspace-id user-id status urgency source due-date payment-intent-id]}))

(defn create-job
  "Create a job for an order"
  [id workspace-id order-id package-id description form-data status]
  (postgres/execute-sql 
   "INSERT INTO jobs (id, workspace_id, order_id, package_id, description, form_data, status)
    VALUES ($1, $2, $3, $4, $5, $6, $7)
    RETURNING id"
   {:params [id workspace-id order-id package-id description form-data status]}))

(defn create-jobs-bulk
  "Create multiple jobs in a single insert"
  [jobs-data]
  (let [placeholders (map-indexed (fn [idx _] 
                                   (let [base (* idx 7)]
                                     (str "($" (+ base 1) ", $" (+ base 2) ", $" (+ base 3) 
                                          ", $" (+ base 4) ", $" (+ base 5) ", $" (+ base 6) 
                                          ", $" (+ base 7) ")"))) 
                                  jobs-data)
        values-clause (clojure.string/join ", " placeholders)
        params (flatten jobs-data)]
    (postgres/execute-sql 
     (str "INSERT INTO jobs (id, workspace_id, order_id, package_id, description, form_data, status)
           VALUES " values-clause "
           RETURNING id")
     {:params params})))

(defn create-batch
  "Create a batch for a job"
  [id workspace-id order-id job-id part-id look-id form-data description quantity status updated-at]
  (postgres/execute-sql 
   "INSERT INTO batches (id, workspace_id, order_id, job_id, part_id, look_id, form_data, description, quantity, status, updated_at)
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
    RETURNING id"
   {:params [id workspace-id order-id job-id part-id look-id form-data description quantity status updated-at]}))

(defn create-batches-bulk
  "Create multiple batches in a single insert"
  [batches-data]
  (let [placeholders (map-indexed (fn [idx _] 
                                   (let [base (* idx 11)]
                                     (str "($" (+ base 1) ", $" (+ base 2) ", $" (+ base 3) 
                                          ", $" (+ base 4) ", $" (+ base 5) ", $" (+ base 6) 
                                          ", $" (+ base 7) ", $" (+ base 8) ", $" (+ base 9) 
                                          ", $" (+ base 10) ", $" (+ base 11) ")"))) 
                                  batches-data)
        values-clause (clojure.string/join ", " placeholders)
        params (flatten batches-data)]
    (postgres/execute-sql 
     (str "INSERT INTO batches (id, workspace_id, order_id, job_id, part_id, look_id, form_data, description, quantity, status, updated_at)
           VALUES " values-clause "
           RETURNING id")
     {:params params})))

(defn order-payment-success
  "Update order payment status"
  [payment-status payment-intent-id total-amount order-id]
  (postgres/execute-sql 
   "UPDATE orders 
    SET payment_status = $1,
        payment_intent_id = $2,
        total_amount = $3,
        updated_at = NOW()
    WHERE id = $4"
   {:params [payment-status payment-intent-id total-amount order-id]}))