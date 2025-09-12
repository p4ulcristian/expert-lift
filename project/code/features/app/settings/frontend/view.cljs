(ns features.app.settings.frontend.view
  "Settings view for workspace configuration"
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [router.frontend.zero :as router]
            [zero.frontend.re-frame :as rf]
            [zero.frontend.react :as zero-react]
            [ui.form-field :as form-field]
            [ui.enhanced-button :as enhanced-button]
            [ui.page-header :as page-header]))

(defn- get-workspace-id
  "Get workspace ID from router parameters"
  []
  (let [router-state @router/state
        workspace-id (get-in router-state [:parameters :path :workspace-id])]
    workspace-id))

(defn- load-settings
  "Load workspace settings"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:workspace-settings/get {}}
    :parquery/context {:workspace-id workspace-id}
    :callback callback}))

(defn- save-settings
  "Save workspace settings"
  [workspace-id settings callback]
  (parquery/send-queries
   {:queries {:workspace-settings/update {:settings settings}}
    :parquery/context {:workspace-id workspace-id}
    :callback callback}))

(defn settings-form
  "Settings form component"
  [workspace-id]
  (let [settings (r/atom nil)
        loading? (r/atom false)]
    
    ;; Load settings on mount
    (zero-react/use-effect
     {:mount (fn []
               (reset! loading? true)
               (load-settings workspace-id
                              (fn [response]
                                (reset! settings (:workspace-settings/get response))
                                (reset! loading? false))))
      :params #js [workspace-id]})
    
    [:div.settings-form {:style {:padding "2rem"}}
     (if @loading?
       [:div "Loading settings..."]
       (when @settings
         [:div
          [:div {:style {:margin-bottom "3rem"}}
           [:h3 {:style {:font-size "1.5rem" :font-weight "600" :margin-bottom "1rem" :color "#1f2937"}}
            "Company Settings"]
           [:p {:style {:color "#6b7280" :margin-bottom "2rem"}}
            "Configure your company logo and workspace details"]]
          
          [:div {:style {:max-width "600px"}}
           ;; Company Logo Section
           [:div {:style {:margin-bottom "2.5rem" :padding "1.5rem" :border "1px solid #e5e7eb" :border-radius "12px" :background "#f9fafb"}}
            [:h4 {:style {:font-size "1.125rem" :font-weight "600" :margin-bottom "1rem" :color "#374151"}}
             "Company Logo"]
            [:p {:style {:color "#6b7280" :margin-bottom "1rem" :font-size "0.875rem"}}
             "Upload your company logo to personalize your workspace"]
            [:div {:style {:border "2px dashed #d1d5db" :border-radius "8px" :padding "2rem" :text-align "center" :background "#ffffff"}}
             [:div {:style {:color "#9ca3af" :font-size "0.875rem"}}
              "Logo upload coming soon..."]]]
           
           ;; Workspace Name Section
           [:div {:style {:margin-bottom "2.5rem"}}
            [:label {:style {:display "block" :font-weight "600" :margin-bottom "0.75rem" :color "#374151" :font-size "1rem"}}
             "Workspace Name"]
            [:input {:type "text"
                     :value (get-in @settings [:settings/general :workspace/name] "")
                     :placeholder "Enter your company or workspace name"
                     :style {:width "100%" 
                             :padding "0.875rem 1rem" 
                             :border "1px solid #d1d5db" 
                             :border-radius "8px"
                             :font-size "1rem"
                             :line-height "1.5"
                             :transition "border-color 0.2s, box-shadow 0.2s"
                             :outline "none"}
                     :on-change #(swap! settings assoc-in [:settings/general :workspace/name] (.. % -target -value))
                     :on-focus #(set! (.. % -target -style -border-color) "#3b82f6")
                     :on-blur #(set! (.. % -target -style -border-color) "#d1d5db")}]]
           
           [:div.form-actions {:style {:margin-top "2rem"}}
            [enhanced-button/enhanced-button
             {:type "primary"
              :on-click (fn []
                        (save-settings workspace-id @settings
                                     (fn [response]
                                       (if (:success response)
                                         (println "Settings saved successfully!")
                                         (println "Error saving settings")))))}
             "Save Settings"]]]]))]))

(defn settings-page
  "Main settings page component"
  []
  [:div {:style {:padding "2rem"}}
   [:h1 "Settings"]
   [:p "Configure your workspace settings"]
   
   [:div {:style {:max-width "600px" :margin-top "2rem"}}
    [:h3 "Company Logo"]
    [:div {:style {:border "2px dashed #ccc" :padding "2rem" :text-align "center" :margin-bottom "2rem"}}
     "Logo upload coming soon..."]
    
    [:h3 "Workspace Name"]
    [:input {:type "text" 
             :placeholder "Enter workspace name"
             :style {:width "100%" :padding "0.75rem" :border "1px solid #ccc" :border-radius "4px"}}]
    
    [:button {:style {:margin-top "1rem" :padding "0.75rem 2rem" :background "#007bff" :color "white" :border "none" :border-radius "4px"}}
     "Save Settings"]]])