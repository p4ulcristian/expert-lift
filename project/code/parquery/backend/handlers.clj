(ns parquery.backend.handlers
  (:require [parquery.backend.query-engine :as query-engine]))

(defn parquery-get-handler
  "Handles GET requests"
  [request]
  {:status 404
   :body "Not found"})

(defn parquery-post-handler
  "Delegates POST requests to query engine"
  [request]
  (query-engine/handle-query request))