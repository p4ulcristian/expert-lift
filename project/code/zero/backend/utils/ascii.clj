(ns zero.backend.utils.ascii)

(def zero-art
  "
███████╗███████╗██████╗  ██████╗ 
╚══███╔╝██╔════╝██╔══██╗██╔═══██╗
  ███╔╝ █████╗  ██████╔╝██║   ██║
 ███╔╝  ██╔══╝  ██╔══██╗██║   ██║
███████╗███████╗██║  ██║╚██████╔╝
╚══════╝╚══════╝╚═╝  ╚═╝ ╚═════╝ ")

(defn generate-border
  "Generates an ASCII border for the given text."
  [text]
  (let [line (apply str (repeat (+ 4 (count text)) "═"))
        top (str "╔" line "╗")
        middle (str "║  " text "  ║")
        bottom (str "╚" line "╝")]
    (str top "\n" middle "\n" bottom)))

(comment
  (println
   (generate-border
    "INTERGRANT BLOCK")))