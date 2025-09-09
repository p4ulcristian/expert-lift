(ns features.common.storage.frontend.blocks.grid-view
  (:require
    [re-frame.core :as r]
    [features.common.storage.frontend.blocks.context-menu :as context-menu]
    [features.common.storage.frontend.blocks.file-preview :as file-preview]
    [utils.time :as time]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(def mime-types
  {"image/png"     "fa-solid fa-file-image"
   "image/jpeg"    "fa-solid fa-file-image"
   "image/svg+xml" "fa-solid fa-file-image"
   "application/octet-stream" "fa-brands fa-unity"
   "model/gltf-binary"        "fa-brands fa-unity"})

(defn icon-class [{:keys [type mime_type]}]
  (if (= type "directory")
    "fa-solid fa-folder"
    (get mime-types mime_type "fa-solid fa-file")))

(defn icon [item-props]
  [:i {:class (icon-class item-props)
       :style {:min-width  "24px"
               :text-align "center"}}])

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Grid Components ----

(defn grid-item [{:keys [id type alias mime_type added_at url] :as item-props}]
  (let [is-image? (and (= type "file") 
                       (when mime_type
                         (.startsWith mime_type "image/")))]
    [:div {:key             id
           :data-id         id
           :style           {:display         "grid"
                             :place-items     "center"
                             :padding         "20px"
                             :border-radius   "8px"
                             :cursor          "pointer"
                             :user-select     "none"
                             :background      "white"
                             :border          "1px solid #e0e0e0"
                             :transition      "all 0.1s ease"
                             :position        "relative"}
           :on-mouse-enter  #(set! (-> % .-target .-style .-background) "#ddd")
           :on-mouse-leave  #(set! (-> % .-target .-style .-background) "white")
           :on-context-menu #(context-menu/open % item-props)
           :on-double-click #(when (= type "file")
                               (file-preview/open item-props))
           :on-click        #(when (= type "directory")
                               (r/dispatch [:storage/open-dir! item-props]))}
     
     ;; Icon or Image Preview
     [:div {:style {:font-size "48px"
                    :color     (if (= type "directory") "#ffd700" "#6c757d")
                    :margin-bottom "12px"
                    :position  "relative"}}
       (if is-image?
         [:div {:style {:width "64px"
                        :height "64px"
                        :border-radius "8px"
                        :overflow "hidden"
                        :background "#f8f9fa"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"}}
           [:img {:src url
                  :alt alias
                  :style {:width "100%"
                          :height "100%"
                          :object-fit "cover"}}]]
         (if (= type "directory")
           [:i {:class "fa-solid fa-folder"}]
           [:i {:class (icon-class item-props)}]))]
     
     ;; Name
     [:p {:style {:font-size "14px"
                  :font-weight "500"
                  :text-align "center"
                  :margin "0 0 8px 0"
                  :max-width "120px"
                  :overflow "hidden"
                  :text-overflow "ellipsis"
                  :white-space "nowrap"}}
       alias]
     
     ;; Date
     [:p {:style {:font-size "12px"
                  :color "#6c757d"
                  :margin 0}}
       (if added_at 
         (time/format-date-no-time added_at)
         "-")]]))

(defn grid-view []
  (let [data @(r/subscribe [:db/get-in [:storage :downloaded-items]])]
    (if (empty? data)
      [:div {:style {:display "flex"
                     :justify-content "center"
                     :align-items "center"
                     :height "200px"
                     :color "#6c757d"}}
        "No files or folders found"]
      [:<>
        (let [directories (filter #(= (:type %) "directory") data)]
          (when-not (empty? directories)
            [:div
              [:h3 {:style {:font-weight "500"
                            :color      "var(--seco-clr)"
                            :text-align "left"}}
                "Folders"]

              [:div {:style {:display "grid"
                             :grid-template-columns "repeat(auto-fill, 150px)"
                             :grid-template-rows "repeat(auto-fill, 150px)"
                             :gap "16px"
                             :padding "16px 0"
                             :overflow "auto"}}
                 (map grid-item (sort-by (juxt :type :alias) directories))]]))
        (let [files (filter #(= (:type %) "file") data)]
          (when-not (empty? files)
            [:div
              [:h3 {:style {:font-weight "500"
                            :color      "var(--seco-clr)"
                            :text-align "left"}}
                "Files"]

              [:div {:style {:display "grid"
                             :grid-template-columns "repeat(auto-fill, minmax(140px, 1fr))"
                             :grid-template-rows "repeat(auto-fill, minmax(120px, 1fr))"
                             :gap "16px"
                             :padding "16px 0"
                             :overflow "auto"}}
         
                (map grid-item (sort-by (juxt :type :alias) files))]]))])))

;; ---- Grid Components ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Public API ----

(defn view []
  [grid-view])

;; ---- Public API ----
;; ----------------------------------------------------------------------------- 