(ns ui.data-table.core
  "Reusable data table component with server-side pagination, sorting, and actions.
   Wraps react-data-table-component for consistent table rendering across the app.

   Key feature: Handles namespaced keyword transformation automatically.
   Your code uses :process/name, :batch/id everywhere - this component converts
   to/from wire format (process__name, batch__id) at the react-data-table boundary."
  (:require
   ["react-data-table-component" :default DataTable]
   [ui.data-table.actions :as actions]
   [ui.data-table.icons :as icons]
   [utils.wire :as wire]
   [reagent.core :as reagent]))

;; -----------------------------------------------------------------------------
;; ---- Default Styles (no theming) ----

(def default-styles
  "Structural styles only - colors come from theme"
  {:headRow
   {:style {:minHeight "48px"}}
   :headCells
   {:style {:fontWeight "600"
            :fontSize   "13px"}}
   :rows
   {:style {:minHeight "48px"
            :padding "0.25rem 0"}}
   :cells
   {:style {:fontSize "14px"}}})

;; ---- Default Styles ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Wire Format Conversion ----

(defn- js-row->clj
  "Convert JS row object back to CLJS map with namespaced keys.
   Used for callbacks and cell functions."
  [^js js-row]
  (when js-row
    (-> (js->clj js-row :keywordize-keys true)
        wire/wire-format->keys)))

(defn- data->js
  "Convert CLJS data (vector of maps with namespaced keys) to JS array.
   {:process/name \"x\"} -> #js {\"process__name\" \"x\"}"
  [data]
  (when (seq data)
    (clj->js (mapv wire/keys->wire data))))

(defn- transform-selector
  "Transform column :selector from keyword to function.
   :process/name -> (fn [row] (aget row \"process__name\"))
   (fn [row] ...) -> wrapped to receive CLJS map

   Note: react-data-table-component v7+ requires function selectors."
  [selector]
  (cond
    (keyword? selector)
    (let [wire-key (wire/key->wire selector)]
      (fn [^js js-row]
        (aget js-row wire-key)))

    (fn? selector)
    (fn [^js js-row]
      (selector (js-row->clj js-row)))

    :else selector))

(defn- transform-cell
  "Wrap cell function to receive CLJS map with namespaced keys instead of JS object.
   (fn [row] [:span (:process/name row)]) works naturally."
  [cell-fn]
  (when cell-fn
    (fn [^js js-row]
      (let [row (js-row->clj js-row)]
        (reagent/as-element (cell-fn row))))))

(defn- transform-column
  "Transform a single column definition for react-data-table-component.
   - :selector keyword -> wire format string
   - :sortField keyword -> wire format string
   - :cell function -> wrapped to receive CLJS map"
  [col]
  (cond-> col
    ;; Transform :selector
    (:selector col)
    (update :selector transform-selector)

    ;; Transform :sortField
    (keyword? (:sortField col))
    (update :sortField wire/key->wire)

    ;; Wrap :cell function
    (:cell col)
    (update :cell transform-cell)))

(defn- transform-columns
  "Transform all column definitions."
  [columns]
  (mapv transform-column columns))

;; ---- Wire Format Conversion ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Defaults ----

(def PAGINATION-OPTIONS [5 10 15 20])

;; ---- Defaults ----
;; -----------------------------------------------------------------------------

(def loading-container-style
  {:display "flex"
   :flex-direction "column"
   :align-items "center"
   :justify-content "center"
   :padding "3rem 1.5rem"
   :min-height "200px"})

(def loading-icon-style
  {:font-size "2rem"
   :color "#3b82f6"
   :margin-bottom "1rem"})

(def loading-text-style
  {:font-size "0.9375rem"
   :color "#6b7280"
   :font-weight "500"})

(def no-data-container-style
  {:display "flex"
   :flex-direction "column"
   :align-items "center"
   :justify-content "center"
   :padding "3rem 1.5rem"
   :min-height "200px"})

(def no-data-icon-style
  {:font-size "2.5rem"
   :color "#d1d5db"
   :margin-bottom "1rem"})

(def no-data-text-style
  {:font-size "0.9375rem"
   :color "#9ca3af"
   :font-weight "500"})

(defn default-loading-component
  "Default loading spinner component"
  [entity-name-plural]
  (reagent/as-element
   [:div {:style loading-container-style}
    [:i {:class (str (icons/icon :loading) " fa-spin")
         :style loading-icon-style}]
    [:div {:style loading-text-style}
     (str "Loading " entity-name-plural "...")]]))

(defn default-no-data-component
  "Default no data message component"
  [entity-name-plural]
  (reagent/as-element
   [:div {:style no-data-container-style}
    [:i {:class "fa-solid fa-inbox"
         :style no-data-icon-style}]
    [:div {:style no-data-text-style}
     (str "No " entity-name-plural " found")]]))

(defn- wrap-row-callback
  "Wrap a callback function to convert JS row to CLJS map with namespaced keys."
  [callback]
  (when callback
    (fn [^js js-row & args]
      (apply callback (js-row->clj js-row) args))))

(defn actions-column
  "Standard actions column with edit/delete buttons.
   Callbacks receive CLJS maps with namespaced keys."
  [config]
  ;; No wrapping needed - cell function already converts js-row to CLJS map
  {:name   "Actions"
   :grow   1
   :style  {:gap "8px"}
   :cell   (fn [^js js-row]
             (let [row (js-row->clj js-row)]
               (reagent/as-element
                [actions/actions-cell row config])))})

(defn custom-actions-column
  "Custom actions column for non-standard actions (e.g., approve/deny).
   Custom actions component receives CLJS map with namespaced keys."
  [{:keys [custom-actions] :as config}]
  {:name  "Actions"
   :grow  1
   :style {:gap "8px"}
   :cell  (fn [^js js-row]
            (let [row (js-row->clj js-row)]
              (reagent/as-element
               [custom-actions row config])))})

(defn add-actions-column [columns config]
  (cond
    (:custom-actions config)
    (into [(custom-actions-column config)] columns)

    (or (:on-edit config) (:on-delete config))
    (into [(actions-column config)] columns)

    :else
    columns))

(defn data-table
  "Reusable data table component with server-side pagination, sorting, and actions.

  NAMESPACED KEYS: This component handles wire format conversion automatically.
  Use namespaced keywords everywhere (:process/name, :batch/id) - the component
  converts to/from wire format at the react-data-table boundary.

  Required:
  :columns        - Vector of column definitions. Use namespaced keywords:
                    {:name \"Name\"
                     :selector :process/name      ; keyword -> wire format
                     :sortField :process/name     ; keyword -> wire format
                     :cell (fn [row] [:span (:process/name row)])} ; receives CLJS map
  :data           - Vector of maps with namespaced keys [{:process/id 1 :process/name \"x\"}]
  :loading?       - Boolean, true when fetching data
  :pagination     - Map with pagination state: {:total-count N :page N :page-size N}
  :on-page-change - (fn [page totalRows]) called when pagination changes
  :on-sort        - (fn [sort-field sort-direction sortedRows]) called when sorting changes
                    sort-field is namespaced keyword (e.g., :process/name)
  :entity         - Map with entity config: {:name \"process\" :name-plural \"processes\"}
  :on-row-click   - (fn [row event]) called when a row is clicked, row is CLJS map

  Optional - Standard Actions (receive CLJS maps with namespaced keys):
  :on-edit             - (fn [entity]) called when edit button clicked
  :on-delete           - (fn [entity]) called when delete button clicked

  Optional - Custom Actions:
  :custom-actions      - (fn [row config]) Reagent component, row is CLJS map

  Optional - Expandable Rows:
  :expandable-rows           - Boolean, enable expandable rows
  :expandable-rows-component - (fn [row]) Reagent component, row is CLJS map

  Optional - Customization:
  :on-page-size-change - (fn [page-size]) called when rows per page changes
  :custom-styles       - Map of custom react-data-table-component styles to merge with defaults
  :loading-component   - Custom Reagent component for loading state
  :no-data-component   - Custom Reagent component for empty state

  Optional - Testing:
  :data-testid         - String, data-testid attribute for the table container"
  [{:as   config
    :keys [columns
           data
           loading?
           pagination
           on-page-change
           on-page-size-change
           on-sort
           on-row-click
           entity
           expandable-rows
           expandable-rows-component
           custom-styles
           data-testid]}]
  (let [;; Transform columns to wire format
        transformed-columns (transform-columns columns)
        ;; Add actions column (already handles wire conversion internally)
        -columns            (add-actions-column transformed-columns config)
        ;; Convert data to JS with wire format keys
        js-data             (or (data->js data) #js [])

        entity-name         (:name entity "item")
        entity-name-plural  (:name-plural entity (str entity-name "s"))
        merged-styles       (if custom-styles
                              (merge default-styles custom-styles)
                              default-styles)]

    [:div (cond-> {:class "data-table-container hide-scroll"}
            data-testid (assoc :data-testid data-testid))
     [:> DataTable
      (cond->
       {:columns      -columns
        :data         js-data
        :theme        "default"
        :customStyles merged-styles

        ;; Pagination
        :pagination                   true
        :paginationServer             true
        :paginationTotalRows          (:total-count pagination 0)
        :paginationPerPage            (:page-size pagination 20)
        :paginationRowsPerPageOptions PAGINATION-OPTIONS

        ;; Pagination handlers
        :onChangePage        (fn [page totalRows]
                               (on-page-change page totalRows))

        :onChangeRowsPerPage (fn [currentRowsPerPage currentPage]
                               (if on-page-size-change
                                 (on-page-size-change currentRowsPerPage)
                                 (on-page-change currentPage currentRowsPerPage)))

        ;; Sorting
        :sortServer true
        :onSort     (fn [^js selectedColumn sortDirection sortedRows]
                      (let [wire-sort-field (or (.-sortField selectedColumn) (.-id selectedColumn))
                            ;; Convert wire format back to namespaced keyword immediately
                            sort-field      (when wire-sort-field (wire/wire->key wire-sort-field))
                            sort-direction  (if (= sortDirection "desc") "desc" "asc")]
                        (when sort-field
                          (on-sort sort-field sort-direction sortedRows))))

        ;; UI options
        :fixedHeader             true
        :fixedHeaderScrollHeight "100%"
        :highlightOnHover        true
        :striped                 false
        :responsive              true
        :pointerOnHover          true
        :persistTableHead        true

        ;; Loading & no data states
        :progressPending   loading?
        :progressComponent (default-loading-component entity-name-plural)
        :noDataComponent   (default-no-data-component entity-name-plural)}

        ;; Row click handler - wrap to convert JS row to CLJS map
        on-row-click
        (assoc :onRowClicked (fn [^js js-row event]
                               (on-row-click (js-row->clj js-row) event)))

        ;; Expandable rows - wrap component to receive CLJS map
        expandable-rows
        (assoc :expandableRows true
               :expandableRowsComponent
               (fn [^js props]
                 (let [row-data (js-row->clj (.-data props))]
                   (reagent/as-element
                    [expandable-rows-component row-data])))))]]))

(defn view
  "Main entry point for data table component.
   See data-table for full documentation."
  [props]
  [data-table props])
