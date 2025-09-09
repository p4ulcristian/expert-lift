
(ns features.customizer.checkout.frontend.blocks.payment
  (:require
    ["react" :as react]
    [re-frame.core :as r]
    [features.customizer.checkout.frontend.blocks.stripe :as stripe]
    [ui.accordion :as accordion]))

(defn payment-form [set-processing]
  [:div {:id    "checkout--payment-form"
         :class "checkout--bg-box hide-scroll"}
        ;;  :style {:overflow "auto"}}
    [accordion/view {:title "Test card"
                     :style {:background "rgba(0, 0, 0, 0.3)"
                             :color "rgba(255, 255, 255, 0.9)"}} 
      
      [:div {:style {:display "grid"
                     :grid-template-columns "1fr 1fr"}}
        [:p "Number"][:p "4242 4242 4242 4242"]
        [:p "Exp"][:p "08/28"]
        [:p "CVC"][:p "123"]]]
   
    ;; Payment button
    [stripe/payment-section {:on-start   #(do (println "start") (set-processing true))
                             :on-end     #(set-processing false)
                             :on-success #(r/dispatch [:checkout/payment-success! %])
                             :on-error   #(r/dispatch [:checkout/payment-error! %])}]])


(r/reg-sub
  :checkout.payment/valid?
  (fn [db [_]]
    true))

(defn view [set-processing]
  (react/useEffect (fn []
                     (when-not @(r/subscribe [:db/get-in [:checkout :order-id]])
                       (r/dispatch [:checkout/create-order!]))
                     (fn [])
                    #js[]))
  [payment-form set-processing]) 