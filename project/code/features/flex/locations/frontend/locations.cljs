(ns features.flex.locations.frontend.locations
  (:require
   [clojure.string :as str]
   [features.flex.locations.frontend.request :as locations-request]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.table.zero :as table]
   [zero.frontend.react :as react]))

;; --- Location Type Configuration ---

(def location-types
  {"workstation" {:icon "🛠️" :label "Workstation" :color "#3b82f6"}
   "rack" {:icon "📦" :label "Rack" :color "#10b981"}
   "area" {:icon "📍" :label "Area/Zone" :color "#8b5cf6"}
   "partner" {:icon "🔗" :label "Service Partner" :color "#f59e0b"}
   "inbound" {:icon "📦↔️" :label "Inbound/Outbound" :color "#6b7280"}})

;; --- Badge Components ---

(defn location-type-badge [type]
  (let [config (get location-types type {:icon "🔧" :label "Custom" :color "#6b7280"})]
    [:span {:style {:background (:color config)
                    :color "#ffffff"
                    :padding "4px 8px"
                    :border-radius "12px"
                    :font-size "0.75rem"
                    :font-weight "600"
                    :display "inline-flex"
                    :align-items "center"
                    :gap "4px"}}
     (:icon config) (:label config)]))

(defn status-badge [status]
  (let [config (case (str/lower-case (or status "active"))
                 "active" {:bg "#10b981" :icon "🟢" :label "Active"}
                 "idle" {:bg "#f59e0b" :icon "🟡" :label "Idle"}
                 "over-capacity" {:bg "#ef4444" :icon "🔴" :label "Over Capacity"}
                 "has-alerts" {:bg "#dc2626" :icon "⚠️" :label "Has Alerts"}
                 {:bg "#6b7280" :icon "🟢" :label "Active"})]
    [:span {:style {:background (:bg config)
                    :color "#ffffff"
                    :padding "4px 8px"
                    :border-radius "12px"
                    :font-size "0.75rem"
                    :font-weight "600"
                    :display "inline-flex"
                    :align-items "center"
                    :gap "4px"}}
     (:icon config) (:label config)]))

(defn capacity-display [location]
  (when (:capacity location)
    [:span {:style {:font-size "0.8rem"
                    :color "#6b7280"
                    :background "#f3f4f6"
                    :padding "2px 6px"
                    :border-radius "4px"}}
     (str "Cap: " (:capacity location))]))

(defn tags-display [tags]
  (when (seq tags)
    [:div {:style {:display "flex"
                   :flex-wrap "wrap"
                   :gap "4px"
                   :margin-top "4px"}}
     (map-indexed
      (fn [idx tag]
        ^{:key idx}
        [:span {:style {:background "#e0e7ff"
                        :color "#3730a3"
                        :padding "2px 6px"
                        :border-radius "4px"
                        :font-size "0.7rem"
                        :font-weight "500"}}
         tag])
      (take 3 tags))
     (when (> (count tags) 3)
       [:span {:style {:color "#6b7280"
                       :font-size "0.7rem"
                       :font-style "italic"}}
        (str "+" (- (count tags) 3) " more")])]))

(defn partner-indicator [is-partner]
  (when is-partner
    [:span {:style {:background "#fef3c7"
                    :color "#92400e"
                    :padding "2px 6px"
                    :border-radius "4px"
                    :font-size "0.7rem"
                    :font-weight "600"
                    :border "1px solid #f59e0b"}}
     "🔗 External"]))

;; --- Table Components ---

(defn actions-cell [wsid row]
  [:div {:style {:display "flex" :gap "8px"}}
   [button/view {:mode :clear
                 :style {:padding "4px 8px" 
                        :font-size "0.8rem"
                        :color "#3b82f6"}
                 :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/locations/" (:id row))})}
    "✏️ Edit"]
   [button/view {:mode :clear
                 :style {:padding "4px 8px" 
                        :font-size "0.8rem"
                        :color "#10b981"}
                 :on-click #(js/alert "View on Process Map - Coming Soon!")}
    "🗺️ Map"]])

(defn location-name-cell [location]
  [:div
   [:div {:style {:font-weight "600"
                  :color "#1f2937"
                  :margin-bottom "2px"}}
    (:name location)]
   (when (and (:description location) (not= (:description location) ""))
     [:div {:style {:font-size "0.8rem"
                    :color "#6b7280"
                    :font-style "italic"}}
      (:description location)])
   [tags-display (:tags location)]])

(defn location-details-cell [location]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "4px"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :gap "8px"
                  :flex-wrap "wrap"}}
    [location-type-badge (:type location)]
    [capacity-display location]
    [partner-indicator (:is_partner_location location)]]
   (when (:geo_info location)
     [:div {:style {:font-size "0.7rem"
                    :color "#6b7280"}}
      "📍 " (:geo_info location)])])

(defn location-table-row-element [style content]
  [:div {:style (merge {:display "grid"
                        :grid-template-columns "2fr 1fr 1fr 1fr 1.5fr"
                        :align-items "center"
                        :gap "12px"
                        :padding "12px 8px"
                        :border-bottom "1px solid #e5e7eb"
                        :hover {:background-color "#f9fafb"}} style)}
   content])

(defn location-table [locations wsid]
  [table/view
   {:rows locations
    :columns [:name :type_details :status :linked_info :actions]
    :labels {:name "Location"
             :type_details "Type & Details"
             :status "Status"
             :linked_info "Linked Info"
             :actions "Actions"}
    :column-elements {:name (fn [_item row] [location-name-cell row])
                      :type_details (fn [_item row] [location-details-cell row])
                      :status (fn [_item row] [status-badge (:status row)])
                      :linked_info (fn [_item row] 
                                     [:div {:style {:font-size "0.8rem" :color "#6b7280"}}
                                      (cond
                                        (seq (:linked_operators row)) 
                                        (str "👥 " (count (:linked_operators row)) " operators")
                                        
                                        (seq (:workstation_processes row))
                                        (str "⚙️ " (count (:workstation_processes row)) " processes")
                                        
                                        :else "—")])
                      :actions (fn [_item row] [actions-cell wsid row])}
    :row-element location-table-row-element}])

;; --- Stats Cards ---

(defn stats-card [title value icon color]
  [:div {:style {:background "#ffffff"
                 :border-radius "12px"
                 :padding "20px"
                 :box-shadow "0 1px 3px rgba(0, 0, 0, 0.1)"
                 :border-left (str "4px solid " color)}}
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "center"}}
    [:div
     [:h3 {:style {:margin "0 0 8px 0"
                   :color "#1f2937"
                   :font-size "2rem"
                   :font-weight "700"}} value]
     [:p {:style {:margin 0
                  :color "#6b7280"
                  :font-size "0.875rem"
                  :font-weight "500"}} title]]
    [:div {:style {:font-size "2rem"
                   :opacity "0.6"}} icon]]])

(defn stats-overview [locations]
  (let [total (count locations)
        by-type (group-by :type locations)
        active (count (filter #(= "active" (:status %)) locations))
        partners (count (filter :is_partner_location locations))]
    [:div {:style {:display "grid"
                   :grid-template-columns "repeat(auto-fit, minmax(200px, 1fr))"
                   :gap "20px"
                   :margin-bottom "32px"}}
     [stats-card "Total Locations" total "📍" "#3b82f6"]
     [stats-card "Workstations" (count (get by-type "workstation" [])) "🛠️" "#10b981"]
     [stats-card "Storage Racks" (count (get by-type "rack" [])) "📦" "#8b5cf6"]
     [stats-card "Active" active "🟢" "#10b981"]
     [stats-card "Partners" partners "🔗" "#f59e0b"]]))

;; --- Search and Filters ---

(defn search-and-filters [search-text set-search-text type-filter set-type-filter status-filter set-status-filter]
  [:div {:style {:display "flex"
                 :gap "16px"
                 :align-items "center"
                 :margin-bottom "24px"
                 :flex-wrap "wrap"}}
   ;; Search Input
   [:input {:type "text"
            :placeholder "Search locations..."
            :value search-text
            :on-change #(set-search-text (.. ^js % -target -value))
            :style {:padding "8px 12px"
                   :border "1px solid #d1d5db"
                   :border-radius "6px"
                   :background "#ffffff"
                   :min-width "300px"
                   :font-size "0.875rem"}}]
   
   ;; Type Filter
   [:select {:value type-filter
             :on-change #(set-type-filter (.. ^js % -target -value))
             :style {:padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :background "#ffffff"}}
    [:option {:value "all"} "All Types"]
    (map (fn [[type config]]
           ^{:key type}
           [:option {:value type} (str (:icon config) " " (:label config))])
         location-types)]
   
   ;; Status Filter
   [:select {:value status-filter
             :on-change #(set-status-filter (.. ^js % -target -value))
             :style {:padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :background "#ffffff"}}
    [:option {:value "all"} "All Status"]
    [:option {:value "active"} "🟢 Active"]
    [:option {:value "idle"} "🟡 Idle"]
    [:option {:value "over-capacity"} "🔴 Over Capacity"]
    [:option {:value "has-alerts"} "⚠️ Has Alerts"]]
   
   ;; Partner Toggle
   [:label {:style {:display "flex"
                    :align-items "center"
                    :gap "8px"
                    :font-size "0.875rem"
                    :color "#374151"}}
    [:input {:type "checkbox"
             :style {:margin 0}}]
    "Show only partners"]])

;; --- Filter Logic ---

(defn filter-locations [locations search-text type-filter status-filter]
  (let [search-term (str/lower-case search-text)]
    (->> locations
         (filter (fn [location]
                   (or (empty? search-term)
                       (str/includes? (str/lower-case (or (:name location) "")) search-term)
                       (str/includes? (str/lower-case (or (:description location) "")) search-term)
                       (some #(str/includes? (str/lower-case %) search-term) (:tags location)))))
         (filter (fn [location]
                   (or (= "all" type-filter)
                       (= type-filter (:type location)))))
         (filter (fn [location]
                   (or (= "all" status-filter)
                       (= (str/lower-case status-filter) (str/lower-case (or (:status location) "")))))))))

(defn locations-content []
  (let [[search-text set-search-text] (react/use-state "")
        [type-filter set-type-filter] (react/use-state "all")
        [status-filter set-status-filter] (react/use-state "all")
        wsid @(r/subscribe [:workspace/get-id])
        all-locations @(r/subscribe [:db/get-in [:locations :list] []])
        filtered-locations (filter-locations all-locations search-text type-filter status-filter)]
    [:div
     ;; Stats Overview
     [stats-overview all-locations]
     
     ;; Search and Filters
     [search-and-filters search-text set-search-text type-filter set-type-filter status-filter set-status-filter]
     
     ;; Locations Table
     [:div {:style {:background "#ffffff"
                    :border-radius "12px"
                    :box-shadow "0 1px 3px rgba(0, 0, 0, 0.1)"
                    :overflow "hidden"}}
      [:div {:style {:padding "20px 24px"
                     :border-bottom "1px solid #e5e7eb"
                     :background "#f9fafb"}}
       [:h2 {:style {:margin 0 :font-size "1.25rem" :font-weight "600" :color "#1f2937"}}
        (str "Locations (" (count filtered-locations) "/" (count all-locations) ")")]]
      [location-table filtered-locations wsid]]]))

(defn view []
  (let [wsid (r/subscribe [:workspace/get-id])]
    (react/use-effect 
     {:mount (fn [] 
               (when @wsid
                 (locations-request/get-locations
                  @wsid
                  (fn [response]
                    (println "Response:" response)
                    (r/dispatch [:db/assoc-in [:locations :list] response])))))})
    
    [body/view
     {:title "Locations"
      :description "Manage powder coating facility locations and storage areas."
      :title-buttons [button/view {:mode :filled
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" @wsid "/locations/new")})}
                      "Add Location"]
      :body [locations-content]}]))