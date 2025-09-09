(ns features.site.zero
  (:require
   [features.site.orders.backend.resolvers :as orders-resolvers]
   [features.site.orders.backend.mutations :as orders-mutations]
   [features.site.profile.backend.resolvers :as profile-resolvers]))

(def pathom-handlers
  (concat
   orders-resolvers/resolvers
   orders-mutations/mutations
   profile-resolvers/resolvers))