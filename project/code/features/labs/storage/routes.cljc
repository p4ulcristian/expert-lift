
(ns features.labs.storage.routes
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware]))
  #?(:cljs (:require [features.labs.storage.frontend.views :as storage])))

(def path "/irunrainbow/storage")

(def routes
  #?(:clj  [{:path path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]
     :cljs [{:path path
             :view #'storage/view}]))