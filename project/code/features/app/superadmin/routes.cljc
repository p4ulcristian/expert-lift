(ns features.app.superadmin.routes
  #?(:cljs (:require [features.app.superadmin.frontend.view :as superadmin]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view])))

(def superadmin-path "/superadmin")

(def routes
  #?(:cljs [{:path superadmin-path
             :view #'superadmin/view
             :title "Super Admin"}]
     :clj  [{:path superadmin-path
             :get #'backend-view/response}]))