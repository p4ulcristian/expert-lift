(ns features.flex.batches.backend.db
  (:require
   [clojure.set :as set]
   [zero.backend.state.postgres :as postgres]))

(defn transform-batch-keys
  "Transform SQL column names to namespaced keywords for batches"
  [batch]
  (when batch
    (set/rename-keys batch
      {:id :batch/id
       :workspace_id :batch/workspace-id
       :job_id :batch/job-id
       :description :batch/description
       :quantity :batch/quantity
       :status :batch/status
       :current_step :batch/current-step
       :current_workstation_id :batch/current-workstation-id
       :previous_workstation_id :batch/previous-workstation-id
       :workflow_state :batch/workflow-state
       :is_current :batch/is-current
       :created_at :batch/created-at
       :updated_at :batch/updated-at
       :part_name :batch/part-name
       :part_picture_url :batch/part-picture-url
       :color_name :batch/color-name
       :color_basecolor :batch/color-basecolor
       :order_id :batch/order-id})))

(defn get-batches-by-job
  "Get batches by job with part and look information"
  [job-id workspace-id]
  (->> (postgres/execute-sql
        "SELECT 
           batches.id::text,
           batches.workspace_id::text,
           batches.job_id::text,
           batches.description,
           batches.quantity,
           batches.status,
           batches.current_step,
           batches.current_workstation_id::text,
           batches.previous_workstation_id::text,
           batches.workflow_state,
           batches.is_current,
           to_char(batches.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(batches.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at,
           parts.name as part_name,
           parts.picture_url as part_picture_url,
           looks.name as color_name,
           looks.basecolor as color_basecolor,
           jobs.order_id::text as order_id
         FROM batches
         INNER JOIN jobs ON batches.job_id = jobs.id
         LEFT JOIN parts ON batches.part_id = parts.id
         LEFT JOIN looks ON batches.look_id = looks.id
         WHERE batches.job_id = $1 
           AND batches.workspace_id = $2"
        {:params [job-id workspace-id]})
       (map transform-batch-keys)))

(defn deactivate-job-batches
  "Deactivate all batches for a job"
  [job-id workspace-id]
  (let [result (postgres/execute-sql
                "UPDATE batches 
                 SET is_current = false, updated_at = NOW()
                 WHERE job_id = $1 
                   AND workspace_id = $2"
                {:params [job-id workspace-id]})]
    (count result)))

(defn update-batch
  "Update an existing batch"
  [batch-data]
  (first (postgres/execute-sql
          "UPDATE batches 
           SET description = $1,
               quantity = $2,
               status = $3,
               current_step = $4,
               current_workstation_id = $5,
               previous_workstation_id = $6,
               workflow_state = $7,
               is_current = true,
               updated_at = NOW()
           WHERE id = $8 
             AND workspace_id = $9
           RETURNING id"
          {:params [(:batch/description batch-data)
                    (:batch/quantity batch-data)
                    (:batch/status batch-data)
                    (:batch/current-step batch-data)
                    (:batch/current-workstation-id batch-data)
                    (:batch/previous-workstation-id batch-data)
                    (:batch/workflow-state batch-data)
                    (:batch/id batch-data)
                    (:batch/workspace-id batch-data)]})))

(defn create-batch
  "Create a new batch"
  [batch-data]
  (first (postgres/execute-sql
          "INSERT INTO batches (id, workspace_id, job_id, description, quantity, status, current_step, current_workstation_id, previous_workstation_id, workflow_state, is_current, created_at, updated_at)
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, true, NOW(), NOW())
           RETURNING id"
          {:params [(:batch/id batch-data)
                    (:batch/workspace-id batch-data)
                    (:batch/job-id batch-data)
                    (:batch/description batch-data)
                    (:batch/quantity batch-data)
                    (:batch/status batch-data)
                    (:batch/current-step batch-data)
                    (:batch/current-workstation-id batch-data)
                    (:batch/previous-workstation-id batch-data)
                    (:batch/workflow-state batch-data)]})))

(defn get-batch-by-id
  "Get a batch by ID"
  [id workspace-id]
  (first (postgres/execute-sql
          "SELECT id, workspace_id, job_id, description, quantity, status, current_step, current_workstation_id, workflow_state, is_current
           FROM batches 
           WHERE id = $1 AND workspace_id = $2"
          {:params [id workspace-id]})))

(defn delete-batch-processes
  "Delete all processes for a batch"
  [batch-id]
  (let [result (postgres/execute-sql
                "DELETE FROM batch_processes 
                 WHERE batch_id = $1"
                {:params [batch-id]})]
    (count result)))

(defn create-batch-process
  "Create a new batch process"
  [batch-id process-id step-order]
  (first (postgres/execute-sql
          "INSERT INTO batch_processes (batch_id, process_id, step_order, created_at)
           VALUES ($1, $2, $3, NOW())
           RETURNING id"
          {:params [batch-id process-id step-order]})))

(defn get-batch-processes
  "Get all processes for a batch"
  [batch-id]
  (postgres/execute-sql
   "SELECT 
      bp.id,
      bp.batch_id::text,
      bp.process_id::text, 
      bp.step_order,
      to_char(bp.start_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as start_time,
      to_char(bp.finish_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as finish_time,
      p.name as process_name,
      p.description as process_description
    FROM batch_processes bp
    INNER JOIN processes p ON bp.process_id = p.id
    WHERE bp.batch_id = $1
    ORDER BY bp.step_order"
   {:params [batch-id]}))

(defn get-process-workstations
  "Get all workstations that can handle a specific process"
  [process-id]
  (postgres/execute-sql
   "SELECT 
      w.id::text as workstation_id,
      w.name as workstation_name,
      w.description as workstation_description
    FROM workstations w
    INNER JOIN workstation_processes wp ON w.id = wp.workstation_id
    WHERE wp.process_id = $1
    ORDER BY w.name"
   {:params [process-id]}))

(defn update-batch-workflow-state
  "Update batch workflow state"
  [batch-id workflow-state workspace-id]
  (let [result (postgres/execute-sql
                "UPDATE batches 
                 SET workflow_state = $1,
                     updated_at = NOW()
                 WHERE id = $2 AND workspace_id = $3"
                {:params [workflow-state batch-id workspace-id]})]
    (count result)))

(defn progress-batch-to-next-workstation
  "Progress batch to next workstation when moved to 'done'"
  [batch-id next-workstation-id workspace-id]
  (let [result (postgres/execute-sql
                "UPDATE batches 
                 SET current_step = current_step + 1,
                     previous_workstation_id = current_workstation_id,
                     current_workstation_id = $1,
                     workflow_state = 'to-do',
                     updated_at = NOW()
                 WHERE id = $2 AND workspace_id = $3"
                {:params [next-workstation-id batch-id workspace-id]})]
    (count result)))

(defn get-next-workstation-for-batch
  "Get the next workstation for a batch based on its next process"
  [batch-id workspace-id]
  (first (postgres/execute-sql
          "SELECT 
             w.id::text as workstation_id,
             w.name as workstation_name
           FROM batches b
           JOIN batch_processes bp ON b.id = bp.batch_id AND bp.step_order = (b.current_step + 1)
           JOIN workstation_processes wp ON bp.process_id = wp.process_id
           JOIN workstations w ON wp.workstation_id = w.id
           WHERE b.id = $1 
             AND b.workspace_id = $2
           LIMIT 1"
          {:params [batch-id workspace-id]})))

(defn update-batch-process-start-time
  "Set start_time on current step when workflow state changes to 'doing'"
  [batch-id step-order]
  (let [result (postgres/execute-sql
                "UPDATE batch_processes 
                 SET start_time = NOW()
                 WHERE batch_id = $1 
                   AND step_order = $2
                   AND start_time IS NULL"
                {:params [batch-id step-order]})]
    (count result)))

(defn update-batch-process-finish-time
  "Set finish_time on a specific step when it's completed"
  [batch-id step-order]
  (let [result (postgres/execute-sql
                "UPDATE batch_processes 
                 SET finish_time = NOW()
                 WHERE batch_id = $1 
                   AND step_order = $2
                   AND finish_time IS NULL"
                {:params [batch-id step-order]})]
    (count result)))

(defn get-current-batch-step
  "Get current step info for a batch"
  [batch-id workspace-id]
  (first (postgres/execute-sql
          "SELECT 
             b.current_step,
             b.workflow_state
           FROM batches b
           WHERE b.id = $1 
             AND b.workspace_id = $2"
          {:params [batch-id workspace-id]})))

(defn update-batch-status
  "Update batch status"
  [id status updated-at workspace-id]
  (let [result (postgres/execute-sql
                "UPDATE batches 
                 SET status = $1,
                     updated_at = $2
                 WHERE id = $3 
                   AND workspace_id = $4"
                {:params [status updated-at id workspace-id]})]
    (count result)))

(defn update-job-status
  "Update job status"
  [id status workspace-id]
  (let [result (postgres/execute-sql
                "UPDATE jobs 
                 SET status = $1
                 WHERE id = $2 
                   AND workspace_id = $3"
                {:params [status id workspace-id]})]
    (count result)))

(defn get-jobs-by-order
  "Get all jobs for an order"
  [order-id workspace-id]
  (postgres/execute-sql
   "SELECT 
      j.id,
      j.status,
      j.order_id
    FROM jobs j
    WHERE j.order_id = $1 
      AND j.workspace_id = $2"
   {:params [order-id workspace-id]}))

(defn update-order-status
  "Update order status"
  [id status workspace-id]
  (let [result (postgres/execute-sql
                "UPDATE orders 
                 SET status = $1
                 WHERE id = $2 
                   AND workspace_id = $3"
                {:params [status id workspace-id]})]
    (count result)))