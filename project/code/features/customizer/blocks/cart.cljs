
(ns features.customizer.blocks.cart
  (:require
    [features.customizer.blocks.cart.subs]
    [features.customizer.blocks.cart.effects]

    [router.frontend.zero :as router]
    [re-frame.core                  :as r]
    [clojure.string                 :as clojure.string]
    [ui.floater                     :as floater]
    [ui.button                      :as button]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn parts->colors [parts]
  (clojure.string/join ", "
    (mapv (fn [[_ v]]
            (get-in v [:look :basecolor]))
          parts)))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn icon 
  ([{:keys [picture_url]}]
   [:img {:src   picture_url
          :style {:width "48px" :filter "invert(1)"}}])
  ([{:keys [picture_url]} width]
   [:img {:src   picture_url
          :style {:width width :filter "invert(1)"}}]))

(defn label [index {:keys [name precursor type] :as job-props}]
  [:div {:style {:flex-grow 1}}
    [:p [:strong (str "#" (inc index))] " " precursor]
    [:p {:style {:display "flex" :align-items "flex-end" :justify-content "space-between"}}]

    [:span {:style {:font-size "1rem"}}
       [:b name]
       (when (= type "part") 
         [:span {:style {:font-size "14px"}} 
           (str " (x " (get-in job-props ["formdata" "quantity" :qty]) ")")])]])

;; -----------------------------------------------------------------------------
;; ---- Part ----

(defn part-label [{:keys [name]
                   :strs [formdata]}]
  [:div {:style {:display     "flex"
                 :gap         "3px"
                 :overflow    "hidden"
                 :margin-left "13px"
                 :flex-grow   1}}
                 
    [:b {:style {:text-overflow "ellipsis"
                 :overflow      "hidden"
                 :white-space "nowrap"}}
        name]
    [:span {:style {:white-space "nowrap"}} 
      (str " (x " (get-in formdata ["quantity" :qty]) ")")]])

(defn part-remove-button [job-id part-id basecolor]
  [button/view {:mode     :clear_2
                :style    {:padding "4px" :min-height "auto" :font-size "12px" :width "20px" :height "20px" :display "flex" :align-items "center" :justify-content "center"}
                :on-click #(do (r/dispatch [:cart/remove-part! job-id part-id]))}
    [:i {:class ["fa-solid" "fa-xmark"]}]])

(defn part-edit-button [job-id part-props basecolor]
  [button/view {:mode     :clear_2
                :style    {:padding "4px" :min-height "auto" :font-size "12px" :width "20px" :height "20px" :display "flex" :align-items "center" :justify-content "center"}
                :on-click #(do
                             (r/dispatch [:customizer.item.part/edit! job-id part-props])
                             (r/dispatch [:x.db/set-item! [:cart.preview.drawer/state] :close]))}

    [:i {:class ["fa-solid" "fa-sliders"]}]])

(defn part-look [{:keys [look]}]
  [:<>
    [:p "Look:"] 
    [:b (:name look 2)]])

(defn part [job-id [part-id {:keys [price look] :as part-props
                             :or {price "n/a"}}]]
  [:div {:style {:border        "1px solid"
                 :border-color  (:basecolor look)
                 :padding       "8px"
                 :border-radius "6px"
                 :font-size     "0.75rem"}}
    [:div {:class "cart-item--wrapper"
           :style {:margin-bottom "6px"}}
      [icon part-props "35px"]
      [part-label part-props]
      [:div {:style {:display "flex"
                     :gap      "1px"}}
        [part-edit-button job-id part-props (:basecolor look)]
        [part-remove-button job-id part-id (:basecolor look)]]]
    [:div {:key   (str "-" part-id)
           :style {:display               "grid"
                   :gap                   "3px 19px"
                   :grid-template-columns "auto 1fr"}}
      [part-look part-props]
      [:div "Price:"][:b "$" price]]])

;; ---- Part ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Grouped Job ----

(defn edit-button [job-id job-props]
  [button/view {:mode     :clear_2
                :style    {:padding "4px" :min-height "auto" :font-size "12px" :width "20px" :height "20px" :display "flex" :align-items "center" :justify-content "center"}
                :on-click #(do
                             (r/dispatch [:customizer/edit! job-id job-props])
                             (r/dispatch [:x.db/set-item! [:cart.preview.drawer/state] :close]))}
   
    [:i {:class ["fa-solid" "fa-sliders"]}]])

(defn remove-button [job-id]
  [button/view {:mode     :clear_2
                :style    {:padding "4px" :min-height "auto" :font-size "12px" :width "20px" :height "20px" :display "flex" :align-items "center" :justify-content "center"}
                :on-click #(r/dispatch [:cart/remove-job! job-id])}
    [:i {:class ["fa-solid" "fa-xmark"]}]])

;; ---- Grouped Job ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Job ----

(defn job-controls [index job-id job-props]
  [:div {:class "cart-item--wrapper"}
   
    [icon job-props]
    [label index job-props]
    [:div {:class "cart-item--options"}
       [edit-button job-id job-props]
       [remove-button job-id]]])

(defn job [index job-id {:keys [look price] :as job-props}]
  [:div {:key   job-id
         :class "cart-item--container"
         :style {"--cart-item-clr" (:basecolor look)
                 :font-size        "0.8rem"}}
   
    [job-controls index job-id job-props]
    [:div {:style {:display        "flex"
                   :flex-direction "column"
                   :gap            "8px"}}
  
      [:div {:key   (str "-" job-id)
             :style {:display               "grid"
                     :gap                   "3px 17px"
                     :grid-template-columns "auto 1fr"}}
       ;; [part-name job-props]
       [part-look job-props]
       [:div "Price:"][:b "$" price]]]])

;; ---- Job ----
;; -----------------------------------------------------------------------------


;; -----------------------------------------------------------------------------
;; ---- Package ----

(defn package-edit-button [job-id job-props]
  [button/view {:mode     :clear_2
                :style    {:padding "4px" :min-height "auto" :font-size "12px" :width "20px" :height "20px" :display "flex" :align-items "center" :justify-content "center"}
                :on-click #(do
                             (r/dispatch [:customizer/edit! job-id job-props])
                             (r/dispatch [:x.db/set-item! [:cart.preview.drawer/state] :close]))}
   
    [:i {:class ["fa-solid" "fa-sliders"]}]])

(defn package-remove-button [job-id]
  [button/view {:mode     :clear_2
                :style    {:padding "4px" :min-height "auto" :font-size "12px" :width "20px" :height "20px" :display "flex" :align-items "center" :justify-content "center"}
                :on-click #(r/dispatch [:cart/remove-job! job-id])}
    [:i {:class ["fa-solid" "fa-xmark"]}]])

(defn package-controls [index job-id job-props]
  [:div {:class "cart-item--wrapper"
         :style {:margin-left "8px"}}
    [icon job-props]
    [label index job-props]
    [:div {:style {:display "flex"
                     :gap      "6px"}}
      [package-edit-button job-id job-props]
      [package-remove-button job-id]]])

(defn package [index job-id {:keys [parts price] :as job-props}]
  (let [parts (sort-by #(-> % second :name) parts)]
    [:div {:class "cart-item--container"
           :style {"--cart-item-clr" (parts->colors parts)
                   :font-size        "0.8rem"}}
      [package-controls index job-id job-props]
      [:div {:key   (str "-" job-id)
             :style {:display               "grid"
                     :gap                   "3px 17px"
                     :grid-template-columns "auto 1fr"}}
         [:p "Price:"][:b "$" price]]]))
      

(defn package-with-parts [index job-id {:keys [parts] :as job-props}]
  (let [parts (sort-by #(-> % second :name) parts)]
    [:div {:class "cart-item--container"
           :style {"--cart-item-clr" (parts->colors parts)
                   :font-size        "0.8rem"}}
      [package-controls index job-id job-props]
      [:div {:style {:display        "flex"
                     :flex-direction "column"
                     :gap            "10px"}}

        (map (fn [[part-id _ :as part-props]]
               ^{:key (str job-id "-" part-id)}
               [part job-id part-props])
           parts)]]))

;; ---- Package ----
;; -----------------------------------------------------------------------------

(defn cart-item-card [index [job-id {:keys [type parts] :as job-data}]]
  (cond 
    (and (= "package" type) (map? parts))    ^{:key job-id} [package-with-parts index job-id job-data]
    (and (= "package" type) (vector? parts)) ^{:key job-id} [package index job-id job-data]
    (= "part" type)                          ^{:key job-id} [job index job-id job-data]
    :else
    ^{:key job-id}[job index job-id job-data]))

(defn cart-preview []
  (let [cart-content @(r/subscribe [:db/get-in [:cart :content]])]
    [:div {:id    "cart-items-preview"
           :class "hide-scroll"}
      (map-indexed cart-item-card cart-content)]))

(defn checkout-button []
  [button/view {:id       "cart-preview--checkout-button"
                :mode     :filled
                :color    "var(--irb-clr)"
                :style    {:width "100%"
                           :height "48px"
                           :border-radius "12px"
                           :font-size "16px"
                           :font-weight "600"
                           :padding "12px"}
                :on-click #(if true; @(r/subscribe [:db/get-in [:user-profile]])
                             (router/navigate! {:path "/checkout"})
                             (r/dispatch [:cart.drawer/close!]))}
   "Checkout"])

(defn create-order-button []
  [button/view {:id       "cart-preview--create-order-button"
                :mode     :outline
                :color    "var(--irb-clr)"
                :style    {:width "100%"
                           :height "40px"
                           :border-radius "12px"
                           :font-size "14px"
                           :font-weight "600"
                           :padding "8px"
                           :margin-top "8px"}
                :on-click #(do
                             (r/dispatch [:checkout/create-order!])
                             (r/dispatch [:notifications/notify! :info "Creating order..."]))}
   "Create Order"])

(defn total-display []
  (let [total @(r/subscribe [:cart/total])]
    [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :align-items "center"
                   :padding "12px 0"
                   :font-size "18px"
                   :font-weight "600"}}
     [:span "Total:"]
     [:span {:style {:color "var(--irb-clr)"}} (str "$" total)]]))

(defn content []
  [:div {:style {:margin "15px 0"}}
    [cart-preview]
    [total-display]
    [checkout-button]
    [create-order-button]])

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn close-button []
  [button/view {:mode     :clear_2
                :on-click #(r/dispatch [:cart.drawer/close!])
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
    [:p "Cart"]
    [close-button]])

(defn badge [cart-count]
  [:div {:id    "cart-badge"
         :style {:display (if (< 0 cart-count) "inline-flex" "none")
                 :height  "15px" :border-radius "50vw"}}
    [:span cart-count]])

(defn cart-button []
  (let [{:keys [count]} @(r/subscribe [:cart/data])
        drawer-open? @(r/subscribe [:db/get-in [:cart.preview.drawer/state] false])]
    [:div {:style {:position  "relative"
                   :isolation "isolate" 
                   :display   "flex"}}
      [badge count]
      [button/view {:id       "header--cart-button"
                    :class    "header--grow-button"
                    :color    (if drawer-open? "var(--irb-clr)" "rgba(255, 255, 255, 0.1)")
                    :style    {:color (when drawer-open? "black")}
                    :on-click #(if drawer-open?
                                 (r/dispatch [:cart.drawer/close!])
                                 (r/dispatch [:cart.drawer/open!]))}
        [:i {:class ["fa-solid" "fa-cart-shopping"]}]
        "Cart"]]))

(defn view []
  [floater/view {:orientation :right
                 :class       "floater customizer--floater-ui"
                 :state       @(r/subscribe [:db/get-in [:cart.preview.drawer/state] false])
                 :on-close    #(r/dispatch [:cart.drawer/close!])
                 :config      {:bg false}}
                ;;  :style       {:z-index   "100"}}
   
    [:<> 
      [title]
      [content]
     ]])

  