(ns features.app.worksheets.routes
  #?(:cljs (:require [features.app.worksheets.frontend.view :as worksheets]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view]
                     [router.backend.middleware :refer [wrap-require-authentication]])))

(def worksheets-path "/app/:workspace-id/worksheets")

(def routes
  #?(:cljs [{:path worksheets-path
             :view #'worksheets/view
             :title "Worksheets"}]
     :clj  [{:path worksheets-path
             :get #'backend-view/response
             :middleware [wrap-require-authentication]}]))