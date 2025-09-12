(ns features.app.addresses.routes
  #?(:cljs (:require [features.app.addresses.frontend.view :as addresses]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view]
                     [router.backend.middleware :refer [wrap-require-authentication]])))

(def addresses-path "/app/:workspace-id/addresses")

(def routes
  #?(:cljs [{:path addresses-path
             :view #'addresses/view
             :title "Addresses"}]
     :clj  [{:path addresses-path
             :get #'backend-view/response
             :middleware [wrap-require-authentication]}]))