(ns features.labs.dashboard.routes
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware]))
  #?(:cljs (:require [features.labs.dashboard.frontend.views :as labs])))

(def path "/irunrainbow")

(def routes
  #?(:clj  [{:path path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]
     :cljs [{:path path
             :view #'labs/view}]))