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
   [ui.data-table :as data-table]
   [ui.enhanced-button :as enhanced-button]
   [ui.page-header :as page-header]
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
        worksheets-data (r/atom [])
        loading? (r/atom false)
        modal-worksheet (r/atom nil)
        modal-is-new? (r/atom false)

        load-worksheets (fn [params]
                          (queries/load-worksheets workspace-id params worksheets-data loading?))

        save-worksheet (fn [worksheet callback]
                         (queries/save-worksheet worksheet workspace-id modal-is-new? callback
                                                 modal-worksheet load-worksheets))

        on-edit (fn [worksheet]
                  (handle-edit worksheet modal-worksheet modal-is-new?))

        on-pdf handle-pdf

        on-delete (fn [worksheet]
                    (handle-delete worksheet workspace-id (fn [] (load-worksheets {}))))]

    (fn []
      ;; Load initial data
      (when (empty? @worksheets-data)
        (load-worksheets {}))

      [:div {:style {:min-height "100vh" :background "#f9fafb"}}
       [:div {:style {:max-width "1200px" :margin "0 auto" :padding "2rem"}}
        ;; Page header with add button
        [page-header/page-header
         {:title (tr/tr :worksheets/page-title)
          :description (tr/tr :worksheets/page-description)
          :action-button [enhanced-button/enhanced-button
                          {:variant :success
                           :on-click #(handle-add-new modal-worksheet modal-is-new?)
                           :text (tr/tr :worksheets/add-new-worksheet)}]}]

        ;; Worksheets table
        [data-table/server-side-data-table
         {:headers (table/get-columns)
          :data-source @worksheets-data
          :loading? @loading?
          :empty-message (tr/tr :worksheets/no-worksheets-found)
          :id-key :worksheet/id
          :table-id :worksheets-table
          :show-search? true
          :show-pagination? true
          :query-fn load-worksheets
          :actions (table/get-actions on-edit on-pdf on-delete)}]

        ;; Modal when open
        (when @modal-worksheet
          [components/worksheet-modal
           @modal-worksheet
           @modal-is-new?
           save-worksheet
           (fn [] (reset! modal-worksheet nil))
           workspace-id])]])))
