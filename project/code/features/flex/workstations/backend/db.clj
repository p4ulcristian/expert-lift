(ns features.flex.workstations.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn add-workstation!
  "Adds a new workstation to the database"
  [{:keys [id workspace-id name description]}]
  (postgres/execute-sql
    "INSERT INTO workstations (id, workspace_id, name, description)
    VALUES ($1, $2, $3, $4)
    RETURNING id"
    {:params [id workspace-id name description]}))

(defn update-workstation!
  "Updates an existing workstation"
  [{:keys [workstation-id workspace-id name description]}]
  (postgres/execute-sql
    "UPDATE workstations
    SET name = $3,
        description = $4,
        updated_at = NOW()
    WHERE id = $1
      AND workspace_id = $2
    RETURNING id"
    {:params [workstation-id workspace-id name description]}))

(defn delete-workstation!
  "Deletes a workstation"
  [{:keys [workstation-id workspace-id]}]
  (postgres/execute-sql
    "DELETE FROM workstations
    WHERE id = $1
      AND workspace_id = $2
    RETURNING id"
    {:params [workstation-id workspace-id]}))

(defn get-workstations
  "Get all workstations for a workspace ordered by name"
  [{:keys [workspace_id]}]
  (->> (postgres/execute-sql
         "SELECT 
           workstations.id,
           workstations.name,
           workstations.description,
           workstations.workspace_id,
           to_char(workstations.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(workstations.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
         FROM workstations
         WHERE workstations.workspace_id = $1
         ORDER BY workstations.name"
         {:params [workspace_id]})
       (mapv (fn [row]
               {:workstation/id (:id row)
                :workstation/name (:name row)
                :workstation/description (:description row)
                :workstation/workspace-id (:workspace_id row)
                :workstation/created-at (:created_at row)
                :workstation/updated-at (:updated_at row)}))))

(defn get-workstation
  "Get a single workstation by id"
  [{:keys [id]}]
  (when-let [row (first
                   (postgres/execute-sql
                     "SELECT 
                       workstations.id,
                       workstations.name,
                       workstations.description,
                       workstations.workspace_id,
                       to_char(workstations.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
                       to_char(workstations.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
                     FROM workstations
                     WHERE workstations.id = $1"
                     {:params [id]}))]
    {:workstation/id (:id row)
     :workstation/name (:name row)
     :workstation/description (:description row)
     :workstation/workspace-id (:workspace_id row)
     :workstation/created-at (:created_at row)
     :workstation/updated-at (:updated_at row)}))

(defn create-workstation
  "Create a new workstation"
  [{:keys [id name description workspace_id]}]
  (when-let [row (first
                   (postgres/execute-sql
                     "INSERT INTO workstations (id, name, description, workspace_id)
                     VALUES ($1, $2, $3, $4)
                     RETURNING 
                       id,
                       name,
                       description,
                       workspace_id,
                       to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
                       to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at"
                     {:params [id name description workspace_id]}))]
    {:workstation/id (:id row)
     :workstation/name (:name row)
     :workstation/description (:description row)
     :workstation/workspace-id (:workspace_id row)
     :workstation/created-at (:created_at row)
     :workstation/updated-at (:updated_at row)}))

(defn edit-workstation
  "Edit a workstation by id"
  [{:keys [id name description]}]
  (postgres/execute-sql
    "UPDATE workstations
    SET name = $2,
        description = $3,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $1"
    {:params [id name description]}))

(defn delete-workstation
  "Delete a workstation by id"
  [{:keys [id]}]
  (postgres/execute-sql
    "DELETE FROM workstations WHERE id = $1"
    {:params [id]}))

(defn get-workstation-machines
  "Get all machines assigned to a workstation"
  [{:keys [workstation_id]}]
  (->> (postgres/execute-sql
         "SELECT 
           machines.id,
           machines.name,
           machines.description,
           machines.workspace_id,
           machines.workstation_id,
           to_char(machines.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(machines.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
         FROM machines
         WHERE machines.workstation_id = $1
         ORDER BY machines.name"
         {:params [workstation_id]})
       (mapv (fn [row]
               {:machine/id (:id row)
                :machine/name (:name row)
                :machine/description (:description row)
                :machine/workspace-id (:workspace_id row)
                :machine/workstation-id (:workstation_id row)
                :machine/created-at (:created_at row)
                :machine/updated-at (:updated_at row)}))))

(defn get-available-machines
  "Get all machines that aren't assigned to any workstation"
  [{:keys [workspace_id]}]
  (postgres/execute-sql
    "SELECT 
      machines.id,
      machines.name,
      machines.description,
      machines.workspace_id,
      machines.workstation_id,
      to_char(machines.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(machines.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM machines
    WHERE machines.workspace_id = $1
      AND machines.workstation_id IS NULL
    ORDER BY machines.name"
    {:params [workspace_id]}))

(defn assign-machine-to-workstation
  "Assign a machine to a workstation"
  [{:keys [workstation_id machine_id]}]
  (postgres/execute-sql
    "UPDATE machines
    SET workstation_id = $1,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $2"
    {:params [workstation_id machine_id]}))

(defn unassign-machine-from-workstation
  "Remove a machine from a workstation"
  [{:keys [machine_id]}]
  (postgres/execute-sql
    "UPDATE machines
    SET workstation_id = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $1"
    {:params [machine_id]}))

(defn get-workstation-processes
  "Get all processes assigned to a workstation"
  [{:keys [workstation_id]}]
  (postgres/execute-sql
    "SELECT 
      processes.id,
      processes.name,
      processes.description,
      processes.workspace_id,
      to_char(processes.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(processes.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM processes
    INNER JOIN workstation_processes ON processes.id = workstation_processes.process_id
    WHERE workstation_processes.workstation_id = $1
    ORDER BY processes.name"
    {:params [workstation_id]}))

(defn get-available-processes
  "Get all processes for a workspace (processes can be assigned to multiple workstations)"
  [{:keys [workspace_id]}]
  (postgres/execute-sql
    "SELECT 
      processes.id,
      processes.name,
      processes.description,
      processes.workspace_id,
      to_char(processes.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(processes.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM processes
    WHERE processes.workspace_id = $1
    ORDER BY processes.name"
    {:params [workspace_id]}))

(defn assign-process-to-workstation
  "Assign a process to a workstation"
  [{:keys [workstation_id process_id]}]
  (postgres/execute-sql
    "INSERT INTO workstation_processes (workstation_id, process_id, created_at)
    VALUES ($1, $2, CURRENT_TIMESTAMP)
    ON CONFLICT (workstation_id, process_id) DO NOTHING"
    {:params [workstation_id process_id]}))

(defn unassign-process-from-workstation
  "Remove a process from a workstation"
  [{:keys [workstation_id process_id]}]
  (postgres/execute-sql
    "DELETE FROM workstation_processes
    WHERE workstation_id = $1 
      AND process_id = $2"
    {:params [workstation_id process_id]}))

(defn unassign-all-machines-from-workstation
  "Remove all machines from a workstation"
  [{:keys [workstation_id]}]
  (postgres/execute-sql
    "UPDATE machines
    SET workstation_id = NULL,
        updated_at = CURRENT_TIMESTAMP
    WHERE workstation_id = $1"
    {:params [workstation_id]}))

(defn delete-all-workstation-processes
  "Remove all process associations for a workstation"
  [{:keys [workstation_id]}]
  (postgres/execute-sql
    "DELETE FROM workstation_processes WHERE workstation_id = $1"
    {:params [workstation_id]}))

(defn get-batches-with-current-step-on-workstation
  "Get batches that have their current step on this workstation"
  [{:keys [workspace_id workstation_id]}]
  (postgres/execute-sql
    "WITH batch_current_processes AS (
      SELECT 
        b.id as batch_id,
        b.description as batch_name,
        b.quantity,
        b.current_step,
        b.workflow_state,
        parts.name as part_name,
        parts.picture_url as part_picture_url,
        looks.name as color_name,
        looks.basecolor as color_basecolor,
        bp.process_id,
        bp.step_order,
        p.name as process_name
      FROM batches b
      JOIN batch_processes bp ON b.id = bp.batch_id
      JOIN processes p ON bp.process_id = p.id
      JOIN jobs ON b.job_id = jobs.id
      LEFT JOIN parts ON b.part_id = parts.id
      LEFT JOIN looks ON b.look_id = looks.id
      WHERE b.workspace_id = $1
        AND bp.step_order = b.current_step
    )
    SELECT 
      bcp.batch_id,
      bcp.batch_name,
      bcp.quantity,
      bcp.current_step,
      bcp.workflow_state,
      bcp.part_name,
      bcp.part_picture_url,
      bcp.color_name,
      bcp.color_basecolor,
      bcp.process_id,
      bcp.process_name
    FROM batch_current_processes bcp
    JOIN workstation_processes wp ON bcp.process_id = wp.process_id
    WHERE wp.workstation_id = $2
    ORDER BY bcp.batch_name"
    {:params [workspace_id workstation_id]}))

(defn get-workstation-batches
  "Get all batches currently assigned to a specific workstation AND completed batches still visible"
  [{:keys [workspace_id workstation_id]}]
  (postgres/execute-sql
    "WITH batch_processes_info AS (
      SELECT 
        b.id as batch_id,
        b.current_step,
        bp.step_order,
        p.name as process_name,
        LAG(p.name) OVER (PARTITION BY b.id ORDER BY bp.step_order) as previous_process_name,
        LEAD(p.name) OVER (PARTITION BY b.id ORDER BY bp.step_order) as next_process_name
      FROM batches b
      JOIN batch_processes bp ON b.id = bp.batch_id
      JOIN processes p ON bp.process_id = p.id
    )
    SELECT 
      b.id::text as batch_id,
      b.description as batch_name,
      b.quantity,
      b.current_step,
      b.workflow_state,
      b.status,
      to_char(b.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      parts.name as part_name,
      parts.picture_url as part_picture_url,
      looks.name as color_name,
      looks.basecolor as color_basecolor,
      bp.process_id::text,
      bp.step_order,
      p.name as process_name,
      bpi.previous_process_name,
      bpi.next_process_name,
      to_char(bp.start_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as start_time,
      to_char(bp.finish_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as finish_time,
      CASE 
        WHEN b.current_workstation_id = $2 THEN 'current'
        WHEN b.previous_workstation_id = $2 THEN 'previous'
      END as batch_location
    FROM batches b
    JOIN batch_processes bp ON b.id = bp.batch_id AND bp.step_order = b.current_step
    JOIN processes p ON bp.process_id = p.id
    JOIN jobs ON b.job_id = jobs.id
    LEFT JOIN parts ON b.part_id = parts.id
    LEFT JOIN looks ON b.look_id = looks.id
    LEFT JOIN batch_processes_info bpi ON b.id = bpi.batch_id AND bp.step_order = bpi.step_order
    WHERE (
      -- Current batches at this workstation
      b.current_workstation_id = $2
      OR 
      -- Completed batches that haven't started at next workstation yet
      (b.previous_workstation_id = $2 AND b.workflow_state = 'to-do')
    ) AND b.workspace_id = $1
    ORDER BY 
      CASE WHEN b.current_workstation_id = $2 THEN 0 ELSE 1 END,
      b.workflow_state, 
      b.description"
    {:params [workspace_id workstation_id]}))