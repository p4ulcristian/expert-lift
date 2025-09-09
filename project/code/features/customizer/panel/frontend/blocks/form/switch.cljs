
(ns features.customizer.panel.frontend.blocks.form.switch
  (:require 
    [ui.button :as button]
    [re-frame.core :as r]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn switch [{:keys [id prefix value-path value title]}]
  (let [switched? (not (nil? @(r/subscribe [:db/get-in value-path])))]
    [:div {:key id
           :style {:margin-bottom "15px"}}
      [button/view {:class         ""
                    :style         {:background (if switched? "var(--irb-clr)" "rgba(255, 255, 255, 0.1)")
                                    :color      (if switched? "black" "white")
                                    :width "100%"
                                    :border (if switched? 
                                              "1px solid var(--irb-clr)" 
                                              "1px solid rgba(255, 255, 255, 0.2)")
                                    :border-radius "8px"
                                    :padding "12px"}
                    :on-click      #(if switched?
                                      (r/dispatch [:db/assoc-in value-path nil])
                                      (r/dispatch [:db/assoc-in value-path value]))}
        title]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [input-props]
  [switch input-props])