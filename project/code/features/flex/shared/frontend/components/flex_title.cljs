(ns features.flex.shared.frontend.components.flex-title
  (:require ["react" :as react]
            [features.flex.shared.frontend.components.info-tooltip :as info-tooltip]))

(defn info-content [description]
  [:div {:style {:margin "0"
                 :font-size "14px"
                 :line-height "1.4"
                 :color "#6b7280"}}
   description])

(defn info-button [description]
  (when description
    [info-tooltip/view {:content [info-content description]
                        :hover-delay 200}
     [:button {:style {:background "none"
                      :border "none"
                      :cursor "pointer"
                      :padding "6px"
                      :border-radius "50%"
                      :color "#6b7280"
                      :font-size "14px"
                      :transition "all 0.2s ease"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :width "28px"
                      :height "28px"}
              :on-mouse-enter #(do
                                (set! (-> % .-target .-style .-color) "#374151")
                                (set! (-> % .-target .-style .-background-color) "#f3f4f6"))
              :on-mouse-leave #(do
                                (set! (-> % .-target .-style .-color) "#6b7280")
                                (set! (-> % .-target .-style .-background-color) "transparent"))}
      [:i {:class "fas fa-info-circle"}]]]))

(defn view
  "Flex-specific title component with optional info tooltip
   
   Parameters:
   - title: Page title string
   - options: Map with optional keys:
     - :right-content - Components to show on right side
     - :description - Description for info tooltip (shows icon only if provided)
     - :style - Custom title styling
     - :container-style - Custom container styling"
  ([title]
   (view title {}))
  ([title options]
   (let [description (:description options)
         title-with-info [:div {:style {:display "flex"
                                       :align-items "center"
                                       :gap "12px"}}
                          [:h1 {:style (merge {:font-size "24px"
                                               :font-weight "700"
                                               :margin "0"
                                               :color "#222"}
                                              (:style options))}
                           title]
                          [info-button description]]]
     (if (:right-content options)
       [:div {:style (merge {:display "flex"
                             :justify-content "space-between"
                             :align-items "center"
                             :margin "0 0 24px 0"}
                            (:container-style options))}
        title-with-info
        (:right-content options)]
       [:div {:style {:margin "0 0 24px 0"}}
        title-with-info]))))