(ns features.customizer.panel.frontend.blocks.menu.effects
  (:require
   ["react-hot-toast" :as toast]
   [re-frame.core :as r]
   [reagent.core  :as reagent]))

;; -----------------------------------------------------------------------------
;; ---- Drawer ----

(r/reg-event-db
  :customizer.menu/open!
  (fn [db [_]]
    (assoc-in db [:customizer/menu :drawer] true)))

(r/reg-event-db
  :customizer.menu/close!
  (fn [db [_]]
    (assoc-in db [:customizer/menu :drawer] false)))

;; ---- Drawer ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Events ----

(r/reg-event-db
  :customizer.menu.title/set!
  (fn [db [_ title]]
    (assoc-in db [:customizer/menu :title] title)))

(r/reg-event-db
  :customizer.menu.path/put!
  (fn [db [_ id]]
    (update-in db [:customizer/menu :path] #(-> % vec (conj (keyword id) :children)))))

(r/reg-event-db
  :customizer.menu.path/pop!
  (fn [db [_]]
    (update-in db [:customizer/menu :path] #(->> % (drop-last 2) vec))))

(r/reg-event-db
  :customizer.menu.selected/set!
  (fn [db [_ {:keys [id] :as category-props}]]
    (assoc-in db [:customizer/menu :selected] (dissoc category-props :children))))

;; ---- Events ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-fx
  :customizer.menu/back!
  (fn [{:keys [db]} [_]]
    (let [path      (get-in db [:customizer/menu :path])
          prev-item (-> db (get-in [:customizer/menu :items])
                           (get-in (drop-last 3 path)))]

      {:dispatch-n [[:customizer.menu.path/pop!]
                    [:customizer.menu.selected/set! prev-item]
                    [:customizer.menu.title/set! (:name prev-item)]
                    [:db/assoc-in [:customizer :package-id] (:package-id prev-item (:id prev-item))]
                    [:customizer/clean!]
                    [:customizer.url/update-url!]]})))

(r/reg-event-fx
  :customizer.menu.title/click!
  ;; Only for to be able select package when a part is selected
  (fn [{:keys [db]} [_]]
    (let [prev-path      (vec (butlast (get-in db [:customizer/menu :path])))
          prev-item      (-> db (get-in [:customizer/menu :items])
                            (get-in prev-path))
          item-data      (get-in db [:customizer :packages (:id prev-item)])]
      {:dispatch-n [[:customizer.menu.selected/set! prev-item]
                    [:customizer.menu.title/set! (:name prev-item)]
                    [:customizer.url/update-url!]]})))

;; ---- Card effects ----

(r/reg-event-fx
  :customizer.menu.category/click!
  (fn [{:keys [db]} [_ {:keys [id name] :as category-props}]]
    {:dispatch-n [[:customizer.menu.selected/set! category-props]
                  [:customizer.menu.path/put! id]
                  [:customizer.menu.title/set! name]
                  [:customizer.url/update-url!]]}))
                  ;; (when-not (get-in db [::wathced] false)
                  ;;   [:customizer.missing-model/alert])]}))

(r/reg-event-fx
  :customizer.menu.package/click!
  (fn [{:keys [db]} [_ {:keys [id model_url name children] :as package-props}]]
    (let [children-bigger-than-one (< 1 (count children))]
      {:dispatch-n [[:customizer.menu.selected/set! package-props]
                    [:customizer.menu.title/set! name]

                    [:customizer.url/update-url!]
                    (when children-bigger-than-one
                      ;; Only navigate to the package if there are more than one part
                      [:customizer.menu.path/put! id])

                    [:customizer/load! package-props]
                    (if children-bigger-than-one
                      ;; Select the first part of the package
                      [:customizer.menu.part/click! (first (vals children))]
                      ;; Load first part of the package
                      [:customizer/load! (first (vals children))])]})))          

(r/reg-event-fx
 :customizer.menu.part/click!
 (fn [{:keys [db]} [_ {:keys [id model_url] :as part-props}]]
  {:dispatch-n [[:customizer.menu.selected/set! part-props]
                [:customizer.url/update-url!]
                [:customizer/load! part-props]]}))

(r/reg-event-fx
  :customizer.menu.card/click!
  (fn [{:keys [db]} [_ menu-item]]
    {:dispatch
      (case (:type menu-item)
        "package"  [:customizer.menu.package/click! menu-item]
        "part"     [:customizer.menu.part/click! menu-item]
        [:customizer.menu.category/click! menu-item])}))

;; ---- Card effects ----

(r/reg-event-fx
  :customizer.model/select-part!
  (fn [{:keys [db]} [_ id]]
    (when id
      (if-let [part-data (get-in db [:customizer :parts id])]
        (let [applied-look (:look part-data)
              looks-collection (get-in db [:customizer/looks :items])
              look-index (when applied-look
                           (first (keep-indexed #(when (= (:id %2) (:id applied-look)) %1) looks-collection)))
              dispatch-events [[:customizer.menu.part/click! part-data]]]
          {:dispatch-n (if applied-look
                         (-> dispatch-events
                             (conj [:db/assoc-in [:customizer :selected-look] applied-look])
                             (conj [:db/assoc-in [:customizer/looks :index] look-index]))
                         (-> dispatch-events
                             (conj [:db/dissoc-in [:customizer :selected-look]])
                             (conj [:db/assoc-in [:customizer/looks :index] 0])))})
        (let [package-id (get-in db [:customizer :package-id])
              part-data  (get-in db [:customizer :packages package-id :children (keyword id)])
              applied-look (:look part-data)
              looks-collection (get-in db [:customizer/looks :items])
              look-index (when applied-look
                           (first (keep-indexed #(when (= (:id %2) (:id applied-look)) %1) looks-collection)))
              dispatch-events [[:customizer.menu.part/click! part-data]]]
          {:dispatch-n (if applied-look
                         (-> dispatch-events
                             (conj [:db/assoc-in [:customizer :selected-look] applied-look])
                             (conj [:db/assoc-in [:customizer/looks :index] look-index]))
                         (-> dispatch-events
                             (conj [:db/dissoc-in [:customizer :selected-look]])
                             (conj [:db/assoc-in [:customizer/looks :index] 0])))})))))

(r/reg-event-fx
  :customizer.categories/close-on-mobile
  (fn [{:keys [db]} [_ {:keys [type]}]]
    (when (and (= "part" type)
               (>= 750 (get-in db [:x.environment :viewport-handler/meta-items :viewport-width])))
      {:dispatch [:customizer.menu/close!]})))

;; ---- Effects ----
;; -----------------------------------------------------------------------------


;; -----------------------------------------------------------------------------
;; ---- Popular Items Effects ----


(defn find-path
  "Find the path to a given ID in the tree structure.
   Returns a vector of IDs representing the path from root to the target node.
   Returns nil if the ID is not found."
  [tree target-id]
  (letfn [(search-node [node path]
            (if (= (:id node) target-id)
              (conj path (:id node))
              (some #(search-node % (-> path vec (conj (keyword (:id node)) :children)))
                    (vals (:children node)))))]
    (some #(search-node % []) (vals tree))))

(r/reg-event-fx
  :customizer.menu.popular/click!
  (fn [{:keys [db]} [_ {:keys [id model_url type] :as popular-item}]]
    
    (let [menu-state   [:e9d7464e-fd5d-4449-ba56-fd657834084b :children :d50ee0e1-b2ec-48b6-a6cc-c1f925e8f575 :children :cbd74c42-cfa7-43f7-a3da-e412edcdbb9e :children]];(find-path (get-in db [:customizer/menu :items]) id)
 
      (if (= "package" type)
        {:dispatch-n [[:db/assoc-in [:customizer/menu :path] menu-state]
                      [:customizer.menu.selected/set! (dissoc popular-item :package)]
                      [:customizer/load! (dissoc popular-item :package)]]}
        
        (let [package (:package popular-item)]
          {:dispatch-n [[:db/assoc-in [:customizer/menu :path] menu-state]
                        [:customizer.menu.part/click! (dissoc popular-item :package)]
                        [:db/assoc-in [:customizer :packages (:id package)] package]

                        [:customizer.categories/close-on-mobile]]})))))
                      

;; ---- Popular Items Effects ----
;; -----------------------------------------------------------------------------

(defn- message []
  [:div {:class    "gradient-border"
         :on-click #(.dismiss toast/toast)
         :style    {:font-size      "0.85rem"
                    :cursor         "pointer"
                    :padding        "8px 10px"
                    :background     "rgb(64 64 64 / 85%)"
                    :border-radius  "12px"
                    :max-width      "400px"
                    :margin         "55px 15px"
                    :box-shadow     "0px 0px 15px 0px var(--irb-clr), inset 0px 0px 10px 0px var(--irb-clr)"}}
    "Don't worry, although we haven't uploaded a 3D model for this part, you can still book coating services for it!"])

(r/reg-event-fx
  :customizer.missing-model/alert
 (fn [{:keys [db]} [_]]
    {:dispatch-n [[:notifications/notify!
                     :custom
                     (reagent/as-element [message])
                     #js{:duration 5000
                         :position "top-center"}]
                  [:db/assoc-in [::wathced] true]]}))   

;; -----------------------------------------------------------------------------
;; ---- Menu State Builder ----

(defn- find-part [parts target-id]
  (first (filter #(= (:id %) target-id) parts)))

(defn- find-category-by-id [categories id]
  (first (filter #(= (:id %) id) categories)))

(defn- build-category-history [categories target-id]
  (letfn [(build-path [id acc]
            (if-let [category (find-category-by-id categories id)]
              (if-let [parent-id (:parent_id category)]
                (build-path parent-id (conj acc id))
                (conj acc id))
              acc))]
    (vec (reverse (build-path target-id [])))))

(defn- build-part-history [categories parts target-id]
  (letfn [(find-parent-part [part-id]
            (first (filter #(some #{part-id} (:part_ids %)) parts)))
          (build-parts-path [id acc]
            (if-let [part (find-part parts id)]
              (if-let [parent (find-parent-part id)]
                (build-parts-path (:id parent) (conj acc id))
                (conj acc id))
              acc))]
    (if-let [part (find-part parts target-id)]
      (let [parts-history (vec (reverse (build-parts-path target-id [])))
            category-id   (:category_id part)
            menu-history  (if category-id
                           (build-category-history categories category-id)
                           [])
            ;; Remove target-id from history if it's type "part"
            final-history (if (= "part" (:type part))
                           (vec (remove #{target-id} parts-history))
                           parts-history)]
        {:menu-history menu-history
         :parts-history final-history})
      {:menu-history []
       :parts-history []})))

;; ---- Menu State Builder ----
;; -----------------------------------------------------------------------------

;; ... existing code ...   