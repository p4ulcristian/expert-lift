(ns parquery.backend.query-engine
  (:require [cheshire.core :as json]
            [parquery.backend.config :as config]
            [schemas.spec :as spec]))

;; Schema definitions
(def query-request-schema
  [:map
   [:queries [:map-of :keyword :map]]
   [:context {:optional true} [:maybe :map]]
   [:parquery/context {:optional true} [:maybe :map]]])

(def query-result-schema
  "Schema for individual query results"
  :any)  ; Allow any data structure for now - can be made more specific later

(def final-response-schema
  "Schema for the complete parquery response"
  [:map-of :keyword query-result-schema])


(defn sanitize-for-json
  "Recursively converts database objects to serializable values"
  [data]
  (cond
    (nil? data) nil
    (string? data) data
    (number? data) data
    (boolean? data) data
    (keyword? data) data
    (instance? java.util.Date data) (str data)
    (instance? java.sql.Timestamp data) (str data)
    (instance? java.time.LocalDateTime data) (str data)
    (instance? java.time.OffsetDateTime data) (str data)
    (instance? java.time.Instant data) (str data)
    (instance? java.util.UUID data) (str data)
    ;; Handle PostgreSQL RowMap objects
    (instance? org.pg.clojure.RowMap data) (into {} (map (fn [[k v]] [k (sanitize-for-json v)]) data))
    (map? data) (into {} (map (fn [[k v]] [k (sanitize-for-json v)]) data))
    (coll? data) (mapv sanitize-for-json data)
    :else (str data)))

(defn execute-query
  "Executes a single query by calling its handler function"
  [fn-key fn-params session-atom]
  (let [handler (config/get-query-handler fn-key)
        result (if handler
                 (try
                   (let [raw-result (handler fn-params)
                         sanitized-result (sanitize-for-json raw-result)
                         validated-result (spec/validate query-result-schema sanitized-result (str "query result for " fn-key))]
                     ;; Check for session data in the result and update session if present
                     (when (map? validated-result)
                       (when-let [session-data (:session-data validated-result)]
                         (swap! session-atom merge session-data)))
                     ;; Return result without session-data key (only for maps)
                     (if (map? validated-result)
                       (dissoc validated-result :session-data)
                       validated-result))
                   (catch Exception e
                     {:error (.getMessage e)
                      :query fn-key}))
                 {:error "Unknown query"
                  :query fn-key})]
    {fn-key result}))

(defn process-queries
  "Processes all queries, handling read and write queries appropriately"
  [queries-map context request session-atom]
  (let [query-pairs (seq queries-map)
        grouped (group-by #(config/get-query-type (first %)) query-pairs)
        read-queries  (get grouped :read [])
        write-queries (get grouped :write [])
        unknown-queries (get grouped :unknown [])
        ;; Merge context with individual fn-params and add request
        merge-params (fn [[fn-key fn-params]]
                       (let [merged-context (assoc context 
                                                   :user-id (get-in request [:session :user-id]))
                             merged-params (assoc fn-params 
                                                  :parquery/context merged-context
                                                  :parquery/request request)]
                         merged-params))
        ;; Execute queries and return as map
        execute-queries (fn [queries]
                          (into {} 
                                (map (fn [[fn-key fn-params]]
                                       (let [merged-params (merge-params [fn-key fn-params])
                                             result-map (execute-query fn-key merged-params session-atom)
                                             [result-key result] (first (seq result-map))]
                                         [result-key result]))
                                     queries)))
        _ (when (seq unknown-queries)
            (throw (ex-info "Unknown queries found" 
                           {:unknown-queries (map first unknown-queries)})))
        read-results (execute-queries read-queries)
        write-results (execute-queries write-queries)]
    (merge read-results write-results)))

(defn handle-query
  "Processes the parquery request"
  [request]
  (try
    (let [raw-params (:transit-params request)
          params (spec/validate query-request-schema raw-params "parquery request")
          queries (:queries params []) 
          context (or (:parquery/context params) (:context params) {})
          initial-session (:session request)
          session-atom (atom initial-session)
          raw-results (process-queries queries context request session-atom)
          results (spec/validate final-response-schema raw-results "parquery final response")
          updated-session @session-atom]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string results)
       :session updated-session})
      (catch Exception e
        (let [error-data (ex-data e)]
          {:status 400
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string 
                  (cond
                    (:errors error-data)  ; schema validation error
                    {:error (.getMessage e)
                     :validation-errors (:errors error-data)}
                    
                    (:unknown-queries error-data)  ; unknown queries error
                    {:error (.getMessage e)
                     :unknown-queries (:unknown-queries error-data)}
                    
                    :else
                    {:error (.getMessage e)}))}))))