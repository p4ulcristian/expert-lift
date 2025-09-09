(ns features.customizer.panel.frontend.views-desktop
  (:require
   [re-frame.core          :as r]

   [features.customizer.data.core :refer [USA-STATES]]

   [ui.button :as button]

   [features.customizer.blocks.cart                   :as cart]
   [features.customizer.panel.frontend.blocks.canvas  :as canvas]
   [features.customizer.panel.frontend.blocks.looks   :as looks]
   [features.customizer.panel.frontend.blocks.menu    :as menu]
   [features.customizer.panel.frontend.blocks.form    :as form]
   [features.customizer.panel.frontend.blocks.price   :as price]
   [features.customizer.panel.frontend.blocks.filters :as filters]
   [features.customizer.panel.frontend.blocks.location :as location]))

;; -----------------------------------------------------------------------------
;; ---- Header ----

(defn- logo []
  [:a {:href "/"
       :style {:position  "absolute"
               :left      "50%"
               :translate "-50%"}}
    [:img {:src "/logo/text-horizontal.svg"
           :style {:max-width "150px"
                   :width     "100%"}}]])
       

(defn- location-button []
  (let [shop-address @(r/subscribe [:db/get-in [:shop :facility-address]])]
    [button/view {:id       "customizer--location-button"
                  :class    "header--grow-button"
                  :color    "rgba(255, 255, 255, 0.1)"
                  :data-open (not (nil? shop-address))
                  :on-click  #(r/dispatch [:customizer.location.sheet/open!])}
      [:i {:class ["fa-solid" "fa-location-dot"]}]
      (if shop-address
        (str (:city shop-address) ", " (->> (:state shop-address)
                                            (get USA-STATES)))
        "Locations")]))

(defn- search-action-button []
  [button/view {:class "header--button"
                :color "rgba(255, 255, 255, 0.1)"}        
    [:i {:class ["fa-solid" "fa-magnifying-glass"]}]])

(defn- tutorial-button []
  [button/view {:id       "customizer--sidebar--tutorial-btn"
                :class    "header--grow-button"
                :color    "rgba(255, 255, 255, 0.1)"
                :on-click #(r/dispatch [:db/assoc-in [:tour :open?] true])}
    [:i {:class ["fa-solid" "fa-book-open"]}]
    "Tutorial"])

(defn- profile-button []
  [:a {:href "/orders"
       :style {:text-decoration "none"
               :color "inherit"}}
    [button/view {:id       "header--profile-button"
                  :class    "header--button"
                  :color    "rgba(255, 255, 255, 0.1)"
                  :style    {:color "inherit"}}
      [:i {:class ["fa-solid" "fa-user"]
           :style {:color "inherit"}}]]])

(defn- my-designs-button []
  (let [drawer-open? @(r/subscribe [:db/get-in [:my-designs.drawer/state] false])]
    [button/view {:id       "header--my-designs-button"
                  :class    "header--button"
                  :color    (if drawer-open? "var(--irb-clr)" "rgba(255, 255, 255, 0.1)")
                  :style    {:color (when drawer-open? "black")}
                  :on-click #(r/dispatch [:my-designs.drawer/open!])}
      [:i {:class ["fa-solid" "fa-heart"]}]]))

(defn- header [edited-item]
  [:div {:id "customizer--header"}
    [:div {:style {:display      "flex" 
                   :gap          "15px"
                   :margin-right "auto"}}
      [menu/desktop-view]
      [filters/desktop-view]
      ;; [filters/desktop-view]
      ;; [coating-info/desktop-view part-data]
      [tutorial-button]]
    [logo]
    [:div {:id    "navbar-menu-items--buttons"
           :style {:font-size "0.9rem"
                   :display   "flex"
                   :gap       "15px"
                   :align-items "center"}}
     [search-action-button]
     [location/button]
     [form/view edited-item]
     [cart/cart-button]
     [my-designs-button]
     [profile-button]]])

;; ---- Header ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Footer ----

(defn add-button []
  [button/view {:id       "customizer--proceed-button"
                :mode     :filled
                :color    "var(--irb-clr)"
                :style    {:width "100%"
                           :height "100%"
                           :border-radius "12px"
                           :font-size "16px"
                           :font-weight "600"
                           :padding "12px"}
                :on-click #(r/dispatch [:customizer/proceed!])}
      "Add to cart"])

(defn edit-button []
  [button/view {:id       "customizer--proceed-button"
                :mode     :filled
                :color    "var(--irb-clr)"
                :style    {:width "100%"
                           :height "100%"
                           :border-radius "12px"
                           :font-size "16px"
                           :font-weight "600"
                           :padding "12px"}
                :on-click #(r/dispatch [:customizer.edit/save!])}
      "Save changes"])

(defn select-button []
  [button/view {:id       "customizer--proceed-button"
                :mode     :filled
                :color    "var(--irb-clr)"
                :style    {:width "100%"
                           :height "100%"
                           :border-radius "12px"
                           :font-size "16px"
                           :font-weight "600"
                           :padding "12px"}
                :on-click #(do  
                             (r/dispatch [:customizer.menu/open!])
                             (r/dispatch [:notifications/notify! nil [:div {:style {:display "flex" :gap "10px" :align-items "center"}}
                                                                       [:i {:class ["fa-solid" "fa-circle-info"] :style {:color "var(--irb-clr)"}}]
                                                                       [:p "Pick a " [:b "package/part"] " to customize"]]]))} 
   "Select Part"])

(defn- proceed-button [edited-item]
  [:<>
    ;; [cart-dialog/view]
    (case @(r/subscribe [:db/get-in [:customizer :state]])
      :add  [add-button]
      :edit [edit-button]
      [select-button])])

(defn- footer [edited-item]
  [:div {:id    "customizer-dock"
         :style {:display               "grid"
                 :background            "rgba(0, 0, 0, 0.544)"
                 :gap                   "0"
                 :padding               "6px 0"
                 :border-radius         "12px"
                 :position              "absolute" 
                 :bottom                "0"
                 :left                  "0"
                 :right                 "0"
                 :align-items           "center"
                 :grid-template-columns "280px 2px minmax(45%, auto) 2px 280px"}}
    [looks/favorite]
    [:div {:style {:height "100%" :width "2px" :background "gray"}}]
    [:div {:style {:padding "0 15px"}}
     [looks/menu]]
    [:div {:style {:height "100%" :width "2px" :background "gray"}}]
    [:div {:style {:padding "5px"}}
     [proceed-button edited-item]]])

;; ---- Footer ----
;; -----------------------------------------------------------------------------

(defn- desktop-view []
  (let [edited-item @(r/subscribe [:customizer/get-edited-item])]
    [:div {:id "customizer--desktop-view"}
      [header edited-item]
      
      [canvas/view edited-item]
      [footer edited-item]]))

(defn view []
  [desktop-view])

