
(ns ui.note)

(defn view [& components]
  [:div {:style {:background "#ececec"
                 :padding "8px"
                 :border-radius "6px"}}
    [:b {:style {:color "gray"}} "Note"]
    (into [:<>] components)])