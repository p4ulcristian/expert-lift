(ns features.labs.parts.frontend.package-form
  (:require
   ["react" :as react]
   [app.frontend.request :as request]
   [features.common.storage.frontend.picker :as picker]
   [features.labs.parts.frontend.glb-viewer :refer [glb-viewer]]
   [features.common.form.frontend.builder :as form-builder]
   [ui.button :as button]
   [ui.notification :as notification]
   [zero.frontend.re-frame :refer [dispatch]]))

;; ===== Reusable Form Components =====

(defn text-field [{:keys [label value on-change placeholder]}]
  [:div
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :color "#333"
                    :font-weight "500"}}
    label]
   [:input {:type "text"
            :value value
            :placeholder placeholder
            :style {:width "100%"
                    :padding "8px"
                    :border "1px solid #ddd"
                    :border-radius "4px"
                    :font-size "14px"}
            :on-change #(on-change (.. ^js % -target -value))}]])

(defn textarea-field [{:keys [label value on-change placeholder]}]
  [:div
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :color "#333"
                    :font-weight "500"}}
    label]
   [:textarea {:value value
               :placeholder placeholder
               :style {:width "100%"
                       :padding "8px"
                       :border "1px solid #ddd"
                       :border-radius "4px"
                       :font-size "14px"
                       :min-height "80px"}
               :on-change #(on-change (.. ^js % -target -value))}]])

(defn form-selector [{:keys [selected-form-id forms on-change on-create-form]}]
  [:div
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :color "#333"
                    :font-weight "500"}}
    "Form"]
   [:select {:value (or selected-form-id "")
             :style {:width "100%"
                     :padding "8px"
                     :border "1px solid #ddd"
                     :border-radius "4px"
                     :font-size "14px"
                     :margin-bottom "8px"}
             :on-change #(on-change (.. ^js % -target -value))}
    [:option {:value ""} "No form"]
    (for [form forms]
      ^{:key (:id form)}
      [:option {:value (:id form)} (:title form)])]
   [button/view {:type :secondary
                 :size :small
                 :on-click on-create-form
                 :style {:width "100%"
                         :margin-bottom "8px"}}
    [:i {:class "fa-solid fa-plus"
         :style {:margin-right "6px"}}]
    "Create New Form"]])

;; ===== Form Save Helper Functions =====

(defn reset-form-builder-ui [set-creating-form! set-new-form-data]
  (set-creating-form! false)
  (set-new-form-data {:title "New Form" :template [] :price_formula ""})
  (notification/toast! :success "Form created successfully!"))

(defn reload-and-select-form [state form-id set-editing-package editing-package set-creating-form! set-new-form-data]
  (request/pathom
    {:query '[:forms/get-forms]
     :callback (fn [forms-response]
                 (let [forms-list (get-in forms-response [:forms/get-forms])]
                   (println "Setting form_id2 to:" form-id)
                   (swap! state assoc :forms forms-list)
                   (set-editing-package (assoc editing-package :form_id form-id))
                   (reset-form-builder-ui set-creating-form! set-new-form-data)))}))

(defn handle-form-save-success [response state created-form set-editing-package editing-package set-creating-form! set-new-form-data]
  (when created-form
    (println "Created form:" created-form)
    (reload-and-select-form state created-form set-editing-package editing-package set-creating-form! set-new-form-data)))

(defn reload-and-select-form-for-add [state created-form set-creating-form! set-new-form-data]
  (request/pathom
    {:query '[:forms/get-forms]
     :callback (fn [forms-response]
                 (let [forms-list (get-in forms-response [:forms/get-forms])
                       form-id (str (:id created-form))]
                   (println "Setting form_id 1 to:" form-id)
                   (swap! state assoc :forms forms-list)
                   (swap! state assoc-in [:new-package :form_id] form-id)
                   (reset-form-builder-ui set-creating-form! set-new-form-data)))}))

(defn handle-form-save-success-for-add [response state created-form set-creating-form! set-new-form-data]
  (when created-form
    (println "Created form:" created-form)
    (reload-and-select-form-for-add state created-form set-creating-form! set-new-form-data)))

(defn save-new-form-for-add [new-form-data state set-creating-form! set-new-form-data]
  (request/pathom
    {:query `[(forms/save-form-data! {:form-data ~new-form-data})]
     :callback (fn [response]
                 (println "Save form response:" response)
                 (let [created-form (get-in response [:forms/save-form-data!])]
                   (handle-form-save-success-for-add response state created-form set-creating-form! set-new-form-data)))}))

(defn save-new-form [new-form-data state set-editing-package editing-package set-creating-form! set-new-form-data]
  (request/pathom
    {:query `[(forms/save-form-data! {:form-data ~new-form-data})]
     :callback (fn [response]
                 (println "Save form response:" response)
                 (let [created-form (get-in response [:forms/save-form-data!])]
                   (handle-form-save-success response state created-form set-editing-package editing-package set-creating-form! set-new-form-data)))}))

;; ===== Package Form Functions =====

(defn create-package-query [new-package category-id]
  `[{(packages/create-package! {:name        ~(:name new-package)
                                :description ~(:description new-package)
                                :picture_url ~(:picture_url new-package)
                                :prefix      ~(:prefix new-package)
                                :category_id ~category-id
                                :model_url   ~(:model_url new-package)
                                :popular     ~(:popular new-package)
                                :form_id     ~(:form_id new-package)})
     [:id :name :description :picture_url :prefix :category_id :model_url :popular :form_id :created_at :updated_at]}])

(defn create-package [{:keys [state]}]
  (let [new-package (:new-package @state)
        current-category-id (when (seq (:breadcrumbs @state))
                             (last (:breadcrumbs @state)))]
    (when (not-empty (:name new-package))
      (request/pathom
       {:query    (create-package-query new-package current-category-id)
        :callback (fn [response]
                    (when-let [created-package (get-in response [:packages/create-package!])]
                      (swap! state update :packages conj created-package)
                      (swap! state assoc :new-package {})
                      (dispatch [:modals/close :add-package-modal])
                      ;; Reload packages for current category
                      (when-let [load-fn (:load-packages-fn @state)]
                        (load-fn current-category-id))))}))))

(defn update-package-query [package]
  `[{(packages/update-package! ~package)
     [:id :name :description :picture_url :prefix :category_id :model_url :popular :form_id :created_at :updated_at]}])

(defn update-package [{:keys [state package]}]
  (request/pathom
   {:query    (update-package-query package)
    :callback (fn [response]
                (when-let [updated-package (get-in response [:packages/update-package!])]
                  (swap! state update :packages
                         (fn [packages]
                           (mapv #(if (= (:id %) (:id updated-package))
                                   updated-package
                                   %) packages)))
                  (swap! state assoc :editing-package-id nil)
                  (dispatch [:modals/close :edit-package-modal])
                  ;; Reload packages for current category
                  (when-let [load-fn (:load-packages-fn @state)]
                    (let [current-category-id (last (:breadcrumbs @state))]
                      (load-fn current-category-id)))))}))

;; ===== Package Form UI =====

(defn add-package-modal [{:keys [state]}]
  (let [new-package (:new-package @state)
        forms (:forms @state)
        [creating-form? set-creating-form!] (react/useState false)
        [new-form-data set-new-form-data] (react/useState {:title "New Form" :template [] :price_formula ""})]
    [:div {:style {:padding "20px"
                   :width "100%"
                   :min-width "500px"
                   :max-height "85vh"
                   :height "auto"
                   :display "flex"
                   :flex-direction "column"
                   :overflow "hidden"}}
     [:div {:style {:overflow-y "auto"
                    :flex "1 1 auto"
                    :padding-right "10px"
                    :margin-right "-10px"}}
      ;; Name field
      [text-field {:label "Name"
                   :value (:name new-package)
                   :placeholder "Package name"
                   :on-change #(swap! state assoc-in [:new-package :name] %)}]
      
      ;; Description field
      [textarea-field {:label "Description"
                       :value (:description new-package)
                       :placeholder "Package description"
                       :on-change #(swap! state assoc-in [:new-package :description] %)}]
      
      ;; Prefix field
      [text-field {:label "Prefix"
                   :value (:prefix new-package)
                   :placeholder "Package prefix (e.g., PKG-)"
                   :on-change #(swap! state assoc-in [:new-package :prefix] %)}]
      
      ;; Picture field with storage picker
      [:div {:style {:margin "16px 0"}}
       [:label {:style {:display "block"
                        :margin-bottom "8px"
                        :color "#333"
                        :font-weight "500"}}
        "Picture"]
       (if (:picture_url new-package)
         [:div {:style {:width "200px"
                        :height "200px"
                        :border-radius "8px"
                        :overflow "hidden"
                        :border "1px solid #ddd"
                        :margin-bottom "12px"}}
          [:img {:src (:picture_url new-package)
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
                        :justify-content "center"
                        :margin-bottom "12px"}}
          [:i {:class "fa-solid fa-image"
               :style {:font-size "32px"
                       :color "#ccc"}}]])
       [picker/view
        {:value (:picture_url new-package)
         :multiple? false
         :on-select #(swap! state assoc-in [:new-package :picture_url] (:url %))
         :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]]
      
      ;; 3D Model field with storage picker
      [:div {:style {:margin "16px 0"}}
       [:label {:style {:display "block"
                        :margin-bottom "8px"
                        :color "#333"
                        :font-weight "500"}}
        "3D Model"]
       (if (:model_url new-package)
         [glb-viewer {:url (:model_url new-package) :width 200 :height 200}]
         [:div {:style {:width "200px"
                        :height "200px"
                        :border-radius "8px"
                        :border "1px solid #ddd"
                        :background-color "#f9f9f9"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"
                        :margin-bottom "12px"}}
          [:i {:class "fa-solid fa-cube"
               :style {:font-size "32px"
                       :color "#ccc"}}]])
       [picker/view
        {:value (:model_url new-package)
         :multiple? false
         :on-select #(swap! state assoc-in [:new-package :model_url] (:url %))
         :extensions ["model/gltf-binary"]}]]
      
      ;; Form selector
      [form-selector {:selected-form-id (:form_id new-package)
                      :forms forms
                      :on-change #(swap! state assoc-in [:new-package :form_id] %)
                      :on-create-form #(set-creating-form! true)}]
      
      ;; Inline form builder
      (when creating-form?
        [:div {:style {:margin "16px 0"
                       :padding "16px"
                       :border "2px solid #007bff"
                       :border-radius "8px"
                       :background "#f8f9fa"}}
         [:div {:style {:display "flex"
                        :justify-content "space-between"
                        :align-items "center"
                        :margin-bottom "16px"}}
          [:h4 {:style {:margin "0"
                        :color "#007bff"
                        :font-size "16px"}}
           "Create New Form"]
          [button/view {:type :secondary
                        :size :small
                        :on-click #(set-creating-form! false)}
           [:i {:class "fa-solid fa-times"}]]]
         
         [text-field {:label "Form Title"
                      :value (:title new-form-data)
                      :placeholder "Enter form title..."
                      :on-change #(set-new-form-data (assoc new-form-data :title %))}]
         
         [form-builder/view (:template new-form-data)
                           #(set-new-form-data (assoc new-form-data :template %))]
         
         [textarea-field {:label "Price Formula"
                          :value (:price_formula new-form-data)
                          :placeholder "[:quantity] * [:price]"
                          :on-change #(set-new-form-data (assoc new-form-data :price_formula %))}]
         
         [:div {:style {:display "flex"
                        :gap "8px"
                        :justify-content "flex-end"
                        :margin-top "16px"}}
          [button/view {:type :secondary
                        :on-click #(do (set-creating-form! false)
                                      (set-new-form-data {:title "New Form" :template [] :price_formula ""}))}
           "Cancel"]
          [button/view {:type :primary
                        :on-click #(save-new-form-for-add new-form-data state set-creating-form! set-new-form-data)}
           "Save Form"]]])
      
      ;; Popular checkbox
      [:div {:style {:margin "16px 0"}}
       [:label {:style {:display "flex"
                        :align-items "center"
                        :gap "8px"
                        :color "#333"
                        :font-weight "500"}}
        [:input {:type "checkbox"
                 :checked (boolean (:popular new-package))
                 :on-change #(swap! state assoc-in [:new-package :popular] (.. % -target -checked))}]
        "Popular Package"]]]
     
     ;; Modal buttons
     [:div {:style {:display "flex"
                    :justify-content "flex-end"
                    :gap "12px"
                    :flex-shrink 0
                    :margin-top "20px"
                    :padding-top "16px"
                    :border-top "1px solid #e0e0e0"
                    :background "white"}}
      [button/view {:type :secondary
                    :on-click #(do
                               (swap! state assoc :new-package {})
                               (dispatch [:modals/close :add-package-modal]))}
       "Cancel"]
      [button/view {:type :primary
                    :on-click #(create-package {:state state})}
       "Create Package"]]]))

(defn package-form [{:keys [state]}]
  (let [new-package (:new-package @state)
        forms (:forms @state)]
    [:div {:style {:background "white"
                   :border-radius "8px"
                   :padding "20px"
                   :margin "20px 0"
                   :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}}
     [:h3 {:style {:margin "0 0 20px 0"
                   :color "#333"
                   :font-size "18px"
                   :font-weight "600"}}
      "Add New Package"]
     
     [text-field {:label "Name"
                  :value (:name new-package)
                  :placeholder "Package name"
                  :on-change #(swap! state assoc-in [:new-package :name] %)}]
     
     [textarea-field {:label "Description"
                      :value (:description new-package)
                      :placeholder "Package description"
                      :on-change #(swap! state assoc-in [:new-package :description] %)}]
     
     [text-field {:label "Prefix"
                  :value (:prefix new-package)
                  :placeholder "Package prefix (e.g., PKG-)"
                  :on-change #(swap! state assoc-in [:new-package :prefix] %)}]
     
     ;; Picture field with storage picker
     [:div {:style {:margin "16px 0"}}
      [:label {:style {:display "block"
                       :margin-bottom "8px"
                       :color "#333"
                       :font-weight "500"}}
       "Picture"]
      (if (:picture_url new-package)
        [:div {:style {:width "200px"
                       :height "200px"
                       :border-radius "8px"
                       :overflow "hidden"
                       :border "1px solid #ddd"
                       :margin-bottom "12px"}}
         [:img {:src (:picture_url new-package)
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
                       :justify-content "center"
                       :margin-bottom "12px"}}
         [:i {:class "fa-solid fa-image"
              :style {:font-size "32px"
                      :color "#ccc"}}]])
      [picker/view
       {:value (:picture_url new-package)
        :multiple? false
        :on-select #(swap! state assoc-in [:new-package :picture_url] (:url %))
        :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]]
     
     ;; 3D Model field with storage picker
     [:div {:style {:margin "16px 0"}}
      [:label {:style {:display "block"
                       :margin-bottom "8px"
                       :color "#333"
                       :font-weight "500"}}
       "3D Model"]
      (if (:model_url new-package)
        [glb-viewer {:url (:model_url new-package) :width 200 :height 200}]
        [:div {:style {:width "200px"
                       :height "200px"
                       :border-radius "8px"
                       :border "1px solid #ddd"
                       :background-color "#f9f9f9"
                       :display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :margin-bottom "12px"}}
         [:i {:class "fa-solid fa-cube"
              :style {:font-size "32px"
                      :color "#ccc"}}]])
      [picker/view
       {:value (:model_url new-package)
        :multiple? false
        :on-select #(swap! state assoc-in [:new-package :model_url] (:url %))
        :extensions ["model/gltf-binary"]}]]
     
     [form-selector {:selected-form-id (:form_id new-package)
                     :forms forms
                     :on-change #(swap! state assoc-in [:new-package :form_id] %)}]
     
     [:div {:style {:margin "16px 0"}}
      [:label {:style {:display "flex"
                       :align-items "center"
                       :gap "8px"
                       :color "#333"
                       :font-weight "500"}}
       [:input {:type "checkbox"
                :checked (boolean (:popular new-package))
                :on-change #(swap! state assoc-in [:new-package :popular] (.. % -target -checked))}]
       "Popular Package"]]
     
     [:div {:style {:display "flex"
                    :gap "10px"
                    :justify-content "flex-end"
                    :margin-top "20px"
                    :padding-top "16px"
                    :border-top "1px solid #e0e0e0"
                    :background "white"}}
      [button/view {:on-click #(create-package {:state state})
                    :type :primary}
       "Create Package"]]]))

(defn add-package-header-button [{:keys [state]}]
  [button/view {:on-click #(do
                           (swap! state assoc :new-package {})
                           (dispatch [:modals/add {:id :add-package-modal
                                                 :label "Add New Package"
                                                 :content [add-package-modal {:state state}]
                                                 :open? true}]))
                :type :secondary
                :style {:padding "8px 16px"
                        :font-size "14px"}}
   [:i {:class "fa-solid fa-plus"
        :style {:margin-right "8px"}}]
   "Add Package"])

;; ===== Package Edit Form =====

(defn edit-package-modal [{:keys [state package]}]
  (let [forms (:forms @state)
        [editing-package set-editing-package] (react/useState (or package {}))
        [creating-form? set-creating-form!] (react/useState false)
        [new-form-data set-new-form-data] (react/useState {:title "New Form" :template [] :price_formula ""})]
    ;; Update editing-package when package prop changes
    (react/useEffect #(set-editing-package (or package {})) #js [package])
    [:div {:style {:padding "20px"
                   :width "100%"
                   :min-width "500px"
                   :max-height "85vh"
                   :height "auto"
                   :display "flex"
                   :flex-direction "column"
                   :overflow "hidden"}}
     [:div {:style {:overflow-y "auto"
                    :flex "1 1 auto"
                    :padding-right "10px"
                    :margin-right "-10px"}}
      ;; Name field
      [text-field {:label "Name"
                   :value (:name editing-package)
                   :placeholder "Package name"
                   :on-change #(set-editing-package (assoc editing-package :name %))}]
      
      ;; Description field
      [textarea-field {:label "Description"
                       :value (:description editing-package)
                       :placeholder "Package description"
                       :on-change #(set-editing-package (assoc editing-package :description %))}]
      
      ;; Prefix field
      [text-field {:label "Prefix"
                   :value (:prefix editing-package)
                   :placeholder "Package prefix (e.g., PKG-)"
                   :on-change #(set-editing-package (assoc editing-package :prefix %))}]
      
      ;; Picture field with storage picker
      [:div {:style {:margin "16px 0"}}
       [:label {:style {:display "block"
                        :margin-bottom "8px"
                        :color "#333"
                        :font-weight "500"}}
        "Picture"]
       (if (:picture_url editing-package)
         [:div {:style {:width "200px"
                        :height "200px"
                        :border-radius "8px"
                        :overflow "hidden"
                        :border "1px solid #ddd"
                        :margin-bottom "12px"}}
          [:img {:src (:picture_url editing-package)
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
                        :justify-content "center"
                        :margin-bottom "12px"}}
          [:i {:class "fa-solid fa-image"
               :style {:font-size "32px"
                       :color "#ccc"}}]])
       [picker/view
        {:value (:picture_url editing-package)
         :multiple? false
         :on-select #(set-editing-package (assoc editing-package :picture_url (:url %)))
         :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]]
      
      ;; 3D Model field with storage picker
      [:div {:style {:margin "16px 0"}}
       [:label {:style {:display "block"
                        :margin-bottom "8px"
                        :color "#333"
                        :font-weight "500"}}
        "3D Model"]
       (if (:model_url editing-package)
         [glb-viewer {:url (:model_url editing-package) :width 200 :height 200}]
         [:div {:style {:width "200px"
                        :height "200px"
                        :border-radius "8px"
                        :border "1px solid #ddd"
                        :background-color "#f9f9f9"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"
                        :margin-bottom "12px"}}
          [:i {:class "fa-solid fa-cube"
               :style {:font-size "32px"
                       :color "#ccc"}}]])
       [picker/view
        {:value (:model_url editing-package)
         :multiple? false
         :on-select #(set-editing-package (assoc editing-package :model_url (:url %)))
         :extensions ["model/gltf-binary"]}]]
      
      ;; Form selector
      [form-selector {:selected-form-id (:form_id editing-package)
                      :forms forms
                      :on-change #(set-editing-package (assoc editing-package :form_id %))
                      :on-create-form #(set-creating-form! true)}]
      
      ;; Inline form builder
      (when creating-form?
        [:div {:style {:margin "16px 0"
                       :padding "16px"
                       :border "2px solid #007bff"
                       :border-radius "8px"
                       :background "#f8f9fa"}}
         [:div {:style {:display "flex"
                        :justify-content "space-between"
                        :align-items "center"
                        :margin-bottom "16px"}}
          [:h4 {:style {:margin "0"
                        :color "#007bff"
                        :font-size "16px"}}
           "Create New Form"]
          [button/view {:type :secondary
                        :size :small
                        :on-click #(set-creating-form! false)}
           [:i {:class "fa-solid fa-times"}]]]
         
         [text-field {:label "Form Title"
                      :value (:title new-form-data)
                      :placeholder "Enter form title..."
                      :on-change #(set-new-form-data (assoc new-form-data :title %))}]
         
         [form-builder/view (:template new-form-data)
                           #(set-new-form-data (assoc new-form-data :template %))]
         
         [textarea-field {:label "Price Formula"
                          :value (:price_formula new-form-data)
                          :placeholder "[:quantity] * [:price]"
                          :on-change #(set-new-form-data (assoc new-form-data :price_formula %))}]
         
         [:div {:style {:display "flex"
                        :gap "8px"
                        :justify-content "flex-end"
                        :margin-top "16px"}}
          [button/view {:type :secondary
                        :on-click #(do (set-creating-form! false)
                                      (set-new-form-data {:title "New Form" :template [] :price_formula ""}))}
           "Cancel"]
          [button/view {:type :primary
                        :on-click #(save-new-form new-form-data state set-editing-package editing-package set-creating-form! set-new-form-data)}
           "Save Form"]]])
      
      ;; Popular checkbox
      [:div {:style {:margin "16px 0"}}
       [:label {:style {:display "flex"
                        :align-items "center"
                        :gap "8px"
                        :color "#333"
                        :font-weight "500"}}
        [:input {:type "checkbox"
                 :checked (boolean (:popular editing-package))
                 :on-change #(set-editing-package (assoc editing-package :popular (.. % -target -checked)))}]
        "Popular Package"]]]
     
     ;; Modal buttons
     [:div {:style {:display "flex"
                    :justify-content "flex-end"
                    :gap "12px"
                    :flex-shrink 0
                    :margin-top "20px"
                    :padding-top "16px"
                    :border-top "1px solid #e0e0e0"
                    :background "white"}}
      [button/view {:type :secondary
                    :on-click #(dispatch [:modals/close :edit-package-modal])}
       "Cancel"]
      [button/view {:type :primary
                    :on-click #(update-package {:state state :package editing-package})}
       "Update Package"]]]))

(defn open-edit-package-modal [{:keys [state package]}]
  (dispatch [:modals/add {:id :edit-package-modal
                        :label "Edit Package"
                        :content [edit-package-modal {:state state :package package}]
                        :open? true}]))

(defn edit-package-form [{:keys [state package]}]
  (let [forms (:forms @state)
        [editing-package set-editing-package] (react/useState (or package {}))]
    ;; Update editing-package when package prop changes
    (react/useEffect #(set-editing-package (or package {})) #js [package])
    [:div {:style {:background "white"
                   :border-radius "8px"
                   :padding "20px"
                   :margin "20px 0"
                   :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}}
     [:h3 {:style {:margin "0 0 20px 0"
                   :color "#333"
                   :font-size "18px"
                   :font-weight "600"}}
      "Edit Package"]
     
     [text-field {:label "Name"
                  :value (:name editing-package)
                  :placeholder "Package name"
                  :on-change #(set-editing-package (assoc editing-package :name %))}]
     
     [textarea-field {:label "Description"
                      :value (:description editing-package)
                      :placeholder "Package description"
                      :on-change #(set-editing-package (assoc editing-package :description %))}]
     
     [text-field {:label "Prefix"
                  :value (:prefix editing-package)
                  :placeholder "Package prefix (e.g., PKG-)"
                  :on-change #(set-editing-package (assoc editing-package :prefix %))}]
     
     ;; Picture field with storage picker
     [:div {:style {:margin "16px 0"}}
      [:label {:style {:display "block"
                       :margin-bottom "8px"
                       :color "#333"
                       :font-weight "500"}}
       "Picture"]
      (if (:picture_url editing-package)
        [:div {:style {:width "200px"
                       :height "200px"
                       :border-radius "8px"
                       :overflow "hidden"
                       :border "1px solid #ddd"
                       :margin-bottom "12px"}}
         [:img {:src (:picture_url editing-package)
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
                       :justify-content "center"
                       :margin-bottom "12px"}}
         [:i {:class "fa-solid fa-image"
              :style {:font-size "32px"
                      :color "#ccc"}}]])
      [picker/view
       {:value (:picture_url editing-package)
        :multiple? false
        :on-select #(set-editing-package (assoc editing-package :picture_url (:url %)))
        :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]]
     
     ;; 3D Model field with storage picker
     [:div {:style {:margin "16px 0"}}
      [:label {:style {:display "block"
                       :margin-bottom "8px"
                       :color "#333"
                       :font-weight "500"}}
       "3D Model"]
      (if (:model_url editing-package)
        [glb-viewer {:url (:model_url editing-package) :width 200 :height 200}]
        [:div {:style {:width "200px"
                       :height "200px"
                       :border-radius "8px"
                       :border "1px solid #ddd"
                       :background-color "#f9f9f9"
                       :display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :margin-bottom "12px"}}
         [:i {:class "fa-solid fa-cube"
              :style {:font-size "32px"
                      :color "#ccc"}}]])
      [picker/view
       {:value (:model_url editing-package)
        :multiple? false
        :on-select #(set-editing-package (assoc editing-package :model_url (:url %)))
        :extensions ["model/gltf-binary"]}]]
     
     [form-selector {:selected-form-id (:form_id editing-package)
                     :forms forms
                     :on-change #(set-editing-package (assoc editing-package :form_id %))}]
     
     [:div {:style {:margin "16px 0"}}
      [:label {:style {:display "flex"
                       :align-items "center"
                       :gap "8px"
                       :color "#333"
                       :font-weight "500"}}
       [:input {:type "checkbox"
                :checked (boolean (:popular editing-package))
                :on-change #(set-editing-package (assoc editing-package :popular (.. % -target -checked)))}]
       "Popular Package"]]
     
     [:div {:style {:display "flex"
                    :gap "10px"
                    :justify-content "flex-end"
                    :margin-top "20px"
                    :padding-top "16px"
                    :border-top "1px solid #e0e0e0"
                    :background "white"}}
      [button/view {:on-click #(swap! state assoc :editing-package-id nil)
                    :type :secondary}
       "Cancel"]
      [button/view {:on-click #(update-package {:state state :package editing-package})
                    :type :primary}
       "Update Package"]]]))