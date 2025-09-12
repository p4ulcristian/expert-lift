(ns features.app.pdf-generator.backend.templates
  "PDF templates based on Expert Lift work report forms"
  (:require
   [hiccup.core :as hiccup]))

(defn work-report-template
  "Work report template matching Expert Lift KFT format from screenshot"
  [{:keys [institution-name
           institution-address
           work-type
           arrival-time
           departure-time
           work-description
           materials-used
           notes
           date
           technician-signature
           client-signature]}]
  [:html
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
    [:style
     "
     body { 
       font-family: 'Helvetica', Arial, sans-serif; 
       margin: 40px; 
       font-size: 12px; 
       line-height: 1.4;
     }
     .header { 
       margin-bottom: 30px; 
       overflow: hidden;
     }
     .header .logo { 
       float: left; 
     }
     .header .title { 
       float: right; 
     }
     .logo { 
       font-weight: bold; 
       color: #d32f2f; 
     }
     .title { 
       font-size: 24px; 
       font-weight: bold; 
       text-align: center; 
     }
     .form-row { 
       margin-bottom: 15px; 
       overflow: hidden;
     }
     .form-row label { 
       font-weight: bold; 
       margin-right: 10px; 
       min-width: 140px; 
     }
     .form-row .dotted-line { 
       border-bottom: 1px dotted #000; 
       height: 20px; 
       margin-left: 5px; 
       display: inline-block;
       width: 300px;
     }
     .checkbox-row { 
       margin: 20px 0; 
     }
     .checkbox-item { 
       display: inline-block; 
       margin-right: 40px;
     }
     .checkbox { 
       width: 15px; 
       height: 15px; 
       border: 1px solid #000; 
       display: inline-block; 
     }
     .time-row { 
       margin: 20px 0; 
     }
     .time-row div {
       display: inline-block;
       margin-right: 40px;
     }
     .materials-table { 
       width: 100%; 
       border-collapse: collapse; 
       margin: 20px 0; 
     }
     .materials-table th, 
     .materials-table td { 
       border: 1px solid #000; 
       padding: 8px; 
       text-align: left; 
     }
     .materials-table th { 
       background-color: #f5f5f5; 
       font-weight: bold; 
     }
     .materials-table tr { 
       height: 30px; 
     }
     .signature-section { 
       margin-top: 40px; 
       overflow: hidden;
     }
     .signature-box { 
       text-align: center; 
       width: 200px; 
       float: left;
       margin-right: 20px;
     }
     .signature-line { 
       border-bottom: 1px solid #000; 
       margin: 20px 0 5px 0; 
       height: 20px; 
     }
     .notes-section { 
       margin: 20px 0; 
     }
     .notes-lines { 
       border-bottom: 1px dotted #000; 
       height: 20px; 
       margin: 5px 0; 
     }
     .disclaimer { 
       font-size: 10px; 
       text-align: center; 
       margin-top: 30px; 
       font-style: italic; 
     }
     "]]
   [:body
    [:div.header
     [:div.logo 
      [:div "X"]
      [:div "EXPERT"]
      [:div "LIFT KFT."]]
     [:div.title "MUNKALAP"]]
    
    [:div.form-row
     [:label "Intézmény neve:"]
     [:div.dotted-line (or institution-name "")]]
    
    [:div.form-row
     [:label "Intézmény címe:"]
     [:div.dotted-line (or institution-address "")]]
    
    [:div.checkbox-row
     [:div.checkbox-item
      [:label "Normál:"]
      [:div.checkbox (when (= work-type "normal") "✓")]]
     [:div.checkbox-item
      [:label "Éjszaka:"]
      [:div.checkbox (when (= work-type "night") "✓")]]
     [:div.checkbox-item
      [:label "Hétvége vagy ünnepnap:"]
      [:div.checkbox (when (= work-type "weekend") "✓")]]]
    
    [:div
     [:label {:style "font-weight: bold; margin-bottom: 10px; display: block;"} "Felvonó jelzése:"]]
    
    [:div.time-row
     [:div
      [:label "Érkezés: "]
      [:span {:style "border-bottom: 1px solid #000; padding: 0 20px;"} (or arrival-time "")]
      [:span " óra "]
      [:span {:style "border-bottom: 1px solid #000; padding: 0 20px;"} ""]
      [:span " perc"]]
     [:div
      [:label "Távozás: "]
      [:span {:style "border-bottom: 1px solid #000; padding: 0 20px;"} (or departure-time "")]
      [:span " óra "]
      [:span {:style "border-bottom: 1px solid #000; padding: 0 20px;"} ""]
      [:span " perc"]]]
    
    [:div.checkbox-row
     [:div.checkbox-item
      [:label "Munka típusa: Javítás:"]
      [:div.checkbox ""]]
     [:div.checkbox-item
      [:label "Karbantartás:"]
      [:div.checkbox ""]]
     [:div.checkbox-item
      [:label "Egyéb:"]
      [:div.checkbox ""]]]
    
    [:div
     [:label {:style "font-weight: bold;"} "Munka leírása:"]
     [:div.notes-lines (or work-description "")]
     [:div.notes-lines ""]
     [:div.notes-lines ""]]
    
    [:table.materials-table
     [:thead
      [:tr
       [:th "Anyagfelhasználás"]
       [:th "megnevezés"]
       [:th "m"]]]
     [:tbody
      (for [i (range 6)]
        [:tr
         [:td ""]
         [:td ""]
         [:td ""]])]]
    
    [:div.notes-section
     [:label {:style "font-weight: bold;"} "Megjegyzés:"]
     [:div.notes-lines (or notes "")]
     [:div.notes-lines ""]]
    
    [:div.signature-section
     [:div.signature-box
      [:div.signature-line ""]
      [:div "Dátum"]]
     [:div.signature-box
      [:div.signature-line ""]
      [:div "Átvevő aláírása"]]
     [:div.signature-box
      [:div.signature-line ""]
      [:div "Szerelő aláírása"]]]
    
    [:div.disclaimer
     "Amennyiben Átvevő személy nincs jelen, a munkalap az aláírása nélkül is érvényes!"]]])

(defn generate-work-report-html
  "Generate HTML for work report PDF"
  [data]
  (hiccup/html (work-report-template data)))