(ns features.flex.jobs.frontend.job
  (:require
   [features.flex.jobs.frontend.request :as jobs-request]
   [reagent.core :refer [atom]]
   [zero.frontend.re-frame :refer [subscribe]]
   [zero.frontend.react :as zero-react]))

(def job-state (atom nil))

(defn get-job-data [id]
  (let [workspace-id @(subscribe [:workspace/get-id])]
    (jobs-request/get-job
     workspace-id
     id
     (fn [response]
       (println "Full response:" response)
       (println "Job data:" response)
       (reset! job-state response)))))

(defn navigate-back []
  (.back js/window.history))

(defn job-type-icon [job-type]
  [:i {:class (str "fas " (case job-type
                           "grouped" "fa-users"
                           "solo" "fa-user"
                           "fa-user"))
       :style {:margin-right "8px"
               :color "#666"}}])

(defn description-icon []
  [:i {:class "fas fa-info-circle"
       :style {:margin-right "8px"
               :color "#666"}}])

(defn card-container [title content]
  [:div {:style {:background-color "white"
                 :border-radius "8px"
                 :box-shadow "0 2px 4px rgba(0,0,0,0.1)"
                 :border "1px solid #e0e0e0"
                 :padding "20px"
                 :margin-bottom "20px"}}
   [:h3 {:style {:margin-top 0
                 :margin-bottom "15px"
                 :color "#333"
                 :font-size "18px"}}
    title]
   content])

(defn order-tracking-card []
  [:div
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "center"
                  :margin-bottom "15px"}}
    [:div {:style {:font-weight 600
                   :color "#333"}}
     "Order #12345"]
    [:div {:style {:color "#4CAF50"
                   :font-weight 500}}
     "In Progress"]]
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :margin-bottom "10px"}}
    [:div {:style {:color "#666"}}
     "Estimated Delivery"]
    [:div {:style {:font-weight 500}}
     "March 15, 2024"]]
   [:div {:style {:display "flex"
                  :justify-content "space-between"}}
    [:div {:style {:color "#666"}}
     "Last Updated"]
    [:div {:style {:font-weight 500}}
     "March 10, 2024"]]])

(defn jobs-card [job]
  [:div
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "flex-start"
                  :margin-bottom "15px"}}
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "10px"}}
     [:button {:on-click navigate-back
               :style {:background "none"
                       :border "none"
                       :cursor "pointer"
                       :color "#666"
                       :padding "5px"
                       :display "flex"
                       :align-items "center"
                       :gap "5px"}}
      [:i {:class "fas fa-arrow-left"
           :style {:font-size "14px"}}]
      "Back"]]
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :align-items "flex-end"
                   :gap "5px"}}
     [:div {:style {:font-size "20px"
                    :font-weight 700
                    :color "#333"}}
      "Job #" (subs (:id job) 0 8)]
     [:div {:style {:color "#666"
                    :font-size "12px"}}
      (.toLocaleString (js/Date. (:created_at job)))]]]
   
   [:div {:style {:background-color "#f8f9fa"
                  :padding "20px"
                  :border-radius "8px"
                  :margin-top "10px"}}
    [:div {:style {:display "flex"
                   :align-items "center"
                   :margin-bottom "15px"}}
     [:div {:style {:display "flex"
                    :align-items "center"
                    :gap "10px"}}
      [job-type-icon (:type job)]
      [:div {:style {:display "flex"
                     :flex-direction "column"}}
       [:div {:style {:font-weight 600
                      :color "#333"}}
        (:type job)]
       [:div {:style {:font-size "12px"
                      :color "#666"}}
        "Job Type"]]]]
    
    [:div {:style {:display "flex"
                   :align-items "center"
                   :margin-bottom "15px"}}
     [:div {:style {:display "flex"
                    :align-items "center"
                    :gap "10px"}}
      [description-icon]
      [:div {:style {:display "flex"
                     :flex-direction "column"}}
       [:div {:style {:font-weight 600
                      :color "#333"}}
        "Description"]
       [:div {:style {:font-size "12px"
                      :color "#666"}}
        (:description job)]]]]]])

(defn client-card []
  [:div
   [:div {:style {:display "flex"
                  :align-items "center"
                  :margin-bottom "15px"}}
    [:div {:style {:width "50px"
                   :height "50px"
                   :border-radius "25px"
                   :background-color "#e0e0e0"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :margin-right "15px"}}
     [:i {:class "fas fa-user"
          :style {:font-size "24px"
                  :color "#666"}}]]
    [:div
     [:div {:style {:font-weight 600
                    :color "#333"}}
      "John Doe"]
     [:div {:style {:color "#666"
                    :font-size "12px"}}
      "Client ID: CL12345"]]]
   [:div {:style {:margin-top "15px"}}
    [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :margin-bottom "8px"}}
     [:span {:style {:color "#666"}}
      "Email:"]
     [:span {:style {:font-weight 500}}
      "john.doe@example.com"]]
    [:div {:style {:display "flex"
                   :justify-content "space-between"}}
     [:span {:style {:color "#666"}}
      "Phone:"]
     [:span {:style {:font-weight 500}}
      "+1 (555) 123-4567"]]]])

(defn job []
  (let [job @job-state]
    (when (and job (:id job))
      [:div {:style {:max-width "800px"
                     :margin "20px auto"
                     :padding "20px"}}
       [card-container "Order Tracking" [order-tracking-card]]
       [card-container "Job Details" [jobs-card job]]
       [card-container "Client Information" [client-card]]])))

(defn view []
  (let [job-id (subscribe [:db/get-in [:router :path-params :job-id]])]
    (zero-react/use-effect
     {:mount (fn []
               (get-job-data @job-id))})
    [:div
     [job]
     (when (not @job-state)
       [:div "Loading job data..."])]))