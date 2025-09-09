(ns features.flex.orders.backend.write
  (:require
   [zero.backend.state.postgres :as postgres]
   [features.flex.orders.backend.db :as db]))

(def valid-statuses 
  ["order-submitted" "package-arrived" "parts-inspected" "waiting-to-start" 
   "process-planning" "batch-assigned" "in-progress" "job-paused-on-hold" 
   "job-inspected" "job-complete" "packing" "outbound-shipping-ordered" 
   "sent-to-customer" "arrived-at-customer" "customer-accepted-order" 
   "attention-needed" "invoice-issued" "invoice-sent" "invoice-paid" 
   "awaiting-customer-response" "awaiting-parts" "rework-required" 
   "cancelled" "declined" "quote-sent"])

(def valid-urgencies 
  ["low" "normal" "high" "critical" "rush"])

(def valid-sources 
  ["iron-rainbow" "local"])

(def valid-job-statuses
  ["awaiting-inspection" "inspected" "in-progress" "paused" "job-complete"])

(def valid-job-types
  ["grouped" "solo"])

(def valid-materials
  ["aluminum" "steel" "stainless-steel" "brass" "copper" "titanium" "plastic" "carbon-fiber"])

(def valid-surface-conditions
  ["raw" "machined" "painted" "anodized" "powder-coated" "rusty" "oxidized" "polished"])

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workspace-id params)
      (:workspace/id context)))

(defn get-user-id
  "Get a random user ID for demo purposes"
  []
  (let [result (db/get-random-user)]
    (:id result)))

(defn get-random-part-id
  "Get a random part ID for demo purposes"
  []
  (let [result (first (postgres/execute-honey
                       {:select [:id]
                        :from [:parts]
                        :limit 1}))]
    (:id result)))

(defn get-random-look-id
  "Get a random look ID for demo purposes"
  []
  (let [result (first (postgres/execute-honey
                       {:select [:id]
                        :from [:looks]
                        :limit 1}))]
    (:id result)))

(defn create-demo-job
  "Create a demo job for an order"
  [order-id workspace-id]
  (let [job-id (java.util.UUID/randomUUID)
        part-id (get-random-part-id)
        look-id (get-random-look-id)]
    (when (and part-id look-id)
      (try
        (postgres/execute-honey
         {:insert-into :jobs
          :values [{:id job-id
                    :order_id order-id
                    :workspace_id workspace-id
                    :part_id part-id
                    :look_id look-id
                    :status "awaiting-inspection"
                    :type (rand-nth valid-job-types)
                    :description (str "Demo job for " (rand-nth valid-job-types))
                    :created_at (java.time.Instant/now)
                    :quantity (+ 1 (rand-int 50))
                    :material (rand-nth valid-materials)
                    :current_surface (rand-nth valid-surface-conditions)}]})
        job-id
        (catch Exception e
          (println "Error creating job:" (.getMessage e))
          nil)))))

(defn create-demo-order-data
  "Create order data for demo order"
  [order-id workspace-id user-id]
  {:id order-id
   :workspace-id workspace-id
   :user-id user-id
   :created-at (java.time.Instant/now)
   :status "order-submitted"
   :urgency (rand-nth valid-urgencies)
   :source (rand-nth valid-sources)
   :due-date (java.time.Instant/now)})

(defn generate-demo-order
  "Generate a demo order with jobs"
  [{:parquery/keys [context request] :as params}]
  (let [order-id (java.util.UUID/randomUUID)
        workspace-id (get-workspace-id params context)
        user-id (get-user-id)]
    (when (and user-id workspace-id)
      (try
        (let [order-data (create-demo-order-data order-id workspace-id user-id)]
          (db/create-order (:id order-data) (:workspace-id order-data) (:user-id order-data)
                          (:created-at order-data) (:status order-data) (:urgency order-data)
                          (:source order-data) (:due-date order-data))
          (create-demo-job order-id workspace-id)
          (create-demo-job order-id workspace-id)
          {:order/id order-id})
        (catch Exception e
          (println "Error creating order:" (.getMessage e))
          {:error (.getMessage e)})))))

(defn add-demo-order
  "Add a demo order"
  [{:parquery/keys [context request] :as params}]
  (if-let [order-result (generate-demo-order params)]
    (if (:error order-result)
      order-result
      order-result)
    {:error "Could not create demo order - missing required user data"}))

(defn cancel-order
  "Cancel an existing order"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [order-id (:order-id params)]
      (when-let [updated-id (:id (db/cancel-order order-id))]
        {:order/id updated-id :order/status "cancelled"}))
    (catch Exception e
      (println "Error cancelling order:" (.getMessage e))
      {:error (.getMessage e)})))

(defn validate-job-update
  "Validate job update parameters"
  [job-id workspace-id]
  (cond
    (not job-id) {:error "Job ID is required"}
    (not workspace-id) {:error "Workspace ID is required"}
    :else nil))

(defn update-job
  "Update job details"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (get-workspace-id params context)
        job-id (:job-id params)]
    (if-let [error (validate-job-update job-id workspace-id)]
      error
      (try
        (let [result (db/update-job job-id workspace-id
                                   (:material params) (:current-surface params)
                                   (:quantity params) "inspected"
                                   (:type params) (:description params)
                                   (:look-id params) (:part-id params))]
          (if result
            {:job/id job-id
             :job/material (:material result)
             :job/current-surface (:current_surface result)
             :job/quantity (:quantity result)
             :job/status (:status result)
             :job/type (:type result)
             :job/description (:description result)
             :success true}
            {:error "Failed to update job"}))
        (catch Exception e
          (println "Error updating job:" (.getMessage e))
          {:error (.getMessage e)})))))

(defn update-job-status
  "Update job status only"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (get-workspace-id params context)
        job-id (:job-id params)
        status (:status params)]
    (if-let [error (validate-job-update job-id workspace-id)]
      error
      (if-not status
        {:error "Status is required"}
        (try
          (let [result (db/update-job-status job-id workspace-id status)]
            (if result
              {:job/id job-id :job/status (:status result) :success true}
              {:error "Failed to update job status"}))
          (catch Exception e
            (println "Error updating job status:" (.getMessage e))
            {:error (.getMessage e)}))))))

(defn validate-batch-creation
  "Validate batch creation parameters"
  [job-id workspace-id quantity]
  (cond
    (not job-id) {:error "Job ID is required"}
    (not workspace-id) {:error "Workspace ID is required"}
    (not quantity) {:error "Quantity is required"}
    (<= quantity 0) {:error "Quantity must be greater than 0"}
    (db/get-existing-batch job-id workspace-id) {:error "A batch already exists for this job"}
    :else nil))

(defn create-batch
  "Create a batch for a job"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (get-workspace-id params context)
        job-id (:job-id params)
        quantity (:quantity params)
        description (:description params)
        batch-id (java.util.UUID/randomUUID)]
    (if-let [error (validate-batch-creation job-id workspace-id quantity)]
      error
      (try
        (let [batch-data {:id batch-id
                          :workspace-id workspace-id
                          :job-id job-id
                          :description (or description (str "Batch for job " job-id))
                          :quantity quantity
                          :status "awaiting"
                          :current-step 1
                          :is-current true
                          :created-at (java.time.Instant/now)
                          :updated-at (java.time.Instant/now)}]
          (db/create-batch (:id batch-data) (:workspace-id batch-data) (:job-id batch-data)
                          (:description batch-data) (:quantity batch-data) (:status batch-data)
                          (:current-step batch-data) (:is-current batch-data)
                          (:created-at batch-data) (:updated-at batch-data))
          {:batch/id batch-id
           :batch/job-id job-id
           :batch/workspace-id workspace-id
           :batch/description (:description batch-data)
           :batch/quantity quantity
           :batch/status "awaiting"
           :batch/is-current true
           :success true})
        (catch Exception e
          (println "Error creating batch:" (.getMessage e))
          {:error (.getMessage e)})))))

(defn update-order-status
  "Update order status"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [order-id (:order-id params)]
      (when-let [updated-id (:id (db/update-order-status order-id "package-arrived"))]
        {:order/id updated-id :order/status "package-arrived"}))
    (catch Exception e
      (println "Error updating order status:" (.getMessage e))
      {:error (.getMessage e)})))