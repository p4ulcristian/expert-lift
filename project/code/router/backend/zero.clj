(ns router.backend.zero
  (:require [reitit.ring :as reitit]
            [router.backend.transform :as router-transform]
            [router.backend.middleware :as middleware]))

(defn create-ring-handler [routes]
  (reitit/ring-handler
   (reitit/router
    (router-transform/routes->reitit-routes routes))
   (reitit/routes
    (reitit/create-resource-handler {:path "/"})
    (reitit/create-default-handler))
   (middleware/middleware)))