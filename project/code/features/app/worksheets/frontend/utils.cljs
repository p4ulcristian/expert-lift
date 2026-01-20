(ns features.app.worksheets.frontend.utils
  "Utility functions and constants for worksheets feature"
  (:require
   [clojure.string :as str]
   [router.frontend.zero :as router]
   [translations.core :as tr]))

;; =============================================================================
;; Router Helpers
;; =============================================================================

(defn get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state]
    (get-in router-state [:parameters :path :workspace-id])))

;; =============================================================================
;; Validation
;; =============================================================================

(defn- validate-serial-number
  "Validate worksheet serial number - must be at least 2 chars"
  [serial-number]
  (< (count (str/trim (str serial-number))) 2))

(defn- validate-work-description
  "Validate work description - must be at least 5 chars"
  [work-description]
  (< (count (str/trim (str work-description))) 5))

(defn validate-worksheet
  "Validates worksheet data and returns map of field errors"
  [worksheet]
  (let [errors {}
        work-description (:worksheet/work-description worksheet)
        work-type (:worksheet/work-type worksheet)
        service-type (:worksheet/service-type worksheet)
        status (:worksheet/status worksheet)]
    (cond-> errors
      (validate-work-description work-description)
      (assoc :worksheet/work-description "Work description is required (min 5 characters)")

      (empty? work-type)
      (assoc :worksheet/work-type "Work type is required")

      (empty? service-type)
      (assoc :worksheet/service-type "Service type is required")

      (empty? status)
      (assoc :worksheet/status "Status is required")

      (empty? (:worksheet/address-id worksheet))
      (assoc :worksheet/address-id "Address is required"))))

;; =============================================================================
;; Date/Time Helpers
;; =============================================================================

(defn calculate-work-duration
  "Calculate work duration in hours from arrival and departure times.
   Returns nil if times are invalid or departure is before arrival.
   Duration is rounded up to nearest full hour."
  [arrival-time departure-time]
  (when (and arrival-time departure-time
             (not (empty? arrival-time))
             (not (empty? departure-time)))
    (try
      (let [arrival (js/Date. arrival-time)
            departure (js/Date. departure-time)]
        (when (> (.getTime departure) (.getTime arrival))
          (let [diff-ms (- (.getTime departure) (.getTime arrival))
                diff-hours (/ diff-ms 1000 60 60)]
            (Math/ceil diff-hours))))
      (catch js/Error _e
        nil))))

(defn format-datetime-for-input
  "Convert ISO datetime string to datetime-local format (YYYY-MM-DDTHH:mm)"
  [iso-datetime]
  (when (and iso-datetime (not (empty? iso-datetime)))
    (try
      (let [date (js/Date. iso-datetime)
            year (.getFullYear date)
            month (str (.padStart (str (inc (.getMonth date))) 2 "0"))
            day (str (.padStart (str (.getDate date)) 2 "0"))
            hours (str (.padStart (str (.getHours date)) 2 "0"))
            minutes (str (.padStart (str (.getMinutes date)) 2 "0"))]
        (str year "-" month "-" day "T" hours ":" minutes))
      (catch js/Error _e
        ""))))

;; =============================================================================
;; Style Constants
;; =============================================================================

(def status-colors
  "Color mapping for worksheet status badges"
  {"draft"       {:bg "#fef3c7" :color "#92400e"}
   "in_progress" {:bg "#dbeafe" :color "#1e40af"}
   "completed"   {:bg "#d1fae5" :color "#065f46"}
   "cancelled"   {:bg "#fee2e2" :color "#991b1b"}})

(def default-status-color
  {:bg "#f3f4f6" :color "#374151"})

(defn work-type-options
  "Returns work type options with translated labels"
  []
  [["repair" (tr/tr :worksheets/work-type-repair)]
   ["maintenance" (tr/tr :worksheets/work-type-maintenance)]
   ["other" (tr/tr :worksheets/work-type-other)]])

(defn service-type-options
  "Returns service type options with translated labels"
  []
  [["normal" (tr/tr :worksheets/service-type-normal)]
   ["night" (tr/tr :worksheets/service-type-night)]
   ["weekend" (tr/tr :worksheets/service-type-weekend)]
   ["holiday" (tr/tr :worksheets/service-type-holiday)]])

(defn status-options
  "Returns status options with translated labels"
  []
  [["draft" (tr/tr :worksheets/status-draft)]
   ["in_progress" (tr/tr :worksheets/status-in-progress)]
   ["completed" (tr/tr :worksheets/status-completed)]
   ["cancelled" (tr/tr :worksheets/status-cancelled)]])

(defn translate-work-type
  "Translate work type value to localized string"
  [value]
  (case value
    "repair" (tr/tr :worksheets/work-type-repair)
    "maintenance" (tr/tr :worksheets/work-type-maintenance)
    "other" (tr/tr :worksheets/work-type-other)
    value))

(defn translate-service-type
  "Translate service type value to localized string"
  [value]
  (case value
    "normal" (tr/tr :worksheets/service-type-normal)
    "night" (tr/tr :worksheets/service-type-night)
    "weekend" (tr/tr :worksheets/service-type-weekend)
    "holiday" (tr/tr :worksheets/service-type-holiday)
    value))

(defn translate-status
  "Translate status value to localized string"
  [value]
  (case value
    "draft" (tr/tr :worksheets/status-draft)
    "in_progress" (tr/tr :worksheets/status-in-progress)
    "completed" (tr/tr :worksheets/status-completed)
    "cancelled" (tr/tr :worksheets/status-cancelled)
    value))

(def required-fields
  "Set of field keys that are required in worksheet form"
  #{:worksheet/work-description
    :worksheet/work-type
    :worksheet/service-type
    :worksheet/status})

(def input-base-style
  "Base styles for form input fields"
  {:width "100%"
   :padding "0.75rem 1rem"
   :border-radius "8px"
   :font-size "1rem"
   :line-height "1.5"
   :transition "border-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out"
   :outline "none"})

(def input-normal-border
  "1px solid #d1d5db")

(def input-error-border
  "2px solid #dc3545")

(def label-style
  "Base styles for form labels"
  {:display "block"
   :margin-bottom "0.5rem"
   :font-weight "600"
   :font-size "0.875rem"
   :letter-spacing "0.025em"})
