
(ns features.customizer.panel.frontend.blocks.menu.populars
  (:require
    [re-frame.core :as r]
    ["react" :as react]
    [features.customizer.panel.frontend.blocks.looks.utils :as utils]
    ["keen-slider/react" :as keen]))

;; -----------------------------------------------------------------------------
;; ---- Configurations ----

(def MAX-SLIDES 4)

(defn SLIDER-CONFIG [collection]
  #js{:loop   true
      :mode   "free-snap"
      :slides #js{"perView" (if (< (count collection) 4) 3 "auto")
                  "origin"  (if (< (count collection) 4) "left" "center")
                  "spacing" 15}})

;; ---- Configurations ----
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

(defn- header []
  [:div {:style {:text-align "center"
                 :margin     "20px 0 15px 0"
                 :padding    "15px 0 0"
                 :border-top "1px solid rgba(255, 255, 255, 0.15)"}}
    [:h3 {:style {:font-size "18px" 
                  :font-weight "600"
                  :color "white"
                  :margin "0"}}
      "Popular Items"]])

(defn- thumbnail [{:keys [picture_url]}]
  (let [[loaded? set-loaded] (react/useState false)]
    [:div {:style {:width "40px" 
                   :height "40px"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :position "relative"}}
     (when-not loaded?
       [:div {:style {:position "absolute"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :width "40px"
                      :height "40px"}}
        [:i {:class "fa-solid fa-spinner"
             :style {:font-size "18px"
                     :color "rgba(255, 255, 255, 0.5)"
                     :animation "spin 1s linear infinite"}}]])
     (when-not (empty? picture_url)
       [:img {:class   "customizer--category-card--img"
              :src     picture_url
              :style   {:opacity (if loaded? 1 0)
                        :transition "opacity 0.2s ease"}
              :onLoad  #(set-loaded true)
              :onError (fn [this]
                         (.remove (-> this .-target)))}])]))

(defn- label [name]
  [:span {:class "customizer--category-card--label"
          :style {:font-size "11px"
                  :line-height "1.2"
                  :white-space "nowrap"
                  :overflow "hidden"
                  :text-overflow "ellipsis"
                  :width "100%"
                  :display "block"}}
    name])

(defn- popular-item-card [{:keys [id picture_url name] :as popular-data}]
  [:button {:class    "customizer--category-card"
            :style    {:width "60px"
                       :padding "10px 8px"}
            :on-click #(r/dispatch [:customizer.menu.popular/click! popular-data])}
    [thumbnail popular-data]
    [label name]])

(defn- slider [collection]
  (let [[slider-ref instance-ref] (keen/useKeenSlider (SLIDER-CONFIG collection)
                                                      #js[utils/wheel-controls])]
    (refresh-slider! instance-ref collection)

    [:div {:class "keen-slider"
           :ref   slider-ref}
      (map-indexed (fn [item-index item-props]
                     [:div {:key   (str "customizer-popular-" item-index)
                            :class "keen-slider__slide"}
                       [popular-item-card item-props]])
                   collection)]))

(defn- popular-items [popular-items-data]
  [:div {:id    "popular-items"
         :class "dock--outer"}
    [header]
    [:div {:class "dock--inner"}
      (when (seq popular-items-data)
        [slider popular-items-data])]])

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view []
  (when (empty? @(r/subscribe [:db/get-in [:customizer/menu :path]]))
    [popular-items @(r/subscribe [:db/get-in [:customizer/menu :populars]])]))