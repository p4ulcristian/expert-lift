
(ns features.customizer.panel.frontend.blocks.price.effects
  (:require 
    [re-frame.core  :as r]
   
    [features.customizer.blocks.form :as form]))

(defn calc-parts-prices [db package]
  (let [form-template  (get-in package [:form :template])
        inherited-keys (form/extract-inherit-ids form-template)
        inherited-data (select-keys (get package "formdata") inherited-keys)
        
        parts        (select-keys (get-in db [:customizer :parts]) (:parts package))]
    
    (reduce (fn [result [part-id part-data]]
              (let [price (form/calc-price! (get-in db [:forms (:form_id part-data) :price_formula])
                                            (merge (get part-data "formdata") inherited-data))]
                (assoc result part-id price)))
            {}
            parts)))

(defn update-parts-prices [db parts-price]
  (reduce (fn [_db [part-id part-price]]
            (assoc-in _db [:customizer :parts part-id :price] part-price))
          db
          parts-price))

(r/reg-event-db
  :customizer.package.price/calc!
  (fn [db [_]]
    (println "calc package & part")
    (let [package-id       (get-in db [:customizer :package-id])
          package          (get-in db [:customizer :packages package-id])
          package-formdata (get package "formdata")

          price-formula    (get-in db [:forms (:form_id package) :price_formula])

          package-price    (form/calc-price! price-formula package-formdata)
          parts-prices     (calc-parts-prices db package)]
      
      (-> db
          (update-parts-prices parts-prices)
          (assoc-in [:customizer :packages package-id :price] package-price)))))


(defn get-part-formdata [db part-data]
  (let [package-id            (:package-id part-data)
        package               (get-in db [:customizer :packages package-id])
        package-form-template (get-in package [:form :template])
        inherited-input-keys  (form/extract-inherit-ids package-form-template)
        package-formdata      (select-keys (get package "formdata") inherited-input-keys)]
  
    (merge (get part-data "formdata") package-formdata)))

(r/reg-event-db
  :customizer.part.price/calc!
  (fn [db [_ edited-item]]
    (let [cursor        (get-in db [:customizer :cursor])
          dest          (conj cursor :price)
          selected-item (get-in db cursor)
          
          formdata      (get edited-item "formdata")
          
          price-formula (get-in db [:forms (:form_id edited-item) :price_formula])

          price         (form/calc-price! price-formula;(form/item->expr selected-item)
                                          formdata)]
                      
      (assoc-in db dest price))))
 