(ns features.flex.processes.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-processes
  "Get processes data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:processes/get-processes {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [processes (:processes/get-processes response)]
                  (callback processes)))}))

(defn get-process
  "Get single process using ParQuery"
  [workspace-id process-id callback]
  (parquery/send-queries
   {:queries {:processes/get-process {:workspace-id workspace-id :process/id process-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [process (:processes/get-process response)]
                  (callback process)))}))

(defn create-process
  "Create new process using ParQuery"
  [workspace-id process-data callback]
  (parquery/send-queries
   {:queries {:processes/create-process (merge {:workspace-id workspace-id} process-data)}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:processes/create-process response)]
                  (callback result)))}))

(defn edit-process
  "Edit process using ParQuery"
  [workspace-id process-data callback]
  (parquery/send-queries
   {:queries {:processes/edit-process (merge {:workspace-id workspace-id} process-data)}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:processes/edit-process response)]
                  (callback result)))}))

(defn delete-process
  "Delete process using ParQuery"
  [workspace-id process-id callback]
  (parquery/send-queries
   {:queries {:processes/delete-process {:workspace-id workspace-id :process/id process-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:processes/delete-process response)]
                  (callback result)))}))