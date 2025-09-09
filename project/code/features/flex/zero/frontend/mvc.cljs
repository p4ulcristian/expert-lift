(ns features.flex.zero.frontend.mvc
  (:require
   [re-frame.core :as rf]))

;; Re-frame events and subscriptions for user data
(rf/reg-sub :user/get
  (fn [db _]
    (get db :user)))

(rf/reg-event-db :user/set
  (fn [db [_ user-data]]
    (assoc db :user user-data)))

;; Re-frame events and subscriptions for workspace data

(rf/reg-sub :workspace/get
            (fn [db _]
              (get db :workspace)))

(rf/reg-sub :workspace/am-i-owner?
            (fn [db _]
              (:workspace/owner? (get db :workspace))))

(rf/reg-sub :workspace/get-id
            (fn [db _]
              (get-in db [:workspace :workspace/id])))

(rf/reg-event-db :workspace/set
  (fn [db [_ workspace-data]]
    (assoc db :workspace workspace-data)))