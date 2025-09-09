(ns features.flex.batches.routes
  #?(:cljs (:require [features.flex.batches.frontend.view :as batches]))
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def batches-path "/flex/ws/:workspace-id/batches/:job_id")

(def routes
  #?(:cljs [{:path batches-path
             :view #'batches/view
             :title "Batches"}]
     :clj  [{:path batches-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 