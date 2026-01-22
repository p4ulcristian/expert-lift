(ns ui.address-search
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [translations.core :as tr]))

(def page-size 30)

(defn address-search-dropdown
  "Simple address search dropdown with keyboard navigation and infinite scroll"
  [_props]
  (let [state (r/atom {:search nil  ;; nil = show value, "" = user cleared, "x" = user typing
                       :results []
                       :open? false
                       :highlighted-index -1
                       :offset 0
                       :loading? false
                       :has-more? true})
        search-timeout (r/atom nil)
        blur-timeout (r/atom nil)

        load-addresses! (fn [workspace-id search-term offset append?]
                          (when-not (:loading? @state)
                            (swap! state assoc :loading? true)
                            (parquery/send-queries
                              {:queries {:workspace-addresses/search
                                         {:search (or search-term "")
                                          :limit page-size
                                          :offset offset}}
                               :parquery/context {:workspace-id workspace-id}
                               :callback (fn [response]
                                           (let [new-results (:workspace-addresses/search response [])]
                                             (swap! state (fn [s]
                                                            (-> s
                                                                (assoc :loading? false
                                                                       :has-more? (= (count new-results) page-size)
                                                                       :offset (+ offset (count new-results)))
                                                                (update :results (if append?
                                                                                   #(into (vec %) new-results)
                                                                                   (constantly new-results))))))))})))]
    (fn [{:keys [workspace-id value on-select]}]
      (let [select-item! (fn [addr]
                           (swap! state assoc :search nil :open? false :results []
                                  :highlighted-index -1 :offset 0 :has-more? true)
                           (when on-select (on-select addr)))
            {:keys [search results open? highlighted-index loading? has-more?]} @state
            display-value (if (nil? search) (:address/name value "") search)
            results-count (count results)

            handle-scroll (fn [e]
                            (let [target (.-target e)
                                  scroll-top (.-scrollTop target)
                                  scroll-height (.-scrollHeight target)
                                  client-height (.-clientHeight target)
                                  near-bottom? (< (- scroll-height scroll-top client-height) 50)]
                              (when (and near-bottom? has-more? (not loading?))
                                (load-addresses! workspace-id search (:offset @state) true))))]
        [:div {:style {:position "relative" :width "100%"}}
         ;; Input
         [:input {:type "text"
                  :value display-value
                  :placeholder (tr/tr :address-search/placeholder)
                  :on-focus (fn [_]
                              ;; Cancel any pending blur timeout
                              (when @blur-timeout
                                (js/clearTimeout @blur-timeout)
                                (reset! blur-timeout nil))
                              (swap! state assoc :open? true)
                              ;; Load addresses when opening with empty results
                              (when (empty? (:results @state))
                                (load-addresses! workspace-id nil 0 false)))
                  :on-blur (fn [_]
                             (when @blur-timeout (js/clearTimeout @blur-timeout))
                             (reset! blur-timeout
                               (js/setTimeout
                                 (fn []
                                   (swap! state assoc :open? false :search nil :results []
                                          :highlighted-index -1 :offset 0 :has-more? true)
                                   (reset! blur-timeout nil))
                                 200)))
                  :on-key-down (fn [e]
                                 (let [key (.-key e)]
                                   (cond
                                     ;; Arrow Down - move highlight down
                                     (= key "ArrowDown")
                                     (do
                                       (.preventDefault e)
                                       (when (and open? (pos? results-count))
                                         (swap! state update :highlighted-index
                                                (fn [idx] (min (inc idx) (dec results-count))))))

                                     ;; Arrow Up - move highlight up
                                     (= key "ArrowUp")
                                     (do
                                       (.preventDefault e)
                                       (when (and open? (pos? results-count))
                                         (swap! state update :highlighted-index
                                                (fn [idx] (max (dec idx) 0)))))

                                     ;; Enter - select highlighted item
                                     (= key "Enter")
                                     (do
                                       (.preventDefault e)
                                       (when (and open? (>= highlighted-index 0) (< highlighted-index results-count))
                                         (select-item! (nth results highlighted-index))))

                                     ;; Escape - close dropdown
                                     (= key "Escape")
                                     (do
                                       (.preventDefault e)
                                       (swap! state assoc :open? false :highlighted-index -1)))))
                  :on-change (fn [e]
                               (let [v (.. e -target -value)]
                                 (swap! state assoc :search v :open? true :highlighted-index -1
                                        :offset 0 :has-more? true :results [])
                                 ;; Debounced search
                                 (when @search-timeout (js/clearTimeout @search-timeout))
                                 (reset! search-timeout
                                   (js/setTimeout
                                     (fn [] (load-addresses! workspace-id v 0 false))
                                     (if (empty? v) 0 300)))))
                  :style {:width "100%"
                          :padding "8px 12px"
                          :border "1px solid #d1d5db"
                          :border-radius "6px"
                          :font-size "14px"}}]
         ;; Dropdown
         (when (and open? (or (seq results) (seq search) loading?))
           [:div {:style {:position "absolute"
                          :top "100%"
                          :left 0
                          :right 0
                          :background "#fff"
                          :border "1px solid #d1d5db"
                          :border-top "none"
                          :border-radius "0 0 6px 6px"
                          :max-height "250px"
                          :overflow-y "auto"
                          :z-index 1000
                          :box-shadow "0 4px 6px rgba(0,0,0,0.1)"}
                  :on-scroll handle-scroll}
            (if (and (empty? results) (not loading?))
              [:div {:style {:padding "12px" :color "#6b7280" :text-align "center"}}
               (tr/tr :address-search/no-results)]
              [:<>
               (doall
                 (map-indexed
                   (fn [idx addr]
                     (let [is-highlighted? (= idx highlighted-index)]
                       ^{:key (:address/id addr)}
                       [:div {:on-mouse-down (fn [e]
                                               (.preventDefault e)
                                               (select-item! addr))
                              :on-mouse-enter #(swap! state assoc :highlighted-index idx)
                              :style {:padding "10px 12px"
                                      :cursor "pointer"
                                      :border-bottom "1px solid #f3f4f6"
                                      :background (if is-highlighted? "#f3f4f6" "#fff")}}
                        [:div {:style {:font-weight "500"}} (:address/name addr)]
                        [:div {:style {:font-size "12px" :color "#6b7280"}}
                         (str (:address/city addr) ", " (:address/address-line1 addr))]]))
                   results))
               ;; Loading indicator
               (when loading?
                 [:div {:style {:padding "12px" :color "#6b7280" :text-align "center"}}
                  (tr/tr :common/loading)])])])]))))
