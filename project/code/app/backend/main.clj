(ns app.backend.main
  (:gen-class)
  (:require
   [authentication.routes :as authentication-routes]
   [features.customizer.zero.backend.zero :as customizer]
   [features.flex.zero.backend.zero :as flex]
   [features.labs.zero.backend.zero :as labs]
   [features.site.zero.backend.zero :as site]
   [features.site.zero :as site-features]
   [pathom.backend.zero :as pathom]
   [zero.backend.state.env :as env]
   [zero.backend.zero :as zero]
   
   [features.common.stripe.backend.utils :as stripe.utils]
   [features.common.stripe.routes        :as stripe.routes]
   [features.common.health.routes        :as health.routes]
   [parquery.routes                       :as parquery.routes]))

(def pathom-handlers
  (concat
   customizer/pathom-handlers
   labs/pathom-handlers 
   site-features/pathom-handlers))

(def routes
  (concat
   health.routes/routes
   parquery.routes/routes
   site/routes
   customizer/routes
   labs/routes
   flex/routes
   stripe.routes/routes
   pathom/routes
   authentication-routes/routes))

(defn -main [& _args]
  ;; Initialize Stripe
  (stripe.utils/init-stripe!)
  ;; Start server
  (zero/start-server
   {:routes     routes
    :pathom-handlers  pathom-handlers
    :port       @env/port}))
