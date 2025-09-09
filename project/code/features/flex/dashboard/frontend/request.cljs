(ns features.flex.dashboard.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn send-email
  "Send feedback email using ParQuery"
  [workspace-id subject body to-email feedback-type callback]
  (parquery/send-queries
   {:queries {:dashboard/send-email {:subject subject
                                     :body body
                                     :to-email to-email
                                     :feedback-type feedback-type
                                     :workspace/id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:dashboard/send-email response)]
                  (callback result)))}))

(defn load-dashboard
  "Load dashboard data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (callback response))}))