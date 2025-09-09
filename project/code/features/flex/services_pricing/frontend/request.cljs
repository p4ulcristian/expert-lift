(ns features.flex.services-pricing.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-services
  "Get services pricing data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:services-pricing/get-prices {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [services (:services-pricing/get-prices response)]
                  (callback {:workspace-services-pricing/get-services services})))}))

(defn save-service-pricing
  "Save service pricing using ParQuery"
  [workspace-id pricing-data callback]
  (parquery/send-queries
   {:queries {:services-pricing/save-pricing pricing-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:services-pricing/save-pricing response)]
                  (callback {:workspace-services-pricing/save-pricing result})))}))