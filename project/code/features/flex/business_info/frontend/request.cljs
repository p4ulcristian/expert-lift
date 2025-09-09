(ns features.flex.business-info.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-business-info
  "Get business information using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:business-info/get {:workspace/id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [business-info (:business-info/get response)
                      workspace-key (keyword (str "workspace-" workspace-id))]
                  (callback {workspace-key {:business-info/get business-info}})))}))

(defn save-business-info
  "Save business information using ParQuery"
  [workspace-id data callback]
  (parquery/send-queries
   {:queries {:business-info/save-business-info! {:workspace/id workspace-id
                                                  :business-info/data data}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:business-info/save-business-info! response)]
                  (callback result)))}))