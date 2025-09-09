(ns features.flex.inventory.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-inventory
  "Get inventory data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:inventory/get-items {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [inventory (:inventory/get-items response)]
                  (callback inventory)))}))

(defn get-inventory-item
  "Get single inventory item using ParQuery"
  [workspace-id inventory-item-id callback]
  (parquery/send-queries
   {:queries {:inventory/get-item {:id inventory-item-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [item (:inventory/get-item response)]
                  (callback item)))}))

(defn save-inventory-item
  "Save inventory item using ParQuery"
  [workspace-id inventory-item-data callback]
  (parquery/send-queries
   {:queries {:inventory-item/save! inventory-item-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:inventory-item/save! response)]
                  (callback result)))}))

(defn create-inventory-item
  "Create inventory item using ParQuery"
  [workspace-id inventory-item-data callback]
  (parquery/send-queries
   {:queries {:inventory/create-inventory-item inventory-item-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:inventory/create-inventory-item response)]
                  (callback result)))}))

(defn edit-inventory-item
  "Edit inventory item using ParQuery"
  [workspace-id inventory-item-data callback]
  (parquery/send-queries
   {:queries {:inventory/edit-inventory-item inventory-item-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:inventory/edit-inventory-item response)]
                  (callback result)))}))

(defn delete-inventory-item
  "Delete inventory item using ParQuery"
  [workspace-id inventory-item-id callback]
  (parquery/send-queries
   {:queries {:inventory/delete-inventory-item {:id inventory-item-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:inventory/delete-inventory-item response)]
                  (callback result)))}))