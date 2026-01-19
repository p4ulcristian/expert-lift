(ns features.app.addresses.frontend.view
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame]
            [zero.frontend.react :as zero-react]
            [ui.modal :as modal]
            [ui.form-field :as form-field]
            [ui.data-table.core :as data-table]
            [ui.data-table.search :as data-table-search]
            [ui.enhanced-button :as enhanced-button]
            [ui.subheader :as subheader]
            [ui.content-section :as content-section]
            [translations.core :as tr]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    workspace-id))

(defn- load-addresses-query
  "Execute ParQuery to load addresses with pagination"
  [workspace-id params addresses-atom pagination-atom loading-atom]
  (reset! loading-atom true)
  (parquery/send-queries
   {:queries {:workspace-addresses/get-paginated params}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
               (reset! loading-atom false)
               (let [result (:workspace-addresses/get-paginated response)
                     items (:addresses result [])
                     pag (:pagination result)]
                 (reset! addresses-atom items)
                 (when pag
                   (reset! pagination-atom pag))))}))

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
    (parquery/send-queries
     {:queries {query-type address-data}
      :parquery/context context
      :callback (fn [response]
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
  (let [has-error? (contains? errors field-key)
        testid (str "address-" (name field-key) "-input")]
    [:div {:style {:margin-bottom "1.5rem"}}
     [field-label label field-key has-error?]
     [field-input field-key address has-error? (assoc attrs :data-testid testid)]
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
          :data-testid "address-cancel-button"
          :on-click on-cancel
          :text (tr/tr :addresses/cancel)}]
        ^{:key "save"} [enhanced-button/enhanced-button
         {:variant :primary
          :data-testid "address-submit-button"
          :loading? @loading?
          :on-click #(handle-save-click address loading? errors on-save)
          :text (if @loading? (tr/tr :addresses/saving) (tr/tr :addresses/save-address))}]]])))

;; =============================================================================
;; Column Renderers for react-data-table-component
;; =============================================================================

(defn- address-name-cell
  "Custom cell for address name column with full address"
  [row]
  [:div
   [:div {:style {:font-weight "600" :color "#111827" :font-size "0.875rem"}}
    (:address/name row)]
   [:div {:style {:color "#6b7280" :font-size "0.75rem" :margin-top "0.25rem" :line-height "1.4"}}
    (str (:address/address-line1 row)
         (when (:address/address-line2 row) (str ", " (:address/address-line2 row)))
         ", " (:address/city row) " " (:address/postal-code row))]])

(defn- elevators-cell
  "Custom cell for elevators column"
  [row]
  (let [elevators (:address/elevators row)]
    [:div {:style {:display "flex" :flex-wrap "wrap" :gap "0.25rem" :align-items "center"}}
     (if (seq elevators)
       (for [elevator elevators]
         ^{:key (str "table-elevator-" elevator)}
         [:span {:style {:padding "0.25rem 0.5rem" :background "#e0f2fe" :color "#0891b2"
                         :border-radius "12px" :font-size "0.75rem" :font-weight "500"}}
          elevator])
       [:span {:style {:color "#9ca3af" :font-style "italic" :font-size "0.75rem"}}
        (tr/tr :addresses/no-elevators)])]))

(defn- contact-cell
  "Custom cell for contact info"
  [row]
  (let [contact-person (:address/contact-person row)]
    [:div
     (when contact-person
       [:div {:style {:color "#374151" :font-size "0.875rem" :font-weight "500"}}
        contact-person])
     (when (:address/contact-phone row)
       [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
        (:address/contact-phone row)])
     (when (:address/contact-email row)
       [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
        (:address/contact-email row)])]))

(defn- get-columns
  "Get column configuration for addresses table (react-data-table format)"
  []
  [{:name      (tr/tr :addresses/table-header-address)
    :selector  :address/name
    :sortField :address/name
    :sortable  true
    :cell      address-name-cell
    :width     "280px"}
   {:name      (tr/tr :addresses/table-header-elevators)
    :selector  :address/elevators
    :sortable  false
    :cell      elevators-cell
    :width     "250px"}
   {:name      (tr/tr :addresses/table-header-contact)
    :selector  :address/contact-person
    :sortField :address/contact-person
    :sortable  true
    :cell      contact-cell
    :width     "200px"}])

(defn- addresses-subheader
  "Subheader with title and add button"
  [modal-address modal-is-new?]
  [subheader/subheader
   {:title (tr/tr :addresses/page-title)
    :description (tr/tr :addresses/page-description)
    :action-button [enhanced-button/enhanced-button
                    {:variant :success
                     :data-testid "add-address-button"
                     :on-click (fn []
                                (reset! modal-address {:address/country "Hungary"
                                                      :address/elevators []})
                                (reset! modal-is-new? true))
                     :text (tr/tr :addresses/add-new-address)}]}])

(defn- modal-when-open
  "Render modal when address is selected"
  [modal-address modal-is-new? save-address]
  (when @modal-address
    [address-modal @modal-address @modal-is-new? save-address
     (fn [] (reset! modal-address nil))]))

(defn view []
  (let [workspace-id (get-workspace-id)
        ;; Local state
        addresses (r/atom [])
        pagination (r/atom {:total-count 0 :page 0 :page-size 10})
        loading? (r/atom false)
        search-term (r/atom "")
        sort-field (r/atom :address/name)
        sort-direction (r/atom "asc")
        modal-address (r/atom nil)
        modal-is-new? (r/atom false)

        ;; Load function
        load-addresses (fn []
                         (load-addresses-query
                          workspace-id
                          {:search @search-term
                           :sort-by @sort-field
                           :sort-direction @sort-direction
                           :page (:page @pagination)
                           :page-size (:page-size @pagination)}
                          addresses
                          pagination
                          loading?))

        save-address (fn [address callback]
                       (save-address-query address workspace-id modal-is-new?
                                          callback modal-address load-addresses))

        delete-address (fn [address-id]
                         (delete-address-query address-id workspace-id load-addresses))

        on-edit (fn [row]
                  (reset! modal-address row)
                  (reset! modal-is-new? false))

        on-delete (fn [row]
                    (when (js/confirm (tr/tr :addresses/confirm-delete))
                      (delete-address (:address/id row))))]

    (fn []
      ;; Load initial data
      (when (and (empty? @addresses) (not @loading?))
        (load-addresses))

      [:<>
       [addresses-subheader modal-address modal-is-new?]
       [content-section/content-section
        ;; Search bar
        [:div {:style {:margin-bottom "1rem"}}
        [data-table-search/view
         {:search-term @search-term
          :placeholder (tr/tr :addresses/search-placeholder)
          :on-search-change (fn [value]
                              (reset! search-term value))
          :on-search (fn [value]
                       (reset! search-term value)
                       (swap! pagination assoc :page 0)
                       (load-addresses))}]]

       ;; Addresses table
       [data-table/view
        {:columns (get-columns)
         :data @addresses
         :loading? @loading?
         :pagination @pagination
         :entity {:name "address" :name-plural "addresses"}
         :data-testid "addresses-table"
         :on-edit on-edit
         :on-delete on-delete
         ;; Pagination handler
         :on-page-change (fn [page _total-rows]
                           (swap! pagination assoc :page (dec page))
                           (load-addresses))
         :on-page-size-change (fn [new-size]
                                (swap! pagination assoc :page-size new-size :page 0)
                                (load-addresses))
         ;; Sort handler
         :on-sort (fn [field direction _sorted-rows]
                    (reset! sort-field field)
                    (reset! sort-direction direction)
                    (load-addresses))}]

       [modal-when-open modal-address modal-is-new? save-address]]])))
