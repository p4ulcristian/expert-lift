(ns zero.frontend.window-dimensions
  (:require [reagent.core :refer [atom]]))

(def dimensions (atom {:width  (.-innerWidth js/window)
                       :height (.-innerHeight js/window)}))

(defn update-dimensions []
  (reset! dimensions {:width  (.-innerWidth js/window)
                      :height (.-innerHeight js/window)}))

(defn small? []
  (< (:width @dimensions) 600))

(defn medium? []
  (and (>= (:width @dimensions) 600)
       (< (:width @dimensions) 1200)))

(defn large? []
  (>= (:width @dimensions) 1200))

(.addEventListener js/window "resize" update-dimensions)