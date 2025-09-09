(ns router.frontend.transform)

; ╔════════════════════════╗
; ║  Route transformation  ║
; ╚════════════════════════╝

(defn route->reitit-route [route]
  (let [{:keys [path]}
        route]
    [path route]))

(defn routes->reitit-routes [routes]
  (mapv route->reitit-route routes))
