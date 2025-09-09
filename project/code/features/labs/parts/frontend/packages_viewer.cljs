(ns features.labs.parts.frontend.packages-viewer
  (:require
   [app.frontend.request :as request]
   [features.labs.parts.frontend.package-card :as package-card]
   [reagent.core :as r]
   ["react" :as react]
   ["react-sortablejs" :as ReactSortable]))

(defn get-packages-by-category [packages category-id]
  (filter #(= (:category_id %) category-id) packages))

(defn reorder-packages [package-orders state]
  (request/pathom-with-workspace-id
   {:query `[(packages/reorder-packages! {:package-orders ~package-orders})]
    :callback (fn [response]
                (println "Reorder packages response:" response)
                ;; Reload packages for current category after reordering
                (when-let [load-fn (:load-packages-fn @state)]
                  (let [current-category-id (last (:breadcrumbs @state))]
                    (load-fn current-category-id))))}))

(defn initialize-package-order-positions []
  (request/pathom-with-workspace-id
   {:query '[(packages/initialize-package-order-positions! {})]
    :callback (fn [response]
                (println "Initialize package order positions response:" response))}))

(defn handle-package-sort [old-index new-index packages state]
  (when (not= old-index new-index)
    (let [reordered-packages (vec packages)
          moved-package (nth reordered-packages old-index)
          without-moved (vec (concat (take old-index reordered-packages)
                                     (drop (inc old-index) reordered-packages)))
          final-order (vec (concat (take new-index without-moved)
                                   [moved-package]
                                   (drop new-index without-moved)))
          package-orders (map-indexed (fn [idx package]
                                        {:id (:id package)
                                         :order-position idx})
                                      final-order)]
      (reorder-packages package-orders state))))

(defn view [{:keys [state]}]
  (let [current-parent-id (last (:breadcrumbs @state))
        packages (get-packages-by-category (:packages @state) current-parent-id)
        [local-packages set-local-packages] (react/useState packages)]
    
    ;; Update local state when packages change
    (react/useEffect 
     #(set-local-packages packages)
     #js [packages])
    
    [:> (.-ReactSortable ReactSortable)
     {:list (clj->js (mapv #(assoc % :id (:id %)) local-packages))
      :setList (fn [new-list]
                 (let [js-list (js->clj new-list :keywordize-keys true)]
                   (set-local-packages js-list)))
      :onEnd (fn [evt]
               (let [old-index (.-oldIndex evt)
                     new-index (.-newIndex evt)]
                 (when (not= old-index new-index)
                   (handle-package-sort old-index new-index local-packages state))))
      :animation 200
      :delay 0
      :delayOnTouchStart true
      :style {:display :grid 
              :grid-template-columns "1fr 1fr"
              :gap "20px"
              :padding "0 20px 20px 20px"}}
     (map (fn [package] 
            ^{:key (:id package)} 
            [:div {:data-id (:id package)
                   :style {:cursor "grab"}}
             [package-card/view {:state state
                                 :package package}]]) 
          local-packages)]))