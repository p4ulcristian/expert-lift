(ns features.app.worksheets.frontend.table
  "Table column renderers and configuration for worksheets"
  (:require
   [clojure.string :as str]
   [features.app.worksheets.frontend.utils :as utils]
   [translations.core :as tr]))

;; =============================================================================
;; Column Renderers
;; =============================================================================

(defn serial-render
  "Render worksheet serial number column"
  [serial-number _row]
  [:div {:style {:font-weight "600"
                 :color "#111827"
                 :font-size "0.875rem"}}
   serial-number])

(defn work-type-render
  "Render work type column with service type subtitle"
  [work-type row]
  [:div
   [:div {:style {:font-weight "500"
                  :color "#111827"
                  :font-size "0.875rem"
                  :text-transform "capitalize"}}
    work-type]
   [:div {:style {:color "#6b7280"
                  :font-size "0.75rem"
                  :margin-top "0.25rem"
                  :text-transform "capitalize"}}
    (str "Service: " (:worksheet/service-type row))]])

(defn status-render
  "Render status column with colored badges"
  [status _row]
  (let [colors (get utils/status-colors status utils/default-status-color)]
    [:span {:style {:display "inline-block"
                    :padding "0.25rem 0.75rem"
                    :background (:bg colors)
                    :color (:color colors)
                    :border-radius "12px"
                    :font-size "0.75rem"
                    :font-weight "500"
                    :text-transform "capitalize"}}
     (str/replace status "_" " ")]))

(defn address-render
  "Render address column with city subtitle"
  [address-name row]
  [:div
   [:div {:style {:font-weight "500"
                  :color "#111827"
                  :font-size "0.875rem"}}
    address-name]
   (when (:worksheet/address-city row)
     [:div {:style {:color "#6b7280"
                    :font-size "0.75rem"
                    :margin-top "0.25rem"}}
      (:worksheet/address-city row)])])

(defn assigned-to-render
  "Render assigned to column"
  [assigned-to-name _row]
  [:div
   (if assigned-to-name
     [:div {:style {:color "#374151"
                    :font-size "0.875rem"
                    :font-weight "500"}}
      assigned-to-name]
     [:span {:style {:color "#9ca3af"
                     :font-style "italic"
                     :font-size "0.75rem"}}
      "Unassigned"])])

;; =============================================================================
;; Table Configuration
;; =============================================================================

(defn get-columns
  "Get column configuration for worksheets table"
  []
  [{:key :worksheet/serial-number
    :label (tr/tr :worksheets/table-header-serial)
    :render serial-render
    :sortable? true
    :style {:width "200px" :min-width "200px"}
    :cell-style {:width "200px" :min-width "200px"}}
   {:key :worksheet/work-type
    :label (tr/tr :worksheets/table-header-work-type)
    :render work-type-render
    :sortable? true}
   {:key :worksheet/address-name
    :label (tr/tr :worksheets/table-header-address)
    :render address-render
    :sortable? true}
   {:key :worksheet/assigned-to-name
    :label (tr/tr :worksheets/table-header-assigned-to)
    :render assigned-to-render
    :sortable? true}])

(defn get-actions
  "Get action configuration for worksheets table"
  [on-edit on-pdf on-delete]
  [{:key :edit
    :label (tr/tr :worksheets/action-edit)
    :variant :primary
    :on-click on-edit}
   {:key :pdf
    :label (tr/tr :worksheets/action-pdf)
    :variant :secondary
    :on-click on-pdf}
   {:key :delete
    :label (tr/tr :worksheets/action-delete)
    :variant :danger
    :on-click on-delete}])
