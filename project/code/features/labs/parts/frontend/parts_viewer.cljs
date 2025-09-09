(ns features.labs.parts.frontend.parts-viewer
  (:require
   [app.frontend.request :as request]
   [features.labs.parts.frontend.part-card :as part-card]
   ["react" :as react]
   ["react-sortablejs" :as ReactSortable]))

(defn get-all-parts-in-packages [parts]
  (set (mapcat :part_ids (filter #(= (:type %) "package") parts))))

(defn get-parts-by-category [parts category-id]
  (let [all-parts-in-packages (get-all-parts-in-packages parts)
        available-parts (->> parts
                           ;; When category-id is nil (root level), show parts without category_id
                           ;; When category-id is not nil, show parts with matching category_id
                           (filter #(if (nil? category-id)
                                     (nil? (:category_id %))
                                     (= (:category_id %) category-id)))
                           ;; Exclude parts that are already in packages
                           (filter #(not (contains? all-parts-in-packages (:id %))))
                           (sort-by :type #(case [%1 %2]
                                             ["package" "part"] -1
                                             ["part" "package"] 1
                                             0)))]
    available-parts))

(defn get-package-parts [parts package-id]
  (let [package-parts (filter #(= (:package_id %) package-id) parts)]
    (sort-by :order_position package-parts)))

(defn reorder-parts [part-orders state]
  (request/pathom-with-workspace-id
   {:query `[(parts/reorder-parts! {:part-orders ~part-orders})]
    :callback (fn [response]
                (println "Reorder parts response:" response)
                ;; Reload parts after reordering
                (when-let [load-parts-fn (:load-parts-fn @state)]
                  (load-parts-fn)))}))

(defn initialize-part-order-positions []
  (request/pathom-with-workspace-id
   {:query '[(parts/initialize-part-order-positions! {})]
    :callback (fn [response]
                (println "Initialize part order positions response:" response))}))

(defn handle-part-sort [old-index new-index parts state]
  (when (not= old-index new-index)
    (let [reordered-parts (vec parts)
          moved-part (nth reordered-parts old-index)
          without-moved (vec (concat (take old-index reordered-parts)
                                     (drop (inc old-index) reordered-parts)))
          final-order (vec (concat (take new-index without-moved)
                                   [moved-part]
                                   (drop new-index without-moved)))
          part-orders (map-indexed (fn [idx part]
                                     {:id (:id part)
                                      :order-position idx})
                                   final-order)]
      (reorder-parts part-orders state))))

(defn back-navigation [{:keys [state package-navigation-stack current-package-id]}]
  (when current-package-id
    (let [all-parts (:parts @state)
          parent-package-id (when (> (count package-navigation-stack) 1)
                              (nth package-navigation-stack (- (count package-navigation-stack) 2)))]
      [:div {:style {:display :flex
                     :align-items "center"
                     :gap "12px"
                     :padding "0 20px"}}
       [:button {:style {:border "none"
                         :background "none"
                         :cursor "pointer"
                         :color "#666"
                         :display "flex"
                         :align-items "center"
                         :gap "8px"}
                 :on-click (fn []
                             (if parent-package-id
                               ;; Go back to parent package
                               (swap! state update :package-navigation-stack pop)
                               ;; Go back to category view
                               (swap! state assoc :package-navigation-stack [])))}
         [:i {:class "fa-solid fa-arrow-left"
              :style {:font-size "14px"}}]
         (if parent-package-id
           (let [parent-package (first (filter #(= (:id %) parent-package-id) all-parts))]
             (str "Back to " (:name parent-package)))
           "Back to Parts")]])))

(defn sortable-parts-grid [{:keys [local-parts set-local-parts state]}]
  [:> (.-ReactSortable ReactSortable)
   {:list (clj->js (mapv #(assoc % :id (:id %)) local-parts))
    :setList (fn [new-list]
               (let [js-list (js->clj new-list :keywordize-keys true)]
                 (set-local-parts js-list)))
    :onEnd (fn [evt]
             (let [old-index (.-oldIndex evt)
                   new-index (.-newIndex evt)]
               (when (not= old-index new-index)
                 (handle-part-sort old-index new-index local-parts state))))
    :animation 200
    :delay 0
    :delayOnTouchStart true
    :style {:display :grid 
            :grid-template-columns "1fr 1fr"
            :gap "20px"
            :padding "0 20px 20px 20px"}}
   (map (fn [part] 
          ^{:key (:id part)} 
          [:div {:data-id (:id part)
                 :style {:cursor "grab"}}
           [part-card/view {:state state
                            :part part}]]) 
        local-parts)])

(defn regular-parts-grid [{:keys [parts state]}]
  [:div {:style {:display :grid 
                 :grid-template-columns "1fr 1fr"
                 :gap "20px"
                 :padding "0 20px 20px 20px"}} 
   (map (fn [part] 
          ^{:key (:id part)} 
          [part-card/view {:state state
                           :part part}]) 
        parts)])

(defn parts-content [{:keys [current-package-id local-parts set-local-parts parts state]}]
  (if current-package-id
    [sortable-parts-grid {:local-parts local-parts 
                          :set-local-parts set-local-parts 
                          :state state}]
    [regular-parts-grid {:parts parts 
                         :state state}]))

(defn view [{:keys [state]}]
  (let [current-parent-id (last (:breadcrumbs @state))
        package-navigation-stack (:package-navigation-stack @state)
        current-package-id (last package-navigation-stack)
        parts (if current-package-id
                (get-package-parts (:parts @state) current-package-id)
                (get-parts-by-category (:parts @state) current-parent-id))
        [local-parts set-local-parts] (react/useState parts)]
    
    ;; Update local state when parts change
    (react/useEffect 
     #(set-local-parts parts)
     #js [parts])
    
    [:div {:style {:display :flex
                   :flex-direction "column"
                   :gap "20px"}}
     [back-navigation {:state state 
                       :package-navigation-stack package-navigation-stack 
                       :current-package-id current-package-id}]
     [parts-content {:current-package-id current-package-id
                     :local-parts local-parts
                     :set-local-parts set-local-parts
                     :parts parts
                     :state state}]]))