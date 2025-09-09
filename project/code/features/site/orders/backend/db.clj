(ns features.site.orders.backend.db
  (:require 
    [zero.backend.state.postgres :as postgres]))

(defn get-orders-by-user-id
  "Get orders for a specific user"
  [user-id]
  (postgres/execute-sql 
   "SELECT 
      id,
      status,
      payment_status,
      payment_intent_id,
      urgency,
      total_amount,
      source,
      to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(due_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as due_date,
      to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
    FROM orders
    WHERE user_id = $1
    ORDER BY created_at DESC"
   {:params [user-id]}))

(defn get-order-by-payment-intent-id
  "Get an order by payment intent ID"
  [payment-intent-id]
  (first 
    (postgres/execute-sql 
     "SELECT * FROM orders WHERE payment_intent_id = $1"
     {:params [payment-intent-id]})))

(defn get-orders-with-jobs-by-user-id
  "Get orders with their jobs, parts and looks for a specific user"
  [user-id]
  (postgres/execute-sql 
   "SELECT
      o.id,
      o.status,
      o.payment_status,
      o.payment_intent_id,
      o.urgency,
      o.source,
      to_char(o.created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
      to_char(o.due_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as due_date,
      to_char(o.updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at,
      COALESCE(
        json_agg(
          CASE WHEN j.id IS NOT NULL THEN
            json_build_object(
              'job_id', j.id,
              'quantity', j.quantity,
              'material', j.material,
              'part_id', p.id,
              'part_name', p.name,
              'part_picture_url', p.picture_url,
              'look_id', l.id,
              'look_name', l.name,
              'basecolor', l.basecolor,
              'color_family', l.color_family,
              'look_thumbnail', l.thumbnail
            )
          END
        ) FILTER (WHERE j.id IS NOT NULL),
        '[]'::json
      ) as jobs
    FROM orders o
    LEFT JOIN jobs j ON j.order_id = o.id
    LEFT JOIN parts p ON p.id = j.part_id
    LEFT JOIN looks l ON l.id = j.look_id
    WHERE o.user_id = $1
    GROUP BY o.id, o.status, o.payment_status, o.payment_intent_id, o.urgency, o.source, o.created_at, o.due_date, o.updated_at
    ORDER BY o.created_at DESC"
   {:params [user-id]}))

(defn get-order-by-id
  "Get a single order by ID and user ID"
  [id user-id]
  (first 
    (postgres/execute-sql 
     "SELECT 
        id,
        status,
        payment_status,
        payment_intent_id,
        urgency,
        source,
        to_char(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as created_at,
        to_char(due_date, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as due_date,
        to_char(updated_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as updated_at
      FROM orders
      WHERE id = $1 AND user_id = $2"
     {:params [id user-id]})))

(defn get-order-jobs-by-order-id
  "Get all jobs with parts and looks for an order"
  [order-id]
  (postgres/execute-sql 
   "SELECT 
      j.id as job_id,
      j.quantity,
      j.material,
      j.current_surface,
      j.description as job_description,
      p.id as part_id,
      p.name as part_name,
      p.picture_url as part_picture_url,
      p.description as part_description,
      l.id as look_id,
      l.name as look_name,
      l.basecolor,
      l.color_family,
      l.thumbnail as look_thumbnail
    FROM jobs j
    LEFT JOIN parts p ON p.id = j.part_id
    LEFT JOIN looks l ON l.id = j.look_id
    WHERE j.order_id = $1"
   {:params [order-id]}))

(defn update-order-payment-status!
  "Update order payment status"
  [payment-status payment-intent-id order-id]
  (postgres/execute-sql 
   "UPDATE orders 
    SET payment_status = $1,
        payment_intent_id = $2,
        updated_at = NOW()
    WHERE id = $3"
   {:params [payment-status payment-intent-id order-id]}))