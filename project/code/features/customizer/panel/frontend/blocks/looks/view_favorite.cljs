
(ns features.customizer.panel.frontend.blocks.looks.view-favorite
  (:require
    [re-frame.core       :as r]
    ["react"             :as react]
    ["keen-slider/react" :as keen]
   
    [features.customizer.panel.frontend.blocks.looks.utils :as utils]))

;; -----------------------------------------------------------------------------
;; ---- Configs ----

(defn SLIDER_CONFIG [collection]
  #js{:loop   true
      :mode   "free-snap"
      :slides #js{"perView" (if (< (count collection) 4) 3 "auto")
                  "origin"  (if (< (count collection) 4) "left" "center")
                  "spacing" 15}})

;; ---- Configs ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Hooks ----

(defn- refresh-slider! [^js slider-props collection]
  (react/useEffect
   (fn []
     (when slider-props
       (utils/update! (.-current slider-props)))
     (fn []))
   #js[collection]))

;; ---- Hooks ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(r/reg-event-fx
 ::pin!
 (fn [{:keys [db]} [_ pinned? {:keys [id] :as look-data}]]
   (if pinned?
     {:dispatch [:db/assoc-in [:pinned/looks] (vec (remove #(= (:id %) id) (get-in db [:pinned/looks])))]}
     {:dispatch-n [[:db/assoc-in [:pinned.menu/index] nil]
                   [:db/update-in [:pinned/looks] (fn [a] (conj (vec a) look-data))]]})))

(defn look-card [[_item-index _slider-props _collection] {:keys [thumbnail] :as item-props}]
  [:button {:class    "dock-card"
            :on-click        #(do 
                                (r/dispatch [:looks/force-move!]) 
                                (r/dispatch [:looks/select! item-props]))
            :on-double-click #(r/dispatch [::pin! true item-props])}
    [:img {:src   thumbnail
           :class "__img"}]])

(defn- slider [collection]
  (let [[slider-ref instance-ref] (keen/useKeenSlider (SLIDER_CONFIG collection)
                                                      #js[utils/wheel-controls])]
    (refresh-slider! instance-ref collection)

    [:div {:class "keen-slider"
           :ref   slider-ref}
      (map-indexed (fn [item-index item-props]
                     [:div {:key   (str "customizer-look-pinned-" item-index)
                            :class "keen-slider__slide"}
                       [look-card [item-index collection] item-props]])
                   collection)]))

(defn- pinned-looks []
  (let [data @(r/subscribe [:db/get-in [:pinned/looks]])]
    [:div {:id    "pinned-looks"
           :class "dock--outer"}
      [:div {:class "dock--inner"}
        (if (seq data)
          [slider data]
          [:div {:style {:display "flex"
                         :align-items "center"
                         :justify-content "center"
                         :color "rgba(255, 255, 255, 0.6)"
                         :font-size "0.9rem"
                         :white-space "nowrap"
                         :gap "6px"}}
           "Click " [:i {:class "fa-regular fa-star"}] " to add favorite"])]]))

(defn view []
  [pinned-looks])
