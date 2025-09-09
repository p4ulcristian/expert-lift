(ns features.flex.parts-pricing.backend.read
  (:require
   [cheshire.core :as json]
   [features.flex.parts-pricing.backend.db :as db]))

(defn parse-jsonb-field
  "Parse JSONB field from database"
  [jsonb-data]
  (try
    (cond
      (nil? jsonb-data) []
      (string? jsonb-data) (json/parse-string jsonb-data true)
      (map? jsonb-data) jsonb-data
      (sequential? jsonb-data) jsonb-data
      :else [])
    (catch Exception _
      [])))

(defn build-category-tree
  "Build tree structure for a category"
  [category categories]
  (let [children (filter #(= (:parent_id %) (:id category)) categories)]
    (assoc category :children (mapv #(build-category-tree % categories) children))))

(defn build-category-hierarchy
  "Build hierarchical category structure with subcategories"
  [categories]
  (let [root-categories (filter #(nil? (:parent_id %)) categories)]
    (mapv #(build-category-tree % categories) root-categories)))

(defn group-packages-by-category
  "Group packages by their category"
  [packages]
  (group-by :category_id packages))

(defn group-parts-by-package
  "Group parts by their package"
  [parts]
  (group-by :package_id parts))

(defn add-pricing-to-part
  "Add pricing information to a part"
  [part]
  {:id (:id part)
   :name (:name part)
   :description (:description part)
   :picture_url (:picture_url part)
   :created_at (:created_at part)
   :updated_at (:updated_at part)
   :pricing {:basic (:price_basic part)
            :basic-plus (:price_basic_plus part)
            :pro (:price_pro part)
            :pro-plus (:price_pro_plus part)
            :active (:is_active part)
            :pricing-id (:pricing_id part)}})

(defn add-parts-to-package
  "Add parts to a package with pricing data"
  [package parts-by-package]
  (let [package-parts (get parts-by-package (:id package) [])]
    (assoc package :parts (mapv add-pricing-to-part package-parts))))

(defn add-packages-to-category
  "Add packages to a category with their parts"
  [category packages-by-category parts-by-package]
  (let [category-packages (get packages-by-category (:id category) [])]
    (-> category
        (assoc :packages (mapv #(add-parts-to-package % parts-by-package) category-packages))
        (update :children (fn [children]
                            (mapv #(add-packages-to-category % packages-by-category parts-by-package) children))))))

(defn build-hierarchical-structure
  "Build the complete hierarchical structure: categories > packages > parts"
  [categories packages parts]
  (let [category-hierarchy (build-category-hierarchy categories)
        packages-by-category (group-packages-by-category packages)
        parts-by-package (group-parts-by-package parts)]
    (mapv #(add-packages-to-category % packages-by-category parts-by-package) category-hierarchy)))

(defn get-parts
  "Get parts with pricing in hierarchical structure"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [workspace-id (:workspace-id params)]
      (when workspace-id
        (let [parts (db/get-parts-with-pricing workspace-id)
              packages (db/get-packages-with-categories)
              categories (db/get-categories-hierarchy)]
          (build-hierarchical-structure categories packages parts))))
    (catch Exception e
      (println "Error getting parts with pricing:" (.getMessage e))
      [])))