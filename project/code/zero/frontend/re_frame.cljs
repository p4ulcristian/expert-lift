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
                             
(defn reg-event-db          ;; alternative to reg-event-db
  ([id handler-fn]
   (re-frame.core/reg-event-db id standard-interceptors handler-fn))
  ([id interceptors handler-fn]
   (re-frame.core/reg-event-db
    id
    [standard-interceptors interceptors]
    handler-fn)))

(defn reg-sub          ;; alternative to reg-event-db
  [event-key _fn]
  (re-frame.core/reg-sub
   event-key (fn [db params]
               (_fn db (rest params)))))

(def reg-fx         re-frame.core/reg-fx)
(def reg-event-fx   re-frame.core/reg-event-fx)
(def dispatch       re-frame.core/dispatch)
(def subscribe      re-frame.core/subscribe)

(reg-sub
  :db/get
  (fn [db [_key & [default-value]]]
    (get db _key default-value)))

(reg-sub
  :db/get-in
  (fn [db [path & [default-value]]]
    (get-in db path default-value)))

(reg-event-db
  :db/assoc
  (fn [db [& params]]
    (apply assoc (concat [db] params))))

(reg-event-db
  :db/assoc-in
  (fn [db [path _value]]
    (assoc-in db path _value)))

(reg-event-db
 :db/update-in
 (fn [db [path _fn & params]]
   (let [item   (get-in db path)
         params (cons item params)]
     (assoc-in db path (apply _fn params)))))

(reg-event-db
  :db/dissoc
  (fn [db [& params]]
    (apply dissoc (concat [db] params))))

(reg-event-db
  :db/dissoc-in
  (fn [db [path]]
    (update-in db (butlast path) dissoc (last path))))