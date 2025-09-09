(ns features.labs.parts.frontend.category-card
  (:require
   [features.labs.parts.frontend.categories-form :as categories-form]
   [ui.card :as card]
   [zero.frontend.re-frame :refer [dispatch]]))

(defn edit-button [{:keys [state category]}]
  [:button {:style {:position "absolute"
                    :top "12px"
                    :right "48px"
                    :background "rgba(255, 255, 255, 0.9)"
                    :border "1px solid #e0e0e0"
                    :border-radius "8px"
                    :width "32px"
                    :height "32px"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :cursor "pointer"
                    :transition "all 0.2s ease"
                    :box-shadow "0 2px 4px rgba(0, 0, 0, 0.1)"}
            :on-mouse-enter #(set! (-> % .-target .-style .-background) "#f8f9fa")
            :on-mouse-leave #(set! (-> % .-target .-style .-background) "rgba(255, 255, 255, 0.9)")
            :on-click (fn [e]
                       (.stopPropagation ^js e)
                       (swap! state assoc 
                              :editing-category-id (:id category)
                              :new-category {:name (:name category)
                                           :description (:description category)
                                           :picture (:picture_url category)})
                       (dispatch [:modals/add {:id :add-category-modal
                                             :label "Edit Category"
                                             :content [categories-form/add-category-modal {:state state}]
                                             :open? true}]))}
   [:i {:class "fa-solid fa-pen"
        :style {:font-size "12px"
                :color "#666"}}]])

(defn delete-button [{:keys [state category-id]}]
  [:button {:style {:position "absolute"
                    :top "12px"
                    :right "12px"
                    :background "rgba(255, 255, 255, 0.9)"
                    :border "1px solid #e0e0e0"
                    :border-radius "8px"
                    :width "32px"
                    :height "32px"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :cursor "pointer"
                    :transition "all 0.2s ease"
                    :box-shadow "0 2px 4px rgba(0, 0, 0, 0.1)"}
            :on-mouse-enter #(do
                              (set! (-> % .-target .-style .-background) "#fee")
                              (set! (-> % .-target .-style .-border-color) "#fcc"))
            :on-mouse-leave #(do
                              (set! (-> % .-target .-style .-background) "rgba(255, 255, 255, 0.9)")
                              (set! (-> % .-target .-style .-border-color) "#e0e0e0"))
            :on-click (fn [e]
                       (.stopPropagation ^js e)
                       (categories-form/handle-category-delete state category-id))}
   [:i {:class "fa-solid fa-trash"
        :style {:font-size "12px"
                :color "#dc3545"}}]])

(defn category-name [name]
  [:h3 {:style {:margin "0 0 8px 0"
                :font-size "18px"
                :font-weight "600"
                :color "#1a1a1a"
                :line-height "1.3"}}
   name])

(defn category-description [description]
  [:p {:style {:margin "0"
               :font-size "14px"
               :color "#6b7280"
               :line-height "1.5"
               :display "-webkit-box"
               :WebkitLineClamp "2"
               :WebkitBoxOrient "vertical"
               :overflow "hidden"}}
   (or description "No description available.")])

(defn category-picture [picture-url]
  (if picture-url
    [:div {:style {:width "100px"
                   :height "100px"
                   :border-radius "12px"
                   :overflow "hidden"
                   :box-shadow "0 4px 12px rgba(0, 0, 0, 0.1)"
                   :flex-shrink "0"}}
     [:img {:src picture-url
            :style {:width "100%"
                   :height "100%"
                   :object-fit "cover"}}]]
    [:div {:style {:width "100px"
                   :height "100px"
                   :background "linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)"
                   :border-radius "12px"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :border "2px dashed #cbd5e1"
                   :flex-shrink "0"}}
     [:i {:class "fa-solid fa-image"
          :style {:font-size "28px"
                 :color "#94a3b8"}}]]))

(defn view [{:keys [state category]}]
  [card/view
   {:on-click (fn []
                (swap! state update :breadcrumbs conj (:id category))
                ;; Load packages for this category if load-packages-fn is provided
                (when-let [load-packages-fn (:load-packages-fn @state)]
                  (load-packages-fn (:id category))))
    :style {:background "#ffffff"
            :border "1px solid #e5e7eb"
            :border-radius "16px"
            :transition "all 0.3s ease"
            :cursor "pointer"
            :overflow "hidden"}
    :content
    [:div {:style {:padding "20px"
                   :position "relative"
                   :display "flex"
                   :gap "20px"
                   :align-items "flex-start"}}
     [edit-button {:state state :category category}]
     [delete-button {:state state
                     :category-id (:id category)}]
     [category-picture (:picture_url category)]
     [:div {:style {:flex "1"
                    :min-width "0"}}
      [category-name (:name category)]
      [category-description (:description category)]
      [:div {:style {:margin-top "12px"
                     :padding "6px 12px"
                     :background "#f1f5f9"
                     :border-radius "20px"
                     :display "inline-flex"
                     :align-items "center"
                     :gap "6px"
                     :font-size "12px"
                     :color "#475569"
                     :font-weight "500"}}
       [:i {:class "fa-solid fa-folder"
            :style {:font-size "10px"}}]
       "Category"]]]}])