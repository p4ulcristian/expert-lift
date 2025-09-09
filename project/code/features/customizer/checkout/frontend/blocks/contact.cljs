
(ns features.customizer.checkout.frontend.blocks.contact
  (:require
    ["react" :as react]
    [re-frame.core :as r]
    [clojure.string :as string]
    [ui.button :as button]
    [ui.checkbox   :as checkbox]
    [ui.text-field :as text-field]))

;; -----------------------------------------------------------------------------
;; ---- Rules ----

(def rules
  [{:test #(empty? %) :msg "Input cannot be empty"}
   {:test #(not (re-matches #"^[a-zA-ZÀ-ÿ\s]+$" %)) :msg "Only letters and spaces allowed"}
   {:test #(> (count %) 50) :msg "Input is too long (max 50 chars)"}])

(def email-rules
  [{:test #(empty? %) :msg "Input cannot be empty"}
   {:test #(not (re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$" %))
    :msg  "Not valid email!"}])

(def phone-rules
  [{:test #(not (re-matches #"^\(\d{3}\) \d{3}-\d{4}$" %)) :msg "Not valid phone number!"}])

(def zip-rules
  [{:test #(empty? %) :msg "ZIP code is required"}
   {:test #(not (re-matches #"^\d{5}$" %)) :msg "ZIP code must be 5 digits"}])

(def business-rules
  [{:test #(empty? %) :msg "This field is required"}
   {:test #(> (count %) 100) :msg "Input is too long (max 100 chars)"}])

(def tax-id-rules
  [{:test #(empty? %) :msg "Tax ID is required"}
   {:test #(not (re-matches #"^\d{2}-\d{7}$" %)) :msg "Tax ID format: XX-XXXXXXX"}])

;; ---- Rules ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn sanitize-phone-number [phone-number]
  (clojure.string/replace phone-number #"[^0-9]" ""))

(defn format-phone-number [value]
  (cond
    (< 0 (count value) 4) (clojure.string/replace value #"(\d{1,3})" "($1)")
    (< 3 (count value) 7) (clojure.string/replace value #"(\d{1,3})(\d{1,3})" "($1) $2")
    (< 6 (count value) 11) (clojure.string/replace value #"(\d{1,3})(\d{1,3})(\d{1,4})" "($1) $2-$3")
    :else value))

(defn clear-input-button [path]
  [button/view {:on-click #(r/dispatch [:db/dissoc-in path])
                :mode :clear_2
                :style {:padding "4px 8px"
                        :position "absolute"
                        :right "6px"}}
    [:i {:class "fas fa-times"}]])

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Contact Info ----

(defn- client-first-name-field [client-data]
  [text-field/view {:label       "First Name"
                    :placeholder "Joe"
                    :rules       rules
                    :required    true
                    :value       (:first-name client-data)
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :client :first-name] %])}])

(defn client-last-name-field [client-data]
  [text-field/view {:label       "Last Name"
                    :placeholder "Doe"
                    :rules       rules
                    :required    true
                    :value       (:last-name client-data)
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :client :last-name] %])}])

(defn client-email-field [client-data]
  [text-field/view {:label       "Email Address"
                    :placeholder "example@email.com"
                    :rules       email-rules
                    :required    true
                    :value       (:email-address client-data)
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :client :email-address] %])}])

(defn client-phone-field [client-data]
  [text-field/view {:label       "Phone Number (Optional)"
                    :placeholder "123-456-7899"
                    :rules       phone-rules
                    :value       (format-phone-number (:phone-number client-data))
                    :on-change   #(let [value (sanitize-phone-number %)]
                                    (r/dispatch [:db/assoc-in [:checkout :client :phone-number] value]))}])
                                  

(defn client-contact-info-fields [client-data]
  [:div {:class "inner-box"
         :style {:display               "grid"
                 :row-gap               "15px"
                 :grid-template-columns "1fr"}}
    [:p  "Personal information"]

    [client-first-name-field client-data]
    [client-last-name-field client-data]
    [client-email-field client-data]
    [client-phone-field client-data]])

;; ---- Contact Info ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Address ----

(defn client-address-field [client-data]
  [text-field/view {:label       "Address"
                    :placeholder "123 Main Street"
                    :required    true
                    :rules       rules
                    :value       (:address client-data)
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :client :address] %])}])

(defn client-city-field [client-data]
  [text-field/view {:label       "City"
                    :placeholder "New York"
                    :rules       rules
                    :required    true
                    :value       (:city client-data)
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :client :city] %])}])

(defn client-state-field [client-data]
  [text-field/view {:label       "State"
                    :placeholder "NY"
                    :rules       rules
                    :required    true
                    :value       (:state client-data)
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :client :state] %])}])

(defn client-zip-field [client-data]
  [text-field/view {:label       "ZIP Code"
                    :placeholder "10001"
                    :required    true
                    :rules       zip-rules
                    :value       (:zip client-data)
                    :on-change   #(r/dispatch [:db/assoc-in [:checkout :client :zip] %])}])

(defn client-address-fields [client-data]
  [:div {:class "inner-box"}
   
    [:p {:style {:margin-bottom "15px"}}
        "Address information"]
   
    [client-address-field client-data]
    [:div {:style {:display "grid"
                   :margin-top "15px"
                   :grid-template-columns "repeat(auto-fill, minmax(150px, 1fr))"
                   :gap "15px"}}
     [client-city-field client-data]
     [client-state-field client-data]
     [client-zip-field client-data]]])

;; ---- Address ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Business ----

(defn business-name-field [business-data]
  [text-field/view {:label "Business / Company Name"
                    :placeholder "Acme Corporation"
                    :value (:business-name business-data)
                    :required  true
                    :rules business-rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :business-name] %])}])

(defn business-type-field [business-data]
  [text-field/view {:label "Business Type"
                    :placeholder "LLC, Corporation, Partnership, etc."
                    :value (:business-type business-data)
                    :required  true
                    :rules business-rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :business-type] %])}])

(defn business-tax-id-field [business-data]
  [text-field/view {:label "Tax ID / EIN"
                    :placeholder "12-3456789"
                    :value (:tax-id business-data)
                    :required  true
                    :rules tax-id-rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :tax-id] %])}])

(defn business-phone-number-field [business-data]
  [text-field/view {:label "Business Phone Number (Optional)"
                    :placeholder "(555) 123-4567"
                    :rules phone-rules
                    :right-adornment [clear-input-button [:checkout :business :phone-number]]
                    :value (format-phone-number (:phone-number business-data))
                    :disabled  (not (:business? business-data))
                    :on-change #(let [value (sanitize-phone-number %)]
                                  (r/dispatch [:db/assoc-in [:checkout :business :phone-number] value]))}])

(defn business-email-address-field [business-data]
  [text-field/view {:label "Business Email Address"
                    :placeholder "orders@company.com"
                    :value (:email-address business-data)
                    :required  true
                    :rules email-rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :email-address] %])}])

(defn business-address-field [business-data]
  [text-field/view {:label "Business Billing Address"
                    :placeholder "123 Business Street"
                    :value (:address business-data)
                    :required  true
                    :rules business-rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :address] %])}])

(defn business-city-field [business-data]
  [text-field/view {:label "City"
                    :placeholder "Business City"
                    :value (:city business-data)
                    :required  true
                    :rules rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :city] %])}])

(defn business-state-field [business-data]
  [text-field/view {:label "State"
                    :placeholder "NY"
                    :value (:state business-data)
                    :required  true
                    :rules rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :state] %])}])

(defn business-zip-field [business-data]
  [text-field/view {:label "ZIP Code"
                    :placeholder "10001"
                    :value (:zip business-data)
                    :required  true
                    :rules zip-rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :zip] %])}])

(defn business-contact-name-field [business-data]
  [text-field/view {:label "Business Contact Name"
                    :placeholder "John Smith"
                    :value (:contact-name business-data)
                    :required  true
                    :rules rules
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :contact-name] %])}])

(defn business-contact-title-field [business-data]
  [text-field/view {:label "Contact Title (Optional)"
                    :placeholder "Purchasing Manager"
                    :value (:contact-title business-data)
                    :disabled  (not (:business? business-data))
                    :on-change #(r/dispatch [:db/assoc-in [:checkout :business :contact-title] %])}])

(defn set-initial-business-data [business-data client-data]
  (when (empty? business-data)
    (r/dispatch [:db/assoc-in [:checkout :business] 
                  {:address (:address client-data) 
                   :city    (:city client-data)
                   :state   (:state client-data) 
                   :zip     (:zip client-data)
                   :phone-number (:phone-number client-data)
                   :email-address (:email-address client-data)
                   :contact-name (str (:first-name client-data) " " (:last-name client-data))}])))
                   
                   
                   

(defn business-info-fields [client-data]
  (let [business-data @(r/subscribe [:db/get-in [:checkout :business]])]
    [:div {:class "inner-box"
           :style {:display "grid"
                   :gap      "15px"}}
      [checkbox/view {:label     "I am a business"
                      :style     {:width "100%"}
                      :checked?  (:business? business-data)
                      :on-change #(do 
                                    (set-initial-business-data business-data client-data)
                                      
                                    (r/dispatch [:db/update-in [:checkout :business :business?] not]))}]
      
      [:div {:style {:gap      "15px"
                     :display  (if (:business? business-data) "grid" "none")}}
        
        ;; Business Information Section
        [:div {:style {:display "grid" :gap "15px"}}
          [:p {:style {:margin-bottom "10px" :font-weight "600"}} "Business Information"]
          [business-name-field business-data]
          [business-type-field business-data]
          [business-tax-id-field business-data]]
        
        ;; Contact Information Section
        [:div {:style {:display "grid" :gap "15px"}}
          [:p {:style {:margin-bottom "10px" :font-weight "600"}} "Business Contact"]
          [business-contact-name-field business-data]
          [business-contact-title-field business-data]
          [business-phone-number-field business-data]
          [business-email-address-field business-data]]
        
        ;; Billing Address Section
        [:div {:style {:display "grid" :gap "15px"}}
          [:p {:style {:margin-bottom "10px" :font-weight "600"}} "Business Billing Address"]
          [business-address-field business-data]
          [:div {:style {:display "grid"
                         :grid-template-columns "repeat(auto-fill, minmax(150px, 1fr))"
                         :gap "15px"}}
            [business-city-field business-data]
            [business-state-field business-data]
            [business-zip-field business-data]]]]]))


;; ---- Business ----
;; -----------------------------------------------------------------------------
  
(defn client-form []
  (let [client-data @(r/subscribe [:db/get-in [:checkout :client]])]
    [:div {:id    "checkout--client-form"
           :class "checkout--bg-box"
           :style {:display "flex"
                   :flex-direction "column"
                   :gap "30px"}}
      
      [client-contact-info-fields client-data]
      
      [client-address-fields client-data]
      
      [business-info-fields client-data]]))


;; ---- Components ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Subscriptions ----

(r/reg-sub
  :checkout.contact/valid?
  (fn [db [_]]
    (let [client-data (get-in db [:checkout :client])
          business-data (get-in db [:checkout :business])]
       (and (every? false? [(empty? (:first-name client-data))
                            (empty? (:last-name client-data))
                            (empty? (:email-address client-data))
                            (empty? (:address client-data))
                            (empty? (:city client-data))
                            (empty? (:state client-data))
                            (empty? (:zip client-data))])
           (if (:business? business-data)
             (every? false?
               [(empty? (:business-name business-data))
                (empty? (:business-type business-data))
                (empty? (:tax-id business-data))
                (empty? (:contact-name business-data))
                (empty? (:email-address business-data))
                (empty? (:address business-data))
                (empty? (:city business-data))
                (empty? (:state business-data))
                (empty? (:zip business-data))])
             true)))))                
  
                               
;; ---- Subscriptions ----
;; -----------------------------------------------------------------------------

(defn view []
  (react/useEffect 
    (fn []
      (when-let [user-data @(r/subscribe [:db/get-in [:user-profile]])]
        (when-not @(r/subscribe [:db/get-in [:checkout :client]])
          (r/dispatch [:db/assoc-in [:checkout :client] {:first-name   (first (clojure.string/split (:name user-data) #" "))
                                                         :last-name    (last (clojure.string/split (:name user-data) #" "))
                                                         :email-address (:email user-data)
                                                         :phone-number (:phone-number user-data)
                                                         :address      (:address user-data)
                                                         :city         (:city user-data)
                                                         :state        (:state user-data)
                                                         :zip          (:zip user-data)}])))
      (fn []))
    #js[])
  [client-form])