(ns sentinel.zero
  (:require
   [sentinel.format :as format]
   [sentinel.hot-reload.zero :as hot-reload]
   [sentinel.nrepl :as nrepl]
   [sentinel.portal]
   [app.backend.main :as app]))


(defn keep-app-alive
  "A function which keeps the main thread alive"
  []
  (while true
    (Thread/sleep 5000)))      


(comment 
  (format/fix ["project/code"]))

(defn develop! [{:keys [paths]}]
  (nrepl/save-nrepl-port! {}) 
  (hot-reload/init  {:paths paths})
  (app/-main)
  (keep-app-alive))
