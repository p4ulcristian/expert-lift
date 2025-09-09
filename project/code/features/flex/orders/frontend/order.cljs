(ns features.flex.orders.frontend.order
  (:require
   [features.flex.orders.frontend.request :as orders-request]
   [cljs.pprint]
   [clojure.walk]
   [features.flex.orders.frontend.job-components :as job-components]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [utils.time :as time]
   [zero.frontend.react :as zero-react]))

;; ============================================================================
;; DATA & STATE
;; ============================================================================

;; ============================================================================
;; UTILITY FUNCTIONS
;; ============================================================================

(defn truncate-id [id]
  (if id (subs id 0 8) "Unknown"))

(defn get-status-color [status]
  (case status
    "pending" "#f0ad4e"
    "active" "#5bc0de"
    "done" "#5cb85c"
    "completed" "#5cb85c"
    "in-progress" "#5bc0de"
    "cancelled" "#d9534f"
    "#666"))

;; ============================================================================
;; NAVIGATION FUNCTIONS
;; ============================================================================

(defn navigate-to-orders []
  (let [wsid @(r/subscribe [:workspace/get-id])
        url (str "/flex/ws/" wsid "/orders")]
    (router/navigate! {:path url})))

;; ============================================================================
;; BASIC UI COMPONENTS
;; ============================================================================

(defn order-status-label [status]
  [:div
   {:style {:background-color (get-status-color status)
            :padding "5px"
            :border-radius 10
            :color "#333"
            :text-align :center
            :width "120px"
            :font-size 12
            :font-weight 800
            :margin-left "auto"}}
   status])

(defn jobs-overview-card [order refresh-fn]
  [job-components/jobs-overview-card order refresh-fn])

;; ============================================================================
;; CUSTOMER COMPONENTS
;; ============================================================================

(defn customer-avatar []
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
                :color "#666"}}]])

(defn customer-name [customer]
  [:div {:style {:font-weight 600
                 :color "#333"
                 :font-size "18px"}}
   (if customer
     (str (:customer/first-name customer) " " (:customer/last-name customer))
     "Unknown Customer")])

(defn customer-id-display [customer]
  [:div {:style {:color "#666"
                 :font-size "12px"}}
   "User ID: " (if-let [customer-id (:customer/id customer)]
                 (truncate-id customer-id)
                 "Unknown")])

(defn customer-header-info [customer]
  [:div
   [customer-name customer]
   [customer-id-display customer]])

(defn customer-header [customer]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :margin-bottom "25px"}}
   [customer-avatar]
   [customer-header-info customer]])

(defn contact-item [icon-class text]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "12px"
                 :min-height "24px"}}
   [:i {:class icon-class
        :style {:color "#666"
                :width "20px"
                :text-align "center"}}]
   [:div {:style {:color "#666"}} text]])

(defn customer-contact-info [customer]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "12px"
                 :background-color "#f8f9fa"
                 :padding "15px"
                 :border-radius "8px"}}
   [contact-item "fas fa-envelope" 
    (or (:customer/email customer) "No email provided")]])

(defn customer-details-card [customer]
  (when customer
    [:div
     [customer-header customer]
     [customer-contact-info customer]]))

;; ============================================================================
;; TIMELINE COMPONENTS
;; ============================================================================

(defn timeline-step-icon [status is-active?]
  [:div {:style {:width "24px"
                 :height "24px"
                 :border-radius "12px"
                 :background-color (if is-active? "#5bc0de" "#e0e0e0")
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :margin-bottom "8px"
                 :z-index 2}}
   (when (= status "completed")
     [:i {:class "fas fa-check"
          :style {:color "white"
                  :font-size "12px"}}])])

(defn timeline-connector [is-last? is-active? status]
  (when-not is-last?
    [:div {:style {:position "absolute"
                   :top "12px"
                   :left "50%"
                   :right "-50%"
                   :height "2px"
                   :background-color (if is-active?
                                      "#5bc0de"
                                      "#e0e0e0")
                   :z-index 1}}]))

(defn timeline-step-label [label is-active?]
  [:div {:style {:font-size "12px"
                 :color (if is-active? "#333" "#999")
                 :text-align "center"
                 :white-space "nowrap"
                 :overflow "hidden"
                 :text-overflow "ellipsis"
                 :width "100%"}}
   label])

(defn timeline-step [label status is-last? is-active?]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :position "relative"
                 :flex 1
                 :min-width "120px"}}
   [timeline-step-icon status is-active?]
   [timeline-connector is-last? is-active? status]
   [timeline-step-label label is-active?]])

(defn order-timeline [order]
  (let [status (:order/status order)
        steps ["order-submitted" "in-progress" "job-inspected" "packing" "ready-to-transport" "customer-accepted-order"]
        current-index (.indexOf steps status)
        is-step-complete? (fn [step-index]
                           (and (>= current-index 0)
                                (< step-index current-index)))
        is-current-step? (fn [step-index]
                          (and (>= current-index 0)
                               (= step-index current-index)))]
    [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :align-items "center"
                   :margin-bottom "20px"
                   :position "relative"
                   :padding "0 12px"}}
     [timeline-step "Order Submitted" "order-submitted" false (or (is-step-complete? 0) (is-current-step? 0))]
     [timeline-step "In Production" "in-progress" false (or (is-step-complete? 1) (is-current-step? 1))]
     [timeline-step "Quality Check" "job-inspected" false (or (is-step-complete? 2) (is-current-step? 2))]
     [timeline-step "Packing" "packing" false (or (is-step-complete? 3) (is-current-step? 3))]
     [timeline-step "Shipping" "ready-to-transport" false (or (is-step-complete? 4) (is-current-step? 4))]
     [timeline-step "Complete" "customer-accepted-order" true (or (is-step-complete? 5) (is-current-step? 5))]]))

;; ============================================================================
;; CARD COMPONENTS
;; ============================================================================

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

(defn order-header-info [order]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :align-items "center"
                 :margin-bottom "30px"}}
   [:div {:style {:font-weight 600
                  :color "#333"}}
    "#" (truncate-id (:order/id order))]
   [order-status-label (or (:order/status order) "unknown")]])

(defn order-created-info [created-at]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"
                 :margin-top "30px"
                 :justify-content "flex-end"
                 :font-size "12px"
                 :color "#999"}}
   [:div "Created:"]
   [:div (if created-at (time/format-date-str created-at) "Unknown")]])

(defn notification-form [message set-message]
  [:div {:style {:margin-top "16px"
                 :padding "16px"
                 :background-color "#f8f9fa"
                 :border-radius "8px"
                 :border "1px solid #e0e0e0"}}
   [:textarea {:value message
               :on-change #(set-message (.. ^js % -target -value))
               :style {:width "100%"
                      :min-height "100px"
                      :padding "12px"
                      :border "1px solid #ddd"
                      :border-radius "4px"
                      :margin-bottom "12px"
                      :font-family "inherit"
                      :resize "vertical"}}]
   [:button {:style {:background-color "#5cb85c"
                     :color "white"
                     :border "none"
                     :padding "8px 16px"
                     :border-radius "4px"
                     :font-weight "600"
                     :cursor "pointer"
                     :transition "background-color 0.2s"}
             :on-mouse-over #(.. % -target (setAttribute "style" "background-color: #4cae4c; color: white; border: none; padding: 8px 16px; border-radius: 4px; font-weight: 600; cursor: pointer; transition: background-color 0.2s"))
             :on-mouse-out #(.. % -target (setAttribute "style" "background-color: #5cb85c; color: white; border: none; padding: 8px 16px; border-radius: 4px; font-weight: 600; cursor: pointer; transition: background-color 0.2s"))
             :on-click #(js/alert message)}
    "Send Message"]])

(defn order-tracking-card [order]
  (let [[show-notification set-show-notification] (zero-react/use-state false)
        [message set-message] (zero-react/use-state "Your order is ready, come pick it up. In our opening hours")]
    [:div
     [order-header-info order]
     [order-timeline order]
     [order-created-info (:order/created-at order)]
     (when (= (:order/status order) "ready-to-transport")
       [:div {:style {:margin-top "20px"
                      :text-align "center"
                      :display "flex"
                      :flex-direction "column"
                      :align-items "center"
                      :gap "12px"}}
        [:div {:style {:display "flex"
                       :justify-content "center"
                       :gap "12px"}}
         [:button {:style {:background-color "#5bc0de"
                          :color "white"
                          :border "none"
                          :padding "10px 20px"
                          :border-radius "4px"
                          :font-weight "600"
                          :cursor "pointer"
                          :transition "background-color 0.2s"}
                   :on-mouse-over #(.. % -target (setAttribute "style" "background-color: #4ba8c8; color: white; border: none; padding: 10px 20px; border-radius: 4px; font-weight: 600; cursor: pointer; transition: background-color 0.2s"))
                   :on-mouse-out #(.. % -target (setAttribute "style" "background-color: #5bc0de; color: white; border: none; padding: 10px 20px; border-radius: 4px; font-weight: 600; cursor: pointer; transition: background-color 0.2s"))}
          "Deliver"]
         [:button {:style {:background-color "#5cb85c"
                          :color "white"
                          :border "none"
                          :padding "10px 20px"
                          :border-radius "4px"
                          :font-weight "600"
                          :cursor "pointer"
                          :transition "background-color 0.2s"}
                   :on-mouse-over #(.. % -target (setAttribute "style" "background-color: #4cae4c; color: white; border: none; padding: 10px 20px; border-radius: 4px; font-weight: 600; cursor: pointer; transition: background-color 0.2s"))
                   :on-mouse-out #(.. % -target (setAttribute "style" "background-color: #5cb85c; color: white; border: none; padding: 10px 20px; border-radius: 4px; font-weight: 600; cursor: pointer; transition: background-color 0.2s"))
                   :on-click #(set-show-notification (not show-notification))}
          "Notify Customer"]]
        (when show-notification
          [notification-form message set-message])])]))

;; ============================================================================
;; LOADING & ERROR STATES
;; ============================================================================

(defn loading-state-view []
  [:div {:style {:text-align "center" :padding "40px"}}
   [:div "Loading order..."]
   [:div {:style {:margin-top "10px"}}
    [:i {:class "fas fa-spinner fa-spin" 
         :style {:font-size "24px" :color "#ccc"}}]]])

(defn order-not-found-view []
  [:div {:style {:text-align "center" :padding "40px"}}
   [:div {:style {:color "#d9534f" :font-size "18px" :margin-bottom "10px"}}
    "Order not found"]
   [:div {:style {:color "#666" :margin-bottom "20px"}}
    "This order doesn't exist or you don't have permission to view it."]
   [:button {:on-click navigate-to-orders
             :style {:background "#5bc0de"
                     :border "none"
                     :color "white"
                     :padding "10px 20px"
                     :border-radius "4px"
                     :cursor "pointer"}}
    "Back to Orders"]])


(defn remove-texture-material [data]
  (clojure.walk/postwalk
   (fn [item]
     (if (map? item)
       (dissoc item :texture :material)
       item))
   data))

(defn order-raw-data-card [order]
  [:div
   [:pre {:style {:background-color "#f8f9fa"
                  :border-radius "8px"
                  :padding "20px"
                  :margin-bottom "20px"
                  :border "1px solid #e9ecef"
                  :font-family "monospace"
                  :font-size "10px"
                  :color "#333"
                  :overflow-x "auto"
                  :white-space "pre-wrap"
                  :max-height "400px"
                  :overflow-y "scroll"}}
    (with-out-str (cljs.pprint/pprint (remove-texture-material order)))]])

(defn order-content [order refresh-fn]
  [:div {:style {:max-width "800px"
                 :margin "20px auto"
                 :padding "20px"}}
   [card-container "Order Tracking" [order-tracking-card order]]
   [card-container "Jobs" [jobs-overview-card order refresh-fn]]
   [card-container "Customer Details" [customer-details-card (:order/customer order)]]])

;; ============================================================================
;; DATA FETCHING
;; ============================================================================

(defn handle-order-response [response]
  (r/dispatch [:db/assoc-in [:flex/order :loading?] false])
  (let [order-data (:orders/get-order response)]
    (cond
      (and (sequential? order-data) (empty? order-data))
      (r/dispatch [:db/assoc-in [:flex/order :data] nil])
      
      (nil? order-data)
      (r/dispatch [:db/assoc-in [:flex/order :data] nil])
      
      (map? order-data)
      (r/dispatch [:db/assoc-in [:flex/order :data] order-data])
      
      (and (sequential? order-data) (not-empty order-data))
      (r/dispatch [:db/assoc-in [:flex/order :data] (first order-data)])
      
      :else
      (r/dispatch [:db/assoc-in [:flex/order :data] nil]))))

(defn get-order-data [workspace-id id]
  (when workspace-id
    (r/dispatch [:db/assoc-in [:flex/order :loading?] true])
    (r/dispatch [:db/assoc-in [:flex/order :data] nil])
    (orders-request/get-order
     workspace-id
     id
     handle-order-response)))

;; ============================================================================
;; MAIN COMPONENTS
;; ============================================================================

(defn order []
  (let [order @(r/subscribe [:db/get-in [:flex/order :data]])
        loading @(r/subscribe [:db/get-in [:flex/order :loading?]])
        wsid @(r/subscribe [:workspace/get-id])
        refresh-fn #(get-order-data wsid (:order/id order))]
    [body/view
     {:title "Order Inspection"
      :description "View and manage order details, track progress, and handle customer communications."
      :title-buttons (list
                      ^{:key "back"}
                      [button/view {:mode :outlined
                                   :color "var(--seco-clr)"
                                   :style {:fontWeight 500 
                                          :padding "8px 20px"}
                                   :on-click #(router/navigate! {:path (str "/flex/ws/" wsid "/orders")})}
                       "Back"])
      :body (cond
              loading [loading-state-view]
              (and (not loading) (nil? order)) [order-not-found-view]
              :else [order-content order refresh-fn])}]))

(defn view []
  (let [order-id @(r/subscribe [:db/get-in [:router :path-params :order-id]])
        workspace-id @(r/subscribe [:workspace/get-id])]
    (zero-react/use-effect
     {:mount (fn []
               (get-order-data workspace-id order-id))
      :params #js[workspace-id order-id]})
    [:div [order]]))