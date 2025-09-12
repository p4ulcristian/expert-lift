(ns ui.address-search
  (:require ["react" :as react]
            [re-frame.core :as rf]
            [parquery.frontend.request :as parquery]
            [clojure.string :as str]
            [translations.core :as tr]))

;; Re-frame state for address search
(rf/reg-sub
  :ui/address-search
  (fn [db [_ component-id]]
    (get-in db [:ui :address-search component-id])))

(rf/reg-event-db
  :ui/address-search-set-state
  (fn [db [_ component-id state]]
    (assoc-in db [:ui :address-search component-id] state)))

(rf/reg-event-db
  :ui/address-search-update
  (fn [db [_ component-id key value]]
    (assoc-in db [:ui :address-search component-id key] value)))

;; Debounced search function
(defn debounced-search [search-fn delay]
  (let [timeout-ref (atom nil)]
    (fn [search-term workspace-id callback]
      (when @timeout-ref
        (js/clearTimeout @timeout-ref))
      (reset! timeout-ref
        (js/setTimeout
          (fn []
            (search-fn search-term workspace-id callback))
          delay)))))

;; Search addresses via ParQuery
(defn search-addresses [search-term workspace-id callback]
  (println "DEBUG: address-search searching for:" search-term)
  (parquery/send-queries
    {:queries {:workspace-addresses/search {:search search-term :limit 20}}
     :parquery/context {:workspace-id workspace-id}
     :callback (fn [response]
                 (let [addresses (:workspace-addresses/search response [])]
                   (println "DEBUG: address-search found" (count addresses) "addresses")
                   (callback addresses)))}))

;; Create debounced search with 300ms delay
(def debounced-search-addresses (debounced-search search-addresses 300))

(defn address-search-dropdown
  "Searchable address dropdown component"
  [{:keys [component-id workspace-id value on-select placeholder disabled]}]
  ;; Initialize state on first render
  (when-not @(rf/subscribe [:ui/address-search component-id])
    (rf/dispatch [:ui/address-search-set-state component-id
                  {:search-term ""
                   :addresses []
                   :loading? false
                   :show-dropdown? false
                   :selected-address (or value {})}]))
  
  (fn [{:keys [component-id workspace-id value on-select placeholder disabled]}]
    (let [state @(rf/subscribe [:ui/address-search component-id])
          search-term (or (:search-term state) "")
          addresses (or (:addresses state) [])
          loading? (or (:loading? state) false)
          show-dropdown? (or (:show-dropdown? state) false)
          selected-address (or (:selected-address state) value)]
      
      (println "DEBUG: address-search component rendering:")
      (println "  component-id:" component-id)
      (println "  workspace-id:" workspace-id)
      (println "  value:" value)
      (println "  state:" state)
      (println "  search-term:" search-term)
      (println "  selected-address:" selected-address)
      
      ;; Update selected address when value prop changes
      (when (and value (not= value selected-address))
        (rf/dispatch [:ui/address-search-update component-id :selected-address value]))
    
    [:div {:class "address-search-container" :style {:position "relative" :width "100%"}}
     
     ;; Search input
     [:input {:type "text"
              :placeholder (or placeholder (tr/tr :address-search/placeholder))
              :value search-term
              :disabled disabled
              :on-change (fn [e]
                           (let [new-term (-> e .-target .-value)]
                             (rf/dispatch [:ui/address-search-update component-id :search-term new-term])
                             (rf/dispatch [:ui/address-search-update component-id :show-dropdown? (seq new-term)])
                             ;; Trigger search if conditions are met
                             (when (and workspace-id 
                                       (seq new-term) 
                                       (>= (count new-term) 1))
                               (rf/dispatch [:ui/address-search-update component-id :loading? true])
                               (debounced-search-addresses 
                                 new-term 
                                 workspace-id
                                 (fn [results]
                                   (rf/dispatch [:ui/address-search-update component-id :addresses results])
                                   (rf/dispatch [:ui/address-search-update component-id :loading? false]))))))
              :on-focus (fn [_]
                          (rf/dispatch [:ui/address-search-update component-id :show-dropdown? true]))
              :on-blur (fn [_]
                         ;; Delay hiding to allow clicks on dropdown items
                         (js/setTimeout 
                           #(rf/dispatch [:ui/address-search-update component-id :show-dropdown? false]) 
                           200))
              :style {:width "100%"
                      :padding "8px 12px"
                      :border "1px solid #ccc"
                      :border-radius "4px"
                      :font-size "14px"
                      :background (if disabled "#f5f5f5" "#fff")}}]
     
     ;; Selected address display (when not searching)
     (when (and selected-address (str/blank? search-term))
       [:div {:style {:position "absolute"
                      :top "0"
                      :left "0" 
                      :right "0"
                      :height "100%"
                      :padding "8px 12px"
                      :background "#f9f9f9"
                      :border "1px solid #ccc"
                      :border-radius "4px"
                      :display "flex"
                      :align-items "center"
                      :pointer-events "none"
                      :color "#333"}}
        (:address/display selected-address (:address/name selected-address))])
     
     ;; Dropdown with results
     (when (and show-dropdown? (seq search-term))
       [:div {:class "address-search-dropdown"
              :style {:position "absolute"
                      :top "100%"
                      :left "0"
                      :right "0"
                      :background "#fff"
                      :border "1px solid #ccc"
                      :border-top "none"
                      :border-radius "0 0 4px 4px"
                      :max-height "200px"
                      :overflow-y "auto"
                      :z-index 1000
                      :box-shadow "0 2px 8px rgba(0,0,0,0.1)"}}
        
        ;; Loading indicator
        (when loading?
          [:div {:style {:padding "12px" :text-align "center" :color "#666"}}
           (tr/tr :address-search/searching)])
        
        ;; No results message
        (when (and (not loading?) (empty? addresses) (seq search-term))
          [:div {:style {:padding "12px" :text-align "center" :color "#666"}}
           (tr/tr :address-search/no-results)])
        
        ;; Address options
        (for [address addresses]
          [:div {:key (:address/id address)
                 :class "address-option"
                 :on-click (fn [_]
                             (rf/dispatch [:ui/address-search-update component-id :selected-address address])
                             (rf/dispatch [:ui/address-search-update component-id :search-term ""])
                             (rf/dispatch [:ui/address-search-update component-id :show-dropdown? false])
                             (when on-select
                               (on-select address)))
                 :style {:padding "8px 12px"
                         :cursor "pointer"
                         :border-bottom "1px solid #eee"}}
           [:div {:style {:font-weight "500"}} (:address/name address)]
           [:div {:style {:font-size "12px" :color "#666"}} 
            (str (:address/address-line1 address) ", " (:address/city address))]]) ])])))