(ns features.flex.service-areas.frontend.blocks.side-effects 
  (:require [re-frame.core :as r]
            [features.flex.service-areas.frontend.request :as service-areas-request]
            [ui.notification]))

(defn set-maptiler-api-key []
  (set! (-> js/maptilersdk .-config .-apiKey) "sCrFAhxFG901juHJICOy"))


(defn address->geolocation-center [[address func]]
  (-> (.forward (.-geocoding js/maptilersdk)
                address
                #js{"types"      #js["address" "postal_code"]
                    "limit"      5
                    "fuzzyMatch" true})
      
      (.then func)
                       
      (.catch (fn [e]
                (.log js/console "failed address->geolocation-center" e)))))

(r/reg-fx :app.setup.map/address->geolocation-center address->geolocation-center)

(r/reg-fx :app.setup.map/set-maptiler-api-key! set-maptiler-api-key)

(r/reg-event-fx :app.setup.map/address->geolocation-center
  (fn [_ [_ address callback]]
    {:app.setup.map/address->geolocation-center [address callback]}))

(r/reg-event-fx :app.setup.map/set-maptiler-api-key!
  (fn [_ _]
    {:app.setup.map/set-maptiler-api-key! []}))

(r/reg-event-fx :service-areas/save-business-info
  (fn [_ [_ business-info-data set-saving-fn]]
    {:dispatch [:notifications/loading! "save-business-info" "Saving business information..."]
     :fx [[:dispatch [:service-areas/save-business-info-request business-info-data set-saving-fn]]]}))

(r/reg-event-fx :service-areas/save-business-info-request
  (fn [_ [_ business-info-data set-saving-fn]]
    {:fx [[:dispatch-later {:ms 10
                           :dispatch [:service-areas/make-save-request business-info-data set-saving-fn]}]]}))

(r/reg-event-fx :service-areas/make-save-request
  (fn [_ [_ business-info-data set-saving-fn]]
    (let [workspace-id @(r/subscribe [:workspace/get-id])]
      (service-areas-request/save-business-info
       workspace-id
       business-info-data
       (fn [response]
         (when set-saving-fn (set-saving-fn false))
         (if (:error response)
           (r/dispatch [:notifications/error! "save-business-info" "Failed to save business information" 3000])
           (do
             (r/dispatch [:notifications/success! "save-business-info" "Business information saved successfully!" 3000])
             (r/dispatch [:db/assoc-in [:setup :business-info] business-info-data])
             ;; Trigger coordinate lookup for facility address
             (when-let [facility-address (:facility-address business-info-data)]
               (r/dispatch [:workspace.setup.business-info.facility-address/get-coordinate!])))))))
    {}))

(r/reg-event-fx :service-areas/fetch-business-info
  (fn [_ _]
    {:dispatch [:notifications/loading! "fetch-business-info" "Loading business information..."]
     :fx [[:dispatch [:service-areas/fetch-business-info-request]]]}))

(r/reg-event-fx :service-areas/fetch-business-info-request
  (fn [_ _]
    (let [workspace-id @(r/subscribe [:workspace/get-id])]
      (service-areas-request/get-business-info
       workspace-id
       (fn [response]
         (js/console.log "Business info fetch response:" (clj->js response))
         (if (:error response)
           (do
             (js/console.log "Error fetching business info")
             (r/dispatch [:notifications/error! "fetch-business-info" "Failed to load business information" 3000])
             (r/dispatch [:db/assoc-in [:ui :service-areas :business-info-loading?] false]))
           (let [business-info (:business-info/get response)]
             (js/console.log "Business info from server:" (clj->js business-info))
             (if business-info
               (do
                 (r/dispatch [:db/assoc-in [:setup :business-info] business-info])
                 (r/dispatch [:notifications/success! "fetch-business-info" "Business information loaded" 2000]))
               (js/console.log "No business info returned from server"))
             (r/dispatch [:db/assoc-in [:ui :service-areas :business-info-loading?] false]))))))
    {}))