(ns zero.backend.server.zero
  (:require
   [clojure.data]
   [org.httpkit.server :refer [run-server]]
   [router.backend.zero :as router]))

(defn create-server [routes port]
  (run-server (router/create-ring-handler routes)
              {:port port}))




