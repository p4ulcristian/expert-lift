(ns features.app.worksheets.frontend.subscriptions
  "Re-frame subscriptions for worksheets feature"
  (:require
   [zero.frontend.re-frame :as rf]))

;; =============================================================================
;; Worksheet Data Subscriptions
;; =============================================================================

(rf/reg-sub
 :worksheets/data
 (fn [db _]
   (get-in db [:worksheets :data] {:worksheets [] :pagination {}})))

(rf/reg-sub
 :worksheets/loading?
 (fn [db _]
   (get-in db [:worksheets :loading?] false)))

(rf/reg-sub
 :worksheets/modal-worksheet
 (fn [db _]
   (get-in db [:worksheets :modal-worksheet] nil)))

(rf/reg-sub
 :worksheets/modal-is-new?
 (fn [db _]
   (get-in db [:worksheets :modal-is-new?] false)))

(rf/reg-sub
 :worksheets/authenticated?
 (fn [db _]
   (get-in db [:worksheets :authenticated?] nil)))

;; =============================================================================
;; Modal Form Subscriptions
;; =============================================================================

(rf/reg-sub
 :worksheets/modal-form-data
 (fn [db _]
   (get-in db [:worksheets :modal-form-data] {})))

(rf/reg-sub
 :worksheets/modal-form-errors
 (fn [db _]
   (get-in db [:worksheets :modal-form-errors] {})))

(rf/reg-sub
 :worksheets/modal-form-loading?
 (fn [db _]
   (get-in db [:worksheets :modal-form-loading?] false)))

(rf/reg-sub
 :worksheets/modal-form-field
 (fn [db [_ field-key]]
   (get-in db [:worksheets :modal-form-data field-key] nil)))

;; =============================================================================
;; Signature Subscriptions
;; =============================================================================

(rf/reg-sub
 :worksheets/maintainer-signature-ref
 (fn [db _]
   (get-in db [:worksheets :maintainer-signature-ref])))

(rf/reg-sub
 :worksheets/customer-signature-ref
 (fn [db _]
   (get-in db [:worksheets :customer-signature-ref])))

(rf/reg-sub
 :worksheets/signature-zoom-data
 (fn [db _]
   (get-in db [:worksheets :signature-zoom-data])))

(rf/reg-sub
 :worksheets/zoom-signature-ref
 (fn [db _]
   (get-in db [:worksheets :zoom-signature-ref])))

;; =============================================================================
;; Material Template Subscriptions
;; =============================================================================

(rf/reg-sub
 :material-templates/all
 (fn [db _]
   (get-in db [:material-templates :all] [])))
