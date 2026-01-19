(ns parquery.backend.query-engine
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [parquery.backend.config :as config]
            [schemas.spec :as spec]
            [utils.wire :as wire]))

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


(defn- parse-json-string
  "Parse JSON string, return empty vector on failure"
  [s]
  (try
    (when (and s (not= s "null"))
      (json/parse-string s true))
    (catch Exception e
      [])))

(defn- process-json-fields
  "Process fields ending in _json - parse them and rename without suffix"
  [m]
  (reduce-kv
   (fn [acc k v]
     (let [k-name (name k)]
       (if (str/ends-with? k-name "_json")
         ;; Rename field and parse JSON
         (let [new-key (keyword (subs k-name 0 (- (count k-name) 5)))]
           (assoc acc new-key (if (string? v) (parse-json-string v) v)))
         (assoc acc k v))))
   {}
   m))

(defn sanitize-for-json
  "Recursively converts database objects to serializable values.
   Also handles _json fields by parsing them and renaming."
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
    (instance? org.pg.clojure.RowMap data)
    (-> (into {} (map (fn [[k v]] [k (sanitize-for-json v)]) data))
        process-json-fields)
    (map? data)
    (-> (into {} (map (fn [[k v]] [k (sanitize-for-json v)]) data))
        process-json-fields)
    (coll? data) (mapv sanitize-for-json data)
    :else (str data)))

(defn transform-response
  "Transform database response to namespaced keywords.
   Handles paginated responses, lists, and single items."
  [data entity-ns]
  (if-not entity-ns
    data
    (cond
      ;; Paginated response: {:worksheets [...] :pagination {...}}
      (and (map? data) (:pagination data))
      (let [data-key (first (filter #(not= % :pagination) (keys data)))
            items (get data data-key)]
        {data-key (wire/wire->keys items entity-ns)
         :pagination (:pagination data)})

      ;; Error response or success response - don't transform
      (and (map? data) (or (:error data) (:success data)))
      data

      ;; List response
      (sequential? data)
      (wire/wire->keys data entity-ns)

      ;; Single item map
      (map? data)
      (wire/wire->keys data entity-ns)

      :else data)))

(defn execute-query
  "Executes a single query by calling its handler function"
  [fn-key fn-params session-atom]
  (let [handler (config/get-query-handler fn-key)
        entity-ns (config/get-entity-ns fn-key)
        result (if handler
                 (try
                   (let [raw-result (handler fn-params)
                         sanitized-result (sanitize-for-json raw-result)
                         ;; Apply wire->keys transformation if entity-ns is defined
                         transformed-result (transform-response sanitized-result entity-ns)
                         validated-result (spec/validate query-result-schema transformed-result (str "query result for " fn-key))]
                     ;; Check for session data in the result and update session if present
                     (when (map? validated-result)
                       (when (contains? validated-result :session-data)
                         (let [session-data (:session-data validated-result)]
                           (if (nil? session-data)
                             (reset! session-atom nil)
                             (swap! session-atom merge session-data)))))
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