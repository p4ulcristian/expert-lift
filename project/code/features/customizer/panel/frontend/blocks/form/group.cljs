
(ns features.customizer.panel.frontend.blocks.form.group
  (:require
    ["react"       :as react]
    [re-frame.core :as r]
    [ui.accordion :as accordion]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn my-group-header [props]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :padding "8px 15px"
                 :border-bottom "1px solid"}}
    [:p (str (:title props))]
    [:button {:on-click (fn [] (r/dispatch [:x.ui/remove-popup! ::group]))
              :style {:position "absolute" :right "8px"}}
     [:i {:class ["fa-solid" "fa-xmark"]}]]])

(defn group-description [description-data]
  (let [[open? set-open] (react/useState false)]
    [:p {:style {:max-width   "580px"
                 :white-space "pre-line"}} 
      (str description-data)]))

(defn my-group-body [props content]
 [:div {:style {:display        "flex"
                :flex-direction "column"
                :align-items    "center"
                :gap            "15px"
                :padding        "0 6px"
                :width          "100%"}} 
  (into [:<>] content)])
   ;[group-description (:description props)]])
   

(defn my-group-sheet [{:keys [id title] :as props} content]
  ;; [shared.components/sheet {:title       [:p (str title)]
  ;;                           :state       @(r/subscribe [:x.db/get-item [id] :close])
  ;;                           :on-minimize #(r/dispatch [:x.db/set-item! [id] :close])
  ;;                           :on-close    #(r/dispatch [:x.db/set-item! [id] :close])
  ;;                           :on-open     #(r/dispatch [:x.db/set-item! [id] :open])}
   
    ;;  [my-group-header props]
     [my-group-body props content])
   

(defn group-old [{:keys [id] :as props} content]
  [:<> 
    [my-group-sheet props content]
    [:div 
      [:button  {:class "effect--bg-grow"
                 :on-click #(r/dispatch [:x.db/set-item! [id] :open])
                 :style {:border-color  "var(--irb-clr)"
                         "--clr"        "#00D4CA"
                         :background    "rgba(0, 0, 0, 0.35)"
                         :width         "100%"
                         :border-radius "25px"
                         :padding       "5px 8px"
                         :text-overflow "ellipsis"
                         :white-space   "nowrap"
                         :overflow      "hidden"
                         :display       "flex"
                         :align-items   "center"
                         :border        "1px solid rgb(0, 212, 202)"
                         :box-shadow    "rgba(12, 255, 183, 0.56) 0px 0px 4px 2px"
                         :justify-content "center"
                         :gap             "5px"}}
                       
          (:title props)]]])

(defn group [props content]
  [:div {:style {:width "100%"
                 :margin-bottom "30px"}}
    [:div {:style {:margin-bottom "20px"
                   :padding-bottom "10px"
                   :border-bottom "1px solid rgba(255, 255, 255, 0.15)"}}
      [:h4 {:style {:font-size "15px"
                    :font-weight "700"
                    :color "rgba(255, 255, 255, 0.95)"
                    :margin "0"
                    :text-transform "uppercase"
                    :letter-spacing "0.8px"}}
        (:title props)]]
    [my-group-body props content]])

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [input-props content]
  ^{:key (:id input-props)}
  [group input-props content])