(ns features.app.zero.frontend.zero
  (:require
   [features.app.homepage.routes :as homepage-routes]
   [router.frontend.zero :as router]))

(def routes homepage-routes/routes)

(defn view []
  (let [router-data (:data @router/state)]
    [:div
     [(:view router-data)]]))
