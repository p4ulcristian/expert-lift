(ns features.app.pdf-generator.backend.templates
  "PDF templates based on Expert Lift work report forms"
  (:require
   [hiccup.core :as hiccup]
   [clojure.java.io :as io])
  (:import
   [java.util Base64]
   [java.io ByteArrayOutputStream]))

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

(defn parse-time
  "Parse time string like '08:30' into hours and minutes"
  [time-str]
  (when time-str
    (let [parts (clojure.string/split time-str #":")]
      (when (= 2 (count parts))
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
  "Work report template matching Expert Lift KFT format from screenshot"
  [{:keys [institution-name
           institution-address
           work-type
           worksheet-work-type
           arrival-time
           departure-time
           work-duration-hours
           work-description
           materials-used
           notes
           date
           technician-signature
           client-signature
           workspace-logo-path]}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
    [:style
     "
     body { 
       font-family: 'Helvetica', Arial, sans-serif; 
       margin: 20px; 
       font-size: 11px; 
       line-height: 1.2;
     }
     .header { 
       margin-bottom: 20px; 
       position: relative;
       height: 100px;
       overflow: hidden;
     }
     .header .title { 
       float: left;
       font-size: 28px; 
       font-weight: bold;
       line-height: 40px;
       margin: 0;
       letter-spacing: 2px;
       margin-top: 10px;
     }
     .header .logo { 
       float: right;
       width: 100px;
       height: 100px;
     }
     .form-row { 
       margin-bottom: 8px; 
       overflow: hidden;
     }
     .form-row label { 
       font-weight: bold; 
       margin-right: 10px; 
       min-width: 140px; 
     }
     .form-row .field-value { 
       height: 20px; 
       margin-left: 5px; 
       display: inline-block;
       width: 300px;
       font-weight: bold;
     }
     .checkbox-row { 
       margin: 10px 0; 
       overflow: hidden;
     }
     .checkbox-item { 
       float: left;
       width: 30%;
       margin-right: 3%;
     }
     .checkbox-item:last-child {
       margin-right: 0;
     }
     .checkbox { 
       width: 18px; 
       height: 18px; 
       border: 2px solid #333; 
       display: inline-block; 
       margin-left: 8px;
       vertical-align: middle;
       position: relative;
       background: white;
       text-align: center;
       line-height: 14px;
       font-size: 12px;
       font-weight: bold;
     }
     .checkbox-label {
       vertical-align: middle;
       margin-right: 8px;
     }
     .time-row { 
       margin: 10px 0; 
       overflow: hidden;
     }
     .time-row div {
       float: left;
       width: 47%;
       margin-right: 6%;
     }
     .time-row div:last-child {
       margin-right: 0;
     }
     .work-type-section {
       margin: 10px 0;
     }
     .work-type-row {
       margin: 5px 0;
       overflow: hidden;
     }
     .work-type-item {
       float: left;
       width: 30%;
       margin-right: 3%;
     }
     .work-type-item:last-child {
       margin-right: 0;
     }
     .materials-table { 
       width: 100%; 
       border-collapse: collapse; 
       margin: 10px 0; 
     }
     .materials-table th, 
     .materials-table td { 
       border: 1px solid #000; 
       padding: 4px; 
       text-align: left; 
     }
     .materials-table th { 
       background-color: #f5f5f5; 
       font-weight: bold; 
     }
     .materials-table tr { 
       height: 20px; 
     }
     .signature-section { 
       margin-top: 20px; 
       overflow: hidden;
     }
     .signature-box { 
       text-align: center; 
       width: 30%; 
       float: left;
       margin-right: 3%;
       box-sizing: border-box;
     }
     .signature-box:last-child {
       margin-right: 0;
     }
     .signature-line { 
       border-bottom: 1px solid #000; 
       margin: 20px 0 5px 0; 
       height: 20px; 
     }
     .notes-section { 
       margin: 20px 0; 
     }
     .notes-content { 
       height: 20px; 
       margin: 5px 0; 
       font-weight: bold;
     }
     .disclaimer { 
       font-size: 9px; 
       text-align: center; 
       margin-top: 15px; 
     }
     "]]
   [:body
    [:div.header
     [:div.title "MUNKALAP"]
     (let [logo-path (or workspace-logo-path "project/resources/public/logo/logo-256.png")]
       (when-let [logo-data (encode-image-to-base64 logo-path)]
         [:img.logo {:src logo-data}]))]
    
    [:div.form-row
     [:label "Intézmény neve:"]
     [:div.field-value (or institution-name "")]]
    
    [:div.form-row
     [:label "Intézmény címe:"]
     [:div.field-value (or institution-address "")]]
    
    [:div.checkbox-row
     [:div.checkbox-item
      [:span.checkbox-label "Normál:"]
      [:div.checkbox (when (= work-type "normal") "X")]]
     [:div.checkbox-item
      [:span.checkbox-label "Éjszaka:"]
      [:div.checkbox (when (= work-type "night") "X")]]
     [:div.checkbox-item
      [:span.checkbox-label "Hétvége vagy ünnepnap:"]
      [:div.checkbox (when (= work-type "weekend") "X")]]]
    
    [:div
     [:label {:style "font-weight: bold; margin-bottom: 10px; display: block;"} "Felvonó jelzése:"]]
    
    [:div.time-row
     (format-time-fields arrival-time "Érkezés")
     (format-time-fields departure-time "Távozás")]
    
    ;; Work duration display
    (when work-duration-hours
      [:div {:style "margin: 10px 0; font-weight: bold; font-size: 12px;"}
       (str "Munkaidő: " work-duration-hours " óra")])
    
    [:div.work-type-section
     [:label {:style "font-weight: bold; display: block; margin-bottom: 10px;"} "Munka típusa:"]
     [:div.work-type-row
      [:div.work-type-item
       [:span.checkbox-label "Javítás:"]
       [:div.checkbox (when (= worksheet-work-type "repair") "X")]]
      [:div.work-type-item
       [:span.checkbox-label "Karbantartás:"]
       [:div.checkbox (when (= worksheet-work-type "maintenance") "X")]]
      [:div.work-type-item
       [:span.checkbox-label "Egyéb:"]
       [:div.checkbox (when (= worksheet-work-type "other") "X")]]]]
    
    [:div
     [:label {:style "font-weight: bold;"} "Munka leírása:"]
     [:div.notes-content (or work-description "")]]
    
    ;; Only show materials table if there are materials used
    (when (and materials-used (seq materials-used))
      [:table.materials-table
       [:thead
        [:tr
         [:th {:colspan "2"} "Anyagfelhasználás"]]
        [:tr  
         [:th "megnevezés"]
         [:th "mennyiség"]]]
       [:tbody
        (for [material materials-used]
          [:tr
           [:td (str (:name material) " (" (:unit material) ")")]
           [:td (:quantity material)]])]])
    
    [:div.notes-section
     [:label {:style "font-weight: bold;"} "Megjegyzés:"]
     [:div.notes-content (or notes "")]]
    
    [:div.signature-section
     [:div.signature-box
      [:div.signature-line ""]
      [:div "Dátum"]]
     [:div.signature-box
      (if client-signature
        [:img {:src client-signature :style "width: 150px; height: 60px; border: 2px solid #000; object-fit: contain; background: #f9f9f9;"}]
        [:div.signature-line ""])
      [:div "Átvevő aláírása"]]
     [:div.signature-box
      (if technician-signature
        [:img {:src technician-signature :style "width: 150px; height: 60px; border: 2px solid #000; object-fit: contain; background: #f9f9f9;"}]
        [:div.signature-line ""])
      [:div "Szerelő aláírása"]]]
    
    [:div.disclaimer
     "Amennyiben Átvevő személy nincs jelen, a munkalap az aláírása nélkül is érvényes!"]]])

(defn generate-work-report-html
  "Generate HTML for work report PDF"
  [data]
  (hiccup/html (work-report-template data)))