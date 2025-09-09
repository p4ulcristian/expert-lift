(ns features.labs.parts.frontend.part-form
  (:require
   ["@react-three/drei" :as r3d]
   ["react" :as react]
   [app.frontend.request :as request]
   [clojure.string :as str]
   [features.common.storage.frontend.picker :as picker]
   [features.common.form.frontend.builder :as form-builder]
   [features.labs.parts.frontend.glb-viewer :refer [glb-viewer]]
   [features.labs.parts.frontend.utils :as utils]
   [ui.button :as button]
   [ui.notification :as notification]
   [zero.frontend.re-frame :refer [dispatch]]))

;; ===== Form Save Helper Functions =====

(defn reset-form-builder-ui [set-creating-form! set-new-form-data]
  (set-creating-form! false)
  (set-new-form-data {:title "New Form" :template [] :price_formula ""})
  (notification/toast! :success "Form created successfully!"))

(defn reload-and-select-form [state form-id set-new-part new-part set-creating-form! set-new-form-data]
  (request/pathom
    {:query '[:forms/get-forms]
     :callback (fn [forms-response]
                 (let [forms-list (get-in forms-response [:forms/get-forms])
                       ;; Ensure form-id is a string for comparison
                       form-id-str (str form-id)]
                   (println "Setting form_id to:" form-id-str)
                   (println "Available forms:" (map #(select-keys % [:id :title]) forms-list))
                   (swap! state assoc :forms forms-list)
                   (set-new-part (assoc new-part :form_id form-id-str))
                   (reset-form-builder-ui set-creating-form! set-new-form-data)))}))

(defn handle-form-save-success [response state created-form-id set-new-part new-part set-creating-form! set-new-form-data]
  (when created-form-id
    (println "Created form:" created-form-id)
    ;; The response is just the UUID, not an object with :id
    (reload-and-select-form state (str created-form-id) set-new-part new-part set-creating-form! set-new-form-data)))

(defn save-new-form [new-form-data state set-new-part new-part set-creating-form! set-new-form-data]
  (request/pathom
    {:query `[(forms/save-form-data! {:form-data ~new-form-data})]
     :callback (fn [response]
                 (println "Save form response:" response)
                 (let [created-form-id (get-in response [:forms/save-form-data!])]
                   (handle-form-save-success response state created-form-id set-new-part new-part set-creating-form! set-new-form-data)))}))

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

(defn select-field [{:keys [label value on-change options]}]
  [:div
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :color "#333"
                    :font-weight "500"}}
    label]
   [:select {:value value
             :style {:width "100%"
                     :padding "8px"
                     :border "1px solid #ddd"
                     :border-radius "4px"
                     :font-size "14px"}
             :on-change #(on-change (.. ^js % -target -value))}
    (for [[value label] options]
      ^{:key value}
      [:option {:value value} label])]])

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
      ^{:key (str (:id form))}
      [:option {:value (str (:id form))} (:title form)])]
   [button/view {:type :secondary
                 :size :small
                 :on-click on-create-form
                 :style {:width "100%"
                         :margin-bottom "8px"}}
    [:i {:class "fa-solid fa-plus"
         :style {:margin-right "6px"}}]
    "Create New Form"]])

(defn textarea-field [{:keys [label value on-change]}]
  [:div
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :color "#333"
                    :font-weight "500"}}
     label]
   [:textarea {:value value
               :style {:width "100%"
                       :padding "8px"
                       :border "1px solid #ddd"
                       :border-radius "4px"
                       :font-size "14px"
                       :min-height "100px"
                       :resize "vertical"}
               :on-change #(on-change (.. ^js % -target -value))}]])

(defn picture-field [{:keys [picture on-change]}]
  [:div {:style {:flex-shrink 0
                 :display "flex"
                 :flex-direction "column"
                 :gap "12px"}}
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :color "#333"
                    :font-weight "500"}}
    "Picture"]
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
                   :color "#ccc"}}]])
   [:div {:style {:width "200px"}}
    [picker/view
     {:value picture
      :multiple? false
      :on-select #(on-change (:url %))
      :extensions ["image/png" "image/jpeg" "image/svg+xml"]}]]])

(defn model-field [{:keys [model on-change]}]
  [:div {:style {:flex-shrink 0
                 :display "flex"
                 :flex-direction "column"
                 :gap "12px"}}
   [:label {:style {:display "block"
                    :margin-bottom "8px"
                    :color "#333"
                    :font-weight "500"}}
    "3D Model"]
   (if model
     [glb-viewer {:url model :width 200 :height 200}]
     [:div {:style {:width "200px"
                    :height "200px"
                    :border-radius "8px"
                    :border "1px solid #ddd"
                    :background-color "#f9f9f9"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"}}
      [:i {:class "fa-solid fa-cube"
           :style {:font-size "32px"
                   :color "#ccc"}}]])
   [:div {:style {:width "200px"}}
    [picker/view
     {:value model
      :multiple? false
      :on-select #(on-change (:url %))
      :extensions ["model/gltf-binary"]}]]])

(defn mesh-selector-component [{:keys [model-url mesh-id on-change]}]
  (let [^js gltf   (try (r3d/useGLTF model-url) (catch js/Error e (println e)))]
    (when gltf
      [:select {:value     (or mesh-id "")
                :on-change #(on-change (.. ^js % -target -value))
                :style     {:width "100%"
                            :padding "8px"
                            :border "1px solid #ddd"
                            :border-radius "4px"
                            :font-size "14px"}}
        [:option {:value ""} "Select a mesh..."]
        (for [mesh (-> (.-meshes gltf)(js/Object.keys))]
          ^{:key mesh}
          [:option {:value mesh} mesh])])))
       

(defn mesh-selector [{:keys [model-url mesh-id on-change]}]
  (when model-url
    [:div
     [:label {:style {:display "block"
                      :margin-bottom "8px"
                      :color "#333"
                      :font-weight "500"}}
      "Select Mesh"]
     [:> react/Suspense {:fallback "Loading meshes..."}
      [mesh-selector-component {:model-url model-url
                                :mesh-id   mesh-id
                                :on-change on-change}]]]))

(defn part-selector [{:keys [selected-parts all-parts on-add on-remove state]}]
  (let [available-parts (filter #(not (some #{(:id %)} selected-parts))
                                all-parts)]
    [:div
     [:label {:style {:display "block"
                      :margin-bottom "8px"
                      :color "#333"
                      :font-weight "500"}}
      "Part IDs"]
     [:div {:style {:display "flex"
                    :flex-direction "column"
                    :gap "8px"}}
      ;; Selected parts display
      (when (seq selected-parts)
        [:div {:style {:display "flex"
                       :flex-wrap "wrap"
                       :gap "8px"
                       :padding "8px"
                       :border "1px solid #ddd"
                       :border-radius "4px"
                       :background-color "#f9f9f9"}}
         (for [part-id selected-parts
               :let [part (first (filter #(= (:id %) part-id) all-parts))]]
           ^{:key part-id}
           [:div {:style {:display "flex"
                          :align-items "center"
                          :gap "8px"
                          :padding "4px 8px"
                          :background-color "white"
                          :border "1px solid #ddd"
                          :border-radius "4px"}}
            [:span (:name part)]
            [:button {:style {:border "none"
                              :background "none"
                              :color "#666"
                              :cursor "pointer"
                              :padding "0 4px"}
                      :on-click #(on-remove part-id)}
             [:i {:class "fa-solid fa-xmark"}]]])])
      
      ;; Part selection dropdown
      [:select {:value ""
                :style {:width "100%"
                        :padding "8px"
                        :border "1px solid #ddd"
                        :border-radius "4px"
                        :font-size "14px"}
                :on-change #(let [selected-id (.. ^js % -target -value)]
                              (when (not= selected-id "")
                                (on-add selected-id)))}
        [:option {:value ""} "Select a part or package to add..."]
        (for [part available-parts]
          [:option {:key (:id part)
                    :value (:id part)}
           (str (:name part) " (" (clojure.string/capitalize (:type part)) ")")])]]]))

;; ===== Form Sections =====

;; ===== Backend Integration =====

(defn create-part-query [new-part category-id package-id]
  `[(parts/create-part! 
     {:name        ~(:name new-part)
      :description ~(:description new-part)
      :picture_url ~(:picture new-part)
      :category_id ~category-id
      :package_id  ~package-id
      :type        ~(:type new-part)
      :part_ids    ~(:part_ids new-part)
      :mesh_id     ~(:mesh_id new-part)
      :popular     ~(:popular new-part)
      :form_id     ~(:form_id new-part)})])

(defn update-part-query [new-part]
  `[(parts/update-part! 
     {:id ~(:id new-part)
      :name ~(:name new-part)
      :description ~(:description new-part)
      :picture_url ~(:picture new-part)
      :type ~(:type new-part)
      :part_ids ~(:part_ids new-part)
      :mesh_id ~(:mesh_id new-part)
      :popular ~(:popular new-part)
      :form_id ~(:form_id new-part)})])

(defn handle-part-response [state response]
  (when-let [new-part (get-in response [:parts/create-part!])]
    (swap! state update :parts conj new-part)
    ;; If we're viewing a package, automatically add the new part to it
    (when-let [current-package-id (last (:package-navigation-stack @state))]
      (let [all-parts (:parts @state)
            package (first (filter #(= (:id %) current-package-id) all-parts))
            updated-part-ids (conj (:part_ids package) (:id new-part))]
        (request/pathom-with-workspace-id
         {:query `[(parts/update-part!
                    {:id          ~current-package-id
                     :name        ~(:name package)
                     :description ~(:description package)
                     :picture_url ~(:picture_url package)
                     :model_url   ~(:model_url package)
                     :category_id ~(:category_id package)
                     :type        ~(:type package)
                     :prefix      ~(:prefix package)
                     :package_id  ~(:id package)
                     :part_ids    ~updated-part-ids})]
          :callback (fn [response]
                      (when-let [updated-package (get-in response [:parts/update-part!])]
                        (swap! state update :parts
                               (fn [parts]
                                 (mapv (fn [p]
                                         (if (= (:id p) current-package-id)
                                           updated-package
                                           p))
                                       parts)))))})))
    (utils/reset-new-part state)
    (swap! state assoc :editing-part-id nil)
    (dispatch [:modals/close :add-part-modal])))

(defn handle-part-creation [state new-part]
  (let [current-package-id (last (:package-navigation-stack @state))
        ;; Ensure the part always has type "part"
        part-with-type (assoc new-part :type "part")]
    ;; Parts should always be created within a package
    (if current-package-id
      (request/pathom-with-workspace-id
       {:query    (create-part-query part-with-type nil current-package-id)
        :callback #(handle-part-response state %)})
      (js/alert "Parts can only be created inside packages. Please navigate to a package first."))))

(defn handle-part-edit [state editing-id new-part]
  (let [current-package-id (last (:package-navigation-stack @state))
        ;; Parts should always be edited within their package context
        final-package-id (or current-package-id (:package_id new-part))
        ;; Ensure the part always has type "part"
        part-with-type (assoc new-part :type "part")]
    (request/pathom-with-workspace-id
     {:query `[(parts/update-part! 
                {:id          ~editing-id
                 :name        ~(:name part-with-type)
                 :description ~(:description part-with-type)
                 :picture_url ~(:picture_url part-with-type)
                 :package_id  ~final-package-id
                 :model_url   ~(:model_url part-with-type)
                 :mesh_id     ~(:mesh_id part-with-type)
                 :type        ~(:type part-with-type)
                 :part_ids    ~(:part_ids part-with-type)
                 :popular     ~(:popular part-with-type)
                 :form_id     ~(:form_id part-with-type)})]
      :callback (fn [response]
                  (when-let [updated-part (get-in response [:parts/update-part!])]
                    (swap! state update :parts
                           (fn [parts]
                             (mapv (fn [p]
                                     (if (= (:id p) editing-id)
                                       updated-part
                                       p))
                                   parts)))
                    (utils/reset-new-part state)
                    (swap! state assoc :editing-part-id nil)
                    (dispatch [:modals/close :add-part-modal])))})))

(defn handle-part-delete [state part-id]
  (when (js/confirm "Are you sure you want to delete this part?")
    (request/pathom-with-workspace-id
     {:query `[(parts/delete-part! {:id ~part-id})]
      :callback (fn [response]
                  (when-let [deleted-id (get-in response [:parts/delete-part! :id])]
                    (swap! state update :parts
                           (fn [parts]
                             (filterv #(not= (:id %) deleted-id)
                                     parts)))))})))

;; ===== Form Section Components =====

(defn basic-info-section [{:keys [new-part on-change forms on-create-form]}]
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "16px"
                 :flex-grow 1}}
   [text-field {:label "Name"
                :value (:name new-part)
                :placeholder "Part name"
                :on-change #(on-change :name %)}]
   
   [form-selector {:selected-form-id (:form_id new-part)
                   :forms forms
                   :on-change #(on-change :form_id %)
                   :on-create-form on-create-form}]
   
   [:div {:style {:margin "16px 0"}}
    [:label {:style {:display "flex"
                     :align-items "center"
                     :gap "8px"
                     :color "#333"
                     :font-weight "500"}}
     [:input {:type "checkbox"
              :checked (boolean (:popular new-part))
              :on-change #(on-change :popular (.. % -target -checked))}]
     "Popular Part"]]])

(defn package-parts-section [{:keys [new-part parts on-change state]}]
  ;; This section can be used for part relationships if needed
  [:div])

;; ===== Main Components =====

(defn add-part-modal [{:keys [state]}]
  (let [editing-id (:editing-part-id @state)
        [new-part set-new-part] (react/useState (:new-part @state))
        forms (:forms @state)
        [creating-form? set-creating-form!] (react/useState false)
        [new-form-data set-new-form-data] (react/useState {:title "New Form" :template [] :price_formula ""})
        current-package-id (last (:package-navigation-stack @state))
        current-package (when current-package-id
                         (first (filter #(= (:id %) current-package-id) (:packages @state))))
        submit-button-text (if editing-id "Save Changes" "Add Part")]
    ;; Sync with state when modal opens
    (react/useEffect #(set-new-part (:new-part @state)) #js [(:new-part @state)])
    [:div {:style {:padding "20px"
                   :width "100%"
                   :min-width "600px"
                   :max-width "900px"
                   :max-height "85vh"
                   :height "auto"
                   :display "flex"
                   :flex-direction "column"
                   :overflow "hidden"}}
     [:div {:style {:overflow-y "auto"
                    :flex "1 1 auto"
                    :padding-right "10px"
                    :margin-right "-10px"}}
      [:div {:style {:display "flex"
                     :gap "24px"
                     :margin-bottom "24px"
                     :flex-wrap "wrap"}}
       [picture-field {:picture (:picture_url new-part)
                       :on-change #(set-new-part (assoc new-part :picture_url %))}]
       [basic-info-section {:new-part new-part
                            :on-change #(set-new-part (assoc new-part %1 %2))
                            :forms forms
                            :on-create-form #(set-creating-form! true)}]
       
       ;; Show parent package 3D model and mesh selector
       (when (and current-package (:model_url current-package))
         [:div {:style {:flex-shrink 0
                        :display "flex"
                        :flex-direction "column"
                        :gap "12px"}}
          [:label {:style {:display "block"
                           :margin-bottom "8px"
                           :color "#333"
                           :font-weight "500"}}
           (str "Parent Package Model: " (:name current-package))]
          [glb-viewer {:url (:model_url current-package) :width 200 :height 200}]
          [mesh-selector {:model-url (:model_url current-package)
                         :mesh-id (:mesh_id new-part)
                         :on-change #(set-new-part (assoc new-part :mesh_id %))}]])]
      
      [:div {:style {:display "flex"
                     :flex-direction "column"
                     :gap "16px"}}
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
                         :on-click #(save-new-form new-form-data state set-new-part new-part set-creating-form! set-new-form-data)}
            "Save Form"]]])
       
       [package-parts-section {:new-part new-part
                               :parts (:parts @state)
                               :on-change #(set-new-part (assoc new-part %1 %2))
                               :state state}]
       
       [textarea-field {:label "Description"
                        :value (:description new-part)
                        :on-change #(set-new-part (assoc new-part :description %))}]]]
     
     [:div {:style {:display "flex"
                    :justify-content "flex-end"
                    :gap "12px"
                    :flex-shrink 0
                    :margin-top "20px"
                    :padding-top "16px"
                    :border-top "1px solid #e0e0e0"
                    :background "white"}}
      [button/view {:mode :clear
                    :type :secondary
                    :on-click #(do
                                (utils/reset-new-part state) 
                                (swap! state assoc :editing-part-id nil) 
                                (dispatch [:modals/close :add-part-modal]))}
       "Cancel"]
      
      [button/view {:mode :filled
                    :type :primary
                    :on-click (fn []
                                ;; Update state with latest new-part values before saving
                                (swap! state assoc :new-part new-part)
                                (if editing-id
                                  (handle-part-edit state editing-id new-part)
                                  (handle-part-creation state new-part)))}
       submit-button-text]]]))

(defn add-part-button [{:keys [state]}]
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
                             (utils/reset-new-part state) 
                             (swap! state assoc :editing-part-id nil)
                             (dispatch [:modals/add {:id :add-part-modal
                                                     :label "Add New Part"
                                                     :content [add-part-modal {:state state}]
                                                     :open? true}]))}
   [:i {:class "fa-solid fa-puzzle-piece"
        :style {:font-size "20px"}}] 
   "Add Part"])

(defn add-part-header-button [{:keys [state]}]
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
                         (utils/reset-new-part state) 
                         (swap! state assoc :editing-part-id nil)
                         (dispatch [:modals/add {:id :add-part-modal
                                                 :label "Add New Part"
                                                 :content [add-part-modal {:state state}]
                                                 :open? true}]))}
   [:i {:class "fa-solid fa-plus"
        :style {:font-size "12px"}}]
   "Add Part"])

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
                :placeholder "Part Name"}]])

(defn description-field [{:keys [value on-change]}]
  [:div
   [form-label {:text "Description"}]
   [textarea-input {:value value
                    :on-change on-change}]])

(defn popular-field [{:keys [value on-change]}]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"}}
   [:input {:type "checkbox"
            :checked value
            :on-change #(on-change (.. % -target -checked))
            :style {:width "16px"
                    :height "16px"}}]
   [form-label {:text "Popular Part"}]])

;; Form Layout Components
(defn form-fields [{:keys [new-part on-change]}]
  [:div {:style {:flex-grow 1
                 :display "flex"
                 :flex-direction "column"
                 :gap "16px"}}
   [name-field {:value (:name new-part)
                :on-change #(on-change :name %)}]
   [description-field {:value (:description new-part)
                       :on-change #(on-change :description %)}]
   [popular-field {:value (:popular new-part)
                   :on-change #(on-change :popular %)}]])