(ns features.flex.batches.backend.read
  (:require
   [features.flex.batches.backend.db :as batches-db]))

(defn fetch-process-workstations
  "Get workstations for a process"
  [process-id]
  (try
    (batches-db/get-process-workstations process-id)
    (catch Exception e
      (println "Error getting workstations for process:" process-id "error:" (.getMessage e))
      [])))

(defn enrich-batch-process
  "Add workstation data to a batch process"
  [process]
  (let [workstations (fetch-process-workstations (:process_id process))]
    {:process/id (:process_id process)
     :process/name (:process_name process)
     :process/description (:process_description process)
     :process/color "#666"
     :process/step-order (:step_order process)
     :process/start-time (:start_time process)
     :process/finish-time (:finish_time process)
     :process/workstations workstations}))

(defn fetch-processes-for-batch
  "Get processes for a batch with their workstations"
  [batch-id]
  (try
    (->> (batches-db/get-batch-processes batch-id)
         (mapv enrich-batch-process))
    (catch Exception e
      (println "Error getting processes for batch:" batch-id "error:" (.getMessage e))
      [])))

(defn enrich-batch-with-processes
  "Add process data to a batch"
  [batch]
  (let [processes (fetch-processes-for-batch (:batch/id batch))]
    (assoc batch :batch/processes processes)))

(defn get-current-batches
  "Get current batches for a job with their processes"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [job-id (:job-id params)
          workspace-id (or (:workspace-id context) 
                           (get-in params [:parquery/request :transit-params :parquery/context :workspace-id]))]
      (when (and job-id workspace-id)
        (let [batches (batches-db/get-batches-by-job job-id workspace-id)]
          (mapv enrich-batch-with-processes batches))))
    (catch Exception e
      (println "Error getting current batches for job:" (.getMessage e))
      [])))