(ns features.flex.services-pricing.backend.read
  (:require
   [features.flex.services-pricing.backend.db :as db]))

(defn format-service-pricing
  "Format service with pricing data"
  [service]
  (-> service
      (dissoc :id)
      (assoc :pricing {:price (:price service)
                       :active (:is_active service)
                       :pricing-id (:pricing_id service)})
      (dissoc :price :is_active :pricing_id)))

(defn format-services-for-frontend
  "Transform database services and associate them with their pricing"
  [services]
  (mapv (fn [service]
          [(:id service) (format-service-pricing service)])
        services))

(defn get-services
  "Get services with pricing for a workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (let [services (db/get-services-with-pricing workspace-id)]
          (format-services-for-frontend services))))
    (catch Exception e
      (println "Error getting services with pricing:" (.getMessage e))
      [])))

(defn get-service
  "Get single service with pricing by ID"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)
          service-id (:service-id params)]
      (when (and workspace-id service-id)
        (let [service (db/get-service-by-id workspace-id service-id)]
          (when service
            (format-service-pricing service)))))
    (catch Exception e
      (println "Error getting service by ID:" (.getMessage e))
      nil)))