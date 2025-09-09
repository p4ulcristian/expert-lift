(ns features.customizer.panel.routes
  #?(:clj  (:require [features.customizer.zero.backend.view :as backend-view]))
  #?(:cljs (:require [features.customizer.panel.frontend.views :as customizer])))

(def path "/customize")

(def routes
  #?(:clj  [{:path path
             :get  #'backend-view/response}]
     :cljs [{:path path
             :view #'customizer/view}]))