
(ns features.customizer.panel.frontend.blocks.form.digit
  (:require
    [features.common.form.frontend.inputs.digit :as digit]))


(defn digit-input [input-props]
  [:div {:style {:width "100%"
                 :margin-bottom "15px"}}
    [digit/view (merge 
                  input-props
                  {:class "customizer--digit-input--container"
                   :title [:p {:style {:margin-bottom "10px"
                                       :font-size     "14px"
                                       :font-weight   "600"
                                       :color "rgba(255, 255, 255, 0.9)"}} 
                            (:title input-props)]})]])
                            

(defn view [input-props]
  [digit-input input-props])