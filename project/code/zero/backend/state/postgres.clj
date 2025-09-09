(ns zero.backend.state.postgres
  (:require
   [pg.core :as pg]
   [pg.honey :as pgh]
   [zero.backend.state.env :as env]))

(def sql-log-lock (Object.))

(defn log-sql-safely
  "Thread-safe SQL logging"
  [sql & [params]]
  (locking sql-log-lock
    (if params
      (println "Executing SQL with params:" sql params)
      (println "Executing SQL:" sql))))

(def config
  (delay
    {:host @env/db-host
     :port @env/db-port
     :user @env/db-user
     :password @env/db-password
     :database @env/db-name
     :pool-min-size 5
     :pool-max-size 20
     :pool-expire-threshold-ms 600000
     :pool-borrow-conn-timeout-ms 30000
     :pool-conn-timeout-ms 30000}))

(declare conn)

(def db-pool
  (delay (pg/pool @config)))


(defn with-pool [f]
  (pg/with-connection [conn @db-pool]
    (f conn)))

(defn retry-with-backoff 
  "Retry a function with exponential backoff"
  [f max-retries]
  (loop [retries 0]
    (let [result (try
                   {:success (f)}
                   (catch Exception e
                     {:error e}))]
      (if (:success result)
        (:success result)
        (if (>= retries max-retries)
          (throw (:error result))
          (do
            (Thread/sleep (long (* 100 (Math/pow 2 retries))))
            (recur (inc retries))))))))

(defn execute-sql 
  ([sql]
   (retry-with-backoff
    #(do 
       (pg/execute @db-pool sql))
    3))
  ([sql params]
   (retry-with-backoff 
    #(do
       (pg/execute @db-pool sql params))
    3)))

(defn execute-honey [honey]
  (retry-with-backoff
    #(with-pool
       (fn [conn]
         (pgh/execute conn honey)))
    3))

(defn namespaced-map->name-map [data]
  ;; Careful!
  ;; No more then one :some/id in the map allowed

  (reduce merge
          (map (fn [[k v]]
                 {(keyword (name k)) v})
               data)))

(defn data->pg-item [data]
  {:id (:id (namespaced-map->name-map data))
   :data [:lift data]})

(data->pg-item {:customer/id (java.util.UUID/randomUUID)
                :customer/name "Ivean"
                :customer/address {:address/name "moszkvai krt 33"}})

(defn insert-honey [table data]
  (with-pool
    (fn [conn]
      (mapv :data
            (pgh/insert conn table [(data->pg-item data)]
                        {:returning [:id :data]})))))

(defn insert--beta [table data]
  (with-pool
    (fn [conn]
      (try
        (pgh/insert conn table [data]
                    {:returning [:*]})
        (catch Exception e
          (println "Error inserting data:" (.getMessage e))
          (throw e))))))

(defn insert--gamma [table params]
  (with-pool
    (fn [conn]
      (try
        (pg/execute conn
                    (str "insert into " table " () values ()")
                    {:params params})
        (catch Exception e
          (println "Error inserting data:" (.getMessage e))
          (throw e))))))

(defn create-default-table [table]
  (execute-sql
   (str "create table " (name table) " (
    id uuid primary key,
    data jsonb not null 
  )")))

(defn drop-table [table]
  (execute-sql (str "drop table " (name table))))

(defn show-table [table]
  (execute-sql (str "select * from " (name table))))

;; Helpers

(defn keyword->string [k]
  (str
   (namespace k)
   "/"
   (name k)))

(defn select-in-jsonb [column path]
  [:raw (str (name column)
             (apply str
                    (map
                     (fn [k] (str "->'" (keyword->string k) "'"))
                     path))
             " as " (name column))])



