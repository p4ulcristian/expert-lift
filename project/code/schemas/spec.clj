(ns schemas.spec
  (:require
   [clojure.pprint :as pprint]
   [malli.core :as m]
   [malli.error :as me]))


(defmacro validate
  [schema data & [message]] 
  `(let [valid?# (m/validate ~schema ~data)]
     (if valid?#
       ~data
       (let [expl# (m/explain ~schema ~data)
             human# (me/humanize expl#) 
             pretty-msg# (with-out-str (pprint/pprint human#))]
         (let [prefix# (if ~message (str ~message " - ") "")
               error-msg# (str "❌ Validation failed: " prefix# pretty-msg#)]
           (println "ERROR:" error-msg#)  ; Log the error
           (throw (ex-info error-msg# {:errors human#})))))))