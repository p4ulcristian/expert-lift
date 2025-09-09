(ns features.labs.parts.frontend.package-card
  (:require
   [app.frontend.request :as request]
   [features.labs.parts.frontend.package-form :as package-form]
   [ui.button :as button]))

(defn delete-package [{:keys [state package-id]}]
  (when (js/confirm "Are you sure you want to delete this package? This will also delete all parts in this package.")
    (request/pathom-with-workspace-id
     {:query `[(packages/delete-package! {:id ~package-id})]
      :callback (fn [response]
                  (when (get-in response [:packages/delete-package!])
                    (swap! state update :packages 
                           (fn [packages] (filter #(not= (:id %) package-id) packages)))))})))

(defn package-icon []
  [:i {:class "fa-solid fa-box"
       :style {:color "#4CAF50"
               :font-size "24px"}}])

(defn view [{:keys [state package]}]
  [:div {:style {:background "#ffffff"
                 :border "1px solid #ddd"
                 :border-radius "8px"
                 :padding "16px"
                 :cursor "pointer"
                 :position "relative"}
         :on-click #(swap! state update :package-navigation-stack conj (:id package))}
   
   ;; Package Icon
   [:div {:style {:display "flex"
                  :justify-content "center"
                  :margin-bottom "16px"}}
    [package-icon]]
   
   ;; Package Image
   (when (:picture_url package)
     [:div {:style {:display "flex"
                    :justify-content "center"
                    :margin-bottom "16px"}}
      [:img {:src (:picture_url package)
             :alt (:name package)
             :style {:max-width "120px"
                     :max-height "80px"
                     :object-fit "cover"}}]])
   
   ;; Package Info
   [:div {:style {:text-align "center"}}
    [:h3 {:style {:margin "0 0 8px 0"
                  :color "#333"
                  :font-size "18px"
                  :font-weight "600"}}
     (:name package)]
    
    (when (:description package)
      [:p {:style {:margin "0 0 12px 0"
                   :color "#666"
                   :font-size "14px"
                   :line-height "1.4"
                   :overflow "hidden"
                   :display "-webkit-box"
                   :-webkit-line-clamp "3"
                   :-webkit-box-orient "vertical"}}
       (:description package)])
    
    (when (:prefix package)
      [:div {:style {:background "#f5f5f5"
                     :padding "4px 8px"
                     :margin "8px 0"
                     :display "inline-block"}}
       [:small {:style {:color "#666"
                        :font-family "monospace"}}
        (:prefix package)]])
    
    (when (:popular package)
      [:div {:style {:background "#FFD700"
                     :color "#B8860B"
                     :padding "2px 8px"
                     :font-size "12px"
                     :font-weight "600"
                     :margin "8px 0"
                     :display "inline-block"}}
       "POPULAR"])
    
    ;; Action Buttons
    [:div {:style {:display "flex"
                   :gap "8px"
                   :justify-content "center"
                   :margin-top "16px"}
           :on-click #(.stopPropagation ^js %)}
     [button/view {:on-click #(do (.stopPropagation ^js %)
                                  (package-form/open-edit-package-modal {:state state :package package}))
                   :type :secondary
                   :size :small
                   :style {:padding "4px 8px"
                           :font-size "12px"}}
      [:i {:class "fa-solid fa-edit"}]]
     
     [button/view {:on-click #(do (.stopPropagation ^js %)
                                  (delete-package {:state state :package-id (:id package)}))
                   :type :danger
                   :size :small
                   :style {:padding "4px 8px"
                           :font-size "12px"}}
      [:i {:class "fa-solid fa-trash"}]]]]])