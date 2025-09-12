
(ns ui.button)

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
    :clear_2    "button-clear-2"
    :underlined "button-underlined"
    "button-filled"))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn button [{:keys [id color style class disabled disable-effect on-click override] :as input-props} content]
  [:button (merge {:id id
                   :class    ["input-button" (mode-class input-props) (type-class input-props) class]
                   :style    (merge {"--clr" color}
                                    style)
                   :disabled disabled
                   :on-click (fn [event]
                               (when on-click (on-click event)))}
                  override)
   (into [:<>] content)])

(defn view [input-props & content]
  [button input-props content])