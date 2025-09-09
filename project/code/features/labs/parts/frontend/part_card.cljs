(ns features.labs.parts.frontend.part-card
  (:require [ui.card :as card]
            [features.labs.parts.frontend.part-form :as part-form]
            [features.labs.parts.frontend.utils :as utils]
            [zero.frontend.re-frame :refer [dispatch]]
            [clojure.string :as str]))

(defn type-icon [type]
  (case type
    "package" [:i {:class "fa-solid fa-box"
                   :style {:font-size "24px"
                           :color "#666"}}]
    [:i {:class "fa-solid fa-puzzle-piece"
         :style {:font-size "24px"
                 :color "#666"}}]))

(defn edit-button [{:keys [state part]}]
  [:button {:style {:position "absolute"
                    :top "12px"
                    :right "48px"
                    :background "rgba(255, 255, 255, 0.9)"
                    :border "1px solid #e0e0e0"
                    :border-radius "8px"
                    :width "32px"
                    :height "32px"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :cursor "pointer"
                    :transition "all 0.2s ease"
                    :box-shadow "0 2px 4px rgba(0, 0, 0, 0.1)"}
            :on-mouse-enter #(set! (-> % .-target .-style .-background) "#f8f9fa")
            :on-mouse-leave #(set! (-> % .-target .-style .-background) "rgba(255, 255, 255, 0.9)")
            :on-click (fn [e]
                       (.stopPropagation ^js e)
                       (swap! state assoc 
                              :editing-part-id (:id part)
                              :new-part part)
                       (dispatch [:modals/add {:id :add-part-modal
                                               :label "Edit Part"
                                               :content [part-form/add-part-modal {:state state}]
                                               :open? true}]))}
   [:i {:class "fa-solid fa-pen"
        :style {:font-size "12px"
                :color "#666"}}]])

(defn delete-button [{:keys [state part-id]}]
  [:button {:style {:position "absolute"
                    :top "12px"
                    :right "12px"
                    :background "rgba(255, 255, 255, 0.9)"
                    :border "1px solid #e0e0e0"
                    :border-radius "8px"
                    :width "32px"
                    :height "32px"
                    :display "flex"
                    :align-items "center"
                    :justify-content "center"
                    :cursor "pointer"
                    :transition "all 0.2s ease"
                    :box-shadow "0 2px 4px rgba(0, 0, 0, 0.1)"}
            :on-mouse-enter #(do
                              (set! (-> % .-target .-style .-background) "#fee")
                              (set! (-> % .-target .-style .-border-color) "#fcc"))
            :on-mouse-leave #(do
                              (set! (-> % .-target .-style .-background) "rgba(255, 255, 255, 0.9)")
                              (set! (-> % .-target .-style .-border-color) "#e0e0e0"))
            :on-click (fn [e]
                       (.stopPropagation ^js e)
                       (part-form/handle-part-delete state part-id))}
   [:i {:class "fa-solid fa-trash"
        :style {:font-size "12px"
                :color "#dc3545"}}]])

(defn part-name [{:keys [name type state part]}]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"}}
   (if (= type "package")
     [:div {:style {:display "flex"
                    :align-items "center"
                    :gap "8px"
                    :cursor "pointer"
                    :padding "4px 8px"
                    :border-radius "4px"
                    :background-color "#f8f9fa"
                    :border "1px solid #eee"
                    :transition "all 0.2s ease"
                    :hover {:background-color "#f0f2f5"
                            :border-color "#ddd"}}
            :on-click (fn [e]
                       (.stopPropagation ^js e)
                       (swap! state update :package-navigation-stack conj (:id part)))}
      [:h3 {:style {:margin "0"
                    :font-size "16px"
                    :color "#333"}}
       name]
      [:div {:style {:display "flex"
                     :align-items "center"
                     :gap "6px"}}
       [:i {:class "fa-solid fa-chevron-right"
            :style {:font-size "12px"
                    :color "#666"}}]]]
     [:h3 {:style {:margin "0 0 8px 0"
                   :font-size "18px"
                   :font-weight "600"
                   :color "#1a1a1a"
                   :line-height "1.3"}}
      name])])

(defn part-description [description]
  [:p {:style {:margin "0"
               :font-size "14px"
               :color "#6b7280"
               :line-height "1.5"
               :display "-webkit-box"
               :WebkitLineClamp "2"
               :WebkitBoxOrient "vertical"
               :overflow "hidden"}}
   (or description "No description available.")])

(defn part-type [type]
  [:div {:style {:display "flex"
                 :align-items "center"
                 :gap "8px"
                 :margin-top "8px"}}
   [type-icon type]
   [:span {:style {:font-size "14px"
                   :color "#666"}}
    (str/capitalize (or type "part"))]])

(defn part-picture [picture-url]
  (if picture-url
    [:div {:style {:width "100px"
                   :height "100px"
                   :border-radius "12px"
                   :overflow "hidden"
                   :box-shadow "0 4px 12px rgba(0, 0, 0, 0.1)"
                   :flex-shrink "0"}}
     [:img {:src picture-url
            :style {:width "100%"
                    :height "100%"
                    :object-fit "cover"}}]]
    [:div {:style {:width "100px"
                   :height "100px"
                   :background "linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)"
                   :border-radius "12px"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :border "2px dashed #cbd5e1"
                   :flex-shrink "0"}}
     [type-icon "part"]]))

(defn related-parts [{:keys [all-parts state package]}]
  (let [direct-parts (filter #(= (:package_id %) (:id package)) all-parts)]
    [:div {:style {:margin-top "12px"
                   :padding-top "12px"
                   :border-top "1px solid #eee"}}
     [:div {:style {:font-size "12px"
                    :color "#666"
                    :margin-bottom "8px"}}
      "Package Contents:"]
     [:div {:style {:display "flex"
                    :flex-wrap "wrap"
                    :gap "8px"}}
      (for [part direct-parts]
        ^{:key (:id part)}
        [:div {:style {:display "flex"
                       :align-items "center"
                       :gap "8px"
                       :padding "4px 8px"
                       :background-color "#f5f5f5"
                       :border-radius "4px"
                       :border "1px solid #eee"
                       :cursor "pointer"}
               :on-click (fn [e]
                          (.stopPropagation ^js e)
                          (swap! state assoc 
                                 :editing-part-id (:id part)
                                 :new-part part)
                          (dispatch [:modals/add {:id :add-part-modal
                                                  :label "Edit Part"
                                                  :content [part-form/add-part-modal {:state state}]
                                                  :open? true}]))}
         (if (:picture_url part)
           [:img {:src (:picture_url part)
                  :style {:width "24px"
                          :height "24px"
                          :object-fit "cover"
                          :border-radius "2px"}}]
           [:div {:style {:width "24px"
                          :height "24px"
                          :background-color "#eee"
                          :border-radius "2px"
                          :display "flex"
                          :align-items "center"
                          :justify-content "center"}}
            [:i {:class (if (= (:type part) "package")
                         "fa-solid fa-box"
                         "fa-solid fa-puzzle-piece")
                 :style {:font-size "12px"
                         :color "#999"}}]])
         [:span {:style {:font-size "12px"
                         :color "#333"}}
          (:name part)]])]]))

(defn view [{:keys [state part]}]
  [card/view
   {:style {:background "#ffffff"
            :border "1px solid #e5e7eb"
            :border-radius "16px"
            :transition "all 0.3s ease"
            :overflow "hidden"}
    :content
    [:div {:style {:padding "20px"
                   :position "relative"
                   :display "flex"
                   :gap "20px"
                   :align-items "flex-start"}}
     [edit-button {:state state :part part}]
     [delete-button {:state state
                     :part-id (:id part)}]
     [part-picture (:picture_url part)]
     [:div {:style {:flex "1"
                    :min-width "0"}}
      [part-name {:name (:name part)
                  :type (:type part)
                  :state state
                  :part part}]
      [part-description (:description part)]
      [part-type (:type part)]
      (when (= (:type part) "package")
        [related-parts {:package   part
                        :all-parts (:parts @state)
                        :state     state}])]]}])