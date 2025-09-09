(ns parquery.backend.handlers
  (:require [parquery.backend.view :as view]
            [parquery.backend.query-engine :as query-engine]))

(defn parquery-get-handler
  "Delegates GET requests to view"
  [request]
  (view/render-html request))

(defn parquery-post-handler
  "Delegates POST requests to query engine"
  [request]
  (query-engine/handle-query request))