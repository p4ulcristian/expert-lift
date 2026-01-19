(ns ui.data-table.filters.bar
  "Filter bar component for data tables.
   Declarative filter bar that supports various filter types."
  (:require
   [ui.select :as select]))

;; =============================================================================
;; Data Table Filter Bar Component
;; =============================================================================
;;
;; Declarative filter bar for data tables.
;; Eliminates boilerplate for filter UI and state management.
;;
;; Usage:
;;   [filters-bar/view
;;    {:filters [{:id :process-id
;;                :type :select
;;                :label "Process"
;;                :icon "gear"
;;                :options [{:value "123" :label "Process A"}]
;;                :placeholder "All Processes"}]
;;     :active-filters {:process-id "123"}
;;     :on-change (fn [filter-id new-value] ...)}]

;; -----------------------------------------------------------------------------
;; Helpers
;; -----------------------------------------------------------------------------

(defn add-all-option
  "Add 'All' option to select filter options."
  [options filter-config]
  (let [all-label (:all-label filter-config (str "All " (:label filter-config "Items")))]
    (if (#{:select :multi-select} (:type filter-config))
      (vec (concat [{:value nil :label all-label}] options))
      (vec options))))

;; -----------------------------------------------------------------------------
;; Filter Renderers
;; -----------------------------------------------------------------------------

(defn render-select-filter
  "Render a single-select filter inline."
  [filter-config active-filters on-change]
  (let [filter-id        (:id filter-config)
        options          (:options filter-config [])
        active-value     (get active-filters filter-id)
        options-with-all (add-all-option options filter-config)
        selected         (when active-value
                           (first (filter #(= (:value %) active-value) options-with-all)))]

    [select/view
     {:id          (str "filter-" (name filter-id))
      :placeholder (:placeholder filter-config (:label filter-config))
      :options     options-with-all
      :value       selected
      :on-select   (fn [option]
                    (let [new-value (:value option)]
                     (on-change filter-id new-value)))
      :override    {:class "filter-select-control"
                    :style (:style filter-config)}}]))

(defn render-filter
  "Render a single filter based on its type."
  [filter-config active-filters on-change]
  (case (:type filter-config)
    :select [render-select-filter filter-config active-filters on-change]

    ;; Fallback for unsupported types
    [:div {:class "filter-unsupported"}
     (str "Unsupported filter type: " (:type filter-config))]))

;; -----------------------------------------------------------------------------
;; Display Modes
;; -----------------------------------------------------------------------------

(defn inline-filters
  "Render filters inline (horizontally)."
  [{:keys [filters active-filters on-change]}]
  [:div {:class "filters-bar-inline"}
   (for [filter-config filters]
     ^{:key (:id filter-config)}
     [render-filter filter-config active-filters on-change])])

;; -----------------------------------------------------------------------------
;; Main Component
;; -----------------------------------------------------------------------------

(defn view
  "Declarative filter bar component.

   Props:
     :filters               - Vector of filter configurations with :options already resolved
     :active-filters        - Map of active filter values {filter-id value}
     :on-change             - (fn [filter-id new-value]) called when filter changes
     :display-mode          - :inline (default) | :panel
     :container-class       - Optional CSS class for container (default: 'filters-bar-container')

   Filter Config:
     :id                    - Filter identifier (keyword)
     :type                  - Filter type (:select | :multi-select | :text | :date-range)
     :label                 - Display label
     :icon                  - Optional icon (emoji or font-awesome class)
     :placeholder           - Placeholder text
     :options               - Resolved options vector [{:value :label}]
     :all-label             - Label for 'All' option (default: 'All {label}')
     :on-load               - Optional callback to load options lazily
     :style                 - Additional styles for the filter control"
  [{:as config :keys [container-class]}]

  [:div {:class (or container-class "filters-bar-container")}
    ;; Filters UI
    [inline-filters config]])
