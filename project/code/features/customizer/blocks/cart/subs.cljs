(ns features.customizer.blocks.cart.subs
  (:require
    [re-frame.core  :as r]))

(r/reg-sub
  :cart/data
  (fn [db [_]]
    (get db :cart)))

(r/reg-sub
  :cart/total
  (fn [db [_]]
    (let [cart (get-in db [:cart :content])]
      (if (empty? cart)
        0
        (let [total (reduce (fn [total [_ item]]
                              (let [item-total (or (:total item) (:price item) 0)]
                                (+ total item-total)))
                            0 cart)]
          total)))))
