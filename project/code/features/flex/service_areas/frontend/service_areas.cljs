
(ns features.flex.service-areas.frontend.service-areas
  (:require
    [features.flex.service-areas.frontend.request :as service-areas-request]
    [features.flex.service-areas.frontend.blocks.views :as blocks]
    [features.flex.shared.frontend.components.body :as body]
    [re-frame.core :as r]
    [zero.frontend.react :as zero-react]))

(defn status-code->string [status-code]
  (case status-code
    "d" "disabled"
    "r" "reserved"
    "p" "preserved"
    "e" "empty"
    "disabled"))

(defn transform-zcta-data [response]
  (let [{:keys [data]} (-> response :service-areas/get-service-areas)  ]
    
    (reduce (fn [acc [k v]]
              (let [[population _ _ workspace-id status] v] 
                (assoc acc (name k)
                       [population
                        (status-code->string status) 
                        workspace-id])))
            {}
            data)))
     

(defn load-test-data []
  (println "load-test-data" @(r/subscribe [:workspace/get-id]))
  (r/dispatch [:db/assoc-in [:setup :business-info :facility-address :coordinates] {:lng -122.4194 :lat 37.7749}])
  (r/dispatch [:db/assoc-in [:ui :service-areas :zcta-loading?] true])
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (service-areas-request/get-service-areas
     workspace-id
     (fn [response]
       (let [data (transform-zcta-data response)]
         (r/dispatch [:db/assoc-in [:zcta-data] data])
         (r/dispatch [:db/assoc-in [:ui :service-areas :zcta-loading?] false]))))))

(defn view []
  (zero-react/use-effect 
    {:mount (fn []
              (r/dispatch [:app.setup.map/set-maptiler-api-key!])
              (load-test-data))})
 
  [body/view
   {:title "Service Area"
    :description "Define geographic coverage areas for powder coating services."
    :body [:div {:style {:max-width "600px" :margin "0 auto"}}
           [blocks/view]]}])