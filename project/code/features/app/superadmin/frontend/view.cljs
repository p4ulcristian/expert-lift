(ns features.app.superadmin.frontend.view)

(defn view []
  [:div {:style {:min-height "100vh"
                 :background "linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)"
                 :display "flex"
                 :flex-direction "column"
                 :align-items "center"
                 :justify-content "center"
                 :padding "2rem"}}
   [:div {:style {:max-width "800px"
                  :margin "0 auto"
                  :text-align "center"
                  :background "rgba(255, 255, 255, 0.05)"
                  :border-radius "16px"
                  :padding "3rem"
                  :backdrop-filter "blur(10px)"
                  :border "1px solid rgba(255, 255, 255, 0.1)"}}
    [:h1 {:style {:font-size "3rem"
                  :font-weight "700"
                  :color "#fff"
                  :margin-bottom "1rem"
                  :text-shadow "0 2px 20px rgba(0,0,0,0.3)"}}
     "🔧 Super Admin Panel"]
    [:p {:style {:font-size "1.2rem"
                 :color "rgba(255, 255, 255, 0.8)"
                 :margin-bottom "3rem"
                 :line-height "1.6"}}
     "Expert Lift System Administration"]
    
    [:div {:style {:display "grid"
                   :grid-template-columns "repeat(auto-fit, minmax(300px, 1fr))"
                   :gap "2rem"
                   :margin-top "2rem"}}
     [:div {:style {:background "rgba(34, 197, 94, 0.1)"
                    :border "1px solid rgba(34, 197, 94, 0.3)"
                    :border-radius "12px"
                    :padding "2rem"
                    :transition "all 0.3s ease"
                    :cursor "pointer"}
            :on-mouse-enter #(do
                              (set! (.-style.background (.-target %)) "rgba(34, 197, 94, 0.15)")
                              (set! (.-style.transform (.-target %)) "translateY(-4px)")
                              (set! (.-style.boxShadow (.-target %)) "0 8px 25px rgba(34, 197, 94, 0.2)"))
            :on-mouse-leave #(do
                              (set! (.-style.background (.-target %)) "rgba(34, 197, 94, 0.1)")
                              (set! (.-style.transform (.-target %)) "translateY(0)")
                              (set! (.-style.boxShadow (.-target %)) "none"))}
      [:div {:style {:font-size "2.5rem"
                     :margin-bottom "1rem"}} "👥"]
      [:h3 {:style {:color "#22c55e"
                    :font-size "1.3rem"
                    :margin-bottom "0.5rem"}}
       "User Management"]
      [:p {:style {:color "rgba(255, 255, 255, 0.7)"
                   :font-size "0.95rem"
                   :line-height "1.5"}}
       "Manage employees, admins, and permissions"]]
     
     [:div {:style {:background "rgba(59, 130, 246, 0.1)"
                    :border "1px solid rgba(59, 130, 246, 0.3)"
                    :border-radius "12px"
                    :padding "2rem"
                    :transition "all 0.3s ease"
                    :cursor "pointer"}
            :on-mouse-enter #(do
                              (set! (.-style.background (.-target %)) "rgba(59, 130, 246, 0.15)")
                              (set! (.-style.transform (.-target %)) "translateY(-4px)")
                              (set! (.-style.boxShadow (.-target %)) "0 8px 25px rgba(59, 130, 246, 0.2)"))
            :on-mouse-leave #(do
                              (set! (.-style.background (.-target %)) "rgba(59, 130, 246, 0.1)")
                              (set! (.-style.transform (.-target %)) "translateY(0)")
                              (set! (.-style.boxShadow (.-target %)) "none"))}
      [:div {:style {:font-size "2.5rem"
                     :margin-bottom "1rem"}} "⚙️"]
      [:h3 {:style {:color "#3b82f6"
                    :font-size "1.3rem"
                    :margin-bottom "0.5rem"}}
       "System Settings"]
      [:p {:style {:color "rgba(255, 255, 255, 0.7)"
                   :font-size "0.95rem"
                   :line-height "1.5"}}
       "Configure logos, templates, and system preferences"]]
     
     [:div {:style {:background "rgba(168, 85, 247, 0.1)"
                    :border "1px solid rgba(168, 85, 247, 0.3)"
                    :border-radius "12px"
                    :padding "2rem"
                    :transition "all 0.3s ease"
                    :cursor "pointer"}
            :on-mouse-enter #(do
                              (set! (.-style.background (.-target %)) "rgba(168, 85, 247, 0.15)")
                              (set! (.-style.transform (.-target %)) "translateY(-4px)")
                              (set! (.-style.boxShadow (.-target %)) "0 8px 25px rgba(168, 85, 247, 0.2)"))
            :on-mouse-leave #(do
                              (set! (.-style.background (.-target %)) "rgba(168, 85, 247, 0.1)")
                              (set! (.-style.transform (.-target %)) "translateY(0)")
                              (set! (.-style.boxShadow (.-target %)) "none"))}
      [:div {:style {:font-size "2.5rem"
                     :margin-bottom "1rem"}} "📊"]
      [:h3 {:style {:color "#a855f7"
                    :font-size "1.3rem"
                    :margin-bottom "0.5rem"}}
       "System Analytics"]
      [:p {:style {:color "rgba(255, 255, 255, 0.7)"
                   :font-size "0.95rem"
                   :line-height "1.5"}}
       "View usage statistics and system reports"]]
     
     [:div {:style {:background "rgba(245, 158, 11, 0.1)"
                    :border "1px solid rgba(245, 158, 11, 0.3)"
                    :border-radius "12px"
                    :padding "2rem"
                    :transition "all 0.3s ease"
                    :cursor "pointer"}
            :on-mouse-enter #(do
                              (set! (.-style.background (.-target %)) "rgba(245, 158, 11, 0.15)")
                              (set! (.-style.transform (.-target %)) "translateY(-4px)")
                              (set! (.-style.boxShadow (.-target %)) "0 8px 25px rgba(245, 158, 11, 0.2)"))
            :on-mouse-leave #(do
                              (set! (.-style.background (.-target %)) "rgba(245, 158, 11, 0.1)")
                              (set! (.-style.transform (.-target %)) "translateY(0)")
                              (set! (.-style.boxShadow (.-target %)) "none"))}
      [:div {:style {:font-size "2.5rem"
                     :margin-bottom "1rem"}} "🗄️"]
      [:h3 {:style {:color "#f59e0b"
                    :font-size "1.3rem"
                    :margin-bottom "0.5rem"}}
       "Data Management"]
      [:p {:style {:color "rgba(255, 255, 255, 0.7)"
                   :font-size "0.95rem"
                   :line-height "1.5"}}
       "Backup, export, and maintain system data"]]]
    
    [:div {:style {:margin-top "3rem"
                   :padding-top "2rem"
                   :border-top "1px solid rgba(255, 255, 255, 0.1)"}}
     [:p {:style {:color "rgba(255, 255, 255, 0.5)"
                  :font-size "0.9rem"}}
      "Expert Lift Management System v1.0"]]]])