
(ns features.customizer.panel.frontend.blocks.form.select
  (:require
    [re-frame.core :as r]
    [ui.select :as select]))

;; -----------------------------------------------------------------------------
;; ---- Components ----

(r/reg-event-db
  ::on-select
  (fn [db [_ {:keys [modifier]}]]
    (assoc-in db (:path modifier) (:value modifier))))

(defn select [{:keys [id title value-path options description] :as props}]
  ^{:key id}
  [:div {:style {:width "100%"
                 :margin-bottom "15px"}}
    [:p {:style {:margin-bottom "10px"
                 :font-size     "14px"
                 :font-weight   "600"
                 :color "rgba(255, 255, 255, 0.9)"}} 
      title]
    [select/view (merge (dissoc props :title)
                   {:value             @(r/subscribe [:db/get-in value-path])
                    :on-select         #(r/dispatch [:db/assoc-in value-path %])
                    :override          {:style {:background "rgba(255, 255, 255, 0.1)"
                                                :color "white"
                                                :width "100%"
                                                :min-width "200px"
                                                "--hover-clr" "var(--irb-clr)"
                                                :border "1px solid rgba(255, 255, 255, 0.2)"
                                                :border-radius "8px"}}
                    :dropdown-override {:style {:background "rgb(49, 49, 49)"
                                                :border "1px solid rgba(255, 255, 255, 0.2)"
                                                :border-radius "8px"}}
                    :option            {:override {:style {"--bg-clr" "rgb(49, 49, 49)"
                                                           :color "white"}}}})]])

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [input-props]
  [select input-props])