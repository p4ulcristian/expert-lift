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
    
    [:div.settings-form
     (if @loading?
       [:div "Loading settings..."]
       (when @settings
         [:div
          [:h3 "General Settings"]
          [:div {:style {:margin-bottom "1.5rem"}}
           [:label {:style {:display "block" :font-weight "600" :margin-bottom "0.5rem"}}
            "Workspace Name"]
           [:input {:type "text"
                    :value (get-in @settings [:settings/general :workspace/name] "")
                    :style {:width "100%" :padding "0.75rem" :border "1px solid #d1d5db" :border-radius "8px"}
                    :on-change #(swap! settings assoc-in [:settings/general :workspace/name] (.. % -target -value))}]]
          
          [:h3 "Notifications"]
          [form-field/checkbox-field
           {:label "Email Notifications"
            :checked (get-in @settings [:settings/notifications :email-notifications] false)
            :on-change #(swap! settings assoc-in [:settings/notifications :email-notifications] (.. % -target -checked))}]
          
          [form-field/checkbox-field
           {:label "Push Notifications"
            :checked (get-in @settings [:settings/notifications :push-notifications] false)
            :on-change #(swap! settings assoc-in [:settings/notifications :push-notifications] (.. % -target -checked))}]
          
          [:h3 "Security Settings"]
          [:p {:style {:margin-bottom "2rem" :color "#666"}}
           "Configure security preferences for your workspace"]
          
          [:div.form-actions {:style {:margin-top "2rem"}}
           [enhanced-button/enhanced-button
            {:type "primary"
             :on-click (fn []
                         (save-settings workspace-id @settings
                                        (fn [response]
                                          (if (:success response)
                                            (println "Settings saved successfully!")
                                            (println "Error saving settings")))))}
            "Save Settings"]]]))]))

(defn settings-page
  "Main settings page component"
  []
  (let [workspace-id (get-workspace-id)]
    [:div.settings-page
     [page-header/page-header
      {:title "Settings"
       :subtitle "Configure your workspace settings"}]
     
     [:div.page-content
      (if workspace-id
        [settings-form workspace-id]
        [:div "Loading workspace..."])]]))