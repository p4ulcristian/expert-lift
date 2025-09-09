(ns features.flex.recipes.routes
  #?(:cljs (:require [features.flex.recipes.frontend.recipe :as  recipe]
                     [features.flex.recipes.frontend.recipes :as recipes])
     :clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def recipes-path "/flex/ws/:workspace-id/recipes")
(def recipe-path "/flex/ws/:workspace-id/recipes/:recipe-id")

(def routes
  #?(:cljs [{:path "/flex/ws/:workspace-id/recipes"
             :view recipes/view}
            {:path "/flex/ws/:workspace-id/recipes/:recipe-id"
             :view recipe/view}]
     :clj  [{:path recipes-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path recipe-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 