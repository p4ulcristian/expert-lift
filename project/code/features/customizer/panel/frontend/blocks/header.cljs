
(ns features.customizer.panel.frontend.blocks.header
  (:require
   [features.customizer.data.core :refer [USA-STATES]]
   [re-frame.core          :as r]
   [ui.button :as button]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn- logo []
  [:a {:href "/"
       :style {:position  "absolute"
               :left      "50%"
               :translate "-50%"}}
    [:img {:src "/logo/text-horizontal.svg"
           :style {:max-width "150px"
                   :width     "100%"}}]])

(defn- badge [cart-count]
  [:div {:id    "cart-badge"
         :style {:display (if (< 0 cart-count) "inline-flex" "none")
                 :height  "15px" :border-radius "50vw"}}
    [:span cart-count]])

(defn- cart-button []
  (let [{:keys [count]} @(r/subscribe [:cart/data])
        drawer-open? @(r/subscribe [:db/get-in [:cart.preview.drawer/state] false])]
    [:div {:style {:isolation "isolate" :display "flex"}}
      [badge count]
      [button/view {:id       "header--cart-button"
                    :class    "effect--bg-grow"
                    :on-click #(r/dispatch [:cart.drawer/open!])
                    :style    {:padding  "5px" 
                               :border-radius "50vw"
                               :background (when drawer-open? "var(--irb-clr)")
                               :color (when drawer-open? "black")}}
        [:i {:class ["fa-solid" "fa-cart-shopping"]}]]]))

(defn- profile-button []
  [:a {:href "/orders"
       :style {:text-decoration "none"
               :color "inherit"}}
    [button/view {:id       "header--profile-button"
                  :class    "effect--bg-grow"
                  :style    {:padding  "5px" :border-radius "50vw"
                             :color "inherit"}}
      [:i {:class ["fa-solid" "fa-user"]
           :style {:color "inherit"}}]]])

(defn- my-designs-button []
  (let [drawer-open? @(r/subscribe [:db/get-in [:my-designs.drawer/state] false])]
    [button/view {:id       "header--my-designs-button"
                  :class    "effect--bg-grow"
                  :on-click #(r/dispatch [:my-designs.drawer/open!])
                  :style    {:padding  "5px" 
                             :border-radius "50vw"
                             :background (when drawer-open? "var(--irb-clr)")
                             :color (when drawer-open? "black")}}
      [:i {:class ["fa-solid" "fa-heart"]}]]))
       

(defn- location-button []
  (let [shop-address @(r/subscribe [:db/get-in [:shop :facility-address]])
        selected-workshop-name @(r/subscribe [:customizer.location/selected-workshop-name])]
    [button/view {:id        "customizer--location-button"
                  :class     "customizer-header--menu-items"
                  :data-open (not (nil? shop-address))
                  :on-click  #(r/dispatch [:db/assoc-in [:customizer/location :state] true])
                  :style     {:white-space "nowrap"}}
       [:i {:class ["fa-solid" "fa-location-dot"]}]
       (or selected-workshop-name "Locations")]))

(defn- search-action-button []
  [button/view {:icon :search
                :style {:border-radius "50vw"
                        :aspect-ratio  1}
                :hover-color "var(--irb-clr)"
                :background-color "rgba(255, 255, 255, 0.1)"}        
    [:i {:class ["fa-solid" "fa-magnifying-glass"]}]])

(defn- tutorial-icon []
  [:i {:class ["fa-solid" "fa-book-open"]}])

(defn- tutorial-button []
  [:button {:id       "customizer--sidebar--tutorial-btn"
            :class    "customizer-header--menu-items"
            :on-click #(r/dispatch [:db/assoc-in [:tour :open?] true])}
    [tutorial-icon]
    "Tutorial"])

(defn- header []
  [:div {:id "customizer--header"}
    [:div {:style {:display "flex" :gap "15px"
                   :margin-right "auto"}}
      ;; [menu/desktop-view]
      ;; [filters/desktop-view]
      ;; [coating-info/desktop-view part-data]
      [tutorial-button]]
    [logo]
    [:div {:id    "navbar-menu-items--buttons"
           :style {:font-size "0.9rem"
                   :display   "flex"
                   :align-items "center"}}
     [search-action-button]
     [location-button]
     [cart-button]
     [my-designs-button]
     [profile-button]]])


;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [part-data]
  [header part-data])