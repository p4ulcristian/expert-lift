(ns features.app.settings.frontend.view
  "Settings view for workspace configuration"
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame :as rf]
            [zero.frontend.react :as zero-react]
            [ui.form-field :as form-field]
            [ui.enhanced-button :as enhanced-button]
            [ui.page-header :as page-header]
            [translations.core :as tr]))

;; Re-frame Events
(rf/reg-event-db
 :settings/load-start
 (fn [db _]
   (assoc-in db [:settings :loading?] true)))

(rf/reg-event-db
 :settings/load-success
 (fn [db [_ settings-data]]
   (-> db
       (assoc-in [:settings :data] settings-data)
       (assoc-in [:settings :loading?] false))))

(rf/reg-event-db
 :settings/load-error
 (fn [db [_ error]]
   (-> db
       (assoc-in [:settings :error] error)
       (assoc-in [:settings :loading?] false))))

(rf/reg-event-db
 :settings/update-field
 (fn [db [_ path value]]
   (assoc-in db (concat [:settings :data] path) value)))

(rf/reg-event-db
 :settings/set-uploading
 (fn [db [_ uploading?]]
   (assoc-in db [:settings :uploading?] uploading?)))

(rf/reg-event-db
 :settings/set-selected-file
 (fn [db [_ file]]
   (assoc-in db [:settings :selected-file] file)))

(rf/reg-event-db
 :settings/set-preview-url
 (fn [db [_ url]]
   (assoc-in db [:settings :preview-url] url)))

;; Re-frame Subscriptions
(rf/reg-sub
 :settings/data
 (fn [db _]
   (get-in db [:settings :data])))

(rf/reg-sub
 :settings/loading?
 (fn [db _]
   (get-in db [:settings :loading?] false)))

(rf/reg-sub
 :settings/uploading?
 (fn [db _]
   (get-in db [:settings :uploading?] false)))

(rf/reg-sub
 :settings/selected-file
 (fn [db _]
   (get-in db [:settings :selected-file])))

(rf/reg-sub
 :settings/preview-url
 (fn [db _]
   (get-in db [:settings :preview-url])))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    workspace-id))

(defn- load-settings
  "Load workspace settings"
  [workspace-id]
  (rf/dispatch [:settings/load-start])
  (parquery/send-queries
   {:queries {:workspace-settings/get {}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (if-let [settings-data (:workspace-settings/get response)]
                  (rf/dispatch [:settings/load-success settings-data])
                  (rf/dispatch [:settings/load-error (tr/tr :settings/error-no-data)])))}))

(defn- save-settings
  "Save workspace settings"
  [workspace-id settings callback]
  (parquery/send-queries
   {:queries {:workspace-settings/update {:settings settings}}
    :parquery/context {:workspace-id workspace-id}
    :callback callback}))

(defn- submit-settings
  "Submit settings via multipart form (both workspace name and optional file)"
  [workspace-id workspace-name file callback]
  (let [form-data (js/FormData.)
        upload-url (str "/app/" workspace-id "/settings/upload-logo")]
    
    ;; Add workspace name to form data
    (.append form-data "workspaceName" workspace-name)
    
    ;; Add file to form data if provided
    (when file
      (.append form-data "file" file))
    
    ;; Send multipart request
    (-> (js/fetch upload-url
                  #js {:method "POST"
                       :body form-data})
        (.then (fn [response]
                 (.json response)))
        (.then (fn [data]
                 (callback (js->clj data :keywordize-keys true))))
        (.catch (fn [error]
                  (println "Upload error:" error)
                  (callback {:success false :error (str error)}))))))

(defn- settings-page-header
  "Page header for settings page using consistent UI component"
  []
  [page-header/page-header
   {:title (tr/tr :settings/title)
    :description (tr/tr :settings/description)}])

(defn- handle-file-select
  "Handle file selection for logo upload"
  [e]
  (let [file (-> e .-target .-files (aget 0))]
    (rf/dispatch [:settings/set-selected-file file])
    (when file
      (println "Selected file:" (.-name file))
      ;; Create preview URL for selected image
      (let [reader (js/FileReader.)]
        (set! (.-onload reader)
              (fn [e]
                (rf/dispatch [:settings/set-preview-url (-> e .-target .-result)])))
        (.readAsDataURL reader file)))))

(defn- upload-status-display
  "Display upload status and file selection"
  []
  (let [uploading? @(rf/subscribe [:settings/uploading?])
        selected-file @(rf/subscribe [:settings/selected-file])
        preview-url @(rf/subscribe [:settings/preview-url])
        settings @(rf/subscribe [:settings/data])
        existing-logo-url (get-in settings [:settings/logo :logo-url])]
    (cond
      uploading?
      [:div {:style {:color "#9ca3af" :font-size "0.875rem" :margin-bottom "0.5rem"}}
       (tr/tr :settings/uploading)]
      
      selected-file
      [:div
       ;; Image preview (new file selected)
       (when preview-url
         [:div {:style {:margin-bottom "1rem"}}
          [:img {:src preview-url
                 :alt (tr/tr :settings/logo-preview)
                 :style {:max-width "200px"
                         :max-height "150px"
                         :border-radius "8px"
                         :box-shadow "0 2px 4px rgba(0,0,0,0.1)"
                         :display "block"
                         :margin "0 auto"}}]])
       [:div {:style {:color "#10b981" :font-size "0.875rem" :margin-bottom "0.5rem"}}
        (str (tr/tr :settings/selected) ": " (.-name selected-file))]
       [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
        (tr/tr :settings/click-different)]]
      
      ;; Show existing logo if available
      existing-logo-url
      [:div
       [:div {:style {:margin-bottom "1rem"}}
        [:img {:src existing-logo-url
               :alt (tr/tr :settings/current-logo-alt)
               :style {:max-width "200px"
                       :max-height "150px"
                       :border-radius "8px"
                       :box-shadow "0 2px 4px rgba(0,0,0,0.1)"
                       :display "block"
                       :margin "0 auto"}}]]
       [:div {:style {:color "#6b7280" :font-size "0.875rem" :margin-bottom "0.5rem"}}
        (tr/tr :settings/current-logo)]
       [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
        (tr/tr :settings/upload-new)]]
      
      :else
      [:div
       [:div {:style {:color "#9ca3af" :font-size "0.875rem" :margin-bottom "0.5rem"}}
        (tr/tr :settings/click-upload)]
       [:div {:style {:color "#6b7280" :font-size "0.75rem"}}
        (tr/tr :settings/supported-formats)]])))

(defn- logo-upload-section
  "Company logo upload section"
  []
  (let [uploading? @(rf/subscribe [:settings/uploading?])]
    [:div {:style {:margin-bottom "2.5rem" :padding "1.5rem" :border "1px solid #e5e7eb" :border-radius "12px" :background "#f9fafb"}}
     [:h4 {:style {:font-size "1.125rem" :font-weight "600" :margin-bottom "1rem" :color "#374151"}}
      (tr/tr :settings/company-logo)]
     [:p {:style {:color "#6b7280" :margin-bottom "1rem" :font-size "0.875rem"}}
      (tr/tr :settings/logo-description)]
     [:div {:style {:border "2px dashed #d1d5db" :border-radius "8px" :padding "2rem" :text-align "center" :background "#ffffff"}}
      [:input {:type "file"
               :id "logo-upload"
               :accept "image/*"
               :style {:display "none"}
               :on-change handle-file-select}]
      [:label {:for "logo-upload"
               :style {:cursor (if uploading? "not-allowed" "pointer") 
                       :color (if uploading? "#9ca3af" "#3b82f6") 
                       :font-weight "500"}}
       [upload-status-display]]]]))

(defn- workspace-name-input
  "Workspace name input field"
  []
  (let [settings @(rf/subscribe [:settings/data])]
    [:div {:style {:margin-bottom "2.5rem"}}
     [:label {:style {:display "block" :font-weight "600" :margin-bottom "0.75rem" :color "#374151" :font-size "1rem"}}
      (tr/tr :settings/workspace-name)]
     [:input {:type "text"
              :value (get-in settings [:settings/general :workspace/name] "")
              :placeholder (tr/tr :settings/workspace-name-placeholder)
              :style {:width "100%" 
                      :padding "0.875rem 1rem" 
                      :border "1px solid #d1d5db" 
                      :border-radius "8px"
                      :font-size "1rem"
                      :line-height "1.5"
                      :transition "border-color 0.2s, box-shadow 0.2s"
                      :outline "none"}
              :on-change #(rf/dispatch [:settings/update-field [:settings/general :workspace/name] (.. % -target -value)])
              :on-focus #(set! (.. % -target -style -border-color) "#3b82f6")
              :on-blur #(set! (.. % -target -style -border-color) "#d1d5db")}]]))

(defn- handle-save-response
  "Handle save response"
  [workspace-id response]
  (rf/dispatch [:settings/set-uploading false])
  (if (:success response)
    (do
      (println "Settings saved successfully!")
      (when (:file-uploaded response)
        (println "Logo uploaded:" (:filename response)))
      (when (:workspace-name-updated response)
        (println "Workspace name updated"))
      (rf/dispatch [:settings/set-selected-file nil])
      (rf/dispatch [:settings/set-preview-url nil])
      ;; Reload settings to show updated logo
      (load-settings workspace-id))
    (println "Error saving settings:" (:error response))))

(defn- handle-save-click
  "Handle save button click"
  [workspace-id]
  (let [settings @(rf/subscribe [:settings/data])
        selected-file @(rf/subscribe [:settings/selected-file])
        workspace-name (get-in settings [:settings/general :workspace/name])]
    (rf/dispatch [:settings/set-uploading true])
    (submit-settings workspace-id workspace-name selected-file 
                     (partial handle-save-response workspace-id))))

(defn- save-button
  "Save settings button"
  [workspace-id]
  (let [loading? @(rf/subscribe [:settings/loading?])
        uploading? @(rf/subscribe [:settings/uploading?])]
    [:div.form-actions {:style {:margin-top "2rem"}}
     [enhanced-button/enhanced-button
      {:variant :primary
       :loading? (or loading? uploading?)
       :on-click #(handle-save-click workspace-id)
       :text (if (or loading? uploading?) (tr/tr :settings/saving) (tr/tr :settings/save-settings))}]]))

(defn- settings-form-content
  "Main settings form content"
  [workspace-id]
  [:div {:style {:max-width "600px"}}
   [workspace-name-input]
   [logo-upload-section]
   [save-button workspace-id]])

(defn settings-form
  "Settings form component"
  [workspace-id]
  (zero-react/use-effect
   {:mount (fn []
             (println "DEBUG: Loading settings for workspace-id:" workspace-id)
             (load-settings workspace-id))
    :params #js [workspace-id]})
  
  (let [settings @(rf/subscribe [:settings/data])
        loading? @(rf/subscribe [:settings/loading?])]
    [:div.settings-form
     [settings-page-header]
     (do 
       (println "DEBUG: settings value:" settings)
       (println "DEBUG: loading?" loading?)
       (println "DEBUG: settings exists?" (boolean settings))
       (cond
         loading?
         [:div (tr/tr :settings/loading-settings)]
         
         settings
         [:div [settings-form-content workspace-id]]
         
         :else
         [:div (tr/tr :settings/no-settings)]))]))

(defn settings-page
  "Main settings page component"
  []
  (let [workspace-id (get-workspace-id)]
    [:div {:style {:max-width "1200px" :margin "0 auto" :padding "2rem"}}
     [settings-form workspace-id]]))