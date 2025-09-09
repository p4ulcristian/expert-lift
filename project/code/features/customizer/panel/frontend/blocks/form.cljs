
(ns features.customizer.panel.frontend.blocks.form
  (:require
   [re-frame.core                  :as r]
   ["react"                        :as react]
   [ui.floater                     :as floater]
   [ui.button                      :as button]
   [ui.accordion                   :as accordion]
   
   [features.common.form.frontend.reader :as form-reader]

   [features.customizer.blocks.dialog                     :as dialog]
   [features.customizer.panel.frontend.blocks.form.digit  :as digit]
   [features.customizer.panel.frontend.blocks.form.group  :as group]
   [features.customizer.panel.frontend.blocks.form.select :as select]
   [features.customizer.panel.frontend.blocks.form.switch :as switch]))




;; -----------------------------------------------------------------------------
;; ---- Components ----


(defn form-dialog []
  [dialog/view {:state      @(r/subscribe [:db/get-in [:test]])
                :on-cancel  #(r/dispatch [:db/assoc-in [:test] false])
                :on-confirm #(do
                               (r/dispatch [:db/assoc-in [:test] false])
                               (r/dispatch [:customizer.menu/open!]))}
    [:<>
      [:p "Seems like you don't selected any part"]]])

(defn form-button [part-selected? edited-item]
  (let [drawer-open? @(r/subscribe [:db/get-in [:details.drawer/state] false])]
    [button/view {:class     "header--grow-button"
                  :on-click  #(r/dispatch [:details.drawer/open!])
                  :color    (if drawer-open? "var(--irb-clr)" "rgba(255, 255, 255, 0.1)")
                  :style    {:color (when drawer-open? "black")}}
      [:i {:class ["fa-solid" "fa-file-lines"]}]
      "Details"]))


(defn header [edited-item set-state]
  (let [package @(r/subscribe [:db/get-in [:customizer :packages (:package_id edited-item)]])]
    [:div {:style {:position        "relative"
                   :display         "flex"
                   :justify-content "center"
                   :align-items     "center"}}
      [:p {:style {:font-weight "500" :text-align "center"}}
        (when (< 1 (count (:children package)))
          (:name package))
        " " (:name edited-item)]
   
      [button/view {:on-click #(set-state false)
                    :mode     :clear_2
                    :style    {:position "absolute"
                               :right    0}}
        [:i {:class ["fa-solid" "fa-chevron-right"]}]]]))

(defn pacakge-form [cursor edited-item]
  (let [[open? set-o] (react/useState true)]
    (when-let [package  @(r/subscribe [:db/get-in [:customizer :packages (:package_id edited-item)]])]
      (when-let [form-id (:form_id package)]
        [accordion/view {:open? open?
                         :title (:name package)
                         :style {:background "rgba(0, 0, 0, 0.35)"}}
          ^{:key (:id package)}
          [form-reader/view form-id
            {:init-data  (get package "formdata")
             :value-path cursor ;[:customizer :packages (:package_id edited-item)]
             :on-change  (fn [package-formdata]
                           (r/dispatch [:db/assoc-in [:customizer :packages (:package_id edited-item) "formdata"] package-formdata])
                           (r/dispatch [:customizer.package.price/calc!]))
             :clear?     false
             :inputs     {:select       select/view
                          :switch       switch/view
                          :digit_input  digit/view
                          :group        group/view}}]]))))

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

(defn form [edited-item]
  (when edited-item
    (let [cursor @(r/subscribe [:db/get-in [:customizer :cursor]])]
      [:div {:id    "customizer--form"
             :class "hide-scroll"
             :style {:height         "100%"
                     :display        "flex"
                     :flex-direction "column"
                     :gap            "25px"
                     :overflow-y     "auto"}}
   
        [pacakge-form cursor edited-item]
        [part-form cursor edited-item]])))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [edited-item]
  (let [part-selected? (contains? edited-item :form_id)]
    [form-button part-selected? edited-item]))
