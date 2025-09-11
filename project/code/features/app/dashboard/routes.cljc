(ns features.app.dashboard.routes
  #?(:cljs (:require [features.app.dashboard.frontend.view :as dashboard]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view])))

(def app-path "/app")

(def routes
  #?(:cljs [{:path app-path
             :view #'dashboard/view
             :title "Dashboard"}]
     :clj  [{:path app-path
             :get #'backend-view/response}]))