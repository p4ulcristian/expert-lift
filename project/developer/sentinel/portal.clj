(ns sentinel.portal
  (:require [portal.api :as p]
            [clojure.core :as c]))



(def portal-atom (atom nil))

(defn open-portal []
  (reset! portal-atom
          (p/open @portal-atom {:launcher :vs-code})))  ; jvm / node only


(defn close-portal [] 
  (p/close)
  (reset! portal-atom nil))


(defn clear-and-submit [value]
  (open-portal)  ; jvm / node only
  (p/clear)
  (p/submit value))

(add-tap #'clear-and-submit) ; Add portal as a tap> target

;; (tap> :hello) ; Start tapping out values

;;  ; Clear all values

;; (Thread/sleep 1000)
;; (tap> :world) 

(.addShutdownHook (Runtime/getRuntime)
                  (Thread.
                   (fn []
                     (close-portal)
                     (println "Closing opened portal."))))

