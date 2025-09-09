(ns features.flex.service-areas.frontend.blocks.business-info
  (:require
    ["react" :as react]
    [re-frame.core :as r]
    [ui.button :as button]
    [features.flex.business-info.frontend.view :as business-info]
    [features.flex.service-areas.frontend.blocks.subs]
    [features.flex.service-areas.frontend.blocks.side-effects]))

(defn loading-spinner []
  [:div {:style {:text-align "center" :padding "20px"}}
   [:div {:style {:width "24px"
                  :height "24px"
                  :border "2px solid #f59e0b"
                  :border-top "2px solid transparent"
                  :border-radius "50%"
                  :animation "spin 1s linear infinite"
                  :margin "0 auto 12px auto"}}]
   [:p {:style {:color "#92400e" :margin "0" :font-size "0.9rem"}}
    "Loading your business information..."]
   [:style "@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }"]])

(defn header []
  [:div {:style {:display "flex"
                 :align-items "center"
                 :margin-bottom "16px"}}
   [:div {:style {:width "24px"
                  :height "24px"
                  :background "#f59e0b"
                  :color "white"
                  :border-radius "50%"
                  :display "flex"
                  :align-items "center"
                  :justify-content "center"
                  :margin-right "12px"}}
    [:i {:class "fa-solid fa-exclamation"
         :style {:font-size "12px"}}]]
   [:h3 {:style {:margin "0"
                 :font-size "1.1rem"
                 :color "#92400e"
                 :font-weight "600"}}
    "Complete Your Business Information"]])

(defn form-fields [temp-form-data set-temp-form-data!]
  [business-info/business-info-edit-form {:data temp-form-data
                                          :on-change set-temp-form-data!}])

(defn save-button [saving? temp-form-data set-saving! set-temp-form-data!]
  [:div {:style {:display "flex" :justify-content "center" :margin-top "20px"}}
   [button/view {:on-click #(let [workspace-id @(r/subscribe [:workspace/get-id])]
                              (business-info/handle-save-business-info 
                                workspace-id 
                                temp-form-data 
                                set-saving! 
                                (fn [data] ; set-display-data! - not needed in modal context
                                  (r/dispatch [:db/assoc-in [:setup :business-info] data]))
                                set-temp-form-data! ; set-form-data!
                                (fn [_] nil))) ; set-edit-mode! - not needed in modal context
                 :style {:background "#f59e0b"
                         :color "white"
                         :border "none"
                         :padding "10px 20px"
                         :border-radius "6px"
                         :font-weight "600"
                         :cursor "pointer"}
                 :disabled saving?}
    (if saving? "Saving..." "Save & Continue")]])

(defn form-section []
  (let [business-info @(r/subscribe [:db/get-in [:setup :business-info]])
        ;; Merge existing data with empty form to ensure all fields exist
        initial-data (if (and business-info 
                             (:business-name business-info)
                             (:owner-name business-info))
                      business-info
                      (merge (business-info/create-empty-form-data) 
                             (or business-info {})))
        [temp-form-data set-temp-form-data!] (react/useState initial-data)
        [saving? set-saving!] (react/useState false)
        [loading? set-loading!] (react/useState false)
        [initialized? set-initialized!] (react/useState false)]
    
    ;; Fetch business info on mount if not already loaded or incomplete
    (react/useEffect
      (fn []
        (when (and (not initialized?) 
                   (or (not business-info)
                       (not (:business-name business-info))
                       (not (:owner-name business-info))))
          (set-initialized! true)
          (set-loading! true)
          (js/console.log "Fetching business info because it's missing or incomplete")
          (r/dispatch [:service-areas/fetch-business-info]))
        (fn []))
      #js[])
    
    ;; Update form data when business-info loads from fetch
    (react/useEffect
      (fn []
        (when (and business-info 
                   (:business-name business-info)
                   (:owner-name business-info))
          (set-temp-form-data! business-info)
          (set-loading! false))
        (fn []))
      #js[business-info])
    
    [:div {:style {:margin-bottom "30px"
                   :padding "20px"
                   :background "rgba(255, 243, 224, 0.3)"
                   :border "1px solid rgba(251, 146, 60, 0.2)"
                   :border-radius "12px"}}
     [header]
     [:p {:style {:margin "0 0 20px 0"
                  :color "#78350f"
                  :font-size "0.9rem"
                  :line-height "1.5"}}
      "Please provide your business details before scheduling an appointment with Ironrainbow."]
     
     (if loading?
       [loading-spinner]
       [:div
        [form-fields temp-form-data set-temp-form-data!]
        [save-button saving? temp-form-data set-saving! set-temp-form-data!]])]))