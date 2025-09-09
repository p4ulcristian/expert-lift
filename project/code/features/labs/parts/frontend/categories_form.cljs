(ns features.labs.parts.frontend.categories-form
  (:require
   [app.frontend.request :as request]
   [features.common.storage.frontend.picker :as picker]
   [features.labs.parts.frontend.utils :as utils]
   [ui.button :as button]
   [zero.frontend.re-frame :refer [dispatch]]))

;; Category actions
(defn create-category-query [new-category parent-id]
  `[(categories/create-category! 
     {:name ~(:name new-category)
      :description ~(:description new-category)
      :picture_url ~(:picture new-category)
      :category_id ~parent-id})])

(defn handle-category-response [state response]
  (when-let [new-cat (get-in response [:categories/create-category!])]
    (swap! state update :categories conj new-cat)
    (utils/reset-new-category state)
    (swap! state assoc :editing-category-id nil)
    (dispatch [:modals/close :add-category-modal])))

(defn handle-category-creation [state new-category]
  (let [parent-id (when (seq (:breadcrumbs @state))
                    (last (:breadcrumbs @state)))]
    (request/pathom-with-workspace-id
     {:query (create-category-query new-category parent-id)
      :callback #(handle-category-response state %)})))

(defn handle-category-edit [state editing-id new-category]
  (request/pathom-with-workspace-id
   {:query `[(categories/update-category! 
              {:id ~editing-id
               :name ~(:name new-category)
               :description ~(:description new-category)
               :picture_url ~(:picture new-category)
               :popular ~(:popular new-category)})]
    :callback (fn [response]
                (when-let [updated-category (get-in response [:categories/update-category!])]
                  (swap! state update :categories
                         (fn [categories]
                           (mapv (fn [cat]
                                   (if (= (:id cat) editing-id)
                                     updated-category
                                     cat))
                                 categories)))
                  (utils/reset-new-category state)
                  (swap! state assoc :editing-category-id nil)
                  (dispatch [:modals/close :add-category-modal])))}))

(defn handle-category-delete [state category-id]
  (when (js/confirm "Are you sure you want to delete this category?")
    (request/pathom-with-workspace-id
     {:query `[(categories/delete-category! {:id ~category-id})]
      :callback (fn [response]
                  (when-let [deleted-id (get-in response [:categories/delete-category! :id])]
                    (swap! state update :categories
                           (fn [categories]
                             (filterv #(not= (:id %) deleted-id)
                                     categories)))))})))

;; Form Label Component
(defn form-label [{:keys [text]}]
  [:label {:style {:display "block"
                  :margin-bottom "8px"
                  :color "#333"
                  :font-weight "500"}}
   text])

;; Input Field Components
(defn text-input [{:keys [value on-change placeholder]}]
  [:input {:type "text"
           :value value
           :placeholder placeholder
           :style {:width "100%"
                  :padding "8px"
                  :border "1px solid #ddd"
                  :border-radius "4px"
                  :font-size "14px"}
           :on-change #(on-change (.. ^js % -target -value))}])

(defn textarea-input [{:keys [value on-change]}]
  [:textarea {:value value
              :style {:width "100%"
                     :padding "8px"
                     :border "1px solid #ddd"
                     :border-radius "4px"
                     :font-size "14px"
                     :min-height "100px"
                     :resize "vertical"}
              :on-change #(on-change (.. ^js % -target -value))}])

;; Form Field Components
(defn name-field [{:keys [value on-change]}]
  [:div
   [form-label {:text "Name"}]
   [text-input {:value value
                :on-change on-change
                :placeholder "Category Name"}]])

(defn description-field [{:keys [value on-change]}]
  [:div
   [form-label {:text "Description"}]
   [textarea-input {:value value
                    :on-change on-change}]])

;; Picture Component
(defn picture-preview [{:keys [picture]}]
  (if picture
    [:div {:style {:width "200px"
                  :height "200px"
                  :border-radius "8px"
                  :overflow "hidden"
                  :border "1px solid #ddd"}}
     [:img {:src picture
            :style {:width "100%"
                   :height "100%"
                   :object-fit "cover"}}]]
    [:div {:style {:width "200px"
                  :height "200px"
                  :border-radius "8px"
                  :border "1px solid #ddd"
                  :background-color "#f9f9f9"
                  :display "flex"
                  :align-items "center"
                  :justify-content "center"}}
     [:i {:class "fa-solid fa-image"
          :style {:font-size "32px"
                 :color "#ccc"}}]]))

(defn category-picture [{:keys [picture on-select]}]
  [:div {:style {:flex-shrink 0
                 :display "flex"
                 :flex-direction "column"
                 :gap "12px"}}
   [form-label {:text "Picture"}]
   [picture-preview {:picture picture}]
   [:div {:style {:width "200px"}}
    [picker/view
     {:value picture
      :multiple? false
      :on-select on-select
      :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]]])

;; Form Layout Components
(defn form-fields [{:keys [new-category on-change]}]
  [:div {:style {:flex-grow 1
                 :display "flex"
                 :flex-direction "column"
                 :gap "16px"}}
   [name-field {:value (:name new-category)
                :on-change #(on-change :name %)}]
   [description-field {:value (:description new-category)
                      :on-change #(on-change :description %)}]])

(defn form-buttons [{:keys [on-cancel on-submit submit-text]}]
  [:div {:style {:display "flex"
                 :justify-content "flex-end"
                 :gap "12px"
                 :flex-shrink 0
                 :margin-top "20px"}}
   [button/view {:mode :clear
                 :type :secondary
                 :on-click on-cancel}
    "Cancel"]
   [button/view {:mode :filled
                 :type :primary
                 :on-click on-submit}
    submit-text]])

;; Modal Component
(defn add-category-modal [{:keys [state]}]
  (let [editing-id (:editing-category-id @state)
        new-category (:new-category @state)
        submit-text (if editing-id "Save Changes" "Add Category")]
    [:div {:style {:padding "20px"
                   :min-width "400px"
                   :max-height "80vh"
                   :display "flex"
                   :flex-direction "column"}}
     [:div {:style {:overflow-y "auto"
                    :flex-grow 1
                    :padding-right "10px"}}
      [:div {:style {:display "flex"
                     :gap "24px"
                     :margin-bottom "24px"}}
       [category-picture {:picture (:picture new-category)
                         :on-select #(swap! state assoc-in [:new-category :picture] (:url %))}]
       [form-fields {:new-category new-category
                     :on-change #(swap! state assoc-in [:new-category %1] %2)}]]]
     [form-buttons {:on-cancel #(do
                                (utils/reset-new-category state)
                                (swap! state assoc :editing-category-id nil)
                                (dispatch [:modals/close :add-category-modal]))
                   :on-submit #(if editing-id
                               (handle-category-edit state editing-id new-category)
                               (handle-category-creation state new-category))
                   :submit-text submit-text}]]))

(defn add-category-button [{:keys [state]}]
  [button/view {:mode :filled
                :type :primary
                :style {:width "100%" 
                        :height "60px" 
                        :display "flex"
                        :flex-direction "row" 
                        :align-items "center"
                        :justify-content "center"
                        :gap "10px"} 
                :on-click #(do
                             (utils/reset-new-category state)
                             (swap! state assoc :editing-category-id nil)
                             (dispatch [:modals/add {:id :add-category-modal
                                                   :label "Add New Category"
                                                   :content [add-category-modal {:state state}]
                                                   :open? true}]))}
   [:i {:class "fa-solid fa-folder-plus"
        :style {:font-size "20px"}}]
   "Add Category"])

(defn add-category-header-button [{:keys [state]}]
  [:button {:style {:background "rgba(255, 255, 255, 0.15)"
                    :backdrop-filter "blur(10px)"
                    :border "1px solid rgba(255, 255, 255, 0.3)"
                    :border-radius "8px"
                    :padding "8px 16px"
                    :color "var(--ir-text-primary)"
                    :font-weight "500"
                    :font-size "14px"
                    :cursor "pointer"
                    :transition "all 0.2s ease"
                    :display "flex"
                    :align-items "center"
                    :gap "8px"
                    :box-shadow "0 2px 8px rgba(0, 0, 0, 0.1)"}
            :on-mouse-enter #(do
                              (set! (-> % .-target .-style .-background) "rgba(255, 255, 255, 0.25)")
                              (set! (-> % .-target .-style .-transform) "translateY(-1px)")
                              (set! (-> % .-target .-style .-box-shadow) "0 4px 12px rgba(0, 0, 0, 0.15)"))
            :on-mouse-leave #(do
                              (set! (-> % .-target .-style .-background) "rgba(255, 255, 255, 0.15)")
                              (set! (-> % .-target .-style .-transform) "translateY(0)")
                              (set! (-> % .-target .-style .-box-shadow) "0 2px 8px rgba(0, 0, 0, 0.1)"))
            :on-click #(do
                         (utils/reset-new-category state)
                         (swap! state assoc :editing-category-id nil)
                         (dispatch [:modals/add {:id :add-category-modal
                                               :label "Add New Category"
                                               :content [add-category-modal {:state state}]
                                               :open? true}]))}
   [:i {:class "fa-solid fa-plus"
        :style {:font-size "12px"}}]
   "Add Category"])