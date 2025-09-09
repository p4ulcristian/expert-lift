(ns features.flex.jobs.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-jobs
  "Get jobs data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:jobs/get-jobs {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [jobs (:jobs/get-jobs response)]
                  (callback jobs)))}))

(defn get-job
  "Get single job using ParQuery"
  [workspace-id job-id callback]
  (parquery/send-queries
   {:queries {:jobs/get-job {:job/id job-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [job (:jobs/get-job response)]
                  (callback job)))}))

(defn create-job
  "Create job using ParQuery"
  [workspace-id job-data callback]
  (parquery/send-queries
   {:queries {:jobs/create-job job-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:jobs/create-job response)]
                  (callback result)))}))