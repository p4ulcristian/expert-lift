(ns features.flex.shared.frontend.ui.header
  (:require
   ["react" :as react]
   [app.frontend.request :as request]
   [reagent.core :as r]
   [re-frame.core :as rf]
   [router.frontend.zero :as router]
   [ui.button :as button]
   [ui.tooltip :as tooltip]
   [features.flex.shared.frontend.ui.sidebar :as sidebar]))

(defn hamburger-menu []
  [:div {:style {:margin-right "16px"}}
   [tooltip/view {:tooltip "Menu"
                  :align [:center :bottom]
                  :anchor [:center :top]
                  :popover-style {:z-index 1001}}
    [button/view {:mode :text
                  :color "white"
                  :style {:width "32px"
                          :height "32px"
                          :border-radius "6px"
                          :display "flex"
                          :align-items "center"
                          :justify-content "center"
                          :background "rgba(255, 255, 255, 0.05)"
                          :border "1px solid rgba(255, 255, 255, 0.1)"
                          :transition "all 0.2s ease"
                          :hover {:background-color "rgba(255, 215, 13, 0.15)"
                                  :border-color "rgba(255, 215, 13, 0.3)"
                                  :color "white"}}
                  :on-click #(swap! sidebar/open? not)}
     [:i {:class "fa-solid fa-bars"
          :style {:font-size "14px"
                  :color "white"}}]]]])

(defn logo []
  [:div {:style {:display "flex"
                 :align-items "center"
                 :height "100%"}}
   [hamburger-menu]
   [:img {:src "/logo/text-horizontal.svg"
          :style {:height "28px"
                  :cursor "pointer"}
          :on-click #(let [wsid @(rf/subscribe [:workspace/get-id])]
                       (router/navigate! {:path (str "/flex/ws/" wsid)}))}]])

(defn search-bar []
  (let [search-value (r/atom "")]
    (fn []
      [:div {:style {:display "flex"
                     :align-items "center"
                     :background "rgba(255, 255, 255, 0.1)"
                     :border-radius "8px"
                     :padding "8px 12px"
                     :min-width "320px"
                     :max-width "500px"
                     :flex "1"
                     :margin "0 20px"
                     :border "1px solid rgba(255, 255, 255, 0.2)"}}
       [:i {:class "fa-solid fa-search"
            :style {:color "rgba(255, 255, 255, 0.7)"
                    :margin-right "8px"
                    :font-size "14px"}}]
       [:input {:type "text"
                :placeholder "Search orders, jobs, customers..."
                :value @search-value
                :on-change #(reset! search-value (-> ^js % .-target .-value))
                :class "search-input"
                :style {:border "none"
                        :background "transparent"
                        :outline "none"
                        :font-size "14px"
                        :width "100%"
                        :color "white"}}]
       [:style "
        .search-input::placeholder {
          color: rgba(255, 255, 255, 0.6) !important;
        }"]])))

(defn- notifications-dropdown-menu []
  [:div {:style {:position "absolute"
                 :top "100%"
                 :right "0"
                 :background "#242936"
                 :border "1px solid rgba(255, 215, 13, 0.2)"
                 :border-radius "8px"
                 :box-shadow "0 4px 12px rgba(0,0,0,0.4)"
                 :min-width "200px"
                 :z-index 1000
                 :margin-top "4px"}}
   [:div {:style {:padding "8px 0"}}
    [:div {:style {:padding "8px 16px"
                   :border-bottom "1px solid rgba(255, 255, 255, 0.1)"
                   :color "rgba(255, 255, 255, 0.7)"
                   :font-size "12px"
                   :text-transform "uppercase"
                   :letter-spacing "0.5px"}}
     "Notifications"]
    [:div {:style {:padding "16px"
                   :color "rgba(255, 255, 255, 0.6)"
                   :font-size "14px"
                   :text-align "center"}}
     "No notifications"]]])

(defn notifications-button []
  (let [[dropdown-open? set-dropdown-open] (react/useState false)
        dropdown-ref (react/useRef nil)]
    
    (react/useEffect
     (fn []
       (let [handle-click-outside (fn [event]
                                    (when (and dropdown-open?
                                               (.-current dropdown-ref)
                                               (not (.contains (.-current dropdown-ref) (.-target event))))
                                      (set-dropdown-open false)))]
         (.addEventListener js/document "mousedown" handle-click-outside)
         (fn []
           (.removeEventListener js/document "mousedown" handle-click-outside))))
     #js [dropdown-open?])
    
    [:div {:ref dropdown-ref
           :style {:margin-right "16px"
                   :position "relative"}}
     [button/view {:mode :text
                   :color "white"
                   :style {:width "32px"
                           :height "32px"
                           :border-radius "6px"
                           :display "flex"
                           :align-items "center"
                           :justify-content "center"
                           :background "rgba(255, 255, 255, 0.05)"
                           :border "1px solid rgba(255, 255, 255, 0.1)"
                           :transition "all 0.2s ease"
                           :hover {:background-color "rgba(255, 215, 13, 0.15)"
                                   :border-color "rgba(255, 215, 13, 0.3)"
                                   :color "white"}}
                   :on-click #(set-dropdown-open (not dropdown-open?))}
      [:i {:class "fa-solid fa-bell"
           :style {:font-size "14px"
                   :color "white"}}]]
     (when dropdown-open?
       [notifications-dropdown-menu])]))

(defn fullscreen-button [{:keys [is-fullscreen]}]
  [:div {:style {:margin-right "16px"}}
   [tooltip/view {:tooltip (if is-fullscreen "Exit Fullscreen" "Fullscreen")
                  :align [:center :bottom]
                  :anchor [:center :top]
                  :popover-style {:z-index 1001}}
    [button/view {:mode :text
                  :color "white"
                  :style {:width "32px"
                          :height "32px"
                          :border-radius "6px"
                          :display "flex"
                          :align-items "center"
                          :justify-content "center"
                          :background "rgba(255, 255, 255, 0.05)"
                          :border "1px solid rgba(255, 255, 255, 0.1)"
                          :transition "all 0.2s ease"
                          :hover {:background-color "rgba(255, 215, 13, 0.15)"
                                  :border-color "rgba(255, 215, 13, 0.3)"
                                  :color "white"}}
                  :on-click #(if (.-fullscreenElement js/document)
                               (.exitFullscreen js/document)
                               (.requestFullscreen (.-documentElement js/document)))}
     [:i {:class (if is-fullscreen "fas fa-compress" "fas fa-expand")
          :style {:font-size "14px"
                  :color "white"}}]]]])



(defn- dropdown-account-section [{:keys [user-data]}]
  [:div
   [:div {:style {:padding "8px 16px"
                  :border-bottom "1px solid rgba(255, 255, 255, 0.1)"
                  :color "rgba(255, 255, 255, 0.7)"
                  :font-size "12px"
                  :text-transform "uppercase"
                  :letter-spacing "0.5px"}}
    "Account"]
   [:div {:style {:padding "12px 16px"
                  :color "white"}}
    [:div {:style {:font-size "14px"
                   :font-weight "500"
                   :margin-bottom "4px"}}
     (or (:user/full-name user-data) "User")]
    [:div {:style {:font-size "12px"
                   :color "rgba(255, 255, 255, 0.6)"}}
     (or (:user/email user-data) "")]]])

(defn- dropdown-workspace-section []
  (let [workspace @(rf/subscribe [:workspace/get])]
    [:div
     [:div {:style {:padding "8px 16px"
                    :border-bottom "1px solid rgba(255, 255, 255, 0.1)"
                    :color "rgba(255, 255, 255, 0.7)"
                    :font-size "12px"
                    :text-transform "uppercase"
                    :letter-spacing "0.5px"}}
      "Workspace"]
     [:div {:style {:padding "12px 16px"
                    :color "white"}}
      [:div {:style {:display "flex"
                     :align-items "center"}}
       [:i {:class "fa-solid fa-building"
            :style {:margin-right "8px"
                    :font-size "14px"
                    :color "rgba(255, 215, 13, 0.8)"}}]
       [:div {:style {:font-size "14px"
                      :font-weight "500"}}
        (or (:workspace/name workspace) "None selected")]]]]))

(defn- dropdown-actions [{:keys [dropdown-open? set-dropdown-open]}]
  [:div
   [:div {:style {:padding "8px 16px"
                  :cursor "pointer"
                  :font-size "14px"
                  :color "white"
                  :hover {:background-color "rgba(255, 255, 255, 0.1)"}}
          :on-click (fn []
                      (set-dropdown-open false)
                      (router/navigate! {:path "/flex"}))}
    [:i {:class "fa-solid fa-th"
         :style {:margin-right "8px"
                 :width "14px"}}]
    "Switch Workspace"]
   [:div {:style {:height "1px"
                  :background "rgba(255, 255, 255, 0.2)"
                  :margin "4px 0"}}]
   [:div {:style {:padding "8px 16px"
                  :cursor "pointer"
                  :font-size "14px"
                  :color "#ff6b6b"
                  :hover {:background-color "rgba(255, 255, 255, 0.1)"}}
          :on-click (fn [_]
                      (set-dropdown-open false)
                      (when (js/confirm "Are you sure you want to log out?")
                        ;; Navigate directly to backend logout handler which handles Auth0 properly
                        (set! js/window.location.href "/logout/flex")))}
    [:i {:class "fa-solid fa-right-from-bracket"
         :style {:margin-right "8px"
                 :width "14px"}}]
    "Logout"]])

(defn- profile-button-content [{:keys [profile-pic]}]
  (if profile-pic
    [:img {:src profile-pic
           :style {:width "24px"
                   :height "24px"
                   :max-width "fit-content"
                   :border-radius "50%"
                   :object-fit "cover"}}]
    [:i {:class "fa-solid fa-user"
         :style {:color "white"
                 :font-size "14px"}}]))

(defn- profile-button [{:keys [profile-pic on-click]}]
  [button/view {:mode :text
                :color "white"
                :style {:width "32px"
                        :height "32px"
                        :border-radius "6px"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"
                        :background "rgba(255, 255, 255, 0.05)"
                        :border "1px solid rgba(255, 255, 255, 0.1)"
                        :transition "all 0.2s ease"
                        :hover {:background-color "rgba(255, 215, 13, 0.15)"
                                :border-color "rgba(255, 215, 13, 0.3)"
                                :color "white"}}
                :on-click on-click}
   [profile-button-content {:profile-pic profile-pic}]])

(defn- dropdown-menu [{:keys [user-data dropdown-open? set-dropdown-open]}]
  [:div {:style {:position "absolute"
                 :top "100%"
                 :right "0"
                 :background "#242936"
                 :border "1px solid rgba(255, 215, 13, 0.2)"
                 :border-radius "8px"
                 :box-shadow "0 4px 12px rgba(0,0,0,0.4)"
                 :min-width "180px"
                 :z-index 1000
                 :margin-top "4px"}}
   [:div {:style {:padding "8px 0"}}
    [dropdown-account-section {:user-data user-data}]
    [dropdown-workspace-section]
    [dropdown-actions {:dropdown-open? dropdown-open? 
                       :set-dropdown-open set-dropdown-open}]]])

(defn profile-dropdown [{:keys [user-data]}]
  (let [[dropdown-open? set-dropdown-open] (react/useState false)
        user-data @(rf/subscribe [:user/get])
        profile-pic (:user/picture-url user-data)
        dropdown-ref (react/useRef nil)]
    
    (react/useEffect
     (fn []
       (let [handle-click-outside (fn [event]
                                    (when (and dropdown-open?
                                               (.-current dropdown-ref)
                                               (not (.contains (.-current dropdown-ref) (.-target event))))
                                      (set-dropdown-open false)))]
         (.addEventListener js/document "mousedown" handle-click-outside)
         (fn []
           (.removeEventListener js/document "mousedown" handle-click-outside))))
     #js [dropdown-open?])
    
    [:div {:ref dropdown-ref
           :style {:position "relative"}}
     [profile-button {:profile-pic profile-pic 
                      :on-click #(set-dropdown-open (not dropdown-open?))}]
     (when dropdown-open?
       [dropdown-menu {:user-data user-data
                       :dropdown-open? dropdown-open?
                       :set-dropdown-open set-dropdown-open}])]))

(defn view [{:keys [user-data is-fullscreen grid-column grid-row]}]
  [:header {:style (merge {:display "flex"
                          :align-items "center"
                          :justify-content "space-between"
                          :height "60px"
                          :background "linear-gradient(135deg, #1a1f2e 0%, #0f1419 100%)"
                          :border-bottom "1px solid rgba(255, 215, 13, 0.2)"
                          :padding "0 24px"
                          :position "sticky"
                          :top "0"
                          :z-index "100"}
                         (when grid-column {:grid-column grid-column})
                         (when grid-row {:grid-row grid-row}))}
   [logo]
   [search-bar]
   [:div {:style {:display "flex"
                  :align-items "center"}}
    [fullscreen-button {:is-fullscreen is-fullscreen}]
    [notifications-button]
    [profile-dropdown {:user-data user-data}]]])