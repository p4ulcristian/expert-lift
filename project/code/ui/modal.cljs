(ns ui.modal
  (:require [reagent.core :as r]))

(defn- modal-overlay-style []
  "Modal overlay background styles"
  {:position "fixed" :top 0 :left 0 :right 0 :bottom 0
   :background "rgba(0, 0, 0, 0.6)" :z-index 1000
   :display "flex" :align-items "center" :justify-content "center"
   :backdrop-filter "blur(4px)"
   :animation "fadeIn 0.2s ease-out"})

(defn- modal-content-style []
  "Modal content container styles"
  {:background "white" :padding "2rem" :border-radius "16px"
   :min-width "480px" :max-width "640px" :max-height "90vh" :overflow "auto"
   :box-shadow "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)"
   :transform "scale(1)" :animation "slideIn 0.2s ease-out"})

(defn modal-header
  "Modal header with title and optional subtitle"
  [{:keys [title subtitle]}]
  [:div {:style {:margin-bottom "2rem" :padding-bottom "1rem" :border-bottom "1px solid #e5e7eb"}}
   [:h3 {:style {:font-size "1.5rem" :font-weight "600" :color "#111827" :margin "0"}}
    title]
   (when subtitle
     [:p {:style {:color "#6b7280" :font-size "0.875rem" :margin "0.5rem 0 0 0"}}
      subtitle])])

(defn modal-footer
  "Modal footer with action buttons"
  [& buttons]
  [:div {:style {:display "flex" :gap "0.75rem" :margin-top "2.5rem" :padding-top "2rem"
                 :border-top "1px solid #e5e7eb" :justify-content "flex-end"}}
   (for [[index button] (map-indexed vector buttons)]
     ^{:key index} button)])

(defn modal
  "Reusable modal component with overlay, content, and optional close on backdrop click"
  [{:keys [on-close close-on-backdrop?]} & content]
  [:div {:style (modal-overlay-style)
         :on-click (when (and close-on-backdrop? on-close)
                    (fn [e] 
                      (when (= (.-target e) (.-currentTarget e))
                        (on-close))))}
   [:div {:style (modal-content-style)}
    (for [[index child] (map-indexed vector content)]
      ^{:key index} child)]])