(ns features.site.orders.frontend.order
  (:require
   [zero.frontend.react :as react]
   [zero.frontend.re-frame :refer [subscribe]]
   [app.frontend.request :as request]))

(defn status-badge [status]
  [:span {:style {:padding "0.5rem 1rem"
                  :border-radius "9999px"
                  :font-size "0.875rem"
                  :font-weight "600"
                  :text-transform "uppercase"
                  :letter-spacing "0.05em"
                  :background (case status
                               "order-submitted" "#10b981"
                               "in-progress" "#3b82f6"
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

(defn job-card [job]
  [:div {:style {:background "white"
                 :border "1px solid #e5e7eb"
                 :border-radius "8px"
                 :padding "16px"
                 :margin-bottom "12px"
                 :display "flex"
                 :gap "16px"}}
   ;; Part image
   [:div {:style {:flex-shrink 0}}
    (if (:part_picture_url job)
      [:img {:src (:part_picture_url job)
             :alt (:part_name job)
             :style {:width "80px"
                     :height "80px"
                     :object-fit "cover"
                     :border-radius "6px"
                     :border "1px solid #e5e7eb"}}]
      [:div {:style {:width "80px"
                     :height "80px"
                     :background "#f3f4f6"
                     :border-radius "6px"
                     :display "flex"
                     :align-items "center"
                     :justify-content "center"
                     :color "#9ca3af"
                     :font-size "2rem"}}
       "📦"])]
   
   ;; Job details
   [:div {:style {:flex 1}}
    [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :align-items "flex-start"
                   :margin-bottom "8px"}}
     [:h4 {:style {:font-size "1.1rem"
                   :font-weight "600"
                   :color "#1f2937"
                   :margin 0}}
      (:part_name job)]
     [:span {:style {:font-size "1rem"
                     :font-weight "600"
                     :color "#6b7280"}}
      (str "Qty: " (:quantity job))]]
    
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "12px"
                   :margin-bottom "8px"}}
     ;; Look/Color info
     (when (:look_name job)
       [:div {:style {:display "flex"
                      :align-items "center"
                      :gap "8px"}}
        (when (:look_thumbnail job)
          [:img {:src (:look_thumbnail job)
                 :alt (:look_name job)
                 :style {:width "24px"
                         :height "24px"
                         :border-radius "4px"
                         :border "1px solid #e5e7eb"}}])
        [:div {:style {:display "flex"
                       :flex-direction "column"}}
         [:span {:style {:font-size "0.875rem"
                         :font-weight "500"
                         :color "#374151"}}
          (:look_name job)]
         [:span {:style {:font-size "0.75rem"
                         :color "#6b7280"}}
          (:color_family job)]]])
     
     ;; Material
     (when (:material job)
       [:span {:style {:padding "2px 6px"
                       :background "#f3f4f6"
                       :border-radius "4px"
                       :font-size "0.75rem"
                       :color "#374151"}}
        (:material job)])]
    
    ;; Description
    (when (and (:part_description job) (not-empty (:part_description job)))
      [:p {:style {:font-size "0.875rem"
                   :color "#6b7280"
                   :margin 0
                   :line-height "1.4"}}
       (:part_description job)])]])

(defn order-jobs [jobs]
  [:div {:style {:background "white"
                 :border-radius "12px"
                 :padding "24px"
                 :margin-top "24px"
                 :box-shadow "0 1px 3px rgba(0,0,0,0.1)"}}
   [:h3 {:style {:font-size "1.25rem"
                 :font-weight "600"
                 :color "#1f2937"
                 :margin-bottom "16px"}}
    "Parts & Finishes"]
   
   (if (empty? jobs)
     [:div {:style {:text-align "center"
                    :padding "32px"
                    :color "#6b7280"}}
      [:div {:style {:font-size "3rem"
                     :margin-bottom "8px"}} "📋"]
      [:p "No parts found for this order"]]
     [:div
      (map-indexed 
       (fn [idx job]
         ^{:key (str (:job_id job) "-" idx)}
         [job-card job])
       jobs)])])

(defn format-date [date-str]
  (when date-str
    (let [date (js/Date. date-str)]
      (.toLocaleDateString date "en-US" #js {:year "numeric"
                                            :month "short"
                                            :day "numeric"
                                            :hour "2-digit"
                                            :minute "2-digit"}))))

(defn order-header [order]
  [:div {:style {:background "linear-gradient(135deg, #2c3e50 0%, #34495e 50%, #2c3e50 100%)"
                 :color "white"
                 :padding "32px"
                 :border-radius "12px"
                 :margin-bottom "24px"
                 :box-shadow "0 8px 32px rgba(0,0,0,0.12)"}}
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "flex-start"}}
    [:div
     [:h1 {:style {:font-size "2rem" 
                   :font-weight 700 
                   :color "white" 
                   :margin "0 0 8px 0"
                   :text-shadow "0 2px 4px rgba(0,0,0,0.2)"}} 
      (str "Order #" (subs (:id order) 0 8))]
     [:p {:style {:font-size "1.1rem"
                  :margin 0
                  :opacity 0.9
                  :font-weight 400}}
      (str "Created: " (format-date (:created_at order)))]]
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "12px"}}
     [status-badge (:status order)]]]])

(defn order-details [order]
  [:div {:style {:background "white"
                 :border-radius "12px"
                 :padding "24px"
                 :box-shadow "0 1px 3px rgba(0,0,0,0.1)"}}
   [:div {:style {:display "grid"
                  :grid-template-columns "repeat(auto-fit, minmax(250px, 1fr))"
                  :gap "24px"}}
    [:div
     [:h3 {:style {:font-size "1.125rem"
                   :font-weight "600"
                   :color "#1f2937"
                   :margin-bottom "16px"}}
      "Order Information"]
     [:div {:style {:display "flex"
                    :flex-direction "column"
                    :gap "12px"}}
      [:div
       [:span {:style {:font-size "0.875rem"
                       :color "#6b7280"}}
        "Order ID"]
       [:p {:style {:margin "4px 0 0 0"
                    :font-weight "500"
                    :color "#1f2937"}}
        (:id order)]]
      [:div
       [:span {:style {:font-size "0.875rem"
                       :color "#6b7280"}}
        "Created At"]
       [:p {:style {:margin "4px 0 0 0"
                    :font-weight "500"
                    :color "#1f2937"}}
        (format-date (:created_at order))]]
      [:div
       [:span {:style {:font-size "0.875rem"
                       :color "#6b7280"}}
        "Due Date"]
       [:p {:style {:margin "4px 0 0 0"
                    :font-weight "500"
                    :color "#1f2937"}}
        (format-date (:due_date order))]]
      
      (when (:payment_intent_id order)
        [:div
         [:span {:style {:font-size "0.875rem"
                         :color "#6b7280"}}
          "Payment Intent"]
         [:p {:style {:margin "4px 0 0 0"
                      :font-weight "500"
                      :color "#1f2937"
                      :font-family "monospace"
                      :font-size "0.8rem"}}
          (:payment_intent_id order)]])]]
    
    [:div
     [:h3 {:style {:font-size "1.125rem"
                   :font-weight "600"
                   :color "#1f2937"
                   :margin-bottom "16px"}}
      "Status & Priority"]
     [:div {:style {:display "flex"
                    :flex-direction "column"
                    :gap "12px"}}
      [:div
       [:span {:style {:font-size "0.875rem"
                       :color "#6b7280"}}
        "Order Status"]
       [:div {:style {:margin-top "4px"}}
        [status-badge (:status order)]]]
      [:div
       [:span {:style {:font-size "0.875rem"
                       :color "#6b7280"}}
        "Payment Status"]
       [:div {:style {:margin-top "4px"}}
        [payment-status-badge (:payment_status order)]]]
      [:div
       [:span {:style {:font-size "0.875rem"
                       :color "#6b7280"}}
        "Urgency"]
       [:div {:style {:margin-top "4px"}}
        [urgency-badge (:urgency order)]]]
      [:div
       [:span {:style {:font-size "0.875rem"
                       :color "#6b7280"}}
        "Source"]
       [:div {:style {:margin-top "4px"}}
        [source-badge (:source order)]]]]]]])

(defn view []
  (let [order-id (subscribe [:db/get-in [:router :path-params :order-id]])
        [order set-order] (react/use-state nil)
        [loading set-loading] (react/use-state true)]
    
    (react/use-effect
     {:mount (fn []
               (set-loading true)
               (request/pathom
                {:query '[:site/order :site/order-jobs]
                 :initial-data {:order/id @order-id}
                 :callback (fn [response]
                            (set-loading false)
                            (set-order (merge (:site/order response)
                                              {:jobs (:site/order-jobs response)})))}))})
    
    (if loading
      [:div {:style {:display "flex"
                     :justify-content "center"
                     :align-items "center"
                     :min-height "400px"}}
       [:div {:style {:color "#6b7280"
                      :font-size "1.125rem"}}
        "Loading order details..."]]
      (if order
        [:div {:style {:max-width "1200px"
                       :margin "0 auto"
                       :padding "24px"}}
         [order-header order]
         [order-details order]
         [order-jobs (:jobs order)]]
        [:div {:style {:display "flex"
                       :justify-content "center"
                       :align-items "center"
                       :min-height "400px"}}
         [:div {:style {:color "#6b7280"
                        :font-size "1.125rem"}}
          "Order not found"]]))))