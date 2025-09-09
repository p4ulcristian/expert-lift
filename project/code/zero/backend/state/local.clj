(ns zero.backend.state.local)

(def state (atom {}))

(add-watch state :on-change
           (fn [_ _ _ _new-db]
             (println "State changed")
             ;(log/pretty-print new-db)
             ))

(defn reset []
  (reset! state {}))

(defn set-in-db [path value]
  (swap! state assoc-in path value))

(defn dissoc-in-db [path]
  (let [path-to-map     (butlast path)
        key-to-dissoc   (last path)
        original-map    (get-in @state path-to-map)
        updated-map      (dissoc original-map key-to-dissoc)]
    (swap! state assoc-in path-to-map updated-map)))

(defn get-in-db [path]
  (get-in @state path))

(defn get-state []
  @state)
