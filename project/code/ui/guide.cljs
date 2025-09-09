
(ns ui.guide
  (:require 
    ["react" :as react]))

(defn- guide-button [o? set-o]
  [:button {:on-click #(set-o not)
            :class    "effect--bg-opacity"
            :style    {:display       "flex"
                       :gap           "8px"
                       :color         "white !important"
                       :align-items   "center"
                       :padding       "6px"
                       :border-radius "6px"
                       :font-size     "1rem"
                       "--effect-clr" "hsl(0deg 0% 100% / 19%)"}}
    (if o? 
      [:i {:class ["fa-solid" "fa-caret-up"]}]
      [:i {:class ["fa-solid" "fa-caret-down"]}])
    [:b {:style {:text-align "center"}} "Guide"]])

(defn- guide [components]
  (let [[o? set-o] (react/useState false)]
    [:div {:style {:display       "grid"
                   :gap           "10px"
                   :padding       "6px"
                   :border-radius "6px" 
                   :background    "#ececec"}} 
      [guide-button o? set-o]
        
      (when o?
        [:div {:style {:display    "grid"
                       :gap        "10px"
                       :padding    "0 6px"}}
          (into [:<>] components)])]))   

(defn view [& comp]
  [guide comp])