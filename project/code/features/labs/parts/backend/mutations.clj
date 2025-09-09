(ns features.labs.parts.backend.mutations
  (:require
   [cheshire.core :as json]
   [clj-time.format :as f]
   [com.wsscode.pathom3.connect.operation :as pco]
   [features.labs.parts.backend.db :as db]
   [features.customizer.panel.backend.resolvers :as customizer-menu]))

;; -----------------------------------------------------------------------------
;; ---- Categories ----

(defn- get-next-category-position 
  "Get the next order position for a category"
  [category_id]
  (let [result (db/get-next-category-order-position {:category_id category_id})]
    (:next_order_position result)))

(defn- format-created-at-string 
  "Convert created_at timestamp to string"
  [category]
  (update category :created_at #(str %)))

(defn create-category-fn [{:keys [_request] :as _env} {:keys [name description picture_url category_id]}]
  (println "Creating category with data:" {:name name :description description :picture_url picture_url :category_id category_id})
  (try
     (let [next-order-position (get-next-category-position category_id)
           new-category (-> (db/create-category
                              {:name        name
                               :description description
                               :picture_url picture_url
                               :category_id category_id
                               :order_position next-order-position})
                            format-created-at-string)]

       (customizer-menu/refresh-menu!)
       new-category)
      
    (catch Exception e
      (println "Error creating category:" (.getMessage e))
      :error)))

(pco/defmutation create-category! [env mutation-props]
  {::pco/op-name 'categories/create-category!}
  (create-category-fn env mutation-props))

(defn update-category-fn [{:keys [_request] :as _env} {:keys [id name description picture_url]}]
  (println "Updating category with data:" {:id id :name name :description description :picture_url picture_url})
  (try
     (let [result (-> (db/update-category
                        {:id          id
                         :name        name
                         :description description
                         :picture_url picture_url})
                      format-created-at-string)]
       (customizer-menu/refresh-menu!)
       result)
    (catch Exception e
      (println "Error updating category:" (.getMessage e))
      :error)))

(pco/defmutation update-category! [env mutation-props]
  {::pco/op-name 'categories/update-category!}
  (update-category-fn env mutation-props))

(defn delete-category-fn [{:keys [_request] :as _env} {:keys [id]}]
  (println "Deleting category with id:" id)
  (try
    (let [result (db/delete-category {:id id})]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error deleting category:" (.getMessage e))
      :error)))

;; ---- Categories ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Parts ----

(pco/defmutation delete-category! [env mutation-props]
  {::pco/op-name 'categories/delete-category!}
  (delete-category-fn env mutation-props))

(defn- update-single-category-order
  "Update order position for a single category"
  [{:keys [id order-position]}]
  (db/update-category-order-position {:id id :order_position order-position}))

(defn reorder-categories-fn [{:keys [_request] :as _env} {:keys [category-orders]}]
  (println "Reordering categories with data:" category-orders)
  (try
    (let [result (doall (map update-single-category-order category-orders))]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error reordering categories:" (.getMessage e))
      :error)))

(pco/defmutation reorder-categories! [env mutation-props]
  {::pco/op-name 'categories/reorder-categories!}
  (reorder-categories-fn env mutation-props))

(defn initialize-category-order-positions-fn [{:keys [_request] :as _env} _]
  (println "Initializing category order positions")
  (try
    (let [result (db/initialize-category-order-positions)]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error initializing category order positions:" (.getMessage e))
      :error)))

(pco/defmutation initialize-category-order-positions! [env mutation-props]
  {::pco/op-name 'categories/initialize-category-order-positions!}
  (initialize-category-order-positions-fn env mutation-props))

;; -----------------------------------------------------------------------------
;; ---- Packages ----

(defn- get-next-package-position 
  "Get the next order position for a package"
  [category_id]
  (let [result (db/get-next-package-order-position {:category_id category_id})]
    (:next_order_position result)))

(defn- format-package-timestamps
  "Convert created_at and updated_at timestamps to strings"
  [package]
  (-> package
      (update :created_at #(str %))
      (update :updated_at #(str %))))

(defn- prepare-package-form-id
  "Prepare form_id value, converting empty strings to nil"
  [form_id]
  (if (and form_id (not= form_id "")) form_id nil))

(defn create-package-fn [{:keys [_request] :as _env} 
                         {:keys [name description picture_url prefix category_id model_url popular form_id]}]
  (println "Creating package with data:" {:name name :description description :picture_url picture_url :prefix prefix :category_id category_id :model_url model_url :popular popular :form_id form_id})
  (try
     (let [next-order-position (get-next-package-position category_id)
           prepared-form-id (prepare-package-form-id form_id)
           new-package (-> (db/create-package
                             {:name        name
                              :description description
                              :picture_url picture_url
                              :prefix      prefix
                              :category_id category_id
                              :model_url   model_url
                              :popular     (or popular false)
                              :form_id     prepared-form-id
                              :order_position next-order-position})
                           format-package-timestamps)]
       (customizer-menu/refresh-menu!)
       new-package)
    (catch Exception e
      (println "Error creating package:" (.getMessage e))
      :error)))

(pco/defmutation create-package! [env mutation-props]
  {::pco/op-name 'packages/create-package!}
  (create-package-fn env mutation-props))

(defn update-package-fn [{:keys [_request] :as _env} 
                         {:keys [id name description picture_url prefix category_id model_url popular form_id]}]
  (println "Updating package with data:" {:id id :name name :description description :picture_url picture_url :prefix prefix :category_id category_id :model_url model_url :popular popular :form_id form_id})
  (try
     (let [prepared-form-id (prepare-package-form-id form_id)
           result (-> (db/update-package
                        {:id          id
                         :name        name
                         :description description
                         :picture_url picture_url
                         :prefix      prefix
                         :category_id category_id
                         :model_url   model_url
                         :popular     (or popular false)
                         :form_id     prepared-form-id})
                      format-package-timestamps)]
       (customizer-menu/refresh-menu!)
       result)
    (catch Exception e
      (println "Error updating package:" (.getMessage e))
      :error)))

(pco/defmutation update-package! [env mutation-props]
  {::pco/op-name 'packages/update-package!}
  (update-package-fn env mutation-props))

(defn delete-package-fn [{:keys [_request] :as _env} {:keys [id]}]
  (println "Deleting package with id:" id)
  (try
    (let [result (db/delete-package {:id id})]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error deleting package:" (.getMessage e))
      :error)))

(pco/defmutation delete-package! [env mutation-props]
  {::pco/op-name 'packages/delete-package!}
  (delete-package-fn env mutation-props))

(defn- update-single-package-order
  "Update order position for a single package"
  [{:keys [id order-position]}]
  (db/update-package-order-position {:id id :order_position order-position}))

(defn reorder-packages-fn [{:keys [_request] :as _env} {:keys [package-orders]}]
  (println "Reordering packages with data:" package-orders)
  (try
    (let [result (doall (map update-single-package-order package-orders))]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error reordering packages:" (.getMessage e))
      :error)))

(pco/defmutation reorder-packages! [env mutation-props]
  {::pco/op-name 'packages/reorder-packages!}
  (reorder-packages-fn env mutation-props))

(defn initialize-package-order-positions-fn [{:keys [_request] :as _env} _]
  (println "Initializing package order positions")
  (try
    (let [result (db/initialize-package-order-positions)]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error initializing package order positions:" (.getMessage e))
      :error)))

(pco/defmutation initialize-package-order-positions! [env mutation-props]
  {::pco/op-name 'packages/initialize-package-order-positions!}
  (initialize-package-order-positions-fn env mutation-props))

;; ---- Packages ----
;; -----------------------------------------------------------------------------

(defn- get-next-part-position 
  "Get the next order position for a part"
  [package_id]
  (let [result (db/get-next-part-order-position {:package_id package_id})]
    (:next_order_position result)))

(defn- format-part-timestamps
  "Convert created_at and updated_at timestamps to strings"
  [part]
  (-> part
      (update :created_at #(str %))
      (update :updated_at #(str %))))

(defn- prepare-part-form-id
  "Prepare form_id value, converting empty strings to nil"
  [form_id]
  (if (and form_id (not= form_id "")) form_id nil))

(defn create-part-fn [{:keys [_request] :as _env} 
                      {:keys [name description picture_url popular mesh_id package_id form_id]}]
  (println "Creating part with data:" {:name name :description description :picture_url picture_url :popular popular :mesh_id mesh_id :package_id package_id :form_id form_id})
  (try
     (let [next-order-position (get-next-part-position package_id)
           prepared-form-id (prepare-part-form-id form_id)
           result (-> (db/create-part
                        {:name        name
                         :description description
                         :picture_url picture_url
                         :package_id  package_id
                         :popular     (or popular false)
                         :mesh_id     mesh_id
                         :form_id     prepared-form-id
                         :order_position next-order-position})
                      format-part-timestamps)]
       result)
    (catch Exception e
      (println "Error creating part:" (.getMessage e))
      :error)))

(pco/defmutation create-part! [env mutation-props]
  {::pco/op-name 'parts/create-part!} 
  (create-part-fn env mutation-props))

(defn update-part-fn [{:keys [_request] :as _env} 
                      {:keys [id name description picture_url popular mesh_id package_id form_id]}]
  (println "Updating part with data:" {:id id :name name :description description :picture_url picture_url :popular popular :mesh_id mesh_id :package_id package_id :form_id form_id})
  (try
     (let [prepared-form-id (prepare-part-form-id form_id)]
       (-> (db/update-part
             {:id          id
              :name        name
              :description description
              :picture_url picture_url
              :package_id  package_id
              :popular     (or popular false)
              :mesh_id     mesh_id
              :form_id     prepared-form-id})
           format-part-timestamps))
     (catch Exception e
       (println "Error updating part:" (.getMessage e))
       :error)))

(pco/defmutation update-part! [env mutation-props]
  {::pco/op-name 'parts/update-part!}
  (update-part-fn env mutation-props))

(defn delete-part-fn [{:keys [_request] :as _env} {:keys [id]}]
  (println "Deleting part with id:" id)
  (try
     (let [result (db/delete-part {:id id})]
       (customizer-menu/refresh-menu!)
       result)
     (catch Exception e
        (println "Error deleting part:" (.getMessage e))
        :error)))

(pco/defmutation delete-part! [env mutation-props]
  {::pco/op-name 'parts/delete-part!}
  (delete-part-fn env mutation-props))

(defn- update-single-part-order
  "Update order position for a single part"
  [{:keys [id order-position]}]
  (db/update-part-order-position {:id id :order_position order-position}))

(defn reorder-parts-fn [{:keys [_request] :as _env} {:keys [part-orders]}]
  (println "Reordering parts with data:" part-orders)
  (try
    (let [result (doall (map update-single-part-order part-orders))]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error reordering parts:" (.getMessage e))
      :error)))

(pco/defmutation reorder-parts! [env mutation-props]
  {::pco/op-name 'parts/reorder-parts!}
  (reorder-parts-fn env mutation-props))

(defn initialize-part-order-positions-fn [{:keys [_request] :as _env} _]
  (println "Initializing part order positions")
  (try
    (let [result (db/initialize-part-order-positions)]
      (customizer-menu/refresh-menu!)
      result)
    (catch Exception e
      (println "Error initializing part order positions:" (.getMessage e))
      :error)))

(pco/defmutation initialize-part-order-positions! [env mutation-props]
  {::pco/op-name 'parts/initialize-part-order-positions!}
  (initialize-part-order-positions-fn env mutation-props))

;; ---- Parts ----
;; -----------------------------------------------------------------------------

(def mutations [create-category! update-category! delete-category! reorder-categories! initialize-category-order-positions! create-package! update-package! delete-package! reorder-packages! initialize-package-order-positions! create-part! update-part! delete-part! reorder-parts! initialize-part-order-positions!]) 