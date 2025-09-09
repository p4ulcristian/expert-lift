(ns zero.frontend.reveal
  (:require
   [clojure.core.async :refer [<! go-loop timeout]]
   [portal.web :as p]
   [re-frame.core :as r]))

(defn get-db []
  @(r/subscribe [:db/get-in []]))

(defn tap-loop []
  (go-loop [old-db nil]
    (<! (timeout 5000))
    (let [new-db (get-db)]
      (when (not= new-db old-db)
        (tap> (get-db))))
    (recur (get-db))))

(defn inspect-db []
  (p/open {:launcher     :vs-code
           :app          false
           :window-title "Frontend"})
  (add-tap #'p/submit) ; Add portal as a tap> target  
  (tap-loop)) ; Start tapping out values