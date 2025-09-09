(ns features.flex.batches.frontend.batch-editor.utils
  (:require [clojure.string]))

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
  (-> process
      (assoc :process/original-id (:process/id process))
      (assoc :process/id (create-unique-id (:process/id process)))))

(defn copy-recipe-processes [recipe]
  (mapv copy-process (:recipe/processes recipe)))

(defn copy-multiple-processes [processes]
  (mapv copy-process processes))

(defn copy-multiple-recipe-processes [recipes]
  (mapcat copy-recipe-processes recipes))

;; -----------------------------------------------------------------------------
;; ---- Smart Naming Functions ----

(defn generate-smart-batch-name 
  "Generate a smart batch name using part name, color name, sequence, and quantity"
  [batch batches suffix]
  (let [part-name (or (:batch/part-name batch) "Part")
        color-name (or (:batch/color-name batch) "Unknown Color")
        quantity (:batch/quantity batch)
        batch-suffix (if suffix (str " " suffix) "")
        full-name (str part-name " - " color-name batch-suffix " - " quantity)]
    full-name))

(defn get-next-batch-suffix 
  "Get the next batch suffix for splitting (e.g., 'A', 'B', 'C')"
  [source-batch-name existing-batches]
  (let [base-name-pattern (clojure.string/replace source-batch-name #" [A-Z] - [0-9]+$" "")
        existing-suffixes (->> existing-batches
                              (map :batch/name)
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
  (let [original-name (:batch/name source-batch)
        base-batch (assoc source-batch :batch/quantity remaining-quantity)
        split-batch (assoc source-batch :batch/quantity split-quantity)
        
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

(defn- create-split-batch [source-batch split-quantity split-new-name]
  "Creates a new batch from split operation"
  {:batch/id (str (random-uuid))
   :batch/name split-new-name
   :batch/quantity split-quantity
   :batch/part-type (:batch/part-type source-batch)
   :batch/part-icon (:batch/part-icon source-batch)
   :batch/current-step (or (:batch/current-step source-batch) 1)
   :batch/processes (mapv copy-process (:batch/processes source-batch))
   :batch/part-name (:batch/part-name source-batch)
   :batch/color-name (:batch/color-name source-batch)})

(defn- update-original-batch [batches source-batch-id split-quantity original-new-name]
  "Updates the original batch after split operation"
  (mapv #(if (= (:batch/id %) source-batch-id)
           (-> %
               (update :batch/quantity - split-quantity)
               (assoc :batch/name original-new-name))
           %)
        batches))

;; -----------------------------------------------------------------------------
;; ---- State Update Functions ----

(defn update-batches! [batches new-batches on-batches-change]
  (when on-batches-change
    (on-batches-change new-batches))
  new-batches)

(defn create-new-batch! [batches source-batch-id split-quantity on-batches-change]
  (let [source-batch (first (filter #(= (:batch/id %) source-batch-id) batches))]
    (when (and source-batch 
               (> split-quantity 0) 
               (< split-quantity (:batch/quantity source-batch)))
      (let [remaining-quantity (- (:batch/quantity source-batch) split-quantity)
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
  (let [new-batches (mapv #(if (= (:batch/id %) batch-id)
                             (assoc % :batch/display-name new-name :batch/name new-name)
                             %)
                          batches)]
    (update-batches! batches new-batches on-batches-change)))

(defn remove-process-from-batch! [batches batch-id process-id on-batches-change]
  (let [new-batches (mapv #(if (= (:batch/id %) batch-id)
                             (-> %
                                 (update :batch/processes (fn [processes] 
                                                            (filterv (fn [p] 
                                                                      (not= (:process/id p) process-id)) 
                                                                    processes)))
                             %))
                          batches)]
    (update-batches! batches new-batches on-batches-change)))