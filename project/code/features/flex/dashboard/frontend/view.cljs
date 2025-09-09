(ns features.flex.dashboard.frontend.view
  (:require
   [features.flex.dashboard.frontend.request :as dashboard-request]
   [features.flex.shared.frontend.components.body :as body]
   [goog.string.format]
   [reagent.core :as r]
   [zero.frontend.re-frame :as rf]
   [ui.card :as card]
   [ui.button :as button]
   [zero.frontend.react :as zero-react]))


(def dashboard-state (r/atom {}))
(def feedback-state (r/atom {:subject "" :body "" :to-email "p4ulcristian@gmail.com" :feedback-type "General Feedback"}))

(defn send-feedback! []
  (let [{:keys [subject body to-email feedback-type]} @feedback-state
        workspace-id @(rf/subscribe [:workspace/get-id])]
    (when (and (not-empty subject) (not-empty body))
      (dashboard-request/send-email
       workspace-id subject body to-email feedback-type
       (fn [result]
         (println "Feedback result:" result)
         (if (:success result)
           (do
             (js/alert "Feedback sent successfully!")
             (reset! feedback-state {:subject "" :body "" :to-email "p4ulcristian@gmail.com" :feedback-type "General Feedback"}))
           (js/alert (str "Failed to send feedback: " (:message result) 
                         (when (:debug result) 
                           (str "\n\nDebug: " (:debug result)))))))))))


(defn feedback-type-selector []
  "Feedback type selector component"
  [:div {:style {:margin-bottom "20px"}}
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :font-weight "500"
                    :color "#374151"}}
    "Feedback Type:"]
   [:select {:value (:feedback-type @feedback-state)
             :on-change #(swap! feedback-state assoc :feedback-type (-> % .-target .-value))
             :style {:width "100%"
                     :padding "12px"
                     :border "1px solid #d1d5db"
                     :border-radius "6px"
                     :background-color "white"
                     :font-size "14px"}}
    [:option {:value "General Feedback"} "General Feedback"]
    [:option {:value "Bug Report"} "Bug Report"]
    [:option {:value "Feature Request"} "Feature Request"]
    [:option {:value "Support Request"} "Support Request"]]])

(defn feedback-input-field [field-key label input-type placeholder]
  "Reusable input field component for feedback form"
  [:div {:style {:margin-bottom "20px"}}
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :font-weight "500"
                    :color "#374151"}}
    (str label ":")]
   (if (= input-type :textarea)
     [:textarea {:value (field-key @feedback-state)
                 :placeholder placeholder
                 :on-change #(swap! feedback-state assoc field-key (-> % .-target .-value))
                 :style {:width "100%"
                         :height "120px"
                         :padding "12px"
                         :border "1px solid #d1d5db"
                         :border-radius "6px"
                         :font-size "14px"
                         :line-height "1.5"
                         :resize "vertical"
                         :background-color "white"}}]
     [:input {:type (name input-type)
              :value (field-key @feedback-state)
              :placeholder placeholder
              :on-change #(swap! feedback-state assoc field-key (-> % .-target .-value))
              :style {:width "100%"
                      :padding "12px"
                      :border "1px solid #d1d5db"
                      :border-radius "6px"
                      :font-size "14px"
                      :background-color "white"}}])])

(defn feedback-form []
  "Feedback form component for dashboard"
  [card/view 
   {:content [:div {:style {:max-width "600px"
                             :padding "24px"}}
              [:h3 {:style {:margin-bottom "16px"
                            :font-size "20px"
                            :font-weight "600"
                            :color "#111827"}}
               "Send Feedback"]
              [:p {:style {:margin-bottom "24px"
                           :color "#6b7280"
                           :line-height "1.6"}}
               "Your feedback helps us improve. User and workspace information will be automatically included."]
              [feedback-type-selector]
              [feedback-input-field :subject "Subject" :text "Brief description of your feedback"]
              [feedback-input-field :body "Message" :textarea "Please provide detailed feedback..."]
              [:div {:style {:margin-top "32px"
                             :padding-top "24px"
                             :border-top "1px solid #e5e7eb"}}
               [button/view {:on-click send-feedback!
                             :color :green
                             :disabled (or (empty? (:subject @feedback-state))
                                          (empty? (:body @feedback-state)))}
                "Send Feedback"]]]}])

(defn view []
  (let [workspace-id @(rf/subscribe [:workspace/get-id])]
    (zero-react/use-effect 
     {:mount (fn []
               (when workspace-id
                 (dashboard-request/load-dashboard
                  workspace-id
                  (fn [response]
                    (reset! dashboard-state response)))))
      :params #js [workspace-id]})
    [body/view 
     {:title "Dashboard"
      :description "Main dashboard for managing your flex workspace and sending feedback"
      :body [:div {:style {:display "flex"
                           :justify-content "center"
                           :padding "40px 20px"}}
             [:div {:style {:width "100%"
                            :max-width "800px"}}
              [feedback-form]]]}]))
