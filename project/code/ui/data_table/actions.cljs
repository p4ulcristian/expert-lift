(ns ui.data-table.actions
  "Action button components for data table rows.
   Provides standard edit/delete buttons and extensible action primitives."
  (:require
   [ui.data-table.icons :as icons]
   [ui.button :as button]))

;; -----------------------------------------------------------------------------
;; ---- Action Button Primitives ----

(defn action-button
  "Generic action button with title tooltip.

   Props:
     :tooltip-text - Text to show on hover (uses title attr)
     :icon-class   - Font Awesome icon class (e.g., 'fa-solid fa-pen')
     :button-type  - Button type (:secondary, :warning, :success, :primary)
     :on-click     - Click handler
     :class        - Additional CSS class
     :data-testid  - Test ID for e2e testing"
  [{:keys [tooltip-text icon-class button-type on-click class data-testid]}]
  [button/view {:mode        :outlined
                :type        (or button-type :secondary)
                :class       (str "data-table-actions-button " class)
                :override    {:data-testid data-testid
                              :title       tooltip-text}
                :on-click    on-click}
   [:i {:class icon-class}]])

;; ---- Action Button Primitives ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Standard Action Buttons ----

(defn edit-button
  "Standard edit button component"
  [row {:keys [entity on-edit]}]
  (let [entity-name  (:name entity "item")
        id-key       (keyword (str entity-name "/id"))
        entity-id    (get row id-key)]
    [action-button
     {:tooltip-text (str "Edit " entity-name)
      :icon-class   (icons/icon :edit)
      :button-type  :secondary
      :class        (str "edit-" entity-name "-button")
      :data-testid  (str "edit-" entity-name "-" entity-id)
      :on-click     #(on-edit row)}]))

(defn delete-button
  "Standard delete button component"
  [row {:keys [on-delete entity]}]
  (let [entity-name  (:name entity "item")
        id-key       (keyword (str entity-name "/id"))
        entity-id    (get row id-key)]
    [action-button
     {:tooltip-text (str "Delete " entity-name)
      :icon-class   (icons/icon :delete)
      :button-type  :warning
      :class        (str "delete-" entity-name "-button")
      :data-testid  (str "delete-" entity-name "-" entity-id)
      :on-click     #(on-delete row)}]))

(defn actions-cell
  "Standard action buttons wrapper (edit + delete)"
  [row {:keys [on-edit on-delete] :as config}]
  [:<>
    (when on-edit [edit-button row config])
    (when on-delete [delete-button row config])])

;; ---- Standard Action Buttons ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Custom Action Buttons (for approve/deny, etc.) ----

(defn approve-button
  "Approve action button"
  [{:keys [on-click disabled? processing?]}]
  [button/view {:mode     :filled
                :type     :success
                :class    "data-table-actions-button"
                :disabled (or disabled? processing?)
                :on-click on-click}
   (if processing? "..." "Approve")])

(defn deny-button
  "Deny action button"
  [{:keys [on-click disabled? processing?]}]
  [button/view {:mode     :filled
                :type     :warning
                :class    "data-table-actions-button"
                :disabled (or disabled? processing?)
                :on-click on-click}
   (if processing? "..." "Deny")])

;; ---- Custom Action Buttons ----
;; -----------------------------------------------------------------------------
