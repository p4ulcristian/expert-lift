(ns features.flex.orders.backend.db
  (:require
   [clojure.set :as set]
   [zero.backend.state.postgres :as postgres]))

(defn transform-customer-json
  "Transform customer JSON from SQL to namespaced keywords"
  [customer]
  (when customer
    (set/rename-keys customer
      {:id :customer/id
       :first_name :customer/first-name
       :last_name :customer/last-name
       :email :customer/email
       :picture_url :customer/picture-url
       :created_at :customer/created-at
       :updated_at :customer/updated-at})))

(defn transform-batch-json
  "Transform batch JSON from SQL to namespaced keywords"
  [batch]
  (when batch
    (set/rename-keys batch
      {:id :batch/id
       :description :batch/description
       :quantity :batch/quantity
       :status :batch/status
       :current_step :batch/current-step
       :workflow_state :batch/workflow-state
       :is_current :batch/is-current
       :part_id :batch/part-id
       :look_id :batch/look-id
       :part_name :batch/part-name
       :part_picture_url :batch/part-picture-url
       :color_name :batch/color-name
       :color_basecolor :batch/color-basecolor
       :created_at :batch/created-at
       :updated_at :batch/updated-at
       :processes :batch/processes})))

(defn transform-job-json
  "Transform job JSON from SQL to namespaced keywords"
  [job]
  (when job
    (-> job
        (set/rename-keys
         {:id :job/id
          :package_id :job/package-id
          :package_name :job/package-name
          :package_description :job/package-description
          :status :job/status
          :description :job/description
          :form_data :job/form-data
          :created_at :job/created-at
          :batches :job/batches})
        (update :job/batches #(when % (mapv transform-batch-json %))))))

(defn transform-order-keys
  "Transform SQL column names to namespaced keywords for orders"
  [order]
  (when order
    (-> order
        (set/rename-keys
         {:id :order/id
          :status :order/status
          :urgency :order/urgency
          :source :order/source
          :user_id :order/user-id
          :workspace_id :order/workspace-id
          :total_amount :order/total-amount
          :payment_intent_id :order/payment-intent-id
          :payment_status :order/payment-status
          :due_date :order/due-date
          :created_at :order/created-at
          :updated_at :order/updated-at
          :customer :order/customer
          :jobs :order/jobs
          :customer_first_name :customer/first-name
          :customer_last_name :customer/last-name
          :customer_email :customer/email
          :customer_name :order/customer-name})
        (update :order/customer transform-customer-json)
        (update :order/jobs #(when % (mapv transform-job-json %))))))

(defn transform-user-keys
  "Transform SQL column names to namespaced keywords for users"
  [user]
  (when user
    (set/rename-keys user
      {:id :user/id
       :first_name :user/first-name
       :last_name :user/last-name
       :email :user/email
       :picture_url :user/picture-url
       :created_at :user/created-at
       :updated_at :user/updated-at})))

(defn transform-job-keys
  "Transform SQL column names to namespaced keywords for jobs"
  [job]
  (when job
    (set/rename-keys job
      {:id :job/id
       :order_id :job/order-id
       :workspace_id :job/workspace-id
       :package_id :job/package-id
       :status :job/status
       :description :job/description
       :form_data :job/form-data
       :created_at :job/created-at
       :package_name :job/package-name
       :package_description :job/package-description
       :batches :job/batches})))

(defn get-order [order-id workspace-id]
  (-> (postgres/execute-sql
       "SELECT 
          orders.id,
          orders.status,
          orders.urgency,
          orders.source,
          orders.user_id,
          orders.workspace_id,
          orders.total_amount,
          orders.payment_intent_id,
          orders.payment_status,
          to_char(orders.due_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as due_date,
          to_char(orders.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
          to_char(orders.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at,
          -- Get customer information
          (SELECT json_build_object(
            'id', u.id,
            'first_name', u.first_name,
            'last_name', u.last_name,
            'email', u.email,
            'picture_url', u.picture_url,
            'created_at', to_char(u.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
            'updated_at', to_char(u.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"')
          )
          FROM users u
          WHERE u.id = orders.user_id) as customer,
          -- Get jobs with their batches
          COALESCE(
            (SELECT json_agg(
              json_build_object(
                'id', j.id,
                'package_id', j.package_id,
                'package_name', pkg.name,
                'package_description', pkg.description,
                'status', j.status,
                'description', j.description,
                'form_data', j.form_data,
                'created_at', to_char(j.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
                'batches', COALESCE(
                  (SELECT json_agg(
                    json_build_object(
                      'id', b.id,
                      'description', b.description,
                      'quantity', b.quantity,
                      'status', b.status,
                      'current_step', b.current_step,
                      'workflow_state', b.workflow_state,
                      'is_current', b.is_current,
                      'part_id', b.part_id,
                      'look_id', b.look_id,
                      'part_name', p.name,
                      'part_picture_url', p.picture_url,
                      'color_name', c.name,
                      'color_basecolor', c.basecolor,
                      'created_at', to_char(b.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
                      'updated_at', to_char(b.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
                      'processes', COALESCE(
                        (SELECT json_agg(
                          json_build_object(
                            'id', bp.process_id,
                            'step_order', bp.step_order,
                            'start_time', to_char(bp.start_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
                            'finish_time', to_char(bp.finish_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
                            'name', pr.name,
                            'description', pr.description
                          ) ORDER BY bp.step_order
                        )
                        FROM batch_processes bp
                        JOIN processes pr ON bp.process_id = pr.id
                        WHERE bp.batch_id = b.id
                        ), '[]'::json
                      )
                    )
                  )
                  FROM batches b
                  LEFT JOIN parts p ON b.part_id = p.id
                  LEFT JOIN looks c ON b.look_id = c.id
                  WHERE b.job_id = j.id 
                  AND b.workspace_id = j.workspace_id
                  ), '[]'::json
                )
              )
            )
            FROM jobs j
            LEFT JOIN packages pkg ON j.package_id = pkg.id
            WHERE j.order_id = orders.id 
            AND j.workspace_id = orders.workspace_id
            ), '[]'::json
          ) as jobs
        FROM orders
        WHERE orders.id = $1 AND orders.workspace_id = $2"
       {:params [order-id workspace-id]})
      first
      transform-order-keys))

(defn get-order-jobs [order-id workspace-id]
  (postgres/execute-sql
   "SELECT 
      j.id,
      j.order_id,
      j.workspace_id,
      j.package_id,
      j.status,
      j.description,
      j.form_data,
      pkg.name as package_name,
      pkg.description as package_description,
      to_char(j.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      (
        SELECT json_agg(
          json_build_object(
            'id', b.id,
            'description', b.description,
            'quantity', b.quantity,
            'status', b.status,
            'current_step', b.current_step,
            'workflow_state', b.workflow_state,
            'is_current', b.is_current,
            'part_id', b.part_id,
            'look_id', b.look_id,
            'part_name', p.name,
            'part_picture_url', p.picture_url,
            'color_name', c.name,
            'color_basecolor', c.basecolor,
            'created_at', to_char(b.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
            'updated_at', to_char(b.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
            'processes', COALESCE(
              (
                SELECT json_agg(
                  json_build_object(
                    'id', bp.process_id,
                    'step_order', bp.step_order,
                    'start_time', to_char(bp.start_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
                    'finish_time', to_char(bp.finish_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"'),
                    'name', pr.name,
                    'description', pr.description
                  ) ORDER BY bp.step_order
                )
                FROM batch_processes bp
                JOIN processes pr ON bp.process_id = pr.id
                WHERE bp.batch_id = b.id
              ),
              '[]'::json
            )
          )
        )
        FROM batches b
        LEFT JOIN parts p ON b.part_id = p.id
        LEFT JOIN looks c ON b.look_id = c.id
        WHERE b.job_id = j.id 
        AND b.workspace_id = j.workspace_id
        AND b.is_current = true
      ) as batches
    FROM jobs j
    LEFT JOIN packages pkg ON j.package_id = pkg.id
    WHERE j.order_id = $1 AND j.workspace_id = $2"
   {:params [order-id workspace-id]}))

(defn get-order-customer [order-id workspace-id]
  (-> (postgres/execute-sql
       "SELECT 
          u.id,
          u.first_name,
          u.last_name,
          u.email,
          u.picture_url,
          to_char(u.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
          to_char(u.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
        FROM users u
        INNER JOIN orders o ON u.id = o.user_id
        WHERE o.id = $1 AND o.workspace_id = $2"
       {:params [order-id workspace-id]})
      first
      transform-user-keys))

(defn get-orders [workspace-id search-term limit offset]
  (->> (postgres/execute-sql
        "SELECT 
           orders.id,
           orders.status,
           orders.urgency,
           orders.source,
           orders.user_id,
           orders.workspace_id,
           to_char(orders.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(orders.due_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as due_date,
           u.first_name as customer_first_name,
           u.last_name as customer_last_name,
           u.email as customer_email
         FROM orders
         LEFT JOIN users u ON orders.user_id = u.id
         WHERE orders.workspace_id = $1
         AND (
           CASE 
             WHEN $2 = '' THEN true
             ELSE (
               LOWER(u.first_name) LIKE LOWER('%' || $2 || '%')
               OR LOWER(u.last_name) LIKE LOWER('%' || $2 || '%')
               OR LOWER(u.email) LIKE LOWER('%' || $2 || '%')
             )
           END
         )
         ORDER BY orders.created_at DESC
         LIMIT CAST($3 AS INTEGER)
         OFFSET CAST($4 AS INTEGER)"
        {:params [workspace-id search-term limit offset]})
       (map transform-order-keys)))

(defn get-recent-orders [workspace-id]
  (->> (postgres/execute-sql
        "SELECT 
           orders.id,
           orders.status,
           orders.urgency,
           orders.source,
           to_char(orders.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
           to_char(orders.due_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as due_date,
           CONCAT(u.first_name, ' ', u.last_name) as customer_name
         FROM orders
         LEFT JOIN users u ON orders.user_id = u.id
         WHERE orders.workspace_id = $1
         ORDER BY orders.created_at DESC
         LIMIT 5"
        {:params [workspace-id]})
       (map (fn [order]
              (-> order
                  transform-order-keys
                  (assoc :order/customer-name (:customer_name order))
                  (dissoc :customer_name))))))

(defn create-order [id workspace-id user-id created-at status urgency source due-date]
  (first (postgres/execute-sql
          "INSERT INTO orders (id, workspace_id, user_id, created_at, status, urgency, source, due_date)
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
           RETURNING id"
          {:params [id workspace-id user-id created-at status urgency source due-date]})))

(defn cancel-order [id]
  (first (postgres/execute-sql
          "UPDATE orders
           SET status = 'cancelled'
           WHERE id = $1
           RETURNING id"
          {:params [id]})))

(defn get-job [job-id workspace-id]
  (-> (postgres/execute-sql
       "SELECT 
          id,
          order_id,
          workspace_id,
          status,
          description,
          to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at
        FROM jobs
        WHERE id = $1 AND workspace_id = $2"
       {:params [job-id workspace-id]})
      first
      transform-job-keys))

(defn update-job [job-id workspace-id material current-surface quantity status type description look-id part-id]
  (first (postgres/execute-sql
          "UPDATE jobs
           SET material = COALESCE($3, material),
               current_surface = COALESCE($4, current_surface),
               quantity = COALESCE($5, quantity),
               status = COALESCE($6, status),
               type = COALESCE($7, type),
               description = COALESCE($8, description),
               look_id = COALESCE($9, look_id),
               part_id = COALESCE($10, part_id)
           WHERE id = $1 AND workspace_id = $2
           RETURNING id, material, current_surface, quantity, status, type, description"
          {:params [job-id workspace-id material current-surface quantity status type description look-id part-id]})))

(defn update-job-status [job-id workspace-id status]
  (first (postgres/execute-sql
          "UPDATE jobs
           SET status = $3
           WHERE id = $1 AND workspace_id = $2
           RETURNING id, status"
          {:params [job-id workspace-id status]})))

(defn create-batch [id workspace-id job-id description quantity status current-step is-current created-at updated-at]
  (first (postgres/execute-sql
          "INSERT INTO batches (id, workspace_id, job_id, description, quantity, status, current_step, is_current, created_at, updated_at)
           VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
           RETURNING id"
          {:params [id workspace-id job-id description quantity status current-step is-current created-at updated-at]})))

(defn get-existing-batch [job-id workspace-id]
  (first (postgres/execute-sql
          "SELECT id, job_id, workspace_id, description, quantity, status, is_current
           FROM batches
           WHERE job_id = $1 AND workspace_id = $2
           LIMIT 1"
          {:params [job-id workspace-id]})))

(defn update-order-status [id status]
  (first (postgres/execute-sql
          "UPDATE orders
           SET status = $2
           WHERE id = $1
           RETURNING id"
          {:params [id status]})))

(defn get-random-user []
  (first (postgres/execute-sql
          "SELECT id
           FROM users
           ORDER BY RANDOM()
           LIMIT 1"
          {:params []})))