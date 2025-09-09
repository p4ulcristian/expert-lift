
(ns features.customizer.panel.frontend.subs
  (:require
    [re-frame.core :as r]))

(r/reg-sub :customizer/get-edited-item
  (fn [db [_]]
    ;; TODO: Get model_url from package instead merging in
    (if-let [cursor (get-in db [:customizer :cursor])]
      (let [item (get-in db cursor)]
        (assoc item :model_url (get-in db [:customizer :packages (:package_id item) :model_url])))
      false)))

(r/reg-sub
  :customizer/layout
  (fn [db [_]]
     (if (<= 750 (get-in db [:x.environment :viewport-handler/meta-items :viewport-width]))
        "desktop" 
        "mobile")))

(r/reg-sub
  :customizer/get
  (fn [db [_]]
    (get-in db [:customizer])))

(r/reg-sub
 :customizer/edit?
 (fn [db [_]]
   (= :start-edit (get-in db [:customizer :state]))))

(r/reg-sub
 :customizer.total/zero?
 (fn [db]
   (zero? (get-in db [:customizer :total]))))

