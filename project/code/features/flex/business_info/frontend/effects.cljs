(ns features.flex.business-info.frontend.effects
  (:require
   [map.api      :as map]
   [re-frame.api :as r]))

;; -----------------------------------------------------------------------------
;; ---- Copy Address ----

(r/reg-event-fx
  :business-info/copy-address!
  (fn [{:keys [db]} [_]]
    (let [address (get-in db [:setup :business-info :mailing-address])]
      {:dispatch-n [[:x.db/set-item! [:setup :business-info :facility-address] address]
                    [:business-info/get-coordinate!]]})))

;; -----------------------------------------------------------------------------
;; ---- Get Coordinates ----

(r/reg-event-fx
 :business-info/get-coordinate!
 (fn [{:keys [db]} [_]]
   (let [{:keys [zip-code address city state]} (get-in db [:setup :business-info :facility-address])]
     (when (not-any? nil? [(seq address) (seq (str zip-code)) (seq city) (seq state)])
       {:fx [:app.setup.map/address->geolocation-center
             (str address ", " zip-code ", " city ", " state)
             (fn [^js e]
              (let [[lng lat] (aget e "features" 0 "center")]
                (r/dispatch [:x.db/set-item! [:setup :business-info :facility-address :coordinates]
                                             {:lng lng :lat lat}])))]}))))

;; -----------------------------------------------------------------------------
;; ---- Map Effect Handler ----

(defn- address->geolocation-center-f [address callback]
  (map/fetch-center {:address address} callback))

(r/reg-fx
 :app.setup.map/address->geolocation-center
 (fn [[address callback]]
   (address->geolocation-center-f address callback)))