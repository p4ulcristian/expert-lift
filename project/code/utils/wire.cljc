(ns utils.wire
  "Wire format transformation for keys.

   Converts between database snake_case and Clojure namespaced keywords:
   - {:serial_number \"WS-001\"} + \"worksheet\" → {:worksheet/serial-number \"WS-001\"}
   - {:worksheet/serial-number \"WS-001\"} → {:worksheet__serial_number \"WS-001\"}

   Works in both Clojure and ClojureScript."
  (:require [clojure.string :as str]))

;; -----------------------------------------------------------------------------
;; String Utilities

(defn snake->kebab
  "snake_case → kebab-case"
  [s]
  (str/replace s "_" "-"))

(defn kebab->snake
  "kebab-case → snake_case"
  [s]
  (str/replace s "-" "_"))

;; -----------------------------------------------------------------------------
;; Key Transformation

(defn add-ns
  "Add namespace to a flat keyword, converting snake_case to kebab-case.

   (add-ns \"worksheet\" :serial_number) → :worksheet/serial-number
   (add-ns \"address\" :address_line1) → :address/address-line1"
  [entity-ns k]
  (let [k-name (if (keyword? k) (name k) (str k))]
    (keyword entity-ns (snake->kebab k-name))))

(defn key->wire
  "Convert namespaced keyword to wire format keyword.

   :worksheet/serial-number → :worksheet__serial_number
   :id → :id (non-namespaced pass through)"
  [k]
  (if (keyword? k)
    (let [ns-part (namespace k)
          name-part (name k)]
      (if ns-part
        (keyword (str (kebab->snake ns-part) "__" (kebab->snake name-part)))
        (keyword (kebab->snake name-part))))
    k))

(defn wire->key
  "Convert wire format string/keyword to namespaced keyword.

   \"worksheet__serial_number\" → :worksheet/serial-number
   :worksheet__serial_number → :worksheet/serial-number"
  [s]
  (let [s-str (if (keyword? s) (name s) (str s))]
    (if (str/includes? s-str "__")
      (let [[ns-part name-part] (str/split s-str #"__" 2)]
        (keyword (snake->kebab ns-part) (snake->kebab name-part)))
      (keyword (snake->kebab s-str)))))

;; -----------------------------------------------------------------------------
;; Map Transformation (Wire Format → Clojure Namespaced)

(defn wire-format->keys
  "Transform wire format keys (with __) back to namespaced Clojure keywords.
   Used at react-data-table boundary where namespace is embedded in key.

   {:worksheet__serial_number \"WS-001\"} → {:worksheet/serial-number \"WS-001\"}

   Recursively handles nested structures."
  [data]
  (cond
    (map? data)
    (reduce-kv (fn [acc k v]
                 (assoc acc (wire->key k) (wire-format->keys v)))
               {}
               data)

    (sequential? data)
    (mapv wire-format->keys data)

    :else data))

;; -----------------------------------------------------------------------------
;; Map Transformation (Database → Clojure)

(defn wire->keys
  "Transform flat database keys to namespaced Clojure keywords.

   (wire->keys {:serial_number \"WS-001\" :work_type \"maintenance\"} \"worksheet\")
   => {:worksheet/serial-number \"WS-001\" :worksheet/work-type \"maintenance\"}

   Recursively handles nested structures.
   If entity-ns is nil, converts snake_case to kebab-case without namespace."
  [data entity-ns]
  (cond
    (map? data)
    (reduce-kv (fn [acc k v]
                 (let [new-key (if entity-ns
                                 (add-ns entity-ns k)
                                 (keyword (snake->kebab (name k))))]
                   (assoc acc new-key (wire->keys v entity-ns))))
               {}
               data)

    (sequential? data)
    (mapv #(wire->keys % entity-ns) data)

    :else data))

;; -----------------------------------------------------------------------------
;; Map Transformation (Clojure → Wire/Database)

(defn keys->wire
  "Transform namespaced Clojure keywords to wire format.

   {:worksheet/serial-number \"WS-001\"} → {:worksheet__serial_number \"WS-001\"}

   Recursively handles nested structures."
  [data]
  (cond
    (map? data)
    (reduce-kv (fn [acc k v]
                 (assoc acc (key->wire k) (keys->wire v)))
               {}
               data)

    (sequential? data)
    (mapv keys->wire data)

    :else data))

;; -----------------------------------------------------------------------------
;; Pagination-aware Transformation

(defn transform-paginated-response
  "Transform a paginated response, preserving the wrapper structure.

   (transform-paginated-response
     {:worksheets [{:id 1 :serial_number \"WS-001\"}]
      :pagination {:total-count 50}}
     \"worksheet\")
   => {:worksheets [{:worksheet/id 1 :worksheet/serial-number \"WS-001\"}]
       :pagination {:total-count 50}}"
  [data entity-ns]
  (if (and (map? data) (:pagination data))
    (let [data-key (first (filter #(not= % :pagination) (keys data)))
          items (get data data-key)]
      {data-key (wire->keys items entity-ns)
       :pagination (:pagination data)})
    (wire->keys data entity-ns)))

;; -----------------------------------------------------------------------------
;; Testing

(comment
  ;; Database → Clojure (with namespace)
  (wire->keys {:serial_number "WS-001" :work_type "maintenance"} "worksheet")
  ;; => {:worksheet/serial-number "WS-001" :worksheet/work-type "maintenance"}

  ;; Clojure → Wire format
  (keys->wire {:worksheet/serial-number "WS-001" :worksheet/work-type "maintenance"})
  ;; => {:worksheet__serial_number "WS-001" :worksheet__work_type "maintenance"}

  ;; Nested structures
  (wire->keys {:id 1 :materials [{:name "Paint" :quantity 5}]} "worksheet")
  ;; => {:worksheet/id 1 :worksheet/materials [{:worksheet/name "Paint" :worksheet/quantity 5}]}

  ;; Paginated response
  (transform-paginated-response
   {:worksheets [{:id 1 :serial_number "WS-001"}]
    :pagination {:total-count 50 :page 0}}
   "worksheet")
  ;; => {:worksheets [{:worksheet/id 1 :worksheet/serial-number "WS-001"}]
  ;;     :pagination {:total-count 50 :page 0}}

  ;; Round-trip
  (-> {:worksheet/serial-number "WS-001"}
      keys->wire
      (wire->keys "worksheet"))
  ;; => {:worksheet/serial-number "WS-001"}
  )
