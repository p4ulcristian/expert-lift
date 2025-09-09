(ns features.common.form.backend.db
  (:require 
    [zero.backend.state.postgres :as postgres]))

(defn get-form-by-id
  "Get a form by its ID"
  [id]
  (first 
    (postgres/execute-sql 
     "SELECT 
        id,
        title,
        template,
        price_formula,
        created_at,
        updated_at
      FROM forms
      WHERE id = $1"
     {:params [id]})))