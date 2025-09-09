(ns features.common.storage.frontend.blocks.file-preview
  (:require
    [re-frame.core :as r]
    [ui.popup :as popup]
    [ui.button :as button]
    [utils.time :as time]
    [features.labs.parts.frontend.glb-viewer :as glb-viewer]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn get-file-size [size]
  (cond
    (< size 1024)       (str size " B")
    (< size 1048576)    (str (.toFixed (/ size 1024) 2) " KB")
    (< size 1073741824) (str (.toFixed (/ size 1048576) 2) " MB")
    :else (str (.toFixed (/ size 1073741824) 2) " GB")))

(defn is-image? [mime-type]
  (when mime-type
    (.startsWith mime-type "image/")))

(defn is-text? [mime-type]
  (when mime-type
    (or (.startsWith mime-type "text/")
        (= mime-type "application/json")
        (= mime-type "application/xml")
        (= mime-type "application/javascript"))))

(defn is-glb? [mime-type url]
  (or (= mime-type "model/gltf-binary")
      (= mime-type "application/octet-stream")
      (when url
        (.endsWith url ".glb"))))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- 3D Model Components ----

(defn GLBViewer [{:keys [url]}]
  [glb-viewer/glb-viewer {:url    url
                          :width  770
                          :height 400}])

;; ---- 3D Model Components ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Preview Content ----

(defn get-preview-content [item]
  (let [{:keys [url mime_type type alias]} item]
    (cond
      (= type "directory")
      [:div {:style {:display "flex"
                     :flex-direction "column"
                     :align-items "center"
                     :justify-content "center"
                     :height "300px"
                     :color "var(--seco-clr)"}}
        [:i {:class "fa-solid fa-folder"
             :style {:font-size "64px"
                     :margin-bottom "16px"}}]
        [:p "Directory Preview"]
        [:p {:style {:font-size "14px"}} "This is a directory"]]
      
      (is-image? mime_type)
      [:div {:style {:display "flex"
                     :justify-content "center"
                     :align-items "center"
                     :min-height "300px"
                     :max-height "500px"}}
        [:img {:src url
               :alt alias
               :style {:max-width "100%"
                       :max-height "100%"
                       :object-fit "contain"}}]]
      
      (is-glb? mime_type url)
      [:div {:style {:display "flex"
                     :flex-direction "column"
                     :align-items "center"
                     :gap "12px"}}
        [:div {:style {:display "flex"
                       :align-items "center"
                       :gap "8px"
                       :color "var(--seco-clr)"}}
          [:i {:class "fa-solid fa-cube"
               :style {:font-size "16px"}}]
          [:span "3D Model Viewer"]]
        [GLBViewer {:url url}]]
      
      (is-text? mime_type)
      [:div {:style {:max-height "400px"
                     :overflow "auto"
                     :background "#f5f5f5"
                     :border-radius "6px"
                     :padding "16px"}}
        [:pre {:style {:margin 0
                       :font-family "monospace"
                       :font-size "12px"
                       :white-space "pre-wrap"
                       :word-break "break-word"}}
          "Loading text content..."]]
      
      :else
      [:div {:style {:display "flex"
                     :flex-direction "column"
                     :align-items "center"
                     :justify-content "center"
                     :height "300px"
                     :color "var(--seco-clr)"}}
        [:i {:class "fa-solid fa-file"
             :style {:font-size "64px"
                     :margin-bottom "16px"}}]
        [:p "Preview Not Available"]
        [:p {:style {:font-size "14px"}} "This file type cannot be previewed"]])))

;; ---- Preview Content ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn file-info [item]
  (let [{:keys [alias mime_type size added_at added_by]} item]
    [:div {:style {:display "grid"
                   :gap "12px"
                   :padding "16px"
                   :background "#f8f9fa"
                   :border-radius "6px"
                   :margin-bottom "16px"}}
      [:div {:style {:display "grid"
                     :grid-template-columns "auto 1fr"
                     :gap "8px"
                     :align-items "center"}}
        [:span {:style {:font-weight "600"
                        :color "var(--seco-clr)"}} "Name:"]
        [:span alias]]
      
      (when mime_type
        [:div {:style {:display "grid"
                       :grid-template-columns "auto 1fr"
                       :gap "8px"
                       :align-items "center"}}
          [:span {:style {:font-weight "600"
                          :color "var(--seco-clr)"}} "Type:"]
          [:span mime_type]])
      
      (when size
        [:div {:style {:display "grid"
                       :grid-template-columns "auto 1fr"
                       :gap "8px"
                       :align-items "center"}}
          [:span {:style {:font-weight "600"
                          :color "var(--seco-clr)"}} "Size:"]
          [:span (get-file-size size)]])
      
      (when added_at
        [:div {:style {:display "grid"
                       :grid-template-columns "auto 1fr"
                       :gap "8px"
                       :align-items "center"}}
          [:span {:style {:font-weight "600"
                          :color "var(--seco-clr)"}} "Added:"]
          [:span (time/format-date-no-time added_at)]])
      
      (when added_by
        [:div {:style {:display "grid"
                       :grid-template-columns "auto 1fr"
                       :gap "8px"
                       :align-items "center"}}
          [:span {:style {:font-weight "600"
                          :color "var(--seco-clr)"}} "By:"]
          [:span added_by]])]))

(defn view [id props]
  (let [preview-item @(r/subscribe [:db/get-in [:storage :preview-item]])]
    [popup/view {:state    @(r/subscribe [:db/get id])
                 :on-close (fn []
                             (r/dispatch [:db/dissoc id])
                             (r/dispatch [:db/dissoc-in [:storage :preview-item]]))
                 :style    {:z-index 2
                            :max-width "800px"
                            :width "90vw"}
                 :cover    {:z-index 1}}
      [:div {:style {:display "grid" :gap "20px"}}
        [:div {:style {:display "flex"
                       :justify-content "space-between"
                       :align-items "center"}}
          [:h3 {:style {:margin 0}} "File Preview"]
          [button/view {:mode :clear_2
                        :type :secondary
                        :on-click #(r/dispatch [:db/dissoc id])}
            [:i {:class "fa-solid fa-times"}]]]
        
        (when preview-item
          [:<>
            [file-info preview-item]
            [get-preview-content preview-item]
            
            [:div {:style {:display "flex"
                           :gap "8px"
                           :justify-content "flex-end"}}
              [button/view {:mode :clear_2
                            :type :secondary
                            :on-click #(r/dispatch [:db/dissoc id])}
                "Close"]
              [button/view {:type :primary
                            :on-click #(js/window.open (:url preview-item) "_blank")}
                "Open in New Tab"]]])]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Public API ----

(defn open [item]
  (r/dispatch [:db/assoc-in [:storage :preview-item] item])
  (r/dispatch [:db/assoc :file-preview true]))

;; ---- Public API ----
;; -----------------------------------------------------------------------------