(ns features.flex.business-info.frontend.view
  (:require
   [features.flex.business-info.frontend.request :as business-info-request]
   [clojure.string :as clojure.string]
   [features.flex.shared.frontend.components.body :as body]
   [zero.frontend.react :as zero-react]
   [zero.frontend.re-frame :refer [subscribe dispatch]]))

(def us-states
  ["Alabama" "Alaska" "Arizona" "Arkansas" "California" "Colorado" "Connecticut" 
   "Delaware" "Florida" "Georgia" "Hawaii" "Idaho" "Illinois" "Indiana" "Iowa" 
   "Kansas" "Kentucky" "Louisiana" "Maine" "Maryland" "Massachusetts" "Michigan" 
   "Minnesota" "Mississippi" "Missouri" "Montana" "Nebraska" "Nevada" "New Hampshire" 
   "New Jersey" "New Mexico" "New York" "North Carolina" "North Dakota" "Ohio" 
   "Oklahoma" "Oregon" "Pennsylvania" "Rhode Island" "South Carolina" "South Dakota" 
   "Tennessee" "Texas" "Utah" "Vermont" "Virginia" "Washington" "West Virginia" 
   "Wisconsin" "Wyoming"])

(defn text-field [{:keys [label placeholder value required? on-change]}]
  [:div {:style {:margin-bottom "15px"}}
   (when label
     [:label {:style {:display "block"
                      :font-weight "600" 
                      :color "#374151" 
                      :font-size "14px"
                      :margin-bottom "5px"}} 
      (str label (when required? " *"))])
   [:input {:type "text"
            :value (or value "")
            :placeholder placeholder
            :on-change #(when on-change (on-change (-> % .-target .-value)))
            :style {:width "100%"
                    :padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :font-size "14px"
                    :outline "none"
                    :transition "border-color 0.2s"
                    :box-sizing "border-box"}}]])

(defn select-field [{:keys [label placeholder value options required? on-change]}]
  [:div {:style {:margin-bottom "15px"}}
   (when label
     [:label {:style {:display "block"
                      :font-weight "600" 
                      :color "#374151" 
                      :font-size "14px"
                      :margin-bottom "5px"}} 
      (str label (when required? " *"))])
   [:select {:value (or value "")
             :on-change #(when on-change (on-change (-> % .-target .-value)))
             :style {:width "100%"
                     :padding "8px 12px"
                     :border "1px solid #d1d5db"
                     :border-radius "6px"
                     :font-size "14px"
                     :outline "none"
                     :background "white"
                     :transition "border-color 0.2s"
                     :box-sizing "border-box"}}
    [:option {:value ""} (or placeholder "Select...")]
    (for [option options]
      ^{:key option} [:option {:value option} option])]])


(defn address-section [{:keys [label address on-change]}]
  [:div {:style {:margin-bottom (if (empty? label) "0px" "20px")}}
   (when-not (empty? label)
     [:label {:style {:display "block"
                      :font-weight "600" 
                      :color "#374151" 
                      :font-size "14px"
                      :margin-bottom "10px"}} 
      (str label " *")])
   [:div {:style {:display "grid"
                  :gap "15px"}}
    [text-field {:placeholder "Address Line 1"
                 :value (:address address)
                 :on-change #(on-change (assoc address :address %))}]
    [:div {:style {:display "grid"
                   :grid-template-columns "2fr 1fr 1fr"
                   :gap "15px"}}
     [text-field {:placeholder "City"
                  :value (:city address)
                  :on-change #(on-change (assoc address :city %))}]
     [select-field {:placeholder "State"
                    :value (:state address)
                    :options us-states
                    :on-change #(on-change (assoc address :state %))}]
     [text-field {:placeholder "5-digit Zip"
                  :value (:zip-code address)
                  :on-change #(on-change (assoc address :zip-code %))}]]]])

(defn business-info-edit-form [{:keys [data on-change]}]
  [:div {:style {:display "grid"
                 :gap "20px"
                 :max-width "600px"
                 :width "100%"
                 :margin "0 auto"}}
   
   ;; Business Details Card
   [:div {:style {:background-color "white"
                  :border "1px solid #e5e7eb"
                  :border-radius "8px"
                  :padding "20px"
                  :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
    [:h3 {:style {:margin-top 0 
                  :margin-bottom "16px"
                  :color "#374151" 
                  :font-size "16px"
                  :font-weight "600"}} "Business Details"]
    
    [text-field {:label "Business Name"
                 :value (:business-name data)
                 :required? true
                 :on-change #(on-change (assoc data :business-name %))}]
    
    [text-field {:label "Owner Name"
                 :value (:owner-name data)
                 :required? true
                 :on-change #(on-change (assoc data :owner-name %))}]
    
    [text-field {:label "Phone Number"
                 :value (:phone-number data)
                 :required? true
                 :on-change #(on-change (assoc data :phone-number %))}]
    
    [text-field {:label "Email Address"
                 :value (:email-address data)
                 :required? true
                 :on-change #(on-change (assoc data :email-address %))}]]
   
   ;; Mailing Address Card
   [:div {:style {:background-color "white"
                  :border "1px solid #e5e7eb"
                  :border-radius "8px"
                  :padding "20px"
                  :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
    [:h3 {:style {:margin-top 0 
                  :margin-bottom "12px"
                  :color "#374151" 
                  :font-size "16px"
                  :font-weight "600"}} "Mailing Address"]
    [address-section {:label ""
                      :address (:mailing-address data)
                      :on-change #(on-change (assoc data :mailing-address %))}]]
   
   ;; Facility Address Card
   [:div {:style {:background-color "white"
                  :border "1px solid #e5e7eb"
                  :border-radius "8px"
                  :padding "20px"
                  :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
    [:h3 {:style {:margin-top 0 
                  :margin-bottom "12px"
                  :color "#374151" 
                  :font-size "16px"
                  :font-weight "600"}} "Facility Address"]
    [address-section {:label ""
                      :address (:facility-address data)
                      :on-change #(on-change (assoc data :facility-address %))}]]])

(defn info-item [{:keys [label value]}]
  [:div {:style {:margin-bottom "12px"}}
   [:label {:style {:display "block"
                    :font-weight "600" 
                    :color "#6b7280" 
                    :font-size "12px"
                    :margin-bottom "3px"}} label]
   [:div {:style {:font-size "14px"
                  :color (if (or (nil? value) (empty? value)) "#9ca3af" "#374151")
                  :font-style (if (or (nil? value) (empty? value)) "italic" "normal")}} 
    (if (or (nil? value) (empty? value)) "Not set" value)]])

(defn address-display [{:keys [address]}]
  (let [{:keys [address city state zip-code]} address
        has-any-field? (or (not (empty? address))
                          (not (empty? city))
                          (not (empty? state))
                          (not (empty? zip-code)))]
    [:div {:style {:font-size "14px"
                   :color (if has-any-field? "#374151" "#9ca3af")
                   :font-style (if has-any-field? "normal" "italic")
                   :line-height "1.4"}}
     (if has-any-field?
       [:div
        [:div (if (empty? address) "" address)]
        [:div (str (if (empty? city) "" city)
                  (when (and (not (empty? city)) (not (empty? state))) ", ")
                  (if (empty? state) "" state)
                  (when (and (not (empty? state)) (not (empty? zip-code))) " ")
                  (if (empty? zip-code) "" zip-code))]]
       [:div "Address not set"])]))

(defn business-info-display [data]
  [:div {:style {:display "grid"
                 :gap "20px"
                 :max-width "600px"
                 :width "100%"
                 :margin "0 auto"}}
   
   ;; Business Details Section
   [:div {:style {:background-color "white"
                  :border "1px solid #e5e7eb"
                  :border-radius "8px"
                  :padding "20px"
                  :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
    [:h3 {:style {:margin-top 0 
                  :margin-bottom "16px"
                  :color "#374151" 
                  :font-size "16px"
                  :font-weight "600"}} "Business Details"]
    
    [info-item {:label "Business Name" :value (:business-name data)}]
    [info-item {:label "Owner" :value (:owner-name data)}]
    [info-item {:label "Phone Number" :value (:phone-number data)}]
    [info-item {:label "Email Address" :value (:email-address data)}]]
   
   ;; Mailing Address Section
   [:div {:style {:background-color "white"
                  :border "1px solid #e5e7eb"
                  :border-radius "8px"
                  :padding "20px"
                  :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
    [:h3 {:style {:margin-top 0 
                  :margin-bottom "12px"
                  :color "#374151" 
                  :font-size "16px"
                  :font-weight "600"}} "Mailing Address"]
    [address-display {:address (:mailing-address data)}]]
   
   ;; Facility Address Section
   [:div {:style {:background-color "white"
                  :border "1px solid #e5e7eb"
                  :border-radius "8px"
                  :padding "20px"
                  :box-shadow "0 1px 3px 0 rgba(0, 0, 0, 0.1)"}}
    [:h3 {:style {:margin-top 0 
                  :margin-bottom "12px"
                  :color "#374151" 
                  :font-size "16px"
                  :font-weight "600"}} "Facility Address"]
    [address-display {:address (:facility-address data)}]]])

(defn button [{:keys [type label on-click style]}]
  [:button {:style (merge {:padding "8px 16px"
                           :border-radius "6px"
                           :font-weight "500"
                           :font-size "14px"
                           :cursor "pointer"
                           :transition "all 0.2s"
                           :border (case type
                                    :primary "1px solid #3b82f6"
                                    :success "1px solid #059669"
                                    :secondary "1px solid #6b7280"
                                    "1px solid #d1d5db")
                           :background (case type
                                        :primary "#3b82f6"
                                        :success "#059669"
                                        :secondary "white"
                                        "white")
                           :color (case type
                                   :primary "white"
                                   :success "white"
                                   :secondary "#6b7280"
                                   "#374151")}
                          style)
            :on-click on-click}
   label])

(defn get-business-info [workspace-id callback]
  (business-info-request/get-business-info workspace-id callback))

(defn save-business-info [workspace-id data callback]
  (business-info-request/save-business-info workspace-id data callback))


(defn handle-save-business-info
  "Handle saving business info with notifications"
  [workspace-id form-data set-saving! set-display-data! set-form-data! set-edit-mode!]
  (set-saving! true)
  (dispatch [:notifications/loading! "save-business-info" "Saving business information..."])
  (save-business-info workspace-id form-data
                     (fn [response]
                       (set-saving! false)
                       (js/console.log "Save response:" (clj->js response))
                       (if (:error response)
                         (dispatch [:notifications/error! "save-business-info" "Failed to save business information"])
                         (do
                           (set-edit-mode! false)
                           (dispatch [:notifications/success! "save-business-info" "Business information saved successfully!"])
                           ;; Refetch the data after successful save
                           (get-business-info workspace-id
                                             (fn [fetch-response]
                                               (let [workspace-key (first (keys fetch-response))
                                                     business-info (get-in fetch-response [workspace-key :business-info/get])]
                                                 (set-display-data! business-info)
                                                 (set-form-data! business-info)))))))))

(defn create-empty-form-data
  "Create empty form data structure"
  []
  {:business-name ""
   :owner-name ""
   :phone-number ""
   :email-address ""
   :mailing-address {:address "" :city "" :state "" :zip-code ""}
   :facility-address {:address "" :city "" :state "" :zip-code ""}})



(defn business-info-content [loading? edit-mode? display-data form-data set-form-data!]
  (cond
    loading?
    [:div {:style {:text-align "center"
                   :padding "40px"
                   :max-width "600px"
                   :margin "0 auto"}}
     [:div {:style {:font-size "16px"
                    :color "#6b7280"}} "Loading business information..."]]
    
    edit-mode?
    [business-info-edit-form {:data form-data
                             :on-change set-form-data!}]
    
    display-data
    [business-info-display display-data]
    
    :else
    [:div {:style {:text-align "center"
                   :padding "40px"
                   :max-width "600px"
                   :margin "0 auto"}}
     [:div {:style {:font-size "16px"
                    :color "#6b7280"
                    :margin-bottom "16px"}} "No business information found."]
     [:div {:style {:font-size "14px"
                    :color "#9ca3af"}} "Click 'Edit' to add your business information."]]))

(defn business-info-page []
  (let [workspace-id @(subscribe [:workspace/get-id])
        [edit-mode? set-edit-mode!] (zero-react/use-state false)
        [form-data set-form-data!] (zero-react/use-state nil)
        [display-data set-display-data!] (zero-react/use-state nil)
        [loading? set-loading!] (zero-react/use-state true)
        [saving? set-saving!] (zero-react/use-state false)]
    
    (zero-react/use-effect 
     {:mount (fn []
               (when workspace-id
                 (get-business-info workspace-id
                                   (fn [response]
                                     (let [workspace-key (first (keys response))
                                           business-info (get-in response [workspace-key :business-info/get])]
                                       (set-display-data! business-info)
                                       (set-form-data! business-info)
                                       (set-loading! false))))))
      :params [workspace-id]})
    
    [body/view
     {:title "Business Information"
      :description "Manage company details and contact information."
      :title-buttons (if edit-mode?
                       (list
                        ^{:key "cancel"}
                        [button {:type :secondary
                                :label "Cancel"
                                :on-click #(do (set-edit-mode! false)
                                               (set-form-data! display-data))}]
                        ^{:key "save"}
                        [button {:type :success
                                :label (if saving? "Saving..." "Save Changes")
                                :on-click #(handle-save-business-info workspace-id form-data 
                                                                    set-saving! set-display-data! set-form-data! set-edit-mode!)}])
                       (list
                        (when-not display-data
                          ^{:key "no-data"}
                          [:div {:style {:background "#f3f4f6"
                                         :color "#6b7280"
                                         :padding "6px 12px"
                                         :border-radius "6px"
                                         :font-size "12px"
                                         :font-weight "500"}} "No Data"])
                        ^{:key "edit"}
                        [button {:type :primary
                                :label "Edit"
                                :on-click #(do
                                           (if display-data
                                             (set-form-data! display-data)
                                             (set-form-data! (create-empty-form-data)))
                                           (set-edit-mode! true))}]))
      :body [business-info-content loading? edit-mode? display-data form-data set-form-data!]}]))

(defn view []
  [business-info-page])