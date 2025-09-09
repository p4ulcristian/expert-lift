(ns features.flex.shared.frontend.ui.layout
  (:require
   [features.flex.shared.frontend.ui.layouts.flex :as flex-layout]
   [features.flex.shared.frontend.ui.layouts.basic :as basic-layout]))

(defn decide-layout
  "Decides which layout to use based on the current path"
  [path]
  (if (= path "/accept-invitation")
    basic-layout/view
    flex-layout/view))

(defn view [{:keys [content _title path]}]
  (let [layout-fn (decide-layout path)]
    [layout-fn {:content content}]))