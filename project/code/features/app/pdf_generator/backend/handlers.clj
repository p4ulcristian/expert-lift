(ns features.app.pdf-generator.backend.handlers
  "ParQuery handlers for PDF generation"
  (:require
   [features.app.pdf-generator.backend.pdf :as pdf]
   [features.app.worksheets.backend.db :as worksheets-db]
   [features.app.settings.backend.db :as settings-db]
   [malli.core :as m]
   [cheshire.core]))

(def work-report-schema
  "Schema for work report data validation"
  [:map
   [:institution-name {:optional true} [:string {:min 1}]]
   [:institution-address {:optional true} [:string {:min 1}]]
   [:work-type {:optional true} [:enum "normal" "night" "weekend"]]
   [:arrival-time {:optional true} [:string]]
   [:departure-time {:optional true} [:string]]
   [:work-description {:optional true} [:string]]
   [:materials-used {:optional true} [:vector :any]]
   [:notes {:optional true} [:string]]
   [:date {:optional true} [:string]]])

(defn validate-work-report-data
  "Validate work report data against schema"
  [data]
  (when-not (m/validate work-report-schema data)
    (throw (ex-info "Invalid work report data" 
                   {:data data
                    :errors (m/explain work-report-schema data)}))))

(defn- get-workspace-logo-path
  "Get workspace logo file path if it exists"
  [workspace-id]
  (try
    (let [upload-base-dir (or (System/getenv "EXPERT_LIFT_UPLOAD_DIR") "uploads")
          extensions ["jpg" "jpeg" "png" "gif"]]
      (loop [exts extensions]
        (if (empty? exts)
          nil
          (let [ext (first exts)
                file-path (str upload-base-dir "/logos/" workspace-id "." ext)
                logo-file (java.io.File. file-path)]
            (if (.exists logo-file)
              file-path
              (recur (rest exts)))))))
    (catch Exception e
      (println "Error checking workspace logo:" (.getMessage e))
      nil)))

(defn generate-work-report
  "Generate work report PDF"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id context)
        work-report-data (-> (dissoc params :parquery/context :parquery/request)
                           (assoc :workspace-logo-path (get-workspace-logo-path workspace-id)))]
    (validate-work-report-data work-report-data)
    (pdf/generate-work-report-pdf work-report-data)))

(defn generate-sample-work-report
  "Generate sample work report for testing"
  [{:parquery/keys [context request]}]
  (pdf/create-sample-work-report))

(defn- format-time-from-iso
  "Convert ISO datetime or OffsetDateTime to HH:mm format for PDF template"
  [datetime-obj]
  (println "DEBUG: format-time-from-iso called with:" datetime-obj "type:" (type datetime-obj))
  (when datetime-obj
    (try
      (let [formatted (cond
                        ;; If it's already a java.time object, format directly
                        (instance? java.time.OffsetDateTime datetime-obj)
                        (str (.format datetime-obj (java.time.format.DateTimeFormatter/ofPattern "HH:mm")))
                        
                        (instance? java.time.LocalDateTime datetime-obj)
                        (str (.format datetime-obj (java.time.format.DateTimeFormatter/ofPattern "HH:mm")))
                        
                        ;; If it's a string, parse it first
                        (string? datetime-obj)
                        (let [date (java.time.LocalDateTime/parse 
                                    (if (.endsWith datetime-obj "Z")
                                      (.replace datetime-obj "Z" "")
                                      datetime-obj))]
                          (str (.format date (java.time.format.DateTimeFormatter/ofPattern "HH:mm"))))
                        
                        ;; Default case
                        :else "")]
        (println "DEBUG: formatted time result:" formatted)
        formatted)
      (catch Exception e
        (println "Error formatting time:" (.getMessage e))
        ""))))

(defn- worksheet-to-work-report-data
  "Convert worksheet database record to work report format for PDF generation"
  [worksheet workspace-id]
  (let [work-type-mapping {"repair" "normal"
                          "maintenance" "normal" 
                          "other" "normal"}
        service-type-mapping {"normal" "normal"
                             "night" "night"
                             "weekend" "weekend"
                             "holiday" "weekend"}]
    {:institution-name (or (:address_name worksheet) "")
     :institution-address (str (or (:address_name worksheet) "") 
                              (when (:address_city worksheet) 
                                (str ", " (:address_city worksheet))))
     :work-type (get work-type-mapping (:work_type worksheet) "normal")
     :worksheet-work-type (:work_type worksheet)
     :arrival-time (format-time-from-iso (:arrival_time worksheet))
     :departure-time (format-time-from-iso (:departure_time worksheet))
     :work-duration-hours (:work_duration_hours worksheet)
     :work-description (or (:work_description worksheet) "")
     :materials-used (let [materials (:material_usage worksheet)]
                      (println "DEBUG: Raw material_usage from DB:" materials "type:" (type materials))
                      (cond
                        (nil? materials) []
                        (string? materials) (try (cheshire.core/parse-string materials true)
                                                (catch Exception e
                                                  (println "ERROR parsing material_usage JSON:" (.getMessage e))
                                                  []))
                        (vector? materials) materials
                        :else []))
     :notes (or (:notes worksheet) "")
     :date (str (:creation_date worksheet))
     :technician-signature (:maintainer_signature worksheet)
     :client-signature (:customer_signature worksheet)
     :workspace-logo-path (get-workspace-logo-path workspace-id)}))

(defn generate-worksheet-pdf
  "Generate PDF for worksheet"
  [{:parquery/keys [context request] :as params}]
  (let [worksheet-id (:worksheet/id params)
        workspace-id (:workspace-id context)]
    (println "DEBUG: generate-worksheet-pdf called with worksheet-id:" worksheet-id "workspace-id:" workspace-id)
    (if (and worksheet-id workspace-id)
      (let [worksheet-records (worksheets-db/get-worksheet-by-id worksheet-id workspace-id)]
        (println "DEBUG: Found" (count worksheet-records) "worksheet records")
        (if-let [worksheet (first worksheet-records)]
          (do
            (println "DEBUG: Raw worksheet data keys:" (keys worksheet))
            (println "DEBUG: arrival_time value:" (:arrival_time worksheet) "type:" (type (:arrival_time worksheet)))
            (println "DEBUG: departure_time value:" (:departure_time worksheet) "type:" (type (:departure_time worksheet)))
            (println "DEBUG: work_duration_hours:" (:work_duration_hours worksheet))
            (let [work-report-data (worksheet-to-work-report-data worksheet workspace-id)]
              (println "DEBUG: work-report-data for PDF:" work-report-data)
              (pdf/generate-work-report-pdf work-report-data)))
          (throw (ex-info "Worksheet not found" {:worksheet-id worksheet-id :workspace-id workspace-id}))))
      (throw (ex-info "Missing worksheet ID or workspace context" {:worksheet-id worksheet-id :workspace-id workspace-id})))))