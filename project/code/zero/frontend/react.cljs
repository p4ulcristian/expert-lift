(ns zero.frontend.react
  (:require ["react" :as react]))

(defn use-state [initial-value]
 (react/useState initial-value))

(defn use-effect [{:keys [mount unmount params]}]
  (react/useEffect
   (fn []
     (mount)
     (if unmount
       (fn [] (unmount))
       (fn [])))
   (if params params #js[])))