
(ns features.flex.storage.routes
  #?(:clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware]))
  #?(:cljs (:require [features.flex.storage.frontend.views :as storage])))

(def path "/flex/ws/:workspace-id/storage")

(def routes
  #?(:clj  [{:path       path
             :get        #'backend-view/response
             :middleware [user-middleware/require-flex-role]}]
     :cljs [{:path  path
             :view  #'storage/view
             :title "Storage"}]))