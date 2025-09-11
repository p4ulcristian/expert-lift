(ns features.app.workspace.material-templates.backend.db
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-material-templates-by-workspace
  "Get all active material templates for a workspace"
  [workspace-id]
  (postgres/execute-sql 
   "SELECT * FROM expert_lift.material_templates 
    WHERE workspace_id = $1 AND active = true 
    ORDER BY category, name"
   {:params [workspace-id]}))

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