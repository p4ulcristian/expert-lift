
(ns features.common.form.backend.resolver
  (:require 
    [clojure.java.io                       :as io]
    [com.wsscode.pathom3.connect.operation :as pco]
    [zero.backend.state.postgres           :as postgres]
    [features.common.form.backend.db       :as db]))

;; -----------------------------------------------------------------------------
;; ---- SQL ----
;; SQL functions are now in db.clj
;; ---- SQL ----
;; -----------------------------------------------------------------------------

(defn get-form-by-id-fn [env]
  (let [params (-> env pco/params)]
    (try
      (dissoc (db/get-form-by-id (:id params))
              :updated_at :created_at)
      (catch Exception e
        (println "Error getting form by id:" e)
        nil))))

(pco/defresolver get-form-by-id-r [env _]
  {::pco/output [{:form.reader/get-by-id [:id]}]}
  {:form.reader/get-by-id (get-form-by-id-fn env)})

;; ---- Get Form ----
;; -----------------------------------------------------------------------------

(def resolvers [get-form-by-id-r])
