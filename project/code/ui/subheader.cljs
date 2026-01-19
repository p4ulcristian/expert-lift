(ns ui.subheader)

(defn subheader
  "Responsive subheader component that sits below the main header.
   Shows page title, optional description, and optional action button.
   Full-width with centered content matching content-section max-width."
  [{:keys [title description action-button]}]
  [:div {:style {:background "#ffffff"
                 :border-bottom "1px solid #e5e7eb"
                 :padding "1rem 1rem"}}
   [:div {:style {:max-width "800px"
                  :margin "0 auto"
                  :display "flex"
                  :justify-content "space-between"
                  :align-items "center"
                  :flex-wrap "wrap"
                  :gap "1rem"}}
    [:div {:style {:min-width "0"}}
     [:h1 {:style {:font-size "1.5rem"
                   :font-weight "700"
                   :color "#111827"
                   :margin "0"
                   :white-space "nowrap"
                   :overflow "hidden"
                   :text-overflow "ellipsis"}}
      title]
     (when description
       [:p {:style {:color "#6b7280"
                    :font-size "0.875rem"
                    :margin "0.25rem 0 0 0"
                    :white-space "nowrap"
                    :overflow "hidden"
                    :text-overflow "ellipsis"}}
        description])]
    (when action-button
      [:div {:style {:flex-shrink "0"}}
       action-button])]])
