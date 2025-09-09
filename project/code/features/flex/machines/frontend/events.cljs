(ns features.flex.machines.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.machines.frontend.request :as machines-request]))

;; Subscriptions
(rf/reg-sub :flex/machines-get
  (fn [db _]
    (get-in db [:flex/machines :data])))

(rf/reg-sub :flex/machines-loading?
  (fn [db _]
    (get-in db [:flex/machines :loading?] false)))

;; Events  
(rf/reg-event-db :flex/machines-set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:flex/machines :loading?] loading?)))

(rf/reg-event-db :flex/machines-set
  (fn [db [_ machines]]
    (-> db
        (assoc-in [:flex/machines :data] machines)
        (assoc-in [:flex/machines :loading?] false))))

(rf/reg-event-fx :flex/machines
  (fn [{:keys [db]} [_ workspace-id]]
    (when workspace-id
      {:db (assoc-in db [:flex/machines :loading?] true)
       :dispatch-later [{:ms 10
                        :dispatch [:flex/machines-load workspace-id]}]})))

(rf/reg-event-fx :flex/machines-load
  (fn [_ [_ workspace-id]]
    (js/console.log "Loading machines for workspace:" workspace-id)
    (machines-request/get-machines
     workspace-id
     (fn [machines]
       (js/console.log "Received machines:" machines)
       (rf/dispatch [:flex/machines-set machines])))
    {}))