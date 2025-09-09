(ns parquery.frontend.view
  (:require [reagent.core :as r]
            [parquery.frontend.request :as req]))

(defn handle-submit
  "Handles form submission"
  [query-text result-state]
  (let [parsed (req/parse-query-string @query-text)]
    (if (:success parsed)
      (do
        (reset! result-state {:loading true})
        (req/send-queries (assoc 
                           (:data parsed)
                           :callback 
                           (fn [response]
                             (reset! result-state response)))))
      (reset! result-state {:success false :error (:error parsed)}))))

(defn query-input
  "Text area for query input"
  [query-text]
  [:textarea
   {:value @query-text
    :on-change #(reset! query-text (-> % .-target .-value))
    :style {:width "100%"
            :min-height "200px"
            :font-family "monospace"
            :padding "10px"
            :font-size "14px"}}])

(defn submit-button
  "Submit button component"
  [query-text result-state]
  [:button
   {:on-click #(handle-submit query-text result-state)
    :style {:padding "10px 20px"
            :font-size "16px"
            :background-color "#007bff"
            :color "white"
            :border "none"
            :border-radius "4px"
            :cursor "pointer"}}
   "Submit Query"])

(defn results-grid
  "Display results in a nice grid"
  [results]
  [:div {:style {:margin-bottom "20px"}}
   [:h4 {:style {:margin "0 0 15px 0" :color "#1976d2"}} "Results"]
   [:div {:style {:display "grid"
                  :gap "15px"
                  :grid-template-columns "repeat(auto-fit, minmax(300px, 1fr))"}}
    (for [[query-key result] (seq results)]
      [:div {:key (str query-key)
             :style {:padding "15px"
                     :background-color "white"
                     :border "1px solid #e0e0e0"
                     :border-radius "8px"
                     :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}}
       [:h5 {:style {:margin "0 0 10px 0"
                     :color "#424242"
                     :font-family "monospace"
                     :font-size "14px"}}
        (str query-key)]
       [:div {:style {:font-family "monospace"
                      :font-size "12px"
                      :background-color "#f5f5f5"
                      :padding "10px"
                      :border-radius "4px"
                      :white-space "pre-wrap"}}
        (str result)]])]])

(defn nice-result-display
  "Nice UI display for parquery results"
  [result-state]
  (when @result-state
    (cond
      (:loading @result-state)
      [:div {:style {:text-align "center" :padding "40px"}}
       [:div {:style {:font-size "18px" :color "#666"}} "Loading..."]]
      
      (:success @result-state)
      (let [results (:data @result-state)]
        [:div {:style {:margin-top "20px"}}
         (when (seq results)
           [results-grid results])])
      
      :else
      [:div {:style {:margin-top "20px"
                     :padding "15px"
                     :background-color "#ffebee"
                     :border-radius "4px"
                     :border-left "4px solid #f44336"}}
       [:h4 {:style {:margin "0 0 10px 0" :color "#d32f2f"}} "Error"]
       [:p {:style {:color "#d32f2f"}} (:error @result-state)]])))

(defn result-display
  "Display query results - both raw and nice UI"
  [result-state]
  (when @result-state
    [:div {:style {:margin-top "20px"}}
     [:div {:style {:margin-bottom "20px"
                    :padding "15px"
                    :border "1px solid #ddd"
                    :border-radius "4px"
                    :background-color "#f8f9fa"}}
      [:h3 "Raw Response:"]
      (cond
        (:loading @result-state)
        [:div "Loading..."]
        
        (:success @result-state)
        [:pre {:style {:background-color "white"
                       :padding "10px"
                       :border-radius "4px"
                       :overflow "visible"
                       :font-family "monospace"
                       :white-space "pre-wrap"
                       :word-wrap "break-word"
                       :font-size "12px"}}
         (str (:data @result-state))]
        
        :else
        [:div {:style {:color "red"}}
         [:h4 "Error:"]
         [:p (:error @result-state)]])]
     [nice-result-display result-state]]))

(defn view
  "Main parquery view component"
  []
  (let [query-text (r/atom "{:queries {:user/get {:id 123}}\n :context {:a 1}}")
        result-state (r/atom nil)]
    (fn []
      [:div {:style {:padding "20px"
                     :max-width "800px"
                     :margin "0 auto"
                     :background-color "white"
                     :min-height "100vh"}}
       [:h1 "Parquery"]
       [:div {:style {:margin-bottom "10px"}}
        [query-input query-text]]
       [:div
        [submit-button query-text result-state]]
       [result-display result-state]])))