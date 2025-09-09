
(ns features.customizer.checkout.frontend.blocks.delivery
  (:require
    [re-frame.core :as r]
    [ui.text-field :as text-field]))

(def rules
  [{:test #(empty? %) :msg "Input cannot be empty"}
   {:test #(not (re-matches #"^[a-zA-ZÀ-ÿ\s]+$" %)) :msg "Only letters and spaces allowed"}
   {:test #(> (count %) 50) :msg "Input is too long (max 50 chars)"}])

(defn- zipcode-field [delivery-data]
  [text-field/view {:label       "Zip"
                    :placeholder "xxxxx-xxxx"
                    :value       (:zipcode delivery-data)
                    :rules       rules
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :delivery :zipcode] %])}])

(defn- city-field [delivery-data]
  [text-field/view {:label       "City"
                    :placeholder "City"
                    :value       (:city delivery-data)
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :delivery :city] %])}])

(defn- state-field [delivery-data]
  [text-field/view {:label       "State"
                    :placeholder "State"
                    :value       (:state delivery-data)
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :delivery :state] %])}])

(defn- address-field [delivery-data]
  [text-field/view {:label       "Address"
                    :placeholder ""
                    :value       (:address delivery-data)
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :delivery :address] %])}])

(defn- delivery-form []
  (let [delivery-data @(r/subscribe [:db/get-in [:checkout :delivery]])]
    [:div {:id    "checkout--delivery-form"
           :class "checkout--bg-box"}
      [:div 
        [:p {:style {:margin-bottom "15px"}} "Delivery details"]
        [:div {:style {:display               "grid"
                       :row-gap               "15px"
                       :grid-template-columns "1fr"}}
          [zipcode-field delivery-data]
          [city-field delivery-data]
          [state-field delivery-data]
          [address-field delivery-data]]]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Subscriptions ----

(defn valid? [[k v]]
  (or
   (not (nil? v))
   (not (empty? v))))

(r/reg-sub
 :checkout.delivery/valid?
 (fn [db [_]]
   (let [delivery-data (get-in db [:checkout :delivery])]
     (not (empty? (:zipcode delivery-data))))))

;; ---- Subscriptions ----
;; -----------------------------------------------------------------------------

(defn view []
  [delivery-form])