(ns features.labs.parts.frontend.utils)

(def storage-key "categories-data")

(def empty-category
  {:name ""
   :description ""
   :picture nil
   :parent-id nil
   :prefix ""})

(def empty-part
  {:name ""
   :description ""
   :picture nil
   :picture_url nil
   :model_url nil
   :category_id nil
   :package_id nil
   :type "part"
   :part_ids []
   :mesh_id nil
   :form_id nil
   :popular false})


(defn reset-new-category [state]
  (swap! state assoc :new-category empty-category))

(defn reset-new-part [state]
  (swap! state assoc :new-part empty-part))



