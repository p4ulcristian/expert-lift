(ns features.customizer.blocks.cart.effects
  (:require
    [re-frame.core :as r]))

(r/reg-event-db
  :cart.count/inc!
  (fn [db [_]]
    (update-in db [:cart :count] inc)))

(r/reg-event-db
  :cart.count/dec!
  (fn [db [_]]
    (update-in db [:cart :count] dec)))

(r/reg-event-db
  :cart.content/add!
  (fn [db [_ data]]
    (update-in db [:cart :content] #(-> %1 vec (conj %2)) data)))

(r/reg-event-fx
  :cart/add-item!
  (fn [{:keys [db]} [_ data]]
    {:dispatch-n [[:cart.content/add! data]
                  [:notifications/notify! :success "Job added to cart!"]
                  [:cart.count/inc!]]}))

(r/reg-event-fx
  :cart/remove-job!
  (fn [{:keys [db]} [_ job-id]]
    {:dispatch-n [[:db/dissoc-in [:cart :content job-id]]
                  [:cart.count/dec!]]}))

(r/reg-event-fx
  :cart/remove-part!
  (fn [{:keys [db]} [_ job-id part-id]]
    (if (= 1 (count (get-in db [:cart :content job-id :parts])))
      {:dispatch   [:cart/remove-job! job-id]}
      {:dispatch-n [[:db/dissoc-in [:cart :content job-id :parts part-id]]]})))

(r/reg-event-db
  :cart.drawer/close!
  (fn [db [_]]
    (assoc db :cart.preview.drawer/state false)))

(r/reg-event-fx
  :cart.drawer/open!
  (fn [{:keys [db]} [_]]
    {:db (assoc db :cart.preview.drawer/state true)
     :dispatch-n [[:my-designs.drawer/close!]
                  [:details.drawer/close!]]}))

(r/reg-event-db
  :cart/clear!
  (fn [db [_]]
    (-> db
        (assoc-in [:cart :content] {})
        (assoc-in [:cart :count] 0))))
                  