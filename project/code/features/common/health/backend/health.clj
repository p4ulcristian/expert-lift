(ns features.common.health.backend.health
  (:require
   [zero.backend.state.postgres :as postgres]
   [zero.backend.tools.log :as log]))

(defn check-database-connectivity
  "Tests database connection and returns status"
  []
  (try
    (postgres/execute-sql "SELECT 1 as health_check")
    {:status :healthy
     :message "Database connection successful"}
    (catch Exception e
      (log/error-log (str "Database health check failed: " (.getMessage e)))
      {:status :unhealthy
       :message "Database connection failed"
       :error (.getMessage e)})))

(defn check-application-status
  "Performs comprehensive health checks"
  []
  (let [db-check (check-database-connectivity)
        overall-status (if (= (:status db-check) :healthy)
                        :healthy
                        :unhealthy)]
    {:status overall-status
     :timestamp (java.time.Instant/now)
     :version "1.0.0"
     :checks {:database db-check}}))

(defn health-check-handler
  "HTTP handler for health check endpoint"
  [_request]
  (let [health-status (check-application-status)
        http-status (if (= (:status health-status) :healthy) 200 503)]
    {:status http-status
     :headers {"Content-Type" "application/json"
               "Cache-Control" "no-cache, no-store, must-revalidate"}
     :body health-status}))

(defn readiness-check-handler
  "HTTP handler for readiness probe (more strict than health)"
  [_request]
  (let [health-status (check-application-status)]
    (if (= (:status health-status) :healthy)
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body {:status "ready"
              :timestamp (java.time.Instant/now)}}
      {:status 503
       :headers {"Content-Type" "application/json"}
       :body {:status "not-ready"
              :timestamp (java.time.Instant/now)
              :reason "Database connectivity issues"}})))

(defn liveness-check-handler
  "HTTP handler for liveness probe (basic app running check)"
  [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body {:status "alive"
          :timestamp (java.time.Instant/now)}})