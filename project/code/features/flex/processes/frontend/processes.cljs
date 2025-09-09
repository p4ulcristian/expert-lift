(ns features.flex.processes.frontend.processes
  (:require
   [re-frame.core :as r]
   [ui.button :as button]
   [ui.table.zero :as table]
   [router.frontend.zero :as router]
   [zero.frontend.react :as zero-react]
   [features.flex.processes.frontend.request :as processes-request]
   [features.flex.processes.frontend.events]
   [features.flex.shared.frontend.components.flex-title :as flex-title]))

;; --- Table Column Renderers ---

(defn actions-cell [wsid row]
  [button/view {:mode :outlined
                :color "var(--seco-clr)"
                :style {:padding "4px 12px" :fontSize "0.95em"}
                :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/processes/" (:process/id row))})}
   "Edit"])

;; --- Table Component ---
(defn process-table [processes wsid]
  [table/view
   {:rows processes
    :grid-template-columns "1fr 1fr auto"
    :columns [:process/name :process/description :actions]
    :labels {:process/name "Name"
             :process/description "Description"
             :actions "Actions"}
    :column-elements {:actions (fn [_item row] (actions-cell wsid row))}
    :row-element (fn [style content]
                   [:div {:style (merge style
                                       {:transition "all 0.2s ease"
                                        :cursor "pointer"})}
                    content])}])

;; --- Main View ---
(defn view []
  (let [wsid @(r/subscribe [:workspace/get-id])]
    (zero-react/use-effect 
     {:mount (fn [] 
               (r/dispatch [:flex/processes wsid]))
      :params #js[wsid]})
    (let [processes @(r/subscribe [:flex/processes-get])]
      [:div
       [flex-title/view "Processes" 
        {:description "Define powder coating processes and workflows."
         :right-content [button/view {:mode :filled
                                      :color "var(--seco-clr)"
                                      :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/processes/new")})}
                         "New Process"]}]
       [process-table processes wsid]]))) 