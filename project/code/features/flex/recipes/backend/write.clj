(ns features.flex.recipes.backend.write
  (:require
   [features.flex.recipes.backend.db :as db]))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workspace-id params)
      (:workspace/id context)))

(defn add-processes-to-recipe
  "Add processes to a recipe with step order"
  [recipe-id process-ids]
  (when (seq process-ids)
    (doseq [[idx process-id] (map-indexed vector process-ids)]
      (db/add-process-to-recipe {:recipe_id recipe-id 
                                 :process_id process-id 
                                 :step_order (inc idx)}))))

(defn update-recipe-processes
  "Update processes for a recipe, clearing existing ones first"
  [recipe-id process-ids]
  (when (some? process-ids)
    (db/clear-recipe-processes {:recipe_id recipe-id})
    (add-processes-to-recipe recipe-id process-ids)))

(defn create-recipe
  "Create a new recipe with associated processes"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          name (:recipe/name params)
          description (:recipe/description params)
          process-ids (:recipe/process-ids params)
          id (java.util.UUID/randomUUID)]
      (when-let [result (db/create-recipe {:id id
                                           :name name
                                           :description description
                                           :workspace_id workspace-id})]
        (add-processes-to-recipe id process-ids)
        (assoc result :recipe/process-ids process-ids)))
    (catch Exception e
      (println "Error creating recipe:" (.getMessage e))
      {:error (.getMessage e)})))

(defn edit-recipe
  "Edit an existing recipe and update its processes"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:recipe/id params)
          name (:recipe/name params)
          description (:recipe/description params)
          process-ids (:recipe/process-ids params)]
      (when-let [result (db/edit-recipe {:id id :name name :description description})]
        (update-recipe-processes id process-ids)
        (assoc result :recipe/process-ids process-ids)))
    (catch Exception e
      (println "Error editing recipe:" (.getMessage e))
      {:error (.getMessage e)})))

(defn cleanup-recipe-processes
  "Remove all processes associated with a recipe"
  [recipe-id]
  (try
    (db/clear-recipe-processes {:recipe_id recipe-id})
    (catch Exception e
      (println "Error clearing recipe processes:" (.getMessage e)))))

(defn delete-recipe
  "Delete a recipe and its process associations"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [id (:recipe/id params)]
      (cleanup-recipe-processes id)
      (db/delete-recipe {:id id})
      {:recipe/id id})
    (catch Exception e
      (println "Error deleting recipe:" (.getMessage e))
      {:error (.getMessage e)})))