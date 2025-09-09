(ns features.flex.orders.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.orders.frontend.request :as orders-request]))

;; Subscriptions
(rf/reg-sub :flex/orders-get
  (fn [db _]
    (get-in db [:flex/orders :data])))

(rf/reg-sub :flex/orders-loading?
  (fn [db _]
    (get-in db [:flex/orders :loading?] false)))

;; Events  
(rf/reg-event-db :flex/orders-set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:flex/orders :loading?] loading?)))

(rf/reg-event-db :flex/orders-set
  (fn [db [_ orders]]
    (-> db
        (assoc-in [:flex/orders :data] orders)
        (assoc-in [:flex/orders :loading?] false))))

(rf/reg-event-fx :flex/orders
  (fn [{:keys [db]} [_ workspace-id]]
    (when workspace-id
      {:db (assoc-in db [:flex/orders :loading?] true)
       :dispatch-later [{:ms 10
                        :dispatch [:flex/orders-load workspace-id]}]})))

(rf/reg-event-fx :flex/orders-load
  (fn [_ [_ workspace-id]]
    (js/console.log "Loading orders for workspace:" workspace-id)
    (orders-request/get-orders
     workspace-id
     (fn [orders]
       (js/console.log "Received orders:" orders)
       (rf/dispatch [:flex/orders-set orders])))
    {}))