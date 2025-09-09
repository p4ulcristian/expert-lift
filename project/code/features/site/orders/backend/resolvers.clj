(ns features.site.orders.backend.resolvers
  (:require
   [clojure.java.io :as io]
   [com.wsscode.pathom3.connect.operation :as pco]
   [features.site.orders.backend.mutations :as mutations]
   [zero.backend.state.postgres :as postgres]
   [features.site.orders.backend.db :as db])
)

;; SQL functions are now in db.clj


(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

(pco/defresolver get-user-id-res
  [{:keys [request] :as _env} _input]
  {::pco/output [:site/user-id]}
  {:site/user-id (get-user-id-from-request request)})

(pco/defresolver get-user-orders-res
  [{:keys [request] :as _env} _input]
  {::pco/output [{:site/user-orders [:id :status :payment_status :payment_intent_id :urgency :source :created_at :due_date :updated_at]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/user-orders (db/get-orders-by-user-id user-id)}
      {:site/user-orders []})))

(pco/defresolver get-user-orders-with-jobs-res
  [{:keys [request] :as _env} _input]
  {::pco/output [{:site/user-orders-with-jobs [:id :status :payment_status :payment_intent_id :urgency :source :created_at :due_date :updated_at :jobs]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/user-orders-with-jobs (db/get-orders-with-jobs-by-user-id user-id)}
      {:site/user-orders-with-jobs []})))


(pco/defresolver get-order-res
  [{:keys [request] :as _env} {:order/keys [id]}]
  {::pco/output [{:site/order [:id :status :payment_status :payment_intent_id :urgency :source :created_at :due_date :updated_at]}]}
  (let [user-id (get-user-id-from-request request)]
    (if user-id
      {:site/order (db/get-order-by-id id user-id)}
      {:site/order nil})))

(pco/defresolver get-order-jobs-res
  [_env {:order/keys [id]}]
  {::pco/output [{:site/order-jobs [:job_id :quantity :material :current_surface :job_description 
                                    :part_id :part_name :part_picture_url :part_description
                                    :look_id :look_name :basecolor :color_family :look_thumbnail]}]}
  {:site/order-jobs (db/get-order-jobs-by-order-id id)})



(def resolvers [get-user-orders-res get-user-orders-with-jobs-res get-user-id-res get-order-res get-order-jobs-res])