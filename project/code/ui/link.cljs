
(ns ui.link)

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn type-class [{:keys [type]}]
  (case type
    :primary   "input-primary"
    :secondary "input-secondary"
    :warning   "input-warning"
    :success   "input-success"
    ""))

(defn mode-class [{:keys [mode]}]
  (case mode
    :outlined   "button-outlined"
    :filled     "button-filled"
    :clear      "button-clear"
    :underlined "button-underlined"
    "button-filled"))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn link [{:keys [href color style disabled on-click override] :as input-props} content]
  [:a (merge {:class    ["input-button" (mode-class input-props) (type-class input-props)]
              :href     href
              :disabled disabled
              :on-click (fn [_]
                          (when on-click (on-click)))
              :style    (merge {"--clr" color}
                               style)}
             override)
   (into [:<>] content)])

(defn view [input-props & content]
  [link input-props content])