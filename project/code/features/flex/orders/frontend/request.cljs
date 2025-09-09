(ns features.flex.orders.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-orders
  "Get orders data using ParQuery"
  [workspace-id callback]
  (if workspace-id
    (parquery/send-queries
     {:queries {:orders/get-orders {:text ""
                                    :limit 50
                                    :offset 0}}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [orders (:orders/get-orders response)]
                    (callback orders)))})
    (do
      (js/console.warn "get-orders called with nil workspace-id")
      (callback nil))))

(defn get-order
  "Get single order using ParQuery"
  [workspace-id order-id callback]
  (when workspace-id
    (parquery/send-queries
     {:queries {:orders/get-order {:order/id order-id}}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [order (:orders/get-order response)]
                    (callback response)))})))

(defn create-order
  "Create order using ParQuery"
  [workspace-id order-data callback]
  (when workspace-id
    (parquery/send-queries
     {:queries {:orders/create-order order-data}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [result (:orders/create-order response)]
                    (callback result)))})))

(defn edit-order
  "Edit order using ParQuery"
  [workspace-id order-data callback]
  (when workspace-id
    (parquery/send-queries
     {:queries {:orders/edit-order order-data}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [result (:orders/edit-order response)]
                    (callback result)))})))

(defn delete-order
  "Delete order using ParQuery"
  [workspace-id order-id callback]
  (when workspace-id
    (parquery/send-queries
     {:queries {:orders/delete-order {:order/id order-id}}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [result (:orders/delete-order response)]
                    (callback result)))})))

(defn update-job
  "Update job using ParQuery"
  [workspace-id job-data callback]
  (when workspace-id
    (parquery/send-queries
     {:queries {:jobs/update-job job-data}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [result (:jobs/update-job response)]
                    (callback result)))})))

(defn create-jobs-from-configuration
  "Create jobs from configuration using ParQuery"
  [workspace-id order-id callback]
  (when workspace-id
    (parquery/send-queries
     {:queries {:orders/create-jobs-from-configuration! {:order/id order-id}}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [result (:orders/create-jobs-from-configuration! response)]
                    (callback result)))})))

(defn create-batch
  "Create batch using ParQuery"
  [workspace-id batch-data callback]
  (when workspace-id
    (parquery/send-queries
     {:queries {:batches/create-batch! batch-data}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (let [result (:batches/create-batch! response)]
                    (callback result)))})))