
(ns features.labs.storage.frontend.views
  (:require
    [features.labs.shared.frontend.components.header :as header]
    [features.common.storage.frontend.views :as storage]))

(defn view []
  [:<> 
    [header/view]
    [:div {:style {:padding "25px" :height "calc(100% - 60px)"}}
      [storage/view]]])
  