
(ns features.flex.storage.frontend.views
  (:require
     [features.common.storage.frontend.views :as storage]
     [features.flex.shared.frontend.components.body :as body]))

(defn view []
  [body/view
   {:title "File Storage"
    :description "Manage and organize project files and documents."
    :body [:div {:style {:height "100%"}}
           [storage/view]]}])