(ns ui.checkbox)

(defn checkbox [{:keys [id checked? on-change label style]
                 :or {id (random-uuid)}}]
  [:div {:class "checkbox-container"
         :style {"--fill-clr" (:fill-color style)
                 "--mark-clr" (:mark-color style)}}
    [:input {:id        id
             :class     "checkbox-input"
             :checked   checked?
             :on-change on-change
             :type      "checkbox"}]
   
    [:label {:class "checkbox-label" 
             :for   id
             :style style}
       [:span
        [:svg {:width "12px" :height "10px"}
             [:use {"href" "#check-4"}]]]
       [:span label]]
   
    [:svg {:class "inline-svg"}
       [:symbol {:id "check-4" :viewBox "0 0 12 10"}
        [:polyline {:points "1.5 6 4.5 9 10.5 1"}]]]])

(defn view [props]
  [checkbox props])


