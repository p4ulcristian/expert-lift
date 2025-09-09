(ns features.flex.workstations.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.workstations.frontend.request :as workstations-request]))

;; Subscriptions
(rf/reg-sub :flex/workstations-get
  (fn [db _]
    (get-in db [:flex/workstations :data])))

(rf/reg-sub :flex/workstations-loading?
  (fn [db _]
    (get-in db [:flex/workstations :loading?] false)))

;; Events  
(rf/reg-event-db :flex/workstations-set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:flex/workstations :loading?] loading?)))

(rf/reg-event-db :flex/workstations-set
  (fn [db [_ workstations]]
    (-> db
        (assoc-in [:flex/workstations :data] workstations)
        (assoc-in [:flex/workstations :loading?] false))))

(rf/reg-event-fx :flex/workstations
  (fn [{:keys [db]} [_ workspace-id]]
    (when workspace-id
      {:db (assoc-in db [:flex/workstations :loading?] true)
       :dispatch-later [{:ms 10
                        :dispatch [:flex/workstations-load workspace-id]}]})))

(rf/reg-event-fx :flex/workstations-load
  (fn [_ [_ workspace-id]]
    (js/console.log "Loading workstations for workspace:" workspace-id)
    (workstations-request/get-workstations
     workspace-id
     (fn [workstations]
       (js/console.log "Received workstations:" workstations)
       (rf/dispatch [:flex/workstations-set workstations])))
    {}))