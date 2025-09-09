(ns features.app.homepage.routes
  #?(:cljs (:require [features.app.homepage.frontend.view :as homepage]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view])))

(def homepage-path "/")

(def routes
  #?(:cljs [{:path homepage-path
             :view #'homepage/view
             :title "Home"}]
     :clj  [{:path homepage-path
             :get #'backend-view/response}])) 