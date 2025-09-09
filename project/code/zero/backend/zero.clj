(ns zero.backend.zero
  (:require
   ;[zero.backend.app-manager.manager-requires]
   ;[zero.backend.app-manager.manager :as manager]
   ;; App manager contains integrant managers with defmethods
   [zero.backend.server.zero :as server]
   [mount.core :refer [defstate] :as mount]
   ;[zero.backend.watchers.routes-updater]
   ;; Watcher need to be required to add watches
   ))
(defonce server-atom (atom nil))
(defonce routes-atom (atom nil))
(defonce port-atom (atom nil))

(defn set-routes! [routes]
  (reset! routes-atom routes))

(defn set-port! [port]
  (reset! port-atom port))

(defn set-server! [server]
  (reset! server-atom server))


(defstate server
  :start
  (do
    (println "Starting server...")
    (set-server! (try
                   (server/create-server @routes-atom @port-atom)
                   (catch Exception e
                     (println "Error starting server:" (.getMessage e))
                     nil))))
  :stop
  (do
    (println "Stopping server...")
    (@server-atom)))

(defn start-server [{:keys [routes port]}]
  (set-routes! routes)
  (set-port! port)
  (mount/stop)
  (mount/start))




