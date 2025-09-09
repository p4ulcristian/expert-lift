(ns router.frontend.zero
  (:require
   [accountant.core  :as accountant]
   [clerk.core       :as clerk]
   [re-frame.core :as r]
   [reagent.core :as reagent]
   [reitit.frontend :as rf]
   [router.frontend.transform :as router.transform]))

(defonce state (reagent/atom {}))



(defn get-path-params [match]
  (-> match :parameters :path))

(defn init! [routes]
  (let [router (rf/router  (router.transform/routes->reitit-routes routes)
                           {:conflicts nil})]
    (accountant/configure-navigation!
     {:nav-handler
      (fn [path]
        (let [match  (rf/match-by-path router path)]

          (r/dispatch [:db/assoc-in [:router] {:path         path
                                               :path-params  (get-path-params match)
                                               :query-params (get-in match [:query-params])}]) 
          (reset! state match)
          (reagent/after-render clerk/after-render!)
          (clerk/navigate-page! path)))
      :path-exists?
      (fn [path]
        (boolean (rf/match-by-path router path)))})
    (accountant/dispatch-current!)))

(defn navigate! [{:keys [path]}]
  (accountant/navigate! path))

(defn navigate-and-reload! [{:keys [path]}] 
  (accountant/navigate! path)
  (.reload js/location))

;; -----------------------------------------------------------------------------
;; ---- Navigation Effects ----

(r/reg-fx
  :swap-query-params!
  (fn [{:keys [params]}]
    (let [current-path (get-in @state [:path])
          new-params   (->> params
                           (remove (comp nil? val))
                           (into {}))]
      (accountant/navigate! (if (seq new-params)
                              (str current-path "?" (accountant/map->params new-params))
                              current-path)))))

(r/reg-event-fx
  :router/swap-query-params!
  (fn [{:keys [params]}]
    {:swap-query-params! {:params params}}))


;; ---- Navigation Effects ----
;; -----------------------------------------------------------------------------
