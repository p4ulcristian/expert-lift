(ns ui.card)


(defn delete-button [on-click]
  [:button {:style {:border  "2px solid red"
                    :padding "5px 5px"
                    :height "20px"
                    :width "20px"
                    :color :black
                    :display :flex
                    :justify-content :center
                    :align-items :center
                    :cursor "pointer"
                    :position :absolute
                    :top -10
                    :right -10
                    :border-radius "50%"}
            :on-click #(do
                         (.stopPropagation ^js %)
                         (on-click))}
   [:i {:class "fa-solid fa-xmark"}]])

(defn clickable-style [on-click]
  (when on-click
    {:cursor "pointer"}))

(defn view [{:keys [content style
                    on-delete on-click]}]
  [:div {:style (merge {:position :relative
                        :border "1px solid #ddd"
                        :border-radius "12px"
                        :padding "12px"
                        :box-shadow "0 2px 8px rgba(0, 0, 0, 0.05)"}
                       (clickable-style on-click)
                       style)
         :on-click on-click}
   (when on-delete [delete-button on-delete])
   content])