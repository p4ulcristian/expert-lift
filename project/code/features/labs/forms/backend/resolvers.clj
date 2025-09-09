
(ns features.labs.forms.backend.resolvers
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [zero.backend.state.postgres :as postgres]))

(defn get-forms-data-fn [{:keys [_request] :as _env}]
  (try
    (->>
      (postgres/execute-honey
        {:select [:*]
         :from   [:forms]})
      (map #(-> % (update :created_at str)
                  (update :updated_at str))))
    
    (catch Exception e
      (println "Error fetching form data:" (.getMessage e))
      nil)))

(pco/defresolver get-forms-res [env _]
  {:forms/get-forms (get-forms-data-fn env)})

(def resolvers [get-forms-res])