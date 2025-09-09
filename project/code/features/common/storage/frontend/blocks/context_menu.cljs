
(ns features.common.storage.frontend.blocks.context-menu
  (:require
   [features.common.storage.frontend.blocks.file-preview :as file-preview]
   [re-frame.core :as r]
   [ui.button :as button]))

(defn is-previewable? [{:keys [type mime_type url]}]
  (or (= type "directory")
      (when mime_type
        (or (.startsWith mime_type "image/")
            (.startsWith mime_type "text/")
            (= mime_type "application/json")
            (= mime_type "application/xml")
            (= mime_type "application/javascript")
            (= mime_type "model/gltf-binary")
            (= mime_type "application/octet-stream")))
      (when url
        (.endsWith url ".glb"))))

(defn context-menu [item-props]
  [:div {:style {:background    "white"
                 :border        "1px solid"
                 :border-radius "6px"
                 :padding       "15px"
                 :display       "grid"
                 :gap           "15px"}}
    (when (is-previewable? item-props)
      [button/view {:on-click #(do 
                                (file-preview/open item-props)
                                (r/dispatch [:popover/close :row-context-menu]))}
        "Preview"])
    [button/view {:on-click #(do 
                              (r/dispatch [:db/assoc-in [:storage :editing-item] item-props])
                              (r/dispatch [:db/assoc :rename-popup true])
                              (r/dispatch [:popover/close :row-context-menu]))}
      "Rename"]
    [button/view {:disabled true} "Create dir"]
    [button/view {:disabled true} "Download"]
    [button/view {:type :warning
                  :on-click #(do (r/dispatch [:storage/delete-item! (:id item-props)])
                                 (r/dispatch [:popover/close :row-context-menu]))}
      "Delete"]])

(defn open [event row-props]
  (.preventDefault ^js event)
  (r/dispatch [:popover/open :row-context-menu
               {:content [context-menu row-props]
                :target  (.-target event)
                :style   {:top  (.-clientY event)
                          :left (.-clientX event)}}]))