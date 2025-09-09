(ns features.site.zero.frontend.zero
  (:require
   [app.frontend.request :as request]
   [features.site.homepage.routes :as homepage-routes]
   [features.site.orders.routes :as orders-routes]
   [features.site.profile.routes :as profile-routes]
   [router.frontend.zero :as router]
   [zero.frontend.re-frame]
   [zero.frontend.react :as react]))

(defn simple-logout []
  (when (js/confirm "Are you sure you want to log out?")
    ;; Open Auth0 logout in background tab (clears Auth0 session)
    (let [new-tab (js/window.open "https://dev-me4o6oy6ayzpw476.eu.auth0.com/v2/logout?returnTo=https://localhost" "_blank")]
      (.blur new-tab)  ; Don't focus the new tab
      (.focus js/window))  ; Keep focus on current window
    ;; Small delay to let Auth0 logout complete, then navigate main tab
    (js/setTimeout #(set! js/window.location.href "/logout/customizer") 500)))

(def routes (concat
             homepage-routes/routes
             orders-routes/routes
             profile-routes/routes))

(defn profile-picture []
  (let [[user set-user] (react/use-state nil)]
    (react/use-effect
     {:mount (fn []
               (request/pathom
                {:query '[:site/user-profile]
                 :callback (fn [response]
                            (set-user (:site/user-profile response)))}))})
    
    (if user
      ;; Logged in - show profile picture
      [:a {:href "/profile"
           :style {:text-decoration "none"}}
       (if (:picture_url user)
         [:img {:src (:picture_url user)
                :alt "Profile"
                :style {:width "40px"
                        :height "40px"
                        :border-radius "50%"
                        :border "2px solid rgba(246, 213, 92, 0.3)"
                        :object-fit "cover"
                        :transition "all 0.3s ease"}
                :on-mouse-enter #(set! (.-style.borderColor (.-target %)) "#f6d55c")
                :on-mouse-leave #(set! (.-style.borderColor (.-target %)) "rgba(246, 213, 92, 0.3)")}]
         [:div {:style {:width "40px"
                        :height "40px"
                        :border-radius "50%"
                        :background "rgba(246, 213, 92, 0.2)"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"
                        :color "#f6d55c"
                        :font-size "1.2rem"
                        :border "2px solid rgba(246, 213, 92, 0.3)"
                        :transition "all 0.3s ease"}
                :on-mouse-enter #(do
                                  (set! (.-style.borderColor (.-target %)) "#f6d55c")
                                  (set! (.-style.background (.-target %)) "rgba(246, 213, 92, 0.3)"))
                :on-mouse-leave #(do
                                  (set! (.-style.borderColor (.-target %)) "rgba(246, 213, 92, 0.3)")
                                  (set! (.-style.background (.-target %)) "rgba(246, 213, 92, 0.2)"))}
          [:i {:class "fas fa-user"}]])]
      ;; Not logged in - show login button
      [:a {:href "/login/customizer"
           :style {:color "#4ade80"
                   :text-decoration "none"
                   :font-weight "600"
                   :font-size "1rem"
                   :padding "0.75rem 1rem"
                   :border "2px solid rgba(74, 222, 128, 0.3)"
                   :border-radius "12px"
                   :background "rgba(74, 222, 128, 0.1)"
                   :transition "all 0.3s ease"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :backdrop-filter "blur(5px)"
                   :box-shadow "0 2px 10px rgba(0, 0, 0, 0.3)"}
           :title "Login"
           :on-mouse-enter #(do
                             (set! (.-style.background (.-target %)) "#4ade80")
                             (set! (.-style.color (.-target %)) "#333")
                             (set! (.-style.transform (.-target %)) "translateY(-2px)")
                             (set! (.-style.boxShadow (.-target %)) "0 4px 15px rgba(74, 222, 128, 0.4)"))
           :on-mouse-leave #(do
                             (set! (.-style.background (.-target %)) "rgba(74, 222, 128, 0.1)")
                             (set! (.-style.color (.-target %)) "#4ade80")
                             (set! (.-style.transform (.-target %)) "translateY(0)")
                             (set! (.-style.boxShadow (.-target %)) "0 2px 10px rgba(0, 0, 0, 0.3)"))}
       [:i {:class "fas fa-sign-in-alt"}]])))

(defn site-header []
  (let [[user set-user] (react/use-state nil)]
    (react/use-effect
     {:mount (fn []
               (request/pathom
                {:query '[:site/user-profile]
                 :callback (fn [response]
                            (set-user (:site/user-profile response)))}))})
    
    [:div {:style {:background "linear-gradient(135deg, #333 0%, #2a2a2a 50%, #1a1a1a 100%)"
                   :border-bottom "2px solid #f6d55c"
                   :padding "1rem 2rem"
                   :display "flex"
                   :align-items "center"
                   :justify-content "space-between"
                   :box-shadow "0 4px 20px rgba(246, 213, 92, 0.3), 0 0 40px rgba(0, 0, 0, 0.5)"
                   :position "sticky"
                   :top "0"
                   :z-index "1000"
                   :backdrop-filter "blur(10px)"}}
     [:div {:style {:display "flex"
                    :align-items "center"
                    :gap "1.5rem"}}
      [:a {:href "/"
           :style {:display "flex"
                   :align-items "center"
                   :gap "1rem"
                   :text-decoration "none"
                   :cursor "pointer"}}
       [:img {:src "/logo/text-horizontal.svg"
              :style {:height "45px"
                      :width "auto"}}]]]
     [:nav {:style {:display "flex"
                    :align-items "center"
                    :gap "1.5rem"}}
      ;; Show orders only when logged in
      (when user
        [:a {:href "/orders"
             :style {:color "#f6d55c"
                     :text-decoration "none"
                     :font-weight "600"
                     :font-size "1rem"
                     :padding "0.75rem 1.5rem"
                     :border "2px solid rgba(246, 213, 92, 0.3)"
                     :border-radius "12px"
                     :background "rgba(246, 213, 92, 0.1)"
                     :transition "all 0.3s ease"
                     :display "flex"
                     :align-items "center"
                     :gap "0.5rem"
                     :backdrop-filter "blur(5px)"
                     :box-shadow "0 2px 10px rgba(0, 0, 0, 0.3)"}
             :on-mouse-enter #(do
                               (set! (.-style.background (.-target %)) "#f6d55c")
                               (set! (.-style.color (.-target %)) "#333")
                               (set! (.-style.transform (.-target %)) "translateY(-2px)")
                               (set! (.-style.boxShadow (.-target %)) "0 4px 15px rgba(246, 213, 92, 0.4)"))
             :on-mouse-leave #(do
                               (set! (.-style.background (.-target %)) "rgba(246, 213, 92, 0.1)")
                               (set! (.-style.color (.-target %)) "#f6d55c")
                               (set! (.-style.transform (.-target %)) "translateY(0)")
                               (set! (.-style.boxShadow (.-target %)) "0 2px 10px rgba(0, 0, 0, 0.3)"))}
         [:i {:class "fas fa-box"}] "Orders"])
      [:a {:href "/customize"
           :style {:color "#10b981"
                   :text-decoration "none"
                   :font-weight "600"
                   :font-size "1rem"
                   :padding "0.75rem 1.5rem"
                   :border "2px solid rgba(16, 185, 129, 0.3)"
                   :border-radius "12px"
                   :background "rgba(16, 185, 129, 0.1)"
                   :transition "all 0.3s ease"
                   :display "flex"
                   :align-items "center"
                   :gap "0.5rem"
                   :backdrop-filter "blur(5px)"
                   :box-shadow "0 2px 10px rgba(0, 0, 0, 0.3)"}
           :on-mouse-enter #(do
                             (set! (.-style.background (.-target %)) "#10b981")
                             (set! (.-style.color (.-target %)) "#333")
                             (set! (.-style.transform (.-target %)) "translateY(-2px)")
                             (set! (.-style.boxShadow (.-target %)) "0 4px 15px rgba(16, 185, 129, 0.4)"))
           :on-mouse-leave #(do
                             (set! (.-style.background (.-target %)) "rgba(16, 185, 129, 0.1)")
                             (set! (.-style.color (.-target %)) "#10b981")
                             (set! (.-style.transform (.-target %)) "translateY(0)")
                             (set! (.-style.boxShadow (.-target %)) "0 2px 10px rgba(0, 0, 0, 0.3)"))}
       [:i {:class "fas fa-palette"}] "Customizer"]
      [profile-picture]
      ;; Show logout button when logged in
      (when user
        [:button {:style {:color "#ff6b6b"
                          :text-decoration "none"
                          :font-weight "600"
                          :font-size "1rem"
                          :padding "0.75rem 1rem"
                          :border "2px solid rgba(255, 107, 107, 0.3)"
                          :border-radius "12px"
                          :background "rgba(255, 107, 107, 0.1)"
                          :transition "all 0.3s ease"
                          :display "flex"
                          :align-items "center"
                          :justify-content "center"
                          :backdrop-filter "blur(5px)"
                          :box-shadow "0 2px 10px rgba(0, 0, 0, 0.3)"
                          :cursor "pointer"}
                  :title "Logout"
                  :on-mouse-enter #(do
                                    (set! (.-style.background (.-target %)) "#ff6b6b")
                                    (set! (.-style.color (.-target %)) "#333")
                                    (set! (.-style.transform (.-target %)) "translateY(-2px)")
                                    (set! (.-style.boxShadow (.-target %)) "0 4px 15px rgba(255, 107, 107, 0.4)"))
                  :on-mouse-leave #(do
                                    (set! (.-style.background (.-target %)) "rgba(255, 107, 107, 0.1)")
                                    (set! (.-style.color (.-target %)) "#ff6b6b")
                                    (set! (.-style.transform (.-target %)) "translateY(0)")
                                    (set! (.-style.boxShadow (.-target %)) "0 2px 10px rgba(0, 0, 0, 0.3)"))
                  :on-click simple-logout}
         [:i {:class "fas fa-sign-out-alt"}]])]]))

(defn view []
  (let [router-data (:data @router/state)]
    [:div
     [site-header]
     [:div {:style {:min-height "calc(100vh - 80px)"}}
      [(:view router-data)]]]))
