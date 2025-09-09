(ns features.common.health.routes
  #?(:clj  (:require
            [features.common.health.backend.health :as health])))

(def routes
  #?(:clj  [{:path "/health"
             :get #'health/health-check-handler}
            {:path "/health/ready" 
             :get #'health/readiness-check-handler}
            {:path "/health/live"
             :get #'health/liveness-check-handler}]
     :cljs []))