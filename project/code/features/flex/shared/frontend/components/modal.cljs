(ns features.flex.shared.frontend.components.modal
  (:require
   ["framer-motion" :refer [motion]]
   ["react-dom" :as react-dom]
   [re-frame.core :as r]
   [reagent.core :as reagent]
   [ui.button :as button]))

(r/reg-sub :flex-modal/data
  (fn [db _]
    (get db :flex-modal)))

(r/reg-event-db :flex-modal/show
  (fn [db [_ modal-data]]
    (assoc db :flex-modal (assoc modal-data :open? true))))

(r/reg-event-db :flex-modal/close
  (fn [db _]
    (dissoc db :flex-modal)))

(defn show!
  "Shows a flex modal with the given id, label and content"
  [id label content]
  (r/dispatch [:flex-modal/show {:id id
                                 :label label
                                 :content content}]))

(defn close!
  "Closes the flex modal"
  []
  (r/dispatch [:flex-modal/close])
  (r/dispatch [:db/dissoc-in [:ui :service-areas :show-reservation-modal?]]))

(defn modal-header [label]
  [:div {:style {:padding "20px"
                 :border-bottom "1px solid #e5e7eb"
                 :display "flex"
                 :justify-content "space-between"
                 :align-items "center"}}
   [:h2 {:style {:margin 0
                 :font-size "1.25rem"
                 :font-weight "600"
                 :color "#1f2937"}} label]
   [button/view {:type :secondary
                 :mode :clear_2
                 :on-click close!
                 :style {:padding "8px"
                         :aspect-ratio "1"}}
    [:i {:class "fa-solid fa-xmark"}]]])

(defn modal-content [content]
  [:div {:style {:padding "20px"}} content])

(defn modal-card [label content]
  [:div {:style {:background "white"
                 :border-radius "12px"
                 :box-shadow "0 20px 60px rgba(0, 0, 0, 0.3)"
                 :max-width "90%"
                 :max-height "90%"
                 :display "flex"
                 :flex-direction "column"
                 :pointer-events "auto"}
         :on-click (fn [e] (.stopPropagation ^js e))}
   [modal-header label]
   [:div {:style {:overflow "auto"
                  :flex "1"}}
    [modal-content content]]])

(defn modal-container [label content]
  [:div {:style {:display "flex"
                 :justify-content "center"
                 :align-items "center"
                 :height "100%"}}
   [modal-card label content]])

(defn clickaway-overlay []
  [:div {:style {:position "fixed"
                 :top 0
                 :left 0
                 :right 0
                 :bottom 0
                 :background-color "rgba(0, 0, 0, 0.5)"
                 :z-index 1000}
         :on-click close!}])

(defn positioning-overlay [label content]
  [:> (.-div motion)
   {:initial {:opacity 0}
    :animate {:opacity 1}
    :exit {:opacity 0}
    :transition {:duration 0.2}
    :style {:position "fixed"
            :top "60px"
            :left "240px"
            :right 0
            :bottom 0
            :pointer-events "none"
            :z-index 1001}}
   [modal-container label content]])

(defn modal-overlay [{:keys [label content]}]
  [:<>
   [clickaway-overlay]
   [positioning-overlay label content]])


(defn view
  "Renders the flex modal if open"
  []
  (let [modal-data @(r/subscribe [:flex-modal/data])]
    (when (:open? modal-data)
      (.createPortal react-dom
                     (reagent/as-element [modal-overlay modal-data])
                     (.-body js/document)))))