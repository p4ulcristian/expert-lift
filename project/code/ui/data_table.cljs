(ns ui.data-table)

(defn- table-header-style []
  "Table header cell styling"
  {:padding "1rem 1.25rem" :text-align "left" :font-weight "600" 
   :font-size "0.75rem" :letter-spacing "0.05em" :text-transform "uppercase"
   :color "#374151" :background "#f9fafb" :border-bottom "1px solid #e5e7eb"})

(defn- table-cell-style []
  "Table body cell styling"
  {:padding "1rem 1.25rem" :border-bottom "1px solid #f3f4f6" :vertical-align "top"})

(defn- action-button [text on-click variant]
  "Styled action button for table rows"
  (let [variant-styles (case variant
                         :primary {:background "#f3f4f6" :color "#374151" 
                                   :border "1px solid #d1d5db"
                                   :hover {:background "#e5e7eb" :border-color "#9ca3af"}}
                         :danger {:background "#fef2f2" :color "#dc2626"
                                  :border "1px solid #fecaca"
                                  :hover {:background "#fee2e2" :border-color "#fca5a5"}}
                         {:background "#f3f4f6" :color "#374151" 
                          :border "1px solid #d1d5db"
                          :hover {:background "#e5e7eb" :border-color "#9ca3af"}})]
    [:button {:type "button"
              :on-click on-click
              :style (merge {:padding "0.5rem 0.75rem" :border-radius "6px" :cursor "pointer"
                            :font-size "0.75rem" :font-weight "500" :transition "all 0.15s ease-in-out"}
                           variant-styles)}
     text]))

(defn status-badge
  "Status badge component for displaying active/inactive states"
  [active? & {:keys [active-text inactive-text]}]
  [:span {:style {:padding "0.25rem 0.75rem" :border-radius "9999px" :font-size "0.75rem" :font-weight "500"
                  :background (if active? "#dcfce7" "#fee2e2")
                  :color (if active? "#166534" "#dc2626")}}
   (if active? 
     (or active-text "Active") 
     (or inactive-text "Inactive"))])

(defn data-table
  "Modern styled data table with card appearance"
  [{:keys [headers rows loading? empty-message actions]}]
  (cond
    loading?
    [:div {:style {:display "flex" :justify-content "center" :align-items "center" 
                   :padding "4rem" :background "white" :border-radius "12px"
                   :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
     [:div {:style {:text-align "center"}}
      [:div {:style {:width "40px" :height "40px" :border "4px solid #f3f4f6" 
                     :border-top "4px solid #3b82f6" :border-radius "50%"
                     :animation "spin 1s linear infinite" :margin "0 auto 1rem"}}]
      [:div {:style {:color "#6b7280" :font-weight "500"}} "Loading..."]]]

    (empty? rows)
    [:div {:style {:text-align "center" :padding "4rem" :background "white" 
                   :border-radius "12px" :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
     [:div {:style {:color "#9ca3af" :font-size "1.125rem" :font-weight "500"}}
      (or empty-message "No data found")]
     [:div {:style {:color "#6b7280" :font-size "0.875rem" :margin-top "0.5rem"}}
      "There are no items to display"]]

    :else
    [:div {:style {:background "white" :border-radius "12px" :overflow "hidden"
                   :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)"
                   :border "1px solid #e5e7eb"}}
     [:table {:style {:width "100%" :border-collapse "collapse"}}
      [:thead
       [:tr
        (for [header headers]
          ^{:key (:key header)}
          [:th {:style (merge (table-header-style) (:style header))} (:label header)])]]
      [:tbody
       (for [row rows]
         ^{:key (:id row)}
         [:tr {:style {:transition "background-color 0.15s ease-in-out"
                       :hover {:background "#f9fafb"}}}
          (for [header headers]
            ^{:key (str (:id row) "-" (:key header))}
            [:td {:style (merge (table-cell-style) (:cell-style header))}
             (if (:render header)
               ((:render header) (get row (:key header)) row)
               (str (get row (:key header))))])
          (when actions
            [:td {:style (merge (table-cell-style) {:text-align "center"})}
             [:div {:style {:display "flex" :gap "0.5rem" :justify-content "center"}}
              (for [action actions]
                ^{:key (:key action)}
                [action-button (:label action) 
                 #((:on-click action) row)
                 (:variant action)])]])])]]]))

