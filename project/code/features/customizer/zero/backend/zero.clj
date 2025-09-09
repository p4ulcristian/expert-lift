(ns features.customizer.zero.backend.zero
  (:require 
    [features.customizer.checkout.routes            :as checkout.routes]
    [features.customizer.checkout.backend.mutations :as checkout.mutations]

    [features.customizer.panel.routes            :as customizer.routes]
    [features.customizer.panel.backend.resolvers :as customizer.resolvers]
    
    [features.customizer.test-3d.routes          :as test-3d.routes]
   
    [features.common.form.backend.resolver :as form.resolver]))

(def pathom-handlers
  (concat checkout.mutations/mutations
          customizer.resolvers/resolvers
          form.resolver/resolvers))

(def routes
  (concat customizer.routes/routes
          checkout.routes/routes
          test-3d.routes/routes))

