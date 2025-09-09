
(ns features.customizer.panel.frontend.blocks.price.subs
  (:require
    [re-frame.core :as r]))

(defn get-package-total [db package-id]
  (let [package (get-in db [:customizer :packages package-id])]
    (reduce (fn [total [part-id _]]
              (let [part (get-in db [:customizer :parts (name part-id)])]
                (+ total (:price part 0))))
            (:price package 0)
            (:children package))))

(r/reg-sub
  :customizer.package/get-total
  (fn [db [_]]
    (if-let [package-id (get-in db [:customizer :package-id])]
      (get-package-total db package-id)
      0)))