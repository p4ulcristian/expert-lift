(ns zero.backend.server.zero
  (:require
   [clojure.data]
   [org.httpkit.server :refer [run-server]]
   [router.backend.zero :as router]
   [zero.backend.tools.log :as log]))

(defn create-server [routes port]
  (log/info-log (str "🚀 Server starting on port " port))
  (run-server (router/create-ring-handler routes)
              {:port port}))




