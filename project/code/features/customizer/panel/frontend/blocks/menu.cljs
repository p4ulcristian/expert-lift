(ns features.customizer.panel.frontend.blocks.menu
  (:require
   [re-frame.core :as r]
   [ui.floater :as floater]
   [ui.button  :as button]
   
   [features.customizer.panel.frontend.blocks.menu.effects]
   [features.customizer.panel.frontend.blocks.menu.subs]
   
   [features.customizer.panel.frontend.blocks.menu.cards    :as cards]
   [features.customizer.panel.frontend.blocks.menu.populars :as populars]))
 
;; -----------------------------------------------------------------------------
;; ---- Components ----

;; ---- Header ----

(defn- drawer-close-button []
  [button/view {:id       "customizer--category-header--close-button"
                :mode     :clear_2
                :color    "white"
                :on-click #(r/dispatch [:customizer.menu/close!])}
    [:i.fa-solid.fa-xmark {:style {:font-size "20px"}}]])

(defn- back-button []
  (when (seq @(r/subscribe [:db/get-in [:customizer/menu :path]]))
    [button/view {:id       "customizer--category-header--back-button"
                  :mode     :clear_2
                  :color    "white"
                  :on-click #(r/dispatch [:customizer.menu/back!])}
      [:i.fa-solid.fa-chevron-left {:style {:font-size "18px"}}]]))

(defn- title []
  (let [category-title     @(r/subscribe [:customizer.menu/title "Items"])
        selected           @(r/subscribe [:db/get-in [:customizer/menu :selected]])
        package-selected? (= (last @(r/subscribe [:db/get-in [:customizer :cursor]]))
                             (:id selected))]
    
    [:div {:style {:text-align "center"}}
      [:h3 {:style {:font-size "20px" 
                    :font-weight "600"
                    :color "white"
                    :margin "0"}}
        category-title]]))

(defn- header []
  [:div {:id "customizer--category-header"}
    [back-button]
    [title]
    [drawer-close-button]])

;; ---- Header ----

;; ---- Drawer ----

(defn desktop-categories-button []
  (let [drawer-open? @(r/subscribe [:customizer.menu/state])]
    [button/view {:id       "customizer--sidebar--categories-btn"
                  :class    "header--grow-button"
                  :color    (if drawer-open? "var(--irb-clr)" "rgba(255, 255, 255, 0.1)")
                  :style    {:color (when drawer-open? "black")}
                  :on-click #(if drawer-open?
                               (r/dispatch [:customizer.menu/close!])
                               (r/dispatch [:customizer.menu/open!]))}
      [:i.fa-solid.fa-layer-group {:style {:font-size "18px"}}]
      "Items"]))

(defn mobile-categories-button []
  [:button {:class    "customizer--sidebar--item effect--bg-grow"
            :id       "customizer--sidebar--categories-btn"
            :style   {:position "relative"}
            :on-click #(r/dispatch [:customizer.menu/open!])}
    [:i.fa-solid.fa-layer-group {:style {:font-size "18px"}}]
    [:p "Items"]])

(defn drawer []
  [floater/view {:orientation :left
                 :class       "customizer--floater-ui"
                 :state       @(r/subscribe [:customizer.menu/state])
                 :config {:bg false}}
                ;;  :on-close    #(r/dispatch [:customizer.menu/close!])
                ;;  :config      {:bg {:style {:background "rgba(0, 0, 0, 0.5)"}}}}
   [:div {:style    {:height         "100%"
                     :display        "flex"
                     :flex-direction "column"}}
     [header]
     [cards/view]
     [populars/view]]])

(defn desktop-view []
  [:<> 
    [desktop-categories-button]
    [drawer]])

(defn mobile-view []
  [:<>
   [mobile-categories-button]
   [drawer]])