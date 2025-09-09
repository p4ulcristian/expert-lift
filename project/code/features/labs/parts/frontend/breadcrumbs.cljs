(ns features.labs.parts.frontend.breadcrumbs)

(defn get-item-name-and-type [categories packages parts item-id]
  (or 
    ;; Check if it's a category
    (when-let [category (first (filter #(= (:id %) item-id) categories))]
      {:name (:name category) :type :category})
    ;; Check if it's a package
    (when-let [package (first (filter #(= (:id %) item-id) packages))]
      {:name (:name package) :type :package})
    ;; Check if it's a part
    (when-let [part (first (filter #(= (:id %) item-id) parts))]
      {:name (:name part) :type :part})
    ;; Fallback
    {:name (str item-id) :type :unknown}))

(defn get-icon-for-type [type]
  (case type
    :category [:i {:class "fa-solid fa-folder" :style {:font-size "12px" :margin-right "4px"}}]
    :package [:i {:class "fa-solid fa-box" :style {:font-size "12px" :margin-right "4px"}}]
    :part [:i {:class "fa-solid fa-puzzle-piece" :style {:font-size "12px" :margin-right "4px"}}]
    [:i {:class "fa-solid fa-question" :style {:font-size "12px" :margin-right "4px"}}]))

(defn view [state]
  (let [category-crumbs (:breadcrumbs @state)
        package-crumbs (:package-navigation-stack @state)
        categories (:categories @state)
        packages (:packages @state)
        parts (:parts @state)
        ;; Combine all navigation items
        all-crumbs (concat category-crumbs package-crumbs)]
    [:div {:style {:padding "12px 20px"
                   :background "rgba(255, 255, 255, 0.1)"
                   :backdrop-filter "blur(10px)"
                   :border "1px solid rgba(255, 255, 255, 0.2)"
                   :border-radius "12px"
                   :margin-bottom "24px"
                   :display "flex"
                   :align-items "center"
                   :gap "12px"
                   :box-shadow "0 8px 32px rgba(0, 0, 0, 0.1)"}}
     ;; Home is always first
     [:i {:class "fa-solid fa-home"
          :style {:color "var(--ir-text-primary)"
                  :cursor "pointer"
                  :font-size "16px"
                  :transition "all 0.2s ease"}
          :on-click #(do
                       (swap! state assoc :breadcrumbs [])
                       ;; Clear packages when going to root
                       (swap! state assoc :packages []))
          :on-mouse-enter #(set! (-> % .-target .-style .-color) "var(--ir-accent-light)")
          :on-mouse-leave #(set! (-> % .-target .-style .-color) "var(--ir-text-primary)")}]
     
     ;; If we have any breadcrumbs, show them with separators
     (when (seq all-crumbs)
       [:<>
        [:span {:style {:color "var(--ir-text-secondary)"
                        :font-size "14px"}} "/"]
        (map-indexed
         (fn [idx crumb-id]
           (let [item-info (get-item-name-and-type categories packages parts crumb-id)
                 is-category (< idx (count category-crumbs))
                 is-package (>= idx (count category-crumbs))]
             [:<> {:key (str "crumb-" idx)}
              [:span {:style {:color "var(--ir-text-primary)"
                             :font-weight "500"
                             :cursor "pointer"
                             :transition "all 0.2s ease"
                             :padding "4px 8px"
                             :border-radius "6px"
                             :display "flex"
                             :align-items "center"}
                      :on-click (fn []
                                  (cond
                                    ;; Clicking on a category
                                    is-category
                                    (let [new-breadcrumbs (vec (take (inc idx) category-crumbs))]
                                      (swap! state assoc :breadcrumbs new-breadcrumbs)
                                      (swap! state assoc :package-navigation-stack [])
                                      ;; Load packages for the target category
                                      (when-let [load-packages-fn (:load-packages-fn @state)]
                                        (load-packages-fn crumb-id)))
                                    
                                    ;; Clicking on a package
                                    is-package
                                    (let [package-idx (- idx (count category-crumbs))
                                          new-package-stack (vec (take (inc package-idx) package-crumbs))]
                                      (swap! state assoc :package-navigation-stack new-package-stack))))
                      :on-mouse-enter #(set! (-> % .-target .-style .-background) "rgba(255, 255, 255, 0.1)")
                      :on-mouse-leave #(set! (-> % .-target .-style .-background) "transparent")}
               [get-icon-for-type (:type item-info)]
               (:name item-info)]
              (when (< idx (dec (count all-crumbs)))
                [:span {:style {:color "var(--ir-text-secondary)"
                                :font-size "14px"
                                :margin "0 4px"}} "/"])]))
         all-crumbs)])])) 