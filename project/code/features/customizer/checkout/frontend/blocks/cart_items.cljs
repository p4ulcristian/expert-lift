
(ns features.customizer.checkout.frontend.blocks.cart-items
  (:require
    ["react"        :as react]
    [re-frame.core  :as r]
    [ui.button      :as button]
    [ui.link        :as link]
    [clojure.string :as clojure.string]))

(defn parts->colors [parts]
  (clojure.string/join ", "
    (mapv (fn [[_ v]]
            (get-in v [:look :basecolor]))
          parts)))


;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn field-value [value]
  (cond
    (contains? value :label) (get value :label)
    (contains? value :qty)   (get value :value)
    (or (string? value)
        (int? value)) value
    :else (str value)))

(defn render-formdata [data]
  [:<>
    (doall
      (map (fn [[input-id {:keys [value] :as v}]]
             (when-not (= 0 value)
               ^{:key (str (random-uuid))}
               [:<>
                 [:p (if-let [field @(r/subscribe [:form.dictionary/get-by-id input-id])]
                        (str field) ":")]
                 (if (and (map? v)
                          (not (contains? v :value)))
                   [:div {:style {:display               "grid"
                                  :gap                   "8px"
                                  :grid-template-columns "auto 1fr"}}
                     [render-formdata v]]
                   [:b (:prefix v) (field-value v)])]))
           (dissoc data "look-cost" "quantity" "condition")))])

(defn item-formdata [{:keys [color]
                      :strs [formdata]}]
  (let [[o? set-o] (react/useState false)]
    
    [:<> 
      [button/view {:on-click #(set-o not)
                    :mode :clear
                    :style    {:display "flex"
                               :margin-left "auto"}} 
        (if o? "Less" "More")] 
      (when o? 
        [:div {:style {:max-height            "50vh"
                       :display               "grid"
                       :gap                   "8px"
                       :grid-template-columns "auto 1fr"}}
          [:p "Current surface: "] [:b (str (get-in formdata ["condition" :label] "-"))]
          [render-formdata formdata]])]))

;; -----------------------------------------------------------------------------
;; ---- Part ----

(defn part-remove-button [job-id part-id]
  [button/view {:class    "cart-item--remove-button"
                :mode :clear
                :on-click #(do (r/dispatch [:cart/remove-part! job-id part-id]))}
    [:i {:class  "fa-solid fa-square-minus"}]])
    ;; [:i {:class  "fa-solid fa-circle-xmark"}]])


(defn part-edit-button [job-id part-props]
  [link/view {:class    "cart-item--edit-button"
              :href     "/customize"
              :mode :clear
              :on-click #(do
                           (r/dispatch [:customizer.item.part/edit! job-id part-props])
                           (r/dispatch [:db/assoc-in [:cart.preview.drawer/state] :close]))}

    [:i {:class  "fa-solid fa-pen-to-square"}]])

(defn part-name [{:keys [name]
                  :strs [formdata]}]
  [:<>
    [:p "Name:"] 
    [:p 
      [:b name]
      [:span {:style {:font-size "14px"}} (str " (x " (get-in formdata ["quantity" :qty]) ")")]]])

(defn part-look [{:keys [look]}]
  [:<>
    [:p "Look:"] 
    [:b (:name look)]])

(defn part [job-id [part-id {:keys [price name look] :as part-props}]]
  [:div  {:style {:position "relative"
                  :border "1px solid"
                  :border-color (:basecolor look)
                  :background "rgba(0,0,0, 0.25)"
                  :padding "12px"
                  :border-radius "6px"}}
    [:div {:style {:position "relative"}}
      [:div {:style {:position "absolute"
                     :right    "0"
                     :display  "flex"
                     :z-index  1
                     :gap      "12px"
                     :justify-content "flex-end"}}
    
        [part-edit-button job-id part-props]
        [part-remove-button job-id part-id]]
   
      [:img {:src   (:picture_url part-props)
             :style {:width "48px" :filter "invert(1)"}}]]
    [:div {:key   (str "-" part-id)
           :style {:display               "grid"
                   :gap                   "3px 6px"
                   :grid-template-columns "auto 1fr"}}
      [part-name part-props]             
      [part-look part-props]
      [:div "Price:"][:b "$" price]] 
    [item-formdata part-props]])

;; ---- Part ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Grouped Job ----

(defn icon [{:keys [picture_url]}]
  [:img {:src   picture_url
         :style {:width "48px" :filter "invert(1)"}}])

(defn label [index {:keys [name precursor]
                    :strs [formdata]}]
  [:div {:style {:flex-grow 1}}
    [:p [:strong (str "#" (inc index))] " " precursor]
    [:b {:style {:font-size "1.2rem"}} name
      (when-let [qty (get-in formdata ["quantity" :qty])]
        [:span {:style {:font-size "14px"}} (str " (x " qty ")")])]])


(defn edit-button [job-id job-props]
  [link/view {:class    "cart-item--edit-button"
              :href     "/customize"
              :mode :clear
              :on-click #(do
                           (r/dispatch [:customizer/edit! job-id job-props]))}
    [:i {:class  "fa-solid fa-pen-to-square"}]])

(defn remove-button [job-id]
  [button/view {:class    "cart-item--remove-button"
                :mode :clear
                :on-click #(do (r/dispatch [:cart/remove-job! job-id]))}
    [:i {:class "fa-solid fa-trash"}]])

(defn job-controls [index job-id job-props]
  [:div {:class "cart-item--wrapper"}
    [icon job-props]
    [label index job-props]
    [:div {:class "cart-item--options"}
      [edit-button job-id job-props]
      [remove-button job-id]]])

(defn package-job [index job-id {:keys [color total parts] :as job-props}]
  [:div {:key   job-id
         :class "cart-item--container"
         :style {"--cart-item-clr" (parts->colors parts)
                 :font-size        "0.825rem"
                 :background       "rgba(0,0,0, 0.25)"}}
   [job-controls index job-id job-props]
   [:div {:style {:display        "flex"
                  :flex-direction "column"
                  :gap            "15px"
                  :margin-top     "15px"}}
     
     (map (fn [[part-id _ :as part-props]]
            ^{:key (str job-id "-" part-id)}
            [part job-id part-props])
          parts)
     [:p {:style {:text-align "right"}} "Total: " [:b "$" (reduce + (map :price (vals parts)))]]]])

;; ---- Grouped Job ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Job ----

(defn job [index job-id {:keys [look price parts] :as job-props}]
  (let [part-props (first (vals parts))]
    [:div {:key   job-id
           :class "cart-item--container"
           :style {"--cart-item-clr" (parts->colors parts)
                   :font-size        "0.825rem"
                   :background       "rgb(53 54 55)"}}
     [job-controls index job-id job-props]
     [:div {:style {:display        "flex"
                    :flex-direction "column"
                    :gap            "15px"}}
  
       [:div {:key   (str "-" job-id)
              :style {:display               "grid"
                      :gap                   "4px 6px"
                      :grid-template-columns "auto 1fr"}}
                   
        [part-look part-props]
        [:div "Price:"][:b "$" price]]
       [item-formdata part-props]]]))

;; ---- Job ----
;; -----------------------------------------------------------------------------

(defn cart-item-card [index [job-id {:keys [type parts] :as job-data}]]
  ;; (cond
  ;;   (and (= "package" type) (map? parts))    ^{:key job-id} [package-job index job-id job-data]
  ;;   (= "part" type)                          ^{:key job-id} [job index job-id job-data]
  ;;   :else
  ;;   ^{:key job-id} [job index job-id job-data]))
  (if (< 1 (count parts))
    ^{:key job-id}[package-job index job-id job-data]
    ^{:key job-id}[job index job-id job-data]))

(defn cart-items [cart-data]
  [:div {:id "checkout--cart-container"}
    [:p  "Cart"]
    [:div {:id    "checkout--cart-items"
           :class "hide-scrollbar"}
      (map-indexed cart-item-card cart-data)]])

;; ---- Components ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Subscriptions ----

(r/reg-sub
  :checkout.summary/valid?
  (fn [db [_]]
    true))

;; ---- Subscriptions ----
;; -----------------------------------------------------------------------------

(defn view [cart-data]
  [:div {:class "checkout--bg-box hide-scroll"}
    [cart-items cart-data]])