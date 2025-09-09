(ns features.labs.forms.routes
  #?(:cljs (:require [features.labs.forms.frontend.forms :as forms]))
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware])))

(def forms-path "/irunrainbow/forms")

(def routes
  #?(:cljs [{:path forms-path
             :view #'forms/view
             :title "Forms"}]
     :clj  [{:path forms-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]))