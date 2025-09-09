(ns features.site.orders.frontend.orders
  (:require
   [re-frame.core :as r]
   [app.frontend.request :as request]
   [zero.frontend.react :as react]
   [features.site.orders.frontend.stripe :as stripe]
   [router.frontend.zero :as router]))

(defn status-badge [status]
  [:span {:style {:padding "0.5rem 1rem"
                  :border-radius "9999px"
                  :font-size "0.875rem"
                  :font-weight "600"
                  :text-transform "uppercase"
                  :letter-spacing "0.05em"
                  :background (case status
                                "sent-to-customer" "#10b981"
                                "waiting-to-start" "#f59e0b"
                                "parts-inspected" "#3b82f6"
                                "in-progress" "#8b5cf6"
                                "completed" "#10b981"
                                "cancelled" "#ef4444"
                                "#6b7280")
                  :color "#ffffff"}}
   status])

(defn payment-status-badge [payment-status]
  [:span {:style {:padding "0.25rem 0.75rem"
                  :border-radius "6px"
                  :font-size "0.75rem"
                  :font-weight "500"
                  :background (case payment-status
                                "paid" "#dcfce7"
                                "pending" "#fef3c7"
                                "failed" "#fecaca"
                                "unpaid" "#f3f4f6"
                                "#f3f4f6")
                  :color (case payment-status
                           "paid" "#166534"
                           "pending" "#d97706"
                           "failed" "#dc2626"
                           "unpaid" "#6b7280"
                           "#6b7280")}}
   (or payment-status "unpaid")])

(defn urgency-badge [urgency]
  [:span {:style {:padding "0.25rem 0.75rem"
                  :border-radius "6px"
                  :font-size "0.75rem"
                  :font-weight "500"
                  :background (case urgency
                                "rush" "#fef2f2"
                                "high" "#fff7ed"
                                "normal" "#f0f9ff"
                                "low" "#f9fafb"
                                "#f9fafb")
                  :color (case urgency
                           "rush" "#dc2626"
                           "high" "#ea580c"
                           "normal" "#0369a1"
                           "low" "#6b7280"
                           "#6b7280")}}
   urgency])

(defn source-badge [source]
  [:span {:style {:padding "0.25rem 0.75rem"
                  :border-radius "6px"
                  :font-size "0.75rem"
                  :font-weight "500"
                  :background (case source
                                "website" "#f0f9ff"
                                "phone" "#fef2f2"
                                "email" "#f0fdf4"
                                "#f9fafb")
                  :color (case source
                           "website" "#0369a1"
                           "phone" "#dc2626"
                           "email" "#16a34a"
                           "#6b7280")}}
   source])

(defn format-date [date-str]
  (when date-str
    (let [date (js/Date. date-str)]
      (.toLocaleDateString date "en-US" #js {:year "numeric"
                                            :month "short"
                                            :day "numeric"}))))

(defn get-friendly-status-message [status payment-status]
  (cond
    (= payment-status "paid")
    (case status
      "order-submitted" "🎉 Payment received! We're reviewing your order"
      "package-arrived" "📦 Your parts have arrived and look great!"
      "parts-inspected" "🔍 Parts inspected - everything looks perfect!"
      "waiting-to-start" "⏳ In queue - we'll start working on this soon"
      "process-planning" "📋 Our team is planning the perfect finish"
      "in-progress" "🚀 Currently being worked on with care"
      "job-complete" "✅ Finished! Your parts look amazing"
      "packing" "📦 Carefully packing your beautiful parts"
      "sent-to-customer" "🚚 On the way to you!"
      "arrived-at-customer" "🎁 Delivered! Hope you love them"
      "Processing your order with care")
    
    (= payment-status "unpaid")
    "💰 Ready to pay and get started"
    
    (= payment-status "failed")
    "💳 Payment issue - but we're here to help!"
    
    :else "Working on your order"))

(defn compact-job-item [job]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"
                 :margin-bottom "4px"}}
   ;; Part image
   [:div {:style {:flex-shrink 0}}
    (if (:part_picture_url job)
      [:img {:src (:part_picture_url job)
             :alt (:part_name job)
             :style {:width "32px"
                     :height "32px"
                     :object-fit "cover"
                     :border-radius "4px"
                     :border "1px solid #e5e7eb"}}]
      [:div {:style {:width "32px"
                     :height "32px"
                     :background "#f3f4f6"
                     :border-radius "4px"
                     :display "flex"
                     :align-items "center"
                     :justify-content "center"
                     :color "#9ca3af"
                     :font-size "1rem"}}
       "📦"])]
   
   ;; Part details
   [:div {:style {:flex 1
                  :min-width 0}}
    [:div {:style {:font-size "0.875rem"
                   :font-weight "500"
                   :color "#374151"
                   :white-space "nowrap"
                   :overflow "hidden"
                   :text-overflow "ellipsis"}}
     (str (:part_name job) " × " (:quantity job))]
    (when (:look_name job)
      [:div {:style {:font-size "0.75rem"
                     :color "#6b7280"
                     :white-space "nowrap"
                     :overflow "hidden"
                     :text-overflow "ellipsis"}}
       (:look_name job)])]
   
   ;; Look thumbnail
   (when (:look_thumbnail job)
     [:img {:src (:look_thumbnail job)
            :alt (:look_name job)
            :style {:width "20px"
                    :height "20px"
                    :border-radius "3px"
                    :border "1px solid #e5e7eb"
                    :flex-shrink 0}}])])

(defn compact-jobs-display [jobs]
  (when (and jobs (seq jobs))
    [:div {:style {:margin-top "8px"
                   :padding-top "8px"
                   :border-top "1px solid #f3f4f6"}}
     [:div {:style {:font-size "0.75rem"
                    :color "#6b7280"
                    :margin-bottom "6px"
                    :font-weight "500"}}
      "✨ Your beautiful parts:"]
     [:div {:style {:max-height "120px"
                    :overflow-y "auto"}}
      (map-indexed 
       (fn [idx job]
         ^{:key (str (:job_id job) "-compact-" idx)}
         [compact-job-item job])
       jobs)]]))

(defn order-card [order show-payment-modal]
  [:div {:style {:background "#ffffff"
                 :border "1px solid #e5e7eb"
                 :border-radius "12px"
                 :padding "1.5rem"
                 :margin-bottom "1rem"
                 :box-shadow "0 1px 3px rgba(0, 0, 0, 0.1)"
                 :transition "box-shadow 0.2s ease"
                 :cursor "pointer"}
         :on-click #(router/navigate! {:path (str "/orders/" (:id order))})}
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "flex-start"
                  :margin-bottom "1rem"}}
    [:div
     [:h3 {:style {:font-size "1.125rem"
                   :font-weight "600"
                   :color "#1f2937"
                   :margin-bottom "0.25rem"}} 
      (str "Order #" (subs (:id order) 0 8))]
     [:p {:style {:font-size "0.875rem"
                  :color "#6b7280"}}
      (str "Created: " (format-date (:created_at order)))]
     [:div {:style {:margin-top "4px"
                    :padding "4px 8px"
                    :background "#f0f9ff"
                    :border-radius "6px"
                    :border-left "3px solid #10b981"}}
      [:p {:style {:font-size "0.8rem"
                   :color "#059669"
                   :margin 0
                   :font-weight "500"}}
       (get-friendly-status-message (:status order) (:payment_status order))]]]
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "12px"}}
     
     ;; Show Pay Now button for unpaid orders
     (when (= (:payment_status order "unpaid") "unpaid")
       [:button {:style {:background "linear-gradient(135deg, #10b981 0%, #059669 100%)"
                         :color "white"
                         :border "none"
                         :border-radius "6px"
                         :padding "8px 16px"
                         :font-size "0.8rem"
                         :font-weight "600"
                         :cursor "pointer"
                         :transition "all 0.2s ease"
                         :box-shadow "0 2px 8px rgba(16, 185, 129, 0.3)"}
                 :on-click (fn [e]
                            (.stopPropagation ^js e)
                            (show-payment-modal order))}
        "✨ Complete Payment"])]]
   
   [:div {:style {:display "flex"
                  :flex-wrap "wrap"
                  :gap "0.75rem"
                  :margin-bottom "1rem"}}
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "0.5rem"}}
     [:span {:style {:font-size "0.875rem"
                     :color "#374151"
                     :font-weight "500"}} "Status:"]
     [status-badge (:status order)]]
    
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "0.5rem"}}
     [:span {:style {:font-size "0.875rem"
                     :color "#374151"
                     :font-weight "500"}} "Payment:"]
     [payment-status-badge (:payment_status order)]]
    
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "0.5rem"}}
     [:span {:style {:font-size "0.875rem"
                     :color "#374151"
                     :font-weight "500"}} "Urgency:"]
     [urgency-badge (:urgency order)]]
    
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "0.5rem"}}
     [:span {:style {:font-size "0.875rem"
                     :color "#374151"
                     :font-weight "500"}} "Source:"]
     [source-badge (:source order)]]]
   
   [:div {:style {:border-top "1px solid #e5e7eb"
                  :padding-top "1rem"}}
    [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :align-items "center"}}
     [:div {:style {:font-size "0.875rem"
                    :color "#6b7280"}}
      (str "Due: " (format-date (:due_date order)))]
     [:div {:style {:text-align "right"}}
      [:div {:style {:font-size "1.1rem"
                     :font-weight "700"
                     :color "#1f2937"}}
       (str "$" (:total_amount order))]
      [:div {:style {:font-size "0.75rem"
                     :color "#6b7280"}}
       "USD"]]]
    
    ;; Parts and colors compact display
    [compact-jobs-display (:jobs order)]]])

(defn view []
  (let [[orders set-orders] (react/use-state nil)
        [loading set-loading] (react/use-state false)
        [selected-order set-selected-order] (react/use-state nil)
        [show-payment set-show-payment] (react/use-state false)
        [user-id set-user-id] (react/use-state nil)]
    
    (react/use-effect
     {:mount (fn []
               (set-loading true)
               (println "user-id" @(r/subscribe [:db/get-in [:router]]))
               (request/pathom
                {:query '[:site/user-orders  :site/user-id]
                 :callback (fn [response]
                             (set-loading false)
                             (set-orders (:site/user-orders  response))
                             (set-user-id (:site/user-id response)))}))})
    
    [:div {:style {:min-height "100vh"
                   :background "linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%)"
                   :padding "2rem"}}
     [:div {:style {:max-width "800px"
                    :margin "0 auto"}}
      [:div {:style {:text-align "center"
                     :margin-bottom "2.5rem"}}
       [:h1 {:style {:font-size "2.8rem"
                     :font-weight "700"
                     :color "#1f2937"
                     :margin-bottom "0.75rem"
                     :line-height "1.1"}} 
        "👋 Hi there!"]
       [:p {:style {:font-size "1.3rem"
                    :color "#4b5563"
                    :margin-bottom "0.5rem"
                    :font-weight "500"}} 
        "Here are your orders"]
       [:p {:style {:font-size "1rem"
                    :color "#6b7280"
                    :margin-bottom "0"}} 
        "We're working hard to make your parts look amazing! ✨"]]
      
      (cond
        loading 
        [:div {:style {:display "flex"
                       :flex-direction "column"
                       :justify-content "center"
                       :align-items "center"
                       :padding "4rem"
                       :background "#ffffff"
                       :border-radius "16px"
                       :border "1px solid #e5e7eb"
                       :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"}}
         [:div {:style {:font-size "3rem"
                        :margin-bottom "1rem"
                        :animation "pulse 2s infinite"}} "⏳"]
         [:div {:style {:font-size "1.2rem"
                        :color "#4b5563"
                        :font-weight "500"}} "Loading your orders..."]
         [:div {:style {:font-size "0.9rem"
                        :color "#6b7280"
                        :margin-top "0.5rem"}} "Just a moment while we gather everything!"]]
        
        (empty? orders)
        [:div {:style {:text-align "center"
                       :padding "4rem 3rem"
                       :background "#ffffff"
                       :border-radius "16px"
                       :border "1px solid #e5e7eb"
                       :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"}}
         [:div {:style {:font-size "4rem"
                        :margin-bottom "1.5rem"}} "🚀"]
         [:h3 {:style {:font-size "1.5rem"
                       :font-weight "600"
                       :color "#1f2937"
                       :margin-bottom "0.75rem"}} "Ready to get started?"]
         [:p {:style {:color "#6b7280"
                      :font-size "1.1rem"
                      :line-height "1.6"
                      :margin-bottom "2rem"
                      :max-width "400px"
                      :margin-left "auto"
                      :margin-right "auto"}} 
          "Your first order will appear here once you place it. We can't wait to help bring your ideas to life! 💫"]
         [:div {:style {:display "flex"
                        :justify-content "center"
                        :gap "0.5rem"
                        :color "#10b981"
                        :font-size "0.9rem"
                        :font-weight "500"}}
          [:span "💡"] [:span "Browse parts"] [:span "→"] [:span "Customize"] [:span "→"] [:span "Order"] [:span "→"] [:span "Amazing results!"]]]
        
        :else
        [:div
         [:div {:style {:margin-bottom "1.5rem"
                        :text-align "center"}}
          [:h2 {:style {:font-size "1.4rem"
                        :font-weight "600"
                        :color "#374151"
                        :margin-bottom "0.5rem"}}
           (str "🎉 You have " (count orders) " order" (when (not= (count orders) 1) "s") "!")]
          [:p {:style {:font-size "0.95rem"
                       :color "#6b7280"}}
           "Click on any order to see all the details and track progress"]]
         
         (map-indexed 
          (fn [idx order]
            ^{:key (str (:id order) "-" idx)}
            [order-card order (fn [order]
                                (set-selected-order order)
                                (set-show-payment true))])
          orders)])
      
      ;; Payment Modal 
      [stripe/payment-modal
       {:order selected-order
        :visible? show-payment
        :on-close #(do (set-show-payment false)
                       (set-selected-order nil))
        :on-success #(do (set-show-payment false)
                        (set-selected-order nil)
                        (js/alert "🎉 Payment successful! We'll get started on your order right away. Thank you for choosing us!"))
        :on-error #(do (js/alert "😔 Payment didn't go through this time. No worries - you can try again or contact us if you need help!")
                       (js/console.log "Payment failed:" %))}]]])) 