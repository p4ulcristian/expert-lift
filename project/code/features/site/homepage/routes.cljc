(ns features.site.homepage.routes
  #?(:cljs (:require [features.site.homepage.frontend.view :as homepage]))
  #?(:clj  (:require [features.site.zero.backend.view :as backend-view])))

(def homepage-path "/")

(def routes
  #?(:cljs [{:path homepage-path
             :view #'homepage/view
             :title "Home"}]
     :clj  [{:path homepage-path
             :get #'backend-view/response}])) 