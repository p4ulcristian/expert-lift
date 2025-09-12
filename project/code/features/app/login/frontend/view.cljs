(ns features.app.login.frontend.view
  (:require [reagent.core :as r]
            [parquery.frontend.request :as parquery]
            [translations.core :as tr]))

(defn login-form []
  (let [form-data (r/atom {:user/username ""
                           :user/password ""})
        errors (r/atom {})
        loading? (r/atom false)
        
        validate-form (fn [data]
                        (let [username (:user/username data)
                              password (:user/password data)]
                          (cond-> {}
                            (< (count (str username)) 3)
                            (assoc :user/username (tr/tr :login/username-min-length))
                            
                            (< (count (str password)) 1)
                            (assoc :user/password (tr/tr :login/password-required)))))
        
        handle-login (fn []
                       (reset! loading? true)
                       (reset! errors {})
                       (let [validation-errors (validate-form @form-data)]
                         (if (seq validation-errors)
                           (do (reset! errors validation-errors)
                               (reset! loading? false))
                           (parquery/send-queries
                             {:queries {:users/login @form-data}
                              :parquery/context {}
                              :callback (fn [response]
                                          (let [result (:users/login response)]
                                            (reset! loading? false)
                                            (if (:success result)
                                              (cond
                                                (= "superadmin" (:user/role result))
                                                (set! (.-location js/window) "/superadmin")
                                                
                                                (:user/workspace-id result)
                                                (set! (.-location js/window) (str "/app/" (:user/workspace-id result)))
                                                
                                                :else
                                                (set! (.-location js/window) "/app"))
                                              (reset! errors {:general (:error result)}))))}))))]
    
    (fn []
      [:div {:style {:min-height "100vh"
                     :display "flex" 
                     :align-items "center"
                     :justify-content "center"
                     :background "linear-gradient(135deg, #ae6e1f 0%, #72a9bf 50%, #ae6e1f 100%)"}}
       [:div {:style {:background "white"
                      :padding "2rem"
                      :border-radius "8px"
                      :box-shadow "0 4px 6px rgba(0, 0, 0, 0.1)"
                      :width "400px"
                      :max-width "90vw"}}
        [:div {:style {:display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :gap "12px"
                       :margin-bottom "1.5rem"}}
         [:img {:src "/logo/logo-256.webp"
                :alt "Logo"
                :style {:height "50px"
                        :width "50px"
                        :border-radius "12px"
                        :object-fit "cover"}}]
         [:h2 {:style {:margin "0"
                       :color "#ae6e1f"
                       :font-family "'Skranji', cursive"
                       :font-size "1.8rem"
                       :font-weight "700"}}
          "ElevaThor"]]
        [:p {:style {:text-align "center"
                     :margin-bottom "1.5rem"
                     :color "#666"
                     :font-size "1rem"}}
         (tr/tr :login/title)]
        
        (when (:general @errors)
          [:div {:style {:background "#f8d7da"
                         :border "1px solid #f5c6cb"
                         :color "#721c24"
                         :padding "0.75rem"
                         :border-radius "4px"
                         :margin-bottom "1rem"}}
           (:general @errors)])
        
        [:form {:on-submit (fn [e]
                             (.preventDefault e)
                             (handle-login))}
         [:div {:style {:margin-bottom "1rem"}}
          [:label {:style {:display "block"
                           :margin-bottom "0.5rem"
                           :font-weight "bold"
                           :color (if (:user/username @errors) "#dc3545" "inherit")}}
           (tr/tr :login/username)]
          [:input {:type "text"
                   :value (:user/username @form-data)
                   :on-change #(swap! form-data assoc :user/username (.. % -target -value))
                   :placeholder (tr/tr :login/username-placeholder)
                   :style {:width "100%"
                           :padding "0.75rem"
                           :border (str "1px solid " (if (:user/username @errors) "#dc3545" "#ccc"))
                           :border-radius "4px"
                           :font-size "1rem"}}]
          (when (:user/username @errors)
            [:div {:style {:color "#dc3545"
                           :font-size "0.875rem"
                           :margin-top "0.25rem"}}
             (:user/username @errors)])]
         
         [:div {:style {:margin-bottom "1.5rem"}}
          [:label {:style {:display "block"
                           :margin-bottom "0.5rem"
                           :font-weight "bold"
                           :color (if (:user/password @errors) "#dc3545" "inherit")}}
           (tr/tr :login/password)]
          [:input {:type "password"
                   :value (:user/password @form-data)
                   :on-change #(swap! form-data assoc :user/password (.. % -target -value))
                   :placeholder (tr/tr :login/password-placeholder)
                   :style {:width "100%"
                           :padding "0.75rem"
                           :border (str "1px solid " (if (:user/password @errors) "#dc3545" "#ccc"))
                           :border-radius "4px"
                           :font-size "1rem"}}]
          (when (:user/password @errors)
            [:div {:style {:color "#dc3545"
                           :font-size "0.875rem"
                           :margin-top "0.25rem"}}
             (:user/password @errors)])]
         
         [:button {:type "submit"
                   :disabled @loading?
                   :style {:width "100%"
                           :padding "0.75rem"
                           :background (if @loading? "#6c757d" "#72a9bf")
                           :color "white"
                           :border "none"
                           :border-radius "4px"
                           :font-size "1rem"
                           :cursor (if @loading? "not-allowed" "pointer")
                           :opacity (if @loading? 0.6 1)}}
          (if @loading? (tr/tr :login/logging-in) (tr/tr :login/login))]]]])))

(defn view []
  [login-form])