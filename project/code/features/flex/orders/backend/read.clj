(ns features.flex.orders.backend.read
  (:require
   [features.flex.orders.backend.db :as db]))

(defn transform-order-list-item
  "Transform order data for list display"
  [order]
  (-> order
      (assoc :order/customer {:customer/first-name (:customer/first-name order)
                              :customer/last-name (:customer/last-name order)
                              :customer/email (:customer/email order)})
      (dissoc :customer/first-name :customer/last-name :customer/email)))

(defn get-order
  "Get complete order data by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [order-id (:order/id params)
          workspace-id (or (:workspace-id context) 
                           (get-in params [:parquery/request :transit-params :parquery/context :workspace-id]))]
      (when (and order-id workspace-id)
        (db/get-order order-id workspace-id)))
    (catch Exception e
      (println "Error fetching order:" (.getMessage e))
      nil)))

(defn get-my-orders
  "Get orders with search and pagination"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (or (:workspace-id context) 
                           (get-in params [:parquery/request :transit-params :parquery/context :workspace-id]))
          search-term (str (or (:text params) ""))
          query-limit (int (or (:limit params) 10))
          query-offset (int (or (:offset params) 0))]
      (when workspace-id
        (let [raw-orders (db/get-orders workspace-id search-term query-limit query-offset)]
          (map transform-order-list-item raw-orders))))
    (catch Exception e
      (println "Error fetching orders:" (.getMessage e))
      [])))

(defn get-recent-orders
  "Get recent orders for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id context)]
      (when workspace-id
        (db/get-recent-orders workspace-id)))
    (catch Exception e
      (println "Error getting recent orders:" (.getMessage e))
      [])))

(defn get-job
  "Get job data by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [job-id (:job-id params)
          workspace-id (:workspace-id context)]
      (when (and job-id workspace-id)
        (db/get-job job-id workspace-id)))
    (catch Exception e
      (println "Error getting job:" (.getMessage e))
      nil)))