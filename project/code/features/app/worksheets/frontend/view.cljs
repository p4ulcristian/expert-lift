(ns features.app.worksheets.frontend.view
  "Main view component for worksheets feature"
  (:require
   [reagent.core :as r]
   [features.app.worksheets.frontend.utils :as utils]
   [features.app.worksheets.frontend.queries :as queries]
   [features.app.worksheets.frontend.table :as table]
   [features.app.worksheets.frontend.components :as components]
   ;; Load events and subscriptions to register them
   [features.app.worksheets.frontend.events]
   [features.app.worksheets.frontend.subscriptions]
   [ui.data-table.core :as data-table]
   [ui.data-table.search :as data-table-search]
   [ui.enhanced-button :as enhanced-button]
   [ui.subheader :as subheader]
   [ui.content-section :as content-section]
   [translations.core :as tr]))

;; =============================================================================
;; Action Handlers
;; =============================================================================

(defn- handle-edit
  "Handle edit worksheet action"
  [worksheet modal-worksheet modal-is-new?]
  (reset! modal-worksheet worksheet)
  (reset! modal-is-new? false))

(defn- handle-pdf
  "Handle PDF view action"
  [worksheet]
  (let [pdf-url (str "/pdf-generator/worksheet/" (:worksheet/id worksheet))]
    (js/window.open pdf-url "_blank")))

(defn- handle-delete
  "Handle delete worksheet action"
  [worksheet workspace-id load-worksheets-fn]
  (when (js/confirm (tr/tr :worksheets/confirm-delete))
    (queries/delete-worksheet (:worksheet/id worksheet) workspace-id load-worksheets-fn)))

(defn- handle-add-new
  "Handle add new worksheet action"
  [modal-worksheet modal-is-new?]
  (reset! modal-worksheet {:worksheet/status "draft"
                           :worksheet/work-type ""
                           :worksheet/service-type ""
                           :worksheet/creation-date (.toISOString (js/Date.))})
  (reset! modal-is-new? true))

;; =============================================================================
;; Main View Component
;; =============================================================================

(defn view
  "Main worksheets page view"
  []
  (let [workspace-id (utils/get-workspace-id)
        ;; Data state
        worksheets (r/atom [])
        pagination (r/atom {:total-count 0 :page 0 :page-size 10})
        loading? (r/atom false)
        ;; Search and sort state
        search-term (r/atom "")
        sort-field (r/atom :worksheet/serial-number)
        sort-direction (r/atom "desc")
        ;; Modal state
        modal-worksheet (r/atom nil)
        modal-is-new? (r/atom false)

        ;; Load worksheets with current params
        load-worksheets (fn []
                          (queries/load-worksheets
                           workspace-id
                           {:search @search-term
                            :sort-by @sort-field
                            :sort-direction @sort-direction
                            :page (:page @pagination)
                            :page-size (:page-size @pagination)}
                           worksheets
                           pagination
                           loading?))

        save-worksheet (fn [worksheet callback]
                         (queries/save-worksheet worksheet workspace-id modal-is-new? callback
                                                 modal-worksheet load-worksheets))

        on-edit (fn [worksheet]
                  (handle-edit worksheet modal-worksheet modal-is-new?))

        on-pdf handle-pdf

        on-delete (fn [worksheet]
                    (handle-delete worksheet workspace-id load-worksheets))]

    (fn []
      ;; Load initial data
      (when (and (empty? @worksheets) (not @loading?))
        (load-worksheets))

      [:<>
       ;; Subheader with add button
       [subheader/subheader
        {:title (tr/tr :worksheets/page-title)
         :description (tr/tr :worksheets/page-description)
         :action-button [enhanced-button/enhanced-button
                         {:variant :success
                          :data-testid "add-worksheet-button"
                          :on-click #(handle-add-new modal-worksheet modal-is-new?)
                          :text (tr/tr :worksheets/add-new-worksheet)}]}]

       [content-section/content-section
        ;; Search bar
        [:div {:style {:margin-bottom "1rem"}}
        [data-table-search/view
         {:search-term @search-term
          :placeholder (tr/tr :worksheets/search-placeholder)
          :on-search-change (fn [value]
                              (reset! search-term value))
          :on-search (fn [value]
                       (reset! search-term value)
                       (swap! pagination assoc :page 0)
                       (load-worksheets))}]]

       ;; Worksheets table
       [data-table/view
        {:columns (table/get-columns)
         :data @worksheets
         :loading? @loading?
         :pagination @pagination
         :entity {:name "worksheet" :name-plural "worksheets"}
         :data-testid "worksheets-table"
         ;; Custom actions (edit, pdf, delete)
         :custom-actions (fn [row _config]
                           [table/actions-cell row {:on-edit on-edit
                                                    :on-pdf on-pdf
                                                    :on-delete on-delete}])
         ;; Pagination handler
         :on-page-change (fn [page _total-rows]
                           (swap! pagination assoc :page (dec page))
                           (load-worksheets))
         :on-page-size-change (fn [new-size]
                                (swap! pagination assoc :page-size new-size :page 0)
                                (load-worksheets))
         ;; Sort handler
         :on-sort (fn [field direction _sorted-rows]
                    (reset! sort-field field)
                    (reset! sort-direction direction)
                    (load-worksheets))}]

       ;; Modal when open
       (when @modal-worksheet
         [components/worksheet-modal
          @modal-worksheet
          @modal-is-new?
          save-worksheet
          (fn [] (reset! modal-worksheet nil))
          workspace-id])]])))
