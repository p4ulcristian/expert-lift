(ns features.flex.zero.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn fetch-data
  "Fetch generic data using ParQuery"
  [workspace-id queries callback]
  (parquery/send-queries
   {:queries queries
    :parquery/context {:workspace-id workspace-id}
    :callback callback}))