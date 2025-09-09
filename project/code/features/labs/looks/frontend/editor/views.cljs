
(ns features.labs.looks.frontend.editor.views
  (:require
   ["@react-three/fiber" :as r3f]
   ["react"              :as react]
   [features.labs.looks.frontend.editor.control-panel :as control-panel]
   [features.labs.looks.frontend.editor.controls  :as controls]
   [features.labs.looks.frontend.editor.effects]
   [features.labs.looks.frontend.editor.model     :as model]
   [features.labs.looks.frontend.editor.scene     :as scene]
   [features.labs.looks.frontend.editor.settings  :as settings]
   [features.labs.looks.frontend.editor.thumbnail :as thumbnail-generator]
   [re-frame.core        :as r]
   [ui.link :as link]
   [ui.button :as button]
   [ui.floater    :as floater]
   [ui.tooltip :as tooltip]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn- texture-controls-panel-button [fullscreen?]
  (when fullscreen?
            
    [button/view {:on-click #(r/dispatch [:db/assoc-in [::panel] true])
                  :type :secondary
                  :style    {:position         "fixed"
                             :z-index          1000
                             :right            "15px"
                             :top              "15px"
                             :display          "flex"
                             :gap              "6px"
                             :align-items      "center"}}
                             
      [:i {:class ["fa-solid" "fa-chevron-left"]}]
      "Control Panel"]))

(defn- texture-controls-panel [fullscreen? inputs]
  [:<> 
    [texture-controls-panel-button fullscreen?]
    [floater/view {:orientation :right
                   :style {:z-index (when fullscreen? 1000)
                           :margin "16px"
                           :padding "0"
                           :background "var(--ir-secondary)"
                           :border-radius "12px"
                           :box-shadow "var(--ir-shadow-md)"}
                   :unmount-on-close? false
                   :config {:header false
                            :bg false
                            :disable-scroll true}
                   :state @(r/subscribe [:db/get-in [::panel] true])
                   :on-close #(r/dispatch [:db/assoc-in [::panel] false])}
      [:div {:style {:height "100%"
                     :overflow "auto"
                     :padding "20px"}}
        [:div {:style {
                       :margin-bottom   "20px"
                       :padding-bottom  "16px"
                       :border-bottom   "1px solid var(--ir-border-light)"}}
         
          [button/view {:on-click #(r/dispatch [:db/assoc-in [::panel] false])
                        :type :secondary
                        :style {:padding         "8px"
                                :width           "100%"
                                :display         "flex"
                                :align-items     "center"
                                :justify-content "space-between"
                                :gap             "8px"}}
            "Material Properties"
            [:i {:class ["fa-solid" "fa-chevron-right"]}]]]
        inputs]]])

(defn fullscreen-event-listeners []
  (react/useEffect
   (fn []
     (let [on-keydown (fn [^js e]
                        (let [kc (.-keyCode e)]
                          (cond
                            ;; Press "F"
                            (and (= kc 70) @(r/subscribe [:db/get ::focused]))
                            (r/dispatch [:db/assoc ::fullscreen? true])

                            ;; Press "ESC"
                            (= kc 27)
                            (r/dispatch [:db/assoc ::fullscreen? false]))))]
       (.addEventListener js/window "keydown" on-keydown)
       ;; Cleanup
       (fn []
         (.removeEventListener js/window "keydown" on-keydown))))
   #js []))


(defn- render [texture-props]
  (let [fullscreen? @(r/subscribe [:db/get-in [::fullscreen?] false])]
    (fullscreen-event-listeners)
    
    [:div {:id              "labs--canvas"
           :data-fullscreen fullscreen?
           :tab-index       -1
           :on-click        #(r/dispatch [:db/assoc-in [::focused] true])
           :on-blur         #(r/dispatch [:db/assoc-in [::focused] false])}
        
      [:button {:id       "labs--canvas-fullscreen-button" 
                :on-click #(r/dispatch [:db/assoc-in [::fullscreen?] (not fullscreen?)])}
        (if fullscreen?
          [:i {:class ["fa-solid" "fa-compress"]}]
          [:i {:class ["fa-solid" "fa-expand"]}])]
     
      [:> r3f/Canvas {"shadows"     true 
                      "performance" {"min" 0.5}
                      "gl"          {"preserveDrawingBuffer" true}
                      "style"       {:height "100%"
                                     :width "100%"
                                     :background "var(--ir-primary)"
                                     :min-height 0
                                     :min-width 0}}
       
       [scene/camera-control]
       
      ;;  [:> r3d/Sphere {"scale" [10 10 10]}
      ;;   [:meshBasicMaterial {"side" 2 "color" "#cfd2db"}]]
       
       [:> react/Suspense {:fallback nil}
         [scene/environment]]
       
       [:> react/Suspense {:fallback nil}
         [model/view {:texture-props texture-props
                      :model         {:src      "/labs/three_piece_rim.gltf"
                                      :rotation [(/ js/Math.PI 2) 0 0.8]}}]]]]))



(defn settings-header [texture-props]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "15px"}}
    [tooltip/view {:tooltip "Home"}
      [link/view {:href "/irunrainbow"
                  :color "var(--ir-primary)"
                    :mode :filled}   
          [:i {:class ["fa-solid" "fa-house"]}]]]
    
    "|"
    [tooltip/view {:tooltip "Lister"}
      [link/view {:href "/irunrainbow/looks"
                  :color "var(--ir-primary)"
                  :mode :filled}
        [:i {:class ["fa-solid" "fa-list"]}]]]
    [tooltip/view {:tooltip "Drafts"}]])
      

(defn create-look-button [texture-props]
  [button/view {:on-click #(r/dispatch [:labs.looks/create! texture-props])
                :color "var(--ir-primary)"
                :style {:width "100%"
                        :padding "10px"
                        :font-weight "600"
                        :font-size "14px"}}              
    "Create Look"])

(defn update-look-button [texture-props]
  [button/view {:on-click #(r/dispatch [:labs.looks/update! texture-props])
                :color "var(--ir-primary)"
                :style {:width "100%"
                        :padding "10px"
                        :font-weight "600"
                        :font-size "14px"}}
    "Update Look"])

(defn looks-settings [texture-props]
  [:div {:style {:display "flex"
                 :height "100%"       
                 :flex-direction "column"
                 :gap "16px"
                 :padding "16px"
                 :background "var(--ir-secondary)"
                 :border-right "1px solid var(--ir-border-light)"
                 :min-width "250px"
                 :max-width "250px"}}
   [settings-header texture-props]
   [settings/draft-list texture-props]
   
   [thumbnail-generator/view texture-props]
   [settings/look-name-field]
   [settings/color-family-select]
   [settings/price-group-select]
   [settings/coating-notes-field]
   [settings/tags-input-field]
   
   [create-look-button texture-props]
   [update-look-button texture-props]  
   [settings/model-change-select]])

(defn layout [] 
  (let [texture-props @controls/STATE
        fullscreen? @(r/subscribe [:db/get-in [::fullscreen?] false])]
    
    [:div {:style {:display               "grid"
                   :grid-template-columns "250px auto 250px"
                   :height                "100vh"
                   :background            "var(--ir-primary)"}}
      [looks-settings texture-props]
      [:div {:style {:overflow   "hidden"
                     :background "var(--ir-secondary)"}}
        [render texture-props]
        [settings/layers]]

      
      (if fullscreen?
        [texture-controls-panel fullscreen? [control-panel/view]]
        [control-panel/view])]))

(defn view []
  (react/useLayoutEffect
    (fn []
      (if-let [id @(r/subscribe [:db/get-in [:router :path-params :id]])]
        (r/dispatch [:labs.looks/get! id])
        (r/dispatch [:labs.looks.editor/init!]))
      (r/dispatch [:labs.looks.draft/get-suggestions! ""])
      (fn []))
    #js [])

  [:div {:id "looks-editor"}
    ;; [header/view]
    [:> react/Suspense {:fallback "Loading..."}
      (when @(r/subscribe [:db/get-in [:labs]])
        [layout])]])
