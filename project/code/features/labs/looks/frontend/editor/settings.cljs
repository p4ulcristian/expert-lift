
(ns features.labs.looks.frontend.editor.settings
  (:require
   ["react" :as react]
   [re-frame.core :as r]
   [ui.autocomplete :as autocomplete]
   [ui.popup        :as popup]
   [ui.select       :as select]
   [ui.button       :as button]
   
   [clojure.string :as string]))

;; -----------------------------------------------------------------------------
;; ---- Name ----

(defn look-name-field []
  [autocomplete/view {:id         "labs--name-autocomplete"
                      :value       @(r/subscribe [:db/get-in [:labs :name]])
                      :on-change   #(r/dispatch [:db/assoc-in [:labs :name] %])
                      :options     @(r/subscribe [:db/get-in [:labs :name-suggestions]])
                      :on-type-ended #(r/dispatch [:labs.looks.name/get-suggestions! %1 %2])
                      :placeholder "Look name"
                      :style       {:width      "100%"
                                    :background "var(--ir-primary)"}}])

;; ---- Name ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Drafts ----

(defn draft-on-select-dialog [texture-props]
  [popup/view {:state    @(r/subscribe [:db/get-in [:labs.draft-on-select/popup] false])
               :on-close #(r/dispatch [:db/assoc-in [:labs.draft-on-select/popup] false])
               :style    {:background-color "var(--ir-secondary)"
                          :z-index 10}
               :cover    {:z-index 2}}
    [:div
      [:p {:style {:color "black"}} "Drafts"]
      [:div {:style {:display       "flex"
                     :align-items   "center"
                     :gap           "15px"
                     :margin-bottom "15px"}}
        [button/view {:color "var(--ir-primary)"
                      :style {:width "100%"}
                      :on-click #(do
                                   (r/dispatch [:labs.looks/get! (:id texture-props)])
                                   (r/dispatch [:db/assoc-in [:labs.draft-on-select/popup] false]))}
          "Yes"]
        [button/view {:color "var(--ir-primary)"
                      :style {:width "100%"}
                      :on-click #(r/dispatch [:db/assoc-in [:labs.draft-on-select/popup] false])}
          "No"]]]])

(defn draft-list [texture-props]
  (let [options    @(r/subscribe [:db/get-in [:labs :draft-suggestions]])
        draft-name @(r/subscribe [:db/get-in [:labs :draft-name] ""])]
    [:<>
      [draft-on-select-dialog texture-props]
      [autocomplete/view {:id             "labs--autocomplete"
                          :placeholder    "Drafts"
                          :value          draft-name
                          :on-change      #(r/dispatch [:db/assoc-in [:labs :draft-name] %])
                          :on-click       #(r/dispatch [:labs.looks.draft/get-suggestions! ""])
                          :on-select      #(r/dispatch [:labs.looks.draft/get! (:id %)])
                          :on-type-ended  #(r/dispatch [:labs.looks.draft/get-suggestions! %1 %2])
                          :option-label-f #(-> % :name)
                          :option-value-f #(-> % :name)
                          :options        options
                          :override {:no-option (fn [value]
                                                  [button/view {:mode     :clear
                                                                :disabled (empty? draft-name)
                                                                :style    {:width "100%"
                                                                           :text-align "center"}
                                                                :on-click #(r/dispatch [:labs.looks.draft/create! texture-props])}
                                                    "Add as new"])}}]]))

;; ---- Drafts ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Price group ----

(defn price-group-select []
  [select/view
    {:options        [{:label "Basic"   :value "basic"}
                      {:label "Basic +" :value "basic+"}
                      {:label "Pro"     :value "pro"}
                      {:label "Pro +"   :value "pro+"}]
     :placeholder    "Price Category"
     :value          @(r/subscribe [:db/get-in [:labs :price-group-key]])
     :on-select      #(r/dispatch [:db/assoc-in [:labs :price-group-key] (:value % %)])
     :override      {:style {:width "100%"}
                     :dropdown {:style {:background "var(--ir-primary)"
                                        :border "1px solid var(--ir-border-light)"
                                        :min-width "200px"}}
                     :option {:style {:color "black"
                                      :padding "8px 12px"
                                      :width "100%"
                                      :text-align "left"}}}
     :dropdown-override {:style {:display "flex"
                                 :flex-direction "column"
                                 :padding "4px"}}}])

;; ---- Price group ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Color family ----

(defn option-element [option-props _select-props] 
  [:div {:style {:position "relative"
                 :display "grid"
                 :place-items "center"}}
    [:div {:style {:background option-props
                   :height     "40px"
                   :width      "100%"}}]
    [:p {:style {:color option-props
                 :filter         "invert(1) grayscale(1) brightness(1.3) contrast(9000)"
                 :mix-blend-mode "luminosity"
                 :position       "absolute"}} 
      option-props]])

(defn label-element [props label-value]
  [:div {:style {:display "flex":gap "15px"}}
    (when-not (= label-value (:placeholder props))
      [:div {:style {:background label-value
                     :width      "15px"
                     :height     "15px"}}])
    [:b label-value]])

(defn color-family-select []
  [select/view {:multiple      false
                :options        ["black" "white" "gray" "red" "blue" "green" "yellow" "orange" "purple"]
                :value          @(r/subscribe [:db/get-in [:labs :color-family]])
                :on-select      #(r/dispatch [:db/assoc-in [:labs :color-family] %])
                :option-element option-element
                :label-element  label-element
                :placeholder    "Color Family"
                :override {:style {:width "100%"}
                           :input  {:style {:background "var(--ir-primary)"
                                            :border     "1px solid var(--ir-border-light)"}}
                           :dropdown {:style {:background "var(--ir-primary)"}
                                             :border      "1px solid var(--ir-border-light)"}}
                :option  {:override {:style {:color     "var(--ir-text-primary)"
                                             "--bg-clr" "white"}}}
                :dropdown-override {:style {:display "grid"
                                            :grid-template-columns "repeat(2, minmax(40px, 1fr))"
                                            :background "var(--ir-primary)"
                                            :flex-wrap "wrap"
                                            :padding "8px"}}}])

;; ---- Color family ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Tags ----

(defn remove-tag [tag-to-remove]
  (r/dispatch [:db/update-in [:labs :tags] #(vec (remove (fn [t] (= t tag-to-remove)) %))]))
    
(defn add-tag [new-tag]
  (r/dispatch [:db/update-in [:labs :tags] #(conj % new-tag)]))

(defn tags-input-field []
  (let [tags @(r/subscribe [:db/get-in [:labs :tags] []])
        [input-value set-input-value] (react/useState "")
        handle-key-down (fn [event]
                          (when (= (.-key event) "Enter")
                            (let [new-tag (clojure.string/trim input-value)]
                              (when (and (not-empty new-tag)
                                        (not (some #(= % new-tag) tags)))
                                (add-tag new-tag)
                                (set-input-value "")))))]
    
    [:div {:style {:display "flex"
                   :flex-direction "column"
                   :gap "10px"}}
      [:label {:style {:color "var(--ir-text-primary)"
                       :font-weight "500"}}
       "Tags"]
      
      ;; Tags display
      [:div {:style {:display "flex"
                     :flex-wrap "wrap"
                     :gap "8px"
                     :min-height "32px"
                     :padding "4px"
                     :border "1px solid var(--ir-border-light)"
                     :border-radius "4px"
                     :background "var(--ir-primary)"}}
        (map-indexed (fn [idx tag]
                       ^{:key (str "tag-" idx)}
                       [button/view {:mode     :filled
                                     :color    "var(--ir-secondary)"
                                     :on-click #(remove-tag tag)
                                     :style    {:display "flex"
                                                :align-items "center"
                                                :gap "4px"}}
                          tag
                          [:i {:class "fa-solid fa-times"}]])
                     tags)
        
        ;; Input field
        [:input {:value input-value
                 :on-change #(set-input-value (-> % .-target .-value))
                 :on-key-down handle-key-down
                 :placeholder "Add tag..."
                 :style {:border "none"
                         :outline "none"
                         :background "transparent"
                         :color "var(--ir-text-primary)"
                         :font-size "14px"
                         :min-width "80px"
                         :flex "1"}}]]]))

;; ---- Tags ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Coating Notes ----

(defn coating-notes-popup []
  (let [notes             @(r/subscribe [:db/get-in [:labs :coating-notes] ""])
        [state set-state] (react/useState notes)]
    (react/useEffect (fn []
                       (fn []
                         (set-state nil)))
                     #js[])
    
    [popup/view {:state    @(r/subscribe [:db/get-in [:labs.coating-notes/popup] false])
                 :on-close #(r/dispatch [:db/assoc-in [:labs.coating-notes/popup] false])
                 :style    {:background-color "var(--ir-secondary)"
                            :z-index 10}
                 :cover    {:z-index 2}}
     
      [:<>
        [:div {:style {:display         "flex"
                       :align-items     "center"
                       :justify-content "space-between"
                       :margin-bottom   "15px"}}
          [:p "Coating Notes"]
          [button/view {:color "var(--ir-text-primary)"
                        :mode  :clear_2
                        :on-click #(r/dispatch [:db/assoc-in [:labs.coating-notes/popup] false])}
           [:i {:class "fa-solid fa-xmark"}]]]
      
        [:textarea {:value       state
                    :on-change   #(set-state (-> % .-target .-value))
                    :placeholder "Coating Notes"
                    :style       {:width         "100%"
                                  :min-width     "300px"
                                  :height        "200px"
                                  :padding       "8px"
                                  :margin-bottom "15px"
                                  :color         "var(--ir-text-primary)"
                                  :background    "var(--ir-primary)"}}]
        [button/view {:style    {:width "100%"}
                      :on-click (fn []
                                  (r/dispatch [:db/assoc-in [:labs :coating-notes] state])
                                  (r/dispatch [:db/assoc-in [:labs.coating-notes/popup] false]))}
          "Save"]]]))

(defn coating-notes-field []
  [:<>
    (str @(r/subscribe [:db/get-in [:labs :coating-notes]]))
    [coating-notes-popup]
    [button/view {:type     :primary
                  :style    {:width "100%"}
                  :on-click #(r/dispatch [:db/assoc-in [:labs.coating-notes/popup] true])}
      "Coating Notes"]])
 
;; ---- Coating Notes ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Layers ----

;Name, MPN, URL

(defn layer-card [title idx]
  (let [layer              @(r/subscribe [:db/get-in [:labs :layers idx]])
        layers-suggestions @(r/subscribe [:db/get-in [:labs :layers-suggestions]])
        name-suggestions   (distinct (mapv :name layers-suggestions))
        mpn-suggestions    (distinct (mapv :mpn layers-suggestions))
        url-suggestions    (distinct (mapv :url layers-suggestions))]

    [:div {:style {:display        "flex"
                   :flex-direction "column"
                   :width          "100%"
                   :align-items    "center"
                   :gap            "15px"
                   :border         "1px solid var(--ir-border-light)"}}
       [:b title]
       [autocomplete/view {:value       (:name layer)
                           :placeholder "Name"
                           :id          (str "labs--layer-name-autocomplete-" idx)
                           :options     name-suggestions
                           :on-change   #(r/dispatch [:db/assoc-in [:labs :layers idx :name] %])
                           :style       {:padding "4px 8px"}}]
       [autocomplete/view {:value       (:mpn layer)
                           :placeholder "MPN"
                           :options     mpn-suggestions
                           :id          (str "labs--layer-mpn-autocomplete-" idx)
                           :on-change   #(r/dispatch [:db/assoc-in [:labs :layers idx :mpn] %])
                           :style       {:padding "4px 8px"}}]
       [autocomplete/view {:value       (:url layer)
                           :placeholder "URL"
                           :options     url-suggestions
                           :id          (str "labs--layer-url-autocomplete-" idx)
                           :on-change   #(r/dispatch [:db/assoc-in [:labs :layers idx :url] %])
                           :style       {:padding "4px 8px"}}]]))
                                        

(defn layers []
  (let [layers-data @(r/subscribe [:db/get-in [:labs :layers-suggestions] []])]
    (react/useEffect 
      (fn []
        (when (empty? layers-data)
          (r/dispatch [:labs.looks.layers/get-suggestions!]))
        (fn []))
      #js[layers-data])

   [:<>
     (when layers-data
       [:div {:style {:display "grid"
                      :grid-template-columns "1fr 1fr 1fr 1fr"
                      :gap     "15px"
                      :padding "15px"}}
         [layer-card "Layer 1" 0]
         [layer-card "Layer 2" 1]
         [layer-card "Layer 3" 2]
         [layer-card "Layer 4" 3]])]))


;; ---- Layers ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Model Change ----

(defn model-change-select []
  [select/view {:options        [{:label "Three Piece Rim"   :value "three_piece_rim"}
                                 {:label "Sphere" :value "sphere"}
                                 {:label "Cube"     :value "cube"}  
                                 {:label "Cylinder"   :value "cylinder"}
                                 {:label "Cone"   :value "cone"}
                                 {:label "Torus"   :value "torus"}]
                :override {:style {:margin-top "auto"}}
                :placeholder    "Model Change"
                :value          @(r/subscribe [:db/get-in [:labs :model]])
                :on-select      #(r/dispatch [:db/assoc-in [:labs :model] %])}])
                
;; ---- Model Change ----
;; -----------------------------------------------------------------------------