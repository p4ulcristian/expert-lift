(ns features.site.profile.frontend.profile
  (:require
   [app.frontend.request :as request]
   [zero.frontend.react :as react]
   [router.frontend.zero :as router]))

(defn tab-logout [] 
  (when (js/confirm "Are you sure you want to log out?")
    ;; Open Auth0 logout in background tab (clears Auth0 session)
    (let [new-tab (js/window.open "https://dev-me4o6oy6ayzpw476.eu.auth0.com/v2/logout?federated" "_blank")]
      (.blur new-tab)  ; Don't focus the new tab
      (.focus js/window))  ; Keep focus on current window
    ;; Small delay to let Auth0 logout complete, then navigate main tab
    (js/setTimeout #(set! js/window.location.href "/logout/customizer") 500)))

(defn format-date [date-str]
  (when date-str
    (let [date (js/Date. date-str)]
      (.toLocaleDateString date "en-US" #js {:year "numeric"
                                            :month "long"
                                            :day "numeric"}))))

(defn provider-icon [provider]
  (case provider
    "google-oauth2" "🔍"
    "facebook" "📘"
    "twitter" "🐦"
    "github" "🐙"
    "auth0" "🔐"
    "microsoft" "🪟"
    "apple" "🍎"
    "🔗"))

(defn provider-name [provider]
  (case provider
    "google-oauth2" "Google"
    "facebook" "Facebook"
    "twitter" "Twitter"
    "github" "GitHub"
    "auth0" "Auth0"
    "microsoft" "Microsoft"
    "apple" "Apple"
    (str provider " OAuth")))

(defn provider-color [provider]
  (case provider
    "google-oauth2" "linear-gradient(135deg, #4285f4 0%, #34a853 100%)"
    "facebook" "linear-gradient(135deg, #1877f2 0%, #42a5f5 100%)"
    "twitter" "linear-gradient(135deg, #1da1f2 0%, #0d8bd9 100%)"
    "github" "linear-gradient(135deg, #333 0%, #24292e 100%)"
    "auth0" "linear-gradient(135deg, #eb5424 0%, #d63384 100%)"
    "microsoft" "linear-gradient(135deg, #00a1f1 0%, #0078d4 100%)"
    "apple" "linear-gradient(135deg, #000 0%, #333 100%)"
    "linear-gradient(135deg, #6b7280 0%, #4b5563 100%)"))

(defn linked-accounts-section [oauth-providers]
  [:div {:style {:background "white"
                 :border-radius "12px"
                 :padding "2rem"
                 :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"
                 :margin-bottom "2rem"}}
   [:h2 {:style {:font-size "1.5rem"
                 :font-weight "600"
                 :color "#1f2937"
                 :margin-bottom "1.5rem"}}
    "Linked Accounts"]
   
   (if (seq oauth-providers)
     [:div {:style {:display "grid"
                    :grid-template-columns "repeat(auto-fit, minmax(280px, 1fr))"
                    :gap "1rem"}}
      (for [provider oauth-providers]
        [:div {:key (:id provider)
               :style {:display "flex"
                       :align-items "center"
                       :padding "1.2rem"
                       :background (provider-color (:provider provider))
                       :color "white"
                       :border-radius "12px"
                       :box-shadow "0 4px 12px rgba(0,0,0,0.15)"
                       :transition "all 0.2s ease"}}
         [:div {:style {:font-size "2rem"
                        :margin-right "1rem"}}
          (provider-icon (:provider provider))]
         [:div {:style {:flex 1}}
          [:div {:style {:font-size "1.1rem"
                         :font-weight "600"
                         :margin-bottom "0.25rem"}}
           (provider-name (:provider provider))]
          [:div {:style {:font-size "0.875rem"
                         :opacity 0.9}}
           (str "Connected " (format-date (:created_at provider)))]]])]
     
     [:div {:style {:text-align "center"
                    :padding "2rem"
                    :color "#6b7280"}}
      [:div {:style {:font-size "3rem"
                     :margin-bottom "1rem"}} "🔗"]
      [:div {:style {:font-size "1.1rem"
                     :font-weight "500"
                     :margin-bottom "0.5rem"}} "No linked accounts found"]
      [:div {:style {:font-size "0.9rem"}} "Your OAuth providers will appear here once you sign in."]])])

(defn profile-header [user]
  [:div {:style {:background "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
                 :color "white"
                 :padding "3rem 2rem"
                 :border-radius "16px"
                 :margin-bottom "2rem"
                 :box-shadow "0 10px 30px rgba(0,0,0,0.15)"
                 :text-align "center"}}
   [:div {:style {:display "flex"
                  :flex-direction "column"
                  :align-items "center"
                  :gap "1.5rem"}}
    ;; Profile picture
    [:div {:style {:position "relative"}}
     (if (:picture_url user)
       [:img {:src (:picture_url user)
              :alt (str (:name user) "'s profile")
              :style {:width "120px"
                      :height "120px"
                      :border-radius "50%"
                      :border "4px solid rgba(255,255,255,0.3)"
                      :object-fit "cover"
                      :box-shadow "0 8px 32px rgba(0,0,0,0.3)"}}]
       [:div {:style {:width "120px"
                      :height "120px"
                      :border-radius "50%"
                      :background "rgba(255,255,255,0.2)"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :font-size "3rem"
                      :border "4px solid rgba(255,255,255,0.3)"
                      :box-shadow "0 8px 32px rgba(0,0,0,0.3)"}}
        "👤"])]
    
    ;; User info
    [:div
     [:h1 {:style {:font-size "2.5rem"
                   :font-weight "700"
                   :margin "0 0 0.5rem 0"
                   :text-shadow "0 2px 4px rgba(0,0,0,0.3)"}}
      (or (:name user) "Welcome!")]
     [:p {:style {:font-size "1.2rem"
                  :opacity 0.9
                  :margin "0 0 0.5rem 0"}}
      (:email user)]
     [:p {:style {:font-size "0.95rem"
                  :opacity 0.7
                  :margin 0}}
      (str "Member since " (format-date (:created_at user)))]]]])

(defn profile-actions []
  [:div {:style {:background "white"
                 :border-radius "12px"
                 :padding "2rem"
                 :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"
                 :margin-bottom "2rem"}}
   [:h2 {:style {:font-size "1.5rem"
                 :font-weight "600"
                 :color "#1f2937"
                 :margin-bottom "1.5rem"}}
    "Quick Actions"]
   
   [:div {:style {:display "grid"
                  :grid-template-columns "repeat(auto-fit, minmax(200px, 1fr))"
                  :gap "1rem"}}
    ;; View Orders
    [:button {:style {:display "flex"
                      :flex-direction "column"
                      :align-items "center"
                      :padding "1.5rem"
                      :background "linear-gradient(135deg, #10b981 0%, #059669 100%)"
                      :color "white"
                      :border "none"
                      :border-radius "12px"
                      :cursor "pointer"
                      :transition "all 0.2s ease"
                      :box-shadow "0 4px 12px rgba(16, 185, 129, 0.3)"}
              :on-click #(router/navigate! {:path "/orders"})}
     [:div {:style {:font-size "2.5rem"
                    :margin-bottom "0.5rem"}} "📦"]
     [:div {:style {:font-size "1.1rem"
                    :font-weight "600"
                    :margin-bottom "0.25rem"}} "My Orders"]
     [:div {:style {:font-size "0.875rem"
                    :opacity 0.9
                    :text-align "center"}} "View and track your orders"]]
    
    ;; Logout
    [:button {:style {:display "flex"
                      :flex-direction "column"
                      :align-items "center"
                      :padding "1.5rem"
                      :background "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)"
                      :color "white"
                      :border "none"
                      :border-radius "12px"
                      :cursor "pointer"
                      :transition "all 0.2s ease"
                      :box-shadow "0 4px 12px rgba(239, 68, 68, 0.3)"}
              :on-click tab-logout}
     [:div {:style {:font-size "2.5rem"
                    :margin-bottom "0.5rem"}} "🚪"]
     [:div {:style {:font-size "1.1rem"
                    :font-weight "600"
                    :margin-bottom "0.25rem"}} "Sign Out"]
     [:div {:style {:font-size "0.875rem"
                    :opacity 0.9
                    :text-align "center"}} "Log out of your account"]]]])

(defn profile-stats [user]
  [:div {:style {:background "white"
                 :border-radius "12px"
                 :padding "2rem"
                 :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"}}
   [:h2 {:style {:font-size "1.5rem"
                 :font-weight "600"
                 :color "#1f2937"
                 :margin-bottom "1.5rem"}}
    "Account Information"]
   
   [:div {:style {:display "grid"
                  :grid-template-columns "repeat(auto-fit, minmax(250px, 1fr))"
                  :gap "1.5rem"}}
    [:div
     [:h3 {:style {:font-size "0.875rem"
                   :font-weight "500"
                   :color "#6b7280"
                   :margin-bottom "0.5rem"}}
      "User ID"]
     [:p {:style {:font-size "0.875rem"
                  :font-family "monospace"
                  :color "#374151"
                  :background "#f9fafb"
                  :padding "0.5rem"
                  :border-radius "6px"
                  :margin 0}}
      (:id user)]]
    
    [:div
     [:h3 {:style {:font-size "0.875rem"
                   :font-weight "500"
                   :color "#6b7280"
                   :margin-bottom "0.5rem"}}
      "Email Address"]
     [:p {:style {:font-size "1rem"
                  :color "#374151"
                  :margin 0}}
      (:email user)]]
    
    [:div
     [:h3 {:style {:font-size "0.875rem"
                   :font-weight "500"
                   :color "#6b7280"
                   :margin-bottom "0.5rem"}}
      "Account Created"]
     [:p {:style {:font-size "1rem"
                  :color "#374151"
                  :margin 0}}
      (format-date (:created_at user))]]]])

(defn view []
  (let [[user set-user] (react/use-state nil)
        [oauth-providers set-oauth-providers] (react/use-state [])
        [loading set-loading] (react/use-state true)]
    
    (react/use-effect
     {:mount (fn []
               (set-loading true)
               (request/pathom
                {:query '[:site/user-profile :site/user-oauth-providers]
                 :callback (fn [response]
                            (set-loading false)
                            (set-user (:site/user-profile response))
                            (set-oauth-providers (:site/user-oauth-providers response)))}))})
    
    [:div {:style {:min-height "100vh"
                   :background "linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%)"
                   :padding "2rem"}}
     [:div {:style {:max-width "800px"
                    :margin "0 auto"}}
      
      (if loading
        [:div {:style {:display "flex"
                       :flex-direction "column"
                       :justify-content "center"
                       :align-items "center"
                       :padding "4rem"
                       :background "#ffffff"
                       :border-radius "16px"
                       :border "1px solid #e5e7eb"
                       :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"}}
         [:div {:style {:font-size "3rem"
                        :margin-bottom "1rem"
                        :animation "pulse 2s infinite"}} "👤"]
         [:div {:style {:font-size "1.2rem"
                        :color "#4b5563"
                        :font-weight "500"}} "Loading your profile..."]
         [:div {:style {:font-size "0.9rem"
                        :color "#6b7280"
                        :margin-top "0.5rem"}} "Just a moment!"]]
        
        (if user
          [:div
           [profile-header user]
           [profile-actions]
           [linked-accounts-section oauth-providers]
           [profile-stats user]]
          
          [:div {:style {:text-align "center"
                         :padding "4rem"
                         :background "#ffffff"
                         :border-radius "16px"
                         :border "1px solid #e5e7eb"
                         :box-shadow "0 4px 6px -1px rgba(0, 0, 0, 0.1)"}}
           [:div {:style {:font-size "4rem"
                          :margin-bottom "1.5rem"}} "😕"]
           [:h2 {:style {:font-size "1.5rem"
                         :font-weight "600"
                         :color "#1f2937"
                         :margin-bottom "0.5rem"}} "Profile not found"]
           [:p {:style {:color "#6b7280"
                        :font-size "1.1rem"}} "We couldn't load your profile information."]]))]]))