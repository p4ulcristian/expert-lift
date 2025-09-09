(ns zero.frontend.re-frame-viewer.raw)

;; Styling for raw viewer
(defn raw-container-style []
  {:flex "1"
   :overflow "auto"
   :border "2px solid #DAA520"
   :border-radius "0"
   :background "#222"})

(defn raw-text-style []
  {:margin "0"
   :padding "20px"
   :font-family "'Courier New', Monaco, monospace"
   :font-size "13px"
   :line-height "1.4"
   :color "#DAA520"
   :white-space "pre-wrap"
   :word-break "break-word"})

;; Pretty-print helper
(defn pretty-print [data]
  (try
    (cljs.pprint/pprint data)
    (catch js/Error _
      (str data))))

;; Raw data viewer component
(defn raw-viewer [data]
  [:div {:style (raw-container-style)}
   [:pre {:style (raw-text-style)}
    (with-out-str (pretty-print data))]])