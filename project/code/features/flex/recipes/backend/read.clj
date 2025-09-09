(ns features.flex.recipes.backend.read
  (:require
   [features.flex.recipes.backend.db :as db]))

(defn get-process-workstations
  "Get workstations for a process"
  [process-id]
  (try
    (db/get-process-workstations {:process-id process-id})
    (catch Exception e
      (println "Error fetching process workstations:" (.getMessage e))
      [])))

(defn enrich-process-with-workstations
  "Add workstation data to a process"
  [process]
  (let [workstations (get-process-workstations (:process/id process))]
    (assoc process :process/workstations workstations)))

(defn get-recipe-processes-with-workstations
  "Get processes for a recipe with their workstations"
  [recipe-id]
  (try
    (let [processes (db/get-recipe-processes {:recipe_id recipe-id})]
      (mapv enrich-process-with-workstations processes))
    (catch Exception e
      (println "Error fetching recipe processes:" (.getMessage e))
      [])))

(defn enrich-recipe-with-processes
  "Add process data to a recipe"
  [recipe]
  (let [processes (get-recipe-processes-with-workstations (:recipe/id recipe))]
    (assoc recipe :recipe/processes processes)))

(defn get-recipes
  "Get all recipes for a workspace with their processes and workstations"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (let [recipes (db/get-recipes {:workspace_id workspace-id})]
          (map enrich-recipe-with-processes recipes))))
    (catch Exception e
      (println "Error fetching recipes:" (.getMessage e))
      [])))

(defn get-recipe
  "Get a single recipe by ID with its processes and workstations"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:recipe/id params)]
      (when id
        (when-let [recipe (db/get-recipe {:id id})]
          (enrich-recipe-with-processes recipe))))
    (catch Exception e
      (println "Error fetching recipe:" (.getMessage e))
      nil)))

(defn get-recipe-processes
  "Get processes for a specific recipe"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:recipe/id params)]
      (when id
        (db/get-recipe-processes {:recipe_id id})))
    (catch Exception e
      (println "Error fetching recipe processes:" (.getMessage e))
      [])))