(ns features.labs.shared.frontend.components.header
  (:require
   [router.frontend.zero :as router]))

(defn labs-logout []
  "Labs logout: opens Auth0 logout in background tab and redirects to labs logout endpoint"
  (when (js/confirm "Are you sure you want to log out?")
    ;; Open Auth0 logout in background tab (clears Auth0 session)
    (let [new-tab (js/window.open "https://dev-me4o6oy6ayzpw476.eu.auth0.com/v2/logout?federated" "_blank")]
      (.blur new-tab)  ; Don't focus the new tab
      (.focus js/window))  ; Keep focus on current window
    ;; Small delay to let Auth0 logout complete, then navigate main tab
    (js/setTimeout #(set! js/window.location.href "/logout/labs") 500)))

(defn view []
  [:div {:style {:position "relative"
                 :width "100%"
                 :height "60px"
                 :background "var(--ir-primary)"
                 :border-bottom "1px solid var(--ir-border-light)"
                 :display "flex"
                 :align-items "center"
                 :justify-content "space-between"
                 :padding "0 20px"
                 }}
   [:img {:class "dashboard-logo"
          :src "/logo/logo-good-size.png"
          :alt "Iron Rainbow Logo"
          :on-click #(router/navigate! {:path "/irunrainbow"})
          :style {:cursor "pointer"
                  :width "40px"
                  :height "auto"}}]
   [:button {:style {:color "#ff6b6b"
                     :background "rgba(255, 107, 107, 0.1)"
                     :border "2px solid rgba(255, 107, 107, 0.3)"
                     :border-radius "8px"
                     :padding "0.5rem 1rem"
                     :font-weight "600"
                     :font-size "0.9rem"
                     :cursor "pointer"
                     :transition "all 0.3s ease"
                     :display "flex"
                     :align-items "center"
                     :gap "0.5rem"}
             :title "Logout"
             :on-mouse-enter #(do
                               (set! (.-style.background (.-target %)) "#ff6b6b")
                               (set! (.-style.color (.-target %)) "white")
                               (set! (.-style.transform (.-target %)) "translateY(-1px)"))
             :on-mouse-leave #(do
                               (set! (.-style.background (.-target %)) "rgba(255, 107, 107, 0.1)")
                               (set! (.-style.color (.-target %)) "#ff6b6b")
                               (set! (.-style.transform (.-target %)) "translateY(0)"))
             :on-click labs-logout}
    [:i {:class "fas fa-sign-out-alt"}] "Logout"]]) 