(ns features.customizer.blocks.my-designs.effects
  (:require
    [re-frame.core :as r]))

(r/reg-event-db
  :my-designs.drawer/close!
  (fn [db [_]]
    (assoc db :my-designs.drawer/state false)))

(r/reg-event-fx
  :my-designs.drawer/open!
  (fn [{:keys [db]} [_]]
    {:db (assoc db :my-designs.drawer/state true)
     :dispatch-n [[:cart.drawer/close!]
                  [:details.drawer/close!]]}))