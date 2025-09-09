(ns features.site.homepage.frontend.view
  (:require
   [ui.link :as link]))


(defn view []
  [:div {:style {:min-height "100vh"
                 :background "linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 50%, #3a3a3a 100%)"
                 :display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :justify-content "center"}}
   [:div {:style {:max-width "1200px"
                  :margin "0 auto"
                  :padding "1rem"
                  :text-align "center"}}
    [:img {:src "/logo/logo-good-size.png"
           :style {:width "300px"
                   :max-width "100%"
                   :margin-bottom "2rem"}}]
    [:h1 {:style {:font-size "2.25rem"
                  :font-weight "bold"
                  :color "#fff"
                  :margin-bottom "1rem"}}
     "Welcome to Iron Rainbow"]
    [:p {:style {:font-size "1.25rem"
                 :color "#ccc"
                 :margin-bottom "2rem"}}
     "Your trusted partner in powder coating solutions"]
    [:div {:style {:display "flex"
                   :gap "1rem"
                   :justify-content "center"}}
     [link/view {:href "/flex"
                :mode :filled
                :color "#FFB800"
                :style {:text-decoration "none"
                       :display "inline-block"
                       :padding "0.5rem 1rem"
                       :border-radius "4px"
                       :background "#FFB800"
                       :color "#000"}}
      "Flex"]
     [link/view {:href "/orders"
                :mode :filled
                :color "#FFB800"
                :style {:text-decoration "none"
                       :display "inline-block"
                       :padding "0.5rem 1rem"
                       :border-radius "4px"
                       :background "#FFB800"
                       :color "#000"}}
      "Orders"]]
    [:div {:style {:margin-top "3rem"
                   :padding "2rem"
                   :background "rgba(255, 255, 255, 0.05)"
                   :border-radius "8px"
                   :max-width "800px"
                   :margin-left "auto"
                   :margin-right "auto"}}
     [:h2 {:style {:font-size "1.5rem"
                   :color "#fff"
                   :margin-bottom "1rem"}}
      "Simple Workstation Management"]
     [:p {:style {:font-size "1.1rem"
                  :color "#ccc"
                  :line-height "1.6"
                  :margin-bottom "2rem"}}
      "Manage your powder coating workstations, track processes, and keep everything running smoothly. Add new workstations, monitor their status, and organize your facility with ease."]
     [:div {:style {:display "flex"
                    :flex-wrap "wrap"
                    :gap "2rem"
                    :margin-top "1.5rem"
                    :justify-content "center"}}
      [:div {:style {:padding "1.5rem"
                     :background "rgba(255, 255, 255, 0.03)"
                     :border-radius "8px"
                     :width "250px"}}
       [:h3 {:style {:color "#FFB800"
                     :margin-bottom "1rem"}}
        "Workstations"]
       [:p {:style {:color "#ccc"
                    :font-size "0.9rem"
                    :line-height "1.5"}}
        "Add and manage your powder coating stations, blasting booths, and curing ovens."]]
      [:div {:style {:padding "1.5rem"
                     :background "rgba(255, 255, 255, 0.03)"
                     :border-radius "8px"
                     :width "250px"}}
       [:h3 {:style {:color "#FFB800"
                     :margin-bottom "1rem"}}
        "Processes"]
       [:p {:style {:color "#ccc"
                    :font-size "0.9rem"
                    :line-height "1.5"}}
        "Set up and track your powder coating processes and recipes."]]
      [:div {:style {:padding "1.5rem"
                     :background "rgba(255, 255, 255, 0.03)"
                     :border-radius "8px"
                     :width "250px"}}
       [:h3 {:style {:color "#FFB800"
                     :margin-bottom "1rem"}}
        "Inventory"]
       [:p {:style {:color "#ccc"
                    :font-size "0.9rem"
                    :line-height "1.5"}}
        "Keep track of your powder coating materials and supplies."]]]]]])