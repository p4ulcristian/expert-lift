(ns ui.modals.zero
  (:require
   ["framer-motion" :refer [motion]]
   ["react" :as react]
   [ui.button :as button]
   [zero.frontend.re-frame :as r]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(r/reg-sub :modals/open?
  (fn [db [id]]
    (get-in db [:modals id :open?])))

(r/reg-sub :modals/all
  (fn [db _]
    (get db :modals {})))

(r/reg-event-db
  :modals/add
  (fn [db [{:keys [id] :as modal-data}]]
    (assoc-in db [:modals id] modal-data)))

(r/reg-event-db
  :modals/open
  (fn [db [modal-id]]
    (assoc-in db [:modals modal-id :open?] true)))

(r/reg-event-db
  :modals/close
  (fn [db [modal-id]]
    (if (get-in db [:modals modal-id])
      (assoc-in db [:modals modal-id :open?] false)
      db)))

(r/reg-event-db
  :modals/remove
  (fn [db [modal-id]]
    (update db :modals dissoc modal-id)))


(defn- disable-pull-to-refresh! []
  (let [scroll-bar-width (- (.-innerWidth js/window)
                            (-> js/document .-documentElement .-clientWidth))]
    (.setAttribute (.querySelector js/document "html")
                   "style"
                   (str "overscroll-behavior: none;overflow: hidden;margin-right:" scroll-bar-width "px;"))))

(defn- enable-pull-to-refresh! []
  (.setAttribute (.querySelector js/document "html")
                 "style"
                 ""))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

(defn background [id opacity background-color]
  [:div {:class "modal-cover"
         :style (merge {:opacity opacity}
                      (when background-color {:background-color background-color}))
         :on-click #(r/dispatch [:modals/close id])}])

(defn header [{:keys [id label]}]
  [:div {:class "modal-header"}
    [:div {:style {:font-weight "bold"
                    :font-size "20px"}}
       label]

    [button/view {:type :secondary
                  :mode     :clear_2
                  :on-click #(r/dispatch [:modals/close id])
                  :style    {:padding      "0 12px"
                             :aspect-ratio "1"}}
      [:i {:class "fa-solid fa-xmark"}]]])

(defn modal-body [{:keys [opacity y content] :as modal-props}]
  [:div {:class "modal"
         :style {:opacity opacity
                 :transform (str "translateY(" y "px)")}}
   [header modal-props]
   content])

(defn modal [id {:keys [open? content label background-color]}]
  [(fn []
     (react/useEffect
      (fn []
        (if open?
          (disable-pull-to-refresh!)
          (enable-pull-to-refresh!))
        ;; Return cleanup function
        (fn []))
      #js [open?])

     [:> (.-div motion)
      {:initial {:opacity 0 :y -1000}
       :animate {:opacity (if open? 1 0)
                 :y (if open? 0 -1000)}
       :transition {:type "spring" :stiffness 120 :damping 15}
       :onAnimationComplete (fn []
                             (when (not open?)
                               (r/dispatch [:modals/remove id])))
       :style {:position "fixed"
               :top 0
               :left 0
               :width "100%"
               :height "100%"
               :z-index 1}}
      (when open?
        [:div {:class "modal-container"}
         [background id 1 background-color]
         [modal-body {:y 0
                     :opacity 1
                     :id id
                     :label label
                     :content content}]])])])
      

(defn modals []
  (let [modals @(r/subscribe [:modals/all])]
    [:<>
      (map (fn [[modal-k modal-v]]
             ^{:key modal-k}
             [modal modal-k modal-v])
           modals)]))