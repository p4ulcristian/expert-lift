(ns features.common.form.frontend.builder
  (:require
   ["react" :as react]
   ["react-sortablejs" :as ReactSortable]
   [clojure.string]
   [app.frontend.request :as request]
   [ui.button :as button]
   [ui.notification :as notification]
   [ui.text-field :as text-field]
   [ui.select :as select]))

;; Form Builder Component
(def input-types
  [{:label "Select" :value "select" :icon "fa-solid fa-list"}
   {:label "Number" :value "digit_input" :icon "fa-solid fa-hashtag"}
   {:label "Toggle" :value "switch" :icon "fa-solid fa-toggle-off"}
   {:label "Group" :value "group" :icon "fa-solid fa-folder"}])

(defn generate-input-id []
  (str "input_" (random-uuid)))

;; Helper function for removing element at index
(defn remove-nth [coll index]
  (vec (concat (take index coll) (drop (inc index) coll))))

;; Helper function to split icon classes consistently
(defn split-icon-classes [icon-str]
  (clojure.string/split (or icon-str "") #" "))

(defn option-with-icon [option-props _select-props]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"}}
   [:i {:class (split-icon-classes (:icon option-props))}]
   (:label option-props)])

(defn select-label-with-icon [props label-value]
  (let [selected-option (first (filter #(= (:value %) (:value (:value props))) (:options props)))]
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "8px"}}
     (when selected-option
       [:i {:class (split-icon-classes (:icon selected-option))}])
     label-value]))

(defn get-human-readable-type [type]
  (case type
    "select" "Select"
    "digit_input" "Number"
    "switch" "Toggle"
    "group" "Group"
    type))

(defn create-input-template [type]
  (let [human-type (get-human-readable-type type)
        base-template {:id (generate-input-id)
                       :title (str "New " human-type)
                       :type type
                       :condition ""
                       :prefix ""
                       :service_id ""
                       :modifiers {:model ""
                                   :camera ""}}]
    (case type
      "select" (assoc base-template :options [{:label "Option 1" :value "value1" :modifiers {:model "" :camera ""}}
                                              {:label "Option 2" :value "value2" :modifiers {:model "" :camera ""}}])
      "digit_input" (assoc base-template 
                           :value 1
                           :min {:type "num" :value 1}
                           :max {:type "num" :value 100})
      "switch" (assoc base-template :value false)
      "group" (assoc base-template :inputs [])
      base-template)))

(defn group-separator-view
  "Renders a group separator with inline editing and remove functionality"
  [state set-state on-remove]
  [:div {:style {:margin "20px 0"
                 :position "relative"
                 :cursor "grab"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :gap "12px"}}
    [:div {:style {:flex "1"
                   :height "2px"
                   :background "linear-gradient(90deg, transparent, #007bff, transparent)"}}]
    [:div {:style {:background "#007bff"
                   :color "white"
                   :padding "8px 16px"
                   :border-radius "20px"
                   :font-weight "600"
                   :font-size "14px"
                   :display "flex"
                   :align-items "center"
                   :gap "8px"
                   :box-shadow "0 2px 8px rgba(0,123,255,0.2)"}}
     [:i {:class "fa-solid fa-grip-vertical"
          :style {:color "rgba(255,255,255,0.7)"
                  :margin-right "4px"
                  :cursor "grab"}}]
     [:i {:class "fa-solid fa-folder"}]
     [:span {:contentEditable true
             :suppressContentEditableWarning true
             :on-blur #(set-state (assoc state :title (-> % .-target .-textContent)))
             :style {:outline "none"
                     :min-width "60px"
                     :display "inline-block"}}
      (:title state)]]
    [:button {:style {:background "#dc3545"
                      :color "white"
                      :border "none"
                      :border-radius "50%"
                      :width "24px"
                      :height "24px"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :cursor "pointer"
                      :font-size "12px"}
              :on-click #(on-remove (:id state))}
     [:i {:class "fa-solid fa-times"}]]
    [:div {:style {:flex "1"
                   :height "2px"
                   :background "linear-gradient(90deg, transparent, #007bff, transparent)"}}]]])

(defn input-header-view
  "Renders the collapsible header for regular input items"
  [state expanded? set-expanded! on-remove]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :align-items "center"
                 :cursor "pointer"
                 :user-select "none"}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :gap "12px"
                  :flex "1"}
          :on-click #(set-expanded! (not expanded?))}
    [:i {:class (if expanded?
                  "fa-solid fa-chevron-down"
                  "fa-solid fa-chevron-right")
         :style {:color "#666"
                 :font-size "12px"
                 :transition "transform 0.2s"}}]
    [:h4 {:style {:margin "0"
                  :color "#2c3e50"
                  :font-size "16px"
                  :font-weight "600"
                  :display "flex"
                  :align-items "center"
                  :gap "8px"}}
     (let [type-config (first (filter #(= (:value %) (:type state)) input-types))]
       [:<>
        [:i {:class (split-icon-classes (:icon type-config))
             :style {:color "#007bff"}}]
        (str (:title state))])]]
   [button/view {:type :warning
                 :size :small
                 :on-click #(on-remove (:id state))}
    "Remove"]])

(defn select-configuration-view
  "Renders configuration interface for select input type"
  [state set-state]
  [:div
   [:div {:style {:display "flex"
                  :justify-content "space-between"
                  :align-items "center"
                  :margin-bottom "12px"}}
    [:label {:style {:font-weight "600"
                     :color "#555"
                     :font-size "14px"}}
     "Options:"]
    [button/view {:type :primary
                  :size :small
                  :on-click #(let [new-option {:label "New Option" :value "new_option" :modifiers {:model "" :camera ""}}
                                   updated-options (conj (:options state) new-option)]
                               (set-state (assoc state :options updated-options)))}
     "Add Option"]]
   [:> (.-ReactSortable ReactSortable)
    {:list (clj->js (mapv #(assoc % :id (or (:id %) (random-uuid))) (:options state)))
     :setList (fn [new-list]
                (let [js-list (js->clj new-list :keywordize-keys true)
                      updated-options (mapv #(dissoc % :id) js-list)]
                  (set-state (assoc state :options updated-options))))
     :animation 200
     :delay 0
     :delayOnTouchStart true
     :style {:display "flex"
             :flex-direction "column"
             :gap "8px"}}
    (map-indexed
      (fn [index option]
        ^{:key (or (:id option) index)}
        [:div {:data-id (or (:id option) index)
               :style {:display "flex"
                       :flex-direction "column"
                       :gap "8px"
                       :padding "12px"
                       :border "1px solid #e0e0e0"
                       :border-radius "6px"
                       :background "#f9f9f9"
                       :cursor "grab"}}
         [:div {:style {:display "grid"
                        :grid-template-columns "1fr 1fr auto"
                        :gap "8px"
                        :align-items "center"}}
          [text-field/view {:value (:label option)
                            :placeholder "Label"
                            :on-change #(let [updated-options (assoc-in (:options state) [index :label] %)]
                                          (set-state (assoc state :options updated-options)))}]
          [text-field/view {:value (:value option)
                            :placeholder "Value" 
                            :on-change #(let [updated-options (assoc-in (:options state) [index :value] %)]
                                          (set-state (assoc state :options updated-options)))}]
          [button/view {:type :warning
                        :size :small
                        :on-click #(set-state (update state :options remove-nth index))}
           "Remove"]]
         [:div {:style {:display "grid"
                        :grid-template-columns "1fr 1fr"
                        :gap "8px"
                        :padding-top "8px"
                        :border-top "1px solid #e0e0e0"}}
          [:div
           [:label {:style {:display "block"
                            :margin-bottom "4px"
                            :font-weight "500"
                            :color "#666"
                            :font-size "11px"}}
            "Model:"]
           [text-field/view {:value (get-in option [:modifiers :model] "")
                             :placeholder "Model..."
                             :on-change #(let [updated-options (assoc-in (:options state) [index :modifiers :model] %)]
                                           (set-state (assoc state :options updated-options)))}]]
          [:div
           [:label {:style {:display "block"
                            :margin-bottom "4px"
                            :font-weight "500"
                            :color "#666"
                            :font-size "11px"}}
            "Camera:"]
           [text-field/view {:value (get-in option [:modifiers :camera] "")
                             :placeholder "Camera..."
                             :on-change #(let [updated-options (assoc-in (:options state) [index :modifiers :camera] %)]
                                           (set-state (assoc state :options updated-options)))}]]]])
      (:options state))]])

(defn min-max-value-control
  "Renders min/max value control with number/subscribe options"
  [state set-state field-key label-text default-num]
  [:div
   [:div
    [:label {:style {:display "block"
                     :margin-bottom "6px"
                     :font-weight "600"
                     :color "#555"
                     :font-size "14px"}}
     label-text]]
   [:div
    [:div {:style {:display "flex"
                   :align-items "center"
                   :gap "8px"
                   :margin-bottom "8px"}}
     [:label {:style {:display "flex"
                      :align-items "center"
                      :gap "4px"
                      :font-size "12px"
                      :color "#666"}}
      [:input {:type "radio"
               :name (str (name field-key) "-type-" (:id state))
               :checked (= (get-in state [field-key :type]) "num")
               :on-change #(set-state (assoc-in state [field-key :type] "num"))}]
      "Number"]
     [:label {:style {:display "flex"
                      :align-items "center"
                      :gap "4px"
                      :font-size "12px"
                      :color "#666"}}
      [:input {:type "radio"
               :name (str (name field-key) "-type-" (:id state))
               :checked (= (get-in state [field-key :type]) "subscribe")
               :on-change #(set-state (assoc-in state [field-key :type] "subscribe"))}]
      "Subscribe"]]
    (if (= (get-in state [field-key :type]) "num")
      [text-field/view {:type "number"
                        :value (str (get-in state [field-key :value]))
                        :on-change #(let [parsed (js/parseInt % 10)]
                                      (set-state (assoc-in state [field-key :value] (if (js/isNaN parsed) default-num parsed))))}]
      [text-field/view {:value (if (vector? (get-in state [field-key :value]))
                                 (clojure.string/join ", " (get-in state [field-key :value]))
                                 "")
                        :placeholder "field1, field2"
                        :on-change #(set-state (assoc-in state [field-key :value] 
                                                          (mapv clojure.string/trim 
                                                                (clojure.string/split % #","))))}])]])

(defn digit-input-configuration-view
  "Renders configuration interface for digit input type"
  [state set-state]
  [:div
   [:div {:style {:display "grid"
                  :grid-template-columns "1fr"
                  :gap "12px"}}
    [:div
     [:label {:style {:display "block"
                      :margin-bottom "6px"
                      :font-weight "600"
                      :color "#555"
                      :font-size "14px"}}
      "Default Value:"]
     [text-field/view {:type "number"
                       :value (str (:value state))
                       :on-change #(let [parsed (js/parseInt % 10)]
                                     (set-state (assoc state :value (if (js/isNaN parsed) 0 parsed))))}]]]
   
   [:div {:style {:display "grid"
                  :grid-template-columns "1fr 1fr"
                  :gap "12px"
                  :margin-top "16px"}}
    [min-max-value-control state set-state :min "Min Value:" 1]
    [min-max-value-control state set-state :max "Max Value:" 100]]])

(defn switch-configuration-view
  "Renders configuration interface for switch/toggle input type"
  [state set-state]
  [:div
   [:label {:style {:display "block"
                    :margin-bottom "6px"
                    :font-weight "600"
                    :color "#555"
                    :font-size "14px"}}
    "Default Value:"]
   [:label {:style {:display "flex"
                    :align-items "center"
                    :gap "12px"
                    :padding "12px"
                    :background "#ffffff"
                    :border-radius "8px"
                    :border "1px solid #e0e0e0"
                    :cursor "pointer"
                    :transition "all 0.2s ease"
                    :hover {:border-color "#007bff"}}}
    [:input {:type "checkbox"
             :checked (:value state)
             :on-change #(set-state (assoc state :value (-> % .-target .-checked)))
             :style {:width "18px"
                     :height "18px"
                     :accent-color "#007bff"
                     :cursor "pointer"}}]
    [:span {:style {:color "#495057"
                    :font-size "14px"
                    :font-weight "500"
                    :user-select "none"}}
     (if (:value state) "Enabled by default" "Disabled by default")]]])

(defn group-configuration-view
  "Renders configuration interface for group separator type"
  []
  [:div {:style {:text-align "center"
                 :padding "20px"
                 :background "#f8f9fa"
                 :border-radius "8px"}}
   [:p {:style {:color "#666"
                :font-size "14px"
                :margin "0"
                :font-style "italic"}}
    "This group acts as a visual separator. All inputs below it belong to this group until the next group is added."]])

(defn input-title-editor
  "Renders title editing interface"
  [state set-state]
  [:div {:style {:margin-bottom "16px"}}
   [:label {:style {:display "block"
                    :margin-bottom "6px"
                    :font-weight "600"
                    :color "#555"
                    :font-size "14px"}}
    "Title:"]
   [text-field/view {:value (or (:title state) "")
                     :placeholder "Enter input title..."
                     :on-change #(set-state (assoc state :title (clojure.string/trim %)))}]])

(defn input-condition-editor
  "Renders condition editing interface"
  [state set-state]
  [:div {:style {:margin-bottom "16px"}}
   [:label {:style {:display "block"
                    :margin-bottom "6px"
                    :font-weight "600"
                    :color "#555"
                    :font-size "14px"}}
    "Condition:"]
   [text-field/view {:value (or (:condition state) "")
                     :placeholder "Enter condition expression..."
                     :on-change #(set-state (assoc state :condition (clojure.string/trim %)))}]])

(defn input-prefix-editor
  "Renders prefix editing interface"
  [state set-state]
  [:div {:style {:margin-bottom "16px"}}
   [:label {:style {:display "block"
                    :margin-bottom "6px"
                    :font-weight "600"
                    :color "#555"
                    :font-size "14px"}}
    "Prefix:"]
   [text-field/view {:value (or (:prefix state) "")
                     :placeholder "Enter prefix text..."
                     :on-change #(set-state (assoc state :prefix (clojure.string/trim %)))}]])

(defn service-tuple->option
  "Converts a service tuple [id, data] to select option"
  [service-tuple]
  (let [service-id (first service-tuple)
        service-data (second service-tuple)
        service-name (:name service-data)]
    {:label (str service-name)
     :value (str service-id)}))

(defn build-service-options
  "Builds options array for service select"
  [services]
  (vec (concat
    [{:label "No Service" :value ""}]
    (map service-tuple->option services))))

(defn service-loading-view
  "Renders loading state for service selector"
  []
  [:div {:style {:color "#666"
                 :font-style "italic"
                 :padding "8px"}}
   "Loading services..."])

(defn service-select-view
  "Renders the service select dropdown"
  [state set-state services]
  (let [service-options (build-service-options services)
        service-id (or (:service_id state) "")
        current-value (if (empty? service-id)
                        nil
                        (first (filter #(= (:value %) service-id) service-options)))]
    [select/view {:value current-value
                  :placeholder "Select a service..."
                  :options service-options
                  :override {:style {:width "100%"
                                     :min-width "200px"}}
                  :dropdown-override {:style {:gap "4px"
                                              :display "flex"
                                              :flex-direction "column"
                                              :padding "8px"}}
                  :on-select (fn [selected-option]
                               (let [service-value (:value selected-option "")
                                     new-service-id (if (empty? service-value) "" service-value)]
                                 (set-state (assoc state :service_id new-service-id))))}]))

(defn modifiers-editor
  "Renders modifiers group with model and camera inputs"
  [state set-state]
  [:div {:style {:margin-bottom "16px"}}
   [:label {:style {:display "block"
                    :margin-bottom "12px"
                    :font-weight "600"
                    :color "#555"
                    :font-size "14px"}}
    "Modifiers:"]
   [:div {:style {:display "grid"
                  :grid-template-columns "1fr 1fr"
                  :gap "12px"
                  :padding "12px"
                  :background "#f0f0f0"
                  :border-radius "6px"
                  :border "1px solid #e0e0e0"}}
    [:div
     [:label {:style {:display "block"
                      :margin-bottom "6px"
                      :font-weight "500"
                      :color "#666"
                      :font-size "12px"}}
      "Model:"]
     [text-field/view {:value (get-in state [:modifiers :model] "")
                       :placeholder "Enter model..."
                       :on-change #(set-state (assoc-in state [:modifiers :model] %))}]]
    [:div
     [:label {:style {:display "block"
                      :margin-bottom "6px"
                      :font-weight "500"
                      :color "#666"
                      :font-size "12px"}}
      "Camera:"]
     [text-field/view {:value (get-in state [:modifiers :camera] "")
                       :placeholder "Enter camera..."
                       :on-change #(set-state (assoc-in state [:modifiers :camera] %))}]]]])

(defn service-selector
  "Renders service selection interface"
  [state set-state]
  (let [[services set-services] (react/useState [])
        [loading? set-loading!] (react/useState true)]
    
    (react/useEffect
      (fn []
        (request/pathom-with-workspace-id
          {:callback (fn [response]
                      (let [services-data (:workspace-services/get-services response)
                            services-js (js->clj services-data :keywordize-keys true)]
                        (when (sequential? services-js)
                          (set-services services-js))
                        (set-loading! false)))
           :query [:workspace-services/get-services]})
        js/undefined)
      #js[])
    
    [:div {:style {:margin-bottom "16px"}}
     [:label {:style {:display "block"
                      :margin-bottom "6px"
                      :font-weight "600"
                      :color "#555"
                      :font-size "14px"}}
      "Service:"]
     (if loading?
       [service-loading-view]
       [service-select-view state set-state services])]))

(defn input-configuration-content
  "Renders the configuration content based on input type"
  [state set-state]
  [:div {:style {:background "#f8f9fa"
                 :border-radius "8px"
                 :padding "16px"
                 :margin-top "8px"}}
   (case (:type state)
     "select" [select-configuration-view state set-state]
     "digit_input" [digit-input-configuration-view state set-state]
     "switch" [switch-configuration-view state set-state]
     "group" [group-configuration-view]
     nil)])

(defn input-configurator 
  "Renders configuration interface for form inputs with expand/collapse functionality"
  [input-data on-change on-remove]
  (let [[state set-state] (react/useState input-data)
        [expanded? set-expanded!] (react/useState false)
        is-group? (= (:type state) "group")]
    (react/useEffect
      (fn []
        (on-change state)
        js/undefined)
      #js[state])
    
    (if is-group?
      [group-separator-view state set-state on-remove]
      ;; Regular input item design  
      [:div {:style {:border "1px solid #e0e0e0"
                     :border-radius "12px"
                     :padding (if expanded? "20px" "12px")
                     :margin "12px 0"
                     :margin-left "20px"
                     :background "#ffffff"
                     :color "#000000"
                     :box-shadow "0 2px 8px rgba(0,0,0,0.1)"
                     :transition "all 0.2s ease"}}
       [input-header-view state expanded? set-expanded! on-remove]
       
       (when expanded?
         [:div {:style {:margin-top "16px"
                        :padding-top "16px"
                        :border-top "1px solid #f0f0f0"}}
          [input-title-editor state set-state]
          [input-condition-editor state set-state]
          [input-prefix-editor state set-state]
          [service-selector state set-state]
          (when-not (contains? #{"digit_input" "select"} (:type state))
            [modifiers-editor state set-state])
          [input-configuration-content state set-state]])])))

(defn form-builder 
  "Main form builder component for creating dynamic forms"
  [template on-template-change]
  (let [[inputs set-inputs] (react/useState template)]
    
    (react/useEffect
      (fn []
        (set-inputs template)
        js/undefined)
      #js[template])
    
    
    [:div {:style {:border "2px solid #007bff"
                   :border-radius "16px"
                   :padding "24px"
                   :margin "20px 0"
                   :background "linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)"
                   :box-shadow "0 4px 16px rgba(0,123,255,0.1)"}}
     [:h3 {:style {:margin "0 0 20px 0"
                   :color "#007bff"
                   :font-size "20px"
                   :font-weight "700"
                   :display "flex"
                   :align-items "center"
                   :gap "8px"}}
      "🔨 Form Builder"]
     
     [:div {:style {:background "#ffffff"
                    :padding "16px"
                    :border-radius "12px"
                    :border "1px solid #e0e0e0"
                    :margin-bottom "20px"}}
      [:label {:style {:display "block"
                       :margin-bottom "12px"
                       :font-weight "600"
                       :color "#555"
                       :font-size "14px"}}
       "Add Input:"]
      [:div {:style {:display "flex"
                     :gap "12px"
                     :flex-wrap "wrap"}}
       (for [input-type input-types]
         ^{:key (:value input-type)}
         [button/view {:type :primary
                       :on-click #(let [new-input (create-input-template (:value input-type))
                                        updated-inputs (conj inputs new-input)]
                                    (set-inputs updated-inputs)
                                    (on-template-change updated-inputs)
                                    (notification/toast! :success (str "Added " (:label input-type) " input")))}
          [:div {:style {:display "flex"
                         :align-items "center"
                         :gap "6px"}}
           [:i {:class (split-icon-classes (:icon input-type))}]
           (str "Add " (:label input-type))]])]]
     
     (when (seq inputs)
       [:div
        [:h4 {:style {:margin "20px 0 12px 0"
                      :color "#2c3e50"
                      :font-size "18px"
                      :font-weight "600"
                      :display "flex"
                      :align-items "center"
                      :gap "8px"}}
         "Configure Inputs:"]
        [:> (.-ReactSortable ReactSortable)
         {:list (clj->js (mapv #(assoc % :id (:id %)) inputs))
          :setList (fn [new-list]
                     (let [js-list (js->clj new-list :keywordize-keys true)]
                       (set-inputs js-list)
                       (on-template-change js-list)))
          :animation 200
          :delay 0
          :delayOnTouchStart true
          :style {:overflow-y "auto"
                  :padding-right "8px"}}
         (map (fn [input]
                ^{:key (:id input)}
                [:div {:data-id (:id input)
                       :style {:cursor "grab"}}
                 [input-configurator 
                  input
                  (fn [updated-input]
                    (let [updated-inputs (mapv #(if (= (:id %) (:id updated-input))
                                                  updated-input
                                                  %)
                                               inputs)]
                      (set-inputs updated-inputs)
                      (on-template-change updated-inputs)))
                  (fn [input-id]
                    (let [filtered-inputs (filterv #(not= (:id %) input-id) inputs)]
                      (set-inputs filtered-inputs)
                      (on-template-change filtered-inputs)))]])
              inputs)]])]))

(defn view 
  "Public API for the form builder component"
  ([template on-template-change]
   [form-builder template on-template-change]))