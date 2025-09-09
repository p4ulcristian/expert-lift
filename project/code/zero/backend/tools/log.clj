(ns zero.backend.tools.log
  (:require [clojure.pprint :as pprint]
            [zero.backend.utils.ascii :as ascii]))

; ╔══════════════════════════╗
; ║  Pretty Print Functions  ║
; ╚══════════════════════════╝

(defn pretty-print
  "Pretty prints the provided data."
  [data]
  (pprint/pprint data))

(defn pretty-print-str
  "Pretty prints the provided data."
  [data]
  (with-out-str (pretty-print data)))

; ╔══════════════════════════╗
; ║  Pretty Print Functions  ║
; ╚══════════════════════════╝

; ╔═════════════════════╗
; ║  Color Definitions  ║
; ╚═════════════════════╝

(def colors
  "A map of basic colors."
  {:red "\u001B[31m"
   :green "\u001B[32m"
   :yellow "\u001B[33m"
   :blue "\u001B[34m"
   :magenta "\u001B[35m"
   :cyan "\u001B[36m"
   :white "\u001B[37m"
   :bright-red "\u001B[91m"
   :bright-green "\u001B[92m"
   :bright-yellow "\u001B[93m"
   :bright-blue "\u001B[94m"
   :bright-magenta "\u001B[95m"
   :bright-cyan "\u001B[96m"
   :bright-white "\u001B[97m"
   :orange "\u001B[38;5;214m"
   :reset "\u001B[0m"})

(defn get-color
  "Returns the color for the provided key."
  [_key]
  (get colors _key))

; ╔═════════════════════╗
; ║  Color Definitions  ║
; ╚═════════════════════╝

; ╔═════════════════════╗
; ║  Logging Functions  ║
; ╚═════════════════════╝
(defn color-log 
   "Wraps the provided text in the provided color."
  [color-key text] 
  (println
   (str (get-color color-key)
        text
        (get-color :reset))))

(defn welcome-log
  "Prints the welcome message."
  []
  (color-log :bright-cyan ascii/zero-art))

(defn error-log
  "Wraps the provided text in a warning color."
  [text]
  (color-log :red text))

(defn info-log
  "Wraps the provided text in a warning color."
  [text]
  (color-log :bright-blue text))

(defn warning-log
  "Wraps the provided text in a warning color."
  [text]
  (color-log :orange text))

(defn debug-log
  "Wraps the provided text in a warning color."
  ([text]
   (debug-log :default text))
  ([label text]
   (color-log :bright-red    (ascii/generate-border (str label)))
   (color-log :bright-green  text)
   (color-log :bright-red    "-------------------")))

; ╔═════════════════════╗
; ║  Logging Functions  ║
; ╚═════════════════════╝

; ╔══════════════════════╗
; ║  Lifecycle Messages  ║
; ╚══════════════════════╝
(defn started-message
  "Prints the starting message with colorful text."
  [thing-started]
  (color-log :green (str "Starting " thing-started)))

(defn stopped-message
  "Prints the stopping message with colorful text."
  [thing-stopped]
  (color-log :red (str "Stopping " thing-stopped)))

; ╔══════════════════════╗
; ║  Lifecycle Messages  ║
; ╚══════════════════════╝

; ╔═════════════════════╗
; ║  Utility Functions  ║
; ╚═════════════════════╝

(defn ascii-border-log
  "Prints a border with the provided color key."
  ([text]
   (ascii-border-log :bright-blue text))
  ([color-key text]
   (color-log color-key (ascii/generate-border text))))

; ╔═════════════════════╗
; ║  Utility Functions  ║
; ╚═════════════════════╝
