(ns features.labs.zero.backend.zero
  (:require
   [features.labs.looks.routes            :as looks]
   [features.labs.looks.backend.mutations       :as looks.mutations]
   [features.labs.looks.backend.resolvers       :as looks.resolvers]
   [features.labs.looks.backend.draft.mutations :as looks.draft.mutations]
   [features.labs.looks.backend.draft.resolvers :as looks.draft.resolvers]

   [features.labs.dashboard.routes             :as dashboard-routes]
   [features.labs.forms.routes            :as forms-routes]
   [features.labs.forms.backend.resolvers :as forms.resolvers]
   [features.labs.forms.backend.mutations :as forms.mutations]
   [features.labs.parts.routes            :as categories-routes]
   [features.labs.parts.backend.resolvers :as categories.resolvers]
   [features.labs.parts.backend.mutations :as categories.mutations]
   [features.labs.services.routes         :as services-routes]
   [features.labs.services.backend.resolvers :as services.resolvers]
   [features.labs.services.backend.mutations :as services.mutations]
   [features.labs.users.routes            :as users-routes]
   [features.labs.storage.routes            :as storage-routes]
   [features.labs.zip-codes.routes         :as zip-codes-routes]
   [features.labs.zip-codes.backend.resolvers :as zip-codes.resolvers]
   [features.labs.workspaces.routes        :as workspaces-routes]
   [features.labs.workspaces.backend.resolvers :as workspaces.resolvers]
))


(def pathom-handlers
  (concat
    looks.mutations/mutations
    looks.resolvers/resolvers
    looks.draft.mutations/mutations
    looks.draft.resolvers/resolvers
    forms.resolvers/resolvers
    forms.mutations/mutations
    categories.resolvers/resolvers
    categories.mutations/mutations
    services.resolvers/resolvers
    services.mutations/mutations
    zip-codes.resolvers/resolvers
    workspaces.resolvers/resolvers
))

(def routes
  (concat
    looks/routes
    dashboard-routes/routes
    forms-routes/routes
    categories-routes/routes
    services-routes/routes
    users-routes/routes
    storage-routes/routes
    zip-codes-routes/routes
    workspaces-routes/routes
))
