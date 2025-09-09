(ns zero.backend.tools.ssr
  (:require [clojure.string :as clojure.string]))

(defn lazy-seq? [item]
  (instance? clojure.lang.LazySeq item))

(defn reagent->hiccup-calls [reagent-comp]
  (cond
    (lazy-seq? reagent-comp) ;; Lazy sequence need to be evaluated on server.
    (map reagent->hiccup-calls reagent-comp)
    (vector? reagent-comp)
    (let [[tag & args] reagent-comp]
      (cond
        (fn? tag)    ;;if calls a functions, we need the hiccup behind it
        (reagent->hiccup-calls ; We call recursively again, because the hiccup can contain further fn-s
         (apply tag
                (mapv reagent->hiccup-calls args)))
        (= :<> tag) (map reagent->hiccup-calls args) ;;if we have react fragments
        :else ;;else we iterate through the arguments, they may be further components
        (vec (concat [tag] (mapv reagent->hiccup-calls args)))))
    :else
    reagent-comp))

(defn is-last? [k v style-map]
  (let [[_key _val] (last style-map)]
    (and (= k _key) (= v _val))))

(defn unparse [style-map]
  (letfn [(f [style-string k v] (str style-string (name k) ":" (if (keyword? v) (name v) v) (if
                                                                                             (is-last? k v style-map)
                                                                                              "" ";")))]
    (clojure.string/trim (reduce-kv f "" style-map))))

(defn attr-map->style-in-string [attr-map]
  (if (contains? attr-map :style)
    (assoc attr-map :style (unparse (:style attr-map)))
    attr-map))

(defn attr-map->no-on-click [attr-map]
  (dissoc attr-map :on-click))

(defn reagent->hiccup-style [reagent-comp]
  (cond (vector? reagent-comp) ;; If it's a vector we check if it has a style map
        (let [[tag first-param & args] reagent-comp]
          (if (map? first-param)
            (vec (concat [tag (-> first-param attr-map->style-in-string attr-map->no-on-click)]
                         (mapv reagent->hiccup-style args)))
            (vec (concat [tag] (mapv reagent->hiccup-style (concat [first-param] args))))))
        (lazy-seq? reagent-comp) ;; Lazy sequence need to be evaluated on server.
        (map reagent->hiccup-style reagent-comp)
        :else ;; Else we leave it alone. 
        reagent-comp))
