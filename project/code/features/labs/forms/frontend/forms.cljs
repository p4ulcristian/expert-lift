(ns features.labs.forms.frontend.forms
  (:require
   ["react" :as react]
   [app.frontend.request :as request]
   [features.labs.shared.frontend.components.header :as header]
   [features.common.form.frontend.builder :as form-builder]
   [reagent.core :as r]
   [ui.card :as card]
   [ui.button :as button]
   [ui.textarea :as textarea]
   [ui.text-field :as text-field]
   [ui.tree-viewer :as tree-viewer]
   [ui.notification :as notification]
   [zero.frontend.react :as zero-react]))
  

(def forms-data (r/atom {}))
(def new-form-template {:title "! New Form"
                        :template []})

(defn render-field [field]
  (let [field-type (:type field)
        title (:title field)
        description (:description field)]
    [:div {:class "form-field"}
     [:div {:class "form-field-title"}
      title]]))
     
    ;;  (when description
    ;;    [:div {:class "form-field-description"}
    ;;     description])
     
    ;;  (case field-type
    ;;    :select [:div {:class "form-field-info"}
    ;;             (str "Options: " (count (:options field)))]
    ;;    :digit_input [:div {:class "form-field-info"}
    ;;                  (str "Input range: " (:min field) " - " (:max field))]
    ;;    :group [:div {:class "form-field-info"}
    ;;            (str "Group with " (count (:inputs field)) " inputs")]
    ;;    :switch [:div {:class "form-field-info"}
    ;;             "Toggle option"]
    ;;    [:div {:class "form-field-info"}
    ;;     (str "Field type: " field-type)])]))

(defn delete-form [form-id]
  (when (js/confirm "Are you sure you want to delete this form?")
    (request/pathom
      {:query `[(forms/delete-form! {:form-id ~form-id})]
       :callback (fn [response]
                   (println "Delete response:" response)
                   ;; Refresh the forms list after deleting
                   (request/pathom
                     {:query '[:forms/get-forms]
                      :callback (fn [refresh-response]
                                  (reset! forms-data (reduce 
                                                       (fn [acc form]
                                                         (assoc acc (:id form) form))
                                                       {}
                                                       (get-in refresh-response [:forms/get-forms])))
                                  (notification/toast! :success "Form deleted successfully!"))}))})))

(defn form-card [form-data]
  (let [[state set-state] (react/useState form-data)
        [editing? set-editing!] (react/useState false)
        [temp-title set-temp-title!] (react/useState (:title state))]
    [:div {:style {:background    "var(--ir-secondary)"
                   :color         "var(--ir-text-primary)"
                   :border-radius "12px"
                   :padding       "12px"
                   :box-shadow    "var(--box-shadow-black-xx-light)"
                   :display       "grid"
                   :gap           "10px"}}
      ;; Title header with edit mode
      [:div {:style {:display "flex"
                     :align-items "center"
                     :justify-content "space-between"
                     :gap "10px"}}
       (if editing?
         ;; Edit mode
         [:<>
          [text-field/view {:value     temp-title
                            :style     {:color "black"
                                        :flex "1"}
                            :on-change set-temp-title!}]
          [:div {:style {:display "flex"
                         :gap "5px"}}
           [button/view {:type :success
                         :on-click #(do (set-state (assoc state :title temp-title))
                                        (set-editing! false))}
            [:i {:class "fa-solid fa-check"}]]
           [button/view {:type :secondary
                         :on-click #(do (set-temp-title! (:title state))
                                        (set-editing! false))}
            [:i {:class "fa-solid fa-times"}]]]]
         ;; Display mode
         [:<>
          [:b {:style {:font-size "1rem"}} (:title state)]
          [:div {:style {:display "flex"
                         :gap "5px"}}
           [button/view {:type :secondary
                         :on-click #(do (set-temp-title! (:title state))
                                        (set-editing! true))}
            [:i {:class "fa-solid fa-pen"}]]
           [button/view {:type :warning
                         :on-click #(delete-form (:id state))}
            [:i {:class "fa-solid fa-trash"}]]]])]
      
      [form-builder/view (:template state) 
                         #(set-state (assoc state :template %))]
      [:div "Price Formula"]
      [ui.textarea/view {:value       (:price_formula state)
                         :placeholder "[:quantity] * [:price]"
                         :style       {:color "black"}
                         :on-change   #(set-state (assoc state :price_formula %))}]
      [button/view {:type     :primary
                    :disabled (= state form-data)
                    :on-click #(request/pathom
                                 {:query `[(forms/save-form-data! {:form-data ~state})]
                                  :callback (fn [response]
                                              (println "Save response:" response)
                                              (notification/toast! :success "Form saved successfully!"))})}
          "Save"]]))
        ;; [:div {:class "form-card-list"}
        ;;   (for [field (:template form-data)]
        ;;     ^{:key (:id field)}
        ;;     [render-field field])]]}])

(defn forms-grid []
  [:div {:class "forms-grid"}
    (when (and @forms-data (map? @forms-data))
      (for [[_ form-data] (sort-by #(-> % second :title) @forms-data)]
        ^{:key (:id form-data)}
        [form-card form-data]))])

(defn create-new-form []
  (let [form-data new-form-template]
    (request/pathom
      {:query `[(forms/save-form-data! {:form-data ~form-data})]
       :callback (fn [response]
                   (println "Create response:" response)
                   ;; Refresh the forms list after creating
                   (request/pathom
                     {:query '[:forms/get-forms]
                      :callback (fn [refresh-response]
                                  (reset! forms-data (reduce 
                                                       (fn [acc form]
                                                         (assoc acc (:id form) form))
                                                       {}
                                                       (get-in refresh-response [:forms/get-forms]))))}))})))

(defn view []
  (let [callback (fn [response]
                   (println "response" response)
                   (reset! forms-data (reduce 
                                        (fn [acc form]
                                          (assoc acc (:id form) form))
                                        {}
                                        (get-in response [:forms/get-forms]))))]
    
    (zero-react/use-effect
      {:mount #(request/pathom
                 {:query '[:forms/get-forms]
                  :callback callback})})
    
    [:div {:class "forms-container"}
      [header/view]
      [:div {:class "forms-header"
             :style {:display "flex"
                     :justify-content "space-between"
                     :align-items "center"
                     :margin-bottom "24px"
                     :padding "20px 24px"
                     :border-bottom "2px solid #e9ecef"}}
        [:div {:style {:display "flex"
                       :align-items "center"
                       :gap "12px"}}
         [:div {:style {:width "4px"
                        :height "32px"
                        :background "linear-gradient(135deg, #007bff 0%, #0056b3 100%)"
                        :border-radius "2px"}}]
         [:h1 {:style {:margin "0"
                       :color "white"
                       :font-size "32px"
                       :font-weight "800"
                       :letter-spacing "-0.5px"
                       :text-shadow "0 2px 4px rgba(0,0,0,0.3)"}}
          [:i {:class "fa-solid fa-file-lines"
               :style {:margin-right "12px"
                       :color "white"
                       :filter "drop-shadow(0 2px 4px rgba(0,0,0,0.3))"}}]
          "Form Templates"]]
        [button/view {:type :primary
                      :size :large
                      :on-click create-new-form
                      :style {:padding "12px 24px"
                              :font-size "16px"
                              :font-weight "600"
                              :border-radius "8px"
                              :box-shadow "0 4px 12px rgba(0,123,255,0.2)"
                              :transition "all 0.2s ease"
                              :display "flex"
                              :align-items "center"
                              :gap "8px"}}
         [:i {:class "fa-solid fa-plus"
              :style {:font-size "14px"}}]
         "Add New Form"]]
      
      [:div {:class "forms-content"}]
        ;; [tree-viewer/tree-viewer {:data    @forms-data
        ;;                           :on-save (fn [data]
        ;;                                      (request/pathom
        ;;                                        {:query `[(forms/save-form-data! {:form-data ~data})]
        ;;                                         :callback (fn [response]
        ;;                                                     (println "Save response:" response))}))}]]
      [forms-grid]])) 