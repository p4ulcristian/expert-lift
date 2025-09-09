
(ns features.customizer.panel.frontend.blocks.location
  (:require
    ["react" :as react]
    [re-frame.core :as r]
   
    [ui.button :as button]
    [ui.popup :as popup]
    [ui.select :as select]))

(r/reg-sub
  :customizer.location/selected-workshop-info
  (fn [db _]
    (let [selected-value (get-in db [:customizer/location :selected-workspace])
          workspaces (get-in db [:customizer/location :workspaces])]
      (when selected-value
        ;; Handle both cases: when it's a map (from select) or just ID (from localStorage)
        (let [workshop-id (if (map? selected-value) 
                            (:value selected-value) 
                            selected-value)]
          (if (map? selected-value)
            selected-value  ; If we have the map, return it directly
            (some (fn [workshop]     ; Otherwise find it in workspaces
                    (when (= (:value workshop) workshop-id)
                      workshop))
                  workspaces)))))))

(r/reg-sub
  :customizer.location/selected-workshop-name
  (fn [db _]
    (when-let [info @(r/subscribe [:customizer.location/selected-workshop-info])]
      (:label info))))

(r/reg-event-fx
  :temp/get-all-workspaces
  (fn [_ [_]]
    {:pathom/request {:query    [:temp/get-all-workspaces]
                      :callback (fn [response]
                                  (r/dispatch [:db/assoc-in [:customizer/location :workspaces] 
                                                            (map (fn [{:keys [id name facility_state facility_city]}]
                                                                   {:label (if (and facility_city facility_state)
                                                                             (str facility_city ", " facility_state)
                                                                             name)
                                                                    :value id
                                                                    :facility-state facility_state
                                                                    :facility-city facility_city})
                                                              (-> response :temp/get-all-workspaces))]))}}))

(r/reg-event-fx
  :customizer.location/select-workspace
  (fn [{:keys [db]} [_ workspace-data]]
    {:db (assoc-in db [:customizer/location :selected-workspace] workspace-data)
     :fx [[:dispatch [:customizer.location/save-to-storage workspace-data]]]}))

(r/reg-event-fx
  :customizer.location/save-to-storage
  (fn [_ [_ workspace-data]]
    (when workspace-data
      (let [value (if (map? workspace-data) (:value workspace-data) workspace-data)]
        (.setItem js/localStorage "customizer-selected-workspace" value)))
    nil))

(r/reg-event-fx
  :customizer.location/load-from-storage
  (fn [{:keys [db]} [_]]
    (when-let [saved-workspace (.getItem js/localStorage "customizer-selected-workspace")]
      {:db (assoc-in db [:customizer/location :selected-workspace] saved-workspace)})))

(r/reg-event-fx
  :customizer.location/update-zip-code
  (fn [{:keys [db]} [_ zip-code]]
    {:db (-> db
             (assoc-in [:customizer/location :zip-code] zip-code)
             (update-in [:customizer/location] dissoc :error))}))

(defn validate-zip-code [zip-code]
  "Validates American zip codes (5 digits or 5+4 format)"
  (when zip-code
    (re-matches #"^\d{5}(-\d{4})?$" zip-code)))

(r/reg-event-fx
  :customizer.location/set-loading
  (fn [{:keys [db]} [_ loading?]]
    {:db (assoc-in db [:customizer/location :loading] loading?)}))

(r/reg-event-fx
  :customizer.location/find-closest-workspace
  (fn [{:keys [db]} [_ zip-code]]
    {:db (assoc-in db [:customizer/location :loading] true)
     :pathom/request {:query [:customizer/find-closest-workspace]
                      :initial-data {:zip-code zip-code}
                      :callback (fn [response]
                                  (r/dispatch [:customizer.location/closest-workspace-found response]))}}))

(r/reg-event-fx
  :customizer.location/closest-workspace-found
  (fn [{:keys [db]} [_ response]]
    (let [result (:customizer/find-closest-workspace response)]
      (js/console.log "Frontend received result:" (clj->js result))
      {:db (assoc-in db [:customizer/location :loading] false)
       :fx (if (and result (:error-type result))
             ;; Error from backend - store it to show in UI
             [[:dispatch [:db/assoc-in [:customizer/location :error] result]]]
             ;; Success or other cases
             (if (and result (:workspace-id result))
               [[:dispatch [:customizer.location/select-closest-workspace (:workspace-id result)]]]
               [[:dispatch [:notifications/error! "find-closest" "No nearby locations found for this zip code"]]]))})))

(r/reg-event-fx
  :customizer.location/select-closest-workspace
  (fn [{:keys [db]} [_ workspace-id]]
    (let [workspaces (get-in db [:customizer/location :workspaces])
          closest-workspace (some (fn [ws] (when (= (:value ws) workspace-id) ws)) workspaces)]
      (if closest-workspace
        {:db (assoc-in db [:customizer/location :selected-workspace] closest-workspace)
         :fx [[:dispatch [:customizer.location/save-to-storage closest-workspace]]
              [:dispatch [:db/dissoc-in [:customizer/location :state]]]
              [:dispatch [:notifications/success! "find-closest" "Found closest location!"]]]}
        {:fx [[:dispatch [:notifications/error! "find-closest" "Location found but not available in list"]]]}))))

(defn popup []
  (let [state @(r/subscribe [:db/get-in [:customizer/location :state]])]
    (react/useEffect
      (fn []
        (when state
          (r/dispatch [:temp/get-all-workspaces]))
        (fn []))
      #js[state])
    
    [popup/view {:id       "customizer--location-popup"
                 :state    state
                 :on-close #(r/dispatch [:db/dissoc-in [:customizer/location :state]])
                 :style    {:padding          "15px"
                            :min-width        "300px"
                            :background-color "rgb(81 82 83)"}}
      [:div 
        [:p {:style {:margin-bottom "10px"
                     :text-align "center"}} "Choose your zip code"]
        
        ;; Zip Code Input
        (let [zip-code @(r/subscribe [:db/get-in [:customizer/location :zip-code]])
              loading? @(r/subscribe [:db/get-in [:customizer/location :loading]])
              error @(r/subscribe [:db/get-in [:customizer/location :error]])
              is-valid-zip? (validate-zip-code zip-code)]
          [:div {:style {:margin-bottom "15px"}}
           [:input {:type "text"
                    :placeholder "Enter zip code (e.g. 90210 or 90210-1234)"
                    :value (or zip-code "")
                    :on-change #(r/dispatch [:customizer.location/update-zip-code (.. ^js % -target -value)])
                    :style {:width "100%"
                            :padding "10px"
                            :border "1px solid #ccc"
                            :border-radius "4px"
                            :font-size "14px"
                            :box-sizing "border-box"
                            :background-color "white"
                            :color "#333"}}]
           ;; Error display area - reserved space to prevent UI jumping
           [:div {:style {:min-height "20px"
                          :margin-top "5px"}}
            ;; Format validation error
            (when (and zip-code (not is-valid-zip?))
              [:div {:style {:color "#ff4444"
                             :font-size "12px"}}
               "Invalid zip code format"])
            
            ;; Backend validation error - show after button click
            (when error
              [:div {:style {:color "#ff4444"
                             :font-size "12px"}}
               "This zip code does not exist."])]
           
           ;; Submit Button
           [:button {:type "button"
                     :disabled (or loading? (not is-valid-zip?) (not zip-code))
                     :on-click #(r/dispatch [:customizer.location/find-closest-workspace zip-code])
                     :style {:width "100%"
                             :margin-top "10px"
                             :padding "10px"
                             :background-color (if (or loading? (not is-valid-zip?) (not zip-code))
                                                 "#ccc"
                                                 "#007bff")
                             :color "white"
                             :border "none"
                             :border-radius "4px"
                             :font-size "14px"
                             :cursor (if (or loading? (not is-valid-zip?) (not zip-code))
                                       "not-allowed"
                                       "pointer")}}
            (if loading?
              "Finding Closest Location..."
              "Find Closest Location")]])
        
        (let [selected-id @(r/subscribe [:db/get-in [:customizer/location :selected-workspace]])
              workspaces @(r/subscribe [:db/get-in [:customizer/location :workspaces]])
              ;; Convert ID to full map format for the select component
              selected-value (if (map? selected-id)
                               selected-id
                               (some (fn [ws] 
                                       (when (= (:value ws) selected-id)
                                         ws))
                                     workspaces))]
          [select/view {:title   "Workshops (temporary)"
                        :options workspaces
                        :value selected-value
                        :on-select #(r/dispatch [:customizer.location/select-workspace %])
                        :override {:style   {:width "100%"}}
                        :dropdown-override {:style {:max-height "300px"
                                                    :overflow "auto"}}}])]]))
                                 

(defn button []
  (let [shop-address @(r/subscribe [:db/get-in [:shop :facility-address]])
        selected-workshop-name @(r/subscribe [:customizer.location/selected-workshop-name])]
    [:<>
      [popup]
      [button/view {:id       "customizer--location-button"
                    :class    "header--grow-button"
                    :color    "rgba(255, 255, 255, 0.1)"
                    :data-open (not (nil? shop-address))
                    :on-click  #(r/dispatch [:db/assoc-in [:customizer/location :state] true])}
        [:i {:class ["fa-solid" "fa-location-dot"]}]
        [:span {:style {:overflow "hidden"
                        :text-overflow "ellipsis"
                        :white-space "nowrap"
                        :max-width "120px"
                        :display "inline-block"}}
         (or selected-workshop-name "Locations")]]]))
