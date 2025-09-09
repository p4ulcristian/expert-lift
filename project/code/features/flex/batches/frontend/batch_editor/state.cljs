(ns features.flex.batches.frontend.batch-editor.state
  (:require [reagent.core :as r]))

;; -----------------------------------------------------------------------------
;; ---- Multi-Drag Selection State ----

(def selection-state (r/atom #{}))

(defn toggle-selection [item-id available-processes] 
  (let [current-selection @selection-state
        ;; Check if the item is a process (not a recipe)
        is-process (some #(= (:process/id %) item-id) available-processes)]
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
  (filter #(is-selected? (:process/id %)) items))