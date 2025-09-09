
(ns features.flex.service-areas.frontend.blocks.views
  (:require
    ["react"       :as react]
    ["@turf/turf"  :as turf]
    ["vanilla-calendar-pro" :refer [Calendar]]
    [reagent.core  :as reagent]
    [re-frame.core :as r]
   
    [ui.button :as button]
    [features.flex.shared.frontend.components.modal :as modal]
    [features.flex.service-areas.frontend.blocks.business-info :as business-info]
    [features.flex.service-areas.frontend.blocks.appointment :as appointment]
    [features.flex.service-areas.frontend.blocks.map :as service-map]
 
    [features.flex.service-areas.frontend.blocks.subs]
    [features.flex.service-areas.frontend.blocks.side-effects])) 


;; -----------------------------------------------------------------------------
;; ---- Notifications ----

(defn area-out-of-reach-notification []
  (r/dispatch [:notifications/notify! nil 
                (reagent/as-element 
                  [:div {:style {:padding "6px 12px"}}
                    [:p "This area is out of reach!"]
                    [:p "Please operate within the blue circle."]
                    [:a {:href ""
                         :style {:color "var(--color-primary)"}}
                       "Click here to find out more."]])]))

(defn area-is-reserved-notification [zip-id]
  (r/dispatch [:notifications/notify! nil 
                (reagent/as-element
                  [:div {:style {:padding "6px 12px"}}
                    [:p "Zip " [:b (str zip-id)] " is already taken."]
                    [:a {:href ""
                         :style {:color "var(--color-primary)"}}
                       "Click here to find out more."]])]))

(defn area-is-preserved-notification [zip-id]
  (r/dispatch [:notifications/notify! nil 
                (reagent/as-element 
                  [:div {:style {:padding "6px 12px"}}
                    [:p "Zip " [:b (str zip-id)] " has been preserved."]
                    [:a {:href ""
                         :style {:color "var(--color-primary)"}}
                       "Click here to find out more."]])]))

(defn area-is-disabled-notification [zip-id]
  (r/dispatch [:notifications/notify! nil 
                (reagent/as-element 
                  [:div {:style {:padding "6px 12px"}}
                    [:p "Area can't be reserved."]
                    [:a {:href ""
                         :style {:color "var(--color-primary)"}}
                       "Click here to find out more."]])]))

(defn area-is-own-notification [zip-id]
  (r/dispatch [:notifications/notify! nil 
                (reagent/as-element 
                  [:div {:style {:padding "6px 12px"}}
                    [:p "Yoo, you already own this, well done!🎉"]])]))

;; ---- Notifications ----
;; -----------------------------------------------------------------------------

(def MAX_REACH_DISTANCE 50)

(def MAP-STYLE "https://api.maptiler.com/maps/bf556e92-46e4-4422-8267-dfc9d658b63f/style.json?key=1WKoyb3HRdfzRHkA6ZAG")


;; Status Codes
;; Own       - (= workspace-id zip-code)
;; Reserved  - "r"
;; Disabled  - "d"
;; Preserved - "p"
;; Empty     - "e"

;; -----------------------------------------------------------------------------
;; ---- Utils ----


(defn- coords->point [^js coords]
  (turf/point #js[(.-lng coords) (.-lat coords)]))

(defn format-currency [n]
  (.format (js/Intl.NumberFormat. "en-US") n))

(defn- in-reach? [^js from ^js to]
  (> MAX_REACH_DISTANCE
     (turf/distance
      (coords->point from)
      (coords->point to)
      #js{"units" "kilometers"}))) 

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn- init-map [^js MAP]
  ^js (new MAP.Map
           #js{"container" "map-container"
               "style"     MAP-STYLE
               "center"    #js[-101.002121, 38.998909]
               "zoom"      3
               "geolocateControl"  false
               "navigationControl" false
               "attributionControl" false}))

(defn- init-reach-area [^js ws-coords]
  (turf/circle (coords->point ws-coords)
               MAX_REACH_DISTANCE #js{"units" "kilometers"}))

 
(defn- on-load [^js MAP {:keys [^js ws-coords]}]
  (.on MAP "load"
    (fn []
      (println "SETUP MAP")
      (.addSource MAP "zip-data"
        #js{"type"      "vector"
            "url"       "https://api.maptiler.com/tiles/countries/tiles.json?key=1WKoyb3HRdfzRHkA6ZAG"
            "promoteId" "code"})
      
      (.addSource MAP "reach-area-source"
                  #js{"type" "geojson"
                      "data" (init-reach-area ws-coords)})
      
      (.addLayer MAP 
                 #js{"id"     "reach-area-layer"
                     "type"   "fill"
                     "source" "reach-area-source"
                     "paint"  #js{"fill-color"         "#0594ff"
                                  "fill-opacity"       0
                                  "fill-outline-color" "red"}})
      
      (.addLayer MAP 
                 #js{"id"     "reach-area-line-layer"
                     "type"   "line"
                     "source" "reach-area-source"
                     "paint"  #js{"line-color" "#0594ff"
                                  "line-width" 2}})
            
      (.addLayer MAP
        #js{"id"           "my-layer"
            "source"       "zip-data"
            "source-layer" "postal"
            "type"         "fill"
            "minzoom"      7
            "maxzoom"      22
            "layout"      #js{"visibility" "visible"}
            "paint"       #js{"fill-opacity" #js["case"
                                                  #js["=="      #js["feature-state" "focus"]  true]       0.7
                                                  #js["boolean" #js["feature-state" "hover"]  false]      0.5
                                                  #js["=="      #js["feature-state" "status"] "own"]      0.5
                                                  #js["=="      #js["feature-state" "status"] "disabled"] 0.5
                                                  #js["=="      #js["feature-state" "status"] "reserved"] 0.5
                                                  #js["=="      #js["feature-state" "status"] "preserved"] 0.8
                                                  #js["=="      #js["feature-state" "status"] nil]        0.5
                                                  0.2]
                              "fill-color"   #js["case"
                                                  #js["==" #js["feature-state" "status"] "own"]         "pink"
                                                  #js["==" #js["feature-state" "status"] "disabled"]    "gray"
                                                  #js["==" #js["feature-state" "status"] "reserved"]    "yellow"
                                                  #js["==" #js["feature-state" "status"] "preserved"]   "orange"
                                                  #js["==" #js["feature-state" "status"] "empty"]       "#4cf470"
                                                  #js["==" #js["feature-state" "status"] nil]           "gray"
                                                  "gray"]}})           
      (.moveLayer MAP "my-layer" "River")

      (.addTo (.setLngLat ^js (new (.-Marker js/maptilersdk))
                          ws-coords)
              MAP)
      
      (.flyTo MAP #js{"center"    ws-coords
                      "zoom"      12
                      "essential" true})
      
      (let [geo-control ^js (new (.-GeocodingControl js/maptilersdkMaptilerGeocoder))]
                            ;;  #js{"country" #js["US"]})]
                                ;;  "types"   #js["address" "postal_code"]})]
       
        (.addControl MAP geo-control    
                         "top-left")
        
))))                    

(defn- get-postal-data-layer [^js MAP]
  (.querySourceFeatures MAP "zip-data" 
     #js{"sourceLayer" "postal"
         "filter"      #js["all",
                            #js["==" #js["get" "level_0"]
                                 "US"]]}))

(defn- set-zip-feature-state [^js MAP zip-id state-obj]
  (.setFeatureState MAP 
    #js{"source"      "zip-data"
        "sourceLayer" "postal"
        "id"          zip-id}
    state-obj))

(defn- update-cursor [^js MAP zcta-data zip-id]
  (let [canvas-style (.-style (.getCanvas MAP))]
    (if (= "empty" (get-in zcta-data [zip-id 1] false))
      (set! (.-cursor canvas-style) "pointer")
      (set! (.-cursor canvas-style) "auto"))))

(defn- handle-area-click-notifications [feature-state user-wsid zip-id]
  (if (= user-wsid (get-in @(r/subscribe [:db/get-in [:zcta-data]]) [zip-id 2] false))
    (area-is-own-notification zip-id)
    (case (.-status feature-state)
      "reserved"  (area-is-reserved-notification zip-id)
      "preserved" (area-is-preserved-notification zip-id)
      "disabled"  (area-is-disabled-notification zip-id)
      (area-is-disabled-notification zip-id))))

(defn- toggle-area-selection [zip-id zcta-data focus?]
  (if focus?
    (r/dispatch [:db/assoc-in [:setup :service-areas zip-id] (get zcta-data zip-id)])
    (r/dispatch [:db/dissoc-in [:setup :service-areas zip-id]])))

(defn- setup-sourcedata-handler [^js MAP zcta-data user-wsid]
  (.on MAP "sourcedata"
    (fn [_]
      (when (and (.getSource MAP "zip-data")
                 (.isSourceLoaded MAP "zip-data"))
        (let [postals (get-postal-data-layer MAP)]
          (doseq [postal-props postals]
            (when-let [[_ status wsid] (get zcta-data (.-id postal-props))]
              (set-zip-feature-state MAP (.-id postal-props)
                #js{"status" (if (= user-wsid wsid) "own" status)}))))))))

(defn- setup-mousemove-handler [^js MAP zip-id zcta-data]
  (.on MAP "mousemove" "my-layer"
    (fn [^js e]
      (when (> (-> e .-features .-length) 0)
        (when-not (nil? @zip-id)
          (update-cursor MAP zcta-data @zip-id)
          (set-zip-feature-state MAP @zip-id #js{"hover" false}))
        (reset! zip-id (-> e .-features first .-id))
        (set-zip-feature-state MAP @zip-id #js{"hover" true})))))

(defn- setup-mouseleave-handler [^js MAP zip-id]
  (.on MAP "mouseleave" "my-layer"
    (fn [_]
      (set-zip-feature-state MAP @zip-id #js{"hover" false})
      (reset! zip-id nil))))

(defn- handle-empty-area-click [^js MAP zip-id feature-state zcta-data]
  (let [focus? (not (.-focus feature-state))]
    (toggle-area-selection @zip-id zcta-data focus?)
    (set-zip-feature-state MAP @zip-id #js{"focus" focus?})))

(defn- setup-click-handler [^js MAP zip-id ws-props zcta-data user-wsid]
  (.on MAP "click" "my-layer"
    (fn [^js e]
      (if-not (in-reach? (:ws-coords ws-props) (.-lngLat e))
        (area-out-of-reach-notification)
        (when (> (-> e .-features .-length) 0)
          (let [feature-state (.getFeatureState MAP 
                                #js{"source" "zip-data"
                                    "sourceLayer" "postal"
                                    "id" @zip-id})]
            (if (= "empty" (.-status feature-state))
              (handle-empty-area-click MAP zip-id feature-state zcta-data)
              (handle-area-click-notifications feature-state user-wsid @zip-id))))))))

(defn- step-header [step-number title description]
  [:div {:style {:margin-bottom "20px"
                 :padding "20px"
                 :background "rgba(59, 130, 246, 0.05)"
                 :border "1px solid rgba(59, 130, 246, 0.1)"
                 :border-radius "12px"}}
   [:div {:style {:display "flex" :align-items "center" :margin-bottom "8px"}}
    [:div {:style {:width "32px"
                   :height "32px"
                   :background "#3b82f6"
                   :color "white"
                   :border-radius "50%"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :font-weight "600"
                   :margin-right "12px"}} step-number]
    [:h2 {:style {:margin "0"
                  :font-size "1.3rem"
                  :color "#1f2937"
                  :font-weight "600"}} title]]
   [:p {:style {:margin "0"
                :color "#6b7280"
                :font-size "0.95rem"
                :line-height "1.5"}} description]])


(defn- setup-map-event-handlers [^js MAP zip-id ws-props zcta-data user-wsid]
  (setup-sourcedata-handler MAP zcta-data user-wsid)
  (setup-mousemove-handler MAP zip-id zcta-data)
  (setup-mouseleave-handler MAP zip-id)
  (setup-click-handler MAP zip-id ws-props zcta-data user-wsid))

(defn- initialize-map [maptilersdk ws-props zcta-data user-wsid zip-id]
  (let [MAP ^js (init-map maptilersdk)]
    (on-load MAP ws-props)
    (setup-map-event-handlers MAP zip-id ws-props zcta-data user-wsid)
    MAP))

(defn- map-tiler []
  (let [zip-id    (reagent/atom nil)
        zcta-data @(r/subscribe [:db/get-in [:zcta-data]])]
    
    (react/useEffect
      (fn []
        (let [ws-props  {:user-wsid @(r/subscribe [:workspace/get-id])
                         :ws-coords (clj->js @(r/subscribe [:db/get-in [:setup :business-info :facility-address :coordinates]]))}
              user-wsid (get ws-props :user-wsid)]
          (initialize-map js/maptilersdk ws-props zcta-data user-wsid zip-id))
        (fn []))
      #js[])
    (let [total-population (reduce (fn [result [_ [population _]]] 
                                    (+ result population)) 
                                  0 @(r/subscribe [:db/get-in [:setup :service-areas]]))]
      [:div
       [:div {:style {:width "auto" :margin "auto" :position "relative"}}
        [:div {:id    "map-container" 
               :class "map-container"
               :style {:position "relative"}}]
        [service-map/service-area-panel]]
       [service-map/package-tiers total-population]])))


(defn- service-area-card [zip population]
  [:div {:key zip
         :style {:padding "6px 10px"
                 :background "white"
                 :border "1px solid #e5e7eb"
                 :border-radius "6px"
                 :text-align "center"}}
   [:div {:style {:font-size "0.75rem"
                  :font-weight "600"
                  :color "#374151"}} zip]
   [:div {:style {:font-size "0.65rem"
                  :color "#6b7280"}} (format-currency population)]])

(defn- service-areas-grid [service-areas]
  [:div {:style {:display "grid"
                 :grid-template-columns "repeat(auto-fill, minmax(80px, 1fr))"
                 :gap "8px"
                 :margin-bottom "12px"}}
   (map (fn [[zip [population _]]]
          [service-area-card zip population])
        service-areas)])

(defn- service-areas-total [service-areas total-population]
  [:div {:style {:padding "8px 12px"
                 :background "rgba(59, 130, 246, 0.1)"
                 :border-radius "6px"
                 :display "flex"
                 :justify-content "space-between"
                 :align-items "center"}}
   [:span {:style {:font-size "0.85rem"
                   :color "#374151"
                   :font-weight "500"}}
    (str (count service-areas) " areas selected")]
   [:span {:style {:font-size "0.9rem"
                   :color "#1f2937"
                   :font-weight "600"}}
    (str "Total: " (format-currency total-population))]])

(defn- service-areas-content [service-areas total-population]
  (if (empty? service-areas)
    [:p {:style {:color "#9ca3af"
                 :font-size "0.9rem"
                 :margin "0"}}
     "No service areas selected"]
    [:div
     [service-areas-grid service-areas]
     [service-areas-total service-areas total-population]]))

(defn- service-areas-section [service-areas total-population]
  [:div {:style {:margin-bottom "20px"}}
   [:h4 {:style {:margin "0 0 12px 0"
                 :font-size "1rem"
                 :font-weight "600"
                 :color "#374151"}}
    "Selected Service Areas"]
   [service-areas-content service-areas total-population]])

(defn- appointment-selected [selected-appointment]
  [:div {:style {:padding "12px"
                 :background "white"
                 :border "1px solid #10b981"
                 :border-radius "6px"
                 :color "#065f46"
                 :font-size "0.9rem"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :gap "8px"
                  :margin-bottom "4px"}}
    [:i {:class "fa-solid fa-calendar-check"
         :style {:color "#10b981"}}]
    [:span {:style {:font-weight "600"}}
     (:date selected-appointment)]]
   [:div {:style {:font-size "0.85rem"
                  :color "#374151"}}
    (str "Time: " (:time selected-appointment))]])

(defn- appointment-placeholder []
  [:div {:style {:padding "12px"
                 :background "white"
                 :border "1px solid #e5e7eb"
                 :border-radius "6px"
                 :color "#9ca3af"
                 :font-size "0.9rem"
                 :text-align "center"}}
   "Please select a date and time above"])

(defn- appointment-content [selected-appointment]
  (if selected-appointment
    [appointment-selected selected-appointment]
    [appointment-placeholder]))

(defn- appointment-section [selected-appointment]
  [:div {:style {:margin-bottom "30px"}}
   [:h4 {:style {:margin "0 0 12px 0"
                 :font-size "1rem"
                 :font-weight "600"
                 :color "#374151"}}
    "Selected Appointment"]
   [appointment-content selected-appointment]])

(defn- business-info-summary-section [business-info]
    [:div {:style {:margin-bottom "30px"}}
     [:h4 {:style {:margin "0 0 12px 0"
                   :font-size "1rem"
                   :font-weight "600"
                   :color "#374151"}}
      "Business Information"]
     [:div {:style {:background "white"
                    :border "1px solid #e5e7eb"
                    :border-radius "6px"
                    :padding "12px"}}
      [:div {:style {:display "grid"
                     :grid-template-columns "1fr 1fr"
                     :gap "8px"
                     :font-size "0.85rem"}}
       [:div 
        [:span {:style {:color "#6b7280" :font-weight "500"}} "Business: "]
        [:span {:style {:color "#374151"}} (or (:business-name business-info) "Not set")]]
       [:div
        [:span {:style {:color "#6b7280" :font-weight "500"}} "Owner: "]
        [:span {:style {:color "#374151"}} (or (:owner-name business-info) "Not set")]]
       [:div
        [:span {:style {:color "#6b7280" :font-weight "500"}} "Phone: "]
        [:span {:style {:color "#374151"}} (or (:phone-number business-info) "Not set")]]
       [:div
        [:span {:style {:color "#6b7280" :font-weight "500"}} "Email: "]
        [:span {:style {:color "#374151"}} (or (:email-address business-info) "Not set")]]]
      (when-let [facility-address (:facility-address business-info)]
        [:div {:style {:margin-top "8px"
                       :padding-top "8px"
                       :border-top "1px solid #f3f4f6"}}
         [:div {:style {:font-size "0.85rem"}}
          [:span {:style {:color "#6b7280" :font-weight "500"}} "Facility: "]
          [:span {:style {:color "#374151"}}
           (str (or (:address facility-address) "")
                (when (and (:address facility-address) (:city facility-address)) ", ")
                (or (:city facility-address) "")
                (when (and (:city facility-address) (:state facility-address)) ", ")
                (or (:state facility-address) "")
                (when (and (:state facility-address) (:zip-code facility-address)) " ")
                (or (:zip-code facility-address) ""))]]])]])

(defn- modal-summary []
  (let [service-areas @(r/subscribe [:db/get-in [:setup :service-areas]])
        total-population (reduce (fn [acc [_ [pop _]]] (+ acc pop)) 0 service-areas)
        selected-appointment @(r/subscribe [:db/get-in [:ui :service-areas :selected-appointment]])
        business-info @(r/subscribe [:db/get-in [:setup :business-info]])
        business-info-complete? @(r/subscribe [:setup.service-areas/business-info-complete?])]
    [:div {:style {:margin-top "30px"
                   :padding "20px"
                   :background "rgba(249, 250, 251, 0.8)"
                   :border-radius "12px"
                   :border "1px solid rgba(229, 231, 235, 0.5)"}}
     [appointment-section selected-appointment]
     [business-info-summary-section business-info]
     [service-areas-section service-areas total-population]]))



(defn- modal-content []
  (let [business-info-complete? @(r/subscribe [:setup.service-areas/business-info-complete?])
        reservation-ready? @(r/subscribe [:setup.service-areas/reservation-ready?])]
    [:div {:style {:padding "20px"}}
     ;; Always show business info form
     [business-info/form-section]
     
     ;; Always show appointment calendar section
     [appointment/calendar-component]
     
     [modal-summary]
     [:div {:style {:display "flex" :justify-content "center" :margin-top "30px"}}
      [button/view {:on-click #(do
                                 (modal/close!)
                                 (r/dispatch [:db/dissoc-in [:ui :service-areas :show-reservation-modal?]])
                                 ;; Add reservation logic here
                                 )
                    :style {:background "#10b981"
                            :color "white"
                            :border "none"
                            :padding "12px 24px"
                            :border-radius "8px"
                            :font-size "1rem"
                            :font-weight "600"
                            :cursor "pointer"
                            :transition "all 0.2s ease"}
                    :disabled (not reservation-ready?)}
       "Reserve Appointment"]]]))

(defn- reservation-modal []
  (let [show-modal? @(r/subscribe [:db/get-in [:ui :service-areas :show-reservation-modal?]])
        business-info-complete? @(r/subscribe [:setup.service-areas/business-info-complete?])]
    
    (react/useEffect
      (fn []
        (when show-modal?
          (modal/show! :reservation-modal
                       (if business-info-complete? 
                         "Schedule Your Appointment"
                         "Complete Setup")
                       [modal-content]))
        (when-not show-modal?
          (modal/close!)
          (r/dispatch [:db/dissoc-in [:ui :service-areas :show-reservation-modal?]]))
        
        (fn []))
      #js[show-modal?])
    
    nil))


(defn loading-spinner []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :justify-content "center"
                 :padding "60px 20px"
                 :background "rgba(255, 255, 255, 0.95)"
                 :backdrop-filter "blur(8px)"
                 :border-radius "12px"
                 :box-shadow "0 4px 20px rgba(0, 0, 0, 0.1)"
                 :margin-bottom "20px"}}
   [:div {:style {:width "40px"
                  :height "40px"
                  :border "3px solid #e3e3e3"
                  :border-top "3px solid #0594ff"
                  :border-radius "50%"
                  :animation "spin 1s linear infinite"
                  :margin-bottom "20px"}}]
   [:h3 {:style {:margin "0 0 10px 0"
                 :font-size "1.2rem"
                 :color "#333"
                 :font-weight "600"}} "Loading Service Areas"]
   [:p {:style {:margin "0"
                :font-size "0.9rem"
                :color "#666"
                :text-align "center"
                :line-height "1.4"}} 
    "We're preparing your interactive map with all available zip codes. This usually takes just a moment..."]
   [:style "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }"]])

(defn view []
  (let [map-ready? @(r/subscribe [:setup.service-areas/load-map?])
        loading?   @(r/subscribe [:setup.service-areas/zcta-data-loading?])]
    
    [:div
     ;; Descriptive text about service areas
     [:div {:style {:max-width "800px"
                    :margin "0 auto 30px auto"
                    :padding "24px"
                    :background "rgba(59, 130, 246, 0.05)"
                    :border "1px solid rgba(59, 130, 246, 0.1)"
                    :border-radius "12px"
                    :text-align "center"}}
      [:h2 {:style {:margin "0 0 16px 0"
                    :font-size "1.5rem"
                    :color "#1f2937"
                    :font-weight "700"}}
       "Expand Your Powder Coating Business"]
      [:p {:style {:margin "0 0 12px 0"
                   :font-size "1.05rem"
                   :line-height "1.6"
                   :color "#374151"}}
       "At Ironrainbow, you can purchase exclusive service areas to grow your powder coating business. Select ZIP codes on the interactive map below to define your coverage territory."]
      [:p {:style {:margin "0"
                   :font-size "0.95rem"
                   :line-height "1.5"
                   :color "#6b7280"}}
       "Your package tier is automatically determined by the total population of your selected areas. Higher population areas unlock premium packages with enhanced features and support."]]
     
     (cond
       loading? [loading-spinner]
       map-ready? [map-tiler]
       :else nil)
     
     ;; Always show the reserve button
     [:div {:style {:display "flex" :justify-content "center" :margin-top "30px"}}
      [button/view {:on-click #(r/dispatch [:db/assoc-in [:ui :service-areas :show-reservation-modal?] true])
                    :style {:background "#3b82f6"
                            :color "white"
                            :border "none"
                            :padding "12px 24px"
                            :border-radius "8px"
                            :font-size "1rem"
                            :font-weight "600"
                            :cursor "pointer"
                            :transition "all 0.2s ease"}
                    :disabled (empty? @(r/subscribe [:db/get-in [:setup :service-areas]]))}
       "Reserve Your Areas"]]
     
     [reservation-modal]
     [modal/view]]))