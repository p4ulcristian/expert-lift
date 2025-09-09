(ns features.flex.service-areas.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-service-areas
  "Get service areas data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:service-areas/get-areas {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [service-areas (:service-areas/get-areas response)]
                  (callback {:service-areas/get-service-areas service-areas})))}))

(defn save-service-areas
  "Save service areas using ParQuery"
  [workspace-id service-areas-data callback]
  (parquery/send-queries
   {:queries {:service-areas/save! service-areas-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:service-areas/save! response)]
                  (callback result)))}))

(defn save-business-info
  "Save business info from service areas using ParQuery"
  [workspace-id business-info-data callback]
  (parquery/send-queries
   {:queries {:business-info/save-business-info! {:business-info/data business-info-data}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:business-info/save-business-info! response)]
                  (callback result)))}))

(defn get-business-info
  "Get business info using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:business-info/get {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [business-info (:business-info/get response)]
                  (callback response)))}))