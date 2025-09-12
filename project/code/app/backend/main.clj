(ns app.backend.main
  (:gen-class)
  (:require
   [features.app.zero.backend.zero :as site]
   [zero.backend.state.env :as env]
   [zero.backend.zero :as zero]
   
   [features.common.health.routes        :as health.routes]
   [parquery.routes                       :as parquery.routes]
   [features.app.pdf-generator.routes    :as pdf-generator.routes]))

(def routes
  (concat
   health.routes/routes
   parquery.routes/routes
   pdf-generator.routes/routes
   site/routes))

(defn -main [& _args]
  ;; Start server
  (zero/start-server
   {:routes     routes
    :port       @env/port}))
