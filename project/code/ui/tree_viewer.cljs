(ns ui.tree-viewer
  (:require
   ["json-edit-react" :refer [candyWrapperTheme JsonEditor]]
   ["react" :as react]))

(defn indent [level]
  [:span (apply str (repeat level "  "))])

(defn colorize-value [value level]
  (cond
    (keyword? value) [:span {:style {:color "#B58900"}} (str value)]
    (string? value) [:span {:style {:color "#859900"}} (str "\"" value "\"")]
    (number? value) [:span {:style {:color "#268BD2"}} (str value)]
    (nil? value) [:span {:style {:color "#93A1A1"}} "nil"]
    (boolean? value) [:span {:style {:color "#CB4B16"}} (str value)]
    (map? value) [:div {:style {:margin-left (str (* level 12) "px")}}
                  [:span {:style {:color "#6C71C4"}} "{"]
                  (if (empty? value)
                    [:span {:style {:color "#6C71C4"}} "}"]
                    [:div
                     (for [[k v] value]
                       [:div
                        (colorize-value k (inc level))
                        [:span {:style {:color "#93A1A1"}} " "]
                        (colorize-value v (inc level))])
                     [:span {:style {:color "#6C71C4"}} "}"]])]
    (vector? value) [:div {:style {:margin-left (str (* level 12) "px")}}
                     [:span {:style {:color "#6C71C4"}} "["]
                     (if (empty? value)
                       [:span {:style {:color "#6C71C4"}} "]"]
                       [:div
                        (for [v value]
                          [:div
                           (colorize-value v (inc level))])
                        [:span {:style {:color "#6C71C4"}} "]"]])]
    (list? value) [:div {:style {:margin-left (str (* level 12) "px")}}
                   [:span {:style {:color "#6C71C4"}} "("]
                   (if (empty? value)
                     [:span {:style {:color "#6C71C4"}} ")"]
                     [:div
                      (for [v value]
                        [:div
                         (colorize-value v (inc level))])
                      [:span {:style {:color "#6C71C4"}} ")"]])]
    :else (str value)))

(defn back-to-eden [js-data]
  (js->clj js-data :keywordize-keys true))

(defn tree-viewer [{:keys [data on-change on-save]}]
  (let [[data-state set-data-state] (react/useState data)
        [has-changes set-has-changes] (react/useState false)]
    (react/useEffect
     (fn []
       (set-data-state data)
       (set-has-changes false))
     #js [data])
    
    [:div {:style {:width         "100%"
                   :display       "flex"
                   :flexDirection "column"
                   :alignItems    "center"}}
      [:> JsonEditor
        {:setData (fn [data]
                    (when on-change
                      (on-change (back-to-eden data)))
                    (when on-save
                      (on-save (back-to-eden data)))
                    
                    (set-data-state data)
                    (set-has-changes true))
         :data (clj->js data-state)
         :theme candyWrapperTheme
         :enableClipboard true
         :showErrorMessages true
         :rootFontSize 14}]]))
      ;; [:button {:style {:padding "12px 32px"
      ;;                   :backgroundColor (if has-changes "#B58900" "#666666")
      ;;                   :color "white"
      ;;                   :marginBottom "16px"
      ;;                   :border "none"
      ;;                   :borderRadius "8px"
      ;;                   :cursor (if has-changes "pointer" "not-allowed")
      ;;                   :fontSize "16px"
      ;;                   :fontWeight "600"
      ;;                   :transition "all 0.3s ease"}
      ;;           :on-click #(when has-changes
      ;;                        (on-save (back-to-eden data-state)
      ;;                          (set-has-changes false)))
      ;;           :disabled (not has-changes)}
      ;;   "Save"]]))
