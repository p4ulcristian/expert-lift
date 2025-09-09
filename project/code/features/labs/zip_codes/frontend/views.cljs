(ns features.labs.zip-codes.frontend.views
  (:require
   [app.frontend.request :as request]
   [features.labs.shared.frontend.components.header :as header]
   [reagent.core :as reagent]
   [zero.frontend.react :as zero-react]
   [zero.frontend.re-frame :refer [dispatch]]
   ["react-window" :refer [FixedSizeList]]
   [ui.modals.zero :as modals]
   [clojure.string :as clojure.string]
   [ui.button :as button]
   ))

;; State
(def zip-codes-data (reagent/atom []))
(def loading (reagent/atom false))
(def edit-form-data (reagent/atom {}))
(def workspaces-data (reagent/atom []))
(def workspaces-loading (reagent/atom false))
(def search-term (reagent/atom ""))
(def sort-by-status (reagent/atom "p")) ; Default to Preserved sort

;; Data fetching
(defn fetch-zip-codes []
  (reset! loading true)
  (request/pathom
   {:query '[:zip-codes/get-zip-codes]
    :callback (fn [response]
                (reset! zip-codes-data (get-in response [:zip-codes/get-zip-codes] []))
                (reset! loading false))}))

(defn fetch-workspaces []
  (reset! workspaces-loading true)
  (request/pathom
   {:query '[:zip-codes/get-all-workspaces]
    :callback (fn [response]
                (reset! workspaces-data (get-in response [:zip-codes/get-all-workspaces] []))
                (reset! workspaces-loading false))}))

;; Modal functions
(defn find-row-index [zip-code]
  (let [first-record (first @zip-codes-data)
        data (:data first-record)]
    (js/console.log "🔍 Searching in data with count:" (count data))
    (js/console.log "🔍 First few rows:")
    (doseq [i (range (min 3 (count data)))]
      (let [row (nth data i)
            first-val (first row)]
        (js/console.log "  Row" i ":" row "First value:" first-val "Type:" (type first-val))))
    
    (loop [i 0]
      (if (< i (count data))
        (let [row (nth data i)
              first-val (first row)
              first-val-str (str first-val)]
          (js/console.log "Comparing:" zip-code "vs" first-val-str "(" first-val ")")
          (if (= first-val-str zip-code)
            (do
              (js/console.log "✅ Match found at index:" i)
              i)
            (recur (inc i))))
        (do
          (js/console.log "❌ No match found after checking" (count data) "rows")
          nil)))))

(defn save-zip-code-changes []
  (js/console.log "🔥 Save button clicked!")
  (js/console.log "📝 Form data:" @edit-form-data)
  (js/console.log "🗂️ Zip codes data:" @zip-codes-data)
  
  (let [{:keys [zip-code population workspace-id status]} @edit-form-data
        row-index (find-row-index zip-code)]
    
    (js/console.log "🔍 Looking for zip code:" zip-code)
    (js/console.log "📍 Found row index:" row-index)
    
    (if row-index
      (do
        (js/console.log "✅ Row found, proceeding with update...")
        ;; Update local data
        (let [first-record (first @zip-codes-data)
              schema (:schema first-record)
              data (:data first-record)
              updated-data (vec data)
              current-row (vec (nth updated-data row-index))
              ;; Parse population safely - handle empty strings and NaN
              parsed-population (cond
                                  (or (nil? population) (= population "")) nil
                                  :else (let [parsed (js/parseInt population 10)]
                                          (if (js/isNaN parsed) nil parsed)))
              ;; Handle workspace ID - empty string becomes nil
              normalized-workspace-id (if (or (nil? workspace-id) (= workspace-id "")) nil workspace-id)
              ;; Handle status - ensure it's a valid status code
              normalized-status (or status "p")
              updated-row (-> current-row
                             (assoc 1 parsed-population)
                             (assoc 4 normalized-workspace-id)
                             (assoc 5 normalized-status))
              updated-data-vec (assoc updated-data row-index updated-row)]
          
          (js/console.log "🔧 Current row:" current-row)
          (js/console.log "🔧 Updated row:" updated-row)
          (js/console.log "🔧 Parsed population:" parsed-population)
          (js/console.log "🔧 Normalized workspace ID:" normalized-workspace-id)
          (js/console.log "🔧 Normalized status:" normalized-status)
          
          ;; Update the local atom  
          (swap! zip-codes-data assoc-in [0 :data] updated-data-vec)
          
          (js/console.log "💾 Local data updated")
          
          ;; Prepare data for backend
          (let [full-data {:schema schema
                          :data updated-data-vec}]
            
            (js/console.log "📤 Sending to backend:")
            (js/console.log "📤 Full data:" full-data)
            
            ;; Send to backend
            (request/pathom
             {:query `[(zip-codes/update-zip-codes! {:zip-codes-data ~full-data})]
              :callback (fn [response]
                         (js/console.log "✅ Backend response:" response)
                         ;; Refresh the data to ensure UI is in sync
                         (fetch-zip-codes))}))))
      (js/console.log "❌ Row not found for zip code:" zip-code))
    
    ;; Close modal
    (js/console.log "🚪 Closing modal...")
    (dispatch [:modals/close :edit-zip-code])))

(defn edit-modal-content []
  (let [{:keys [zip-code population workspace-id status]} @edit-form-data]
    [:div {:style {:padding "20px"}}
     [:div {:style {:margin-bottom "16px"}}
      [:label {:style {:display "block"
                       :margin-bottom "6px"
                       :font-weight "600"}}
       "Zip Code (Read-only):"]
      [:div {:style {:padding "8px 12px"
                     :background "#f5f5f5"
                     :border-radius "4px"
                     :color "#666"}}
       zip-code]]
     [:div {:style {:margin-bottom "16px"}}
      [:label {:style {:display "block"
                       :margin-bottom "6px"
                       :font-weight "600"}}
       "Population:"]
      [:input {:type "number"
               :value (or population "")
               :on-change #(swap! edit-form-data assoc :population (-> % .-target .-value))
               :placeholder "Enter population"
               :style {:width "100%"
                       :padding "8px 12px"
                       :border "1px solid #ccc"
                       :border-radius "4px"}}]]
     [:div {:style {:margin-bottom "16px"}}
      [:label {:style {:display "block"
                       :margin-bottom "6px"
                       :font-weight "600"}}
       "Status:"]
      [:select {:value (or status "p")
                :on-change #(swap! edit-form-data assoc :status (-> % .-target .-value))
                :style {:width "100%"
                        :padding "8px 12px"
                        :border "1px solid #ccc"
                        :border-radius "4px"}}
       [:option {:value "r"} "Reserved"]
       [:option {:value "d"} "Disabled"]
       [:option {:value "p"} "Preserved"]
       [:option {:value "e"} "Empty"]]]
     [:div {:style {:margin-bottom "20px"}}
      [:label {:style {:display "block"
                       :margin-bottom "6px"
                       :font-weight "600"}}
       "Workspace:"]
      (if @workspaces-loading
        [:div {:style {:padding "8px 12px"
                       :color "#666"}} "Loading workspaces..."]
        [:select {:value (or workspace-id "")
                  :on-change #(swap! edit-form-data assoc :workspace-id (-> % .-target .-value))
                  :style {:width "100%"
                          :padding "8px 12px"
                          :border "1px solid #ccc"
                          :border-radius "4px"}}
         [:option {:value ""} "Available (No Workspace)"]
         (for [workspace @workspaces-data]
           [:option {:key (:id workspace)
                     :value (str (:id workspace))}
            (str (:id workspace) " - " (:name workspace))])])]
     [:div {:style {:display "flex"
                    :gap "12px"
                    :justify-content "flex-end"}}
      [button/view {:type :secondary
                    :on-click #(dispatch [:modals/close :edit-zip-code])}
       "Cancel"]
      [button/view {:type :primary
                    :on-click (fn []
                               (js/console.log "🖱️ Save button UI clicked!")
                               (save-zip-code-changes))}
       "Save Changes"]]]))

(defn open-edit-modal [zip-code population workspace-id status]
  (js/console.log "🚀 Opening modal for zip code:" zip-code)
  (js/console.log "📊 Initial data - Population:" population "Workspace:" workspace-id "Status:" status)
  
  ;; Initialize form data - convert to strings for form inputs  
  (let [form-data {:zip-code (str zip-code)
                   :population (if (or (nil? population) (= population "nil")) "" (str population))
                   :workspace-id (if (or (nil? workspace-id) (= workspace-id "nil")) "" (str workspace-id))
                   :status (if (or (nil? status) (= status "nil")) "p" (str status))}]
    (js/console.log "📝 Setting form data:" form-data)
    (reset! edit-form-data form-data))
  
  (fetch-workspaces) ; Load workspaces when modal opens
  (dispatch [:modals/add {:id :edit-zip-code
                          :label (str "Edit Zip Code: " zip-code)
                          :content [edit-modal-content]
                          :open? true}]))


;; Status label mapping
(defn get-status-label [status-code]
  (case status-code
    "r" "Reserved"
    "d" "Disabled"
    "p" "Preserved"
    "e" "Empty"
    nil "Unknown"
    "" "Unknown"
    (str "Unknown (" status-code ")")))

;; Status priority for sorting (lower number = higher priority)
(defn get-status-priority [status-code]
  (case status-code
    "r" 1 ; Reserved
    "p" 2 ; Preserved
    "e" 3 ; Empty
    "d" 4 ; Disabled
    5))   ; Unknown/others

;; Filter and sort data
(defn filter-and-sort-data [data]
  (let [search-val (clojure.string/lower-case @search-term)
        filtered (if (empty? search-val)
                  data
                  (filter #(clojure.string/includes?
                           (clojure.string/lower-case (str (first %)))
                           search-val) data))
        sorted (if @sort-by-status
                (sort-by (fn [row]
                          (let [status (str (nth row 5))]
                            (if (= status @sort-by-status) 0 1))) filtered)
                filtered)]
    sorted))

;; Virtual list row component
(defn virtual-row [props]
  (let [index (.-index props)
        style (.-style props)
        item-data (.-data props)
        zip-data (.-zipData item-data)
        schema (.-schema item-data)]
    (if (and (number? index) 
             (>= index 0)
             (< index (.-length zip-data)))
      (let [row-data (aget zip-data index)
            zip-code (str (aget row-data 0))
            population (str (aget row-data 1))
            latitude (str (aget row-data 2))
            longitude (str (aget row-data 3))
            workspace-id (str (aget row-data 4))
            status-code (str (aget row-data 5))
            status-label (get-status-label status-code)]
        (reagent/as-element
         [:div {:style (merge (js->clj style :keywordize-keys true)
                              {:display "flex"
                               :align-items "center"
                               :background-color (if (even? index) "#f8f9fa" "#ffffff")
                               :border-bottom "1px solid #e0e0e0"
                               :padding "8px 0"})}
          [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}} zip-code]
          [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}} population]
          [:div {:style {:padding "8px 12px" :flex "1" :min-width "120px"}} latitude]
          [:div {:style {:padding "8px 12px" :flex "1" :min-width "120px"}} longitude]
          [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}} 
           (if (= workspace-id "nil") "Available" workspace-id)]
          [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}} status-label]
          [:div {:style {:padding "8px 12px" :flex "0 0 auto"}}
           [:button {:on-click #(open-edit-modal zip-code population workspace-id status-code)
                     :style {:padding "4px 8px"
                             :border "1px solid #007bff"
                             :background "#007bff"
                             :color "#ffffff"
                             :border-radius "4px"
                             :cursor "pointer"
                             :font-size "12px"}}
            "Edit"]]]))
      (reagent/as-element 
       [:div {:style (js->clj style :keywordize-keys true)} 
        "Loading..."]))))

;; Status sort radio buttons
(defn status-sort-buttons []
  [:div {:style {:display "flex" :flex-wrap "wrap" :gap "12px" :align-items "center"}}
   [:span {:style {:font-weight "600" :color "white" :margin-right "8px"}}
    "Sort by Status:"]
   [:label {:style {:display "flex" :align-items "center" :gap "4px" :cursor "pointer"}}
    [:input {:type "radio"
             :name "status-sort"
             :value "r"
             :checked (= @sort-by-status "r")
             :on-change #(reset! sort-by-status "r")}]
    [:span {:style {:color "white"}} "Reserved"]]
   [:label {:style {:display "flex" :align-items "center" :gap "4px" :cursor "pointer"}}
    [:input {:type "radio"
             :name "status-sort"
             :value "d"
             :checked (= @sort-by-status "d")
             :on-change #(reset! sort-by-status "d")}]
    [:span {:style {:color "white"}} "Disabled"]]
   [:label {:style {:display "flex" :align-items "center" :gap "4px" :cursor "pointer"}}
    [:input {:type "radio"
             :name "status-sort"
             :value "p"
             :checked (= @sort-by-status "p")
             :on-change #(reset! sort-by-status "p")}]
    [:span {:style {:color "white"}} "Preserved"]]
   [:label {:style {:display "flex" :align-items "center" :gap "4px" :cursor "pointer"}}
    [:input {:type "radio"
             :name "status-sort"
             :value "e"
             :checked (= @sort-by-status "e")
             :on-change #(reset! sort-by-status "e")}]
    [:span {:style {:color "white"}} "Empty"]]])

;; Table header
(defn table-header [schema]
  [:div {:style {:display "flex"
                 :background "#4f46e5"
                 :color "#ffffff"
                 :font-weight "600"
                 :padding "12px 0"}}
   [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}} "Zip Code"]
   [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}} "Population"]
   [:div {:style {:padding "8px 12px" :flex "1" :min-width "120px"}} "Latitude"]
   [:div {:style {:padding "8px 12px" :flex "1" :min-width "120px"}} "Longitude"]
   [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}} "Workspace"]
   [:div {:style {:padding "8px 12px" :flex "1" :min-width "100px"}}
    "Status"]
   [:div {:style {:padding "8px 12px" :flex "0 0 auto" :min-width "80px"}} "Actions"]])

;; Search box component
(defn search-box []
  [:div {:style {:margin-bottom "16px" :display "flex" :align-items "center" :gap "12px"}}
   [:div {:style {:flex "1" :max-width "400px"}}
    [:label {:style {:display "block" :margin-bottom "6px" :font-weight "600" :color "#374151"}}
     "Search Zip Codes:"]
    [:input {:type "text"
             :value @search-term
             :on-change #(reset! search-term (-> % .-target .-value))
             :placeholder "Enter zip code to search..."
             :style {:width "100%"
                     :padding "8px 12px"
                     :border "1px solid #d1d5db"
                     :border-radius "6px"
                     :font-size "14px"}}]]
   [:div {:style {:margin-top "24px"}}
    [status-sort-buttons]]])

;; Main table component
(defn zip-codes-table []
  (if @loading
    [:div {:style {:padding "40px" :text-align "center"}} "Loading zip codes..."]
    (if (seq @zip-codes-data)
      (let [first-record (first @zip-codes-data)
            schema (:schema first-record)
            raw-data (:data first-record)
            filtered-data (filter-and-sort-data raw-data)]
        (if (and schema raw-data (seq raw-data))
          [:div
           [search-box]
           [:div {:style {:border "1px solid #e0e0e0" :border-radius "8px" :overflow "hidden"}}
            [table-header schema]
            [:> FixedSizeList
             {:height 600
              :width "100%"
              :itemCount (count filtered-data)
              :itemSize 50
              :itemData #js {:zipData (clj->js filtered-data) :schema (clj->js schema)}
              :children virtual-row}]
            [:div {:style {:padding "12px" :background "#f8f9fa" :text-align "center" :color "#666"}}
             (str "Showing: " (count filtered-data) " of " (count raw-data) " zip codes")]]]
          [:div "No data structure found"]))
      [:div "No zip codes data available"])))

;; Main view
(defn view []
  (zero-react/use-effect
   {:mount #(fetch-zip-codes)})
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
                   :font-weight "600"}} "Zip Codes Management"]
     [:p {:style {:margin "8px 0 0 0"
                  :opacity "0.9"}} "Search, sort by status (radio buttons), or edit zip codes"]]
    [zip-codes-table]]
   [modals/modals]])