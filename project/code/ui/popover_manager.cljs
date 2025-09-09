
(ns ui.popover-manager
  (:require
   [re-frame.core :as r]
   [ui.popover :as popover]))
   

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-db
 :popover/remove
 (fn [db [_ id]]
   (update db :popovers dissoc id)))

(r/reg-event-db
 :popover/close
 (fn [db [_ id]]
   (if (get-in db [:popovers id] false)
     (assoc-in db [:popovers id :state] false)
     db)))

(r/reg-event-db
  :popover/open
  (fn [db [_ id popover-props]]
    (assoc-in db [:popovers id] popover-props)))

(r/reg-event-db
  :popover/update
  (fn [db [_ id popover-props]]
    (update-in db [:popovers id] merge popover-props)))

;; ---- Effects ----
;; -----------------------------------------------------------------------------

(defn render-popover [id popover-props]
  (let [popover-id (or id (random-uuid))]
    [popover/view
      (merge {:id            popover-id
              :state         (:state popover-props true)
              :on-click-away #(r/dispatch [:popover/close popover-id])}
             popover-props)
      (:content popover-props)]))

(defn view [_props]
  [:<>
    (let [popovers @(r/subscribe [:db/get-in [:popovers]])]
      (map (fn [[id props]]
             ^{:key id}
             [render-popover id (assoc props :scroll-ref ((:scroll-ref _props)))]) 
           popovers))]) 
       
