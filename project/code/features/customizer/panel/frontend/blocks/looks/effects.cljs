
(ns features.customizer.panel.frontend.blocks.looks.effects
  (:require 
    [re-frame.core   :as r]))

;; -----------------------------------------------------------------------------
;; ---- Fetch ----

(r/reg-event-fx
  :customizer.looks/init!
  (fn [{:keys [db]} [_ & [data]]]
    (let [target-look (first (filter #(= (:id %) "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb") data))
          look-to-select (or target-look (first data))
          look-index (first (keep-indexed #(when (= (:id %2) (:id look-to-select)) %1) data))]
      {:dispatch-n [[:db/assoc-in [:customizer/looks] {:items data
                                                       :count (count data)
                                                       :index (or look-index 0)}]
                    ;; Auto-select the target look after looks are initialized
                    (when (and (seq data) 
                               look-to-select
                               (not (get-in db [:customizer :selected-look])))
                      [:looks/select! (assoc look-to-select :index (or look-index 0))])]})))


;; ---- Fetch ----
;; -----------------------------------------------------------------------------

(r/reg-event-fx
  :looks/did-mount!
  (fn [{:keys [db]} [_ slider-props collection]]
    ;; No auto-selection here - handled in :customizer.looks/init!
    {}))

(r/reg-event-fx
  :looks/unselect!
  (fn [{:keys [db]} [_ looks-data]]
    (let [cursor (get-in db [:customizer :cursor])]
      {:dispatch-n [[:db/dissoc-in [:customizer :selected-look]]
                    (when cursor [:db/update-in cursor dissoc :look])
                    [:customizer.look-cost/remove! cursor]]})))

(r/reg-event-fx
  :looks/select!
  (fn [{:keys [db]} [_ looks-data]]
    (let [cursor (get-in db [:customizer :cursor])
          part?  (-> db (get-in cursor) :type (= "part"))]
      {:dispatch-n [[:db/assoc-in [:customizer :selected-look] looks-data]
                    [:db/assoc-in [:customizer/looks :index] (:index looks-data)]
                    (when part? [:db/update-in cursor assoc :look looks-data])
                    [:customizer.look-cost/calc! cursor]]})))

(r/reg-event-fx
  :looks.card/click!
  (fn [{:keys [db]} [_ {:keys [id] :as looks-data}]]
    (let [current-look (get-in db [:customizer :selected-look])]
      (if (= (:id current-look) id)
        {:dispatch [:looks/unselect! looks-data]}
        {:dispatch [:looks/select! looks-data]}))))

(r/reg-event-db
  :looks/force-move!
  (fn [db [_]]
    (assoc-in db [:move] true)))

(r/reg-event-fx
  :looks/select-by-index!
  (fn [{:keys [db]} [_ index]]
    (let [looks-data (get-in db [:customizer/looks :items index])
          cursor     (get-in db [:customizer :cursor])]
     
      {:dispatch-n [[:db/assoc-in [:customizer :selected-look] looks-data]
                    [:db/assoc-in (conj cursor :look) looks-data]
                    [:db/assoc-in [:customizer/looks :index] index]
                    [:customizer.look-cost/calc! cursor]]})))

(r/reg-event-fx
  :looks/pin!
  (fn [{:keys [db]} [_ {:keys [id] :as looks-data}]]
    (let [pinned? (first (filter (fn [a] (= id (:id a))) (get-in db [:pinned/looks])))]
      (if pinned?
        {:dispatch [:db/assoc-in [:pinned/looks] (vec (remove #(= (:id %) id) (get-in db [:pinned/looks])))]}
        {:dispatch-n [[:db/update-in [:pinned/looks] (fn [a] (conj (vec a) looks-data))]]}))))
