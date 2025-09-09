(ns features.flex.workstations.frontend.workstations
  (:require
   [features.flex.workstations.frontend.request :as workstations-request]
   [features.flex.workstations.frontend.events]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [zero.frontend.react :as zero-react]))

(defn get-workstation-current-batches [workstation-id]
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (workstations-request/get-batches-with-current-step-on-workstation
     workspace-id workstation-id
     (fn [batches]
       (when batches
         (r/dispatch [:db/assoc-in [:flex/workstations :current-batches workstation-id] batches]))))))

(defn get-workstations-data []
  (let [wsid @(r/subscribe [:workspace/get-id])]
    (r/dispatch [:flex/workstations wsid])
    (workstations-request/get-workstations wsid
      (fn [workstations]
        (doseq [workstation workstations]
          (get-workstation-current-batches (:workstation/id workstation)))))))


;; Small workstation card components
(defn workstation-title [workstation]
  [:h3 {:style {:font-size "1.25rem"
                :font-weight 600
                :color "#222"
                :margin "0 0 4px 0"}}
   (:workstation/name workstation)])

(defn workstation-description [workstation]
  [:p {:style {:color "#666"
               :margin 0
               :font-size "0.875rem"}}
   (:workstation/description workstation)])

(defn workstation-edit-button [workstation wsid]
  [button/view {:mode :outlined
                :color "var(--seco-clr)"
                :style {:fontWeight 500 
                       :padding "6px 16px"}
                :on-click #(do
                            (.stopPropagation ^js %)
                            (router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" (:workstation/id workstation))}))}
   "Edit"])

(defn workstation-header [workstation wsid]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :align-items "flex-start"}}
   [:div
    [workstation-title workstation]
    [workstation-description workstation]]
   [:div {:style {:display "flex" :gap "8px"}}
    [workstation-edit-button workstation wsid]]])

(defn section-title [title]
  [:h4 {:style {:font-size "1rem"
                :font-weight 500
                :color "#333"
                :margin "0 0 8px 0"}}
   title])

(defn machine-card [machine]
  [:div {:style {:background "#f8f9fa"
                 :border "1px solid #eee"
                 :border-radius "6px"
                 :padding "12px"
                 :display "flex"
                 :flex-direction "column"
                 :gap "4px"}}
   [:div {:style {:font-weight "500"
                  :color "#222"
                  :font-size "0.9rem"}}
    (:name machine)]
   [:div {:style {:color "#666"
                  :font-size "0.8rem"}}
    (:description machine)]])

(defn machines-grid [machines]
  [:div {:style {:display "grid"
                 :grid-template-columns "repeat(auto-fill, minmax(180px, 1fr))"
                 :gap "12px"}}
   (for [machine machines]
     ^{:key (:machine/id machine)}
     [machine-card machine])])

(defn workstation-machines-section [workstation]
  (when (seq (:machines workstation))
    [:div
     [section-title "Assigned Machines"]
     [machines-grid (:machines workstation)]]))

(defn process-card [process]
  [:div {:style {:background "#e8f4fd"
                 :border "1px solid #bee5eb"
                 :border-radius "6px"
                 :padding "12px"
                 :display "flex"
                 :flex-direction "column"
                 :gap "4px"}}
   [:div {:style {:font-weight "500"
                  :color "#222"
                  :font-size "0.9rem"}}
    (:name process)]
   [:div {:style {:color "#666"
                  :font-size "0.8rem"}}
    (:description process)]])

(defn processes-grid [processes]
  [:div {:style {:display "grid"
                 :grid-template-columns "repeat(auto-fill, minmax(180px, 1fr))"
                 :gap "12px"}}
   (for [process processes]
     ^{:key (:process/id process)}
     [process-card process])])

(defn workstation-processes-section [workstation]
  (when (seq (:processes workstation))
    [:div
     [section-title "Assigned Processes"]
     [processes-grid (:processes workstation)]]))

(defn batch-name-and-quantity [batch]
  [:div {:style {:display "flex" :justify-content "space-between" :align-items "flex-start"}}
   [:div {:style {:font-weight "500"
                  :color "#222"
                  :font-size "0.9rem"}}
    (:batch_name batch)]
   [:span {:style {:background-color "#fd79a8"
                   :color "white"
                   :padding "2px 6px"
                   :border-radius "4px"
                   :font-size "0.75rem"
                   :font-weight "500"}}
    (str "Qty: " (:quantity batch))]])

(defn batch-part-image [batch]
  (when (:part_picture_url batch)
    [:img {:src (:part_picture_url batch)
           :alt (:part_name batch)
           :style {:width "40px"
                  :height "40px"
                  :object-fit "cover"
                  :border-radius "4px"
                  :border "1px solid #ddd"}}]))

(defn batch-part-name [batch]
  (when (:part_name batch)
    [:div {:style {:color "#666"
                   :font-size "0.8rem"}}
     (:part_name batch)]))

(defn batch-color-indicator [batch]
  (when (and (:color_name batch) (:color_basecolor batch))
    [:div {:style {:display "flex" :align-items "center" :gap "6px"}}
     [:div {:style {:width "12px"
                    :height "12px"
                    :border-radius "50%"
                    :background-color (:color_basecolor batch)
                    :border "1px solid #ddd"}}]
     [:span {:style {:color "#666"
                     :font-size "0.8rem"}}
      (:color_name batch)]]))

(defn batch-current-step [batch]
  [:div {:style {:color "#e67e22"
                 :font-size "0.8rem"
                 :font-weight "500"
                 :margin-top "4px"}}
   (str "Step " (:current_step batch) ": " (:process_name batch))])

(defn current-batch-card [batch]
  [:div {:style {:background "#e0f7f7"
                 :border "1px solid #4dd0e1"
                 :border-radius "6px"
                 :padding "12px"
                 :display "flex"
                 :flex-direction "column"
                 :gap "4px"}}
   [batch-name-and-quantity batch]
   (when (or (:part_picture_url batch) (:part_name batch))
     [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
      [batch-part-image batch]
      [:div
       [batch-part-name batch]]])
   [batch-color-indicator batch]
   [batch-current-step batch]])

(defn current-batches-grid [batches]
  [:div {:style {:display "grid"
                 :grid-template-columns "repeat(auto-fill, minmax(250px, 1fr))"
                 :gap "12px"}}
   (for [batch batches]
     ^{:key (:batch_id batch)}
     [current-batch-card batch])])

(defn workstation-current-batches-section [workstation]
  (let [current-batches @(r/subscribe [:db/get-in [:workstations :current-batches (:workstation/id workstation)] []])]
    (when (seq current-batches)
      [:div
       [section-title "Current Step Batches"]
       [current-batches-grid current-batches]])))

(defn workstation-card [workstation]
  (let [wsid @(r/subscribe [:workspace/get-id])]
    [:div {:style {:background "#fff"
                   :border-radius "8px"
                   :box-shadow "0 2px 8px rgba(0,0,0,0.05)"
                   :padding "20px"
                   :display "flex"
                   :flex-direction "column"
                   :gap "16px"
                   :cursor "pointer"
                   :transition "box-shadow 0.2s ease"}
           :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" (:workstation/id workstation) "/task-board")})}
     [workstation-header workstation wsid]
     [workstation-machines-section workstation]
     [workstation-processes-section workstation]
     [workstation-current-batches-section workstation]]))

(defn workstations []
  [:div {:style {:display "grid"
                 :grid-template-columns "1fr"
                 :gap "16px"}}
   (for [workstation @(r/subscribe [:db/get-in [:workstations :list] []])]
     ^{:key (:workstation/id workstation)}
     [workstation-card workstation])])

(defn view []
  (let [wsid @(r/subscribe [:workspace/get-id])]
    (zero-react/use-effect
     {:mount get-workstations-data})
    [body/view
     {:title "Workstations"
      :description "Grouped machines with connected processes."
      :title-buttons [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations/new")})}
                      "Add Workstation"]
      :body [workstations]}]))