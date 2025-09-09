(ns sentinel.hot-reload.change-detector
  (:require
   [nextjournal.beholder :as beholder]))

(defn init [{:keys [on-change paths]}] 
  (let [beholder-params (concat [on-change] paths)]
    (apply beholder/watch beholder-params)))