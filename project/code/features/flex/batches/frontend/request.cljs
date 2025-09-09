(ns features.flex.batches.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-batches
  "Get batches data using ParQuery"
  [workspace-id job-id callback]
  (parquery/send-queries
   {:queries {:batches/get-current-batches {:job-id job-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [batches (:batches/get-current-batches response)]
                  (callback batches)))}))

(defn get-batch
  "Get single batch using ParQuery"
  [workspace-id batch-id callback]
  (parquery/send-queries
   {:queries {:batch/get {:batch-id batch-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [batch (:batch/get response)]
                  (callback batch)))}))

(defn create-batch
  "Create batch using ParQuery"
  [workspace-id batch-data callback]
  (parquery/send-queries
   {:queries {:batches/create-batch batch-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:batches/create-batch response)]
                  (callback result)))}))

(defn edit-batch
  "Edit batch using ParQuery"
  [workspace-id batch-data callback]
  (parquery/send-queries
   {:queries {:batches/edit-batch batch-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:batches/edit-batch response)]
                  (callback result)))}))