
(ns features.customizer.panel.frontend.blocks.cart-dialog
  (:require
    [re-frame.core :as r]

    [ui.popup :as popup]
    [ui.button :as button]
    [ui.checkbox :as checkbox]
   
    [features.customizer.blocks.dialog :as dialog]))


;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-fx
  :customizer.cart-dialog/open!
  (fn [_ [_]]
    {:dispatch [:db/assoc-in [:customizer/cart-dialog] true]}))

(r/reg-event-fx
 :customizer.cart-dialog/close!
 (fn [_ [_]]
   {:dispatch [:db/dissoc :customizer/cart-dialog]}))

;; ---- Effects ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn disable-dialog-checkbox []
  [checkbox/view {:label     "Don't show this again"
                  :style     {:font-size "0.8rem"
                              :padding   "4px 8px"
                              :width     "100%"
                              :margin "10px 0"}
                  :checked?  @(r/subscribe [:db/get-in [:user :disable-cart-dialog?] false])
                  :on-change #(r/dispatch [:db/update-in [:user :disable-cart-dialog?] not])}])

(defn title []
  [:p {:style {:font-size "1.2rem"
               :font-weight "500"
               :margin-bottom "15px"
               :text-align "center"
               :color "#fff"}}
    "Are u sure done?"])

(defn cart-dialog []
  [dialog/view {:state      @(r/subscribe [:db/get-in [:customizer/cart-dialog]])
                :on-cancel  #(r/dispatch [:customizer.cart-dialog/close!])
                :on-confirm #(do
                               (r/dispatch [:customizer.cart-dialog/close!])
                               (r/dispatch [:customizer/add-to-cart!]))}
    [:<>
      [title]
      [:p "Seems like you have unconfigured parts."]
      [disable-dialog-checkbox]]])
      
;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view []
  [cart-dialog])