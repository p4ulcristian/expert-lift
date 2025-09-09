(ns sentinel.nrepl
  (:require [nrepl.server :refer [start-server]]))

(def nrepl-server (start-server :port 7888))

(def default-target-path "./.nrepl-port")

(defn save-nrepl-port! [{:keys [path]}]
  (let [port (:port nrepl-server)
        target-path (if path path default-target-path)]
    (println (str "nrepl port saved to " port " -> " target-path))
    (spit target-path port)))


