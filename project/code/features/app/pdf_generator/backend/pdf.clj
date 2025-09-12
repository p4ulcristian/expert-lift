(ns features.app.pdf-generator.backend.pdf
  "PDF generation using OpenHTMLtoPDF"
  (:require
   [features.app.pdf-generator.backend.templates :as templates])
  (:import
   [com.openhtmltopdf.pdfboxout PdfRendererBuilder]
   [java.io ByteArrayOutputStream]
   [java.nio.charset StandardCharsets]))

(defn html-to-pdf
  "Convert HTML string to PDF bytes using OpenHTMLtoPDF"
  [html-content]
  (let [output-stream (ByteArrayOutputStream.)
        builder (PdfRendererBuilder.)
        ; Use DejaVu Sans which has excellent Unicode support including Hungarian
        font-path "/System/Library/Fonts/Helvetica.ttc"
        ; Set base URI to project resources for relative paths
        base-uri "file://project/resources/"]
    (try
      (-> builder
          (.withHtmlContent html-content base-uri)
          ; Add font that supports Hungarian characters
          (cond-> 
            (.exists (java.io.File. font-path))
            (.useFont (java.io.File. font-path) "Helvetica"))
          (.toStream output-stream)
          (.run))
      (.toByteArray output-stream)
      (catch Exception e
        (throw (ex-info "Failed to generate PDF" 
                       {:html-content html-content}
                       e)))
      (finally
        (.close output-stream)))))

(defn generate-work-report-pdf
  "Generate work report PDF from data"
  [work-report-data]
  (let [html (templates/generate-work-report-html work-report-data)]
    (html-to-pdf html)))

(defn create-sample-work-report
  "Create sample work report PDF for testing"
  []
  (let [sample-data {:institution-name "Budapest Főváros Önkormányzata"
                     :institution-address "1052 Budapest, Városház u. 9-11."
                     :work-type "normal"
                     :arrival-time "08:30"
                     :departure-time "12:15" 
                     :work-description "Felvonó rendszeres karbantartása, kábelek ellenőrzése, kenőanyag pótlása"
                     :materials-used []
                     :notes "Minden rendben, következő karbantartás: 2025-10-12"
                     :date "2025-09-12"}]
    (generate-work-report-pdf sample-data)))

(defn save-sample-pdf-to-file
  "Save sample PDF to file (for testing)"
  [filename]
  (let [pdf-bytes (create-sample-work-report)]
    (with-open [out (java.io.FileOutputStream. filename)]
      (.write out pdf-bytes))
    filename))