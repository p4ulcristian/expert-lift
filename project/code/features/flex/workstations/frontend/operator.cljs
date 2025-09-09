(ns features.flex.workstations.frontend.operator
  (:require
   [features.flex.workstations.frontend.request :as workstations-request]
   [features.flex.shared.frontend.components.body :as edit-page]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [zero.frontend.react :as zero-react]
   [clojure.string :as clojure.string]))

(defn get-workstation-data [workstation-id]
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (workstations-request/get-workstation
     workspace-id workstation-id
     (fn [workstation]
       (r/dispatch [:db/assoc-in [:workstation] workstation])))
    (workstations-request/get-workstation-machines
     workspace-id workstation-id
     (fn [machines]
       (when machines
         (r/dispatch [:db/assoc-in [:workstation :machines] machines]))))))

(defn get-random-user []
  (let [names ["Alex Johnson" "Maria Garcia" "James Wilson" "Sarah Chen" "Mike Rodriguez" 
               "Emma Thompson" "David Kim" "Lisa Anderson" "Tom Miller" "Ana Silva"
               "Chris Taylor" "Nina Patel" "Rob Brown" "Kate Murphy" "Sam Lee"]
        colors ["#3b82f6" "#ef4444" "#10b981" "#f59e0b" "#8b5cf6" "#ec4899" "#14b8a6" "#f97316"]]
    {:name (rand-nth names)
     :color (rand-nth colors)}))

(defn user-avatar [user]
  (let [initials (-> (:name user)
                     (clojure.string/split #" ")
                     (->> (map first)
                          (take 2)
                          (apply str)
                          (clojure.string/upper-case)))]
    [:div {:style {:width "32px"
                   :height "32px"
                   :border-radius "50%"
                   :background (:color user)
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :color "white"
                   :font-size "0.75rem"
                   :font-weight "600"}}
     initials]))

(defn machine-card [machine]
  (let [user (get-random-user)]
    [:div {:style {:background "white"
                   :border "1px solid #e5e7eb"
                   :border-radius "12px"
                   :padding "20px"
                   :box-shadow "0 2px 8px rgba(0,0,0,0.04)"
                   :transition "box-shadow 0.2s ease"}}
     [:div {:style {:display "flex"
                    :justify-content "space-between"
                    :align-items "flex-start"
                    :margin-bottom "12px"}}
      [:h4 {:style {:margin 0
                    :color "#222"
                    :font-size "1.1rem"
                    :font-weight "600"}}
       (:name machine)]
      [:div {:style {:display "flex"
                     :align-items "center"
                     :gap "8px"}}
       [:div {:style {:width "10px"
                      :height "10px"
                      :border-radius "50%"
                      :background "#10b981"}}]
       [:span {:style {:color "#10b981"
                       :font-size "0.85rem"
                       :font-weight "500"}}
        "Online"]]]
     [:p {:style {:color "#666"
                  :margin "0 0 16px 0"
                  :font-size "0.9rem"
                  :line-height "1.4"}}
      (or (:description machine) "No description available")]
     
     ;; Current operator section
     [:div {:style {:display "flex"
                    :align-items "center"
                    :gap "12px"
                    :padding "12px"
                    :background "#f8fafc"
                    :border-radius "8px"
                    :margin-bottom "16px"}}
      [user-avatar user]
      [:div
       [:div {:style {:font-size "0.9rem"
                      :font-weight "500"
                      :color "#374151"}}
        (:name user)]
       [:div {:style {:font-size "0.8rem"
                      :color "#6b7280"}}
        "Current Operator"]]]
     
     [:div {:style {:display "flex"
                    :gap "12px"}}
      [button/view {:mode :outlined
                    :size :small
                    :color "var(--seco-clr)"}
       "Monitor"]]]))

(defn machines-grid []
  (let [machines @(r/subscribe [:db/get-in [:workstation :machines] []])]
    [:div
     [:div {:style {:background "#fff3cd"
                    :border "1px solid #ffeaa7"
                    :border-radius "8px"
                    :padding "12px 16px"
                    :margin-bottom "24px"
                    :display "flex"
                    :align-items "center"
                    :gap "8px"}}
      [:i {:class "fas fa-exclamation-triangle"
           :style {:color "#856404"}}]
      [:span {:style {:color "#856404"
                      :font-size "0.9rem"
                      :font-weight "500"}}
       "⚠️ Warning: This module needs to be thought through"]]
     [:h3 {:style {:margin "0 0 20px 0"
                   :color "#1f2937"
                   :font-size "1.3rem"
                   :font-weight "600"}}
      "Assigned Machines"]
     (if (seq machines)
       [:div {:style {:display "grid"
                      :grid-template-columns "repeat(auto-fill, minmax(300px, 1fr))"
                      :gap "20px"}}
        (for [machine machines]
          ^{:key (:machine/id machine)}
          [machine-card machine])]
       [:div {:style {:text-align "center"
                      :color "#6b7280"
                      :padding "40px 20px"
                      :font-style "italic"}}
        "No machines assigned to this workstation"])]))

(defn machines-content []
  [machines-grid])

(defn view []
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])
        wsid @(r/subscribe [:workspace/get-id])
        workstation-name (or @(r/subscribe [:db/get-in [:workstation :name]]) "Workstation")
        workstation-description @(r/subscribe [:db/get-in [:workstation :description]])]
    (zero-react/use-effect
     {:mount (fn []
               (get-workstation-data workstation-id))})
    [edit-page/view
     {:title (str workstation-name " - Operator View")
      :description (str "Monitor and manage machines on " workstation-name 
                       (when workstation-description (str ". " workstation-description)))
      :title-buttons (list
                     ^{:key "task-board"}
                     [button/view {:mode :filled
                                  :color "var(--seco-clr)"
                                  :style {:fontWeight 600 
                                         :padding "8px 20px"}
                                  :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" workstation-id "/task-board")})}
                      "Task Board"]
                     ^{:key "back"}
                     [button/view {:mode :outlined
                                  :color "var(--seco-clr)"
                                  :style {:fontWeight 500 
                                         :padding "8px 20px"}
                                  :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" workstation-id)})}
                      "Back"])
      :body [machines-content]}]))