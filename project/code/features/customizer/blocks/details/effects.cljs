(ns features.customizer.blocks.details.effects
  (:require
    [re-frame.core :as r]))

(r/reg-event-fx
  :details.drawer/close!
  (fn [{:keys [db]} [_]]
    {:db (assoc db :details.drawer/state false)}))

(r/reg-event-fx
  :details.drawer/open!
  (fn [{:keys [db]} [_]]
    {:db (assoc db :details.drawer/state true)
     :dispatch-n [[:cart.drawer/close!]
                  [:my-designs.drawer/close!]]}))