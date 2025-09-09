(ns features.labs.parts.backend.resolvers
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [zero.backend.state.postgres :as postgres])
  (:import [java.time.format DateTimeFormatter]))

(defn get-categories-fn [{:keys [_request] :as _env}]
  (try
    (let [categories (postgres/execute-honey
                       {:select [:id :name :description :picture_url :category_id :created_at :order_position]
                        :from [:categories]
                        :order-by [[:order_position :asc] [:name :asc]]})]
      (mapv #(update % :created_at (fn [date] 
                                    (when date 
                                      (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") date))))
            categories))
    (catch Exception e
      (println "Error fetching categories:" (.getMessage e))
      [])))

(pco/defresolver get-categories-res [env _]
  {::pco/output [{:categories/get-categories [:id :name :description :picture_url :category_id :created_at :order_position]}]}
  {:categories/get-categories (get-categories-fn env)})

(defn get-packages-fn [{:keys [_request] :as _env}]
  (try
    (let [packages (postgres/execute-honey
                     {:select [:id :name :description :picture_url :prefix :category_id :model_url :created_at :updated_at :popular :form_id :order_position]
                      :from [:packages]
                      :order-by [[:order_position :asc] [:name :asc]]})]
      (mapv (fn [package]
              (-> package
                  (update :created_at (fn [date] 
                                      (when date 
                                        (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") date))))
                  (update :updated_at (fn [date]
                                      (when date
                                        (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") date))))))
            packages))
    (catch Exception e
      (println "Error fetching packages:" (.getMessage e))
      [])))

(pco/defresolver get-packages-res [env _]  
  {::pco/output [{:packages/get-packages [:id :name :description :picture_url :prefix :category_id :model_url :created_at :updated_at :popular :form_id :order_position]}]}
  {:packages/get-packages (get-packages-fn env)})

(defn get-parts-fn [{:keys [_request] :as _env}]
  (try
    (let [parts (postgres/execute-honey
                  {:select [:id :name :description :picture_url :mesh_id :created_at :updated_at :popular :package_id :form_id :order_position]
                   :from [:parts]
                   :order-by [[:order_position :asc] [:name :asc]]})]
      (mapv (fn [part]
              (-> part
                  (update :created_at (fn [date] 
                                      (when date 
                                        (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") date))))
                  (update :updated_at (fn [date]
                                      (when date
                                        (.format (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") date))))))
            parts))
    (catch Exception e
      (println "Error fetching parts:" (.getMessage e))
      [])))

(pco/defresolver get-parts-res [env _]  
  {::pco/output [{:parts/get-parts [:id :name :description :picture_url :mesh_id :created_at :updated_at :popular :package_id :form_id :order_position]}]}
  {:parts/get-parts (get-parts-fn env)})

(def resolvers [get-categories-res get-packages-res get-parts-res]) 