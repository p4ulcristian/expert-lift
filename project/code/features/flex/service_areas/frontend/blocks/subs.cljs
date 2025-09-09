
(ns features.flex.service-areas.frontend.blocks.subs
  (:require
    [re-frame.core :as r]))

(r/reg-sub
  :setup.service-areas/coordinates-ready?
  (fn [db [_]]
    (not-empty (get-in db [:setup :business-info :facility-address :coordinates]))))

(r/reg-sub
  :setup.service-areas/zcta-data-loading?
  (fn [db [_]]
    (get-in db [:ui :service-areas :zcta-loading?] false)))

(r/reg-sub
  :setup.service-areas/load-map?
  (fn [db [_]]
    (let [coordinates   (get-in db [:setup :business-info :facility-address :coordinates])
          service-areas (get-in db [:zcta-data])]
      (and (not-empty coordinates)
           (not-empty service-areas)))))

(r/reg-sub 
  :setup.service-areas/valid?
  (fn [db [_]]
    (not-empty (get-in db [:setup :service-areas]))))

(defn business-info-complete? [business-info]
  "Check if business info has all required fields for appointment booking"
  (and business-info
       (not-empty (:business-name business-info))
       (not-empty (:owner-name business-info))
       (not-empty (:phone-number business-info))
       (not-empty (:email-address business-info))
       (let [facility-address (:facility-address business-info)]
         (and facility-address
              (not-empty (:address facility-address))
              (not-empty (:city facility-address))
              (not-empty (:state facility-address))
              (not-empty (:zip-code facility-address))))))

(r/reg-sub
  :setup.service-areas/business-info-complete?
  (fn [db [_]]
    (let [business-info (get-in db [:setup :business-info])]
      (business-info-complete? business-info))))

(r/reg-sub
  :setup.service-areas/reservation-ready?
  (fn [db [_]]
    (and (business-info-complete? (get-in db [:setup :business-info]))
         (not-empty (get-in db [:setup :service-areas]))
         (not-empty (get-in db [:ui :service-areas :selected-appointment])))))