(ns app.backend.main
  (:gen-class)
  (:require
   [features.site.zero.backend.zero :as site]
   [features.site.zero :as site-features]
   [pathom.backend.zero :as pathom]
   [zero.backend.state.env :as env]
   [zero.backend.zero :as zero]
   
   [features.common.health.routes        :as health.routes]
   [parquery.routes                       :as parquery.routes]))

(def pathom-handlers
  (concat
   site-features/pathom-handlers))

(def routes
  (concat
   health.routes/routes
   parquery.routes/routes
   site/routes
   pathom/routes
))

(defn -main [& _args]
  ;; Start server
  (zero/start-server
   {:routes     routes
    :pathom-handlers  pathom-handlers
    :port       @env/port}))
