(ns features.app.pdf-generator.backend.handlers
  "ParQuery handlers for PDF generation"
  (:require
   [features.app.pdf-generator.backend.pdf :as pdf]
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