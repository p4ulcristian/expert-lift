
(ns features.customizer.panel.frontend.blocks.menu.cards
  (:require
    [re-frame.core :as r]
    ["react" :as react]))

(defn- thumbnail [{:keys [picture_url]}]
  (let [[loaded? set-loaded] (react/useState false)]
    [:div {:style {:width "55px" 
                   :height "55px"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :position "relative"}}
     (when-not loaded?
       [:div {:style {:position "absolute"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :width "55px"
                      :height "55px"}}
        [:i {:class "fa-solid fa-spinner"
             :style {:font-size "24px"
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
  [:span {:class "customizer--category-card--label"}
    name])

(defn- card [{:keys [id name] :as category-props} selected-category]
  (let [selected? (and (not= id nil) (= id (:id selected-category)))]
    [:button {:class         "customizer--category-card"
              :data-selected selected?
              :on-click      #(when-not selected?
                                (r/dispatch [:customizer.menu.card/click! category-props]))}
     
      [thumbnail category-props]
      [label name]]))

(defn render-cards []
  (let [history    @(r/subscribe [:customizer.menu/path])
        selected   @(r/subscribe [:db/get-in [:customizer/menu :selected]])]

    [:<>
      (map
        (fn [[id {:keys [name] :as category-props}]]
          ^{:key id}
          [card category-props selected])
        (get-in @(r/subscribe [:db/get-in [:customizer/menu :items]])
                history))]))

(defn view []
  [:div {:id    "customizer--category-body"
         :class "hide-scroll"}
    [:div {:id "customizer--category-cards"} 
      [render-cards]]])