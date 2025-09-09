
(ns ui.table)

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn table-layout [{:keys [layout columns]}]
   (cond 
     (string? layout) layout
     (= layout :fit-content) (str "repeat(" (count columns) ", fit-content(100%))")
     :else (str "repeat(" (count columns) ", 1fr) ")))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn table [{:keys [style override layout columns data sticky-header?] :as table-props} column row]
  [:div (merge {:class       "table"
                :data-layout layout
                :style       (merge {:grid-template-columns (table-layout table-props)}
                                    style)}
               override)

    [:div {:class "table-header" :data-sticky sticky-header?}
      (map (fn [{:keys [id label] :as column-props}]
             ^{:key (or id label)}
             [column column-props table-props])
           columns)]

    (when data
      (map-indexed (fn [index row-props]
                     ^{:key (str (:id table-props) (:id row-props index))}
                     [row index row-props table-props])
                   data))])

(defn view [props column row]
  [table props column row])