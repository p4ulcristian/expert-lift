(ns features.app.addresses.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame]
            [zero.frontend.react :as zero-react]
            [ui.modal :as modal]
            [ui.form-field :as form-field]
            [ui.data-table :as data-table]
            [ui.enhanced-button :as enhanced-button]
            [ui.page-header :as page-header]
            [translations.core :as tr]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    (println "DEBUG: get-workspace-id called")
    (println "  Router state:" router-state)
    (println "  Extracted workspace-id:" workspace-id)
    workspace-id))

(defn- load-addresses-query
  "Execute ParQuery to load addresses with pagination"
  [workspace-id loading? addresses params]
  (println "DEBUG load-addresses-query called with params:" params)
  (reset! loading? true)
  (parquery/send-queries
   {:queries {:workspace-addresses/get-paginated params}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (println "DEBUG load-addresses-query response:" response)
               (reset! loading? false)
               (let [result (:workspace-addresses/get-paginated response)]
                 (reset! addresses result)))}))

(defn- get-query-type
  "Get appropriate query type for save operation"
  [is-new?]
  (if @is-new? 
    :workspace-addresses/create 
    :workspace-addresses/update))

(defn- prepare-address-data
  "Prepare address data for save"
  [address is-new?]
  (if @is-new?
    (dissoc address :address/id)
    address))

(defn- handle-save-response
  "Handle save response and update UI"
  [response query-type callback modal-address load-addresses]
  (callback)
  (if (:success (get response query-type))
    (do (reset! modal-address nil)
        (load-addresses))
    (js/alert (str "Error: " (:error (get response query-type))))))

(defn- save-address-query
  "Execute ParQuery to save address"
  [address workspace-id modal-is-new? callback modal-address load-addresses]
  (let [query-type (get-query-type modal-is-new?)
        address-data (prepare-address-data address modal-is-new?)
        context {:workspace-id workspace-id}]
    (println "DEBUG: save-address-query called")
    (println "  Workspace ID:" workspace-id)
    (println "  Query type:" query-type)
    (println "  Address data:" address-data)
    (println "  Context being sent:" context)
    (parquery/send-queries
     {:queries {query-type address-data}
      :parquery/context context
      :callback (fn [response]
                 (println "DEBUG: save-address-query response:" response)
                 (handle-save-response response query-type callback modal-address load-addresses))})))

(defn- delete-address-query
  "Execute ParQuery to delete address"
  [address-id workspace-id load-addresses]
  (parquery/send-queries
   {:queries {:workspace-addresses/delete {:address/id address-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (if (:success (:workspace-addresses/delete response))
                 (load-addresses)
                 (js/alert "Error deleting address")))}))

(defn- validate-name
  "Validate address name"
  [name]
  (< (count (str/trim (str name))) 2))

(defn- validate-address-line1
  "Validate address line 1"
  [address-line1]
  (< (count (str/trim (str address-line1))) 2))

(defn- validate-city
  "Validate city"
  [city]
  (< (count (str/trim (str city))) 2))

(defn- validate-postal-code
  "Validate postal code"
  [postal-code]
  (< (count (str/trim (str postal-code))) 2))

(defn validate-address
  "Validates address data and returns map of field errors"
  [address]
  (let [errors {}
        name (:address/name address)
        address-line1 (:address/address-line1 address)
        city (:address/city address)
        postal-code (:address/postal-code address)]
    (cond-> errors
      (validate-name name) (assoc :address/name (tr/tr :addresses/error-name))
      (validate-address-line1 address-line1) (assoc :address/address-line1 (tr/tr :addresses/error-address-line1))
      (validate-city city) (assoc :address/city (tr/tr :addresses/error-city))
      (validate-postal-code postal-code) (assoc :address/postal-code (tr/tr :addresses/error-postal-code)))))

(defn- field-label [label field-key has-error?]
  [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                   :font-size "0.875rem" :letter-spacing "0.025em"
                   :color (if has-error? "#dc3545" "#374151")}}
   label 
   (when (#{:address/name :address/address-line1 :address/city :address/postal-code} field-key) 
     [:span {:style {:color "#ef4444" :margin-left "0.25rem"}} "*"])])

(defn- input-base-props
  "Base properties for input fields"
  [field-key address has-error? attrs]
  {:value (str (get @address field-key ""))
   :on-change #(swap! address assoc field-key (.. % -target -value))
   :style (merge {:width "100%"
                  :padding "0.75rem 1rem"
                  :border (if has-error? "2px solid #dc3545" "1px solid #d1d5db")
                  :border-radius "8px"
                  :font-size "1rem"
                  :line-height "1.5"
                  :transition "border-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out"
                  :box-shadow (if has-error? 
                                "0 0 0 3px rgba(220, 53, 69, 0.1)" 
                                "0 1px 2px 0 rgba(0, 0, 0, 0.05)")
                  :outline "none"}
                 (:style attrs)
                 {:focus {:border-color (if has-error? "#dc3545" "#3b82f6")
                         :box-shadow (if has-error? 
                                       "0 0 0 3px rgba(220, 53, 69, 0.1)"
                                       "0 0 0 3px rgba(59, 130, 246, 0.1)")}})})

(defn- render-textarea
  "Render textarea input"
  [field-key address has-error? attrs]
  [:textarea (merge (dissoc attrs :type) (input-base-props field-key address has-error? attrs))])

(defn- render-text-input
  "Render text input"
  [field-key address has-error? attrs]
  [:input (merge attrs (input-base-props field-key address has-error? attrs))])

(defn- field-input
  "Render appropriate input type"
  [field-key address has-error? attrs]
  (if (= (:type attrs) "textarea")
    (render-textarea field-key address has-error? attrs)
    (render-text-input field-key address has-error? attrs)))

(defn- field-error [error-msg]
  (when error-msg
    [:div {:style {:color "#dc3545" :font-size "0.875rem" :margin-top "0.25rem"}}
     error-msg]))

(defn- form-field
  "Complete form field with label, input and error"
  [label field-key address errors attrs]
  (let [has-error? (contains? errors field-key)]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key address has-error? attrs]
     [field-error (get errors field-key)]]))

(defn- elevators-field
  "Field for managing elevators list"
  [address errors]
  (let [new-elevator (r/atom "")]
    (fn [address errors]
      (let [elevators (or (:address/elevators @address) [])
            add-elevator (fn [elevator-id]
                          (when (and elevator-id (not (some #(= % elevator-id) elevators)))
                            (swap! address assoc :address/elevators (conj elevators elevator-id))))
            remove-elevator (fn [index]
                             (swap! address assoc :address/elevators 
                                   (vec (concat (take index elevators) 
                                               (drop (inc index) elevators)))))]
        [:div {:style {:margin-bottom "1.5rem"}}
         [:label {:style {:display "block" :margin-bottom "0.5rem" :font-weight "600"
                          :font-size "0.875rem" :letter-spacing "0.025em" :color "#374151"}}
          (tr/tr :addresses/elevators)]
         
         ;; List of current elevators
         (when (seq elevators)
           [:div {:style {:margin-bottom "1rem"}}
            (for [[index elevator-id] (map-indexed vector elevators)]
              ^{:key (str "elevator-" index)}
              [:div {:style {:display "flex" :align-items "center" :margin-bottom "0.5rem"
                             :padding "0.5rem" :background "#f9fafb" :border-radius "6px"}}
               [:span {:style {:flex "1" :color "#374151" :font-size "0.875rem"}}
                (str (tr/tr :addresses/elevator-prefix) elevator-id)]
               [:button {:type "button"
                         :on-click #(remove-elevator index)
                         :style {:color "#dc3545" :background "none" :border "none"
                                :cursor "pointer" :padding "0.25rem" :font-size "0.75rem"
                                :border-radius "4px"}}
                (tr/tr :addresses/remove)]])])
         
         ;; Add new elevator input
         [:div {:style {:display "flex" :gap "0.5rem" :align-items "flex-end"}}
          [:div {:style {:flex "1"}}
           [:input {:type "text"
                    :placeholder (tr/tr :addresses/elevator-placeholder)
                    :value @new-elevator
                    :on-change #(reset! new-elevator (.. % -target -value))
                    :style {:width "100%" :padding "0.75rem 1rem" :border "1px solid #d1d5db"
                            :border-radius "8px" :font-size "1rem"}}]]
          [:button {:type "button"
                    :on-click #(do (add-elevator @new-elevator) 
                                  (reset! new-elevator ""))
                    :disabled (empty? (str/trim @new-elevator))
                    :style {:padding "0.75rem 1.5rem" :background "#10b981" :color "white"
                            :border "none" :border-radius "8px" :font-size "0.875rem"
                            :cursor (if (empty? (str/trim @new-elevator)) "not-allowed" "pointer")
                            :opacity (if (empty? (str/trim @new-elevator)) "0.5" "1")}}
           (tr/tr :addresses/add-elevator)]]
         
         [:p {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.5rem"}}
          (tr/tr :addresses/elevator-description)]]))))

(defn- form-fields
  "All form input fields"
  [address errors]
  [:div
   [form-field (tr/tr :addresses/name) :address/name address errors
    {:type "text" :placeholder (tr/tr :addresses/name-placeholder)}]
   [form-field (tr/tr :addresses/address-line1) :address/address-line1 address errors
    {:type "text" :placeholder (tr/tr :addresses/address-line1-placeholder)}]
   [form-field (tr/tr :addresses/address-line2) :address/address-line2 address errors
    {:type "text" :placeholder (tr/tr :addresses/address-line2-placeholder)}]
   [form-field (tr/tr :addresses/city) :address/city address errors
    {:type "text" :placeholder (tr/tr :addresses/city-placeholder)}]
   [form-field (tr/tr :addresses/postal-code) :address/postal-code address errors
    {:type "text" :placeholder (tr/tr :addresses/postal-code-placeholder)}]
   [form-field (tr/tr :addresses/country) :address/country address errors
    {:type "text" :placeholder (tr/tr :addresses/country-placeholder)}]
   [form-field (tr/tr :addresses/contact-person) :address/contact-person address errors
    {:type "text" :placeholder (tr/tr :addresses/contact-person-placeholder)}]
   [form-field (tr/tr :addresses/contact-phone) :address/contact-phone address errors
    {:type "text" :placeholder (tr/tr :addresses/contact-phone-placeholder)}]
   [form-field (tr/tr :addresses/contact-email) :address/contact-email address errors
    {:type "text" :placeholder (tr/tr :addresses/contact-email-placeholder)}]
   [elevators-field address errors]])

(defn- handle-save-click
  "Handle save button click with validation"
  [address loading? errors on-save]
  (let [validation-errors (validate-address @address)]
    (if (empty? validation-errors)
      (do (reset! loading? true)
          (reset! errors {})
          (on-save @address (fn [] (reset! loading? false))))
      (reset! errors validation-errors))))

(defn address-modal
  "Modal for creating/editing addresses using new UI components"
  [address-data is-new? on-save on-cancel]
  (let [loading? (r/atom false)
        errors (r/atom {})
        address (r/atom address-data)]
    (fn [address-data is-new? on-save on-cancel]
      (reset! address address-data)
      [modal/modal {:on-close on-cancel :close-on-backdrop? true}
       ^{:key "header"} [modal/modal-header
        {:title (if is-new? (tr/tr :addresses/modal-add-title) (tr/tr :addresses/modal-edit-title))
         :subtitle (if is-new? 
                     (tr/tr :addresses/modal-add-subtitle)
                     (tr/tr :addresses/modal-edit-subtitle))}]
       ^{:key "form"} [form-fields address @errors]
       ^{:key "footer"} [modal/modal-footer
        ^{:key "cancel"} [enhanced-button/enhanced-button
         {:variant :secondary
          :on-click on-cancel
          :text (tr/tr :addresses/cancel)}]
        ^{:key "save"} [enhanced-button/enhanced-button
         {:variant :primary
          :loading? @loading?
          :on-click #(handle-save-click address loading? errors on-save)
          :text (if @loading? (tr/tr :addresses/saving) (tr/tr :addresses/save-address))}]]])))

(defn- address-name-render
  "Custom render function for address name column with full address"
  [name row]
  [:div 
   [:div {:style {:font-weight "600" :color "#111827" :font-size "0.875rem"}}
    name]
   [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem" :line-height "1.4"}}
    (str (:address/address-line1 row)
         (when (:address/address-line2 row) (str ", " (:address/address-line2 row)))
         ", " (:address/city row) " " (:address/postal-code row))]
   
])

(defn- elevators-render
  "Custom render function for elevators column"
  [elevators row]
  [:div
   (if (seq elevators)
     (for [elevator elevators]
       ^{:key (str "table-elevator-" elevator)}
       [:span {:style {:display "inline-block" :margin-right "0.5rem" :margin-bottom "0.25rem"
                       :padding "0.25rem 0.5rem" :background "#e0f2fe" :color "#0891b2"
                       :border-radius "12px" :font-size "0.75rem" :font-weight "500"}}
        (str "🏢 " elevator)])
     [:span {:style {:color "#9ca3af" :font-style "italic" :font-size "0.75rem"}}
      (tr/tr :addresses/no-elevators)])])

(defn- contact-render
  "Custom render function for contact info"
  [contact-person row]
  [:div
   (when contact-person
     [:div {:style {:color "#374151" :font-size "0.875rem" :font-weight "500"}}
      contact-person])
   (when (:address/contact-phone row)
     [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
      (:address/contact-phone row)])
   (when (:address/contact-email row)
     [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
      (:address/contact-email row)])])

(defn addresses-table
  "Addresses table using server-side data-table component with search, sorting, and pagination"
  [addresses loading? on-edit on-delete query-fn]
  [data-table/server-side-data-table
   {:headers [{:key :address/name :label (tr/tr :addresses/table-header-address) :render address-name-render :sortable? true}
              {:key :address/elevators :label (tr/tr :addresses/table-header-elevators) :render elevators-render :sortable? false}
              {:key :address/contact-person :label (tr/tr :addresses/table-header-contact) :render contact-render :sortable? true}
              {:key :address/city :label (tr/tr :addresses/table-header-city) :sortable? true}
              {:key :address/country :label (tr/tr :addresses/table-header-country) :sortable? true}]
    :data-source @addresses
    :loading? @loading?
    :empty-message (tr/tr :addresses/no-addresses-found)
    :id-key :address/id
    :table-id :addresses-table
    :show-search? true
    :show-pagination? true
    :query-fn query-fn
    :on-data-change (fn [result] (reset! addresses result))
    :actions [{:key :edit :label (tr/tr :addresses/action-edit) :variant :primary :on-click on-edit}
              {:key :delete :label (tr/tr :addresses/action-delete) :variant :danger 
               :on-click (fn [row] 
                          (when (js/confirm (tr/tr :addresses/confirm-delete))
                            (on-delete (:address/id row))))}]}])

(defn- addresses-page-header
  "Page header with title and add button using new UI component"
  [modal-address modal-is-new?]
  [page-header/page-header
   {:title (tr/tr :addresses/page-title)
    :description (tr/tr :addresses/page-description)
    :action-button [enhanced-button/enhanced-button
                    {:variant :success
                     :on-click (fn [] 
                                (reset! modal-address {:address/country "Hungary"
                                                      :address/elevators []})
                                (reset! modal-is-new? true))
                     :text (tr/tr :addresses/add-new-address)}]}])

(defn- addresses-content
  "Main content area with server-side data table"
  [addresses loading? modal-address modal-is-new? delete-address query-fn]
  [addresses-table 
   addresses 
   loading?
   (fn [address]
     (reset! modal-address address)
     (reset! modal-is-new? false))
   delete-address
   query-fn])

(defn- modal-when-open
  "Render modal when address is selected"
  [modal-address modal-is-new? save-address]
  (when @modal-address
    [address-modal @modal-address @modal-is-new? save-address
     (fn [] (reset! modal-address nil))]))

(defn view []
  (let [workspace-id (get-workspace-id)
        addresses (r/atom [])
        loading? (r/atom false)
        modal-address (r/atom nil)
        modal-is-new? (r/atom false)
        
        load-addresses (fn [params]
                        (load-addresses-query workspace-id loading? addresses (or params {})))
        
        save-address (fn [address callback]
                       (save-address-query address workspace-id modal-is-new? 
                                          callback modal-address (fn [] (load-addresses {}))))
        
        delete-address (fn [address-id]
                         (delete-address-query address-id workspace-id (fn [] (load-addresses {}))))]
    
    (fn []
      ;; Load addresses on component mount (authentication handled by backend)  
      [:div {:style {:min-height "100vh" :background "#f9fafb"}}
       [:div {:style {:max-width "1200px" :margin "0 auto" :padding "2rem"}}
        [addresses-page-header modal-address modal-is-new?]
        [addresses-content addresses loading? modal-address modal-is-new? delete-address load-addresses]
        [modal-when-open modal-address modal-is-new? save-address]]])))