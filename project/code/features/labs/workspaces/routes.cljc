(ns features.labs.workspaces.routes
  #?(:cljs (:require [features.labs.workspaces.frontend.views :as workspaces]))
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware])))

(def workspaces-path "/irunrainbow/workspaces")

(def routes
  #?(:cljs [{:path workspaces-path
             :view #'workspaces/view
             :title "Coating Partners"}]
     :clj  [{:path workspaces-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]))