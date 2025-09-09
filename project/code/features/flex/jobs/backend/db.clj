(ns features.flex.jobs.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn get-job [job-id]
  (first
   (postgres/execute-sql
    "SELECT 
      jobs.id,
      jobs.order_id,
      jobs.package_id,
      jobs.status,
      jobs.description,
      jobs.form_data,
      packages.name as package_name,
      packages.picture_url as package_picture,
      to_char(jobs.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      (
        SELECT json_agg(
          json_build_object(
            'id', b.id,
            'description', b.description,
            'quantity', b.quantity,
            'status', b.status,
            'current_step', b.current_step,
            'workflow_state', b.workflow_state,
            'created_at', to_char(b.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
            'updated_at', to_char(b.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"')
          )
        )
        FROM batches b
        WHERE b.job_id = jobs.id
        AND b.is_current = true
      ) as batches,
      (
        SELECT json_agg(
          json_build_object(
            'id', p.id,
            'name', p.name,
            'picture_url', p.picture_url,
            'quantity', b.quantity
          )
        )
        FROM batches b
        JOIN parts p ON p.id = b.part_id
        WHERE b.job_id = jobs.id
      ) as selected_parts
    FROM jobs
    LEFT JOIN packages ON packages.id = jobs.package_id
    WHERE jobs.id = $1"
    {:params [job-id]})))

(defn get-jobs [workspace-id limit offset]
  (postgres/execute-sql
   "SELECT 
      jobs.id,
      jobs.order_id,
      jobs.package_id,
      jobs.status,
      jobs.description,
      jobs.form_data,
      packages.name as package_name,
      packages.picture_url as package_picture,
      to_char(jobs.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      (
        SELECT json_agg(
          json_build_object(
            'id', p.id,
            'name', p.name,
            'picture_url', p.picture_url,
            'quantity', b.quantity
          )
        )
        FROM batches b
        JOIN parts p ON p.id = b.part_id
        WHERE b.job_id = jobs.id
      ) as selected_parts
    FROM jobs
    LEFT JOIN packages ON packages.id = jobs.package_id
    WHERE jobs.workspace_id = $1
    ORDER BY jobs.created_at DESC
    LIMIT $2
    OFFSET $3"
   {:params [workspace-id limit offset]}))

(defn get-recent-jobs [workspace-id]
  (postgres/execute-sql
   "SELECT 
      jobs.id,
      jobs.order_id,
      jobs.package_id,
      jobs.status,
      jobs.description,
      jobs.form_data,
      packages.name as package_name,
      packages.picture_url as package_picture,
      to_char(jobs.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      (
        SELECT json_agg(
          json_build_object(
            'id', p.id,
            'name', p.name,
            'picture_url', p.picture_url,
            'quantity', b.quantity
          )
        )
        FROM batches b
        JOIN parts p ON p.id = b.part_id
        WHERE b.job_id = jobs.id
      ) as selected_parts
    FROM jobs
    LEFT JOIN packages ON packages.id = jobs.package_id
    WHERE jobs.workspace_id = $1
    ORDER BY jobs.created_at DESC
    LIMIT 5"
   {:params [workspace-id]}))

(defn create-job [job-id workspace-id order-id package-id status description form-data created-at]
  (postgres/execute-sql
   "INSERT INTO jobs (id, workspace_id, order_id, package_id, status, description, form_data, created_at)
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
    RETURNING to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at"
   {:params [job-id workspace-id order-id package-id status description form-data created-at]}))