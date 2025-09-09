(ns features.flex.machines.frontend.machine
  (:require
   [features.flex.machines.frontend.request :as machines-request]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.text-field :as text-field]
   [ui.textarea :as textarea]
   [zero.frontend.react :as zero-react]))

;; --- Constants ---

(def machine-categories
  ["Washer" "Degreaser" "Dryer" "Blaster" "Oven" "Booth" "Sprayer" "Gun" 
   "Compressor" "AirDryer" "Polisher" "Grinder" "Buffer" "Sander" 
   "RimStraight" "RimWeld" "DiamondCut" "Balance" "TireMount" "DipTank" 
   "Lift" "PaintTrap" "CoolZone" "BatchRack" "QCStation" "Custom"])

(def energy-types
  ["Electrical" "Gas" "Compressed Air" "Water" "Other"])

(def machine-statuses
  ["Idle" "Active" "Maintenance" "Down"])

;; --- Helper Functions ---

(defn get-machine-data [machine-id]
  (if (= machine-id "new") 
    (r/dispatch [:db/assoc-in [:machine] {:name ""
                                          :description ""
                                          :category "Custom"
                                          :status "Idle"
                                          :energy_profiles []
                                          :amortization_time_based false
                                          :amortization_usage_based false
                                          :amortization_time_rate 0
                                          :amortization_usage_rate 0
                                          :maintenance_interval_days 30
                                          :wear_parts []
                                          :assigned_workstations []}])
    (let [workspace-id @(r/subscribe [:workspace/get-id])]
      (machines-request/get-machine 
       workspace-id 
       machine-id
       (fn [response]
         (println "Response:" response)
         (r/dispatch [:db/assoc-in [:machine] response]))))))

;; --- Form Components ---

(defn form-section [title children]
  [:div {:style {:margin-bottom "32px"}}
   [:h3 {:style {:font-size "1.25rem"
                 :font-weight 600
                 :color "#1f2937"
                 :margin-bottom "16px"
                 :padding-bottom "8px"
                 :border-bottom "2px solid #e5e7eb"}} title]
   [:div {:style {:display "flex"
                  :flex-direction "column"
                  :gap "16px"}} children]])

(defn form-row [& children]
  [:div {:style {:display "grid"
                 :grid-template-columns "repeat(auto-fit, minmax(250px, 1fr))"
                 :gap "16px"}} 
   children])

(defn select-field [label value options on-change]
  [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
   [:label {:style {:font-weight 500 :color "#374151" :font-size "0.875rem"}} label]
   [:select {:value value
             :on-change #(on-change (.. ^js % -target -value))
             :style {:padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :background "#ffffff"
                    :font-size "0.875rem"}}
    (for [option options]
      ^{:key option}
      [:option {:value option} option])]])

(defn number-field [label value on-change unit]
  [:div {:style {:display "flex" :flex-direction "column" :gap "4px"}}
   [:label {:style {:font-weight 500 :color "#374151" :font-size "0.875rem"}} label]
   [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
    [:input {:type "number"
             :value value
             :on-change #(on-change (.. ^js % -target -value))
             :style {:flex 1
                    :padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :font-size "0.875rem"}}]
    (when unit
      [:span {:style {:color "#6b7280" :font-size "0.875rem"}} unit])]])

(defn checkbox-field [label checked on-change]
  [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
   [:input {:type "checkbox"
            :checked checked
            :on-change #(on-change (.. % -target -checked))}]
   [:label {:style {:font-weight 500 :color "#374151" :font-size "0.875rem"}} label]])

;; --- Machine Details Section ---

(defn machine-details-section []
  (let [machine @(r/subscribe [:db/get-in [:machine]])]
    [form-section "Machine Details"
     [form-row
      [text-field/view {:label "Machine Name"
                        :value (:name machine)
                        :placeholder "e.g., Powder Booth A"
                        :on-change #(r/dispatch [:db/assoc-in [:machine :name] %])}]
      [select-field "Machine Category" 
                    (:category machine "Custom")
                    machine-categories
                    #(r/dispatch [:db/assoc-in [:machine :category] %])]]
     
     [form-row
      [select-field "Status"
                    (:status machine "Idle")
                    machine-statuses
                    #(r/dispatch [:db/assoc-in [:machine :status] %])]
      [text-field/view {:label "Location/Zone"
                        :value (:location machine)
                        :placeholder "e.g., Building A, Bay 3"
                        :on-change #(r/dispatch [:db/assoc-in [:machine :location] %])}]]
     
     [textarea/view {:label "Description"
                     :value (:description machine)
                     :placeholder "Detailed description of the machine and its purpose..."
                     :on-change #(r/dispatch [:db/assoc-in [:machine :description] %])
                     :rows 4}]]))

;; --- Energy Consumption Section ---

(defn energy-profile-card [profile idx]
  [:div {:style {:padding "16px"
                 :background "#f9fafb"
                 :border "1px solid #e5e7eb"
                 :border-radius "8px"
                 :margin-bottom "12px"}}
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "center"
                  :margin-bottom "12px"}}
    [:h5 {:style {:margin 0
                  :font-size "1rem"
                  :font-weight 600
                  :color "#374151"}}
     (str "Energy Profile " (inc idx))]
    [button/view {:mode :clear
                  :style {:color "#dc2626" :padding "4px 8px"}
                  :on-click #(r/dispatch [:db/assoc-in [:machine :energy_profiles] 
                                         (vec (concat (take idx (:energy_profiles @(r/subscribe [:db/get-in [:machine]]) []))
                                                     (drop (inc idx) (:energy_profiles @(r/subscribe [:db/get-in [:machine]]) []))))])}
     "🗑️ Remove"]]
   
   [form-row
    [select-field "Energy Type"
                  (:energy_type profile "Electrical")
                  energy-types
                  #(r/dispatch [:db/assoc-in [:machine :energy_profiles idx :energy_type] %])]
    [number-field "Consumption"
                  (:energy_consumption profile 0)
                  #(r/dispatch [:db/assoc-in [:machine :energy_profiles idx :energy_consumption] (js/parseFloat %)])
                  (case (:energy_type profile)
                    "Electrical" "kW/h"
                    "Gas" "BTU/h"
                    "Compressed Air" "CFM"
                    "units/h")]]])

(defn energy-consumption-section []
  (let [machine @(r/subscribe [:db/get-in [:machine]])
        energy-profiles (:energy_profiles machine [])]
    [form-section "Energy Consumption Profiles"
     [:div {:style {:display "flex" :flex-direction "column" :gap "12px"}}
      (for [[idx profile] (map-indexed vector energy-profiles)]
        ^{:key idx}
        [energy-profile-card profile idx])
      
      [button/view {:mode :outlined
                    :style {:margin-top "8px" :align-self "flex-start"}
                    :on-click #(r/dispatch [:db/assoc-in [:machine :energy_profiles] 
                                           (conj energy-profiles 
                                                 {:energy_type "Electrical"
                                                  :energy_consumption 0})])}
       "➕ Add Energy Profile"]
      
      (when (empty? energy-profiles)
        [:div {:style {:text-align "center"
                      :padding "20px"
                      :color "#6b7280"
                      :font-style "italic"}}
         "No energy profiles configured. Add one to get started."])]]))

;; --- Amortization Section ---

(defn amortization-section []
  (let [machine @(r/subscribe [:db/get-in [:machine]])]
    [form-section "Amortization Tracking"
     [:div {:style {:display "flex" :flex-direction "column" :gap "12px"}}
      [:h4 {:style {:font-size "1rem" :font-weight 500 :color "#374151"}} "Amortization Methods"]
      [checkbox-field "Time-based (per hour/month)"
                      (:amortization_time_based machine false)
                      #(r/dispatch [:db/assoc-in [:machine :amortization_time_based] %])]
      [checkbox-field "Usage-based (per kg powder, cycles, parts, etc.)"
                      (:amortization_usage_based machine false)
                      #(r/dispatch [:db/assoc-in [:machine :amortization_usage_based] %])]]
     
     [form-row
      [number-field "Time-based Rate"
                    (:amortization_time_rate machine 0)
                    #(r/dispatch [:db/assoc-in [:machine :amortization_time_rate] (js/parseFloat %)])
                    "$/hour"]
      [number-field "Usage-based Rate"
                    (:amortization_usage_rate machine 0)
                    #(r/dispatch [:db/assoc-in [:machine :amortization_usage_rate] (js/parseFloat %)])
                    "$/unit"]]
     
     [form-row
      [text-field/view {:label "Usage Unit (for usage-based)"
                        :value (:usage_unit machine)
                        :placeholder "e.g., kg powder, cycles, parts"
                        :on-change #(r/dispatch [:db/assoc-in [:machine :usage_unit] %])}]]]))

;; --- Maintenance Section ---

(defn maintenance-section []
  (let [machine @(r/subscribe [:db/get-in [:machine]])]
    [form-section "Maintenance & Tracking"
     [form-row
      [text-field/view {:label "Last Maintenance Date"
                        :type "date"
                        :value (:last_maintenance machine)
                        :on-change #(r/dispatch [:db/assoc-in [:machine :last_maintenance] %])}]
      [number-field "Maintenance Interval"
                    (:maintenance_interval_days machine 30)
                    #(r/dispatch [:db/assoc-in [:machine :maintenance_interval_days] (js/parseInt %)])
                    "days"]]
     
     [:div {:style {:margin-top "16px"}}
      [:h4 {:style {:font-size "1rem" :font-weight 500 :color "#374151" :margin-bottom "12px"}} 
       "Wear Parts / Replaceables"]
      [:div {:style {:display "flex" :flex-direction "column" :gap "8px"}}
       (let [wear-parts (:wear_parts machine [])]
         (for [[idx part] (map-indexed vector wear-parts)]
           ^{:key idx}
           [:div {:style {:display "flex" :gap "8px" :align-items "center" :padding "8px" :background "#f9fafb" :border-radius "6px"}}
            [:input {:type "text"
                     :value (:name part)
                     :placeholder "Part name"
                     :on-change #(r/dispatch [:db/assoc-in [:machine :wear_parts idx :name] (.. ^js % -target -value)])
                     :style {:flex 1 :padding "4px 8px" :border "1px solid #d1d5db" :border-radius "4px"}}]
            [:input {:type "text"
                     :value (:trigger part)
                     :placeholder "Replacement trigger"
                     :on-change #(r/dispatch [:db/assoc-in [:machine :wear_parts idx :trigger] (.. ^js % -target -value)])
                     :style {:flex 1 :padding "4px 8px" :border "1px solid #d1d5db" :border-radius "4px"}}]
            [button/view {:mode :clear
                          :style {:color "#dc2626" :padding "4px 8px"}
                          :on-click #(r/dispatch [:db/assoc-in [:machine :wear_parts] 
                                                  (vec (concat (take idx wear-parts) (drop (inc idx) wear-parts)))])}
             "🗑️"]]))
       
       [button/view {:mode :outlined
                     :style {:margin-top "8px" :align-self "flex-start"}
                     :on-click #(r/dispatch [:db/assoc-in [:machine :wear_parts] 
                                             (conj (:wear_parts machine []) {:name "" :trigger ""})])}
        "➕ Add Wear Part"]]]]))




(defn handle-submit []
  (let [machine-data @(r/subscribe [:db/get-in [:machine]])
        wsid @(r/subscribe [:workspace/get-id])
        machine-id @(r/subscribe [:db/get-in [:router :path-params :machine-id]])]
    (println "Submitting machine data:" machine-data)
    (if (= machine-id "new")
      (machines-request/create-machine 
       wsid 
       machine-data
       (fn [response]
         (println "Response:" response)
         (router/navigate! {:path (str "/flex/ws/" wsid "/machines")})))
      (machines-request/save-machine 
       wsid 
       (assoc machine-data :id machine-id)
       (fn [response]
         (println "Response:" response)
         (router/navigate! {:path (str "/flex/ws/" wsid "/machines")}))))))

(defn machine-form []
  [:div {:style {:max-width "600px"
                 :margin "0 auto"
                 :padding-top "32px"
                 :display "flex"
                 :flex-direction "column"
                 :gap "32px"}}
   [machine-details-section]
   [energy-consumption-section]
   [amortization-section]
   [maintenance-section]
   [:div {:style {:text-align "center"}}
    [button/view {:mode :filled
                  :color "var(--seco-clr)"
                  :on-click handle-submit}
     (let [machine-id @(r/subscribe [:db/get-in [:router :path-params :machine-id]])]
       (if (= machine-id "new") "Add Machine" "Save Changes"))]]])

(defn machine []
  (let [machine-id @(r/subscribe [:db/get-in [:router :path-params :machine-id]])
        wsid @(r/subscribe [:workspace/get-id])]
    [body/view
     {:title (if (= machine-id "new") "New Machine" "Edit Machine")
      :description "Create, edit, and remove machines."
      :title-buttons (list
                      (when (and machine-id (not= machine-id "new"))
                        ^{:key "delete"}
                        [button/view {:mode :outlined
                                     :color "var(--seco-clr)"
                                     :style {:fontWeight 500 
                                            :padding "8px 20px"}
                                     :on-click #(when (js/confirm "Are you sure you want to delete this machine?")
                                                 (machines-request/delete-machine 
                                                  wsid 
                                                  machine-id
                                                  (fn [_]
                                                    (router/navigate! {:path (str "/flex/ws/" wsid "/machines")}))))}
                         "Delete"])
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/machines")})}
                       "Back"])
      :body [machine-form]}]))

(defn view []
  (let [machine-id (r/subscribe [:db/get-in [:router :path-params :machine-id]])]
    (zero-react/use-effect
     {:mount (fn []
               (get-machine-data @machine-id))})
    [:div
     [machine]]))