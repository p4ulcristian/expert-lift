(ns features.app.homepage.frontend.view
  (:require
   [ui.link :as link]))


(defn view []
  [:div {:style {:min-height "100vh"
                 :background "linear-gradient(135deg, #2c3e50 0%, #34495e 50%, #3d566e 100%)"
                 :display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :justify-content "center"}}
   [:div {:style {:max-width "800px"
                  :margin "0 auto"
                  :padding "2rem"
                  :text-align "center"}}
    [:h1 {:style {:font-size "3rem"
                  :font-weight "bold"
                  :color "#fff"
                  :margin-bottom "2rem"}}
     "🏢 Expert Lift"]
    [:p {:style {:font-size "1.5rem"
                 :color "#ecf0f1"
                 :margin-bottom "3rem"
                 :line-height "1.4"}}
     "Professional elevator services for modern buildings"]
    [:div {:style {:background "rgba(255, 255, 255, 0.1)"
                   :border-radius "12px"
                   :padding "2rem"
                   :margin-bottom "2rem"}}
     [:h2 {:style {:font-size "1.8rem"
                   :color "#fff"
                   :margin-bottom "1.5rem"}}
      "What We Do"]
     [:div {:style {:display "grid"
                    :grid-template-columns "repeat(auto-fit, minmax(250px, 1fr))"
                    :gap "1.5rem"
                    :margin-top "1rem"}}
      [:div {:style {:text-align "left"}}
       [:h3 {:style {:color "#3498db"
                     :margin-bottom "0.5rem"
                     :font-size "1.1rem"}}
        "🔧 Installation"]
       [:p {:style {:color "#bdc3c7"
                    :font-size "0.95rem"
                    :line-height "1.5"}}
        "Expert installation of passenger and freight elevators"]]
      [:div {:style {:text-align "left"}}
       [:h3 {:style {:color "#3498db"
                     :margin-bottom "0.5rem"
                     :font-size "1.1rem"}}
        "🛠️ Maintenance"]
       [:p {:style {:color "#bdc3c7"
                    :font-size "0.95rem"
                    :line-height "1.5"}}
        "Regular maintenance and safety inspections"]]
      [:div {:style {:text-align "left"}}
       [:h3 {:style {:color "#3498db"
                     :margin-bottom "0.5rem"
                     :font-size "1.1rem"}}
        "⚡ Emergency Service"]
       [:p {:style {:color "#bdc3c7"
                    :font-size "0.95rem"
                    :line-height "1.5"}}
        "24/7 emergency repairs and technical support"]]]]
    [:div {:style {:color "#95a5a6"
                   :font-size "1rem"
                   :margin-top "2rem"}}
     "Serving commercial and residential buildings since 1985"]]])