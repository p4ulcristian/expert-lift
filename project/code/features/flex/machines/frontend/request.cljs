(ns features.flex.machines.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-machines
  "Get machines data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:machines/get-machines {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [machines (:machines/get-machines response)]
                  (callback machines)))}))

(defn get-machine
  "Get single machine using ParQuery"
  [workspace-id machine-id callback]
  (parquery/send-queries
   {:queries {:machines/get-machine {:workspace-id workspace-id :machine-id machine-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [machine (:machines/get-machine response)]
                  (callback machine)))}))

(defn create-machine
  "Create new machine using ParQuery"
  [workspace-id machine-data callback]
  (parquery/send-queries
   {:queries {:machines/create-machine (merge {:workspace-id workspace-id} machine-data)}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:machines/create-machine response)]
                  (callback result)))}))

(defn save-machine
  "Save machine using ParQuery"
  [workspace-id machine-data callback]
  (parquery/send-queries
   {:queries {:machines/edit-machine (merge {:workspace-id workspace-id} machine-data)}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:machines/edit-machine response)]
                  (callback result)))}))

(defn delete-machine
  "Delete machine using ParQuery"
  [workspace-id machine-id callback]
  (parquery/send-queries
   {:queries {:machines/delete-machine {:workspace-id workspace-id :machine-id machine-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:machines/delete-machine response)]
                  (callback result)))}))