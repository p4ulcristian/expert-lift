(ns features.app.feedback.frontend.view
  "Feedback submission form for users"
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [translations.core :as tr]
            [ui.content-section :as content-section]
            [ui.page-header :as page-header]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (get-in @router/state [:parameters :path :workspace-id]))

(defn- format-date [date-str]
  "Format date string for display"
  (when date-str
    (let [date (js/Date. date-str)]
      (.toLocaleDateString date "hu-HU"))))

(defn- load-my-feedbacks [feedbacks loading?]
  (reset! loading? true)
  (parquery/send-queries
   {:queries {:feedbacks/get-my {}}
    :parquery/context {}
    :callback (fn [response]
                (reset! feedbacks (or (:feedbacks/get-my response) []))
                (reset! loading? false))}))

(defn- delete-feedback! [feedback-id feedbacks loading?]
  (when (js/confirm (tr/tr :feedback/confirm-delete))
    (parquery/send-queries
     {:queries {:feedbacks/delete {:feedback/id feedback-id}}
      :parquery/context {}
      :callback (fn [response]
                  (when (:success (:feedbacks/delete response))
                    (load-my-feedbacks feedbacks loading?)))})))

(defn- save-edit! [feedback-id message feedbacks loading? editing-id]
  (parquery/send-queries
   {:queries {:feedbacks/update {:feedback/id feedback-id
                                 :feedback/message message}}
    :parquery/context {}
    :callback (fn [response]
                (when (:success (:feedbacks/update response))
                  (reset! editing-id nil)
                  (load-my-feedbacks feedbacks loading?)))}))

(defn- feedback-item [feedback feedbacks loading? editing-id edit-message]
  (let [is-editing? (= @editing-id (:id feedback))]
    [:div {:key (:id feedback)
           :style {:padding "1rem"
                   :border-bottom "1px solid #e5e7eb"}}
     (if is-editing?
       ;; Edit mode
       [:div
        [:textarea {:value @edit-message
                    :on-change #(reset! edit-message (.. % -target -value))
                    :style {:width "100%"
                            :min-height "80px"
                            :padding "0.5rem"
                            :border "1px solid #d1d5db"
                            :border-radius "6px"
                            :font-size "0.9rem"
                            :resize "vertical"
                            :box-sizing "border-box"
                            :font-family "inherit"
                            :margin-bottom "0.5rem"}}]
        [:div {:style {:display "flex" :gap "0.5rem"}}
         [:button {:on-click #(save-edit! (:id feedback) @edit-message feedbacks loading? editing-id)
                   :disabled (empty? (str/trim @edit-message))
                   :style {:padding "0.4rem 1rem"
                           :background "#6b8e9b"
                           :color "white"
                           :border "none"
                           :border-radius "6px"
                           :cursor "pointer"
                           :font-size "0.85rem"}}
          (tr/tr :common/save)]
         [:button {:on-click #(reset! editing-id nil)
                   :style {:padding "0.4rem 1rem"
                           :background "#e5e7eb"
                           :color "#374151"
                           :border "none"
                           :border-radius "6px"
                           :cursor "pointer"
                           :font-size "0.85rem"}}
          (tr/tr :common/cancel)]]]
       ;; View mode
       [:div
        [:div {:style {:display "flex" :justify-content "space-between" :align-items "flex-start"}}
         [:p {:style {:margin "0" :color "#374151" :white-space "pre-wrap" :flex "1"}}
          (:message feedback)]
         [:div {:style {:display "flex" :gap "0.5rem" :margin-left "1rem"}}
          [:button {:on-click #(do (reset! editing-id (:id feedback))
                                   (reset! edit-message (:message feedback)))
                    :style {:padding "0.25rem 0.5rem"
                            :background "transparent"
                            :color "#6b7280"
                            :border "1px solid #d1d5db"
                            :border-radius "4px"
                            :cursor "pointer"
                            :font-size "0.8rem"}}
           [:i {:class "fa-solid fa-pen" :style {:font-size "0.75rem"}}]]
          [:button {:on-click #(delete-feedback! (str (:id feedback)) feedbacks loading?)
                    :style {:padding "0.25rem 0.5rem"
                            :background "transparent"
                            :color "#dc2626"
                            :border "1px solid #fca5a5"
                            :border-radius "4px"
                            :cursor "pointer"
                            :font-size "0.8rem"}}
           [:i {:class "fa-solid fa-trash" :style {:font-size "0.75rem"}}]]]]
        [:p {:style {:margin "0.5rem 0 0 0" :color "#9ca3af" :font-size "0.8rem"}}
         (format-date (:created_at feedback))]])]))

(defn- my-feedbacks-list [feedbacks loading? editing-id edit-message]
  (when (seq @feedbacks)
    [:div {:style {:background "white"
                   :border-radius "12px"
                   :box-shadow "0 4px 12px rgba(0,0,0,0.08)"
                   :margin-top "1.5rem"}}
     [:div {:style {:padding "1rem 1.5rem"
                    :border-bottom "1px solid #e5e7eb"}}
      [:h3 {:style {:margin "0" :color "#374151" :font-size "1rem"}}
       (tr/tr :feedback/my-feedbacks)]]
     [:div
      (for [feedback @feedbacks]
        ^{:key (:id feedback)}
        [feedback-item feedback feedbacks loading? editing-id edit-message])]]))

(defn- feedback-form [workspace-id feedbacks loading?]
  (let [message (r/atom "")
        submitting? (r/atom false)
        submitted? (r/atom false)
        error (r/atom nil)]
    (fn []
      [:div {:style {:background "white"
                     :border-radius "12px"
                     :padding "2rem"
                     :box-shadow "0 4px 12px rgba(0,0,0,0.08)"}}
       (if @submitted?
         ;; Success state
         [:div {:style {:text-align "center" :padding "2rem 0"}}
          [:div {:style {:width "64px"
                         :height "64px"
                         :background "#D1FAE5"
                         :border-radius "50%"
                         :display "flex"
                         :align-items "center"
                         :justify-content "center"
                         :margin "0 auto 1.5rem auto"}}
           [:i {:class "fa-solid fa-check"
                :style {:font-size "1.5rem" :color "#10B981"}}]]
          [:h2 {:style {:color "#111827" :margin "0 0 0.5rem 0" :font-size "1.5rem"}}
           (tr/tr :feedback/thank-you)]
          [:p {:style {:color "#6b7280" :margin-bottom "2rem"}}
           (tr/tr :feedback/message-sent)]
          [:button {:on-click #(do (reset! submitted? false)
                                   (reset! message "")
                                   (reset! error nil))
                    :style {:padding "0.75rem 2rem"
                            :background "#6b8e9b"
                            :color "white"
                            :border "none"
                            :border-radius "8px"
                            :cursor "pointer"
                            :font-size "1rem"}}
           (tr/tr :feedback/send-another)]]
         ;; Form state
         [:div
          [:p {:style {:color "#6b7280" :margin "0 0 1.5rem 0" :line-height "1.5"}}
           (tr/tr :feedback/intro-text)]
          [:div {:style {:margin-bottom "1.5rem"}}
           [:label {:style {:display "block"
                            :font-weight "600"
                            :color "#374151"
                            :margin-bottom "0.5rem"}}
            (tr/tr :feedback/message-label)]
           [:textarea {:value @message
                       :on-change #(reset! message (.. % -target -value))
                       :placeholder (tr/tr :feedback/message-placeholder)
                       :disabled @submitting?
                       :style {:width "100%"
                               :min-height "150px"
                               :padding "0.75rem"
                               :border "1px solid #d1d5db"
                               :border-radius "8px"
                               :font-size "1rem"
                               :resize "vertical"
                               :box-sizing "border-box"
                               :font-family "inherit"}}]]
          (when @error
            [:div {:style {:background "#FEF2F2"
                           :border "1px solid #FCA5A5"
                           :color "#991B1B"
                           :padding "0.75rem"
                           :border-radius "8px"
                           :margin-bottom "1rem"}}
             @error])
          [:button {:on-click (fn []
                                (when (and (not @submitting?)
                                           (seq (str/trim @message)))
                                  (reset! submitting? true)
                                  (reset! error nil)
                                  (parquery/send-queries
                                   {:queries {:feedbacks/create {:feedback/message @message}}
                                    :parquery/context {:workspace-id workspace-id}
                                    :callback (fn [response]
                                                (let [result (:feedbacks/create response)]
                                                  (reset! submitting? false)
                                                  (if (:success result)
                                                    (do (reset! submitted? true)
                                                        (load-my-feedbacks feedbacks loading?))
                                                    (reset! error (or (:error result) "Unknown error")))))})))
                    :disabled (or @submitting?
                                  (empty? (str/trim @message)))
                    :style {:padding "0.75rem 2rem"
                            :background (if (or @submitting?
                                                (empty? (str/trim @message)))
                                          "#9CA3AF"
                                          "#6b8e9b")
                            :color "white"
                            :border "none"
                            :border-radius "8px"
                            :cursor (if (or @submitting?
                                            (empty? (str/trim @message)))
                                      "not-allowed"
                                      "pointer")
                            :font-size "1rem"
                            :font-weight "500"}}
           (if @submitting?
             (tr/tr :feedback/sending)
             (tr/tr :feedback/send-button))]])])))

(defn view
  "Main feedback view component"
  []
  (let [workspace-id (get-workspace-id)
        feedbacks (r/atom [])
        loading? (r/atom true)
        editing-id (r/atom nil)
        edit-message (r/atom "")]
    (load-my-feedbacks feedbacks loading?)
    (fn []
      [content-section/content-section
       [page-header/page-header
        {:title (tr/tr :feedback/title)
         :description (tr/tr :feedback/description)}]
       [feedback-form workspace-id feedbacks loading?]
       [my-feedbacks-list feedbacks loading? editing-id edit-message]])))
