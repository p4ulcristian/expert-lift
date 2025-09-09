(ns features.flex.workstations.frontend.workstation
  (:require
   [features.flex.workstations.frontend.request :as workstations-request]
   [features.flex.shared.frontend.components.body :as edit-page]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.text-field :as text-field]
   [ui.textarea :as textarea]
   [zero.frontend.react :as zero-react]))

(defn fetch-workstation-machines [workspace-id workstation-id]
  "Fetch and store workstation machines"
  (workstations-request/get-workstation-machines
   workspace-id workstation-id
   (fn [machines]
     (when machines
       (r/dispatch [:db/assoc-in [:workstation :machines] machines])))))

(defn fetch-available-machines [workspace-id]
  "Fetch and store available machines"
  (workstations-request/get-available-machines
   workspace-id
   (fn [machines]
     (when machines
       (r/dispatch [:db/assoc-in [:workstation :available-machines] machines])))))

(defn get-machines-data [workstation-id]
  "Load both workstation and available machines data"
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (fetch-workstation-machines workspace-id workstation-id)
    (fetch-available-machines workspace-id)))

(defn fetch-workstation-processes [workspace-id workstation-id]
  "Fetch and store workstation processes"
  (workstations-request/get-workstation-processes
   workspace-id workstation-id
   (fn [processes]
     (when processes
       (r/dispatch [:db/assoc-in [:workstation :processes] processes])))))

(defn fetch-available-processes [workspace-id]
  "Fetch and store available processes"
  (workstations-request/get-available-processes
   workspace-id
   (fn [processes]
     (when processes
       (r/dispatch [:db/assoc-in [:workstation :available-processes] processes])))))

(defn get-processes-data [workstation-id]
  "Load both workstation and available processes data"
  (let [workspace-id @(r/subscribe [:workspace/get-id])]
    (fetch-workstation-processes workspace-id workstation-id)
    (fetch-available-processes workspace-id)))

(defn get-workstation-data [workstation-id]
  "Load single workstation data or initialize new workstation"
  (if (= workstation-id "new")
    (do
      (r/dispatch [:db/assoc-in [:workstation] {}])
      (r/dispatch [:db/assoc-in [:workstation :name] ""])
      (r/dispatch [:db/assoc-in [:workstation :description] ""]))
    (let [workspace-id @(r/subscribe [:workspace/get-id])]
      (workstations-request/get-workstation
       workspace-id workstation-id
       (fn [workstation]
         (when (not= workstation-id "new")
           (get-machines-data workstation-id))
         (r/dispatch [:db/assoc-in [:workstation] workstation]))))))

(defn refresh-machine-data [workspace-id workstation-id set-assigned set-available]
  "Refresh both assigned and available machine data"
  (workstations-request/get-workstation-machines
   workspace-id workstation-id
   (fn [machines]
     (when machines (set-assigned machines))))
  (workstations-request/get-available-machines
   workspace-id
   (fn [machines]
     (when machines (set-available machines)))))

(defn handle-machine-assignment [workspace-id workstation-id machine-id set-assigned set-available]
  "Handle assigning a machine to workstation"
  (workstations-request/assign-machine
   workspace-id workstation-id machine-id
   (fn [_]
     (refresh-machine-data workspace-id workstation-id set-assigned set-available))))

(defn handle-machine-unassignment [workspace-id workstation-id machine-id set-assigned set-available]
  "Handle unassigning a machine from workstation"
  (workstations-request/unassign-machine
   workspace-id machine-id
   (fn [_]
     (refresh-machine-data workspace-id workstation-id set-assigned set-available))))

(defn machine-selector []
  "Component for selecting and managing machines"
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])
        workspace-id @(r/subscribe [:workspace/get-id])
        [assigned-machines set-assigned-machines] (zero-react/use-state [])
        [available-machines set-available-machines] (zero-react/use-state [])]
    
    (zero-react/use-effect
     {:mount (fn []
               (refresh-machine-data workspace-id workstation-id 
                                   set-assigned-machines set-available-machines))})
    
    [:div 
     [:label {:style {:display "block"
                      :margin-bottom "8px"
                      :color "#333"
                      :font-weight "500"}}
      "Machines"]
     [:div {:style {:display "flex"
                    :flex-direction "column"
                    :gap "8px"}}
      ;; Assigned machines display
      (when (seq assigned-machines)
        [:div {:style {:display "flex"
                       :flex-wrap "wrap"
                       :gap "8px"
                       :padding "8px"
                       :border "1px solid #ddd"
                       :border-radius "4px"
                       :background-color "#f9f9f9"}}
         (for [machine assigned-machines]
           ^{:key (:id machine)}
           [:div {:style {:display "flex"
                          :align-items "center"
                          :gap "8px"
                          :padding "4px 8px"
                          :background-color "white"
                          :border "1px solid #ddd"
                          :border-radius "4px"}}
            [:span (:name machine)]
            [:button {:style {:border "none"
                              :background "none"
                              :color "#666"
                              :cursor "pointer"
                              :padding "0 4px"}
                      :on-click #(handle-machine-unassignment 
                                  workspace-id workstation-id (:id machine)
                                  set-assigned-machines set-available-machines)}
             [:i {:class "fa-solid fa-xmark"}]]])])
      
      ;; Machine selection dropdown
      [:select {:value ""
                :style {:width "100%"
                        :padding "8px"
                        :border "1px solid #ddd"
                        :border-radius "4px"
                        :font-size "14px"}
                :on-change #(let [selected-id (.. ^js % -target -value)]
                              (when (not= selected-id "")
                                (handle-machine-assignment 
                                 workspace-id workstation-id selected-id
                                 set-assigned-machines set-available-machines)))}
       [:option {:value ""} "Select a machine to add..."]
       (for [machine available-machines]
         [:option {:key (:id machine)
                   :value (:id machine)}
          (:name machine)])]]]))

(defn machine-grid []
  "Display grid of assigned machines"
  (let [assigned-machines @(r/subscribe [:db/get-in [:workstation :machines] []])]
    [:div
     [:label {:style {:display "block"
                     :margin-bottom "8px"
                     :color "#333"
                     :font-weight "500"}}
      "Assigned Machines"]
     [:div {:style {:display "grid"
                   :grid-template-columns "repeat(auto-fill, minmax(200px, 1fr))"
                   :gap "16px"
                   :margin-top "8px"}}
      (if (seq assigned-machines)
        (for [machine assigned-machines]
          ^{:key (:id machine)}
          [:div {:style {:background "white"
                        :border "1px solid #ddd"
                        :border-radius "8px"
                        :padding "16px"
                        :display "flex"
                        :flex-direction "column"
                        :gap "8px"}}
           [:div {:style {:font-weight "500"
                         :color "#222"}}
            (:name machine)]
           [:div {:style {:color "#666"
                         :font-size "0.9em"}}
            (:description machine)]])
        [:div {:style {:grid-column "1 / -1"
                      :text-align "center"
                      :color "#666"
                      :padding "24px"
                      :background "#f9f9f9"
                      :border-radius "8px"}}
         "No machines assigned to this workstation"])]]))

(defn refresh-process-data [workspace-id workstation-id set-assigned set-available]
  "Refresh both assigned and available process data"
  (workstations-request/get-workstation-processes
   workspace-id workstation-id
   (fn [processes]
     (when processes (set-assigned processes))))
  (workstations-request/get-available-processes
   workspace-id
   (fn [processes]
     (when processes (set-available processes)))))

(defn handle-process-assignment [workspace-id workstation-id process-id set-assigned set-available]
  "Handle assigning a process to workstation"
  (workstations-request/assign-process
   workspace-id workstation-id process-id
   (fn [_]
     (refresh-process-data workspace-id workstation-id set-assigned set-available))))

(defn handle-process-unassignment [workspace-id workstation-id process-id set-assigned set-available]
  "Handle unassigning a process from workstation"
  (workstations-request/unassign-process
   workspace-id workstation-id process-id
   (fn [_]
     (refresh-process-data workspace-id workstation-id set-assigned set-available))))

(defn process-selector []
  "Component for selecting and managing processes"
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])
        workspace-id @(r/subscribe [:workspace/get-id])
        [assigned-processes set-assigned-processes] (zero-react/use-state [])
        [available-processes set-available-processes] (zero-react/use-state [])]
    
    (zero-react/use-effect
     {:mount (fn []
               (refresh-process-data workspace-id workstation-id 
                                   set-assigned-processes set-available-processes))})
    
    [:div
     [:label {:style {:display "block"
                     :margin-bottom "8px"
                     :color "#333"
                     :font-weight "500"}}
      "Processes"]
     [:div {:style {:display "flex"
                   :flex-direction "column"
                   :gap "8px"}}
      ;; Assigned processes display
      (when (seq assigned-processes)
        [:div {:style {:display "flex"
                      :flex-wrap "wrap"
                      :gap "8px"
                      :padding "8px"
                      :border "1px solid #ddd"
                      :border-radius "4px"
                      :background-color "#f9f9f9"}}
         (for [process assigned-processes]
           ^{:key (:id process)}
           [:div {:style {:display "flex"
                         :align-items "center"
                         :gap "8px"
                         :padding "4px 8px"
                         :background-color "white"
                         :border "1px solid #ddd"
                         :border-radius "4px"}}
            [:span (:name process)]
            [:button {:style {:border "none"
                             :background "none"
                             :color "#666"
                             :cursor "pointer"
                             :padding "0 4px"}
                      :on-click #(handle-process-unassignment 
                                 workspace-id workstation-id (:id process)
                                 set-assigned-processes set-available-processes)}
             [:i {:class "fa-solid fa-xmark"}]]])])
      
      ;; Process selection dropdown
      [:select {:value ""
                :style {:width "100%"
                       :padding "8px"
                       :border "1px solid #ddd"
                       :border-radius "4px"
                       :font-size "14px"}
                :on-change #(let [selected-id (.. ^js % -target -value)]
                            (when (not= selected-id "")
                              (handle-process-assignment 
                               workspace-id workstation-id selected-id
                               set-assigned-processes set-available-processes)))}
       [:option {:value ""} "Select a process to add..."]
       ;; Only show processes that aren't already assigned
       (for [process (filter #(not (some (fn [assigned] (= (:id assigned) (:id %))) assigned-processes))
                            available-processes)]
         [:option {:key (:id process)
                  :value (:id process)}
          (:name process)])]]]))

(defn process-grid []
  "Display grid of assigned processes"
  (let [assigned-processes @(r/subscribe [:db/get-in [:workstation :processes] []])]
    [:div
     [:label {:style {:display "block"
                     :margin-bottom "8px"
                     :color "#333"
                     :font-weight "500"}}
      "Assigned Processes"]
     [:div {:style {:display "grid"
                   :grid-template-columns "repeat(auto-fill, minmax(200px, 1fr))"
                   :gap "16px"
                   :margin-top "8px"}}
      (if (seq assigned-processes)
        (for [process assigned-processes]
          ^{:key (:id process)}
          [:div {:style {:background "white"
                        :border "1px solid #ddd"
                        :border-radius "8px"
                        :padding "16px"
                        :display "flex"
                        :flex-direction "column"
                        :gap "8px"}}
           [:div {:style {:font-weight "500"
                         :color "#222"}}
            (:name process)]
           [:div {:style {:color "#666"
                         :font-size "0.9em"}}
            (:description process)]])
        [:div {:style {:grid-column "1 / -1"
                      :text-align "center"
                      :color "#666"
                      :padding "24px"
                      :background "#f9f9f9"
                      :border-radius "8px"}}
         "No processes assigned to this workstation"])]]))

(defn handle-submit []
  "Handle workstation form submission"
  (let [workstation-data @(r/subscribe [:db/get-in [:workstation]])
        wsid @(r/subscribe [:workspace/get-id])
        workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])]
    (if (= workstation-id "new")
      (workstations-request/create-workstation
       wsid
       {:id (random-uuid)
        :name (:name workstation-data) 
        :description (:description workstation-data)}
       (fn [_]
         (router/navigate! {:path (str "/flex/ws/" wsid "/workstations")})))
      (workstations-request/edit-workstation
       wsid
       {:id workstation-id 
        :name (:name workstation-data) 
        :description (:description workstation-data)}
       (fn [_]
         (router/navigate! {:path (str "/flex/ws/" wsid "/workstations")}))))))

(defn workstation-form []
  "Form component for workstation details"
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])]
    [:div {:style {:max-width "600px"
                   :margin "0 auto"
                   :padding-top "32px"
                   :display "flex"
                   :flex-direction "column"
                   :gap "32px"}}
     [:div {:style {:display "grid" 
                   :grid-template-columns "1fr" 
                   :gap "24px"}}
      [text-field/view {:label "Name"
                       :value @(r/subscribe [:db/get-in [:workstation :name] ""])
                       :on-change #(r/dispatch [:db/assoc-in [:workstation :name] %])}]
      [textarea/view {:label "Description"
                     :value @(r/subscribe [:db/get-in [:workstation :description] ""])
                     :on-change #(r/dispatch [:db/assoc-in [:workstation :description] %])
                     :rows 6}]
      [machine-selector]
      [machine-grid]
      [process-selector]
      [process-grid]]
     [:div {:style {:text-align "center"}}
      [button/view {:mode :filled
                   :color "var(--seco-clr)"
                   :on-click handle-submit}
       (if (= workstation-id "new") "Add Workstation" "Save Changes")]]]))

(defn handle-delete [workstation-id]
  "Handle workstation deletion"
  (let [wsid @(r/subscribe [:workspace/get-id])]
    (workstations-request/delete-workstation
     wsid workstation-id
     (fn [_]
       (router/navigate! {:path (str "/flex/ws/" wsid "/workstations")})))))

(defn workstation [workstation-id]
  "Main workstation component"
  (let [wsid @(r/subscribe [:workspace/get-id])]
    [edit-page/view
     {:title (if (= workstation-id "new") "New Workstation" "Edit Workstation")
      :description "Create, edit, and remove workstations."
      :title-buttons (list
                      (when (and workstation-id (not= workstation-id "new"))
                        ^{:key "delete"}
                        [button/view {:mode :outlined
                                     :color "var(--seco-clr)"
                                     :style {:fontWeight 500 
                                            :padding "8px 20px"}
                                     :on-click #(handle-delete workstation-id)}
                         "Delete"])
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/workstations")})}
                       "Back"])
      :body [workstation-form]}]))

(defn view []
  "Main view component with data loading"
  (let [workstation-id @(r/subscribe [:db/get-in [:router :path-params :workstation-id]])]
    (zero-react/use-effect
     {:mount (fn []
               (get-workstation-data workstation-id)
               (when (not= workstation-id "new")
                 (get-processes-data workstation-id)))})
    [:div
     [workstation workstation-id]]))