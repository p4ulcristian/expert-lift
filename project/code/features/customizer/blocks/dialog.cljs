(ns features.customizer.blocks.dialog
  (:require
    [ui.button   :as button]
    [ui.checkbox :as checkbox]
    [ui.popup    :as popup]))

(defn cancel-button [on-cancel]
  [button/view {:color    "gray"
                :on-click #(on-cancel)
                :style    {:color         "black"
                           :min-width     "100px"
                           :font-size     "0.85rem"}}
    "Cancel"])

(defn confirm-button [on-confirm]
  [button/view {:color "var(--irb-clr)"
                :on-click #(on-confirm)
                :style    {:color         "black"
                           :min-width     "100px"
                           :font-size     "0.85rem"}}
                        
    "Confirm"])

(defn dialog [{:keys [state on-cancel on-confirm]} content]
  [popup/view {:bg       false
               :style    {:padding          "15px"
                          :background-color "rgb(81 82 83)"}
                          ;; :transition       "all 0.3s ease-in-out"}
               :state    state
               :on-close #(on-cancel)}
    [:div {:style {:font-size "0.8rem"}}
      content
      [:div {:style {:margin-top      "5px"
                     :display         "flex"
                     :gap             "15px"
                     :justify-content "space-between"}}
        [cancel-button on-cancel]
        [confirm-button on-confirm]]]])

(defn view [props content]
  [dialog props content])