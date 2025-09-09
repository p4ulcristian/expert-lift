(ns features.customizer.panel.frontend.blocks.url
  (:require
    [re-frame.core :as r]))

(defn build-customizer-query-params [db]
  (let [selected-id (get-in db [:customizer/menu :selected :id])
        filter  (get-in db [:customizer/filters])
        params  (cond-> {}
                  selected-id (assoc :menu-id selected-id)
                  filter      (assoc :filter filter))]
    params))

(r/reg-event-fx
  :customizer.url/update-url!
  (fn [{:keys [db]} [_]]
    (let [params (build-customizer-query-params db)]
      {:swap-query-params! {:params params}})))
