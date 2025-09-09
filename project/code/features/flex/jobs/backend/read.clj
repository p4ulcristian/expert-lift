(ns features.flex.jobs.backend.read
  (:require
   [features.flex.jobs.backend.db :as db]))

(defn get-job-data
  "Get job data with default empty batches"
  [job-id]
  (let [job (db/get-job job-id)]
    (when job
      (update job :batches #(or % [])))))

(defn get-job
  "Get a single job by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [job-id (:job-id params)]
      (when job-id
        (get-job-data job-id)))
    (catch Exception e
      (println "Error fetching job:" (.getMessage e))
      nil)))

(defn get-jobs
  "Get jobs for a workspace with pagination"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)
          limit (or (:limit params) 10)
          offset (or (:offset params) 0)]
      (when workspace-id
        (db/get-jobs workspace-id limit offset)))
    (catch Exception e
      (println "Error fetching jobs:" (.getMessage e))
      [])))

(defn get-recent-jobs
  "Get recent jobs for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (db/get-recent-jobs workspace-id)))
    (catch Exception e
      (println "Error fetching recent jobs:" (.getMessage e))
      [])))