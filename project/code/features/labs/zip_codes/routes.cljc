(ns features.labs.zip-codes.routes
  #?(:cljs (:require [features.labs.zip-codes.frontend.views :as zip-codes]))
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware])))

(def zip-codes-path "/irunrainbow/zip_codes")

(def routes
  #?(:cljs [{:path zip-codes-path
             :view #'zip-codes/view
             :title "Zip Codes"}]
     :clj  [{:path zip-codes-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]))