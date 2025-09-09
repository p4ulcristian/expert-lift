
(ns app.frontend.labs
  (:require
    [reagent.core         :as reagent.core]
    [reagent.dom.client   :as reagent.dom.client]
    [router.frontend.zero :as router]
    
    [features.labs.zero.frontend.zero :as main]))

(def functional-compiler
  (reagent.core/create-compiler {:function-components true}))

(reagent.core/set-default-compiler! functional-compiler)

(def root-el (.getElementById js/document "reagent-container"))

(defonce root
  (reagent.dom.client/create-root root-el))

(defn render-app!  []
  (router/init!  main/routes)
  (reagent.dom.client/render root [#'main/view]))

(defn start-app!  []
  (render-app!))