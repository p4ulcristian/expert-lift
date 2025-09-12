(ns ui.page-header)

(defn page-header
  "Page header component with title, description, and optional action button"
  [{:keys [title description action-button]}]
  [:div {:style {:display "flex" :justify-content "space-between" :align-items "center"
                 :margin-bottom "2rem" :padding-bottom "1.5rem" :border-bottom "1px solid #e5e7eb"}}
   [:div
    [:h1 {:style {:font-size "1.875rem" :font-weight "700" :color "#111827" :margin "0"}}
     title]
    (when description
      [:p {:style {:color "#6b7280" :font-size "0.875rem" :margin "0.5rem 0 0 0"}}
       description])]
   (when action-button
     action-button)])

(defn breadcrumb-item [text href active?]
  (if active?
    [:span {:style {:color "#374151" :font-weight "500"}} text]
    [:a {:href href
         :style {:color "#6b7280" :text-decoration "none" :hover {:color "#374151"}}}
     text]))

(defn breadcrumbs
  "Breadcrumb navigation component"
  [items]
  [:nav {:style {:margin-bottom "1rem"}}
   [:ol {:style {:display "flex" :align-items "center" :space-x "0.25rem" :list-style "none" :padding "0" :margin "0"}}
    (for [[index item] (map-indexed vector items)]
      ^{:key index}
      [:li {:style {:display "flex" :align-items "center"}}
       (when (> index 0)
         [:span {:style {:color "#9ca3af" :margin "0 0.5rem"}} "/"]) 
       [breadcrumb-item (:text item) (:href item) (:active? item)]])]]) 