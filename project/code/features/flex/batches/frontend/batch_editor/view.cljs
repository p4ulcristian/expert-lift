(ns features.flex.batches.frontend.batch-editor.view
  (:require [reagent.core :as r]
            [clojure.string]
            [zero.frontend.react :as zero-react]
            [ui.modals.zero :as modals]
            ["react-sortablejs" :as ReactSortable]
            [features.flex.batches.frontend.batch-editor.styles :as styles]
            [features.flex.batches.frontend.batch-editor.state :as state]
            [features.flex.batches.frontend.batch-editor.drag-drop :as drag-drop]
            [features.flex.batches.frontend.batch-editor.modals :as batch-modals]
            [features.flex.batches.frontend.batch-editor.components.process :as process]
            [features.flex.batches.frontend.batch-editor.components.recipe :as recipe]
            [features.flex.batches.frontend.batch-editor.components.batch :as batch]))

;; -----------------------------------------------------------------------------
;; ---- Default Data ---- (removed - data comes from backend)

;; -----------------------------------------------------------------------------
;; ---- Available Items Helper Components ----

(defn- filter-recipes-by-search [recipes search-term]
  "Filters recipes based on search term"
  (if (empty? search-term)
    recipes
    (filter #(clojure.string/includes? 
             (clojure.string/lower-case (:recipe/name %))
             (clojure.string/lower-case search-term))
           recipes)))

(defn- recipes-search-input [search-term set-search-term]
  "Search input component for recipes"
  [:input {:type "text"
           :placeholder "Search recipes..."
           :value search-term
           :on-change #(set-search-term (.. ^js % -target -value))
           :style {:width "100%"
                   :padding "8px"
                   :margin-bottom "10px"
                   :border "1px solid #dee2e6"
                   :border-radius "4px"
                   :font-size "14px"
                   :background-color "#ffffff"}}])

(defn- recipes-scrollable-list [filtered-recipes container-height]
  "Scrollable list of recipe components with sortable functionality"
  [:> (.-ReactSortable ReactSortable)
   {:list (clj->js (mapv #(assoc % :id (:recipe/id %)) filtered-recipes))
    :setList (fn [new-list] nil) ; Read-only for available recipes  
    :group {:name "shared" :pull "clone" :put false}
    :sort false
    :onClone (fn [evt] nil)
    :animation 200
    :style {:height (str (* container-height 0.4) "px")
            :overflow-y "auto"
            :background-color "#f8f9fa" 
            :border "2px dashed #dee2e6" 
            :border-radius "4px" 
            :padding "10px"}}
   (doall
    (map-indexed (fn [index recipe-item]
                   ^{:key (:recipe/id recipe-item)}
                   [:div {:data-id (:recipe/id recipe-item)
                          :data-type "recipe"}
                    [recipe/recipe-component recipe-item index]])
                 filtered-recipes))])

(defn available-recipes-section [available-recipes container-height]
  (let [[search-term set-search-term] (zero-react/use-state "")
        filtered-recipes (filter-recipes-by-search available-recipes search-term)]
    [:div {:style {:margin-bottom "20px"}}
     [recipes-search-input search-term set-search-term]
     [recipes-scrollable-list filtered-recipes container-height]]))

(defn- filter-processes-by-search [processes search-term]
  "Filters processes based on search term"
  (if (empty? search-term)
    processes
    (filter #(clojure.string/includes? 
             (clojure.string/lower-case (:process/name %))
             (clojure.string/lower-case search-term))
           processes)))

(defn- processes-search-input [search-term set-search-term]
  "Search input component for processes"
  [:input {:type "text"
           :placeholder "Search processes..."
           :value search-term
           :on-change #(set-search-term (.. ^js % -target -value))
           :style {:width "100%"
                   :padding "8px"
                   :margin-bottom "10px"
                   :border "1px solid #dee2e6"
                   :border-radius "4px"
                   :font-size "14px"
                   :background-color "#ffffff"}}])

(defn- processes-scrollable-list [filtered-processes container-height available-processes]
  "Scrollable list of process components with sortable functionality"
  [:> (.-ReactSortable ReactSortable)
   {:list (clj->js (mapv #(assoc % :id (:process/id %)) filtered-processes))
    :setList (fn [new-list] nil) ; Read-only for available processes
    :group {:name "shared" :pull "clone" :put false}
    :sort false
    :onClone (fn [evt] nil)
    :animation 200
    :style {:height (str (* container-height 0.4) "px")
            :overflow-y "auto"
            :background-color "#f8f9fa"
            :border "2px dashed #dee2e6" 
            :border-radius "4px" 
            :padding "10px"}}
   (doall
    (map-indexed (fn [index process-item]
                   ^{:key (:process/id process-item)}
                   [:div {:data-id (:process/id process-item)
                          :data-type "process"}
                    [process/process-component process-item index nil nil nil available-processes]])
                 filtered-processes))])

(defn available-processes-section [available-processes container-height]
  (let [[search-term set-search-term] (zero-react/use-state "")
        filtered-processes (filter-processes-by-search available-processes search-term)]
    [:div
     [processes-search-input search-term set-search-term]
     [processes-scrollable-list filtered-processes container-height available-processes]]))

;; -----------------------------------------------------------------------------
;; ---- Sidebar Components ----

(defn available-items-sidebar [available-recipes available-processes container-height]
  [:div {:style {:flex "0 0 300px"}}
   [available-recipes-section available-recipes container-height]
   [available-processes-section available-processes container-height]])

;; -----------------------------------------------------------------------------
;; ---- Batches Section Components ----

(defn batches-section [batches on-batches-change available-processes available-recipes container-height]
  [:div {:style {:flex 1}}
   [:div {:style {:height (str container-height "px")
                  :overflow-y "auto"
                  :gap "20px"}}
    (doall
     (map (fn [batch-item]
            ^{:key (:batch/id batch-item)}
            [batch/batch-component batch-item batches 
             on-batches-change
             #(batch-modals/open-split-modal! % batches on-batches-change)
             available-processes available-recipes])
          batches))]])

;; -----------------------------------------------------------------------------
;; ---- Main Layout Components ----

(defn batch-editor-content [batches available-recipes available-processes on-batches-change container-height]
  [:div {:style {:display "flex" :gap "20px"}}
   [available-items-sidebar available-recipes available-processes container-height]
   [batches-section batches on-batches-change available-processes available-recipes container-height]])

(defn batch-editor-container [batches available-recipes available-processes on-batches-change]
  (let [[container-height set-container-height] (zero-react/use-state 0)]
    
    ;; Calculate 80% of page height
    (zero-react/use-effect
     {:mount (fn []
               (let [calculate-height (fn []
                                        (let [window-height (.-innerHeight js/window)
                                              batches-height (* window-height 0.8)]
                                          (set-container-height batches-height)))
                     resize-listener (fn [] (calculate-height))]
                 
                 ;; Initial calculation
                 (calculate-height)
                 
                 ;; Add resize listener
                 (.addEventListener js/window "resize" resize-listener)
                 
                 ;; Cleanup function
                 (fn []
                   (.removeEventListener js/window "resize" resize-listener))))
      :deps []})
    
    [:div {:style {:padding "20px" :font-family "Arial, sans-serif"}
           :class "batch-editor-container"}
     [batch-editor-content batches available-recipes available-processes on-batches-change container-height]]))

(defn drag-drop-wrapper [batches available-processes available-recipes on-batches-change content]
  ;; This wrapper integrates with react-sortablejs for drag and drop functionality
  content)

;; -----------------------------------------------------------------------------
;; ---- State Management Hook ----

(defn use-batch-editor-state [initial-job-name initial-batches initial-available-processes initial-available-recipes]
  (let [job-name (r/atom (or initial-job-name "40 Rims - Black Powder Coating"))
        batches (r/atom initial-batches)
        available-processes (r/atom initial-available-processes)
        available-recipes (r/atom initial-available-recipes)]
    
    (zero-react/use-effect
     {:mount (fn []
               (when initial-job-name (reset! job-name initial-job-name))
               (when initial-batches (reset! batches initial-batches))
               (when initial-available-processes (reset! available-processes initial-available-processes))
               (when initial-available-recipes (reset! available-recipes initial-available-recipes)))
      :deps [initial-job-name initial-batches initial-available-processes initial-available-recipes]})
    
    {:job-name job-name
     :batches batches
     :available-processes available-processes
     :available-recipes available-recipes}))

;; -----------------------------------------------------------------------------
;; ---- Main Component ----

(defn batch-editor [{:keys [initial-job-name initial-batches initial-available-processes 
                            initial-available-recipes on-batches-change on-job-name-change
                            on-processes-change on-recipes-change]}]
  (let [state (use-batch-editor-state initial-job-name initial-batches 
                                      initial-available-processes initial-available-recipes)
        batches-change-handler (fn [new-batches]
                                 (reset! (:batches state) new-batches)
                                 (when on-batches-change (on-batches-change new-batches)))]
    [:div
     [styles/batch-editor-styles]
     [drag-drop-wrapper @(:batches state) @(:available-processes state) @(:available-recipes state) batches-change-handler
      [batch-editor-container @(:batches state) @(:available-recipes state) @(:available-processes state)
       batches-change-handler]]
     [modals/modals]]))

(def batch-editor-component batch-editor)