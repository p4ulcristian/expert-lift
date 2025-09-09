(ns ui.unit-select)

(def units
  ["kilogram"
   "gram"
   "tonne"
   "liter"
   "milliliter"
   "cubic meter"
   "meter"
   "centimeter"
   "millimeter"
   "watt-hour"
   "kilowatt-hour"
   "joule"
   "calorie"
   "second"
   "minute"
   "hour"
   "piece"
   "unit"])

(defn view [{:keys [value on-change label style]}]
  [:div {:style (merge {:display "flex" :flexDirection "column" :width "100%"} style)}
   (when label
     [:label {:style {:fontWeight 500 :fontSize "1rem" :marginBottom "4px" :color "#333"}} label])
   [:select {:value (or value "")
             :on-change #(when on-change (on-change (.. ^js % -target -value)))
             :style {:padding "12px 14px"
                     :border "1px solid #d1d5db"
                     :borderRadius "8px"
                     :fontSize "1rem"
                     :background "#f9fafb"
                     :color "#222"
                     :outline "none"
                     :fontFamily "inherit"}}
    [:option {:value "" :disabled true} "Select unit..."]
    (for [u units]
      ^{:key u} [:option {:value u} u])]])