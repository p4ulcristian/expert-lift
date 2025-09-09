(ns features.flex.batches.frontend.batch-editor.view
  (:require [reagent.core :as r]
            [clojure.string]
            [router.frontend.zero :as router]
            [zero.frontend.react :as zero-react]
            [zero.frontend.re-frame :as rf]
            [ui.modals.zero :as modals]
            [ui.button :as button]
            ["react-sortablejs" :as ReactSortable]))

;; -----------------------------------------------------------------------------
;; ---- Default Data ----

(def default-batches
  [{:id "batch-1"
    :name "Batch 1"
    :quantity 40
    :partType "rims"
    :partIcon "🚗"
    :processes []}])

(def default-available-processes
  [{:id "process-1" :name "Sandblasting" :color "#ff6b6b"}
   {:id "process-2" :name "Chemical Clean" :color "#4ecdc4"}
   {:id "process-3" :name "Powder Coating" :color "#45b7d1"}
   {:id "process-4" :name "Curing Oven" :color "#96ceb4"}
   {:id "process-5" :name "Quality Check" :color "#feca57"}
   {:id "process-6" :name "Clear Coat" :color "#ff9ff3"}])

(def default-available-recipes
  [{:id "recipe-1"
    :name "Standard Black Coating"
    :type "recipe"
    :color "#333"
    :processes [{:id "process-1" :name "Sandblasting" :type "prep" :color "#ff6b6b"}
                {:id "process-2" :name "Chemical Clean" :type "prep" :color "#4ecdc4"}
                {:id "process-3" :name "Powder Coating" :type "coating" :color "#45b7d1"}
                {:id "process-4" :name "Curing Oven" :type "curing" :color "#96ceb4"}
                {:id "process-5" :name "Quality Check" :type "inspection" :color "#feca57"}]}
   {:id "recipe-2"
    :name "Premium Clear Coat"
    :type "recipe"
    :color "#8e44ad"
    :processes [{:id "process-1" :name "Sandblasting" :type "prep" :color "#ff6b6b"}
                {:id "process-2" :name "Chemical Clean" :type "prep" :color "#4ecdc4"}
                {:id "process-3" :name "Powder Coating" :type "coating" :color "#45b7d1"}
                {:id "process-6" :name "Clear Coat" :type "coating" :color "#ff9ff3"}
                {:id "process-4" :name "Curing Oven" :type "curing" :color "#96ceb4"}
                {:id "process-5" :name "Quality Check" :type "inspection" :color "#feca57"}]}
   {:id "recipe-3"
    :name "Basic Prep Only"
    :type "recipe"
    :color "#27ae60"
    :processes [{:id "process-1" :name "Sandblasting" :type "prep" :color "#ff6b6b"}
                {:id "process-2" :name "Chemical Clean" :type "prep" :color "#4ecdc4"}]}])

;; -----------------------------------------------------------------------------
;; ---- Multi-Drag Selection State ----

(def selection-state (r/atom #{}))

(defn toggle-selection [item-id available-processes] 
  (let [current-selection @selection-state
        ;; Check if the item is a process (not a recipe)
        is-process (some #(= (:id %) item-id) available-processes)]
    (when is-process  ; Only allow selection of processes
      ;; Always toggle the item (checkbox behavior)
      (if (contains? current-selection item-id)
        (swap! selection-state disj item-id)
        (swap! selection-state conj item-id)))))

(defn clear-selection []
  (reset! selection-state #{}))

(defn is-selected? [item-id] 
  (contains? @selection-state item-id))

(defn get-selected-items [items] 
  (filter #(is-selected? (:id %)) items))

;; -----------------------------------------------------------------------------
;; ---- Timing Functions ----

(defn parse-iso-date [iso-string] 
  (when iso-string
    (js/Date. iso-string)))

(defn calculate-elapsed-time [start-time end-time] 
  (when (and start-time end-time)
    (- (.getTime (parse-iso-date end-time))
       (.getTime (parse-iso-date start-time)))))

(defn calculate-current-duration [start-time] 
  (when start-time
    (- (js/Date.now)
       (.getTime (parse-iso-date start-time)))))

(defn format-duration [duration-ms] 
  (when duration-ms
    (let [seconds (Math/floor (/ duration-ms 1000))
          minutes (Math/floor (/ seconds 60))
          hours (Math/floor (/ minutes 60))
          remaining-minutes (mod minutes 60)
          remaining-seconds (mod seconds 60)]
      (cond
        (>= hours 1) (str hours "h " remaining-minutes "m")
        (>= minutes 1) (str minutes "m " remaining-seconds "s")
        :else (str remaining-seconds "s")))))

(defn live-timer [start-time]
  (let [[current-time set-current-time] (zero-react/use-state (js/Date.now))]
    
    (zero-react/use-effect
     {:mount (fn []
               ;; Set up interval to update timer every second
               (let [interval (js/setInterval
                               (fn []
                                 (set-current-time (js/Date.now)))
                               1000)]
                 ;; Return cleanup function
                 (fn []
                   (js/clearInterval interval))))
      :params [start-time]})  ; Re-run effect if start-time changes
    
    (let [current-duration (calculate-current-duration start-time)]
      [:span {:style {:color "#f59e0b"
                      :font-weight "600"
                      :font-size "0.7rem"}}
       "⏱️ " (format-duration current-duration)])))

(defn get-waiting-time [process batch batch-processes]
  (let [process-order (:step_order process)
        batch-processes-sorted (sort-by :step_order batch-processes)
        previous-step-process (first (filter #(= (:step_order %) (dec process-order)) batch-processes-sorted))
        batch-created-time (or (:created_at batch) (:created-at batch))]
    (cond
      ;; If there's a previous step and it's finished, calculate from its finish time
      (and previous-step-process (:finish_time previous-step-process))
      (calculate-current-duration (:finish_time previous-step-process))
      
      ;; If this is the first step or no previous step finished, calculate from batch creation
      batch-created-time
      (calculate-current-duration batch-created-time)
      
      ;; Fallback
      :else 0)))

(defn static-completed-time [start-time finish-time]
  (let [static-duration (calculate-elapsed-time start-time finish-time)]
    [:span {:class "timing-complete"
            :style {:font-weight "600"
                    :font-size "0.7rem"}}
     "✓ " (format-duration static-duration)]))

(defn live-waiting-timer [process batch batch-processes]
  (let [[current-time set-current-time] (zero-react/use-state (js/Date.now))]
    
    (zero-react/use-effect
     {:mount (fn []
               ;; Set up interval to update timer every second
               (let [interval (js/setInterval
                               (fn []
                                 (set-current-time (js/Date.now)))
                               1000)]
                 ;; Return cleanup function
                 (fn []
                   (js/clearInterval interval))))
      :params [process batch]})  ; Re-run effect if process or batch changes
    
    (let [waiting-duration (get-waiting-time process batch batch-processes)]
      [:span {:style {:font-weight "600"
                      :font-size "0.7rem"}}
       "⏳ " (format-duration waiting-duration)])))

(defn timing-display [process batch]
  (let [process-order (:step_order process)
        current-step (or (:current_step batch) (:current-step batch) 1)
        start-time (:start_time process)
        finish-time (:finish_time process)
        batch-processes (:processes batch)
        workflow-state (or (:workflow_state batch) (:workflow-state batch) "to-do")]
    [:div {:class "process-timing"}
     (cond
       ;; Process is done - show static elapsed time
       (and finish-time start-time)
       [static-completed-time start-time finish-time]
       
       ;; Process is currently running - show live timer
       (and start-time (not finish-time) (= process-order current-step))
       [:div {:class "timing-active"}
        [live-timer start-time]]
       
       ;; Process is waiting to start but only show elapsed time if in "doing" state
       (and (not start-time) (= process-order current-step) (= workflow-state "doing"))
       [:div {:class "timing-ready"}
        [live-waiting-timer process batch batch-processes]]
       
       ;; Future process or to-do state
       :else nil)]))

;; -----------------------------------------------------------------------------
;; ---- Helper Functions ----

(defn reorder [list start-index end-index]
  (let [result (vec list)
        item (nth result start-index)
        without-item (into (subvec result 0 start-index) 
                           (subvec result (inc start-index)))]
    (into (subvec without-item 0 end-index)
          (cons item (subvec without-item end-index)))))

(defn create-unique-id [base-id]
  (str base-id "-" (random-uuid)))

(defn copy-process [process]
  (let [copied-process (-> process
                           (assoc :original-process-id (:id process))
                           (assoc :id (create-unique-id (:id process))))]
    (js/console.log "🔄 Copying process:" 
                    "original-id:" (:id process)
                    "new-id:" (:id copied-process)
                    "has-workstations:" (boolean (:workstations process))
                    "workstations-count:" (count (:workstations process)))
    copied-process))

(defn copy-recipe-processes [recipe]
  (mapv copy-process (:processes recipe)))

(defn copy-multiple-processes [processes]
  (mapv copy-process processes))

(defn copy-multiple-recipe-processes [recipes]
  (mapcat copy-recipe-processes recipes))

;; -----------------------------------------------------------------------------
;; ---- Drag and Drop Logic ----

(defn copy-from-available [source destination source-items dest-items]
  (let [item (nth source-items (:index source))]
    (if (contains? item :processes) ; Check if it's a recipe by presence of processes
      (let [new-processes (copy-recipe-processes item)]
        (into (subvec dest-items 0 (:index destination))
              (into new-processes (subvec dest-items (:index destination)))))
      (let [new-process (copy-process item)]
        (into (subvec dest-items 0 (:index destination))
              (cons new-process (subvec dest-items (:index destination))))))))

(defn move-between-lists [source-items dest-items source destination]
  (let [item (nth source-items (:index source))
        updated-source (into (subvec source-items 0 (:index source))
                             (subvec source-items (inc (:index source))))
        updated-dest (into (subvec dest-items 0 (:index destination))
                           (cons item (subvec dest-items (:index destination))))]
    {:source updated-source
     :dest updated-dest}))

;; -----------------------------------------------------------------------------
;; ---- Multi-Drag Drop Logic ----

(defn copy-multiple-from-available [source destination source-items dest-items selected-ids]
  (let [selected-items (filter #(contains? selected-ids (:id %)) source-items)
        new-items (if (some #(contains? % :processes) selected-items)
                    ;; Handle mix of recipes and processes
                    (let [recipes (filter #(contains? % :processes) selected-items)
                          processes (filter #(not (contains? % :processes)) selected-items)
                          recipe-processes (copy-multiple-recipe-processes recipes)
                          copied-processes (copy-multiple-processes processes)]
                      (concat recipe-processes copied-processes))
                    ;; Handle only processes
                    (copy-multiple-processes selected-items))]
    (into (subvec dest-items 0 (:index destination))
          (into new-items (subvec dest-items (:index destination))))))

(defn move-multiple-between-lists [source-items dest-items selected-ids destination]
  (let [selected-items (filter #(contains? selected-ids (:id %)) source-items)
        remaining-source (filterv #(not (contains? selected-ids (:id %))) source-items)
        updated-dest (into (subvec dest-items 0 (:index destination))
                           (into selected-items (subvec dest-items (:index destination))))]
    {:source remaining-source
     :dest updated-dest}))

;; -----------------------------------------------------------------------------
;; ---- Smart Naming Functions ----

(defn generate-smart-batch-name 
  "Generate a smart batch name using part name, color name, sequence, and quantity"
  [batch batches suffix]
  (let [part-name (or (:part_name batch) (:partType batch) "Part")
        color-name (or (:color_name batch) "Unknown Color")
        quantity (:quantity batch)
        batch-suffix (if suffix (str " " suffix) "")
        full-name (str part-name " - " color-name batch-suffix " - " quantity)]
    full-name))

(defn get-next-batch-suffix 
  "Get the next batch suffix for splitting (e.g., 'A', 'B', 'C')"
  [source-batch-name existing-batches]
  (let [base-name-pattern (clojure.string/replace source-batch-name #" [A-Z] - [0-9]+$" "")
        existing-suffixes (->> existing-batches
                              (map :name)
                              (filter #(clojure.string/starts-with? % base-name-pattern))
                              (map #(let [parts (clojure.string/split % #" - ")]
                                      (when (>= (count parts) 3)
                                        (nth parts 2))))
                              (filter #(and % (= (count %) 1) (re-matches #"[A-Z]" %)))
                              set)
        alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        next-suffix (first (filter #(not (contains? existing-suffixes %)) alphabet))]
    next-suffix))

(defn generate-split-batch-names 
  "Generate names for original and new batch when splitting"
  [source-batch remaining-quantity split-quantity existing-batches]
  (let [original-name (:name source-batch)
        base-batch (assoc source-batch :quantity remaining-quantity)
        split-batch (assoc source-batch :quantity split-quantity)
        
        ; Check if the original name already has a suffix pattern (Part - Color A - Quantity)
        has-suffix-pattern (re-find #" [A-Z] - [0-9]+$" original-name)
        
        ; Generate new names
        [original-suffix new-suffix] (if has-suffix-pattern
                                       ; If already has suffix, increment
                                       (let [next-suffix (get-next-batch-suffix original-name existing-batches)]
                                         [nil next-suffix])
                                       ; If no suffix, add A and B
                                       ["A" "B"])
        
        original-new-name (generate-smart-batch-name base-batch existing-batches original-suffix)
        split-new-name (generate-smart-batch-name split-batch existing-batches new-suffix)]
    
    [original-new-name split-new-name]))

;; -----------------------------------------------------------------------------
;; ---- Batch Creation Helper Functions ----

;; -----------------------------------------------------------------------------
;; ---- State Update Functions ----

(defn update-batches! [batches new-batches on-batches-change]
  (when on-batches-change
    (on-batches-change new-batches))
  new-batches)

(defn- create-split-batch [source-batch split-quantity split-new-name]
  "Creates a new batch from split operation"
  {:id (str (random-uuid))
   :name split-new-name
   :quantity split-quantity
   :partType (:partType source-batch)
   :partIcon (:partIcon source-batch)
   :current-step (or (:current-step source-batch) 1)
   :processes (mapv copy-process (:processes source-batch))
   :part_name (:part_name source-batch)
   :color_name (:color_name source-batch)})

(defn- update-original-batch [batches source-batch-id split-quantity original-new-name]
  "Updates the original batch after split operation"
  (mapv #(if (= (:id %) source-batch-id)
           (-> %
               (update :quantity - split-quantity)
               (assoc :name original-new-name))
           %)
        batches))

(defn create-new-batch! [batches source-batch-id split-quantity on-batches-change]
  (let [source-batch (first (filter #(= (:id %) source-batch-id) batches))]
    (when (and source-batch 
               (> split-quantity 0) 
               (< split-quantity (:quantity source-batch)))
      (let [remaining-quantity (- (:quantity source-batch) split-quantity)
            [original-new-name split-new-name] (generate-split-batch-names 
                                                source-batch 
                                                remaining-quantity 
                                                split-quantity 
                                                batches)
            new-batch (create-split-batch source-batch split-quantity split-new-name)
            updated-batches (update-original-batch batches source-batch-id split-quantity original-new-name)
            new-batches (conj updated-batches new-batch)]
        (update-batches! batches new-batches on-batches-change)))))

(defn rename-batch! [batches batch-id new-name on-batches-change]
  (let [new-batches (mapv #(if (= (:id %) batch-id)
                             (assoc % :name new-name)
                             %)
                          batches)]
    (update-batches! batches new-batches on-batches-change)))

(defn remove-process-from-batch! [batches batch-id process-id on-batches-change]
  (let [new-batches (mapv #(if (= (:id %) batch-id)
                             (update % :processes (fn [processes] 
                                                    (filterv (fn [p] (not= (:id p) process-id)) processes)))
                             %)
                          batches)]
    (update-batches! batches new-batches on-batches-change)))

;; -----------------------------------------------------------------------------
;; ---- Drag Operation Helper Functions ----

;; -----------------------------------------------------------------------------
;; ---- Updated Drag Drop Event Handlers ----

(defn handle-copy-from-processes [source destination batches available-processes on-batches-change]
  (let [target-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId destination)) %1) batches))]
    (when target-batch-idx
      (let [new-processes (copy-from-available source destination available-processes 
                                               (:processes (nth batches target-batch-idx)))
            updated-batch (assoc (nth batches target-batch-idx) :processes new-processes)
            new-batches (assoc batches target-batch-idx updated-batch)]
        (on-batches-change new-batches)))))

(defn handle-copy-from-recipes [source destination batches available-recipes on-batches-change]
  (let [target-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId destination)) %1) batches))]
    (when target-batch-idx
      (let [new-processes (copy-from-available source destination available-recipes 
                                               (:processes (nth batches target-batch-idx)))
            updated-batch (assoc (nth batches target-batch-idx) :processes new-processes)
            new-batches (assoc batches target-batch-idx updated-batch)]
        (on-batches-change new-batches)))))

(defn handle-reorder-within-batch [source destination batches on-batches-change]
  (let [batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId source)) %1) batches))]
    (when batch-idx
      (let [batch (nth batches batch-idx)
            reordered-processes (reorder (:processes batch) (:index source) (:index destination))
            updated-batch (assoc batch :processes reordered-processes)
            new-batches (assoc batches batch-idx updated-batch)]
        (on-batches-change new-batches)))))

(defn handle-move-between-batches [source destination batches on-batches-change]
  (let [source-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId source)) %1) batches))
        dest-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId destination)) %1) batches))]
    (when (and source-batch-idx dest-batch-idx)
      (let [source-batch (nth batches source-batch-idx)
            dest-batch (nth batches dest-batch-idx)
            move-result (move-between-lists (:processes source-batch) (:processes dest-batch) source destination)
            updated-source-batch (assoc source-batch :processes (:source move-result))
            updated-dest-batch (assoc dest-batch :processes (:dest move-result))
            new-batches (-> batches
                            (assoc source-batch-idx updated-source-batch)
                            (assoc dest-batch-idx updated-dest-batch))]
        (on-batches-change new-batches)))))

(defn handle-multi-copy-from-processes [source destination batches available-processes on-batches-change selected-ids]
  (let [target-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId destination)) %1) batches))]
    (when target-batch-idx
      (let [new-processes (copy-multiple-from-available source destination available-processes 
                                                        (:processes (nth batches target-batch-idx)) selected-ids)
            updated-batch (assoc (nth batches target-batch-idx) :processes new-processes)
            new-batches (assoc batches target-batch-idx updated-batch)]
        (clear-selection)
        (on-batches-change new-batches)))))

(defn handle-multi-copy-from-recipes [source destination batches available-recipes on-batches-change selected-ids]
  (let [target-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId destination)) %1) batches))]
    (when target-batch-idx
      (let [new-processes (copy-multiple-from-available source destination available-recipes 
                                                        (:processes (nth batches target-batch-idx)) selected-ids)
            updated-batch (assoc (nth batches target-batch-idx) :processes new-processes)
            new-batches (assoc batches target-batch-idx updated-batch)]
        (clear-selection)
        (on-batches-change new-batches)))))

(defn handle-multi-move-between-batches [source destination batches on-batches-change selected-ids]
  (let [source-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId source)) %1) batches))
        dest-batch-idx (first (keep-indexed #(when (= (:id %2) (:droppableId destination)) %1) batches))]
    (when (and source-batch-idx dest-batch-idx)
      (let [source-batch (nth batches source-batch-idx)
            dest-batch (nth batches dest-batch-idx)
            move-result (move-multiple-between-lists (:processes source-batch) (:processes dest-batch) selected-ids destination)
            updated-source-batch (assoc source-batch :processes (:source move-result))
            updated-dest-batch (assoc dest-batch :processes (:dest move-result))
            new-batches (-> batches
                            (assoc source-batch-idx updated-source-batch)
                            (assoc dest-batch-idx updated-dest-batch))]
        (clear-selection)
        (on-batches-change new-batches)))))

(defn- determine-drag-type [selected-ids dragged-id]
  "Determines if this should be treated as multi-drag or single drag"
  (and (contains? selected-ids dragged-id) 
       (> (count selected-ids) 1)))

(defn- handle-multi-drag-operations [source destination batches available-processes available-recipes on-batches-change selected-ids]
  "Handles all multi-drag operations based on source type"
  (let [source-id (:droppableId source)
        dest-id (:droppableId destination)]
    (cond
      (= source-id "available-processes")
      (handle-multi-copy-from-processes source destination batches available-processes on-batches-change selected-ids)
      
      (= source-id "available-recipes")
      (handle-multi-copy-from-recipes source destination batches available-recipes on-batches-change selected-ids)
      
      (not= source-id dest-id)
      (handle-multi-move-between-batches source destination batches on-batches-change selected-ids)
      
      :else
      (clear-selection))))

(defn- handle-single-drag-operations [source destination batches available-processes available-recipes on-batches-change]
  "Handles all single-drag operations based on source and destination"
  (clear-selection)
  (let [source-id (:droppableId source)
        dest-id (:droppableId destination)]
    (cond
      (= source-id "available-processes")
      (handle-copy-from-processes source destination batches available-processes on-batches-change)
      
      (= source-id "available-recipes")
      (handle-copy-from-recipes source destination batches available-recipes on-batches-change)
      
      (= source-id dest-id)
      (handle-reorder-within-batch source destination batches on-batches-change)
      
      :else
      (handle-move-between-batches source destination batches on-batches-change))))

(defn handle-drag-end [result batches available-processes available-recipes on-batches-change]
  (let [source (:source result)
        destination (:destination result)
        dragged-id (:draggableId result)
        selected-ids @selection-state
        is-multi-drag (determine-drag-type selected-ids dragged-id)]
    
    (when destination
      (if is-multi-drag
        (handle-multi-drag-operations source destination batches available-processes available-recipes on-batches-change selected-ids)
        (handle-single-drag-operations source destination batches available-processes available-recipes on-batches-change)))))

;; -----------------------------------------------------------------------------
;; ---- Navigation Functions ----

(defn navigate-to-workstation [workstation-id]
  (let [wsid @(rf/subscribe [:workspace/get-id])]
    (when (and wsid workstation-id)
      (router/navigate! {:path (str "/flex/ws/" wsid "/workstations/" workstation-id "/task-board")}))))

;; -----------------------------------------------------------------------------
;; ---- Batch Process Area Components ----

;; -----------------------------------------------------------------------------
;; ---- Split Modal Components ----

;; -----------------------------------------------------------------------------
;; ---- Batch Component Helper Functions ----

;; -----------------------------------------------------------------------------
;; ---- Recipe Component Helper Functions ----

;; -----------------------------------------------------------------------------
;; ---- Process Component Helper Functions ----

;; -----------------------------------------------------------------------------
;; ---- UI Components ----

(defn get-current-step-info [batch-id batches index]
  (if batch-id
    (let [current-batch (first (filter #(= (:id %) batch-id) batches))
          ; Check both snake_case and kebab-case for current step
          current-step (or (:current_step current-batch)  ; snake_case from backend
                           (:current-step current-batch)  ; kebab-case fallback
                           1)
          process-position (inc index)
          is-current-step (= process-position current-step)
          is-completed-step (< process-position current-step)]
      (js/console.log "🔍 Step status check:" 
                      "batch-id:" batch-id 
                      "index:" index 
                      "process-position:" process-position
                      "current-step:" current-step 
                      "is-current-step:" is-current-step
                      "is-completed-step:" is-completed-step)
      {:current-step current-step
       :is-current-step is-current-step
       :is-completed-step is-completed-step})
    ; If no batch-id, no special status
    {:current-step 0
     :is-current-step false
     :is-completed-step false}))

(defn get-process-styling [process is-current-step snapshot]
  (let [background-color (cond
                          (.-isDragging snapshot) "#f0f0f0"
                          :else "#fff")
        border-color (if (not is-current-step) (:color process) "#28a745")]
    (when is-current-step
      (js/console.log "🟢 Process is current step, adding CSS class:" (:name process)))
    {:background-color background-color
     :border-color border-color}))

(defn process-drag-handle [] 
  [:span {:style {:cursor "grab"} :class "drag-handle"} 
   "⋮⋮"])

(defn process-checkbox [process-id is-selected available-processes] 
  (when available-processes  ; Only show checkbox if this is an available process
    [:input {:type "checkbox"
             :checked is-selected
             :on-change #(toggle-selection process-id available-processes)
             :on-click #(.stopPropagation ^js %)  ; Prevent triggering drag
             :style {:margin-right "8px"
                     :cursor "pointer"
                     :transform "scale(1.2)"}}]))

(defn workflow-state-badge [workflow-state is-current-step]
  (when (and is-current-step workflow-state)
    [:div {:style {:position "absolute"
                   :top "8px"
                   :right "8px"
                   :background (case workflow-state
                                 "to-do" "#6366f1"
                                 "doing" "#f59e0b"
                                 "#6b7280")
                   :color "white"
                   :padding "2px 6px"
                   :border-radius "4px"
                   :font-size "9px"
                   :font-weight "600"
                   :text-transform "uppercase"
                   :letter-spacing "0.5px"
                   :z-index "10"}}
     workflow-state]))

(defn process-name-section [process batch-id batches on-batches-change available-processes] 
  (let [is-selected (is-selected? (:id process))]
    [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
     [process-checkbox (:id process) is-selected available-processes]
     [process-drag-handle]
     [:span {:style {:flex 1} :class "process-name"} (:name process)]]))

(defn circular-remove-button [process batch-id batches on-batches-change] 
  (when batch-id
    [:button {:on-click #(remove-process-from-batch! batches batch-id (:id process) on-batches-change)
              :style {:position "absolute"
                      :top "-10px"
                      :right "-10px"
                      :width "20px"
                      :height "20px"
                      :border-radius "50%"
                      :background "#ef4444"
                      :border "2px solid #ffffff"
                      :color "white"
                      :cursor "pointer"
                      :font-size "12px"
                      :line-height "1"
                      :display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :transition "all 0.2s ease"
                      :opacity "1"
                      :transform "scale(1)"
                      :z-index "20"
                      :box-shadow "0 2px 4px rgba(0,0,0,0.1)"}
              :on-mouse-enter #(-> ^js % .-target .-style (.setProperty "background" "#dc2626"))
              :on-mouse-leave #(-> ^js % .-target .-style (.setProperty "background" "#ef4444"))
              :class "circular-remove-btn"}
     "×"]))

(defn workstation-link [workstation index]
  [:span
   (when (> index 0) ", ")
   [:a {:href "#"
        :on-click (fn [e]
                    (.preventDefault ^js e)
                    (navigate-to-workstation (:workstation_id workstation)))
        :class "workstation-link"}
    (:workstation_name workstation)]])

(defn workstations-section [process] 
  (js/console.log "🏭 Workstations check for process:" 
                  "id:" (:id process)
                  "original-id:" (:original-process-id process)
                  "name:" (:name process)
                  "has-workstations:" (boolean (:workstations process))
                  "workstations:" (:workstations process))
  (when (and (:workstations process) (seq (:workstations process)))
    [:div {:class "workstations"}
     "Workstations: "
     (doall
      (map-indexed
       (fn [index workstation]
         ^{:key (:workstation_id workstation)}
         [workstation-link workstation index])
       (:workstations process)))]))

(defn- compute-process-component-state [process index batch-id batches]
  "Computes the state information needed for process component rendering"
  (let [current-batch (when batch-id (first (filter #(= (:id %) batch-id) batches)))
        workflow-state (or (:workflow_state current-batch) (:workflow-state current-batch) "to-do")
        step-info (get-current-step-info batch-id batches index)
        is-selected (is-selected? (:id process))
        selected-count (count @selection-state)]
    {:current-batch current-batch
     :workflow-state workflow-state
     :step-info step-info
     :is-selected is-selected
     :selected-count selected-count}))

(defn- process-css-class [step-info workflow-state is-selected]
  "Generates CSS class string for process component"
  (str "draggable-item process-item"
       (cond
         (:is-current-step step-info) (str " current-step " workflow-state)
         (:is-completed-step step-info) " completed-step"
         :else "")
       (when is-selected " selected")))

(defn- process-container-style [step-info process is-selected]
  "Generates style map for process container"
  {:border (when-not (:is-current-step step-info) 
            (str "2px solid " (:color process)))
   :background-color (if is-selected "#e3f2fd" "#fff")
   :padding "8px"
   :margin "4px"
   :border-radius "4px"
   :display "flex"
   :flex-direction "column"
   :gap "4px"
   :cursor "grab"
   :position "relative"
   :box-shadow (when is-selected "0 0 0 2px #1976d2")
   :opacity (when (:is-completed-step step-info) "0.7")})

(defn- multi-drag-counter-badge [is-selected selected-count]
  "Renders multi-drag counter badge when multiple items are selected"
  (when (and is-selected (> selected-count 1))
    [:div {:style {:position "absolute"
                   :top "-8px"
                   :right "-8px"
                   :background "#ff4444"
                   :color "white"
                   :border-radius "50%"
                   :width "20px"
                   :height "20px"
                   :display "flex"
                   :align-items "center"
                   :justify-content "center"
                   :font-size "12px"
                   :font-weight "600"
                   :z-index "15"}
           :class "multi-drag-counter"}
     selected-count]))

(defn process-component [process index batch-id batches on-batches-change available-processes]
  (let [state (compute-process-component-state process index batch-id batches)
        css-class (process-css-class (:step-info state) (:workflow-state state) (:is-selected state))
        container-style (process-container-style (:step-info state) process (:is-selected state))]
    [:div {:data-id (:id process)
           :style container-style
           :class css-class}
     [multi-drag-counter-badge (:is-selected state) (:selected-count state)]
     [workflow-state-badge (:workflow-state state) (:is-current-step (:step-info state))]
     [circular-remove-button process batch-id batches on-batches-change]
     [process-name-section process batch-id batches on-batches-change available-processes]
     [workstations-section process]
     (when (:current-batch state)
       [timing-display process (:current-batch state)])]))

(defn- recipe-container-style [recipe]
  "Style map for recipe component container"
  {:border (str "2px solid " (:color recipe))
   :background-color "#fff"
   :padding "12px"
   :margin "4px"
   :border-radius "8px"
   :display "flex"
   :flex-direction "column"
   :gap "8px"
   :cursor "grab"
   :position "relative"
   :box-shadow "0 2px 4px rgba(0,0,0,0.1)"
   :transition "all 0.2s ease"})

(defn- recipe-header-section [recipe]
  "Recipe header with drag handle and color indicator"
  [:div {:style {:display "flex" :align-items "center" :gap "8px" :margin-bottom "4px"}}
   [:span {:style {:color "#666" :font-size "12px" :cursor "grab"}} "⋮⋮"]
   [:div {:style {:flex 1 :display "flex" :align-items "center" :gap "8px"}}
    [:div {:style {:width "12px"
                   :height "12px"
                   :border-radius "2px"
                   :background-color (:color recipe)
                   :flex-shrink "0"}}]
    [:div {:style {:font-weight "600" :font-size "14px" :color "#333"}} (:name recipe)]]])

(defn- recipe-details-section [recipe]
  "Recipe details with step count and badge"
  [:div {:style {:display "flex" :align-items "center" :justify-content "space-between"}}
   [:div {:style {:font-size "12px" :color "#666"}} 
    (str (count (:processes recipe)) " step" (when (not= (count (:processes recipe)) 1) "s"))]
   [:div {:style {:background-color (:color recipe)
                  :color "white"
                  :padding "3px 8px"
                  :border-radius "12px"
                  :font-size "10px"
                  :font-weight "600"
                  :text-transform "uppercase"
                  :letter-spacing "0.5px"}}
    "Recipe"]])

(defn- recipe-process-preview [recipe]
  "Process preview for recipes with 3 or fewer steps"
  (when (<= (count (:processes recipe)) 3)
    [:div {:style {:font-size "11px" :color "#888" :line-height "1.3"}}
     (clojure.string/join " → " (map :name (:processes recipe)))]))

(defn recipe-component [recipe index]
  [:div {:data-id (:id recipe)
         :style (recipe-container-style recipe)
         :class "draggable-item recipe-item"
         :on-mouse-enter #(-> ^js % .-target .-style (.setProperty "transform" "translateY(-2px)"))
         :on-mouse-leave #(-> ^js % .-target .-style (.setProperty "transform" "translateY(0)"))}
   [recipe-header-section recipe]
   [recipe-details-section recipe]
   [recipe-process-preview recipe]])

(defn- current-batch-info-section [batch]
  "Displays the current batch information"
  [:div {:style {:margin-bottom "24px"}}
   [:div {:style {:font-size "16px"
                  :font-weight "600"
                  :color "#333"
                  :margin-bottom "8px"}}
    "Current Batch"]
   [:div {:style {:font-size "14px"
                  :color "#666"
                  :background-color "#f8f9fa"
                  :padding "12px"
                  :border-radius "6px"
                  :border "1px solid #e9ecef"}}
    (str "Quantity: " (:quantity batch))]])

(defn- split-quantity-input-section [batch split-quantity]
  "Input section for specifying split quantity"
  [:div {:style {:margin-bottom "24px"}}
   [:label {:style {:display "block"
                    :font-size "14px"
                    :font-weight "500"
                    :color "#333"
                    :margin-bottom "8px"}}
    "Split Quantity:"]
   [:input {:type "number"
            :min 1
            :max (dec (:quantity batch))
            :value @split-quantity
            :on-change #(reset! split-quantity (js/parseInt (.. ^js % -target -value) 10))
            :style {:width "100%"
                    :padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :font-size "14px"
                    :outline "none"
                    :transition "border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out"}
            :on-focus #(set! (-> ^js % .-target .-style .-borderColor) "#5da7d9")
            :on-blur #(set! (-> ^js % .-target .-style .-borderColor) "#d1d5db")}]])

(defn- split-result-preview-section [batch split-quantity]
  "Preview section showing the result of the split"
  [:div {:style {:background-color "#e8f4f8"
                 :border "1px solid #b8e6f0"
                 :border-radius "6px"
                 :padding "16px"}}
   [:div {:style {:font-size "14px"
                  :font-weight "500"
                  :color "#0f5132"
                  :margin-bottom "8px"}}
    "Split Result:"]
   [:div {:style {:font-size "14px"
                  :color "#0f5132"
                  :line-height "1.5"}}
    [:div (str "Original batch: " (- (:quantity batch) @split-quantity) " units")]
    [:div (str "New batch: " @split-quantity " units")]]])

(defn- split-modal-body-content [batch split-quantity]
  "Main body content of the split modal"
  [:div {:style {:overflow-y "auto"
                 :flex-grow 1
                 :padding "20px"
                 :padding-bottom "10px"}}
   [current-batch-info-section batch]
   [split-quantity-input-section batch split-quantity]
   [split-result-preview-section batch split-quantity]])

(defn- split-modal-footer-buttons [batch batches split-quantity on-batches-change]
  "Footer buttons for the split modal"
  [:div {:style {:display "flex"
                 :justify-content "flex-end"
                 :gap "12px"
                 :flex-shrink 0
                 :padding "20px"
                 :padding-top "10px"
                 :border-top "1px solid #e9ecef"}}
   [button/view {:mode :clear_2
                 :type :secondary
                 :on-click #(rf/dispatch [:modals/close :split-batch])}
    "Cancel"]
   [button/view {:mode :filled
                 :type :primary
                 :on-click #(do
                              (create-new-batch! batches (:id batch) @split-quantity on-batches-change)
                              (rf/dispatch [:modals/close :split-batch]))}
    "Split Batch"]])

(defn split-batch-modal-content [batch batches on-batches-change]
  (let [split-quantity (r/atom 1)]
    (fn [batch batches on-batches-change]
      [:div {:style {:min-width "400px"
                     :max-height "80vh"
                     :display "flex"
                     :flex-direction "column"}}
       [split-modal-body-content batch split-quantity]
       [split-modal-footer-buttons batch batches split-quantity on-batches-change]])))

(defn open-split-modal! [batch batches on-batches-change]
  (rf/dispatch [:modals/add {:id :split-batch
                             :open? true
                             :label (str "Split Batch: " (:name batch))
                             :content [split-batch-modal-content batch batches on-batches-change]}])
  (rf/dispatch [:modals/open :split-batch]))


(defn- filter-recipes-by-search [recipes search-term]
  "Filters recipes based on search term"
  (if (empty? search-term)
    recipes
    (filter #(clojure.string/includes? 
             (clojure.string/lower-case (:name %))
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
  "Scrollable list of recipe components"
  [:div {:style {:height (str (* container-height 0.4) "px")
                 :overflow-y "auto"
                 :background-color "#f8f9fa" 
                 :border "2px dashed #dee2e6" 
                 :border-radius "4px" 
                 :padding "10px"}}
   (doall
    (map-indexed (fn [index recipe]
                   ^{:key (:id recipe)}
                   [recipe-component recipe index])
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
             (clojure.string/lower-case (:name %))
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
  "Scrollable list of process components"
  [:div {:style {:height (str (* container-height 0.4) "px")
                 :overflow-y "auto"
                 :background-color "#f8f9fa"
                 :border "2px dashed #dee2e6" 
                 :border-radius "4px" 
                 :padding "10px"}}
   (doall
    (map-indexed (fn [index process]
                   ^{:key (:id process)}
                   [process-component process index nil nil nil available-processes])
                 filtered-processes))])

(defn available-processes-section [available-processes container-height]
  (let [[search-term set-search-term] (zero-react/use-state "")
        filtered-processes (filter-processes-by-search available-processes search-term)]
    [:div
     [processes-search-input search-term set-search-term]
     [processes-scrollable-list filtered-processes container-height available-processes]]))

(defn step-progress-indicator [batch]
  (let [current-step (or (:current_step batch) (:current-step batch) 1)
        total-steps (count (:processes batch))]
    (when (> total-steps 0)
      [:div {:class "batch-step-indicator"}
       [:span "Step"]
       [:span {:class "step-number"} 
        (str current-step "/" total-steps)]])))

(defn- empty-batch-drop-zone []
  "Empty state component for when batch has no processes"
  [:div {:style {:min-height "120px"
                 :background-color "#fff"
                 :border "2px dashed #ccc"
                 :border-radius "4px"
                 :padding "10px"
                 :text-align "center"
                 :color "#666"}}
   [:p "Drop recipes or processes here"]
   [:div {:style {:font-size "24px"}} "⚙️ 🧾"]])

(defn- batch-process-list [batch-processes batch batches on-batches-change]
  "Renders the list of processes for a batch with sortable functionality"
  [:> (.-ReactSortable ReactSortable)
   {:list (clj->js (mapv #(assoc % :id (:id %)) batch-processes))
    :setList (fn [new-list]
               (let [js-list (js->clj new-list :keywordize-keys true)
                     new-batches (mapv #(if (= (:id %) (:id batch))
                                         (assoc % :processes js-list)
                                         %)
                                       batches)]
                 (on-batches-change new-batches)))
    :animation 200
    :delay 0
    :delayOnTouchStart true
    :style {:min-height "120px"
            :background-color "#fff"
            :border "2px dashed #ccc"
            :border-radius "4px"
            :padding "10px"}}
   (map (fn [process] 
          ^{:key (:id process)} 
          [:div {:data-id (:id process)
                 :style {:cursor "grab"}}
           [process-component process (.indexOf batch-processes process) (:id batch) batches on-batches-change nil]]) 
        batch-processes)])

(defn batch-processes-area [batch batches on-batches-change available-processes]
  (let [batch-processes (:processes batch)]
    (if (empty? batch-processes)
      [empty-batch-drop-zone]
      [batch-process-list batch-processes batch batches on-batches-change])))

(defn- batch-part-image-section [batch]
  "Renders batch part image if available"
  (when (:part_picture_url batch)
    [:img {:src (:part_picture_url batch)
           :alt (:part_name batch)
           :class "batch-part-image"}]))

(defn- batch-color-info-section [batch]
  "Renders batch color information if available"
  (when (and (:color_name batch) (:color_basecolor batch))
    [:div {:class "batch-color-info"}
     [:div {:class "batch-color-dot"
            :style {:background-color (:color_basecolor batch)}}]
     [:span (:color_name batch)]]))

(defn- batch-info-section [batch batches on-batches-change]
  "Renders batch information section with image and details"
  [:div {:class "batch-info"}
   [batch-part-image-section batch]
   [:div {:class "batch-details"}
    [:input {:type "text"
             :value (:name batch)
             :on-change #(rename-batch! batches (:id batch) (.. ^js % -target -value) on-batches-change)
             :class "batch-name-input"}]
    [batch-color-info-section batch]
    [step-progress-indicator batch]]])

(defn- batch-controls-section [batch on-split-dialog]
  "Renders batch controls with quantity and split button"
  [:div {:class "batch-controls"}
   [:span {:class "batch-quantity"} (str "Qty: " (:quantity batch))]
   [:button {:on-click #(on-split-dialog batch)
             :class "batch-split-btn"}
    "✂️"]])

(defn batch-component [batch batches on-batches-change on-split-dialog available-processes]
  [:div {:class "batch-card"}
   [:div {:class "batch-header"}
    [batch-info-section batch batches on-batches-change]
    [batch-controls-section batch on-split-dialog]]
   [:div {:class "batch-processes"}
    [batch-processes-area batch batches on-batches-change available-processes]]])

;; -----------------------------------------------------------------------------
;; ---- CSS Style Components ----

;; -----------------------------------------------------------------------------
;; ---- UI Panel Components ----

(defn- draggable-item-styles []
  "CSS styles for draggable items and selection states"
  ".draggable-item.selected {
     background-color: #e3f2fd !important;
     border-color: #1976d2 !important;
   }")

(defn- recipe-item-styles []
  "CSS styles for recipe items and their interactions"
  ".recipe-item {
     transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
   }
   
   .recipe-item:hover {
     transform: translateY(-2px);
     box-shadow: 0 4px 12px rgba(0,0,0,0.15);
   }
   
   .recipe-item:active {
     transform: translateY(0);
     transition: all 0.1s ease;
   }")

(defn- process-item-styles []
  "CSS styles for process items and their interactions"
  ".process-item {
     transition: all 0.2s ease;
   }
   
   .process-item:hover {
     box-shadow: 0 2px 8px rgba(0,0,0,0.1);
   }")

(defn- multi-drag-styles []
  "CSS styles for multi-drag functionality"
  ".multi-drag-ghost {
     opacity: 0.8;
     transform: rotate(5deg);
   }
   
   .multi-drag-counter {
     position: absolute;
     top: -8px;
     right: -8px;
     background: #ff4444;
     color: white;
     border-radius: 50%;
     width: 20px;
     height: 20px;
     display: flex;
     align-items: center;
     justify-content: center;
     font-size: 12px;
     font-weight: 600;
     z-index: 15;
     animation: bounceIn 0.3s ease;
   }
   @keyframes bounceIn {
     0% { transform: scale(0); }
     50% { transform: scale(1.2); }
     100% { transform: scale(1); }
   }")

(defn- timing-display-styles []
  "CSS styles for process timing displays"
  ".process-timing {
     display: flex;
     justify-content: flex-end;
     padding-top: 4px;
     border-top: 1px solid rgba(0,0,0,0.05);
     margin-top: 4px;
   }
   
   .timing-ready {
     color: #6366f1 !important;
   }
   
   .timing-active {
     color: #f59e0b !important;
     animation: pulse 2s infinite;
   }
   
   .timing-complete {
     color: #10b981 !important;
   }
   
   @keyframes pulse {
     0% { opacity: 1; }
     50% { opacity: 0.7; }
     100% { opacity: 1; }
   }")

(defn- container-styles []
  "CSS styles for main container elements"
  ".batch-editor-container {
     position: relative;
   }")

(defn batch-editor-styles [] 
  [:style (str
           (draggable-item-styles) "\n\n"
           (recipe-item-styles) "\n\n"
           (process-item-styles) "\n\n"
           (multi-drag-styles) "\n\n"
           (timing-display-styles) "\n\n"
           (container-styles))])

;; -----------------------------------------------------------------------------
;; ---- Available Items Helper Components ----

;; -----------------------------------------------------------------------------
;; ---- Sidebar Components ----

(defn available-items-sidebar [available-recipes available-processes container-height]
  [:div {:style {:flex "0 0 300px"}}
   [available-recipes-section available-recipes container-height]
   [available-processes-section available-processes container-height]])

;; -----------------------------------------------------------------------------
;; ---- Batches Section Components ----

(defn batches-section [batches on-batches-change available-processes container-height]
  [:div {:style {:flex 1}}
   ;[:h2 {:style {:margin "0 0 20px 0"}} (str "Batches (" (count batches) ")")]
   [:div {:style {:height (str container-height "px")
                  :overflow-y "auto"
                  :gap "20px"}}
    (doall
     (map (fn [batch]
            ^{:key (:id batch)}
            [batch-component batch batches 
             on-batches-change
             #(open-split-modal! % batches on-batches-change)
             available-processes])
          batches))]])

;; -----------------------------------------------------------------------------
;; ---- Main Layout Components ----

(defn batch-editor-content [batches available-recipes available-processes on-batches-change container-height]
  [:div {:style {:display "flex" :gap "20px"}}
   [available-items-sidebar available-recipes available-processes container-height]
   [batches-section batches on-batches-change available-processes container-height]])

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
  content)

;; -----------------------------------------------------------------------------
;; ---- State Management Hook ----

(defn use-batch-editor-state [initial-job-name initial-batches initial-available-processes initial-available-recipes]
  (let [job-name (r/atom (or initial-job-name "40 Rims - Black Powder Coating"))
        batches (r/atom (or initial-batches default-batches))
        available-processes (r/atom (or initial-available-processes default-available-processes))
        available-recipes (r/atom (or initial-available-recipes default-available-recipes))]
    
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
     [batch-editor-styles]
     [drag-drop-wrapper @(:batches state) @(:available-processes state) @(:available-recipes state) batches-change-handler
      [batch-editor-container @(:batches state) @(:available-recipes state) @(:available-processes state)
       batches-change-handler]]
     [modals/modals]]))

(def batch-editor-component batch-editor)