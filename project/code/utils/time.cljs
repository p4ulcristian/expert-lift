(ns utils.time)

;; 2025-04-21T01:38:03.220863Z
;; Transform date to human readable format in Clojurescript
;; In America date formate

(defn format-date-str [date]
  (let [date-time (js/Date. date)
        options #js {"year" "numeric"
                     "month" "long"
                     "day" "numeric"
                     "hour" "2-digit"
                     "minute" "2-digit"
                     "second" "2-digit"}]
    (.toLocaleString date-time "en-US" options)))

;; And one function without time

(defn format-date-no-time [date]
  (let [date-time (js/Date. date)
        options #js {"year" "numeric"
                     "month" "long"
                     "day" "numeric"}]
    (.toLocaleString date-time "en-US" options)))

;; and one with only time 

(defn format-time [date]
  (let [date-time (js/Date. date)
        options #js {"hour" "2-digit"
                     "minute" "2-digit"
                     "second" "2-digit"}]
    (.toLocaleString date-time "en-US" options)))
