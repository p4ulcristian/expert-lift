(ns features.flex.locations.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.locations.frontend.request :as locations-request]))

;; Subscriptions
(rf/reg-sub :flex/locations-get
  (fn [db _]
    (get-in db [:flex/locations :data])))

(rf/reg-sub :flex/locations-loading?
  (fn [db _]
    (get-in db [:flex/locations :loading?] false)))

;; Events  
(rf/reg-event-db :flex/locations-set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:flex/locations :loading?] loading?)))

(rf/reg-event-db :flex/locations-set
  (fn [db [_ locations]]
    (-> db
        (assoc-in [:flex/locations :data] locations)
        (assoc-in [:flex/locations :loading?] false))))

(rf/reg-event-fx :flex/locations
  (fn [{:keys [db]} [_ workspace-id]]
    (when workspace-id
      {:db (assoc-in db [:flex/locations :loading?] true)
       :dispatch-later [{:ms 10
                        :dispatch [:flex/locations-load workspace-id]}]})))

(rf/reg-event-fx :flex/locations-load
  (fn [_ [_ workspace-id]]
    (js/console.log "Loading locations for workspace:" workspace-id)
    (locations-request/get-locations
     workspace-id
     (fn [locations]
       (js/console.log "Received locations:" locations)
       (rf/dispatch [:flex/locations-set locations])))
    {}))