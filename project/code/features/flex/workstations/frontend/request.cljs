(ns features.flex.workstations.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-workstations
  "Get workstations data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-workstations {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [workstations (:workstations/get-workstations response)]
                  (callback workstations)))}))

(defn get-workstation
  "Get single workstation using ParQuery"
  [workspace-id workstation-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-workstation {:workstation-id workstation-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [workstation (:workstations/get-workstation response)]
                  (callback workstation)))}))

(defn create-workstation
  "Create new workstation using ParQuery"
  [workspace-id workstation-data callback]
  (parquery/send-queries
   {:queries {:workstations/create-workstation workstation-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstations/create-workstation response)]
                  (callback result)))}))

(defn edit-workstation
  "Edit existing workstation using ParQuery"
  [workspace-id workstation-data callback]
  (parquery/send-queries
   {:queries {:workstations/edit-workstation workstation-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstations/edit-workstation response)]
                  (callback result)))}))

(defn delete-workstation
  "Delete workstation using ParQuery"
  [workspace-id workstation-id callback]
  (parquery/send-queries
   {:queries {:workstations/delete-workstation {:workstation-id workstation-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstations/delete-workstation response)]
                  (callback result)))}))

(defn get-task-board-data
  "Get task board data using ParQuery"
  [workspace-id workstation-id callback]
  (parquery/send-queries
   {:queries {:workstation/task-board-data {:workstation-id workstation-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [data (:workstation/task-board-data response)]
                  (callback data)))}))

(defn update-batch-status
  "Update batch status using ParQuery"
  [workspace-id batch-id status callback]
  (parquery/send-queries
   {:queries {:workstation/update-batch-status {:batch-id batch-id :status status}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstation/update-batch-status response)]
                  (callback result)))}))

(defn get-workstation-machines
  "Get workstation machines using ParQuery"
  [workspace-id workstation-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-workstation-machines {:workstation-id workstation-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [machines (:workstations/get-workstation-machines response)]
                  (callback machines)))}))

(defn get-workstation-processes
  "Get workstation processes using ParQuery"
  [workspace-id workstation-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-workstation-processes {:workstation-id workstation-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [processes (:workstations/get-workstation-processes response)]
                  (callback processes)))}))

(defn get-available-machines
  "Get available machines using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-available-machines {}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [machines (:workstations/get-available-machines response)]
                  (callback machines)))}))

(defn get-available-processes
  "Get available processes using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-available-processes {}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [processes (:workstations/get-available-processes response)]
                  (callback processes)))}))

(defn assign-machine
  "Assign machine to workstation using ParQuery"
  [workspace-id workstation-id machine-id callback]
  (parquery/send-queries
   {:queries {:workstations/assign-machine {:workstation-id workstation-id :machine-id machine-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstations/assign-machine response)]
                  (callback result)))}))

(defn unassign-machine
  "Unassign machine from workstation using ParQuery"
  [workspace-id machine-id callback]
  (parquery/send-queries
   {:queries {:workstations/unassign-machine {:machine-id machine-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstations/unassign-machine response)]
                  (callback result)))}))

(defn assign-process
  "Assign process to workstation using ParQuery"
  [workspace-id workstation-id process-id callback]
  (parquery/send-queries
   {:queries {:workstations/assign-process {:workstation-id workstation-id :process-id process-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstations/assign-process response)]
                  (callback result)))}))

(defn unassign-process
  "Unassign process from workstation using ParQuery"
  [workspace-id workstation-id process-id callback]
  (parquery/send-queries
   {:queries {:workstations/unassign-process {:workstation-id workstation-id :process-id process-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:workstations/unassign-process response)]
                  (callback result)))}))

(defn get-batches-with-current-step-on-workstation
  "Get batches with current step on workstation using ParQuery"
  [workspace-id workstation-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-batches-with-current-step-on-workstation {:workstation-id workstation-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [batches (:workstations/get-batches-with-current-step-on-workstation response)]
                  (callback batches)))}))

(defn update-batch-workflow-state
  "Update batch workflow state using ParQuery"
  [workspace-id batch-id workflow-state callback]
  (parquery/send-queries
   {:queries {:batches/update-batch-workflow-state {:batch-id batch-id :workflow-state workflow-state}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:batches/update-batch-workflow-state response)]
                  (callback result)))}))

(defn get-workstation-batches
  "Get all workstation batches using ParQuery"
  [workspace-id workstation-id callback]
  (parquery/send-queries
   {:queries {:workstations/get-workstation-batches {:workstation-id workstation-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [batches (:workstations/get-workstation-batches response)]
                  (callback batches)))}))

(defn confirm-batch
  "Confirm batch completion using ParQuery"
  [workspace-id batch-id callback]
  (parquery/send-queries
   {:queries {:batches/confirm-batch {:batch-id batch-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:batches/confirm-batch response)]
                  (callback result)))}))