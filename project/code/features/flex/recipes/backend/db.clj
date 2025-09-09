(ns features.flex.recipes.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-recipes
  "Get all recipes for a workspace"
  [{:keys [workspace_id]}]
  (->> (postgres/execute-sql
         "SELECT 
           id,
           name,
           description,
           workspace_id,
           to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
         FROM recipes
         WHERE workspace_id = $1
         ORDER BY name"
         {:params [workspace_id]})
       (mapv (fn [row]
               {:recipe/id (:id row)
                :recipe/name (:name row)
                :recipe/description (:description row)
                :recipe/workspace-id (:workspace_id row)
                :recipe/created-at (:created_at row)
                :recipe/updated-at (:updated_at row)}))))

(defn get-recipe
  "Get a single recipe by id"
  [{:keys [id]}]
  (when-let [row (first
                   (postgres/execute-sql
                     "SELECT 
                       id,
                       name,
                       description,
                       workspace_id,
                       to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
                       to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
                     FROM recipes
                     WHERE id = $1"
                     {:params [id]}))]
    {:recipe/id (:id row)
     :recipe/name (:name row)
     :recipe/description (:description row)
     :recipe/workspace-id (:workspace_id row)
     :recipe/created-at (:created_at row)
     :recipe/updated-at (:updated_at row)}))

(defn get-recipe-processes
  "Get all processes for a recipe with their step order"
  [{:keys [recipe_id]}]
  (->> (postgres/execute-sql
         "SELECT 
           p.id,
           p.name,
           p.description,
           p.workspace_id,
           to_char(p.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(p.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at,
           rp.step_order
         FROM processes p
         JOIN recipe_processes rp ON rp.process_id = p.id
         WHERE rp.recipe_id = $1
         ORDER BY rp.step_order"
         {:params [recipe_id]})
       (mapv (fn [row]
               {:process/id (:id row)
                :process/name (:name row)
                :process/description (:description row)
                :process/workspace-id (:workspace_id row)
                :process/created-at (:created_at row)
                :process/updated-at (:updated_at row)
                :recipe-process/step-order (:step_order row)}))))

(defn get-process-workstations
  "Get all workstations that can handle a specific process"
  [{:keys [process-id]}]
  (->> (postgres/execute-sql
         "SELECT 
           w.id::text as workstation_id,
           w.name as workstation_name,
           w.description as workstation_description
         FROM workstations w
         INNER JOIN workstation_processes wp ON w.id = wp.workstation_id
         WHERE wp.process_id = $1
         ORDER BY w.name"
         {:params [process-id]})
       (mapv (fn [row]
               {:workstation/id (:workstation_id row)
                :workstation/name (:workstation_name row)
                :workstation/description (:workstation_description row)}))))

(defn create-recipe
  "Create a new recipe"
  [{:keys [id name description workspace_id]}]
  (when-let [row (first
                   (postgres/execute-sql
                     "INSERT INTO recipes (id, name, description, workspace_id)
                     VALUES ($1, $2, $3, $4)
                     RETURNING 
                       id,
                       name,
                       description,
                       workspace_id,
                       to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
                       to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at"
                     {:params [id name description workspace_id]}))]
    {:recipe/id (:id row)
     :recipe/name (:name row)
     :recipe/description (:description row)
     :recipe/workspace-id (:workspace_id row)
     :recipe/created-at (:created_at row)
     :recipe/updated-at (:updated_at row)}))

(defn add-process-to-recipe
  "Add a process to a recipe with step order"
  [{:keys [recipe_id process_id step_order]}]
  (postgres/execute-sql
    "INSERT INTO recipe_processes (recipe_id, process_id, step_order)
    VALUES ($1, $2, $3)"
    {:params [recipe_id process_id step_order]}))

(defn remove-process-from-recipe
  "Remove a process from a recipe"
  [{:keys [recipe_id process_id]}]
  (postgres/execute-sql
    "DELETE FROM recipe_processes
    WHERE recipe_id = $1 AND process_id = $2"
    {:params [recipe_id process_id]}))

(defn clear-recipe-processes
  "Remove all processes from a recipe"
  [{:keys [recipe_id]}]
  (postgres/execute-sql
    "DELETE FROM recipe_processes WHERE recipe_id = $1"
    {:params [recipe_id]}))

(defn edit-recipe
  "Edit a recipe"
  [{:keys [id name description]}]
  (when-let [row (first
                   (postgres/execute-sql
                     "UPDATE recipes
                     SET name = $2,
                         description = $3,
                         updated_at = CURRENT_TIMESTAMP
                     WHERE id = $1
                     RETURNING 
                       id,
                       name,
                       description,
                       workspace_id,
                       to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
                       to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at"
                     {:params [id name description]}))]
    {:recipe/id (:id row)
     :recipe/name (:name row)
     :recipe/description (:description row)
     :recipe/workspace-id (:workspace_id row)
     :recipe/created-at (:created_at row)
     :recipe/updated-at (:updated_at row)}))

(defn delete-recipe
  "Delete a recipe"
  [{:keys [id]}]
  (postgres/execute-sql
    "DELETE FROM recipes WHERE id = $1"
    {:params [id]}))