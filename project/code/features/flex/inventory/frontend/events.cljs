(ns features.flex.inventory.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.inventory.frontend.request :as inventory-request]))

;; Subscriptions
(rf/reg-sub :flex/inventory-get
  (fn [db _]
    (get-in db [:flex/inventory :data])))

(rf/reg-sub :flex/inventory-loading?
  (fn [db _]
    (get-in db [:flex/inventory :loading?] false)))

;; Events  
(rf/reg-event-db :flex/inventory-set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:flex/inventory :loading?] loading?)))

(rf/reg-event-db :flex/inventory-set
  (fn [db [_ inventory]]
    (-> db
        (assoc-in [:flex/inventory :data] inventory)
        (assoc-in [:flex/inventory :loading?] false))))

(rf/reg-event-fx :flex/inventory
  (fn [{:keys [db]} [_ workspace-id]]
    (when workspace-id
      {:db (assoc-in db [:flex/inventory :loading?] true)
       :dispatch-later [{:ms 10
                        :dispatch [:flex/inventory-load workspace-id]}]})))

(rf/reg-event-fx :flex/inventory-load
  (fn [_ [_ workspace-id]]
    (js/console.log "Loading inventory for workspace:" workspace-id)
    (inventory-request/get-inventory
     workspace-id
     (fn [inventory]
       (js/console.log "Received inventory:" inventory)
       (rf/dispatch [:flex/inventory-set inventory])))
    {}))