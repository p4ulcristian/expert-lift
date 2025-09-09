(ns router.backend.transform)

; ╔════════════════════════╗
; ║  Route transformation  ║
; ╚════════════════════════╝

(defn route->reitit-route [route]
  (let [{:keys [path get post put delete middleware]}
        route]
    [path
     (merge
      (when get    {:get    {:handler get}})
      (when post   {:post   {:handler post}})
      (when put    {:put    {:handler put}})
      (when delete {:delete {:handler delete}})
      {:middleware middleware})]))

(defn routes->reitit-routes [routes]
  (mapv route->reitit-route routes))
