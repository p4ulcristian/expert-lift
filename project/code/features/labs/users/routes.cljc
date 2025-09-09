(ns features.labs.users.routes
  #?(:cljs (:require
            [features.labs.users.frontend.views :as views]))
  #?(:clj  (:require [features.labs.zero.backend.view :as backend-view]
                     [authentication.middlewares.labs :as labs-middleware])))

(def users-path "/irunrainbow/users")

(def routes
  #?(:cljs [{:path users-path
             :view #'views/view
             :title "Users"}]
     :clj  [{:path users-path
             :get (labs-middleware/require-labs-role #'backend-view/response)}]))
