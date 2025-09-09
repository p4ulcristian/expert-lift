(ns features.flex.batches.backend.write
  (:require
   [clojure.string :as clojure.string]
   [features.flex.batches.backend.db :as batches-db]))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:batch/workspace-id params)
      (:workspace/id context)))

(defn is-valid-uuid?
  "Check if string is a valid UUID format"
  [uuid-str]
  (if (and uuid-str (string? uuid-str))
    (let [parts (clojure.string/split uuid-str #"-")]
      (and (= (count parts) 5)
           (= (count (nth parts 0)) 8)
           (= (count (nth parts 1)) 4)
           (= (count (nth parts 2)) 4)
           (= (count (nth parts 3)) 4)
           (= (count (nth parts 4)) 12)))
    false))

(defn extract-original-process-id
  "Extract original process ID from compound ID"
  [process-id]
  (if (and process-id (string? process-id))
    (let [parts (clojure.string/split process-id #"-")
          part-count (count parts)]
      (cond
        (= part-count 5) process-id
        (>= part-count 10) (clojure.string/join "-" (take 5 parts))
        (> part-count 5) (clojure.string/join "-" (take 5 parts))
        :else process-id))
    process-id))

(defn get-process-id-for-db
  "Get database-compatible process ID from process data"
  [process]
  (let [original-id (:process/original-id process)
        raw-id (:process/id process)
        validated-original-id (when (is-valid-uuid? original-id) original-id)
        result (or validated-original-id (extract-original-process-id raw-id))]
    result))

(defn get-workstation-for-process
  "Get workstation for a process"
  [process-id]
  (try
    (let [workstations (batches-db/get-process-workstations process-id)]
      (when (seq workstations)
        (:workstation_id (first workstations))))
    (catch Exception e
      (println "Error getting workstation for process" process-id ":" (.getMessage e))
      nil)))

(defn save-batch-processes
  "Save processes for a batch"
  [batch-id processes]
  (try
    (batches-db/delete-batch-processes batch-id)
    (let [saved-count (atom 0)
          failed-count (atom 0)]
      (doseq [[index process] (map-indexed vector processes)]
        (let [db-process-id (get-process-id-for-db process)]
          (if db-process-id
            (try
              (batches-db/create-batch-process batch-id db-process-id (inc index))
              (swap! saved-count inc)
              (catch Exception e
                (swap! failed-count inc)
                (println "Error saving process" (inc index) ":" (.getMessage e))))
            (swap! failed-count inc))))
      (= @failed-count 0))
    (catch Exception e
      (println "Error saving batch processes for batch" batch-id ":" (.getMessage e))
      false)))

(defn validate-batch-input
  "Validate batch creation input"
  [job-id workspace-id batches]
  (cond
    (not job-id) {:error "Job ID is required"}
    (not workspace-id) {:error "Workspace ID is required"}
    (not batches) {:error "Batches data is required"}
    (empty? batches) {:error "At least one batch is required"}
    :else nil))

(defn create-batch-data
  "Create batch data structure for database"
  [batch job-id workspace-id]
  (let [batch-id (or (:batch/id batch) (str (java.util.UUID/randomUUID)))
        first-process (first (:batch/processes batch))
        first-process-id (when first-process (get-process-id-for-db first-process))
        current-workstation-id (when first-process-id 
                                  (get-workstation-for-process first-process-id))]
    {:batch/id batch-id
     :batch/workspace-id workspace-id
     :batch/job-id job-id
     :batch/description (or (:batch/description batch) (str "Batch " batch-id))
     :batch/quantity (or (:batch/quantity batch) 1)
     :batch/status (or (:batch/status batch) "awaiting")
     :batch/current-step (or (:batch/current-step batch) 1)
     :batch/current-workstation-id current-workstation-id
     :batch/previous-workstation-id nil
     :batch/workflow-state "to-do"}))

(defn save-single-batch
  "Save or update a single batch"
  [batch job-id workspace-id]
  (let [batch-data (create-batch-data batch job-id workspace-id)
        batch-id (:batch/id batch-data)
        existing-batch-id (:batch/id batch)]
    (if-let [existing-batch (and existing-batch-id
                                 (batches-db/get-batch-by-id existing-batch-id workspace-id))]
      (do (batches-db/update-batch batch-data)
          (assoc batch-data :action "updated"))
      (do (batches-db/create-batch batch-data)
          (assoc batch-data :action "created")))))

(defn process-batch-with-processes
  "Process a batch and save its associated processes"
  [batch job-id workspace-id]
  (let [result (save-single-batch batch job-id workspace-id)
        batch-id (:batch/id result)
        processes (:batch/processes batch)]
    (when processes
      (save-batch-processes batch-id processes))
    result))

(defn save-batches
  "Save multiple batches for a job"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (get-workspace-id params context)
        job-id (:batch/job-id params)
        batches (:batch/batches params)]
    (if-let [error (validate-batch-input job-id workspace-id batches)]
      error
      (try
        (batches-db/deactivate-job-batches job-id workspace-id)
        (let [processed-batches (mapv #(process-batch-with-processes % job-id workspace-id) batches)]
          (try
            (batches-db/update-job-status job-id "in-progress" workspace-id)
            (catch Exception e
              (println "Error updating job status:" (.getMessage e))))
          {:success true
           :batch/processed-batches processed-batches
           :batch/job-id job-id
           :message (str "Successfully processed " (count processed-batches) " batches")})
        (catch Exception e
          (println "Error in save-batches:" (.getMessage e))
          {:error (.getMessage e)})))))

(defn handle-process-timing
  "Handle timing for process state changes"
  [batch-id workspace-id current-step new-workflow-state]
  (try
    (cond
      (= new-workflow-state "doing")
      (batches-db/update-batch-process-start-time batch-id current-step)
      (= new-workflow-state "done")
      (batches-db/update-batch-process-finish-time batch-id current-step))
    (catch Exception e
      (println "Error handling process timing:" (.getMessage e)))))

(defn handle-step-progression
  "Handle batch progression to next step"
  [batch-id workspace-id current-step]
  (try
    (when (> current-step 0)
      (batches-db/update-batch-process-finish-time batch-id current-step))
    (catch Exception e
      (println "Error handling step progression timing:" (.getMessage e)))))

(defn update-batch-workflow-state
  "Update batch workflow state with timing handling"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (get-workspace-id params context)
        batch-id (:batch-id params)
        workflow-state (:workflow-state params)]
    (try
      (let [batch-info (batches-db/get-current-batch-step batch-id workspace-id)
            current-step (:current_step batch-info)]
        (if (= workflow-state "done")
          (let [next-workstation (try
                                   (batches-db/get-next-workstation-for-batch batch-id workspace-id)
                                   (catch Exception e nil))]
            (handle-process-timing batch-id workspace-id current-step workflow-state)
            (if next-workstation
              (do
                (handle-step-progression batch-id workspace-id current-step)
                (batches-db/progress-batch-to-next-workstation
                 batch-id (:workstation_id next-workstation) workspace-id)
                {:success true :message "Batch progressed to next workstation"})
              (do
                (batches-db/update-batch-workflow-state batch-id workflow-state workspace-id)
                {:success true :message "Batch completed"})))
          (do
            (handle-process-timing batch-id workspace-id current-step workflow-state)
            (batches-db/update-batch-workflow-state batch-id workflow-state workspace-id)
            {:success true :message "Batch workflow state updated"})))
      (catch Exception e
        (println "Error updating batch workflow state:" (.getMessage e))
        {:error (.getMessage e)}))))

(defn check-order-completion
  "Check if all jobs in an order are complete"
  [order-id workspace-id]
  (let [jobs (batches-db/get-jobs-by-order order-id workspace-id)]
    (when (and (seq jobs)
               (every? #(= (:status %) "job-complete") jobs))
      (try
        (batches-db/update-order-status order-id "ready-to-transport" workspace-id)
        (catch Exception e
          (println "Error checking order completion:" (.getMessage e)))))))

(defn check-job-completion
  "Check if all batches in a job are complete"
  [job-id workspace-id]
  (try
    (let [batches (batches-db/get-batches-by-job job-id workspace-id)
          all-batches-complete? (every? #(= (:status %) "complete") batches)]
      (when all-batches-complete?
        (batches-db/update-job-status job-id "job-complete" workspace-id)
        (when-let [job (first (batches-db/get-jobs-by-order (:order_id (first batches)) workspace-id))]
          (check-order-completion (:order_id job) workspace-id))))
    (catch Exception e
      (println "Error checking job completion:" (.getMessage e)))))

(defn confirm-batch
  "Confirm batch completion"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (get-workspace-id params context)
        batch-id (:batch-id params)]
    (try
      (let [batch (batches-db/get-batch-by-id batch-id workspace-id)]
        (when batch
          (batches-db/update-batch-status batch-id "complete" (java.time.LocalDateTime/now) workspace-id)
          (check-job-completion (:job_id batch) workspace-id)
          {:batch/id batch-id :status "complete"}))
      (catch Exception e
        (println "Error confirming batch:" (.getMessage e))
        {:error (.getMessage e)}))))