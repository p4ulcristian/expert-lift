(ns features.app.zero.backend.zero
  (:require [features.app.homepage.routes :as homepage-routes]
            [features.app.superadmin.routes :as superadmin-routes]
            [features.app.login.routes :as login-routes]
            [features.app.workspace.routes :as workspace-routes]
            [features.app.material-templates.routes :as material-templates-routes]
            [features.app.addresses.routes :as addresses-routes]
            [features.app.teams.routes :as teams-routes]
            [features.app.worksheets.routes :as worksheets-routes]
            [features.app.settings.routes :as settings-routes]
            [features.app.feedback.routes :as feedback-routes]))

(def routes
  (concat homepage-routes/routes
          superadmin-routes/routes
          login-routes/routes
          workspace-routes/routes
          material-templates-routes/routes
          addresses-routes/routes
          teams-routes/routes
          worksheets-routes/routes
          settings-routes/routes
          feedback-routes/routes))
