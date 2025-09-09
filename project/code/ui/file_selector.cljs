(ns ui.file-selector
  (:require
    [ui.button :as button]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn select-files-fn [on-selected]
  (let [input (.createElement js/document "input")]
    (set! (.-type input) "file")
    (set! (.-multiple input) true)
    (set! (.-style input) "display: none")
    (set! (.-onchange input)
          (fn [e]
            (let [files (.. ^js e -target -files)]
              (on-selected (js->clj files :keywordize-keys true)))))
    (.appendChild js/document.body input)
    (.click input)
    ;; Optional cleanup
    (js/setTimeout #(do (println "removed") (-> input .remove)) 5000)))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn button [on-select & content]
  [button/view {:on-click (fn [] (select-files-fn #(on-select %)))}
    content])
   
