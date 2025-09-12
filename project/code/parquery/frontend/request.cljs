(ns parquery.frontend.request
  (:require [cljs.core.async :refer [go <!]]
            [cljs-http.client :as http]
            [cljs.reader :as reader]))

(defn send-queries
  "Send queries to the parquery backend"
  [query-map]
  (let [callback (:callback query-map)
        request-data (dissoc query-map :callback)]
    (println "DEBUG: ParQuery send-queries called")
    (println "  Request data:" request-data)
    (println "  Queries:" (keys (:queries request-data)))
    (go
      (let [response (<! (http/post "/parquery"
                                    {:transit-params request-data
                                     :with-credentials? true}))]
        (println "DEBUG: ParQuery response received")
        (println "  Status:" (:status response))
        (println "  Body:" (:body response))
        (if (= 200 (:status response))
          (callback (:body response))
          (throw (js/Error (str "ParQuery request failed with status " (:status response) ": " (:body response)))))))))

(defn parse-query-string
  "Parse EDN string into Clojure data"
  [query-string]
  (try
    (reader/read-string query-string)
    (catch js/Error e
      {:success false :error (str "Parse error: " (.-message e))})))