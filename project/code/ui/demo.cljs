
(ns ui.demo
  (:require
   ["react" :as react]
   [re-frame.core :as r]
   [ui.button     :as button]
   [ui.floater :as floater]
   [ui.popup   :as popup]
   [ui.select  :as select]
   [ui.table      :as table]
   [ui.text-field :as text-field]))

;; -----------------------------------------------------------------------------
;; ---- Button Demo ----

(defn buttons-demo []
  [:div {:style {:display "grid" :gap "15px" :border "1px solid gray" :border-radius "6px" :padding "15px"}}
    [:b "Buttons"]
    [:div
     [button/view {} "Default"]]
  
    [:p "Clear"]
    [:div {:style {:display "flex" :flex-wrap "wrap" :gap "15px"}}
      [button/view {:mode :clear :type :primary :disabled true} "Primary"]
      [button/view {:mode :clear :type :secondary} "Secondary"]
      [button/view {:mode :clear :type :warning} "Warning"]
      [button/view {:mode :clear :type :success} "Success"]
      [button/view {:mode :clear :override {:style {"--clr" "purple"}}} "Custom"]]
   
    [:p "Clear 2"]
    [:div {:style {:display "flex" :flex-wrap "wrap"  :gap "15px"}}
      [button/view {:mode :clear_2 :type :primary :disabled true} "Primary"]
      [button/view {:mode :clear_2 :type :secondary} "Secondary"]
      [button/view {:mode :clear_2 :type :warning} "Warning"]
      [button/view {:mode :clear_2 :type :success} "Success"]
      [button/view {:mode :clear_2 :override {:style {"--clr" "purple"}}} "Custom"]]
   
    [:p "Underline"]
    [:div {:style {:display "flex" :flex-wrap "wrap"  :gap "15px"}}
      [button/view {:mode :underlined :type :primary :disabled true} "Primary"]
      [button/view {:mode :underlined :type :secondary} "Secondary"]
      [button/view {:mode :underlined :type :warning} "Warning"]
      [button/view {:mode :underlined :type :success} "Success"]
      [button/view {:mode :underlined :override {:style {"--clr" "purple"}}} "Custom"]]
 
    [:p "Outlined"]
    [:div {:style {:display "flex" :flex-wrap "wrap"  :gap "15px"}}
      [button/view {:mode :outlined :type :primary :disabled true} "Primary"]
      [button/view {:mode :outlined :type :secondary} "Secondary"]
      [button/view {:mode :outlined :type :warning} "Warning"]
      [button/view {:mode :outlined :type :success} "Success"]
      [button/view {:mode :outlined :override {:style {"--clr" "purple"}}} "Custom"]]
   
    [:p "Filled"]
    [:div {:style {:display "flex" :flex-wrap "wrap"  :gap "15px"}}
      [button/view {:type :primary :disabled true} "Primary"]
      [button/view {:type :secondary} "Secondary"]
      [button/view {:type :warning} "Warning"]
      [button/view {:type :success} "Success"]
      [button/view {:override {:style {"--clr" "purple"}}} "Custom"]]
    
    [:p "With icon"]
    [:div {:style {:display "flex" :flex-wrap "wrap"  :gap "15px"}}
      [button/view {:type :primary :disabled false :color "purple"}
        [:div {:style {:display "flex" :gap "8px" :align-items "center"}}
          [:i {:class "fas fa-folder-plus" :style {:font-size "16px"}}]
          "Create Folder"]]
      [button/view {:type :secondary} 
        [:div {:style {:display "flex" :gap "8px" :align-items "center"}}
          "Share"
          [:i {:class "fas fa-share"}]]]

      [button/view {:type :warning}
        [:i {:class "fas fa-trash"}]]
          
      [button/view {:type :surccess} 
        [:i {:class "fas fa-folder-plus"}]]]])

;; ---- Button Demo ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Text-field Demo ----

(def rules
  [{:test #(empty? %) :msg "Input cannot be empty"}
   {:test #(not (re-matches #"[a-zA-Z0-9]+" %)) :msg "Only letters and numbers allowed"}
   {:test #(> (count %) 10) :msg "Input is too long (max 10 chars)"}])

(def email-rules
  [{:test #(empty? %) :msg "Input cannot be empty"}
   {:test #(not (re-matches #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$" %))
    :msg  "Not valid email!"}])

(defn text-fields-demo []
  [:div {:style {:display "grid" :gap "15px" :border "1px solid gray" :border-radius "6px" :padding "15px"}}
    [:b "Text-fields"]
    [text-field/view {:value         @(r/subscribe [:db/get-in [:name] ""])
                      :on-change     #(r/dispatch [:db/assoc-in [:name] %])
                      :on-type-ended #(r/dispatch [:db/assoc-in [:name] %])
                      :on-enter      #(println "Name:" %)
                      :label           "name"

                      :left-adornment  [:i {:class "fas fa-hand-point-right"}]
                      :right-adornment [:i {:class "fas fa-hand-point-left"}]

                      ;; :rules           rules
                      :rules-timeout   200
                      :override        {:placeholder "name"}}]
                      
          
    [text-field/view {:value          @(r/subscribe [:db/get-in [:email] ""])
                      :on-change      #(r/dispatch [:db/assoc-in [:email] %])
                      :on-type-ended  #(r/dispatch [:db/assoc-in [:email] %])
                      :on-enter       #(println "email:" %)
                      :label          "email"
                      :placeholder    "email"
                      :disabled       (empty? @(r/subscribe [:db/get-in[:name] ""]))

                      :left-adornment [:i {:class "fas fa-envelope"}]
                      :rules          email-rules
                      :rules-timeout   200
                      :override       {:required true}}]])

;; ---- Text-field Demo ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Select ----

(def options [
              {:label "Option 1" :value :option-1}
              {:label "Option 2" :value :option-2}
              {:label "Option 3" :value :option-3}
              {:label "Option 4" :value :option-4}
              {:label "Option 5" :value :option-5}
              {:label "Option 6" :value :option-6}
              {:label "Option 7" :value :option-7}
              {:label "Option 8" :value :option-8}
              {:label "Option 9" :value :option-9}
              {:label "Option 10" :value :option-10}])

(defn select-demo []
  [:div {:style {:background "white" :display "grid" :gap "15px" :border "1px solid gray" :border-radius "6px" :padding "15px"}} 
    [:b "Select"]
    ;; (str @(r/subscribe [:db/get-in [:popovers]]))
    [:div {:id :select.div/test}]
    [select/view {:value       @(r/subscribe [:db/get-in [:select :value]])
                  ;; :on-select   #(r/dispatch [:db/assoc-in [:select :value] %])
                  :on-select   #(r/dispatch [:db/assoc-in [:select :value] %])
                                            
                  :multiple    false
                  ;; :placeholder "placeholder"
                  :options     options}]
    [select/view {:value     @(r/subscribe [:db/get-in [:select-2 :value]])
                  :mobile? true
                  :on-select #(r/dispatch [:db/assoc-in[:select-2 :value] %])
                  :options   ["Alma" "Barack" "Banán"]}]])

;; ---- Select ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Table Demo ----

(defonce data [{:id 1 :name "Alice" :age 25 :city "New York" :email "alice@example.com"}
               {:id 2 :name "Bob" :age 30 :city "San Francisco" :email "bob@example.com"}
               {:id 3 :name "Charlie" :age 22 :city "Chicago" :email "charlie@example.com"}])

(defonce columns [{:key :id :label "ID"}
                  {:key :name :label "Name"}
                  {:key :age :label "Age"}
                  {:key :city :label "City"}
                  {:key :email :label "Email"}])

(defn column [column-props]
  [:b {:style {:text-align "left"}} (:label column-props)])

(defn row [_index {:keys [id name age city email]}]
  [:<>
    [:p id]
    [:p name]
    [:p age]
    [:p city]
    [:p email]])
 
(defn table-demo [_storage-data]
  [:div {:style {:display "grid" :gap "15px" :border "1px solid gray" :border-radius "6px" :padding "15px"}}
    [:b "Table"]
    [table/view {:layout  nil
                 :data    data
                 :columns columns}
      column
      row]])

;; ---- Table Demo ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Popover ----

(defn popover-example []
  [:div {:style {:background    "white"
                 :padding       "8px"
                 :border-radius "6px"
                 :border        "1px solid gray"
                 :width "100%"}}
    "popover-example"])

(def positions
  [[:top :center :bottom]
   [:left :center :right]])

(defn popover-demo []
  (let [[y set-y] (react/useState :top)
        [x set-x] (react/useState :left)
        [a-y set-ay] (react/useState :top)
        [a-x set-ax] (react/useState :left)
        [inherit? set-inherit] (react/useState false)]
    [:div {:style {:display "grid" :gap "15px" :border "1px solid gray" :border-radius "6px" :padding "15px"}}
     [:b "Popover"]
     [:b "Position"]
     [:div {:style {:display "flex" :gap "15px"}}
       [:select {:on-change #(set-y (-> ^js % .-target .-value keyword))}
         [:option {:value :top}    "Top"]
         [:option {:value :center} "Center"]
         [:option {:value :bottom} "Bottom"]]
       [:select {:on-change #(set-x (-> ^js % .-target .-value keyword))}
         [:option {:value :left}    "Left"]
         [:option {:value :center} "Center"]
         [:option {:value :right} "Right"]]]
     [:b "Anchor"]
     [:div {:style {:display "flex" :gap "15px"}}
       [:select {:on-change #(set-ay (-> ^js % .-target .-value keyword))}
         [:option {:value :top}    "Top"]
         [:option {:value :center} "Center"]
         [:option {:value :bottom} "Bottom"]]
       [:select {:on-change #(set-ax (-> ^js % .-target .-value keyword))}
         [:option {:value :left}    "Left"]
         [:option {:value :center} "Center"]
         [:option {:value :right} "Right"]]]
     
     [:div
       [:input {:id "inherit-width" :type "checkbox" :on-change #(set-inherit not) :value inherit?}]
       [:label {:for "inherit-width"} "Inherit element width"]]
     [button/view
       {:on-click (fn [event]
                    (r/dispatch [:popover/open :popover-demo-id
                                  {:content [select-demo]
                                   :event event
                                   :target (.-target ^js event) 
                                   :align   [y x]
                                   :width   (when inherit? :inherit)
                                   :anchor  [a-y a-x]}]))}
                  
      
      
       "Popover"]]))

;; ---- Popover ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Floater Demo ----

(defn floater-demo []
  [:div {:style {:display "grid" :gap "15px" :border "1px solid gray" :border-radius "6px" :padding "15px"}}
    [:b "Floater"]
   
    [button/view {:on-click #(r/dispatch [:db/assoc :state true])}
      "Open Floater"]
    [floater/view {:orientation :bottom
                   :style       {:z-index 100 :margin "15px"}
                   :config      nil
                   :state       @(r/subscribe [:db/get :state false])
                   :on-close    #(r/dispatch [:db/assoc :state false])}
     [:div {:style {:width "300px"}}
      [:div "Floater content"]]]])

;; ---- Floater Demo ----
;; -----------------------------------------------------------------------------
    
;; -----------------------------------------------------------------------------
;; ---- Popup Demo ----

(defn popup-demo []
  [:div {:style {:display "grid" :gap "15px" :border "1px solid gray" :border-radius "6px" :padding "15px"}}
    [:b "Popup"]
    [button/view {:on-click #(r/dispatch [:db/assoc :test :open])} "Open Popup"]
    [popup/view {:state    @(r/subscribe [:db/get-in [:test] false])
                 :position :center
                 :on-close #(r/dispatch [:db/assoc-in [:test] false])}
                            
      [:div {:style {:background-color "white"}}
        [:div {:style {:margin-bottom "15px"}}
          [text-field/view {:value-path [:surface-name] 
                            :placeholder "Look name"}]]

        [button/view {:on-click #()}
            "Save"]]]])


;; ---- Popup Demo ----
;; -----------------------------------------------------------------------------

(defn view []
  [:<>
   [:div {:style {:display "grid" :gap "15px" :margin "50px 15px"}}
    [buttons-demo]
    [text-fields-demo]
    [select-demo]
    [table-demo]
    [popover-demo]
    [floater-demo]
    [popup-demo]]])
   
  ;;  [popover-manager/view]])
   
