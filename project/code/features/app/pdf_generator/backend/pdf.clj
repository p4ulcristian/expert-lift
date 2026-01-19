(ns features.app.pdf-generator.backend.pdf
  "PDF generation using OpenHTMLtoPDF"
  (:require
   [features.app.pdf-generator.backend.templates :as templates])
  (:import
   [com.openhtmltopdf.pdfboxout PdfRendererBuilder]
   [java.io ByteArrayOutputStream]
   [java.nio.charset StandardCharsets]))

(defn- find-unicode-font
  "Find a font file that supports Hungarian/Unicode characters.
   Checks common locations on Linux, macOS, and Windows."
  []
  (let [font-paths [;; Linux (Ubuntu/Debian) - DejaVu has excellent Hungarian support
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
                    ;; Linux alternative paths
                    "/usr/share/fonts/TTF/DejaVuSans.ttf"
                    "/usr/share/fonts/dejavu/DejaVuSans.ttf"
                    ;; Arch Linux / Noto fonts (also good Unicode support)
                    "/usr/share/fonts/noto/NotoSans-Regular.ttf"
                    ;; macOS
                    "/System/Library/Fonts/Helvetica.ttc"
                    "/Library/Fonts/Arial Unicode.ttf"
                    ;; Windows
                    "C:/Windows/Fonts/arial.ttf"
                    "C:/Windows/Fonts/DejaVuSans.ttf"]]
    (->> font-paths
         (filter #(.exists (java.io.File. %)))
         first)))

(defn html-to-pdf
  "Convert HTML string to PDF bytes using OpenHTMLtoPDF"
  [html-content]
  (let [output-stream (ByteArrayOutputStream.)
        builder (PdfRendererBuilder.)
        font-path (find-unicode-font)
        base-uri "file://project/resources/"]
    (try
      (-> builder
          (.withHtmlContent html-content base-uri)
          (cond->
            font-path
            (.useFont (java.io.File. font-path) "DejaVuSans"))
          (.toStream output-stream)
          (.run))
      (.toByteArray output-stream)
      (catch Exception e
        (throw (ex-info "Failed to generate PDF"
                        {:html-content html-content
                         :font-path font-path}
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