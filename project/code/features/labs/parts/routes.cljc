(ns features.labs.parts.routes
  #?(:cljs (:require [features.labs.parts.frontend.categories :as categories]))
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware])))

(def categories-path "/irunrainbow/categories")
(def parts-path "/irunrainbow/parts")

(def routes
  #?(:cljs [{:path categories-path
             :view #'categories/view
             :title "Categories"}
            {:path parts-path
             :view #'categories/view
             :title "Parts"}]
     :clj  [{:path categories-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}
            {:path parts-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}])) 