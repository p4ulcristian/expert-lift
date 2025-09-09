(ns features.flex.shared.frontend.components.body
  (:require [features.flex.shared.frontend.components.flex-title :as flex-title]))

(defn view
  "Shared page layout with optional info tooltip
   
   Parameters:
   - :title - Page title (e.g. 'Orders', 'New Process')
   - :title-buttons - Components to show on the right side of title
   - :description - Description for info tooltip (shows icon only if provided)
   - :body - Main content component to render"
  [{:keys [title title-buttons body description]}]
  [:div
   [flex-title/view title
    {:description description
     :right-content [:div {:style {:display "flex" :gap "12px"}}
                     title-buttons]}]
   body])