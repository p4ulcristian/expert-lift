(ns zero.backend.utils.time
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn human-readable-timestamp-full []
  (let [now (LocalDateTime/now)
        formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss")]
    (.format now formatter)))

(defn human-readable-timestamp-date []
  (let [now (LocalDateTime/now)
        formatter (DateTimeFormatter/ofPattern "HH:mm:ss")]
    (.format now formatter)))
