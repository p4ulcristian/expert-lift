(ns features.flex.zero.backend.zero
  (:require
   [features.flex.dashboard.routes   :as dashboard-routes]
   [features.flex.storage.routes   :as storage.routes]
   [features.flex.workspaces.routes  :as workspaces.routes] 
   [features.flex.orders.routes      :as orders.routes]
   [features.flex.jobs.routes        :as jobs.routes]
   [features.flex.services-pricing.routes    :as services.routes]
   [features.flex.processes.routes   :as processes.routes]
   [features.flex.recipes.routes     :as recipes.routes]
   [features.flex.inventory.routes   :as inventory.routes]
   [features.flex.workstations.routes :as workstations.routes]
   [features.flex.batches.routes      :as batches.routes]
   [features.flex.parts-pricing.routes       :as parts.routes]
   [features.flex.locations.routes  :as locations.routes]
   [features.flex.machines.routes   :as machines.routes]
   [features.flex.service-areas.routes :as service-areas.routes]
   [features.flex.business-info.routes :as business-info.routes]
   [features.flex.teams.routes :as teams.routes]

   [features.common.storage.backend.resolvers :as storage.resolvers]
   [features.common.storage.backend.mutations :as storage.mutations]

   
   [users.backend.resolvers :as users.resolvers]
   [users.backend.mutations :as users.mutations]
   
   
   
  ))

(def pathom-handlers
  (concat
   storage.resolvers/resolvers
   storage.mutations/mutations


   users.resolvers/resolvers
   users.mutations/mutations
   
   ))

(def routes
  (concat
   dashboard-routes/routes
   storage.routes/routes
   workspaces.routes/routes
   orders.routes/routes
   jobs.routes/routes
   services.routes/routes
   processes.routes/routes
   recipes.routes/routes
   inventory.routes/routes
   workstations.routes/routes
   batches.routes/routes
   parts.routes/routes
   locations.routes/routes
   machines.routes/routes
   service-areas.routes/routes 
   business-info.routes/routes
   teams.routes/routes
))
