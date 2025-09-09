(ns features.flex.recipes.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-recipes
  "Get recipes data using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:recipes/get-recipes {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [recipes (:recipes/get-recipes response)]
                  (callback recipes)))}))

(defn get-recipe
  "Get single recipe using ParQuery"
  [workspace-id recipe-id callback]
  (parquery/send-queries
   {:queries {:recipes/get-recipe {:recipe/id recipe-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [recipe (:recipes/get-recipe response)]
                  (callback recipe)))}))

(defn create-recipe
  "Create new recipe using ParQuery"
  [workspace-id recipe-data callback]
  (parquery/send-queries
   {:queries {:recipes/create-recipe recipe-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:recipes/create-recipe response)]
                  (callback result)))}))

(defn edit-recipe
  "Edit existing recipe using ParQuery"
  [workspace-id recipe-data callback]
  (parquery/send-queries
   {:queries {:recipes/edit-recipe recipe-data}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:recipes/edit-recipe response)]
                  (callback result)))}))

(defn delete-recipe
  "Delete recipe using ParQuery"
  [workspace-id recipe-id callback]
  (parquery/send-queries
   {:queries {:recipes/delete-recipe {:recipe/id recipe-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:recipes/delete-recipe response)]
                  (callback result)))}))