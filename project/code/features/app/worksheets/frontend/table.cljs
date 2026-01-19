(ns features.app.worksheets.frontend.table
  "Table column renderers and configuration for worksheets"
  (:require
   [clojure.string :as str]
   [features.app.worksheets.frontend.utils :as utils]
   [translations.core :as tr]))

;; =============================================================================
;; Column Renderers (now receive full row, not just value)
;; =============================================================================

(defn serial-cell
  "Render worksheet serial number column"
  [row]
  [:div {:style {:font-weight "600"
                 :color "#111827"
                 :font-size "0.875rem"}}
   (:worksheet/serial-number row)])

(defn work-type-cell
  "Render work type column with service type subtitle"
  [row]
  [:div
   [:div {:style {:font-weight "500"
                  :color "#111827"
                  :font-size "0.875rem"
                  :text-transform "capitalize"}}
    (:worksheet/work-type row)]
   [:div {:style {:color "#6b7280"
                  :font-size "0.75rem"
                  :margin-top "0.25rem"
                  :text-transform "capitalize"}}
    (str "Service: " (:worksheet/service-type row))]])

(defn status-cell
  "Render status column with colored badges"
  [row]
  (let [status (:worksheet/status row)
        colors (get utils/status-colors status utils/default-status-color)]
    [:span {:style {:display "inline-block"
                    :padding "0.25rem 0.75rem"
                    :background (:bg colors)
                    :color (:color colors)
                    :border-radius "12px"
                    :font-size "0.75rem"
                    :font-weight "500"
                    :text-transform "capitalize"}}
     (str/replace (or status "") "_" " ")]))

(defn address-cell
  "Render address column with city subtitle"
  [row]
  [:div
   [:div {:style {:font-weight "500"
                  :color "#111827"
                  :font-size "0.875rem"}}
    (:worksheet/address-name row)]
   (when (:worksheet/address-city row)
     [:div {:style {:color "#6b7280"
                    :font-size "0.75rem"
                    :margin-top "0.25rem"}}
      (:worksheet/address-city row)])])

(defn assigned-to-cell
  "Render assigned to column"
  [row]
  (let [assigned-to-name (:worksheet/assigned-to-name row)]
    [:div
     (if assigned-to-name
       [:div {:style {:color "#374151"
                      :font-size "0.875rem"
                      :font-weight "500"}}
        assigned-to-name]
       [:span {:style {:color "#9ca3af"
                       :font-style "italic"
                       :font-size "0.75rem"}}
        "Unassigned"])]))

(defn elevator-cell
  "Render elevator identifier column"
  [row]
  (let [elevator-id (:worksheet/elevator-identifier row)]
    [:div
     (if elevator-id
       [:div {:style {:color "#374151"
                      :font-size "0.875rem"
                      :font-weight "500"}}
        elevator-id]
       [:span {:style {:color "#9ca3af"
                       :font-style "italic"
                       :font-size "0.75rem"}}
        "-"])]))

;; =============================================================================
;; Table Configuration for react-data-table-component
;; =============================================================================

(defn get-columns
  "Get column configuration for worksheets table (react-data-table format)"
  []
  [{:name      (tr/tr :worksheets/table-header-serial)
    :selector  :worksheet/serial-number
    :sortField :worksheet/serial-number
    :sortable  true
    :cell      serial-cell
    :width     "140px"}
   {:name      (tr/tr :worksheets/table-header-work-type)
    :selector  :worksheet/work-type
    :sortField :worksheet/work-type
    :sortable  true
    :cell      work-type-cell
    :width     "160px"}
   {:name      (tr/tr :worksheets/table-header-address)
    :selector  :worksheet/address-name
    :sortField :worksheet/address-name
    :sortable  true
    :cell      address-cell
    :width     "200px"}
   {:name      "Felvonó"
    :selector  :worksheet/elevator-identifier
    :sortable  false
    :cell      elevator-cell
    :width     "100px"}
   {:name      (tr/tr :worksheets/table-header-assigned-to)
    :selector  :worksheet/assigned-to-name
    :sortField :worksheet/assigned-to-name
    :sortable  true
    :cell      assigned-to-cell
    :width     "150px"}])

;; =============================================================================
;; Custom Actions Column (PDF button is custom, not standard edit/delete)
;; =============================================================================

(defn actions-cell
  "Custom actions cell with edit, PDF, and delete buttons"
  [row {:keys [on-edit on-pdf on-delete]}]
  (let [worksheet-id (:worksheet/id row)]
    [:div {:style {:display "flex" :gap "8px"}}
     ;; Edit button
     [:button {:class       "input-button button-outlined input-primary"
               :title       (tr/tr :worksheets/action-edit)
               :data-testid (str "edit-worksheet-" worksheet-id)
               :style       {:padding "4px 8px"}
               :on-click    #(on-edit row)}
      [:i {:class "fa-solid fa-pen"}]]
     ;; PDF button
     [:button {:class       "input-button button-outlined input-secondary"
               :title       (tr/tr :worksheets/action-pdf)
               :data-testid (str "pdf-worksheet-" worksheet-id)
               :style       {:padding "4px 8px"}
               :on-click    #(on-pdf row)}
      [:i {:class "fa-solid fa-file-pdf"}]]
     ;; Delete button
     [:button {:class       "input-button button-outlined input-warning"
               :title       (tr/tr :worksheets/action-delete)
               :data-testid (str "delete-worksheet-" worksheet-id)
               :style       {:padding "4px 8px"}
               :on-click    #(on-delete row)}
      [:i {:class "fa-solid fa-trash"}]]]))
