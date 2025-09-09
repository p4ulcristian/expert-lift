(ns sentinel.format
  (:require [cljfmt.tool  :as format-tool]
            [cljfmt.report :as report]))

(defn check [paths]
  (format-tool/check 
   {:paths paths
    :report report/clojure}))

(defn fix [paths]
  (format-tool/fix
   {:paths  paths
    :report report/clojure}))
