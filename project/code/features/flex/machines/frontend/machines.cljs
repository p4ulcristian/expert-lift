(ns features.flex.machines.frontend.machines
  (:require
   [clojure.string :as str]
   [features.flex.machines.frontend.request :as machines-request]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.table.zero :as table]
   [zero.frontend.react :as react]))

;; --- Badge Components ---

(defn status-badge [status]
  (let [config (case (str/lower-case (or status "idle"))
                 "idle" {:bg "#10b981" :icon "🟢" :label "Idle"}
                 "active" {:bg "#ef4444" :icon "🔴" :label "Active"}
                 "maintenance" {:bg "#f59e0b" :icon "🟡" :label "Maintenance"}
                 "down" {:bg "#6b7280" :icon "⚫" :label "Down"}
                 {:bg "#6b7280" :icon "🟢" :label "Idle"})]
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

(defn energy-type-badge [energy-type]
  (let [config (case (str/lower-case (or energy-type "electrical"))
                 "electrical" {:bg "#3b82f6" :icon "⚡" :label "Electric"}
                 "gas" {:bg "#f59e0b" :icon "🔥" :label "Gas"}
                 "compressed-air" {:bg "#06b6d4" :icon "💨" :label "Air"}
                 "hydraulic" {:bg "#8b5cf6" :icon "🔧" :label "Hydraulic"}
                 {:bg "#6b7280" :icon "⚡" :label "Electric"})]
    [:span {:style {:background (:bg config)
                    :color "#ffffff"
                    :padding "4px 8px"
                    :border-radius "12px"
                    :font-size "0.75rem"
                    :font-weight "500"
                    :display "inline-flex"
                    :align-items "center"
                    :gap "4px"}}
     (:icon config) (:label config)]))

(defn maintenance-status [machine]
  (let [last-maint (:last_maintenance machine)
        due-date (:maintenance_due machine)
        is-overdue? (and due-date (< (js/Date. due-date) (js/Date.)))]
    [:span {:style {:color (cond
                             is-overdue? "#dc2626"
                             (and due-date (< (- (js/Date. due-date) (js/Date.)) (* 7 24 60 60 1000))) "#f59e0b"
                             :else "#10b981")
                    :font-size "0.8rem"
                    :font-weight "500"}}
     (cond
       is-overdue? "⚠️ Overdue"
       due-date (str "Due in " (Math/ceil (/ (- (js/Date. due-date) (js/Date.)) (* 24 60 60 1000))) "d")
       :else "✅ Up to date")]))

;; --- Table Components ---

(defn actions-cell [wsid row]
  [:div {:style {:display "flex" :gap "8px"}}
   [button/view {:mode :clear
                 :style {:padding "4px 8px" 
                        :font-size "0.8rem"
                        :color "#3b82f6"}
                 :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/machines/" (:id row))})}
    "✏️ Edit"]
   [button/view {:mode :clear
                 :style {:padding "4px 8px" 
                        :font-size "0.8rem"
                        :color "#10b981"}
                 :on-click #(js/alert "Maintenance scheduled!")}
    "🔧 Maintain"]
   [button/view {:mode :clear
                 :style {:padding "4px 8px" 
                        :font-size "0.8rem"
                        :color "#dc2626"}
                 :on-click #(when (js/confirm "Delete this machine?")
                             (println "Delete machine" (:id row)))}
    "🗑️ Delete"]])

(defn machine-category-display [category]
  (let [icon (case (str/lower-case (or category "custom"))
               "washer" "🧽"
               "oven" "🔥"
               "booth" "🎨"
               "dryer" "💨"
               "blaster" "💥"
               "sprayer" "🎯"
               "compressor" "🔧"
               "polisher" "✨"
               "grinder" "⚙️"
               "dipcank" "🛁"
               "🔧")]
    [:span {:style {:display "inline-flex"
                    :align-items "center"
                    :gap "4px"
                    :font-size "0.85rem"}}
     icon (or category "Custom")]))

(defn machine-table-row-element [style content]
  [:div {:style (merge {:display "grid"
                        :grid-template-columns "2fr 1fr 1fr 1fr 1fr 1fr 2fr"
                        :align-items "center"
                        :gap "12px"
                        :padding "12px 8px"
                        :border-bottom "1px solid #e5e7eb"
                        :hover {:background-color "#f9fafb"}} style)}
   content])

(defn machine-table [machines wsid]
  [table/view
   {:rows machines
    :columns [:name :category :status :energy_type :last_used :maintenance_status :actions]
    :labels {:name "Machine Name"
             :category "Type"
             :status "Status"
             :energy_type "Energy"
             :last_used "Last Used"
             :maintenance_status "Maintenance"
             :actions "Actions"}
    :column-elements {:status (fn [_item row] [status-badge (:status row)])
                      :energy_type (fn [_item row] [energy-type-badge (:energy_type row)])
                      :category (fn [_item row] [machine-category-display (:category row)])
                      :last_used (fn [_item row] 
                                   [:span {:style {:font-size "0.85rem" :color "#6b7280"}}
                                    (or (:last_used row) "Never")])
                      :maintenance_status (fn [_item row] [maintenance-status row])
                      :actions (fn [_item row] [actions-cell wsid row])}
    :row-element machine-table-row-element}])

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

(defn stats-overview [machines]
  (let [total (count machines)
        active (count (filter #(= "active" (:status %)) machines))
        maintenance (count (filter #(= "maintenance" (:status %)) machines))
        idle (count (filter #(= "idle" (:status %)) machines))]
    [:div {:style {:display "grid"
                   :grid-template-columns "repeat(auto-fit, minmax(250px, 1fr))"
                   :gap "20px"
                   :margin-bottom "32px"}}
     [stats-card "Total Machines" total "🏭" "#3b82f6"]
     [stats-card "Active" active "🔴" "#ef4444"]
     [stats-card "Idle" idle "🟢" "#10b981"]
     [stats-card "Maintenance" maintenance "🟡" "#f59e0b"]]))

;; --- Search and Filters ---

(defn search-and-filters [search-text set-search-text status-filter set-status-filter energy-filter set-energy-filter]
  [:div {:style {:display "flex"
                 :gap "16px"
                 :align-items "center"
                 :margin-bottom "24px"
                 :flex-wrap "wrap"}}
   ;; Search Input - using native HTML input for better control
   [:input {:type "text"
            :placeholder "Search machines..."
            :value search-text
            :on-change #(set-search-text (.. ^js % -target -value))
            :style {:padding "8px 12px"
                   :border "1px solid #d1d5db"
                   :border-radius "6px"
                   :background "#ffffff"
                   :min-width "300px"
                   :font-size "0.875rem"}}]
   
   [:select {:value status-filter
             :on-change #(set-status-filter (.. ^js % -target -value))
             :style {:padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :background "#ffffff"}}
    [:option {:value "all"} "All Status"]
    [:option {:value "idle"} "🟢 Idle"]
    [:option {:value "active"} "🔴 Active"]
    [:option {:value "maintenance"} "🟡 Maintenance"]
    [:option {:value "down"} "⚫ Down"]]
   
   [:select {:value energy-filter
             :on-change #(set-energy-filter (.. ^js % -target -value))
             :style {:padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :background "#ffffff"}}
    [:option {:value "all"} "All Energy Types"]
    [:option {:value "electrical"} "⚡ Electrical"]
    [:option {:value "gas"} "🔥 Gas"]
    [:option {:value "compressed-air"} "💨 Compressed Air"]
    [:option {:value "hydraulic"} "🔧 Hydraulic"]
    [:option {:value "electric"} "⚡ Electric"]
    [:option {:value "air"} "💨 Air"]]])

;; --- Filter Logic ---

(defn filter-machines [machines search-text status-filter energy-filter]
  (let [search-term (str/lower-case search-text)]
    (when (seq machines)
      (println "=== Filter Debug ===")
      (println "Status filter:" status-filter "| Energy filter:" energy-filter)
      (println "Sample machine data:")
      (doseq [machine (take 2 machines)]
        (println "  Machine:" (:name machine) "| Status:" (:status machine) "| Energy Type:" (:energy_type machine)))
      (println "Unique statuses in data:" (set (map :status machines)))
      (println "Unique energy types in data:" (set (map :energy_type machines))))
    (->> machines
         (filter (fn [machine]
                   (or (empty? search-term)
                       (str/includes? (str/lower-case (or (:name machine) "")) search-term)
                       (str/includes? (str/lower-case (or (:category machine) "")) search-term))))
         (filter (fn [machine]
                   (let [machine-status (str/lower-case (or (:status machine) ""))
                         filter-status (str/lower-case status-filter)
                         match? (or (= "all" status-filter)
                                   (= filter-status machine-status))]
                     (when (not= "all" status-filter)
                       (println "Status comparison:" filter-status "vs" machine-status "=" match?))
                     match?)))
         (filter (fn [machine]
                   (let [machine-energy (str/lower-case (or (:energy_type machine) ""))
                         filter-energy (str/lower-case energy-filter)
                         match? (or (= "all" energy-filter)
                                   (= filter-energy machine-energy))]
                     (when (not= "all" energy-filter)
                       (println "Energy comparison:" filter-energy "vs" machine-energy "=" match?))
                     match?))))))

;; --- Main View ---

(defn machines-content []
  (let [[search-text set-search-text] (react/use-state "")
        [status-filter set-status-filter] (react/use-state "all")
        [energy-filter set-energy-filter] (react/use-state "all")
        wsid @(r/subscribe [:workspace/get-id])
        all-machines @(r/subscribe [:db/get-in [:machines :list] []])
        filtered-machines (filter-machines all-machines search-text status-filter energy-filter)]
    [:div
     ;; Stats Overview
     [stats-overview all-machines]
     
     ;; Search and Filters
     [search-and-filters search-text set-search-text status-filter set-status-filter energy-filter set-energy-filter]
     
     ;; Alert Center (placeholder)
     (when (seq (filter #(= "maintenance" (:status %)) all-machines))
       [:div {:style {:background "#fef2f2"
                      :border "1px solid #fecaca"
                      :border-radius "8px"
                      :padding "16px"
                      :margin-bottom "24px"}}
        [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
         [:span {:style {:font-size "1.2rem"}} "⚠️"]
         [:p {:style {:margin 0 :color "#dc2626" :font-weight "600"}}
          (str (count (filter #(= "maintenance" (:status %)) all-machines)) 
               " machine(s) require maintenance attention")]]])
     
     ;; Machines Table
     [:div {:style {:background "#ffffff"
                    :border-radius "12px"
                    :box-shadow "0 1px 3px rgba(0, 0, 0, 0.1)"
                    :overflow "hidden"}}
      [:div {:style {:padding "20px 24px"
                     :border-bottom "1px solid #e5e7eb"
                     :background "#f9fafb"}}
       [:h2 {:style {:margin 0 :font-size "1.25rem" :font-weight "600" :color "#1f2937"}}
        (str "Machines (" (count filtered-machines) "/" (count all-machines) ")")]]
      [machine-table filtered-machines wsid]]]))

(defn view []
  (let [wsid (r/subscribe [:workspace/get-id])]
    (react/use-effect 
     {:mount (fn [] 
               (let [workspace-id @wsid]
                 (machines-request/get-machines 
                  workspace-id
                  (fn [response]
                    (println "Response:" response)
                    (r/dispatch [:db/assoc-in [:machines :list] response])))))})
    
    [body/view
     {:title "Machines"
      :description "Manage powder coating machines."
      :title-buttons [button/view {:mode :filled
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" @wsid "/machines/new")})}
                      "Add Machine"]
      :body [machines-content]}]))