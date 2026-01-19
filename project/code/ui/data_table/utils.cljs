(ns ui.data-table.utils
  "Utility functions for data table components.
   Provides common formatting and display helpers.")

(defn format-date
  "Format ISO date string for display using locale formatting."
  [date-str]
  (when date-str
    (try
      (-> date-str js/Date. (.toLocaleDateString))
      (catch js/Error _
        date-str))))

(defn format-datetime
  "Format ISO datetime string for display with date and time."
  [datetime-str]
  (when datetime-str
    (try
      (let [date (js/Date. datetime-str)]
        (str (.toLocaleDateString date) " " (.toLocaleTimeString date)))
      (catch js/Error _
        datetime-str))))

(defn truncate
  "Truncate string to max-length with ellipsis."
  [s max-length]
  (if (and s (> (count s) max-length))
    (str (subs s 0 (- max-length 3)) "...")
    s))
