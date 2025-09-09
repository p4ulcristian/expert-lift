
(ns ui.accordion
  (:require
    ["react"      :as react]))

(defn- accordion-button [{:keys [title]} o? set-o]
  [:button {:on-click #(set-o not)
            :style    {:display         "flex"
                       :align-items     "center"
                       :justify-content "space-between"
                       :font-size       "1rem"
                       :padding         "12px"
                       :color           "inherit"}}
                       
    [:b {:style {:text-align "center"}} title]
    (if o? 
      [:i {:class ["fa-solid" "fa-caret-up"]}]
      [:i {:class ["fa-solid" "fa-caret-down"]}])])

(defn- accordion [{:keys [open? style] :as config} elements]
  (let [[o? set-o] (react/useState open?)]
     [:div {:style (merge {:display       "grid"
                           :border-radius "12px"
                           :background    "white"
                           :box-shadow    "var(--box-shadow-black-xx-light)"}
                          style)}
 
       [accordion-button config o? set-o]

       (when o?
         [:div {:style {:display "grid"
                        :padding "0 12px 12px"
                        :gap     "10px"}}
           (into [:<>] elements)])]))

(defn view [config & elements]
  [accordion config elements])