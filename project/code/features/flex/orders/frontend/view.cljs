(ns features.flex.orders.frontend.view
  (:require
   [clojure.string :as clojure.string]
   [features.flex.orders.frontend.request :as orders-request]
   [features.flex.orders.frontend.events]
   [features.flex.shared.frontend.components.body :as body]
   [reagent.core :refer [atom]]
   [re-frame.core :as rf]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.table.zero :as table]
   [ui.text-field :as text-field]
   [utils.time :as time]
   [zero.frontend.re-frame :refer [subscribe]]
   [zero.frontend.react :as zero-react]))


(def search-state (atom {:search/limit 50
                        :search/offset 0
                        :search/text ""
                        :search/order-by :order/created-at}))


(defn created-at-label [cell-value row] 
  [:div {:style {:font-size "14px"
                 :line-height "1.5"}}
   (time/format-date-str cell-value)])

(defn order-status-label [cell-value row]
  (when cell-value
    [:div 
     {:style {:background-color (case cell-value 
                                  "order-submitted" "#f0ad4e"
                                  "package-arrived" "#5bc0de"
                                  "parts-inspected" "#17a2b8"
                                  "waiting-to-start" "#6c757d"
                                  "process-planning" "#007bff"
                                  "batch-assigned" "#28a745"
                                  "in-progress" "#ffc107"
                                  "job-paused-on-hold" "#fd7e14"
                                  "job-inspected" "#20c997"
                                  "job-complete" "#5cb85c"
                                  "packing" "#6f42c1"
                                  "outbound-shipping-ordered" "#e83e8c"
                                  "sent-to-customer" "#17a2b8"
                                  "arrived-at-customer" "#28a745"
                                  "customer-accepted-order" "#5cb85c"
                                  "attention-needed" "#dc3545"
                                  "invoice-issued" "#6c757d"
                                  "invoice-sent" "#007bff"
                                  "invoice-paid" "#28a745"
                                  "awaiting-customer-response" "#ffc107"
                                  "awaiting-parts" "#fd7e14"
                                  "rework-required" "#dc3545"
                                  "cancelled" "#d9534f"
                                  "declined" "#6c757d"
                                  "quote-sent" "#17a2b8"
                                  "#6c757d")
              :padding "4px 8px"
              :border-radius 12
              :color "white"
              :text-align :center
              :font-size 10
              :font-weight 600
              :white-space :nowrap}}
     (clojure.string/replace cell-value "-" " ")]))

(defn urgency-label [cell-value row]
  (when cell-value
    [:div 
     {:style {:background-color (case cell-value
                                  "low" "#28a745"
                                  "normal" "#6c757d"
                                  "high" "#ffc107"
                                  "critical" "#fd7e14"
                                  "rush" "#dc3545"
                                  "#6c757d")
              :padding "4px 8px"
              :border-radius 12
              :color "white"
              :text-align :center
              :font-size 10
              :font-weight 600
              :text-transform :uppercase}}
     cell-value]))

(defn source-label [cell-value row]
  (when cell-value
    [:div 
     {:style {:background-color (case cell-value
                                  "iron-rainbow" "#007bff"
                                  "local" "#28a745"
                                  "#6c757d")
              :padding "6px 8px"
              :border-radius 12
              :color "white"
              :text-align :center
              :font-size 12
              :font-weight 600
              :display :flex
              :align-items :center
              :justify-content :center
              :min-width "32px"
              :height "24px"}}
     [:i {:class (case cell-value
                   "iron-rainbow" "fas fa-globe"     ; Globe icon for webshop/remote
                   "local" "fas fa-home"             ; Home icon for local
                   "fas fa-question")}]]))

(defn navigate-to-order [order-id]
  (let [wsid @(subscribe [:workspace/get-id]) 
        url (str "/flex/ws/" wsid "/orders/" order-id)]
    (router/navigate! {:path url})))


(defn orders [] 
  (let [orders @(subscribe [:flex/orders-get])]
    [table/view 
     {:rows (or orders [])
    :grid-template-columns "1fr auto auto auto auto auto"
    :columns [:order/customer :order/created-at :order/status :order/urgency :order/source :actions]
    :labels {:order/customer "Customer"
             :order/created-at "Created"
             :order/status "Status"  
             :order/urgency "Priority"
             :order/source "Source"
             :actions "Actions"}
    :header-style {:background "#f8fafc"
                   :border-bottom "1px solid #e2e8f0"
                   :font-weight "600"
                   :font-size "14px"
                   :color "#374151"
                   :padding "12px 8px"
                   :text-transform "uppercase"
                   :letter-spacing "0.5px"}
    :column-elements 
    {:order/created-at created-at-label
     :order/status order-status-label
     :order/urgency urgency-label
     :order/source source-label
     :order/customer (fn [cell-value row]
                       [:div {:style {:font-size "14px"
                                     :line-height "1.5"}}
                        [:div {:style {:font-weight "500"}} 
                         (str (:customer/first-name cell-value) " " (:customer/last-name cell-value))]
                        [:div {:style {:color "#666"
                                      :font-size "12px"}} 
                         (:customer/email cell-value)]])
     :actions (fn [_ row]
                [:div {:style {:display :flex
                              :gap "8px"
                              :align-items :center}}
                 [button/view 
                  {:mode :outlined
                   :color "var(--seco-clr)"
                   :style {:padding "6px 12px"
                           :font-size "12px"
                           :min-width "auto"
                           :height "32px"}
                   :on-click #(navigate-to-order (:order/id row))}
                  [:i {:class "fas fa-eye" 
                       :style {:margin-right "4px"}}] 
                  "View"]
                 [button/view 
                  {:mode (if (or (= (:order/status row) "package-arrived")
                               (= (:order/status row) "cancelled")) :disabled :filled)
                   :color "var(--prim-clr)"
                   :style {:padding "6px 12px"
                           :font-size "12px"
                           :min-width "auto"
                           :height "32px"}
                   :disabled (or (= (:order/status row) "package-arrived")
                               (= (:order/status row) "cancelled"))
                   :on-click (fn [e]
                              (.stopPropagation ^js e)
                              (let [workspace-id @(subscribe [:workspace/get-id])]
                                (orders-request/edit-order
                                 workspace-id
                                 {:order-id (:order/id row)}
                                 (fn [response]
                                   (rf/dispatch [:flex/orders workspace-id])))))}
                  [:i {:class "fas fa-box" 
                       :style {:margin-right "4px"}}] 
                  "Arrived"]
                 [button/view 
                  {:mode :filled
                   :color "#dc3545"
                   :style {:padding "6px 12px"
                           :font-size "12px"
                           :min-width "auto"
                           :height "32px"}
                   :on-click (fn []
                              (let [workspace-id @(subscribe [:workspace/get-id])]
                                (orders-request/delete-order
                                 workspace-id
                                 (:order/id row)
                                 (fn [response]
                                   (rf/dispatch [:flex/orders workspace-id])))))}
                  [:i {:class "fas fa-trash" 
                       :style {:margin-right "4px"}}] 
                  "Cancel"]])}
    :row-element (fn [style content]
                   [:div {:style (merge style 
                                       {:transition "all 0.2s ease"
                                        :cursor "pointer"}
                                       {:hover {:background "rgba(255, 215, 13, 0.05)"}})
                         :on-mouse-enter #(-> % .-target .-style 
                                            (.setProperty "background" "rgba(255, 215, 13, 0.05)"))
                         :on-mouse-leave #(-> % .-target .-style 
                                            (.setProperty "background" "transparent"))}
                    content])}]))

(defn search-orders []
  [:div {:style {:padding 10}}
   [text-field/view
    {:value (:search/text @search-state)
     :on-change #(swap! search-state assoc :search/text %)
     :on-type-ended {:fn  (fn [] 
                            (let [workspace-id @(subscribe [:workspace/get-id])]
                              (rf/dispatch [:flex/orders workspace-id])))}
     :placeholder "Search orders..."
     :style {:width "100%"}}]])

(defn orders-content []
  [:div
   ;; Search Section - 24px padding for data-heavy pages
   [:div {:style {:margin-bottom "24px"}}
    [search-orders]]
   
   ;; Orders Table
   [orders]])

(defn view []
  (let [workspace-id @(subscribe [:workspace/get-id])]
    (zero-react/use-effect
     {:mount (fn []
               (rf/dispatch [:flex/orders workspace-id]))
      :params #js[workspace-id]})
    [body/view
     {:title "Orders"
      :description "Manage and track customer orders, including order status, fulfillment progress, and delivery schedules. View order details, update statuses, and coordinate with your team."
      :body [orders-content]}]))