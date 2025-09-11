(ns features.app.login.routes
  #?(:cljs (:require [features.app.login.frontend.view :as login]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view])))

(def login-path "/login")

(def routes
  #?(:cljs [{:path login-path
             :view #'login/view
             :title "Login"}]
     :clj  [{:path login-path
             :get #'backend-view/response}]))