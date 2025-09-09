(ns features.flex.parts-pricing.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-parts
  "Get parts pricing data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:parts-pricing/get-prices {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [parts (:parts-pricing/get-prices response)]
                  (callback {:workspace-parts/get-parts parts})))}))

(defn save-part-pricing
  "Save part pricing using ParQuery"
  [workspace-id pricing-data callback]
  (parquery/send-queries
   {:queries {:parts-pricing/save-pricing pricing-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:parts-pricing/save-pricing response)]
                  (callback {:workspace-parts/save-pricing result})))}))