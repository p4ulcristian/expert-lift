(ns features.flex.processes.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-processes
  "Get all processes ordered by name"
  [{:keys [workspace/id]}]
  (->> (postgres/execute-sql
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
         {:params [workspace/id]})
       (mapv (fn [row]
               {:process/id (:id row)
                :process/name (:name row)
                :process/description (:description row)
                :process/workspace-id (:workspace_id row)
                :process/created-at (:created_at row)
                :process/updated-at (:updated_at row)}))))

(defn get-process-workstations
  "Get all workstations that can handle a specific process"
  [{:keys [process/id]}]
  (->> (postgres/execute-sql
         "SELECT 
           w.id::text as workstation_id,
           w.name as workstation_name,
           w.description as workstation_description
         FROM workstations w
         INNER JOIN workstation_processes wp ON w.id = wp.workstation_id
         WHERE wp.process_id = $1
         ORDER BY w.name"
         {:params [process/id]})
       (mapv (fn [row]
               {:workstation/id (:workstation_id row)
                :workstation/name (:workstation_name row)
                :workstation/description (:workstation_description row)}))))

(defn get-process
  "Get a single process by id"
  [{:keys [process/id]}]
  (when-let [row (first
                   (postgres/execute-sql
                     "SELECT 
                       processes.id,
                       processes.name,
                       processes.description,
                       processes.workspace_id,
                       to_char(processes.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
                       to_char(processes.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
                     FROM processes
                     WHERE processes.id = $1"
                     {:params [process/id]}))]
    {:process/id (:id row)
     :process/name (:name row)
     :process/description (:description row)
     :process/workspace-id (:workspace_id row)
     :process/created-at (:created_at row)
     :process/updated-at (:updated_at row)}))

(defn create-process
  "Create a new process"
  [{:keys [process/id process/name process/description process/workspace-id]}]
  (when-let [row (first
                   (postgres/execute-sql
                     "INSERT INTO processes (id, name, description, workspace_id)
                     VALUES ($1, $2, $3, $4)
                     RETURNING 
                       id,
                       name,
                       description,
                       workspace_id,
                       to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
                       to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at"
                     {:params [process/id process/name process/description process/workspace-id]}))]
    {:process/id (:id row)
     :process/name (:name row)
     :process/description (:description row)
     :process/workspace-id (:workspace_id row)
     :process/created-at (:created_at row)
     :process/updated-at (:updated_at row)}))

(defn edit-process
  "Edit a process by id"
  [{:keys [process/id process/name process/description]}]
  (postgres/execute-sql
    "UPDATE processes
    SET name = $2,
        description = $3,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = $1"
    {:params [process/id process/name process/description]}))

(defn delete-recipe-process-associations
  "Remove all recipe associations for a process"
  [{:keys [process/id]}]
  (postgres/execute-sql
    "DELETE FROM recipe_processes WHERE process_id = $1"
    {:params [process/id]}))

(defn delete-workstation-process-associations
  "Remove all workstation associations for a process"
  [{:keys [process/id]}]
  (postgres/execute-sql
    "DELETE FROM workstation_processes WHERE process_id = $1"
    {:params [process/id]}))

(defn delete-process
  "Delete a process by id"
  [{:keys [process/id]}]
  (postgres/execute-sql
    "DELETE FROM processes WHERE id = $1"
    {:params [process/id]}))