(ns app.frontend.site
  (:require
   [features.app.zero.frontend.zero :as main]
   [reagent.core :as reagent.core]
   [reagent.dom.client  :as reagent.dom.client]
   [router.frontend.zero :as router]
   [zero.frontend.re-frame-viewer.view :as re-frame-viewer]
   [translations.core]))

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
  (re-frame-viewer/init!)
  (render-app!))