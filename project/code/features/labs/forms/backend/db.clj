(ns features.labs.forms.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn create-form
  "Save a new form to the database"
  [{:keys [id title template price_formula]}]
  (postgres/execute-sql
   "INSERT INTO forms (
        id,
        title,
        template,
        price_formula,
        created_at
    ) VALUES (
        $1,
        $2,
        $3,
        $4,
        NOW()
    ) RETURNING *"
   {:params [id title template price_formula]}))

(defn update-form
  "Update a form by id, updating title and template if it exists"
  [{:keys [id title template price_formula]}]
  (postgres/execute-sql
   "UPDATE forms
    SET title = COALESCE($2, title),
        template = COALESCE($3, template),
        price_formula = COALESCE($4, price_formula)
    WHERE id = $1
    RETURNING *"
   {:params [id title template price_formula]}))

(defn delete-form
  "Delete a form by id"
  [{:keys [id]}]
  (postgres/execute-sql
   "DELETE FROM forms
    WHERE id = $1"
   {:params [id]}))