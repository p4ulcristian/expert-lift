(ns features.customizer.blocks.form
  (:require
    [features.common.key-replacer :as key-replacer]))
 
(defn extract-inherit-ids [form-template]
  ;; This function only walkthrough the first level.
  ;; template should not nest inputs if it attr :inherit true
  (keep (fn [input-props]
          (when (:inherit input-props)
            (:id input-props)))
        form-template))

(defn item->expr [item-data]
  (get-in item-data [:form :price-formula]))

(defn calc-price! [price-formula data]
    ;; (assert (string? price-formula)
    ;;         "\n Invalid price formula!\n")
    (if (empty? price-formula)
      0
      (key-replacer/calc-fn price-formula
                           (fn [value-path]
                             (let [target-path (conj value-path :value)]
                               (get-in data target-path 0))))))
      