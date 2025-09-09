(ns features.flex.recipes.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.recipes.frontend.request :as recipes-request]))

;; Subscriptions
(rf/reg-sub :flex/recipes-get
  (fn [db _]
    (get-in db [:flex/recipes :data])))

(rf/reg-sub :flex/recipes-loading?
  (fn [db _]
    (get-in db [:flex/recipes :loading?] false)))

;; Events  
(rf/reg-event-db :flex/recipes-set-loading
  (fn [db [_ loading?]]
    (assoc-in db [:flex/recipes :loading?] loading?)))

(rf/reg-event-db :flex/recipes-set
  (fn [db [_ recipes]]
    (-> db
        (assoc-in [:flex/recipes :data] recipes)
        (assoc-in [:flex/recipes :loading?] false))))

(rf/reg-event-fx :flex/recipes
  (fn [{:keys [db]} [_ workspace-id]]
    (when workspace-id
      {:db (assoc-in db [:flex/recipes :loading?] true)
       :dispatch-later [{:ms 10
                        :dispatch [:flex/recipes-load workspace-id]}]})))

(rf/reg-event-fx :flex/recipes-load
  (fn [_ [_ workspace-id]]
    (js/console.log "Loading recipes for workspace:" workspace-id)
    (recipes-request/get-recipes
     workspace-id
     (fn [recipes]
       (js/console.log "Received recipes:" recipes)
       (rf/dispatch [:flex/recipes-set recipes])))
    {}))