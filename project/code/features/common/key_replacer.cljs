
(ns features.common.key-replacer
  (:require
    [cljs.reader    :as cljs.reader]
    [clojure.string :as clojure.string]
    ["mathjs"       :as mathjs]
    [sci.core       :as sci]))

(def STRING-PATTERN  #"\[\"([^\]]+)\]")
(def KEYWORD-PATTERN #"\[:([^\]]+)\]")

(defn replace-keys [formula _fn]
  ;; Replace keys in the string with the value from the function
  (clojure.string/replace
    formula
    STRING-PATTERN
    (fn [[a _]]
      (let [value-path (cljs.reader/read-string a)]
        (_fn value-path)))))

(defn condition-reader [condition _fn]
  ;; Evaluates string return by replace-keys as one or multiple Clojure expressions using the Small Clojure Interpreter.
  (-> (replace-keys condition _fn)
      sci/eval-string))

(defn calc-fn [expr _fn]
  ;; Evaluates string return by replace-keys as a math expression using mathjs
  ;; (println "xx expr" expr)
  (-> (replace-keys expr _fn)
      ;; (#(do (println "formula" %) %))
      mathjs/evaluate))
