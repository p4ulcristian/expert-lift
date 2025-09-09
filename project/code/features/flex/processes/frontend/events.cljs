(ns features.flex.processes.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.processes.frontend.request :as processes-request]))

;; Subscriptions
(rf/reg-sub :flex/processes-get
  (fn [db _]
    (get-in db [:flex/processes :data])))

(rf/reg-sub :flex/processes-loading?
  (fn [db _]
    (get-in db [:flex/processes :loading?] false)))

;; Events  
(rf/reg-event-db :flex/processes-set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:flex/processes :loading?] loading?)))

(rf/reg-event-db :flex/processes-set
  (fn [db [_ processes]]
    (-> db
        (assoc-in [:flex/processes :data] processes)
        (assoc-in [:flex/processes :loading?] false))))

(rf/reg-event-fx :flex/processes
  (fn [{:keys [db]} [_ workspace-id]]
    (when workspace-id
      {:db (assoc-in db [:flex/processes :loading?] true)
       :dispatch-later [{:ms 10
                        :dispatch [:flex/processes-load workspace-id]}]})))

(rf/reg-event-fx :flex/processes-load
  (fn [_ [_ workspace-id]]
    (js/console.log "Loading processes for workspace:" workspace-id)
    (processes-request/get-processes
     workspace-id
     (fn [processes]
       (js/console.log "Received processes:" processes)
       (rf/dispatch [:flex/processes-set processes])))
    {}))