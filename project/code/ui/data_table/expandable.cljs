(ns ui.data-table.expandable
  "Shared expandable row components for data tables.
   Provides consistent layout for detail information shown when a row is expanded.")

(defn detail-row
  "Single detail row with label and value.
   Returns nil if value is nil/empty (no empty rows rendered)."
  [label value]
  (when (and value (not= value ""))
    [:div {:class "ui-expandable-detail-row"}
     [:span {:class "ui-expandable-detail-label"} label]
     [:span {:class "ui-expandable-detail-value"} value]]))

(defn detail-section
  "Section with title and child content.
   Use for grouping related details."
  [title & children]
  [:div {:class "ui-expandable-section"}
   [:h4 {:class "ui-expandable-section-title"} title]
   (into [:div {:class "ui-expandable-section-content"}]
         children)])

(defn expanded-content
  "Container for expanded row content.
   Uses a responsive grid layout for sections."
  [& children]
  [:div {:class "ui-expandable-content"}
   (into [:div {:class "ui-expandable-content-grid"}]
         children)])

(defn link-value
  "Render a value as a clickable link."
  [href text]
  [:a {:href href
       :class "ui-expandable-link"}
   text])
