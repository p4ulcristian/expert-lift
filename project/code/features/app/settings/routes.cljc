(ns features.app.settings.routes
  #?(:cljs (:require [features.app.settings.frontend.view :as settings]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view]
                     [features.app.settings.backend.handlers :as settings-handlers]
                     [router.backend.middleware :refer [wrap-require-authentication]])))

(def settings-path "/app/:workspace-id/settings")

(def routes
  #?(:cljs [{:path settings-path
             :view #'settings/settings-page
             :title "Settings"}]
     :clj  [{:path settings-path
             :get #'backend-view/response
             :middleware [wrap-require-authentication]}
            {:path "/app/:workspace-id/settings/upload-logo"
             :post #'settings-handlers/upload-logo
             :middleware [wrap-require-authentication]}
            {:path "/uploads/logos/:filename"
             :get #'settings-handlers/serve-logo
             :middleware [wrap-require-authentication]}]))