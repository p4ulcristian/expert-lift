
(ns features.common.form.frontend.inputs.switch
  (:require
   [re-frame.core :as r]))  

;; -----------------------------------------------------------------------------
;; ---- Utils ----

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn- switch [{:keys [id value-path title label value on-change disabled?]}]
  
  (let [checked @(r/subscribe [:db/get-in value-path])]
    [:div {:style {:display     "flex"
                   :align-items "center"
                   :gap         "12px"
                   :padding     "8px 0"}}
     [:label {:style {:font-size   "14px"
                      :font-weight "500"
                      :color       "#333"
                      :cursor      (when (not disabled?) "pointer")}
              :on-click #(when (and (not disabled?) on-change)
                           (r/dispatch [:db/assoc-in value-path (not checked)]))}
      (or title label "Switch")]
     
     [:div {:style {:position "relative"
                    :width "44px"
                    :height "24px"
                    :background-color (if checked "#007bff" "#ccc")
                    :border-radius "12px"
                    :cursor (if disabled? "not-allowed" "pointer")
                    :transition "background-color 0.2s ease"
                    :opacity (if disabled? 0.6 1)}
            :on-click #(when (and (not disabled?) on-change)
                         (r/dispatch [:db/assoc-in value-path (not checked)]))}
      [:div {:style {:position "absolute"
                     :top "2px"
                     :left (if checked "22px" "2px")
                     :width "20px"
                     :height "20px"
                     :background-color "white"
                     :border-radius "50%"
                     :box-shadow "0 2px 4px rgba(0,0,0,0.2)"
                     :transition "left 0.2s ease"}}]]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [config]
  [switch config])