(ns features.flex.jobs.backend.write
  (:require
   [features.flex.jobs.backend.db :as db]))

(defn get-workspace-id
  "Extract workspace ID from params or context"
  [params context]
  (or (:workspace-id params)
      (:workspace/id context)))

(defn validate-job-creation
  "Validate job creation parameters"
  [workspace-id description package-id]
  (cond
    (not workspace-id) {:error "Workspace ID is required"}
    (not description) {:error "Job description is required"}
    (not package-id) {:error "Package ID is required"}
    :else nil))

(defn create-ad-job
  "Create a new ad job"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (get-workspace-id params context)
          order-id (:order-id params)
          description (:description params)
          package-id (:package-id params)
          form-data (:form-data params)
          job-id (java.util.UUID/randomUUID)
          created-at (java.time.Instant/now)]
      
      (if-let [error (validate-job-creation workspace-id description package-id)]
        error
        (do
          (db/create-job job-id
                         workspace-id
                         order-id
                         package-id
                         "pending"
                         description
                         form-data
                         created-at)
          {:job/id job-id
           :job/status "pending"
           :job/description description
           :job/package-id package-id
           :job/form-data form-data
           :job/created-at created-at})))
    (catch Exception e
      (println "Error creating ad job:" (.getMessage e))
      {:error (.getMessage e)})))