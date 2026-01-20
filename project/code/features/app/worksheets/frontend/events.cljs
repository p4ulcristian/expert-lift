(ns features.app.worksheets.frontend.events
  "Re-frame events for worksheets feature"
  (:require
   [zero.frontend.re-frame :as rf]
   [parquery.frontend.request :as parquery]))

;; =============================================================================
;; Worksheet Data Events
;; =============================================================================

(rf/reg-event-db
 :worksheets/set-loading
 (fn [db [_ loading?]]
   (assoc-in db [:worksheets :loading?] loading?)))

(rf/reg-event-db
 :worksheets/set-data
 (fn [db [_ data]]
   (-> db
       (assoc-in [:worksheets :data] data)
       (assoc-in [:worksheets :loading?] false))))

(rf/reg-event-db
 :worksheets/set-modal-worksheet
 (fn [db [_ worksheet]]
   (assoc-in db [:worksheets :modal-worksheet] worksheet)))

(rf/reg-event-db
 :worksheets/set-modal-is-new
 (fn [db [_ is-new?]]
   (assoc-in db [:worksheets :modal-is-new?] is-new?)))

(rf/reg-event-db
 :worksheets/set-authenticated
 (fn [db [_ authenticated?]]
   (assoc-in db [:worksheets :authenticated?] authenticated?)))

(rf/reg-event-db
 :worksheets/close-modal
 (fn [db _]
   (assoc-in db [:worksheets :modal-worksheet] nil)))

;; =============================================================================
;; Modal Form Events
;; =============================================================================

(defn- normalize-elevators
  "Normalize elevators data to a vector of name strings.
   Handles: JSON string, vector of maps, single map, vector of strings"
  [elevators]
  (let [;; Parse JSON if string
        parsed (cond
                 (string? elevators)
                 (try (js->clj (js/JSON.parse elevators) :keywordize-keys true)
                      (catch :default _ nil))
                 :else elevators)
        ;; Ensure it's a vector
        as-vec (cond
                 (vector? parsed) parsed
                 (sequential? parsed) (vec parsed)
                 (map? parsed) [parsed]  ;; Single object -> wrap in vector
                 :else [])]
    ;; Extract names from each item
    (mapv (fn [e]
            (if (map? e)
              (or (:name e) (:elevator/name e) (str e))
              (str e)))
          as-vec)))

(rf/reg-event-db
 :worksheets/set-modal-form-data
 (fn [db [_ data]]
   ;; Construct address object from address data if present
   ;; Also load address-elevators for the elevator dropdown when editing
   (let [raw-elevators (or (:worksheet/address-elevators data)
                           (:address_elevators data)
                           (:address-elevators data))
         ;; Normalize to vector of name strings
         elevator-names (normalize-elevators raw-elevators)
         enhanced-data (cond-> data
                         ;; Add address object for display
                         (and (:worksheet/address-id data)
                              (:worksheet/address-name data))
                         (assoc :worksheet/address
                                {:address/id (:worksheet/address-id data)
                                 :address/name (:worksheet/address-name data)
                                 :address/display (str (:worksheet/address-name data)
                                                       " - " (:worksheet/address-city data))})
                         ;; Add address-elevators for dropdown (as simple name strings)
                         (seq elevator-names)
                         (assoc :worksheet/address-elevators elevator-names))]
     (assoc-in db [:worksheets :modal-form-data] enhanced-data))))

(rf/reg-event-db
 :worksheets/update-modal-form-field
 (fn [db [_ field-key value]]
   (assoc-in db [:worksheets :modal-form-data field-key] value)))

(rf/reg-event-db
 :worksheets/set-modal-form-errors
 (fn [db [_ errors]]
   (assoc-in db [:worksheets :modal-form-errors] errors)))

(rf/reg-event-db
 :worksheets/set-modal-form-loading
 (fn [db [_ loading?]]
   (assoc-in db [:worksheets :modal-form-loading?] loading?)))

(rf/reg-event-db
 :worksheets/modal-form-set-field
 (fn [db [_ field-key value]]
   (assoc-in db [:worksheets :modal-form-data field-key] value)))

(rf/reg-event-db
 :worksheets/update-form-field
 (fn [db [_ field-key value]]
   (assoc-in db [:worksheets :modal-form-data field-key] value)))

(rf/reg-event-db
 :worksheets/clear-modal-form
 (fn [db _]
   (-> db
       (assoc-in [:worksheets :modal-form-data] {})
       (assoc-in [:worksheets :modal-form-errors] {})
       (assoc-in [:worksheets :modal-form-loading?] false)
       (assoc-in [:worksheets :maintainer-signature-ref] nil)
       (assoc-in [:worksheets :customer-signature-ref] nil)
       (assoc-in [:worksheets :signature-zoom-data] nil)
       (assoc-in [:worksheets :zoom-signature-ref] nil))))

;; =============================================================================
;; Signature Events
;; =============================================================================

(rf/reg-event-db
 :worksheets/set-maintainer-signature-ref
 (fn [db [_ ref]]
   (assoc-in db [:worksheets :maintainer-signature-ref] ref)))

(rf/reg-event-db
 :worksheets/set-customer-signature-ref
 (fn [db [_ ref]]
   (assoc-in db [:worksheets :customer-signature-ref] ref)))

(rf/reg-event-db
 :worksheets/set-zoom-signature-ref
 (fn [db [_ ref]]
   (assoc-in db [:worksheets :zoom-signature-ref] ref)))

(rf/reg-event-db
 :worksheets/open-signature-zoom
 (fn [db [_ ref-dispatch-key label]]
   (assoc-in db [:worksheets :signature-zoom-data]
             {:label label
              :ref-dispatch-key ref-dispatch-key})))

(rf/reg-event-fx
 :worksheets/close-signature-zoom
 (fn [{:keys [db]} _]
   (let [zoom-data (get-in db [:worksheets :signature-zoom-data])
         zoom-ref (get-in db [:worksheets :zoom-signature-ref])]
     ;; Store signature as point data (JSON) for resolution independence
     ;; This allows redrawing at any size without distortion
     (let [new-db (if (and zoom-data zoom-ref (not (.isEmpty ^js zoom-ref)))
                    (let [;; Get point data as JavaScript array, convert to JSON string
                          points-data (.toData ^js zoom-ref)
                          signature-json (js/JSON.stringify (clj->js points-data))
                          ;; Prefix with "points:" to identify the format
                          signature-data (str "points:" signature-json)
                          signature-key (if (= (:ref-dispatch-key zoom-data) :worksheets/set-maintainer-signature-ref)
                                          :worksheet/maintainer-signature
                                          :worksheet/customer-signature)]
                      (-> db
                          (assoc-in [:worksheets :modal-form-data signature-key] signature-data)
                          (assoc-in [:worksheets :signature-zoom-data] nil)
                          (assoc-in [:worksheets :zoom-signature-ref] nil)))
                    (-> db
                        (assoc-in [:worksheets :signature-zoom-data] nil)
                        (assoc-in [:worksheets :zoom-signature-ref] nil)))]
       {:db new-db}))))

;; =============================================================================
;; Material Template Events
;; =============================================================================

(rf/reg-event-db
 :material-templates/load
 (fn [db [_ workspace-id]]
   (when workspace-id
     (parquery/send-queries
      {:queries {:workspace-material-templates/get-all {:workspace-id workspace-id}}
       :parquery/context {:workspace-id workspace-id}
       :callback (fn [response]
                   (rf/dispatch [:material-templates/loaded (:workspace-material-templates/get-all response)]))}))
   db))

(rf/reg-event-db
 :material-templates/loaded
 (fn [db [_ templates]]
   (assoc-in db [:material-templates :all] (or templates []))))

(rf/reg-event-db
 :worksheets/select-material-template
 (fn [db [_ template-id]]
   (assoc-in db [:worksheets :modal-form-data :worksheet/selected-material-template] template-id)))

(rf/reg-event-db
 :worksheets/add-selected-material
 (fn [db _]
   (let [form-data (get-in db [:worksheets :modal-form-data])
         selected-template-id (:worksheet/selected-material-template form-data)
         quantity (:worksheet/new-material-quantity form-data)
         templates (get-in db [:material-templates :all] [])]
     (if (and selected-template-id quantity (not (empty? (str quantity))))
       (let [selected-template (first (filter #(= (:material-template/id %) selected-template-id) templates))
             new-material {:name (:material-template/name selected-template)
                           :unit (:material-template/unit selected-template)
                           :quantity (str quantity)}
             current-materials (get form-data :worksheet/material-usage [])]
         (-> db
             (assoc-in [:worksheets :modal-form-data :worksheet/material-usage]
                       (conj current-materials new-material))
             (assoc-in [:worksheets :modal-form-data :worksheet/selected-material-template] "")
             (assoc-in [:worksheets :modal-form-data :worksheet/new-material-quantity] "")))
       db))))

(rf/reg-event-db
 :worksheets/remove-material
 (fn [db [_ idx]]
   (let [current-materials (get-in db [:worksheets :modal-form-data :worksheet/material-usage] [])]
     (assoc-in db [:worksheets :modal-form-data :worksheet/material-usage]
               (vec (concat (take idx current-materials)
                            (drop (inc idx) current-materials)))))))

(rf/reg-event-db
 :worksheets/add-custom-material
 (fn [db _]
   (let [form-data (get-in db [:worksheets :modal-form-data])
         custom-name (:worksheet/custom-material-name form-data)
         custom-unit (:worksheet/custom-material-unit form-data)
         custom-quantity (:worksheet/custom-material-quantity form-data)]
     (if (and custom-name custom-unit custom-quantity
              (not (empty? (str custom-name)))
              (not (empty? (str custom-unit)))
              (not (empty? (str custom-quantity))))
       (let [new-material {:name (str custom-name)
                           :unit (str custom-unit)
                           :quantity (str custom-quantity)}
             current-materials (get form-data :worksheet/material-usage [])]
         (-> db
             (assoc-in [:worksheets :modal-form-data :worksheet/material-usage]
                       (conj current-materials new-material))
             (assoc-in [:worksheets :modal-form-data :worksheet/custom-material-name] "")
             (assoc-in [:worksheets :modal-form-data :worksheet/custom-material-unit] "")
             (assoc-in [:worksheets :modal-form-data :worksheet/custom-material-quantity] "")))
       db))))

;; =============================================================================
;; Authentication Events
;; =============================================================================

(rf/reg-event-db
 :worksheets/check-authentication
 (fn [db _]
   (parquery/send-queries
    {:queries {:user/current {}}
     :parquery/context {}
     :callback (fn [response]
                 (let [user (:user/current response)]
                   (if (and user (:user/id user))
                     (do
                       (rf/dispatch [:worksheets/set-authenticated true])
                       (when (empty? (:worksheets (get-in db [:worksheets :data]) []))
                         (rf/dispatch [:worksheets/load-data {}])))
                     (rf/dispatch [:worksheets/set-authenticated false]))))})
   db))
