(ns features.app.pdf-generator.backend.handlers
  "ParQuery handlers for PDF generation"
  (:require
   [features.app.pdf-generator.backend.pdf :as pdf]
   [features.app.worksheets.backend.db :as worksheets-db]
   [malli.core :as m]))

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

(defn generate-work-report
  "Generate work report PDF"
  [{:parquery/keys [context request] :as params}]
  (let [work-report-data (dissoc params :parquery/context :parquery/request)]
    (validate-work-report-data work-report-data)
    (pdf/generate-work-report-pdf work-report-data)))

(defn generate-sample-work-report
  "Generate sample work report for testing"
  [{:parquery/keys [context request]}]
  (pdf/create-sample-work-report))

(defn- format-time-from-iso
  "Convert ISO datetime to HH:mm format for PDF template"
  [iso-datetime]
  (when iso-datetime
    (try
      (let [date (java.time.LocalDateTime/parse 
                  (if (.endsWith iso-datetime "Z")
                    (.replace iso-datetime "Z" "")
                    iso-datetime))]
        (str (.format date (java.time.format.DateTimeFormatter/ofPattern "HH:mm"))))
      (catch Exception e
        (println "Error formatting time:" (.getMessage e))
        ""))))

(defn- worksheet-to-work-report-data
  "Convert worksheet database record to work report format for PDF generation"
  [worksheet]
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
     :arrival-time (format-time-from-iso (:arrival_time worksheet))
     :departure-time (format-time-from-iso (:departure_time worksheet))
     :work-duration-hours (:work_duration_hours worksheet)
     :work-description (or (:work_description worksheet) "")
     :materials-used [] ; TODO: Add materials when available
     :notes (or (:notes worksheet) "")
     :date (str (:creation_date worksheet))}))

(defn generate-worksheet-pdf
  "Generate PDF for worksheet"
  [{:parquery/keys [context request] :as params}]
  (let [worksheet-id (:worksheet/id params)
        workspace-id (:workspace-id context)]
    (if (and worksheet-id workspace-id)
      (let [worksheet-records (worksheets-db/get-worksheet-by-id worksheet-id workspace-id)]
        (if-let [worksheet (first worksheet-records)]
          (let [work-report-data (worksheet-to-work-report-data worksheet)]
            (pdf/generate-work-report-pdf work-report-data))
          (throw (ex-info "Worksheet not found" {:worksheet-id worksheet-id :workspace-id workspace-id}))))
      (throw (ex-info "Missing worksheet ID or workspace context" {:worksheet-id worksheet-id :workspace-id workspace-id})))))