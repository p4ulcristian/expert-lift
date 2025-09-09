(ns features.customizer.blocks.my-designs
  (:require
    [features.customizer.blocks.my-designs.effects]
    [re-frame.core :as r]
    [ui.floater :as floater]
    [ui.button :as button]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn close-button []
  [button/view {:mode     :clear_2
                :on-click #(r/dispatch [:my-designs.drawer/close!])
                :style    {:position  "absolute"
                           :right     "0"}}
    [:i {:class ["fa-solid" "fa-xmark"]
         :style {:font-size "20px"}}]])

(defn title []
  [:div {:style {:height "50px"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :position "relative"
                 :font-size "20px"
                 :font-weight "600"}}
    [:p "My Designs"]
    [close-button]])

(defn designs-list []
  [:div {:id    "my-designs-list"
         :class "hide-scroll"
         :style {:display "flex"
                 :flex-direction "column"
                 :gap "10px"}}
    [:div {:style {:padding "20px"
                   :border "1px solid rgba(255, 255, 255, 0.2)"
                   :border-radius "8px"
                   :background "rgba(255, 255, 255, 0.05)"
                   :text-align "center"
                   :color "rgba(255, 255, 255, 0.7)"}}
      [:i {:class ["fa-solid" "fa-heart"]
           :style {:margin-right "8px"
                   :font-size "18px"}}]
      [:p {:style {:margin "10px 0 0 0"}} "No designs saved yet"]
      [:p {:style {:margin "5px 0 0 0"
                   :font-size "14px"
                   :opacity "0.8"}} "Your favorite designs will appear here"]]])

(defn save-design-button []
  [button/view {:id       "save-design-button"
                :color    "var(--irb-clr)"
                :style    {:width "100%"
                           :margin-top "10px"}}
    [:i {:class ["fa-solid" "fa-plus"]
         :style {:margin-right "8px"}}]
    "Save Current Design"])

(defn content []
  [:div {:style {:display            "grid"
                 :height             "95%"
                 :grid-template-rows "1fr auto"
                 :margin             "15px 0"
                 :gap                "25px"}}
    [designs-list]
    [save-design-button]])

(defn view []
  [floater/view {:orientation :right
                 :class       "floater customizer--floater-ui"
                 :state       @(r/subscribe [:db/get-in [:my-designs.drawer/state] false])
                 :on-close    #(r/dispatch [:my-designs.drawer/close!])
                 :config      {:bg false}}
                ;;  :style       {:z-index   "100"}}
   
    [:<> 
      [title]
      [content]]])