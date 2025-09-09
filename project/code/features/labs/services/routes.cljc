(ns features.labs.services.routes
  #?(:cljs (:require [features.labs.services.frontend.services :as services]))
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware])))

(def services-path "/irunrainbow/services")

(def routes
  #?(:cljs [{:path services-path
             :view #'services/view
             :title "Services"}]
     :clj  [{:path services-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]))