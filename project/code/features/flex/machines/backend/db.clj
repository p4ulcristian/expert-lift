(ns features.flex.machines.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-machines
  "Get all machines for a workspace ordered by name"
  [{:keys [workspace_id]}]
  (->> (postgres/execute-sql
         "SELECT 
           machines.id,
           machines.name,
           machines.description,
           machines.category,
           machines.status,
           machines.location,
           machines.energy_profiles,
           machines.amortization_time_based,
           machines.amortization_usage_based,
           machines.amortization_time_rate,
           machines.amortization_usage_rate,
           machines.usage_unit,
           to_char(machines.last_maintenance, 'YYYY-MM-DD') as last_maintenance,
           machines.maintenance_interval_days,
           to_char(machines.maintenance_due, 'YYYY-MM-DD') as maintenance_due,
           machines.wear_parts,
           machines.workspace_id,
           machines.workstation_id,
           workstations.name as workstation_name,
           to_char(machines.last_used, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as last_used,
           to_char(machines.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(machines.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
         FROM machines
         LEFT JOIN workstations ON machines.workstation_id = workstations.id
         WHERE machines.workspace_id = $1
         ORDER BY machines.name"
         {:params [workspace_id]})
       (mapv (fn [row]
               {:machine/id (:id row)
                :machine/name (:name row)
                :machine/description (:description row)
                :machine/category (:category row)
                :machine/status (:status row)
                :machine/location (:location row)
                :machine/workspace-id (:workspace_id row)
                :machine/workstation-id (:workstation_id row)
                :machine/workstation-name (:workstation_name row)
                :machine/created-at (:created_at row)
                :machine/updated-at (:updated_at row)}))))

(defn get-machine
  "Get a single machine by id"
  [{:keys [id]}]
  (first
    (postgres/execute-sql
      "SELECT 
        machines.id,
        machines.name,
        machines.description,
        machines.category,
        machines.status,
        machines.location,
        machines.energy_profiles,
        machines.amortization_time_based,
        machines.amortization_usage_based,
        machines.amortization_time_rate,
        machines.amortization_usage_rate,
        machines.usage_unit,
        to_char(machines.last_maintenance, 'YYYY-MM-DD') as last_maintenance,
        machines.maintenance_interval_days,
        to_char(machines.maintenance_due, 'YYYY-MM-DD') as maintenance_due,
        machines.wear_parts,
        machines.workspace_id,
        machines.workstation_id,
        workstations.name as workstation_name,
        to_char(machines.last_used, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as last_used,
        to_char(machines.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(machines.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
      FROM machines
      LEFT JOIN workstations ON machines.workstation_id = workstations.id
      WHERE machines.id = $1"
      {:params [id]})))

(defn create-machine
  "Create a new machine with all fields"
  [{:keys [id name description category status location energy_profiles 
           amortization_time_based amortization_usage_based amortization_time_rate 
           amortization_usage_rate usage_unit last_maintenance maintenance_interval_days 
           wear_parts workspace_id workstation_id]}]
  (first
    (postgres/execute-sql
      "INSERT INTO machines (
        id, name, description, category, status, location,
        energy_profiles,
        amortization_time_based, amortization_usage_based, amortization_time_rate, amortization_usage_rate, usage_unit,
        last_maintenance, maintenance_interval_days, maintenance_due,
        wear_parts, workspace_id, workstation_id
      )
      VALUES (
        $1, $2, $3, $4, $5, $6,
        $7::jsonb,
        $8, $9, $10, $11, $12,
        $13::date, $14, 
        ($13::date + INTERVAL '1 day' * $15),
        $16::jsonb, $17, $18
      )
      RETURNING 
        id, name, description, category, status, location,
        energy_profiles,
        amortization_time_based, amortization_usage_based, amortization_time_rate, amortization_usage_rate, usage_unit,
        last_maintenance, maintenance_interval_days, maintenance_due,
        wear_parts, workspace_id,
        to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at"
      {:params [id name description category status location energy_profiles
                amortization_time_based amortization_usage_based amortization_time_rate 
                amortization_usage_rate usage_unit last_maintenance maintenance_interval_days
                maintenance_interval_days wear_parts workspace_id workstation_id]})))

(defn edit-machine
  "Edit a machine with all fields"
  [{:keys [id name description category status location energy_profiles 
           amortization_time_based amortization_usage_based amortization_time_rate 
           amortization_usage_rate usage_unit last_maintenance maintenance_interval_days
           wear_parts workstation_id]}]
  (postgres/execute-sql
    "UPDATE machines
    SET name = $2,
        description = $3,
        category = $4,
        status = $5,
        location = $6,
        energy_profiles = $7::jsonb,
        amortization_time_based = $8,
        amortization_usage_based = $9,
        amortization_time_rate = $10,
        amortization_usage_rate = $11,
        usage_unit = $12,
        last_maintenance = $13::date,
        maintenance_interval_days = $14,
        maintenance_due = ($13::date + INTERVAL '1 day' * $15),
        wear_parts = $16::jsonb,
        workstation_id = $17,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $1"
    {:params [id name description category status location energy_profiles
              amortization_time_based amortization_usage_based amortization_time_rate 
              amortization_usage_rate usage_unit last_maintenance maintenance_interval_days
              maintenance_interval_days wear_parts workstation_id]}))

(defn delete-machine
  "Delete a machine by id"
  [{:keys [id]}]
  (postgres/execute-sql
    "DELETE FROM machines WHERE id = $1"
    {:params [id]}))

(defn update-machine-usage
  "Update machine last used timestamp"
  [{:keys [id]}]
  (postgres/execute-sql
    "UPDATE machines
    SET last_used = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $1"
    {:params [id]}))

(defn get-machines-needing-maintenance
  "Get machines that need maintenance soon or are overdue"
  [{:keys [workspace_id]}]
  (postgres/execute-sql
    "SELECT 
      machines.id,
      machines.name,
      machines.category,
      machines.status,
      machines.last_maintenance,
      machines.maintenance_due,
      machines.maintenance_interval_days,
      (machines.maintenance_due < CURRENT_DATE) as is_overdue,
      (machines.maintenance_due - CURRENT_DATE) as days_until_due
    FROM machines
    WHERE machines.workspace_id = $1
      AND machines.maintenance_due IS NOT NULL
      AND machines.maintenance_due <= (CURRENT_DATE + INTERVAL '7 days')
    ORDER BY machines.maintenance_due ASC"
    {:params [workspace_id]}))

(defn get-machine-stats
  "Get machine statistics for dashboard"
  [{:keys [workspace_id]}]
  (first
    (postgres/execute-sql
      "SELECT 
        COUNT(*) as total_machines,
        COUNT(CASE WHEN status = 'active' THEN 1 END) as active_machines,
        COUNT(CASE WHEN status = 'idle' THEN 1 END) as idle_machines,
        COUNT(CASE WHEN status = 'maintenance' THEN 1 END) as maintenance_machines,
        COUNT(CASE WHEN status = 'down' THEN 1 END) as down_machines,
        COUNT(CASE WHEN maintenance_due < CURRENT_DATE THEN 1 END) as overdue_maintenance,
        COUNT(CASE WHEN maintenance_due <= (CURRENT_DATE + INTERVAL '7 days') THEN 1 END) as upcoming_maintenance
      FROM machines
      WHERE workspace_id = $1"
      {:params [workspace_id]})))