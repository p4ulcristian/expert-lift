(ns ui.address-search
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [translations.core :as tr]))

(defn address-search-dropdown
  "Simple address search dropdown"
  [{:keys [workspace-id value on-select]}]
  (let [state (r/atom {:search nil  ;; nil = show value, "" = user cleared, "x" = user typing
                       :results []
                       :open? false})
        search-timeout (r/atom nil)]
    (fn [{:keys [workspace-id value on-select]}]
      (let [{:keys [search results open?]} @state
            display-value (if (nil? search) (:address/name value "") search)]
        [:div {:style {:position "relative" :width "100%"}}
         ;; Input
         [:input {:type "text"
                  :value display-value
                  :placeholder (tr/tr :address-search/placeholder)
                  :on-focus #(swap! state assoc :open? true)
                  :on-blur #(js/setTimeout (fn [] (swap! state assoc :open? false :search nil :results [])) 200)
                  :on-change (fn [e]
                               (let [v (.. e -target -value)]
                                 (swap! state assoc :search v :open? true)
                                 ;; Debounced search
                                 (when @search-timeout (js/clearTimeout @search-timeout))
                                 (when (>= (count v) 1)
                                   (reset! search-timeout
                                     (js/setTimeout
                                       (fn []
                                         (parquery/send-queries
                                           {:queries {:workspace-addresses/search {:search v :limit 10}}
                                            :parquery/context {:workspace-id workspace-id}
                                            :callback (fn [response]
                                                        (swap! state assoc :results
                                                               (:workspace-addresses/search response [])))}))
                                       300)))))
                  :style {:width "100%"
                          :padding "8px 12px"
                          :border "1px solid #d1d5db"
                          :border-radius "6px"
                          :font-size "14px"}}]
         ;; Dropdown
         (when (and open? (or (seq results) (seq search)))
           [:div {:style {:position "absolute"
                          :top "100%"
                          :left 0
                          :right 0
                          :background "#fff"
                          :border "1px solid #d1d5db"
                          :border-top "none"
                          :border-radius "0 0 6px 6px"
                          :max-height "200px"
                          :overflow-y "auto"
                          :z-index 1000
                          :box-shadow "0 4px 6px rgba(0,0,0,0.1)"}}
            (if (empty? results)
              [:div {:style {:padding "12px" :color "#6b7280" :text-align "center"}}
               (tr/tr :address-search/no-results)]
              (for [addr results]
                ^{:key (:address/id addr)}
                [:div {:on-mouse-down (fn [e]
                                        (.preventDefault e)
                                        (swap! state assoc :search "" :open? false :results [])
                                        (when on-select (on-select addr)))
                       :style {:padding "10px 12px"
                               :cursor "pointer"
                               :border-bottom "1px solid #f3f4f6"}
                       :on-mouse-over #(set! (.-backgroundColor (.-style (.-target %))) "#f9fafb")
                       :on-mouse-out #(set! (.-backgroundColor (.-style (.-target %))) "#fff")}
                 [:div {:style {:font-weight "500"}} (:address/name addr)]
                 [:div {:style {:font-size "12px" :color "#6b7280"}}
                  (str (:address/city addr) ", " (:address/address-line1 addr))]]))])]))))
