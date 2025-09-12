(ns features.app.pdf-generator.frontend.view
  "PDF generator views for creating and managing PDF templates"
  (:require
   [zero.frontend.react :as zero-react]))

(defn pdf-generator-page
  "Main PDF generator page"
  []
  [:div.pdf-generator-page
   [:h1 "PDF Generator"]
   [:p "PDF template generation tools"]])