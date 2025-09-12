(ns features.app.material-templates.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-material-templates-by-workspace
  "Get all active material templates for a workspace"
  [workspace-id]
  (postgres/execute-sql 
   "SELECT * FROM expert_lift.material_templates 
    WHERE workspace_id = $1 AND active = true 
    ORDER BY category, name"
   {:params [workspace-id]}))

(defn get-material-templates-paginated
  "Get material templates with server-side filtering, sorting, and pagination"
  [workspace-id {:keys [search sort-by sort-direction page page-size]
                 :or {sort-by "name" sort-direction "asc" page 0 page-size 10}}]
  (let [offset (* page page-size)
        has-search? (and search (not (empty? search)))
        search-condition (if has-search?
                          "AND (LOWER(name) LIKE $2 OR LOWER(category) LIKE $2 OR LOWER(description) LIKE $2 OR LOWER(unit) LIKE $2)"
                          "")
        search-param (when has-search? (str "%" (clojure.string/lower-case search) "%"))
        order-direction (if (= sort-direction "desc") "DESC" "ASC")
        
        ;; Map frontend column names to database columns
        db-column (case sort-by
                    "material-template/name" "name"
                    "material-template/unit" "unit"
                    "material-template/category" "category"
                    "material-template/active" "active"
                    "name")
        
        ;; Build the query parameters
        params (if has-search? 
                [workspace-id search-param page-size offset]
                [workspace-id page-size offset])
        
        query (str "SELECT * FROM expert_lift.material_templates 
                   WHERE workspace_id = $1 " 
                   search-condition
                   " ORDER BY " db-column " " order-direction
                   " LIMIT $" (if has-search? "3" "2")
                   " OFFSET $" (if has-search? "4" "3"))
        
        count-query (str "SELECT COUNT(*) as total 
                         FROM expert_lift.material_templates 
                         WHERE workspace_id = $1 " 
                         search-condition)
        count-params (if has-search? [workspace-id search-param] [workspace-id])]
    
    (let [material-templates (postgres/execute-sql query {:params params})
          total-count (:total (first (postgres/execute-sql count-query {:params count-params})))]
      
      {:material-templates material-templates
       :total-count total-count
       :page page
       :page-size page-size
       :total-pages (Math/ceil (/ total-count page-size))})))

(defn get-material-template-by-id
  "Get material template by ID (within workspace)"
  [template-id workspace-id]
  (postgres/execute-sql 
   "SELECT * FROM expert_lift.material_templates 
    WHERE id = $1 AND workspace_id = $2"
   {:params [template-id workspace-id]}))

(defn create-material-template
  "Create new material template in workspace"
  [workspace-id name unit category description]
  (postgres/execute-sql 
   "INSERT INTO expert_lift.material_templates (workspace_id, name, unit, category, description) 
    VALUES ($1, $2, $3, $4, $5) 
    RETURNING *"
   {:params [workspace-id name unit category description]}))

(defn update-material-template
  "Update existing material template (within workspace)"
  [template-id workspace-id name unit category description active]
  (postgres/execute-sql 
   "UPDATE expert_lift.material_templates 
    SET name = $1, unit = $2, category = $3, description = $4, active = $5, updated_at = NOW()
    WHERE id = $6 AND workspace_id = $7
    RETURNING *"
   {:params [name unit category description active template-id workspace-id]}))

(defn delete-material-template
  "Soft delete material template (within workspace)"
  [template-id workspace-id]
  (postgres/execute-sql 
   "UPDATE expert_lift.material_templates 
    SET active = false, updated_at = NOW() 
    WHERE id = $1 AND workspace_id = $2"
   {:params [template-id workspace-id]}))