(ns zero.frontend.re-frame
  (:require [re-frame.core :as re-frame.core
             :refer [->interceptor console get-coeffect]]))

(def excluded-list [])

(defn excluded-event? [event-key]
  (boolean (some #(= event-key %) excluded-list)))

(def debug
  (->interceptor
   :id     :debug
  ;;  :before (fn debug-before
  ;;            [context]
  ;;            (console :log "Handling re-frame event:" (get-coeffect context :event))
  ;;            context)
   :after  (fn debug-after
             [context]
             (let [event   (get-coeffect context :event)
                   [event-key & event-params] event]
               (when-not (excluded-event? event-key)
                 ;(console :log context)
                 (console :log event-key event-params))
               context))))

(def standard-interceptors  [re-frame.core/trim-v])
                             ;debug
                             


(def reg-fx         re-frame.core/reg-fx)
(def reg-event-fx   re-frame.core/reg-event-fx)
(def dispatch       re-frame.core/dispatch)
(def subscribe      re-frame.core/subscribe)
(def reg-event-db   re-frame.core/reg-event-db)  ; Use standard re-frame without interceptors
(def reg-sub        re-frame.core/reg-sub)       ; Use standard re-frame subscriptions

(reg-sub
  :db/get
  (fn [db [_ key & [default-value]]]
    (get db key default-value)))

(reg-sub
  :db/get-in
  (fn [db [_ path & [default-value]]]
    (get-in db path default-value)))

(reg-event-db
  :db/assoc
  (fn [db [_ & params]]
    (apply assoc (concat [db] params))))

(reg-event-db
  :db/assoc-in
  (fn [db [_ path value]]
    (assoc-in db path value)))

(reg-event-db
 :db/update-in
 (fn [db [_ path _fn & params]]
   (let [item   (get-in db path)
         params (cons item params)]
     (assoc-in db path (apply _fn params)))))

(reg-event-db
  :db/dissoc
  (fn [db [_ & params]]
    (apply dissoc (concat [db] params))))

(reg-event-db
  :db/dissoc-in
  (fn [db [_ path]]
    (update-in db (butlast path) dissoc (last path))))