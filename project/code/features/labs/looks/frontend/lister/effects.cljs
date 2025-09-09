(ns features.labs.looks.frontend.lister.effects
  (:require
    [re-frame.core :as r]))

;; -----------------------------------------------------------------------------
;; ---- List Look ----

(defn list-looks-callback [response]
  (let [data (-> response (get :looks/list!))]
    (r/dispatch [:db/assoc-in [:labs :lister :looks] data])))

(r/reg-event-fx :labs.looks/list!
  (fn [{:keys [_db]} [_ {:keys [search]}]]
    (let [params {:search (if (empty? search) nil search)}]
      {:dispatch [:pathom/ws-request!
                   {:callback list-looks-callback
                    :query    [`(:looks/list! ~params)]}]})))

;; ---- List Look ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Delete Look ----

(defn delete-look-callback [response]
  (let [data (-> response (get :looks/delete!))]
    (println data)))
    ;; (r/dispatch [:db/assoc-in [:labs :lister :looks] data])))

(r/reg-event-fx :labs.looks.lister/delete!
  (fn [{:keys [_db]} [_ id]]
    {:dispatch [:pathom/ws-request!
                 {:callback delete-look-callback
                  :query    [`(looks/delete! {:id ~id})]}]}))


;; ---- Delete Look ----
;; -----------------------------------------------------------------------------