(ns features.app.pdf-generator.backend.templates
  "PDF templates based on Expert Lift work report forms"
  (:require
   [hiccup2.core :as hiccup]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [cheshire.core :as json])
  (:import
   [java.util Base64]
   [java.io ByteArrayOutputStream]
   [java.nio.charset StandardCharsets]
   [java.awt Color BasicStroke RenderingHints]
   [java.awt.image BufferedImage]
   [javax.imageio ImageIO]))

(defn encode-image-to-base64
  "Convert image file to base64 data URI for embedding in PDF"
  [image-path]
  (try
    (let [image-file (io/file image-path)]
      (when (.exists image-file)
        (with-open [input-stream (io/input-stream image-file)
                    output-stream (ByteArrayOutputStream.)]
          (io/copy input-stream output-stream)
          (let [image-bytes (.toByteArray output-stream)
                base64-string (.encodeToString (Base64/getEncoder) image-bytes)
                mime-type (cond
                           (.endsWith image-path ".png") "image/png"
                           (.endsWith image-path ".jpg") "image/jpeg" 
                           (.endsWith image-path ".jpeg") "image/jpeg"
                           :else "image/png")]
            (str "data:" mime-type ";base64," base64-string)))))
    (catch Exception e
      (println "Warning: Could not load logo image:" (.getMessage e))
      nil)))

(defn- points-to-png-base64
  "Convert signature points JSON to PNG base64 data URL using Java2D.
   Data format: [[{x, y, color, time}, ...], [...], ...]
   Canvas size: 360x120 (3:1 ratio)"
  [points-json]
  (try
    (let [canvas-width 360
          canvas-height 120
          strokes (json/parse-string points-json true)
          ;; Create image with white background
          image (BufferedImage. canvas-width canvas-height BufferedImage/TYPE_INT_ARGB)
          g2d (.createGraphics image)]
      ;; Setup graphics
      (.setRenderingHint g2d RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON)
      (.setRenderingHint g2d RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_PURE)
      ;; White background
      (.setColor g2d Color/WHITE)
      (.fillRect g2d 0 0 canvas-width canvas-height)
      ;; Draw strokes
      (.setColor g2d Color/BLACK)
      (.setStroke g2d (BasicStroke. 2.0 BasicStroke/CAP_ROUND BasicStroke/JOIN_ROUND))
      (doseq [stroke strokes
              :when (and stroke (seq stroke) (> (count stroke) 1))]
        (let [points (vec stroke)]
          (doseq [i (range (dec (count points)))]
            (let [p1 (nth points i)
                  p2 (nth points (inc i))
                  x1 (int (:x p1))
                  y1 (int (:y p1))
                  x2 (int (:x p2))
                  y2 (int (:y p2))]
              (.drawLine g2d x1 y1 x2 y2)))))
      (.dispose g2d)
      ;; Convert to PNG base64
      (let [baos (ByteArrayOutputStream.)]
        (ImageIO/write image "PNG" baos)
        (let [base64-str (.encodeToString (Base64/getEncoder) (.toByteArray baos))]
          (str "data:image/png;base64," base64-str))))
    (catch Exception e
      (println "ERROR in points-to-png-base64:" (.getMessage e))
      (.printStackTrace e)
      nil)))

(defn render-signature
  "Render signature - handles points (JSON), SVG strings, and base64 image data URLs.
   Points format is converted to PNG for PDF compatibility."
  [signature-data]
  (cond
    ;; No signature
    (or (nil? signature-data) (empty? signature-data))
    [:span {:style "color: #999; font-style: italic;"} "-"]

    ;; New format: points data (JSON) - convert to PNG
    (str/starts-with? signature-data "points:")
    (let [json-str (subs signature-data 7)  ;; Remove "points:" prefix
          png-data-url (points-to-png-base64 json-str)]
      (if png-data-url
        [:img {:src png-data-url
               :style "max-width: 100%; height: 50px; object-fit: contain;"}]
        [:span {:style "color: #999; font-style: italic;"} "-"]))

    ;; Base64 image format (PNG/JPEG)
    (str/starts-with? signature-data "data:image")
    [:img {:src signature-data
           :style "max-width: 100%; height: 50px; object-fit: contain;"}]

    ;; SVG format - try to render (might not work in all PDF renderers)
    (str/starts-with? signature-data "<svg")
    [:div {:style "max-width: 100%; height: 50px; overflow: hidden;"}
     (hiccup/raw signature-data)]

    ;; Fallback - try as image
    :else
    [:img {:src signature-data
           :style "max-width: 100%; height: 50px; object-fit: contain;"}]))

(defn parse-time
  "Parse simple time string like '05:39' into hours and minutes"
  [time-str]
  (when (and time-str (not (empty? (str time-str))))
    (let [parts (str/split (str time-str) #":")]
      (when (>= (count parts) 2)
        {:hours (first parts)
         :minutes (second parts)}))))

(defn format-time-fields
  "Format time into separate hour and minute fields with actual data"
  [time-str label]
  (let [{:keys [hours minutes]} (parse-time time-str)]
    [:div
     [:label (str label ": ")]
     [:span {:style "border: 2px solid #000; padding: 2px 8px; display: inline-block; min-width: 30px; text-align: center; font-weight: bold;"} (or hours "")]
     [:span " óra "]
     [:span {:style "border: 2px solid #000; padding: 2px 8px; display: inline-block; min-width: 30px; text-align: center; font-weight: bold;"} (or minutes "")]
     [:span " perc"]]))

(defn work-report-template
  "Professional work report template - clean, modern design"
  [{:keys [institution-name
           institution-address
           elevator-identifier
           work-type
           worksheet-work-type
           arrival-time
           departure-time
           work-duration-hours
           work-description
           materials-used
           notes
           date
           serial-number
           technician-signature
           client-signature
           workspace-logo-path]}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
    [:title (str "Munkalap - " (or serial-number date) " - " (or institution-name ""))]
    [:style
     "
     * {
       box-sizing: border-box;
     }
     body {
       font-family: 'DejaVuSans', 'Helvetica', Arial, sans-serif;
       margin: 0;
       padding: 30px 40px;
       font-size: 11px;
       line-height: 1.4;
       color: #222;
       background: #fff;
     }

     /* === HEADER === */
     .header {
       display: table;
       width: 100%;
       margin-bottom: 25px;
       padding-bottom: 15px;
       border-bottom: 3px solid #333;
     }
     .header-left {
       display: table-cell;
       vertical-align: middle;
       width: 70%;
     }
     .header-right {
       display: table-cell;
       vertical-align: middle;
       text-align: right;
       width: 30%;
     }
     .main-title {
       font-size: 32px;
       font-weight: bold;
       letter-spacing: 3px;
       color: #111;
       margin: 0 0 8px 0;
     }
     .serial-number {
       font-size: 14px;
       color: #555;
       font-weight: normal;
     }
     .header-date {
       font-size: 13px;
       color: #333;
       margin-top: 4px;
     }
     .logo {
       max-width: 90px;
       max-height: 90px;
     }

     /* === SECTIONS === */
     .section {
       margin-bottom: 20px;
       page-break-inside: avoid;
     }
     .section-title {
       background: #fff;
       padding: 8px 12px;
       font-size: 12px;
       font-weight: bold;
       text-transform: uppercase;
       letter-spacing: 1px;
       color: #333;
       margin-bottom: 12px;
       border-left: 4px solid #333;
     }
     .section-content {
       padding: 0 12px;
     }

     /* === FORM FIELDS === */
     .field-row {
       display: table;
       width: 100%;
       margin-bottom: 10px;
     }
     .field-label {
       display: table-cell;
       width: 140px;
       font-weight: bold;
       color: #444;
       vertical-align: top;
       padding-right: 10px;
     }
     .field-value {
       display: table-cell;
       color: #111;
       border-bottom: 1px dotted #ccc;
       padding-bottom: 2px;
     }

     /* === CHECKBOXES === */
     .checkbox-group {
       display: table;
       width: 100%;
       margin: 8px 0;
     }
     .checkbox-item {
       display: table-cell;
       width: 33.33%;
       padding-right: 15px;
     }
     .checkbox {
       display: inline-block;
       width: 20px;
       height: 20px;
       border: 2px solid #444;
       border-radius: 3px;
       text-align: center;
       line-height: 16px;
       font-size: 14px;
       font-weight: bold;
       vertical-align: middle;
       margin-right: 8px;
       background: #fff;
     }
     .checkbox.checked {
       background: #fff;
       color: #000;
     }
     .checkbox-label {
       vertical-align: middle;
       font-size: 11px;
     }

     /* === TIME FIELDS === */
     .time-grid {
       display: table;
       width: 100%;
       margin: 10px 0;
     }
     .time-cell {
       display: table-cell;
       width: 50%;
       padding-right: 20px;
     }
     .time-cell:last-child {
       padding-right: 0;
     }
     .time-label {
       font-weight: bold;
       color: #444;
       margin-right: 10px;
     }
     .time-box {
       display: inline-block;
       border: 2px solid #444;
       padding: 4px 12px;
       min-width: 40px;
       text-align: center;
       font-weight: bold;
       font-size: 13px;
       background: #fff;
     }
     .time-unit {
       margin: 0 8px 0 4px;
       color: #666;
     }

     /* === WORK DURATION === */
     .duration-display {
       background: #fff;
       padding: 10px 15px;
       border: 1px solid #ddd;
       margin: 10px 0;
       font-size: 13px;
     }
     .duration-value {
       font-weight: bold;
       font-size: 16px;
       color: #222;
     }

     /* === DESCRIPTION BOX === */
     .description-box {
       border: 1px solid #ddd;
       padding: 12px;
       min-height: 50px;
       background: #fff;
       margin-top: 8px;
       line-height: 1.5;
     }

     /* === MATERIALS TABLE === */
     .materials-table {
       width: 100%;
       border-collapse: collapse;
       margin-top: 10px;
     }
     .materials-table th {
       background: #fff;
       color: #333;
       padding: 10px 12px;
       text-align: left;
       font-size: 11px;
       font-weight: bold;
       text-transform: uppercase;
       letter-spacing: 0.5px;
       border-bottom: 2px solid #333;
     }
     .materials-table td {
       padding: 10px 12px;
       border-bottom: 1px solid #ddd;
       background: #fff;
     }

     /* === SIGNATURE SECTION === */
     .signature-section {
       margin-top: 30px;
     }
     .signature-grid {
       display: table;
       width: 100%;
       margin-top: 15px;
     }
     .signature-cell {
       display: table-cell;
       width: 50%;
       padding-right: 20px;
       vertical-align: top;
     }
     .signature-cell:last-child {
       padding-right: 0;
     }
     .signature-field {
       margin-bottom: 15px;
     }
     .signature-field-label {
       font-weight: bold;
       color: #444;
       margin-bottom: 8px;
     }
     .signature-field-value {
       border-bottom: 1px dotted #ccc;
       padding-bottom: 4px;
       min-height: 20px;
     }
     .signature-image-container {
       border: 1px solid #ddd;
       padding: 8px;
       min-height: 60px;
       background: #fff;
       display: flex;
       align-items: center;
       justify-content: center;
     }
     .signature-image-container img {
       max-width: 100%;
       max-height: 50px;
       object-fit: contain;
     }

     /* === FOOTER === */
     .footer {
       margin-top: 25px;
       padding-top: 15px;
       border-top: 1px solid #eee;
       text-align: center;
       font-size: 9px;
       color: #888;
     }
     "]]
   [:body
    ;; === HEADER ===
    [:div.header
     [:div.header-left
      [:div.main-title "MUNKALAP"]
      [:div.serial-number (str "Sorszám: " (or serial-number "-"))]
      [:div.header-date (str "Dátum: " (or date "-"))]]
     [:div.header-right
      (let [logo-path (or workspace-logo-path "project/resources/public/logo/logo-256.png")]
        (when-let [logo-data (encode-image-to-base64 logo-path)]
          [:img.logo {:src logo-data}]))]]

    ;; === ALAPADATOK SECTION ===
    [:div.section
     [:div.section-title "Alapadatok"]
     [:div.section-content
      [:div.field-row
       [:div.field-label "Intézmény neve:"]
       [:div.field-value (or institution-name "-")]]
      [:div.field-row
       [:div.field-label "Cím:"]
       [:div.field-value (or institution-address "-")]]
      [:div.field-row
       [:div.field-label "Felvonó jelzése:"]
       [:div.field-value (or elevator-identifier "-")]]]]

    ;; === MUNKAVÉGZÉS SECTION ===
    [:div.section
     [:div.section-title "Munkavégzés"]
     [:div.section-content
      ;; Service type checkboxes
      [:div {:style "margin-bottom: 15px;"}
       [:div {:style "font-weight: bold; margin-bottom: 8px; color: #444;"} "Szolgáltatás típusa:"]
       [:div.checkbox-group
        [:div.checkbox-item
         [:div {:class (str "checkbox" (when (= work-type "normal") " checked"))}
          (when (= work-type "normal") "✓")]
         [:span.checkbox-label "Normál"]]
        [:div.checkbox-item
         [:div {:class (str "checkbox" (when (= work-type "night") " checked"))}
          (when (= work-type "night") "✓")]
         [:span.checkbox-label "Éjszaka"]]
        [:div.checkbox-item
         [:div {:class (str "checkbox" (when (= work-type "weekend") " checked"))}
          (when (= work-type "weekend") "✓")]
         [:span.checkbox-label "Hétvége / Ünnepnap"]]]]

      ;; Time fields
      [:div.time-grid
       [:div.time-cell
        [:span.time-label "Érkezés:"]
        (let [{:keys [hours minutes]} (parse-time arrival-time)]
          [:span
           [:span.time-box (or hours "--")]
           [:span.time-unit "óra"]
           [:span.time-box (or minutes "--")]
           [:span.time-unit "perc"]])]
       [:div.time-cell
        [:span.time-label "Távozás:"]
        (let [{:keys [hours minutes]} (parse-time departure-time)]
          [:span
           [:span.time-box (or hours "--")]
           [:span.time-unit "óra"]
           [:span.time-box (or minutes "--")]
           [:span.time-unit "perc"]])]]

      ;; Work duration
      (when work-duration-hours
        [:div.duration-display
         "Munkaidő: " [:span.duration-value (str work-duration-hours " óra")]])

      ;; Work type checkboxes
      [:div {:style "margin-top: 15px;"}
       [:div {:style "font-weight: bold; margin-bottom: 8px; color: #444;"} "Munka típusa:"]
       [:div.checkbox-group
        [:div.checkbox-item
         [:div {:class (str "checkbox" (when (= worksheet-work-type "repair") " checked"))}
          (when (= worksheet-work-type "repair") "✓")]
         [:span.checkbox-label "Javítás"]]
        [:div.checkbox-item
         [:div {:class (str "checkbox" (when (= worksheet-work-type "maintenance") " checked"))}
          (when (= worksheet-work-type "maintenance") "✓")]
         [:span.checkbox-label "Karbantartás"]]
        [:div.checkbox-item
         [:div {:class (str "checkbox" (when (= worksheet-work-type "other") " checked"))}
          (when (= worksheet-work-type "other") "✓")]
         [:span.checkbox-label "Egyéb"]]]]]]

    ;; === MUNKA LEÍRÁSA SECTION ===
    [:div.section
     [:div.section-title "Munka leírása"]
     [:div.section-content
      [:div.description-box (or work-description "-")]]]

    ;; === ANYAGFELHASZNÁLÁS SECTION ===
    (when (and materials-used (seq materials-used))
      [:div.section
       [:div.section-title "Anyagfelhasználás"]
       [:div.section-content
        [:table.materials-table
         [:thead
          [:tr
           [:th {:style "width: 70%;"} "Megnevezés"]
           [:th {:style "width: 30%;"} "Mennyiség"]]]
         [:tbody
          (for [material materials-used]
            [:tr
             [:td (str (:name material) " (" (:unit material) ")")]
             [:td {:style "font-weight: bold;"} (:quantity material)]])]]]])

    ;; === MEGJEGYZÉS SECTION ===
    (when (and notes (not (empty? notes)))
      [:div.section
       [:div.section-title "Megjegyzés"]
       [:div.section-content
        [:div.description-box notes]]])

    ;; === ALÁÍRÁSOK SECTION ===
    [:div.section
     [:div.section-title "Aláírások"]
     [:div.section-content
      ;; Date field
      [:div.signature-field
       [:div.signature-field-label "Dátum:"]
       [:div.signature-field-value (or date "-")]]
      ;; Signatures in two columns
      [:div.signature-grid
       [:div.signature-cell
        [:div.signature-field-label "Átvevő aláírása:"]
        [:div.signature-image-container
         (render-signature client-signature)]]
       [:div.signature-cell
        [:div.signature-field-label "Szerelő aláírása:"]
        [:div.signature-image-container
         (render-signature technician-signature)]]]]]

    ;; === FOOTER ===
    [:div.footer
     "Amennyiben az átvevő személy nincs jelen, a munkalap az aláírása nélkül is érvényes."]]])

(defn generate-work-report-html
  "Generate HTML for work report PDF"
  [data]
  (str (hiccup/html (work-report-template data))))