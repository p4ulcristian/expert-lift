(ns ui.textarea)

(defn mode-style [{:keys [mode color]}]
  (merge
   {:border "1px solid #d1d5db"
    :borderRadius "8px"
    :padding "12px 14px"
    :fontSize "1rem"
    :fontFamily "inherit"
    :background "#f9fafb"
    :color "#222"
    :resize "vertical"
    :transition "border-color 0.2s, box-shadow 0.2s"
    :outline "none"
    :boxSizing "border-box"
    :minHeight "96px"
    :width "100%"
    :marginTop "4px"}
   (case mode
     :outlined {:border (str "2px solid " (or color "#a0aec0"))}
     :filled   {:background (or color "#f3f4f6")}
     {})))

(def label-style {:display "block"
                  :fontWeight 500
                  :fontSize "1rem"
                  :marginBottom "4px"
                  :color "#333"})

(def error-style {:color "#e53e3e"
                  :fontSize "0.95rem"
                  :marginTop "6px"
                  :fontWeight 500})

(defn textarea [{:keys [label value on-change color style disabled placeholder mode override rows cols error] :as _input-props}]
  [:div {:style {:width "100%"}}
   (when label
     [:label {:style label-style} label])
   [:textarea (merge {:style    (merge (mode-style {:mode mode :color color}) style)
                      :value    (or value "")
                      :on-change #(when on-change (on-change (.. ^js % -target -value)))
                      :disabled disabled
                      :placeholder placeholder
                      :rows rows
                      :cols cols}
                     (dissoc override :class))]
   (when error
     [:div {:style error-style} error])])

(defn view [input-props]
  [textarea input-props])

