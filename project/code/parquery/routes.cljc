(ns parquery.routes
  #?(:clj (:require [parquery.backend.handlers :as handlers]))
  #?(:cljs (:require [parquery.frontend.view :as view])))

(def parquery-path "/parquery")

(def routes
  #?(:clj [{:path parquery-path
            :get #'handlers/parquery-get-handler
            :post #'handlers/parquery-post-handler}]
     :cljs [{:path parquery-path
             :view #'view/view
             :title "Parquery"}]))