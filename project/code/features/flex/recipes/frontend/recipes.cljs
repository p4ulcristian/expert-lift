(ns features.flex.recipes.frontend.recipes
  (:require
   [re-frame.core :as r]
   [features.flex.shared.frontend.components.flex-title :as flex-title]
   [ui.button :as button]
   [ui.table.zero :as table]
   [router.frontend.zero :as router]
   [zero.frontend.react :as zero-react]
   [features.flex.recipes.frontend.request :as recipes-request]
   [features.flex.recipes.frontend.events]))

;; --- Table Column Renderers ---

(defn processes-cell [_item row]
  (let [processes (:recipe/processes row)]
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :gap "4px"}}
     (if (seq processes)
       (map-indexed 
        (fn [index process]
          ^{:key (:process/id process)}
          [:div {:style {:display "flex"
                        :align-items "center"
                        :gap "8px"}}
           [:div {:style {:width "20px"
                         :height "20px"
                         :border-radius "50%"
                         :border "2px solid #666"
                         :display "flex"
                         :align-items "center"
                         :justify-content "center"
                         :font-size "0.7em"
                         :font-weight "bold"
                         :color "#666"
                         :flex-shrink "0"}}
            (inc index)]
           [:span {:style {:font-size "0.85em"
                          :color "#666"}}
            (:process/name process)]])
        (sort-by :recipe-process/step-order processes))
       [:span {:style {:font-style "italic"
                      :color "#999"
                      :font-size "0.85em"}}
        "No processes"])]))

(defn actions-cell [wsid row]
  [button/view {:mode :outlined
                :color "var(--seco-clr)"
                :style {:padding "4px 12px" :fontSize "0.95em"}
                :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/recipes/" (:recipe/id row))})}
   "Edit"])

;; --- Table Component ---
(defn recipe-table [recipes wsid]
  [table/view
   {:rows recipes
    :grid-template-columns "1fr 1fr 1fr auto"
    :columns [:recipe/name :recipe/description :processes :actions]
    :labels {:recipe/name "Name"
             :recipe/description "Description"
             :processes "Processes"
             :actions "Actions"}
    :column-elements {:processes (fn [_item row] (processes-cell _item row))
                      :actions (fn [_item row] (actions-cell wsid row))}
    :row-element (fn [style content]
                   [:div {:style (merge style
                                       {:transition "all 0.2s ease"
                                        :cursor "pointer"})}
                    content])}])

;; --- Main View ---
(defn view []
  (let [wsid @(r/subscribe [:workspace/get-id])]
    (zero-react/use-effect 
     {:mount (fn [] 
               (r/dispatch [:flex/recipes wsid]))
      :params #js[wsid]})
    (let [recipes @(r/subscribe [:flex/recipes-get])]
      [:div
       [flex-title/view "Recipes"
        {:description "Ordered processes for powder coating jobs."
         :right-content [button/view {:mode :filled
                                      :color "var(--seco-clr)"
                                      :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/recipes/new")})}
                         "New Recipe"]}]
       [recipe-table recipes wsid]]))) 