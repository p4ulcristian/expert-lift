(ns features.flex.jobs.routes
  #?(:cljs (:require [features.flex.jobs.frontend.view :as jobs]
                     [features.flex.jobs.frontend.job :as job]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def jobs-path "/flex/ws/:workspace-id/jobs")
(def job-path "/flex/ws/:workspace-id/jobs/:job-id")

(def routes
  #?(:cljs [{:path jobs-path
             :view #'jobs/view
             :title "Jobs"}
            {:path job-path
             :view #'job/view
             :title "Job"}]
     :clj  [{:path jobs-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path job-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 