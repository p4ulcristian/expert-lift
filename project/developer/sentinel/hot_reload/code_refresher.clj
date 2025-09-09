(ns sentinel.hot-reload.code-refresher
  (:require
   [clj-reload.core :as clj-reload]))

(defn init-ns-reloader [{:keys [paths]}]   
  (clj-reload/init   
   {:dirs    paths
    :output :quieter   
    :no-reload '#{}  
    ;:reload-hook 'after-ns-reload
    }))
 
(defn reload-namespaces []  
  (clj-reload/reload {:throw false})) 