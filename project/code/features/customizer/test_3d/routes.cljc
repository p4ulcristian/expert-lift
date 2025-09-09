(ns features.customizer.test-3d.routes
  #?(:clj  (:require [features.customizer.zero.backend.view :as backend-view]))
  #?(:cljs (:require [features.customizer.test-3d.frontend.views :as test-3d])))

(def path "/test-3d")

(def routes
  #?(:clj  [{:path path
             :get  #'backend-view/response}]
     :cljs [{:path path
             :view #'test-3d/main-view}]))