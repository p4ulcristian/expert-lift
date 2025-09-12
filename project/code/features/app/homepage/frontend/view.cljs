(ns features.app.homepage.frontend.view
  (:require
   [ui.link :as link]
   [ui.enhanced-button :as enhanced-button]
   [router.frontend.zero :as router]))


(defn view []
  [:div {:style {:min-height "100vh"
                 :background "#f9fafb"
                 :display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :justify-content "center"}}
   [:div {:style {:max-width "600px"
                  :margin "0 auto"
                  :padding "2rem"
                  :text-align "center"}}
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :align-items "center"
                   :justify-content "center"
                   :margin-bottom "3rem"}}
     [:div {:style {:width "200px"
                    :height "200px"
                    :border-radius "50%"
                    :background "#fff"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :margin-bottom "2rem"
                    :box-shadow "0 10px 25px rgba(0, 0, 0, 0.1)"
                    :border "4px solid #3b82f6"
                    :overflow "hidden"}}
      [:img {:src "/logo/logo-256.webp" 
             :alt "ElevaThor Logo"
             :style {:width "180px"
                     :height "180px"
                     :object-fit "cover"
                     :border-radius "50%"}}]]
     [:h1 {:style {:font-size "3.5rem"
                   :font-weight "700"
                   :color "#1f2937"
                   :margin "0 0 1rem 0"
                   :font-family "'Skranji', cursive"
                   :letter-spacing "0px"}}
      "ElevaThor"]
     [:h2 {:style {:font-size "1.25rem"
                   :color "#6b7280"
                   :font-weight "400"
                   :margin "0"}}
      "Elevator Maintenance Software"]]
    [:p {:style {:font-size "1.125rem"
                 :color "#4b5563"
                 :line-height "1.6"
                 :max-width "500px"
                 :margin "0 auto 2rem auto"}}
     "Streamline your elevator maintenance operations with our comprehensive software solution. Manage worksheets, track materials, coordinate teams, and ensure compliance - all in one integrated platform."]
    [:div {:style {:margin-top "2rem"}}
     [:a {:href "/login"
          :style {:display "inline-block"
                  :padding "12px 24px"
                  :background "#3b82f6"
                  :color "white"
                  :text-decoration "none"
                  :border-radius "8px"
                  :font-weight "600"
                  :font-size "1.125rem"
                  :transition "background-color 0.2s"}}
      "Login to ElevaThor"]]]])