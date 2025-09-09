(ns features.flex.teams.frontend.invitation
  (:require
   [parquery.frontend.request :as parquery]
   [zero.frontend.react :as react]
   [zero.frontend.re-frame :refer [subscribe]]))

(defn keyframe-styles
  "CSS keyframes for animations"
  []
  [:style 
   "@keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }"])

(defn background-gradient
  "Dark background for invitation page matching email template"
  []
  [:div {:style {:position "fixed"
                 :z-index -1
                 :height "100vh"
                 :width "100vw"
                 :background "#333333"}}])

(defn is-invitation-invalid?
  "Checks if invitation details indicate invalid/deleted invitation"
  [details]
  (or (nil? details)
      (and (nil? (:workspace/name details))
           (nil? (:inviter/email details))
           (nil? (:invitee/email details)))))

(defn fetch-invitation-details
  "Fetches invitation details using ParQuery"
  [invitation-id set-details set-loading set-invalid]
  (when invitation-id
    (set-loading true)
    (parquery/send-queries
     {:queries {:invitation/details {:invitation-id invitation-id}}
      :parquery/context {}
      :callback (fn [response]
                  (set-loading false)
                  (try
                    (let [details (:invitation/details response)]
                      (if (is-invitation-invalid? details)
                        (do
                          (set-invalid true)
                          (set-details nil))
                        (do
                          (set-invalid false)
                          (set-details details))))
                    (catch js/Error e
                      (set-invalid true)
                      (set-details nil))))})))

(defn use-invitation-details-effect
  "Effect to fetch invitation details when invitation-id changes"
  [invitation-id set-details set-loading set-invalid]
  (react/use-effect
   {:mount (fn []
             (fetch-invitation-details invitation-id set-details set-loading set-invalid))
    :params #js [invitation-id]}))

(defn redirect-to-workspace-dashboard
  "Redirects to workspace dashboard after successful invitation acceptance"
  [workspace-id]
  (when workspace-id
    (set! (.-location js/window) (str "/flex/ws/" workspace-id))))

(defn accept-invitation-handler
  "Handles invitation acceptance with given id using ParQuery"
  [id set-accepting]
  (set-accepting true)
  (parquery/send-queries
   {:queries {:invitation/accept {:invitation-id id}}
    :parquery/context {}
    :callback (fn [response]
                (set-accepting false)
                (try
                  (let [result (:invitation/accept response)]
                    (if (:success result)
                      (let [workspace-id (:workspace/id result)]
                        (redirect-to-workspace-dashboard workspace-id))
                      (js/alert (str "Failed to accept invitation: " 
                                    (or (:message result) 
                                        (:error result) 
                                        "Unknown error")))))
                  (catch js/Error e
                    (js/alert (str "Error accepting invitation: " (.-message e))))))}))

(defn invitation-card
  "White card container with email template styling"
  [content]
  [:div {:style {:background "#ffffff"
                 :border-radius "12px"
                 :box-shadow "0 20px 40px rgba(0, 0, 0, 0.3)"
                 :max-width "600px"
                 :width "90%"
                 :overflow "hidden"
                 :animation "fadeInUp 0.6s ease-out"
                 :transform "translateY(0)"}}
   content])

(defn emails-match?
  "Check if logged-in user email matches invited email"
  [current-user details]
  (and current-user 
       details 
       (= (:user/email current-user) (:invitee/email details))))

(defn workspace-title
  "Title showing workspace name"
  [details]
  [:h2 {:style {:margin "0 0 20px 0"
                :color "#333333"
                :font-size "24px"
                :font-weight "600"
                :line-height "1.3"
                :text-align "center"}}
   "Join " [:span {:style {:color "#ffd700"}} (get details :workspace/name "Unknown")]])

(defn invitation-description
  "Description text from inviter"
  [details]
  [:p {:style {:margin "0 0 25px 0"
               :color "#666666"
               :font-size "16px"
               :line-height "1.6"
               :text-align "center"}}
   [:strong {:style {:color "#333333"}} (get details :inviter/email "Unknown")]
   " has invited you to collaborate on Iron Rainbow's most advanced coating management platform."])

(defn loading-message
  "Loading message component"
  []
  [:div {:style {:margin-bottom "30px" :color "#666666" :text-align "center"}}
   "Loading invitation details..."])

(defn login-prompt
  "Prompt to login with button"
  []
  [:div {:style {:text-align "center" :margin "35px 0"}}
   [:p {:style {:margin-bottom "20px" :font-size "16px" :color "#999999" :line-height "1.5"}}
    "Please log in to accept this invitation."]
   [:a 
    {:href (str "/login/flex?redirect=" (js/encodeURIComponent 
                                          (str (.-pathname js/window.location) (.-search js/window.location))))
     :style {:display "inline-block"
             :background "linear-gradient(135deg, #ffd700 0%, #ffed4e 100%)"
             :color "#333333"
             :text-decoration "none"
             :padding "18px 40px"
             :border-radius "50px"
             :font-weight "700"
             :font-size "16px"
             :letter-spacing "0.5px"
             :text-transform "uppercase"
             :box-shadow "0 8px 25px rgba(255, 215, 0, 0.4)"
             :transition "all 0.3s ease"}}
    "LOG IN"]])

(defn accept-invitation-prompt
  "Prompt to accept invitation"
  []
  [:div {:style {:text-align "center" :margin "35px 0"}}
   [:p {:style {:margin-bottom "30px" :font-size "16px" :color "#666666" :line-height "1.5"}}
    "Click below to accept your team invitation:"]])

(defn cannot-accept-message
  "Message when user cannot accept"
  []
  [:p {:style {:margin-bottom "30px" :font-size "16px" :color "#999999" :line-height "1.5" :text-align "center"}}
   "You cannot accept this invitation with your current account."])

(defn accept-button
  "Accept invitation button"
  [can-accept accepting id set-accepting]
  [:div {:style {:text-align "center" :margin "35px 0"}}
   [:button 
    {:on-click #(when (and can-accept (not accepting)) 
                   (accept-invitation-handler id set-accepting))
     :disabled (or (not can-accept) accepting)
     :style (merge
             {:color "#333333"
              :text-decoration "none"
              :padding "18px 40px"
              :border "none"
              :border-radius "50px"
              :font-weight "700"
              :font-size "16px"
              :letter-spacing "0.5px"
              :text-transform "uppercase"
              :transition "all 0.3s ease"
              :font-family "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"}
             (cond
               accepting
               {:background "linear-gradient(135deg, #ffa500 0%, #ff8c00 100%)"
                :box-shadow "0 4px 12px rgba(255, 165, 0, 0.3)"
                :cursor "wait"}
               
               can-accept
               {:background "linear-gradient(135deg, #ffd700 0%, #ffed4e 100%)"
                :box-shadow "0 8px 25px rgba(255, 215, 0, 0.4)"
                :cursor "pointer"}
               
               :else
               {:background "#cccccc"
                :box-shadow "none"
                :cursor "not-allowed"
                :opacity "0.6"}))}
    (cond
      accepting "⏳ ACCEPTING..."
      can-accept "ACCEPT INVITATION"
      :else "CANNOT ACCEPT")]])

(defn email-mismatch-warning
  "Warning component when emails don't match"
  [current-user details invitation-id]
  (let [logged-in-email (:user/email current-user)
        invited-email (:invitee/email details)]
    [:div {:style {:background "linear-gradient(145deg, #fff8e1 0%, #fff3cd 100%)" 
                   :border "2px solid #ffd700"
                   :border-radius "12px"
                   :padding "30px"
                   :margin "35px 0"
                   :text-align "center"
                   :box-shadow "0 8px 25px rgba(255, 215, 0, 0.15)"
                   :animation "fadeInUp 0.6s ease-out"
                   :font-family "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"}}
     [:div {:style {:display "flex" :align-items "center" :justify-content "center" :margin-bottom "20px"}}
      [:div {:style {:font-size "32px" :margin-right "15px"}} "⚠️"]
      [:h3 {:style {:margin 0 :color "#333333" :font-size "24px" :font-weight "700"}}
       "Wrong Account - Email Mismatch"]]
     
     [:div {:style {:background "rgba(255, 255, 255, 0.7)"
                    :border-radius "8px"
                    :padding "20px"
                    :margin "20px 0"
                    :text-align "left"}}
      [:div {:style {:margin-bottom "15px"}}
       [:p {:style {:margin "0 0 8px 0" :color "#666666" :font-size "16px"}}
        "🎯 Invitation sent to:"]
       [:p {:style {:margin 0 :color "#333333" :font-size "18px" :font-weight "600"}}
        invited-email]]
      [:div
       [:p {:style {:margin "0 0 8px 0" :color "#666666" :font-size "16px"}}
        "👤 You're logged in as:"]
       [:p {:style {:margin 0 :color "#333333" :font-size "18px" :font-weight "600"}}
        logged-in-email]]]
     
     [:p {:style {:margin "25px 0" :color "#666666" :font-size "16px" :line-height "1.6"}}
      "You need to logout and login with the correct account to accept this invitation."]
     
     [:div {:style {:display "flex" :justify-content "center"}}
      [:button 
       {:on-click #(set! (.-location js/window) 
                         (str "/logout/flex?redirect=" (js/encodeURIComponent (str "/accept-invitation?id=" invitation-id))))
        :style {:background "linear-gradient(135deg, #ffd700 0%, #ffed4e 100%)"
                :color "#333333"
                :padding "18px 40px"
                :border "none"
                :border-radius "50px"
                :font-weight "700"
                :font-size "16px"
                :letter-spacing "0.5px"
                :text-transform "uppercase"
                :cursor "pointer"
                :transition "all 0.3s ease"
                :box-shadow "0 8px 25px rgba(255, 215, 0, 0.4)"
                :font-family "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"}}
       "🔄 LOGOUT & SWITCH ACCOUNT"]]]))

(defn invitation-header
  "Header with logo and title matching email template"
  []
  [:div {:style {:background "linear-gradient(135deg, #ffd700 0%, #ffed4e 100%)"
                 :padding "40px 40px 30px"
                 :text-align "center"}}
   [:img {:src "https://ironrainbowcoating.com/logo/logo-good-size.png"
          :alt "Iron Rainbow"
          :style {:height "100px"
                  :margin-bottom "20px"}}]
   [:h1 {:style {:margin 0
                 :color "#333333"
                 :font-size "28px"
                 :font-weight "700"
                 :letter-spacing "-0.5px"
                 :font-family "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"}}
    "YOU ARE INVITED TO RAINBOW FLEX"]])

(defn valid-invitation-content
  "Renders content when id is valid"
  [id details loading current-user accepting set-accepting]
  (let [email-match (emails-match? current-user details)
        can-accept (and current-user details email-match)]
    [:div
     [invitation-header]
     [:div {:style {:padding "40px"}}
      
      (if loading
        [loading-message]
        (when details
          [:div
           [workspace-title details]
           [invitation-description details]]))
      
      ;; Show warning if emails don't match
      (when (and current-user details (not email-match))
        [email-mismatch-warning current-user details id])
      
      ;; Show different messages based on state
      (cond
        (not current-user)
        [login-prompt]
        
        (not details)
        [loading-message]
        
        email-match
        [accept-invitation-prompt]
        
        :else
        [cannot-accept-message])
      
      ;; Show Accept button only if user is logged in
      (when (and current-user email-match)
        [accept-button can-accept accepting id set-accepting])]]))

(defn invalid-invitation-content
  "Renders content when id is invalid or invitation doesn't exist"
  []
  [:div
   [invitation-header]
   [:div {:style {:padding "40px" :text-align "center"}}
    [:div {:style {:font-size "80px" :margin-bottom "20px"}} "❌"]
    [:h2 {:style {:margin "0 0 20px 0" 
                  :color "#333333" 
                  :font-size "32px" 
                  :font-weight "700"
                  :font-family "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"}}
     "Invitation Not Available"]
    [:div {:style {:background "#f8f9fa"
                   :border "1px solid #dee2e6"
                   :border-radius "8px"
                   :padding "20px"
                   :margin-bottom "30px"
                   :text-align "left"
                   :max-width "400px"
                   :margin-left "auto"
                   :margin-right "auto"}}
     [:h3 {:style {:margin "0 0 15px 0" :color "#333333" :font-size "18px" :font-weight "600"}}
      "This invitation is no longer valid"]
     [:p {:style {:color "#666666" :font-size "14px" :line-height "1.6" :margin "10px 0"}}
      "This could happen for several reasons:"]
     [:ul {:style {:color "#666666" :font-size "14px" :line-height "1.6" :margin "10px 0" :padding-left "20px"}}
      [:li "The invitation has already been accepted"]
      [:li "The invitation has been revoked by the workspace owner"]
      [:li "The invitation has expired"]
      [:li "The invitation ID is incorrect or corrupted"]]]
    [:div {:style {:display "flex" :justify-content "center" :margin "35px 0"}}
     [:a 
      {:href "/flex"
       :style {:display "inline-block"
               :background "linear-gradient(135deg, #ffd700 0%, #ffed4e 100%)"
               :color "#333333"
               :text-decoration "none"
               :padding "18px 40px"
               :border-radius "50px"
               :font-weight "700"
               :font-size "16px"
               :letter-spacing "0.5px"
               :text-transform "uppercase"
               :box-shadow "0 8px 25px rgba(255, 215, 0, 0.4)"
               :transition "all 0.3s ease"
               :font-family "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"}}
      "🏠 GO TO DASHBOARD"]]]])

(defn invitation-page
  "Main invitation acceptance page component"
  []
  (let [invitation-id @(subscribe [:db/get-in [:router :query-params :id]])
        current-user @(subscribe [:user/get])
        [details set-details] (react/use-state nil)
        [loading set-loading] (react/use-state false)
        [invalid set-invalid] (react/use-state false)
        [accepting set-accepting] (react/use-state false)]
    
    (use-invitation-details-effect invitation-id set-details set-loading set-invalid)
    
    [:div {:style {:height "100vh"
                   :width "100vw"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :position "relative"}}
     [keyframe-styles]
     [background-gradient]
     [invitation-card
      (cond
        ;; No invitation ID provided
        (not invitation-id)
        [invalid-invitation-content]
        
        ;; Invitation is invalid/deleted/expired
        invalid
        [invalid-invitation-content]
        
        ;; Valid invitation ID, show normal flow
        :else
        [valid-invitation-content invitation-id details loading current-user accepting set-accepting])]]))