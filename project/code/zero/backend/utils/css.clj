
(ns zero.backend.utils.css
  (:require [clojure.string :as clojure.string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn unparse [n]
  (letfn [(f [style k v] (str style (name k) ": " (if (keyword? v) (name v) v) "; "))]
    (clojure.string/trim (reduce-kv f "" n))))
