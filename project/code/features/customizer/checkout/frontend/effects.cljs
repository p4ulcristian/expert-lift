
(ns features.customizer.checkout.frontend.effects
  (:require
    [re-frame.core :as r]
    [features.customizer.checkout.frontend.blocks.data :as data]))

;; -----------------------------------------------------------------------------
;; ---- Create Order ----

(defn order-callback [response]
  (let [data (-> response :customizer.checkout/create-order!)]
    (r/dispatch [:db/assoc-in [:checkout :order-id] (:id data)])
    (r/dispatch [:notifications/notify! :success "Order placed successfully"])
    (r/dispatch [:router/swap-query-params! {:params {:order-id (:id data)}}])
    (r/dispatch [:customizer.checkout/clear-storage])))

(r/reg-event-fx
  :checkout/create-order!
  (fn [{:keys [db]} [_]]
    (let [cart              (get-in db [:cart :content] data/cart-content)
          workspace-id      (get-in db [:customizer/location :selected-workspace :value]
                               (get-in db [:customizer/location :selected-workspace]))
                              
          user-id           (get-in db [:user-profile :id])
          payment-intent-id (get-in db [:stripe :payment-intent-id])]
      
      {:dispatch  [:pathom/request!
                    {:callback order-callback
                     :query    [`(customizer.checkout/create-order!
                                   {:data         ~cart
                                    :workspace-id ~workspace-id
                                    :user-id      ~user-id
                                    :payment-intent-id ~payment-intent-id})]}]})))

;; ---- Create Order ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Create Payment Intent ----

(r/reg-event-fx
  :customizer.checkout/create-payment-intent
  (fn [{:keys [db]} [_  params]]
    {:pathom/request {:query    [`(:site/user-profile {})
                                 `(customizer.checkout/create-payment-intent ~params)]
                      :callback (fn [response]
                                  (let [data (get response :customizer.checkout/create-payment-intent)]
                                    (r/dispatch [:db/assoc-in [:user-profile] (get response :site/user-profile)])
                                    (r/dispatch [:db/assoc-in [:stripe] data])))}}))

;; ---- Create Payment Intent ----
;; -----------------------------------------------------------------------------

(defn payment-callback [response]
  (let [data (-> response :customizer.checkout/order-payment-success!)]
    (r/dispatch [:notifications/notify! :success "Order placed successfully"])
    (r/dispatch [:cart/clear!])
    (r/dispatch [:customizer.checkout/clear-storage])))

(r/reg-event-fx
  :checkout/payment-success!
  (fn [{:keys [db]} [_ & [result]]]
    (let [cart         (get-in db [:cart :content])
          workspace-id (get-in db [:customizer/location :selected-workspace :value])
          user-id      (get-in db [:user-profile :id])
          total-amount (get-in db [:stripe :tax-calculation :total])]
      {:dispatch  [:pathom/request!
                     {:callback payment-callback
                      :query    [`(customizer.checkout/order-payment-success!
                                    {:order-id ~result
                                     :payment-intent-id ~result
                                     :total-amount ~total-amount})]}]})))


     