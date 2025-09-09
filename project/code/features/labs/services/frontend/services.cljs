(ns features.labs.services.frontend.services
  (:require
   [app.frontend.request :as request]
   [clojure.string :as str]
   [features.common.storage.frontend.picker :as picker]
   [features.labs.shared.frontend.components.header :as header]
   [reagent.core :as r]
   [ui.button :as button]
   [ui.card :as card]
   [ui.text-field :as text-field]
   [zero.frontend.re-frame :refer [dispatch]]
   [zero.frontend.react :as zero-react]))

(def services-data (r/atom []))
(def create-form-state (r/atom {:name ""
                               :description ""
                               :picture-url ""
                               :loading false
                               :error nil
                               :success nil}))
(def editing-service-id (r/atom nil))

;; ===== Form Components =====

(defn text-field-component [{:keys [label value on-change placeholder]}]
  [:div {:style {:margin-bottom "1rem"}}
   [:label {:style {:display "block"
                   :margin-bottom "0.5rem"
                   :font-weight "500"
                   :color "var(--text-clr)"}}
    label]
   [text-field/view {:value value
                    :placeholder placeholder
                    :on-change on-change
                    :style {:width "100%"}}]])

(defn textarea-field [{:keys [label value on-change]}]
  [:div {:style {:margin-bottom "1rem"}}
   [:label {:style {:display "block"
                   :margin-bottom "0.5rem"
                   :font-weight "500"
                   :color "var(--text-clr)"}}
    label]
   [:textarea {:value value
               :placeholder "Enter service description"
               :on-change #(on-change (.. ^js % -target -value))
               :style {:width "100%"
                       :min-height "80px"
                       :padding "8px"
                       :border "1px solid #e5e7eb"
                       :border-radius "4px"
                       :font-family "inherit"
                       :font-size "0.875rem"
                       :resize "vertical"}}]])

(defn picture-field [{:keys [picture on-change]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "12px"
                 :margin-bottom "1rem"}}
   [:label {:style {:display "block"
                   :margin-bottom "8px"
                   :font-weight "500"
                   :color "var(--text-clr)"}}
    "Picture"]
   (if picture
     [:div {:style {:width "200px"
                   :height "200px"
                   :border-radius "8px"
                   :overflow "hidden"
                   :border "1px solid #e5e7eb"}}
      [:img {:src picture
             :style {:width "100%"
                    :height "100%"
                    :object-fit "cover"}}]]
     [:div {:style {:width "200px"
                   :height "200px"
                   :border-radius "8px"
                   :border "1px solid #e5e7eb"
                   :background-color "#f9fafb"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"}}
      [:i {:class "fa-solid fa-image"
           :style {:font-size "32px"
                  :color "#ccc"}}]])
   [:div {:style {:width "200px"}}
    [picker/view
     {:value picture
      :multiple? false
      :on-select #(on-change (:url %))
      :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]]])

;; ===== Service Creation and Editing =====

(defn reset-form-state! []
  (swap! create-form-state merge {:name ""
                                 :description ""
                                 :picture-url ""
                                 :loading false
                                 :error nil
                                 :success nil})
  (reset! editing-service-id nil))

(defn populate-form-for-edit! [service]
  (swap! create-form-state merge {:name (:name service)
                                 :description (or (:description service) "")
                                 :picture-url (or (:picture_url service) "")
                                 :loading false
                                 :error nil
                                 :success nil})
  (reset! editing-service-id (:id service)))

(defn handle-service-response [response]
  (let [editing-id @editing-service-id
        result (if editing-id
                (:workspace-services/update-service response)
                (:workspace-services/create-service response))]
    (swap! create-form-state assoc :loading false)
    (if (:success result)
      (do
        (swap! create-form-state assoc :success (:message result))
        ;; Refresh services list
        (request/pathom-with-workspace-id
         {:callback (fn [response]
                     (let [services (:workspace-services/get-services response)]
                       (when (sequential? services)
                         (reset! services-data services))))
          :query [:workspace-services/get-services]})
        ;; Close modal and clear success message after 2 seconds
        (dispatch [:modals/close :add-service-modal])
        (js/setTimeout #(swap! create-form-state assoc :success nil) 2000))
      (swap! create-form-state assoc :error (or (:error result) "Failed to save service")))))

(defn save-service! []
  (swap! create-form-state assoc :loading true :error nil :success nil)
  (let [form-data @create-form-state
        editing-id @editing-service-id]
    (when (not (str/blank? (:name form-data)))
      (if editing-id
        ;; Update existing service
        (request/pathom-with-workspace-id
         {:callback handle-service-response
          :query `[(workspace-services/update-service ~{:id editing-id
                                                       :name (:name form-data)
                                                       :description (:description form-data)
                                                       :picture-url (:picture-url form-data)})]})
        ;; Create new service
        (request/pathom-with-workspace-id
         {:callback handle-service-response
          :query `[(workspace-services/create-service ~{:name (:name form-data)
                                                       :description (:description form-data)
                                                       :picture-url (:picture-url form-data)})]})))))

(defn create-service-modal []
  (let [state @create-form-state
        editing-id @editing-service-id 
        submit-text (if editing-id "Save Changes" "Create Service")]
    [:div {:style {:padding "20px"
                   :min-width "500px"
                   :max-height "80vh"
                   :display "flex"
                   :flex-direction "column"}}
     [:div {:style {:overflow-y "auto"
                    :flex-grow 1
                    :padding-right "10px"}}
      
      ;; Picture and basic info section
      [:div {:style {:display "flex"
                     :gap "24px"
                     :margin-bottom "24px"}}
       [picture-field {:picture (:picture-url state)
                      :on-change #(swap! create-form-state assoc :picture-url %)}]
       
       [:div {:style {:flex "1"
                     :display "flex"
                     :flex-direction "column"
                     :gap "1rem"}}
        [text-field-component {:label "Service Name *"
                              :value (:name state)
                              :placeholder "Enter service name"
                              :on-change #(swap! create-form-state assoc :name %)}]
        
        [textarea-field {:label "Description"
                        :value (:description state)
                        :on-change #(swap! create-form-state assoc :description %)}]]]]
     
     ;; Action buttons
     [:div {:style {:display "flex"
                   :justify-content "flex-end"
                   :gap "12px"
                   :flex-shrink 0
                   :margin-top "20px"}}
      [button/view {:mode :clear
                   :type :secondary
                   :on-click #(do
                               (reset-form-state!)
                               (dispatch [:modals/close :add-service-modal]))}
       "Cancel"]
      
      [button/view {:mode :filled
                   :type :primary
                   :disabled (or (:loading state)
                                (str/blank? (:name state)))
                   :on-click save-service!}
       (if (:loading state) "Saving..." submit-text)]]
     
     ;; Error message
     (when (:error state)
       [:div {:style {:padding "8px"
                     :margin-top "1rem"
                     :background-color "#fee2e2"
                     :border "1px solid #fca5a5"
                     :border-radius "4px"
                     :color "#dc2626"
                     :font-size "0.875rem"}}
        (:error state)])]))

(defn add-service-header-button []
  [button/view {:mode :filled
                :type :primary
                :style {:padding "8px 16px"
                        :height "auto"
                        :display "flex"
                        :align-items "center"
                        :gap "8px"
                        :font-size "14px"
                        :border-radius "6px"} 
                :on-click #(do
                             (reset-form-state!)
                             (dispatch [:modals/add {:id :add-service-modal
                                                   :label "Add New Service"
                                                   :content [create-service-modal]
                                                   :open? true}]))}
   [:i {:class "fa-solid fa-plus"
        :style {:font-size "12px"}}] 
   "Add Service"])

(defn services-header []
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :align-items "center"
                 :margin-bottom "2rem"
                 :padding-bottom "1rem"
                 :border-bottom "1px solid rgba(255, 255, 255, 0.15)"}}
   [:h1 {:style {:margin "0"
                 :font-size "2rem"
                 :font-weight "700"
                 :color "rgba(255, 255, 255, 0.95)"
                 :letter-spacing "-0.025em"
                 :text-shadow "0 1px 2px rgba(0, 0, 0, 0.1)"}}
    "Services"]
   [add-service-header-button]])

;; ===== Service Deletion =====

(defn delete-service! [service-id service-name]
  (when (js/confirm (str "Are you sure you want to delete '" service-name "'?"))
    (request/pathom-with-workspace-id
     {:callback (fn [response]
                 (let [result (:workspace-services/delete-service response)]
                   (if (:success result)
                     (do
                       ;; Show success message briefly
                       (swap! create-form-state assoc :success (:message result))
                       ;; Refresh services list
                       (request/pathom-with-workspace-id
                        {:callback (fn [response]
                                    (let [services (:workspace-services/get-services response)]
                                      (when (sequential? services)
                                        (reset! services-data services))))
                         :query [:workspace-services/get-services]})
                       ;; Clear success message after 2 seconds
                       (js/setTimeout #(swap! create-form-state assoc :success nil) 2000))
                     (do
                       ;; Show error message
                       (swap! create-form-state assoc :error (or (:error result) "Failed to delete service"))
                       ;; Clear error message after 3 seconds
                       (js/setTimeout #(swap! create-form-state assoc :error nil) 3000)))))
      :query `[(workspace-services/delete-service {:id ~service-id})]})))

(defn service-card [[id service]]
  ;; Debug: Log service data to see what we're receiving
  (js/console.log "Service card data - ID:" id "Service:" service)
  [card/view
   {:content
    [:div {:style {:width "100%"
                   :display "flex"
                   :flex-direction "column"
                   :gap "0.5rem"
                   :padding "0.75rem"
                   :background-color "#fefefe"
                   :border-radius "8px"
                   :border "1px solid #e1e5e9"
                   :position "relative"}}
     
     ;; Action buttons in top-right corner
     [:div {:style {:position "absolute"
                   :top "8px"
                   :right "8px"
                   :display "flex"
                   :gap "4px"
                   :z-index "1"}}
      
      ;; Edit button
      [:button {:style {:width "24px"
                       :height "24px"
                       :border "none"
                       :border-radius "50%"
                       :background-color "rgba(59, 130, 246, 0.1)"
                       :color "#3b82f6"
                       :cursor "pointer"
                       :display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :font-size "12px"
                       :transition "all 0.2s ease"}
               :on-mouse-enter #(do
                                 (set! (-> % .-target .-style .-background-color) "rgba(59, 130, 246, 0.2)")
                                 (set! (-> % .-target .-style .-transform) "scale(1.1)"))
               :on-mouse-leave #(do
                                 (set! (-> % .-target .-style .-background-color) "rgba(59, 130, 246, 0.1)")
                                 (set! (-> % .-target .-style .-transform) "scale(1)"))
               :on-click #(do
                           (populate-form-for-edit! (assoc service :id id))
                           (dispatch [:modals/add {:id :add-service-modal
                                                 :label "Edit Service"
                                                 :content [create-service-modal]
                                                 :open? true}]))}
       [:i {:class "fa-solid fa-pen"}]]
      
      ;; Delete button
      [:button {:style {:width "24px"
                       :height "24px"
                       :border "none"
                       :border-radius "50%"
                       :background-color "rgba(239, 68, 68, 0.1)"
                       :color "#ef4444"
                       :cursor "pointer"
                       :display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :font-size "12px"
                       :transition "all 0.2s ease"}
               :on-mouse-enter #(do
                                 (set! (-> % .-target .-style .-background-color) "rgba(239, 68, 68, 0.2)")
                                 (set! (-> % .-target .-style .-transform) "scale(1.1)"))
               :on-mouse-leave #(do
                                 (set! (-> % .-target .-style .-background-color) "rgba(239, 68, 68, 0.1)")
                                 (set! (-> % .-target .-style .-transform) "scale(1)"))
               :on-click #(delete-service! id (:name service))}
       [:i {:class "fa-solid fa-xmark"}]]]
     
     ;; Main content: Picture + Name & Description
     [:div {:style {:display "flex"
                   :align-items "flex-start"
                   :padding "0.75rem"
                   :background-color "#f9fafb"
                   :border-radius "6px"
                   :border "1px solid #e5e7eb"
                   :gap "1rem"}}
      
      ;; Picture on the left
      [:div {:style {:flex-shrink "0"}}
       (if (:picture_url service)
         [:div {:style {:width "50px"
                        :height "50px"
                        :position "relative"
                        :border "1px solid var(--border-clr)"
                        :border-radius "6px"
                        :overflow "hidden"}}
          [:img {:src (:picture_url service)
                 :style {:width "100%"
                         :height "100%"
                         :object-fit "cover"}
                 :on-load (fn [_e]
                           (js/console.log "Image loaded successfully:" (:picture_url service)))
                 :on-error (fn [e]
                            (js/console.log "Image failed to load:" (:picture_url service))
                            (let [target (.-target e)
                                  placeholder (.querySelector (.-parentNode target) ".placeholder")]
                              (set! (.-style.display target) "none")
                              (set! (.-style.display placeholder) "flex")))}]
          [:div {:class "placeholder"
                 :style {:display "none"
                        :width "100%"
                        :height "100%"
                        :background-color "var(--muted-clr)"
                        :color "white"
                        :align-items "center"
                        :justify-content "center"
                        :font-size "16px"
                        :border-radius "6px"}}
           "🔧"]]
         ;; No picture_url available
         [:div {:style {:width "50px"
                        :height "50px"
                        :border "1px solid var(--border-clr)"
                        :border-radius "6px"
                        :background-color "var(--bg-clr)"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"
                        :color "var(--muted-clr)"
                        :font-size "16px"}}
          "🔧"])]
      
      ;; Service name and description on the right
      [:div {:style {:flex "1"
                    :min-width "0"
                    :display "flex"
                    :flex-direction "column"
                    :gap "0.25rem"}}
       ;; Service name
       [:h3 {:style {:margin "0"
                    :font-size "1.1rem"
                    :font-weight "600"
                    :color "var(--text-clr)"
                    :white-space "nowrap"
                    :overflow "hidden"
                    :text-overflow "ellipsis"}}
        (:name service)]
       
       ;; Description below name
       (when (:description service)
         [:p {:style {:margin "0"
                     :color "var(--muted-clr)"
                     :font-size "0.85rem"
                     :line-height "1.4"
                     :display "-webkit-box"
                     :-webkit-line-clamp "2"
                     :-webkit-box-orient "vertical"
                     :overflow "hidden"}}
          (:description service)])]]]}])

(defn services-grid []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "1rem"
                 :padding "1rem"
                 :max-width "800px"
                 :margin "0 auto"}}
   (when (sequential? @services-data)
     (->> (for [service-entry @services-data]
            (cond
              ;; Handle [id service] vector format
              (and (vector? service-entry) (= 2 (count service-entry)))
              (let [[id service] service-entry]
                ^{:key (str id)}
                [service-card [id service]])
              
              ;; Handle map format with :id key
              (map? service-entry)
              (let [id (:id service-entry)
                    service (dissoc service-entry :id)]
                ^{:key (str id)}
                [service-card [id service]])
              
              ;; Fallback - log and skip
              :else
              (do
                (js/console.warn "Unexpected service entry format:" service-entry)
                nil)))
          (filter some?)
          (doall)))])

(defn view []
  (zero-react/use-effect
   {:mount (fn []
             (request/pathom-with-workspace-id
              {:callback (fn [response]
                          (js/console.log "Response from backend:" response)
                          (let [services (:workspace-services/get-services response)]
                            (js/console.log "Workspace services data:" services)
                            (if (sequential? services)
                              (reset! services-data services)
                              (do
                                (js/console.error "Expected sequential data, got:" (type services))
                                (reset! services-data [])))))
               :query [:workspace-services/get-services]}))})

  [:<>
   [header/view]
   [:div {:style {:min-height "100vh"
                  :background "var(--ir-primary)"
                  :position "relative"
                  :overflow "hidden"}}
    ;; Background elements like parts
    [:div {:class "dashboard-background"}]
    [:div {:style {:position "relative"
                   :z-index "1"
                   :padding "40px"}}
     [:div {:style {:max-width "1200px"
                    :margin "0 auto"}}
      ;; Success message (if any)
      (when (:success @create-form-state)
        [:div {:style {:padding "12px 16px"
                      :margin-bottom "1rem"
                      :background-color "rgba(34, 197, 94, 0.15)"
                      :border "1px solid rgba(34, 197, 94, 0.3)"
                      :border-radius "8px"
                      :color "rgba(255, 255, 255, 0.9)"
                      :font-size "0.875rem"
                      :font-weight "500"
                      :backdrop-filter "blur(10px)"}}
         (:success @create-form-state)])
      
      [services-header]
      [services-grid]]]]])