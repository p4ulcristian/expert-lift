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
  "Handle PDF view action - opens in modal"
  [worksheet pdf-modal-worksheet]
  (reset! pdf-modal-worksheet worksheet))

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
;; PDF Modal Component
;; =============================================================================

(defn- pdf-modal
  "Modal component for viewing PDF in iframe"
  [worksheet on-close]
  (let [pdf-url (str "/pdf-generator/worksheet/" (:worksheet/id worksheet) "/raw")]
    [:div {:style {:position "fixed" :top 0 :left 0 :right 0 :bottom 0
                   :background "rgba(0, 0, 0, 0.8)" :z-index 1000
                   :display "flex" :flex-direction "column"
                   :animation "fadeIn 0.2s ease-out"}}
     ;; Header bar
     [:div {:style {:display "flex" :justify-content "space-between" :align-items "center"
                    :padding "0.75rem 1rem" :background "#1f2937" :color "white"}}
      [:span {:style {:font-weight "600"}}
       (str (tr/tr :worksheets/worksheet) " - " (:worksheet/serial-number worksheet))]
      [:div {:style {:display "flex" :gap "0.5rem"}}
       ;; Fullscreen / new tab button
       [:button {:style {:background "#374151" :border "none" :color "white"
                         :padding "0.5rem 1rem" :border-radius "0.375rem"
                         :cursor "pointer" :display "flex" :align-items "center" :gap "0.5rem"}
                 :on-click #(js/window.open pdf-url "_blank")}
        [:i {:class "fa-solid fa-up-right-from-square"}]
        (tr/tr :common/open-in-new-tab)]
       ;; Close button
       [:button {:style {:background "#dc2626" :border "none" :color "white"
                         :padding "0.5rem 1rem" :border-radius "0.375rem"
                         :cursor "pointer" :display "flex" :align-items "center" :gap "0.5rem"}
                 :on-click on-close}
        [:i {:class "fa-solid fa-xmark"}]
        (tr/tr :common/close)]]]
     ;; PDF iframe
     [:iframe {:src pdf-url
               :style {:flex 1 :border "none" :width "100%" :height "100%"}}]]))

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
        ;; PDF modal state
        pdf-modal-worksheet (r/atom nil)

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

        on-pdf (fn [worksheet]
                 (handle-pdf worksheet pdf-modal-worksheet))

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

       ;; Worksheet edit modal
       (when @modal-worksheet
         [components/worksheet-modal
          @modal-worksheet
          @modal-is-new?
          save-worksheet
          (fn [] (reset! modal-worksheet nil))
          workspace-id])

       ;; PDF viewer modal
       (when @pdf-modal-worksheet
         [pdf-modal
          @pdf-modal-worksheet
          (fn [] (reset! pdf-modal-worksheet nil))])]])))
