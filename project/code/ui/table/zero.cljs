(ns ui.table.zero)


(defn cell-ui  [row-data cell-key column-elements]
  (let [column-element (get column-elements cell-key)
        cell-value (get row-data cell-key)]
    [:div
     {:style {:padding "10px"
              :border-top "1px solid lightgrey"
              :white-space "nowrap"
              :overflow "hidden"
              :text-overflow "ellipsis" 
              :min-width 0
              :height "100%"
              :display :flex 
              :align-items :center}}
     (if column-element
       [column-element cell-value row-data]
       (str cell-value))]))


(defn row-ui [{:keys [row-data columns column-elements 
                      row-element]}] 
  (let [row-content (map (fn [cell-key] 
                          ^{:key (str "cell-" (hash row-data) "-" (name cell-key))}
                          [cell-ui row-data cell-key column-elements])
                         columns)]
    
    ;; [:div
    ;;  {:style {:display :grid
    ;;           :grid-template-columns "subgrid"
    ;;           :grid-column "1 / -1"}}]
    [row-element  
     {:display :grid
      :grid-template-columns "subgrid"
      :grid-column "1 / -1"}
     row-content]
    
    ))

(defn body [{:keys [rows row-element columns column-elements]}]
  [:<>
   (map (fn [row-data] 
          ^{:key (str "row-" (hash row-data))}
          [row-ui {:row-data row-data 
                   :columns columns 
                   :column-elements column-elements 
                   :row-element row-element}])
        rows)])

(defn header [{:keys [columns labels]}]
  [:div {:style {:display :grid
                 :grid-template-columns "subgrid"
                 :grid-column "1 / -1"}}
   (map
    (fn [cell-key]
      ^{:key (str "header-" (name cell-key))}
      [:div {:style {:padding "10px"
                     :font-size "12px"
                     :color "lightgrey"}}
       (if 
        (contains? labels cell-key)
         (get labels cell-key)
         (name cell-key))])
    columns)])

(defn footer []
  [:div {:style {:display :flex
                 :justify-content :flex-end
                 :grid-column "span 6"}}
   ;; Icon next and previous
   [:i {:style {:padding "10px"
                :cursor "pointer"}
        :class "fa-solid fa-chevron-left"}]
   [:div {:style {:padding "10px"}} "1/10"]
   [:i {:style {:padding "10px"
                :cursor "pointer"}
        :class "fa-solid fa-chevron-right"}]])


(defn view [{:keys [rows row-element 
                    columns column-elements
                    grid-template-columns labels ]}]
  (let [column-count (count columns)] 
    [:div
     {:style {:background-color "white"
              :border-radius "10px"
              :display :grid 
              :grid-template-columns (if grid-template-columns 
                                       grid-template-columns
                                       (str "repeat(" column-count ",1fr)"))}}
     [header {:columns columns
              :labels labels}]
     [body {:rows rows 
            :columns columns 
            :column-elements column-elements  
            :row-element row-element}]
     ;[footer]
     ]))

(defn table [{:keys [style override layout columns data] :as _props}]
  [:div (merge {:class       "table"
                :data-layout layout
                :style       (merge {:grid-template-columns (str "repeat(" (count columns) ", 1fr)")}
                                   style)}
               override)

    [:div {:class "table-header"}
      (map (fn [col]
             (let [{:keys [id label]} col]
               ^{:key (or id label)}
               [:div {:class "table-cell"} 
                (or label "")]))
           (filter map? columns))]

    (when (seq data)
      (map-indexed (fn [index row]
                     ^{:key index}
                     [:div {:class "table-row"}
                      (map (fn [col]
                             (let [{:keys [id]} col]
                               ^{:key id}
                               [:div {:class "table-cell"} 
                                (get row id)]))
                           (filter map? columns))])
                   data))])

