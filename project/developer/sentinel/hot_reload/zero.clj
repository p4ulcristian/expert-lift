(ns sentinel.hot-reload.zero
  (:require 
   [clojure.string :as clojure.string]
   [sentinel.hot-reload.change-detector :as change-detector]
   [sentinel.hot-reload.code-refresher :as code-refresher]
   ;[zero.backend.app-manager.manager :as manager]
   ;[zero.backend.state.local :as state]
   ))


(defn change-obj->filename [change-obj]
  (let [path           (str (:path change-obj))
        path-splitted  (clojure.string/split path #"/")]
    (last path-splitted)))




(defn init [{:keys [paths]}] 
  (code-refresher/init-ns-reloader {:paths paths})
  (change-detector/init 
   {:on-change (fn [change-obj]
                 (let [filename (change-obj->filename change-obj)]
                   (println "File modified:" filename)
                   (code-refresher/reload-namespaces)
                   ((resolve 'app.backend.main/-main))))
    :paths paths}))



