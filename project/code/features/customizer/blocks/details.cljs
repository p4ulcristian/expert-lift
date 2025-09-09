(ns features.customizer.blocks.details
  (:require
    [features.customizer.blocks.details.effects]
    [re-frame.core :as r]
    ["react" :as react]
    [ui.floater :as floater]
    [ui.button :as button]
    [ui.accordion :as accordion]
    
    [features.common.form.frontend.reader :as form-reader]
    [features.customizer.panel.frontend.blocks.form.digit :as digit]
    [features.customizer.panel.frontend.blocks.form.group :as group]
    [features.customizer.panel.frontend.blocks.form.select :as select]
    [features.customizer.panel.frontend.blocks.form.switch :as switch]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn close-button []
  [button/view {:mode     :clear_2
                :on-click #(r/dispatch [:details.drawer/close!])
                :style    {:position  "absolute"
                           :right     "0"}}
    [:i {:class ["fa-solid" "fa-xmark"]
         :style {:font-size "20px"}}]])

(defn header []
  [:div {:style {:height "50px"
                 :display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :position "relative"
                 :font-size "20px"
                 :font-weight "600"}}
    [:p "Details"]
    [close-button]])

(defn title [edited-item]
  (let [package @(r/subscribe [:db/get-in [:customizer :packages (:package_id edited-item)]])]
    [:div {:style {:text-align "center"
                   :font-size  "20px"
                   :display    "flex"
                   :align-items "center"
                   :justify-content "center"
                   :margin-bottom "20px"}}
      [:p {:style {:font-weight "500"}}
        (when (< 1 (count (:children package)))
          (:name package))
        " " (:name edited-item)]]))

(defn package-form [cursor edited-item]
  (when-let [package @(r/subscribe [:db/get-in [:customizer :packages (:package_id edited-item)]])]
    (when-let [form-id (:form_id package)]
      [:div {:style {:margin-bottom "30px"}}
        [:div {:style {:margin-bottom "20px"
                       :padding-bottom "10px"
                       :border-bottom "1px solid rgba(255, 255, 255, 0.15)"}}
          [:h4 {:style {:font-size "15px"
                        :font-weight "700"
                        :color "rgba(255, 255, 255, 0.95)"
                        :margin "0"
                        :text-transform "uppercase"
                        :letter-spacing "0.8px"}}
            (:name package)]]
        ^{:key (:id package)}
        [form-reader/view form-id
          {:init-data  (get package "formdata")
           :value-path cursor
           :on-change  (fn [package-formdata]
                         (r/dispatch [:db/assoc-in [:customizer :packages (:package_id edited-item) "formdata"] package-formdata])
                         (r/dispatch [:customizer.package.price/calc!]))
           :clear?     false
           :inputs     {:select       select/view
                        :switch       switch/view
                        :digit_input  digit/view
                        :group        group/view}}]])))

(defn part-form [cursor {:keys [id form_id formdata] :as edited-item}]
  ^{:key id}
  [form-reader/view form_id
    {:init-data  formdata
     :value-path cursor
     :on-change  (fn [part-formdata]
                   (r/dispatch [:customizer.part.price/calc! edited-item]))
     :clear?     false
     :inputs     {:select      select/view
                  :switch      switch/view
                  :digit_input digit/view
                  :group       group/view}}])

(defn form-section [edited-item]
  (if edited-item
    (let [cursor @(r/subscribe [:db/get-in [:customizer :cursor]])]
      [:div {:style {:padding "20px 0"}}
        [:div {:style {:display        "flex"
                       :flex-direction "column"
                       :gap            "20px"}}
          [title edited-item]
          [package-form cursor edited-item]
          [part-form cursor edited-item]]])
    [:div {:style {:padding "20px 0"
                   :text-align "center"}}
      [:div {:style {:color "rgba(255, 255, 255, 0.9)"
                     :line-height "1.6"}}
        [:i {:class ["fa-solid" "fa-sliders"]
             :style {:font-size "48px"
                     :margin-bottom "15px"
                     :opacity "0.3"}}]
        [:p {:style {:font-size "16px"
                     :margin "0 0 10px 0"
                     :color "rgba(255, 255, 255, 0.9)"}}
          "No item selected"]
        [:p {:style {:font-size "14px"
                     :margin "0"
                     :color "rgba(255, 255, 255, 0.7)"}}
          "Please select a part first to see customization options"]]]))


(defn details-section [edited-item]
  (let [current-category @(r/subscribe [:db/get-in [:customizer/menu :selected]])
        navigation-path @(r/subscribe [:customizer.menu/path])]
    (cond 
      ;; Show selected item details (when editing a specific part/package)
      (and edited-item (not= edited-item false))
      (let [package-id (:package_id edited-item)
            parent-package (when package-id 
                            @(r/subscribe [:db/get-in [:customizer :packages package-id]]))]
        [:div {:style {:padding "20px 0"}}
          ;; Show parent package info if this is a part
          (when (and (= "part" (:type edited-item)) parent-package)
            [:div {:style {:margin-bottom "20px"
                           :padding-bottom "15px"
                           :border-bottom "1px solid rgba(255, 255, 255, 0.15)"}}
              [:h4 {:style {:color "#fff"
                            :margin-bottom "8px"
                            :font-size "16px"
                            :font-weight "600"}}
                (:name parent-package)]
              [:div {:style {:color "rgba(255, 255, 255, 0.8)"
                             :line-height "1.5"
                             :font-size "13px"}}
                (or (:description parent-package) "No package description available.")]])
          
          ;; Show current item details
          [:div
            [:h4 {:style {:color "#fff"
                          :margin-bottom "8px"
                          :font-size "16px"
                          :font-weight "600"}}
              (:name edited-item)]
            [:div {:style {:color "rgba(255, 255, 255, 0.9)"
                           :line-height "1.6"
                           :font-size "14px"}}
              (or (:description edited-item) "No description available for this item.")]]])
      
      ;; Show current category details (when actively browsing inside a category)
      (and current-category (seq navigation-path))
      [:div {:style {:padding "20px 0"}}
        [:h3 {:style {:color "#fff"
                      :margin-bottom "15px"
                      :font-size "18px"}}
          (:name current-category)]
        [:div {:style {:color "rgba(255, 255, 255, 0.9)"
                       :line-height "1.6"
                       :font-size "14px"}}
          (or (:description current-category) 
              "Browse our categories to find your item you want to have coated and enjoy the Iron Rainbow customization experience!")]]
      
      ;; Default welcome message
      :else
      [:div {:style {:padding "20px 0"
                     :text-align "center"}}
        [:div {:style {:color "rgba(255, 255, 255, 0.9)"
                       :line-height "1.6"}}
          [:h3 {:style {:color "#fff"
                        :margin-bottom "15px"
                        :font-size "18px"}}
            "Welcome to Iron Rainbow"]
          [:p {:style {:margin "0 0 10px 0"
                       :color "rgba(255, 255, 255, 0.8)"}}
            "Customize your products with our advanced 3D editor."]
          [:p {:style {:margin "0"
                       :color "rgba(255, 255, 255, 0.7)"
                       :font-size "13px"}}
            "Select a category to start browsing products."]]])))

(defn tab-button [tab-key current-tab label icon on-click]
  [button/view {:class    (str "details-tab-button " (when (= tab-key current-tab) "active"))
                :on-click on-click
                :style    {:flex "1"
                           :padding "10px 12px"
                           :border-radius "8px"
                           :background (if (= tab-key current-tab)
                                         "rgba(255, 255, 255, 0.2)"
                                         "rgba(255, 255, 255, 0.05)")
                           :color (if (= tab-key current-tab) "#fff" "rgba(255, 255, 255, 0.7)")
                           :border (if (= tab-key current-tab)
                                     "1px solid rgba(255, 255, 255, 0.3)"
                                     "1px solid rgba(255, 255, 255, 0.1)")
                           :display "flex"
                           :align-items "center"
                           :justify-content "center"
                           :gap "6px"
                           :min-height "36px"}}
    [:div {:style {:display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :width "16px"
                   :height "16px"}}
      [:i {:class [icon]
           :style {:font-size "11px"
                   :line-height "1"}}]]
    [:span {:style {:font-size "13px"
                    :font-weight "500"
                    :line-height "1"}}
      label]])

(defn tabs-header [current-tab set-tab]
  [:div {:style {:display "flex"
                 :gap "10px"
                 :margin-bottom "20px"
                 :padding "25px 15px 0 15px"}}
    [tab-button :details current-tab "Details" "fa-solid fa-file-lines" #(set-tab :details)]
    [tab-button :form current-tab "Properties" "fa-solid fa-sliders" #(set-tab :form)]])

(defn tab-content [current-tab edited-item]
  [:div {:id    "customizer--details-content"
         :class "hide-scroll"
         :style {:height         "100%"
                 :overflow-y     "auto"
                 :padding        "0 15px"}}
    (case current-tab
      :form [form-section edited-item]
      :details [details-section edited-item]
      [form-section edited-item])])

(defn content []
  (let [edited-item @(r/subscribe [:customizer/get-edited-item])
        [current-tab set-tab] (react/useState :details)]
    [:div {:style {:height         "calc(100% - 50px)"
                   :display        "flex"
                   :flex-direction "column"}}
      [tabs-header current-tab set-tab]
      [tab-content current-tab edited-item]]))

(defn view []
  (let [edited-item @(r/subscribe [:customizer/get-edited-item])]
    [floater/view {:orientation :right
                   :class       "floater customizer--floater-ui"
                   :state       @(r/subscribe [:db/get-in [:details.drawer/state] false])
                   :on-close    #(r/dispatch [:details.drawer/close!])
                   :config      {:bg false}}
                  ;;  :style       {:z-index   "100"}}
     
      [:<>
        [header]
        [content]]]))