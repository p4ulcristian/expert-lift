(ns zero.backend.utils.map)

(defn vec-of-pairs->map [vec-of-pairs]
  (reduce merge
          (mapv (fn [[k v]] {k v})
                vec-of-pairs)))

(defn dissoc-nil-values-from-map [_map]
  (vec-of-pairs->map
   (filterv (fn [[_k v]] (not (nil? v)))
            _map)))