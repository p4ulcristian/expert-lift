(ns features.flex.processes.routes
  #?(:cljs (:require [features.flex.processes.frontend.process :as  process]
                     [features.flex.processes.frontend.processes :as processes])
     :clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def processes-path "/flex/ws/:workspace-id/processes")
(def process-path "/flex/ws/:workspace-id/processes/:process-id")

(def routes
  #?(:cljs [{:path "/flex/ws/:workspace-id/processes"
             :view processes/view}
            {:path "/flex/ws/:workspace-id/processes/:process-id"
             :view process/view}]
     :clj  [{:path processes-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path process-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 