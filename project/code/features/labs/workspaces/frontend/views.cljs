(ns features.labs.workspaces.frontend.views
  (:require
   [app.frontend.request :as request]
   [clojure.string :as str]
   [features.labs.shared.frontend.components.header :as header]
   [reagent.core :as r]
   [zero.frontend.react :as zero-react]
   [zero.frontend.re-frame :refer [dispatch]]
   [ui.modals.zero :as modals]
   [ui.button :as button]))

;; State
(def workspaces-data (r/atom []))
(def loading (r/atom false))
(def edit-form-data (r/atom {}))
(def available-zip-codes (r/atom []))
(def zip-codes-loading (r/atom false))
(def search-term (r/atom ""))

;; Status label mapping (same as zip codes view)
(defn get-status-label [status-code]
  (case status-code
    "r" "Reserved"
    "d" "Disabled"
    "p" "Preserved"
    "e" "Empty"
    nil "Unknown"
    "" "Unknown"
    (str "Unknown (" status-code ")")))

;; Format zip code with status
(defn format-zip-code-with-status [zip-code-obj]
  (if (map? zip-code-obj)
    (str (:zip-code zip-code-obj) "-" (get-status-label (:status zip-code-obj)))
    (str zip-code-obj)))

;; Data fetching
(defn fetch-workspaces []
  (reset! loading true)
  (request/pathom
   {:query '[:workspaces/get-all-workspaces]
    :callback (fn [response]
                (reset! workspaces-data (get-in response [:workspaces/get-all-workspaces] []))
                (reset! loading false))}))

(defn fetch-available-zip-codes []
  (reset! zip-codes-loading true)
  (request/pathom
   {:query '[:zip-codes/get-zip-codes]
    :callback (fn [response]
                (let [zip-codes-data (get-in response [:zip-codes/get-zip-codes] [])
                      all-zip-codes (mapcat #(if (map? %)
                                                (if (vector? (:data %))
                                                  (map (fn [row] {:zip-code (str (first row))
                                                                  :workspace-id (str (nth row 4))
                                                                  :status (str (nth row 5))})
                                                       (:data %))
                                                  [])
                                                [])
                                            zip-codes-data)]
                  (reset! available-zip-codes all-zip-codes)
                  (reset! zip-codes-loading false)))}))

;; Modal functions
(defn add-zip-code-to-workspace [zip-code-str]
  ;; Create zip code object with default preserved status
  (let [zip-code-obj {:zip-code zip-code-str :status "p"}]
    (swap! edit-form-data update :assigned-zip-codes conj zip-code-obj)))


(defn remove-zip-code-from-workspace [zip-code-to-remove]
  (swap! edit-form-data update :assigned-zip-codes 
         #(vec (remove (fn [zc] 
                        (let [zc-code (if (map? zc) (:zip-code zc) zc)
                              remove-code (if (map? zip-code-to-remove) (:zip-code zip-code-to-remove) zip-code-to-remove)]
                          (= zc-code remove-code))) %))))

(defn update-zip-code-status [zip-code new-status]
  (js/console.log "🔧 Updating status for:" (clj->js zip-code) "to:" new-status)
  (swap! edit-form-data update :assigned-zip-codes
         #(mapv (fn [zc]
                 (let [zc-code (if (map? zc) (:zip-code zc) zc)
                       target-code (if (map? zip-code) (:zip-code zip-code) zip-code)]
                   (if (= zc-code target-code)
                     (let [updated (if (map? zc)
                                    (assoc zc :status new-status)
                                    {:zip-code zc :status new-status})]
                       (js/console.log "✅ Updated zip code:" (clj->js updated))
                       updated)
                     zc))) %))
  (js/console.log "📋 All assigned zip codes after update:" (clj->js (:assigned-zip-codes @edit-form-data))))

(defn save-zip-codes-changes []
  (let [{:keys [workspace-id assigned-zip-codes]} @edit-form-data]
    (js/console.log "💾 Saving zip codes for workspace:" workspace-id)
    (js/console.log "📦 Data being sent to backend:" (clj->js assigned-zip-codes))
    (request/pathom
     {:query `[(workspaces/update-workspace-zip-codes! {:workspace-id ~workspace-id
                                                         :zip-codes ~assigned-zip-codes})]
      :callback (fn [response]
                  (js/console.log "✅ Backend response:" (clj->js response))
                  (fetch-workspaces) ; Refresh the data
                  (dispatch [:modals/close :edit-workspace-zip-codes]))})))

(defn edit-modal-content []
  (let [{:keys [workspace-name assigned-zip-codes]} @edit-form-data
        unassigned-zip-codes (->> @available-zip-codes
                                  (filter #(or (nil? (:workspace-id %))
                                               (= (:workspace-id %) "nil")
                                               (= (:workspace-id %) "")))
                                  (map #(str (:zip-code %) "-" (get-status-label (:status %))))
                                  (sort))
        search-value @search-term
        filtered-zip-codes (if (empty? search-value)
                             unassigned-zip-codes
                             (filter #(.includes (.toLowerCase %) (.toLowerCase search-value))
                                     unassigned-zip-codes))]
    [:div {:style {:padding "20px" :max-height "500px" :overflow-y "auto"}}
     [:div {:style {:margin-bottom "20px"}}
      [:h3 {:style {:margin "0 0 10px 0"}} (str "Managing Zip Codes for: " workspace-name)]]
     
     ;; Currently Assigned Zip Codes
     [:div {:style {:margin-bottom "20px"}}
      [:h4 {:style {:margin "0 0 10px 0" :color "#4f46e5"}} "Currently Assigned Zip Codes:"]
      (if (seq assigned-zip-codes)
        [:div {:style {:display "flex" :flex-wrap "wrap" :gap "8px"}}
         (for [zip-code assigned-zip-codes]
           ^{:key (str "assigned-" (if (map? zip-code) (:zip-code zip-code) zip-code))}
           [:div {:style {:display "flex" :align-items "center" :gap "6px"
                          :background "#f0f0f0" :padding "4px 8px" :border-radius "4px"}}
            ;; Zip code number only
            [:span {:style {:font-size "12px" :font-weight "500"}}
             (if (map? zip-code) (:zip-code zip-code) zip-code)]
            ;; Status select dropdown
            [:select {:value (if (map? zip-code) (:status zip-code) "")
                      :on-change #(update-zip-code-status zip-code (-> % .-target .-value))
                      :style {:font-size "10px"
                              :padding "2px 4px"
                              :border "1px solid #ccc"
                              :border-radius "3px"
                              :background "white"
                              :cursor "pointer"
                              :min-width "70px"}}
             [:option {:value "r"} "Reserved"]
             [:option {:value "d"} "Disabled"]
             [:option {:value "p"} "Preserved"]
             [:option {:value "e"} "Empty"]]
            ;; Remove button
            [:button {:on-click #(remove-zip-code-from-workspace zip-code)
                      :style {:background "#ff4444" :color "white" :border "none"
                              :border-radius "50%" :width "16px" :height "16px"
                              :cursor "pointer" :font-size "10px"}}
             "×"]])]
        [:p {:style {:color "#999" :font-style "italic"}} "No zip codes currently assigned"])]
     
     ;; Available Zip Codes to Add
     [:div {:style {:margin-bottom "20px"}}
      [:h4 {:style {:margin "0 0 10px 0" :color "#4f46e5"}} "Available Zip Codes to Add:"]
      ;; Search Input
      [:div {:style {:margin-bottom "15px"}}
       [:input {:type "text"
                :placeholder "Search zip codes..."
                :value @search-term
                :on-change #(reset! search-term (-> % .-target .-value))
                :style {:width "100%"
                        :padding "8px 12px"
                        :border "1px solid #ccc"
                        :border-radius "4px"
                        :font-size "14px"
                        :outline "none"
                        :box-sizing "border-box"}}]
       (when (not-empty @search-term)
         [:button {:on-click #(reset! search-term "")
                   :style {:margin-top "5px"
                           :padding "4px 8px"
                           :background "#f0f0f0"
                           :border "1px solid #ccc"
                           :border-radius "4px"
                           :cursor "pointer"
                           :font-size "12px"}}
          "Clear search"])]
      ;; Search Results Count
      [:div {:style {:margin-bottom "10px" :font-size "12px" :color "#666"}}
       (str "Showing " (count filtered-zip-codes) " of " (count unassigned-zip-codes) " zip codes")]
      ;; Filtered Zip Codes or Loading
      (cond
        @zip-codes-loading
        [:div {:style {:display "flex" :align-items "center" :justify-content "center"
                       :padding "40px" :flex-direction "column" :gap "12px"}}
         [:div {:style {:width "24px" :height "24px" :border "2px solid #f3f3f3"
                        :border-top "2px solid #4f46e5" :border-radius "50%"
                        :animation "spin 1s linear infinite"}}
          ;; Add CSS animation keyframes in head if not already present
          [:style "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }"]]
         [:span {:style {:color "#666" :font-size "14px"}} "Loading zip codes..."]]
        
        (seq filtered-zip-codes)
        [:div {:style {:display "flex" :flex-wrap "wrap" :gap "8px" :max-height "200px" :overflow-y "auto"}}
         (for [[index zip-code-with-status] (map-indexed vector filtered-zip-codes)]
           (let [zip-code-only (first (str/split zip-code-with-status #"-"))
                 unique-key (str "available-zip-" index "-" zip-code-only)]
             [:button {:key unique-key
                       :on-click #(add-zip-code-to-workspace zip-code-only)
                       :style {:background "#e0e0e0" :border "1px solid #ccc"
                               :padding "4px 8px" :border-radius "4px" :cursor "pointer"
                               :hover {:background "#d0d0d0"}}}
              zip-code-with-status]))]
        
        :else
        [:p {:style {:color "#999" :font-style "italic"}} 
         (if (empty? @search-term)
           "No unassigned zip codes available"
           "No zip codes match your search")])]
     
     ;; Action Buttons
     [:div {:style {:display "flex" :gap "12px" :justify-content "flex-end"}}
      [button/view {:type :secondary
                    :on-click #(dispatch [:modals/close :edit-workspace-zip-codes])}
       "Cancel"]
      [button/view {:type :primary
                    :on-click save-zip-codes-changes}
       "Save Changes"]]]))

(defn open-edit-modal [workspace]
  (reset! edit-form-data {:workspace-id (:id workspace)
                          :workspace-name (:name workspace)
                          :assigned-zip-codes (vec (:zip_codes workspace))})
  (reset! search-term "")  ; Reset search when opening modal
  (fetch-available-zip-codes)
  (dispatch [:modals/add {:id :edit-workspace-zip-codes
                          :label (str "Edit Zip Codes for: " (:name workspace))
                          :content [edit-modal-content]
                          :open? true}]))

;; Table row component
(defn workspace-row [workspace]
  [:tr {:style {:border-bottom "1px solid #e0e0e0"
                :background "#ffffff"}}
   [:td {:style {:padding "12px" :text-align "left"}} 
    [:div
     [:div (str (:creator_first_name workspace) " " (:creator_last_name workspace))]
     [:div {:style {:font-size "10px" :color "#666" :margin-top "2px"}} 
      (str "ID: " (:id workspace))]]]
   [:td {:style {:padding "12px" :text-align "left"}} (:creator_email workspace)]
   [:td {:style {:padding "12px" :text-align "left"}} (:name workspace)]
   [:td {:style {:padding "12px" :text-align "left"}} 
    (when (:created_at workspace)
      (.toLocaleDateString (js/Date. (:created_at workspace))))]
   [:td {:style {:padding "12px" :text-align "left"}}
    (let [zip-codes (:zip_codes workspace)]
      (if (seq zip-codes)
        [:div {:style {:display "flex" :flex-wrap "wrap" :gap "4px"}}
         (for [zip-code zip-codes]
           ^{:key (str "workspace-" (:id workspace) "-" (if (map? zip-code) (:zip-code zip-code) zip-code))}
           [:span {:style {:background "#f0f0f0"
                           :padding "2px 6px"
                           :border-radius "4px"
                           :font-size "12px"
                           :color "#666"}}
            (format-zip-code-with-status zip-code)])]
        [:span {:style {:color "#999" :font-style "italic"}} "No zip codes assigned"]))]
   [:td {:style {:padding "12px" :text-align "left"}}
    [:button {:on-click #(open-edit-modal workspace)
              :style {:padding "4px 8px"
                      :border "1px solid #007bff"
                      :background "#007bff"
                      :color "#ffffff"
                      :border-radius "4px"
                      :cursor "pointer"
                      :font-size "12px"}}
     "Edit Zip Codes"]]])

;; Main table component
(defn workspaces-table []
  (if @loading
    [:div {:style {:padding "40px" :text-align "center"}} "Loading coating partners..."]
    (if (seq @workspaces-data)
      [:div {:style {:border "1px solid #e0e0e0" :border-radius "8px" :overflow "hidden"}}
       [:table {:style {:width "100%" :border-collapse "collapse"}}
        [:thead
         [:tr {:style {:background "#4f46e5" :color "#ffffff"}}
          [:th {:style {:padding "12px" :text-align "left" :font-weight "600"}} "Owner"]
          [:th {:style {:padding "12px" :text-align "left" :font-weight "600"}} "Email"]
          [:th {:style {:padding "12px" :text-align "left" :font-weight "600"}} "Name"]
          [:th {:style {:padding "12px" :text-align "left" :font-weight "600"}} "Created Date"]
          [:th {:style {:padding "12px" :text-align "left" :font-weight "600"}} "Assigned Zip Codes"]
          [:th {:style {:padding "12px" :text-align "left" :font-weight "600"}} "Actions"]]]
        [:tbody
         (for [workspace @workspaces-data]
           ^{:key (:id workspace)}
           [workspace-row workspace])]]
       [:div {:style {:padding "12px" :background "#f8f9fa" :text-align "center" :color "#666"}}
        (str "Total: " (count @workspaces-data) " coating partners")]]
      [:div {:style {:padding "40px" :text-align "center"}} "No coating partners found"])))

;; Main view
(defn view []
  (zero-react/use-effect
   {:mount #(fetch-workspaces)})
  [:div
   [header/view]
   [:div {:style {:padding "20px"}}
    [:div {:style {:background "#4f46e5"
                   :color "#ffffff"
                   :text-align "center"
                   :padding "24px"
                   :margin-bottom "20px"
                   :border-radius "8px"}}
     [:h1 {:style {:margin "0"
                   :font-size "28px"
                   :font-weight "600"}} "Coating Partners Management"]
     [:p {:style {:margin "8px 0 0 0"
                  :opacity "0.9"}} "View all coating partners and their owners"]]
    [workspaces-table]]
   [modals/modals]])