(ns features.flex.batches.frontend.batch-editor.drag-drop)

;; -----------------------------------------------------------------------------
;; ---- React-SortableJS Drag and Drop Handlers ----

(defn handle-cross-container-add [evt batch batches available-processes available-recipes on-batches-change]
  "Handles items being dropped into a batch from available items"
  (let [item-element (.-item ^js evt)
        item-id (.getAttribute ^js item-element "data-id")
        item-type (.getAttribute ^js item-element "data-type")
        new-index (.-newIndex ^js evt)]
    
    ;; Remove the cloned item that was automatically added
    (.remove ^js item-element)
    
    ;; Find the original item and handle appropriately
    (let [batch-id (:batch/id batch)
          batch-idx (first (keep-indexed #(when (= (:batch/id %2) batch-id) %1) batches))
          current-batch (nth batches batch-idx)
          current-processes (vec (:batch/processes current-batch))]
      
      (cond
        ;; Handle recipe drop - expand processes
        (= item-type "recipe")
        (let [recipe-item (first (filter #(= (:recipe/id %) item-id) available-recipes))
              recipe-processes (mapv #(assoc % :process/id (str (:process/id %) "-" (random-uuid))) (:recipe/processes recipe-item))
              new-processes (vec (concat (take new-index current-processes)
                                       recipe-processes
                                       (drop new-index current-processes)))
              updated-batch (assoc current-batch :batch/processes new-processes)
              new-batches (assoc (vec batches) batch-idx updated-batch)]
          (on-batches-change new-batches))
        
        ;; Handle single process drop
        (= item-type "process")
        (let [process-item (first (filter #(= (:process/id %) item-id) available-processes))
              new-process (assoc process-item :process/id (str (:process/id process-item) "-" (random-uuid)))
              new-processes (vec (concat (take new-index current-processes)
                                       [new-process]
                                       (drop new-index current-processes)))
              updated-batch (assoc current-batch :batch/processes new-processes)
              new-batches (assoc (vec batches) batch-idx updated-batch)]
          (on-batches-change new-batches))
        
        :else
        nil))))