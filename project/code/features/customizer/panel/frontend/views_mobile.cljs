;; (ns features.customizer.panel.frontend.views-mobile
;;   (:require
;;    ["react"                :as react]
;;    [elements.api           :as elements]
;;    [re-frame.core          :as r]))

;;   ;;  [shared.frontend.components.api                :as shared.components]))
   
;;   ;;  [features.customizer.panel.frontend.blocks.menu         :as menu]
;;   ;;  [features.customizer.panel.frontend.blocks.form         :as form]
;;   ;;  [features.customizer.panel.frontend.blocks.price        :as price]
;;   ;;  [features.customizer.panel.frontend.blocks.canvas       :as canvas]
;;   ;;  [features.customizer.panel.frontend.blocks.colors       :as colors]
;;   ;;  [features.customizer.panel.frontend.blocks.filters      :as filters]
;;   ;;  [features.customizer.panel.frontend.blocks.coating-info :as coating-info]))


;; ;; -----------------------------------------------------------------------------
;; ;; ---- Components ----

;; ;; ---- Header ----

;; (defn- logo []
;;   [:a {:href "/"}
;;    [:img {:src "/logo/text-1.svg"
;;           :style {:max-width "150px"
;;                   :width     "100%"}}]])

;; (defn- badge [cart-count]
;;   [:div {:id    "cart-badge"
;;          :style {:display (if (< 0 cart-count) "inline-flex" "none")
;;                  :height  "15px" :border-radius "50vw"}}
;;     [:span cart-count]])

;; (defn- cart-icon []
;;   [elements/icon {:icon        :shopping_cart
;;                   :icon-family :material-icons-outlined
;;                   :size        :s}])

;; (defn- cart-button []
;;   (let [{:keys [count]} @(r/subscribe [:cart/data])]
;;     [:div {:style {:isolation "isolate" :display "flex"}}
;;       [badge count]
;;       [:button {:id       "header--cart-button"
;;                 :class    "effect--bg-grow"
;;                 :on-click #(r/dispatch [:cart.drawer/open!])}

;;         [cart-icon]]]))

;; (defn- mobile-drawer-button []
;;    [:button {:id       "header--cart-button"
;;              :class    "effect--bg-grow"
;;              :on-click #(r/dispatch [:x.db/set-item! [:customizer.mobile/drawer] :open])}
;;      [elements/icon {:icon        :menu
;;                      :icon-family :material-icons-outlined
;;                      :size        :s}]])

;; (defn- header []
;;   [:div {:id "customizer--header"
;;          :style {:border-radius "unset"
;;                  :padding "10px"
;;                  :height  "auto"}}

;;    [mobile-drawer-button]
;;    [logo]
;;    [cart-button]])

;; ;; ---- Header ----
;; ;; -----------------------------------------------------------------------------

;; ;; -----------------------------------------------------------------------------
;; ;; ---- Drawer ----

;; (defn- location-button []
;;   [:button {:class    "navbar-button effect--bg-grow"
;;             :on-click #(do
;;                          (r/dispatch [:x.db/set-item! [:customizer.tutorial/step] :shop-chooser])
;;                          (r/dispatch [:x.db/set-item! [:customizer.tutorial/sheet] :open]))
;;             :style    {:padding  "5px 10px" 
;;                        :border-radius "50vw"
;;                        :min-width "200px"}}
;;      (str @(r/subscribe [:db/get-in [:map :town] "Locations"]))])

;; (defn- my-shop-button []
;;   [:a {:href  "/signup"
;;        :class "navbar-button effect--bg-grow"
;;        :style {:padding  "5px 10px" :border-radius "50vw"}} 
;;     "My Shop"])

;; (defn- search-bar []
;;   [:div {:style {"--fill-color" "rgba(255,255,255,0.1)"
;;                  :flex-grow     "1"
;;                  :max-width     "500px"}}
;;     [elements/text-field {:style            {:color "inherit" :border "unset"}
;;                           :placeholder      "Search part or color"
;;                           :border-radius    :l
;;                           :start-adornments [{:color :highlight
;;                                               :icon  :search}]}]])

;; ;; (defn- mobile-drawer []
;; ;;   [shared.components/drawer {:orientation :left
;; ;;                              :title       [:p "halika"]
;; ;;                              :state       @(r/subscribe [:db/get-in [:customizer.mobile/drawer] :close])
;; ;;                              :on-minimize #(r/dispatch [:x.db/set-item! [:customizer.mobile/drawer] :close])
;; ;;                              :on-close    #(r/dispatch [:x.db/set-item! [:customizer.mobile/drawer] :close])
;; ;;                              :on-open     #(r/dispatch [:x.db/set-item! [:customizer.mobile/drawer] :open])
;; ;;                              :style       {:height  "100dvh"
;; ;;                                            :width   "350px"
;; ;;                                            :padding "15px"}}
;; ;;     [:div 
;; ;;      [search-bar]
;; ;;      [location-button]
;; ;;      [my-shop-button]]])

;; ;; ---- Drawer ----
;; ;; -----------------------------------------------------------------------------

;; ;; -----------------------------------------------------------------------------
;; ;; ---- Sidebar ----

;; (defn form-drawer-button [part-selected? set-a]
;;   (when part-selected?
;;     [:button {:class       "customizer--sidebar--item effect--bg-grow"
;;               :id          "customizer--details-button"
;;               :data-filled (< 4 (count @(r/subscribe [:db/get-in [:formdata]])))
;;               :on-click    #(set-a true)
;;               :style       {:top      "var(--header-offset)"
;;                             :right    "10px"
;;                             :position "absolute"}}
;;       [elements/icon {:icon :settings}]]))

;; (defn- form-drawer [part-data formdata]
;;   (let [part-selected? (contains? part-data :form)
;;         [a set-a]      (react/useState false)
;;         open?          (if a :open :close)]

;;     [:<> 
;;       [form-drawer-button part-selected? set-a]
;;       [shared.components/floater {:id          "formconfig"
;;                                   :orientation :right
;;                                   :class       "customizer--floater-ui"
;;                                   :config      {:header false
;;                                                 :bg     false
;;                                                 :disable-scroll true}
;;                                   :state       open?}
        
;;         [:div {:style {:height         "100%"
;;                        :display        "flex"
;;                        :flex-direction "column"}}
         
;;           [:div {:style {:display         "flex"
;;                          :align-items     "center"
;;                          :justify-content "center"}}
;;             [:p {:style {:font-weight "500"}} "Details"]
;;             [:button {:on-click #(set-a false)
;;                       :style    {:position "absolute"
;;                                  :right    0}}
;;               [elements/icon {:icon :chevron_right}]]]
         
;;           [:div {:class "hide-scrollbar" 
;;                  :style {:overflow-y "auto"}}
;;             [form/view part-data formdata]]]]]))


;; (defn- upload-parts []
;;   [:button {:id    "customizer--sidebar--upload-parts-btn"
;;             :class "customizer--sidebar--item effect--bg-grow"
;;             :title "Upload Custom Part"
;;             :on-click #(r/dispatch [:parts.custom.sheet/open!])}
;;     [elements/icon {:icon  :add
;;                     :style {:font-size "18px"}}]])

;; (defn- tutorial-icon []
;;   [elements/icon {:icon        :local_library
;;                   :icon-family :material-icons-outlined
;;                   :style       {:font-size "18px"}}])

;; (defn- tutorial-button []
;;   [:button {:id       "customizer--sidebar--tutorial-btn"
;;             :class    "customizer--sidebar--item effect--bg-grow"
;;             :title    "Tutorial"
;;             :on-click #(r/dispatch [:x.db/set-item! [:tour :open?] true])}
;;     [tutorial-icon]])

;; (defn- sidebar [part-data]
;;   (let [[open? set-o] (react/useState false)]
;;     [:div {:style {:position "absolute"
;;                    :bottom   "var(--footer-offset)"
;;                    :left     "10px"}}
;;       [:div {:id "customizer--sidebar"
;;              :style {:height   (if open? "max-content" "0px")
;;                      :overflow "hidden"}}
;;   ;;  [upload-parts]
;;         [tutorial-button]
;;         [coating-info/mobile-view part-data]
;;         [filters/mobile-view]
;;         [menu/mobile-view]]
;;       [:button {:class    "customizer--sidebar--item effect--bg-grow"
;;                  :on-click #(set-o not)
;;                  :style    {}}
;;          [elements/icon {:icon (if open? :close :tune)}]]]))

;; ;; ---- Sidebar ----
;; ;; -----------------------------------------------------------------------------

;; ;; -----------------------------------------------------------------------------
;; ;; ---- Footer ----

;; (defn- footer []
;;   [:div {:id    "customizer-dock"
;;          :style {:background  "rgba(0, 0, 0, 0.544)"
;;                  :gap         "15px"
;;                  :padding     "10px 0px"
;;                  :position    "absolute" 
;;                  :bottom      "0"
;;                  :left        "0"
;;                  :right       "0"
;;                  :align-items "center"}}

;;     [colors/menu]])

;; ;; ---- Footer ----
;; ;; -----------------------------------------------------------------------------

;; (defn add-button []
;;   [:button {:id       "parts-configurator--proceed-button"
;;             :class    "customizer--sidebar--item effect--bg-grow"
;;             :title    "Add to cart"
;;             :on-click #(r/dispatch [:customizer/add-to-cart!])}
;;     [elements/icon {:icon :add_shopping_cart}]])

;; (defn save-button []
;;   [:button {:id       "parts-configurator--proceed-button"
;;             :class    "customizer--sidebar--item effect--bg-grow"
;;             :title    "Save changes"
;;             :style    {:width         "100%"}
;;                       :padding       "8px"
;;                       :border-radius "50vw"
;;             :on-click #(r/dispatch [:customizer.edit/save!])}
;;     [elements/icon {:icon :brush}]])

;; (defn- proceed-button [part-data]
;;   [:<>
;;     [price/view part-data]
;;     [:div {:style {:position "fixed"
;;                    :bottom   "75px"
;;                    :right    "10px"}}
;;        (case @(r/subscribe [:db/get-in [:customizer :state]])
;;            :add  [add-button]
;;            :edit [save-button]
;;         nil)]])

;; (defn- mobile-view []
;;   (let [part-data @(r/subscribe [:db/get-in [:customizer :item]])]
;;      [:div {:id "customizer--mobile-view"}
;;       [header]
;;       [canvas/view (assoc part-data :scale 3)]
;;       [sidebar part-data]
;;       [form-drawer part-data]
;;       [proceed-button part-data]
;;       [footer]]))

;; (defn view []
;;   [:<> 
;;     ;[mobile-drawer]
;;     [mobile-view]])