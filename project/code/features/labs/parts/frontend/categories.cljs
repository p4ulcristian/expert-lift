(ns features.labs.parts.frontend.categories
  (:require
   [app.frontend.request :as request]
   [features.labs.parts.frontend.breadcrumbs :as breadcrumbs]
   [features.labs.parts.frontend.categories-form :as categories-form]
   [features.labs.parts.frontend.category-card :as category-card]
   [features.labs.parts.frontend.package-form :as package-form]
   [features.labs.parts.frontend.packages-viewer :as packages-viewer]
   [features.labs.parts.frontend.part-form :as part-form]
   [features.labs.parts.frontend.parts-viewer :as parts-viewer]
   [features.labs.shared.frontend.components.header :as header]
   [reagent.core :as r]
   [zero.frontend.react :as zero-react]
   ["react" :as react]
   ["react-sortablejs" :as ReactSortable]))

;; State Management
(def state (r/atom {:categories []
                    :packages []
                    :parts []
                    :new-category {:name ""
                                 :description ""
                                 :picture nil}
                    :new-package {}
                    :new-part {}
                    :breadcrumbs []
                    :editing-category-id nil
                    :editing-package-id nil
                    :editing-part-id nil
                    :show-package-form false
                    :package-navigation-stack []
                    :load-packages-fn nil}))

(defn load-categories []
  (request/pathom-with-workspace-id
   {:query '[:categories/get-categories]
    :callback (fn [response]
                (when-let [categories (get-in response [:categories/get-categories])]
                  (swap! state assoc :categories categories)))}))

(defn load-parts []
  (request/pathom-with-workspace-id
   {:query '[:parts/get-parts]
    :callback (fn [response]
                (when-let [parts (get-in response [:parts/get-parts])]
                  (swap! state assoc :parts parts)))}))

(defn load-forms []
  (request/pathom-with-workspace-id
   {:query '[:forms/get-forms]
    :callback (fn [response]
                (when-let [forms (get-in response [:forms/get-forms])]
                  (swap! state assoc :forms forms)))}))

(defn load-packages-for-category [category-id]
  (request/pathom-with-workspace-id
   {:query '[:packages/get-packages]
    :callback (fn [response]
                (when-let [all-packages (get-in response [:packages/get-packages])]
                  (let [category-packages (filter #(= (:category_id %) category-id) all-packages)]
                    (swap! state assoc :packages category-packages))))}))

(defn get-categories-by-parent [categories parent-id]
  (filter #(= (:category_id %) parent-id) categories))

(defn reorder-categories [category-orders]
  (request/pathom-with-workspace-id
   {:query `[(categories/reorder-categories! {:category-orders ~category-orders})]
    :callback (fn [response]
                (println "Reorder response:" response)
                (load-categories))}))

(defn initialize-category-order-positions []
  (request/pathom-with-workspace-id
   {:query '[(categories/initialize-category-order-positions! {})]
    :callback (fn [response]
                (println "Initialize order positions response:" response)
                (load-categories))}))

(defn handle-category-sort [old-index new-index categories]
  (when (not= old-index new-index)
    (let [reordered-categories (vec categories)
          moved-category (nth reordered-categories old-index)
          without-moved (vec (concat (take old-index reordered-categories)
                                     (drop (inc old-index) reordered-categories)))
          final-order (vec (concat (take new-index without-moved)
                                   [moved-category]
                                   (drop new-index without-moved)))
          category-orders (map-indexed (fn [idx category]
                                         {:id (:id category)
                                          :order-position idx})
                                       final-order)]
      (reorder-categories category-orders))))

(defn categories-viewer [categories state]
  (let [current-parent-id (when (seq (:breadcrumbs @state))
                           (last (:breadcrumbs @state)))
        filtered-categories (get-categories-by-parent categories current-parent-id)
        [local-categories set-local-categories] (react/useState filtered-categories)]
    
    ;; Update local state when categories change
    (react/useEffect 
     #(set-local-categories filtered-categories)
     #js [filtered-categories])
    
    [:> (.-ReactSortable ReactSortable)
     {:list (clj->js (mapv #(assoc % :id (:id %)) local-categories))
      :setList (fn [new-list]
                 (let [js-list (js->clj new-list :keywordize-keys true)]
                   (set-local-categories js-list)))
      :onEnd (fn [evt]
               (let [old-index (.-oldIndex evt)
                     new-index (.-newIndex evt)]
                 (when (not= old-index new-index)
                   (handle-category-sort old-index new-index local-categories))))
      :animation 200
      :delay 0
      :delayOnTouchStart true
      :style {:display :grid 
              :grid-template-columns "1fr 1fr"
              :gap "20px"
              :padding "0 20px 20px 20px"}}
     (map (fn [category] 
            ^{:key (:id category)} 
            [:div {:data-id (:id category)
                   :style {:cursor "grab"}}
             [category-card/view {:state state
                                  :category category}]]) 
          local-categories)]))

(defn section-header [title & {:keys [action-button]}]
  [:div {:style {:margin "0 0 20px 0"
                 :padding "16px 24px"
                 :background "rgba(255, 255, 255, 0.1)"
                 :backdrop-filter "blur(10px)"
                 :border "1px solid rgba(255, 255, 255, 0.2)"
                 :border-radius "12px"
                 :box-shadow "0 8px 32px rgba(0, 0, 0, 0.1)"
                 :display "flex"
                 :justify-content "space-between"
                 :align-items "center"}}
   [:h2 {:style {:margin "0"
                 :color "var(--ir-text-primary)"
                 :font-size "24px"
                 :font-weight "600"
                 :text-shadow "0 2px 4px rgba(0, 0, 0, 0.1)"}}
    title]
   (when action-button action-button)])

(defn view []
  (zero-react/use-effect
   {:mount #(do 
             (load-categories)
             (load-parts)
             (load-forms)
             (swap! state assoc :breadcrumbs [])
             (swap! state assoc :editing-category-id nil)
             (swap! state assoc :editing-part-id nil)
             (swap! state assoc :load-packages-fn load-packages-for-category)
             (swap! state assoc :load-parts-fn load-parts))})
  (let [all-categories (:categories @state)
        has-breadcrumbs (seq (:breadcrumbs @state))
        current-parent-id (when has-breadcrumbs (last (:breadcrumbs @state)))
        filtered-categories (get-categories-by-parent all-categories current-parent-id)
        has-subcategories (seq filtered-categories)
        ;; Check if there are uncategorized parts at root level
        uncategorized-parts (when (not has-breadcrumbs)
                             (filter #(nil? (:category_id %)) (:parts @state)))
        has-uncategorized-parts (seq uncategorized-parts)
        ;; Check if we're viewing a package
        current-package-id (last (:package-navigation-stack @state))]
    [:<>
     [header/view]
     [:div {:style {:min-height "100vh"
                    :background "var(--ir-primary)"
                    :position "relative"
                    :overflow "hidden"}}
      ;; Background elements like dashboard
      [:div {:class "dashboard-background"}]
      [:div {:style {:position "relative"
                     :z-index "1"
                     :padding "40px"}}
       [breadcrumbs/view state]
       ;; Show categories section when not in a package
       (when (not current-package-id)
         [:<>
          [section-header "Categories" 
           :action-button [:div {:style {:display :flex :gap "10px"}}
                          [:button {:style {:padding "8px 16px"
                                            :background "rgba(255, 165, 0, 0.8)"
                                            :color "white"
                                            :border "none"
                                            :border-radius "6px"
                                            :cursor "pointer"
                                            :font-size "12px"}
                                    :on-click #(initialize-category-order-positions)}
                           "Fix Order Positions"]
                          [categories-form/add-category-header-button {:state state}]]]
          [categories-viewer all-categories state]])
       ;; Show packages section when inside a category (but not inside a package), parts section otherwise
       (cond
         ;; Inside a package - show parts
         current-package-id
         [:<>
          [section-header "Parts" 
           :action-button [:div {:style {:display :flex :gap "10px"}}
                          [:button {:style {:padding "8px 16px"
                                            :background "rgba(255, 165, 0, 0.8)"
                                            :color "white"
                                            :border "none"
                                            :border-radius "6px"
                                            :cursor "pointer"
                                            :font-size "12px"}
                                    :on-click #(parts-viewer/initialize-part-order-positions)}
                           "Fix Parts Order"]
                          [part-form/add-part-header-button {:state state}]]]
          [parts-viewer/view {:state state}]]
         
         ;; Inside a category (but not in a package) - show packages  
         has-breadcrumbs
         [:<>
          [section-header "Packages" 
           :action-button [:div {:style {:display :flex :gap "10px"}}
                          [:button {:style {:padding "8px 16px"
                                            :background "rgba(255, 165, 0, 0.8)"
                                            :color "white"
                                            :border "none"
                                            :border-radius "6px"
                                            :cursor "pointer"
                                            :font-size "12px"}
                                    :on-click #(packages-viewer/initialize-package-order-positions)}
                           "Fix Package Order"]
                          [package-form/add-package-header-button {:state state}]]]
          [packages-viewer/view {:state state}]]
         
         ;; At root level - don't show parts section (parts can only be added inside packages)
         :else
         nil)]]])) 