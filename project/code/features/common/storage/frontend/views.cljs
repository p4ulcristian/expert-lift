
(ns features.common.storage.frontend.views
  (:require
   ["react" :as react]
   [features.common.storage.frontend.blocks.context-menu :as context-menu]
   [features.common.storage.frontend.blocks.create-dir :as create-dir.popup]
   [features.common.storage.frontend.blocks.file-preview :as file-preview]
   [features.common.storage.frontend.blocks.grid-view :as grid-view]
   [features.common.storage.frontend.blocks.rename-popup :as rename-popup]
   [features.common.storage.frontend.effects]
   [re-frame.core :as r]
   [ui.button     :as button]
   [ui.file-drop-zone :as drop-zone]
   [ui.file-selector :as file-selector]
   [ui.table      :as table]
   [ui.text-field :as text-field] ;;TEMP
   [utils.time    :as time]))

;; -----------------------------------------------------------------------------
;; ---- Header ----

(defn search-field []
  [text-field/view {:value          @(r/subscribe [:db/get-in [:search-term] ""])
                    :on-change      #(r/dispatch [:db/assoc-in [:search-term] %])
                    :on-enter       #(r/dispatch [:storage/request-items!])
                    :on-type-ended  #(r/dispatch [:storage/request-items!])
                    :left-adornment [:i {:class "fas fa-search" :style {:font-size "16px"}}]
                    :override       {:placeholder "Search"}}])
 
  ;; TODO: Uncomment this when autocomplete is ready
  ;; [autocomplete/view {:id             "search-field"
  ;;                     :value          @(r/subscribe [:db/get-in [:search-term] ""])
  ;;                     :on-change      #(r/dispatch [:db/assoc-in [:search-term] %])
  ;;                     :on-enter       #(r/dispatch [:storage/request-search-options! %1])
  ;;                     :on-type-ended  #(r/dispatch [:storage/request-search-options! %1 %2])
  ;;                     :option-label-f #(-> % :alias)
  ;;                     :option-value-f #(-> % :alias)
  ;;                     :options        @(r/subscribe [:db/get-in [:storage :search-options] []])
  ;;                     :override       {:placeholder "Search"
  ;;                                      :text-field {:left-adornment [:i {:class "fas fa-search" :style {:font-size "16px"}}]}}}])

(defn breadcrumbs []
  (when-let [data @(r/subscribe [:db/get-in [:storage :path]])]
    [:div {:style {:display        "flex"
                   :flex-direction "reverse-row"
                   :align-items    "center"
                   :gap            "8px"}}
     [button/view {:on-click #(do
                                (r/dispatch [:db/assoc-in [:storage :path] nil])
                                (r/dispatch [:storage/open-dir-query {:storage/id ""}]))}
       [:i {:class "fa-solid fa-house"}]]
     (map-indexed (fn [index {:keys [id alias] :as item}]
                    ^{:key id}
                    [:<>
                      [:i {:class "fa-solid fa-caret-right"
                           :style {:color     "var(--seco-clr)"
                                   :font-size "12px"}}]
                      [button/view {:on-click #(r/dispatch [:storage/back index item])}
                        alias]])
                  data)]))

(defn create-directory-button []
  [:<>
    [create-dir.popup/icon-button ::create-dir-popup]])
   
(defn file-upload-button []
  [file-selector/button (fn [files]
                          (r/dispatch [:storage/upload-files! files nil]))
    [:i {:class ["fa-solid" "fa-cloud-arrow-up"]}]])

(defn filters-button []
  [button/view {:disabled true} 
    [:i {:class "fas fa-filter"}]])

(defn view-mode-button []
  [button/view {:on-click #(r/dispatch [:db/update-in [:storage :view-mode] not])
                :style {}}
   (if @(r/subscribe [:db/get-in [:storage :view-mode]])
     [:i {:class ["fa-solid" "fa-table-list"]}]
     [:i {:class ["fa-solid" "fa-table-cells"]}])])

(defn header []
  [:div {:style {:display "grid" :gap "15px"}}
   
   [:div {:style {:display "grid" :grid-template-columns "1fr auto auto auto auto" :gap "15px"}}
     [search-field]
     [view-mode-button]
     [filters-button]
     [create-directory-button]
     [file-upload-button]]
     
   [breadcrumbs]])

;; ---- Header ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Body ----

;; ---- Columnn ----

(def columns [{:id :alias    :label "Alias"}
              {:id :added_by :label "Added by"}
              {:id :added_at :label "Added at"}])  

(defn column [{:keys [label]}]
  [button/view {:mode :clear
                :color "black"
                :style {:padding    "8px 15px"
                        :text-align "left"}}
    label])
    ;; [:i {:class "fa-solid fa-caret-down"}]])

;; ---- Column ----

;; ---- Row ----

(def mime-types
  {"image/png"     "fa-solid fa-file-image"
   "image/jpeg"    "fa-solid fa-file-image"
   "image/svg+xml" "fa-solid fa-file-image"})

(defn icon-class [{:keys [type mime_type]}]
  (if (= type "directory")
    "fa-solid fa-folder"
    (get mime-types mime_type "fa-solid fa-file")))

(defn icon [item-props]
  [:i {:class (icon-class item-props)
       :style {:min-width  "24px"
               :text-align "center"}}])

(defn name-cell [{:keys [alias _url] :as item-props}]
  [:div {:style {:display "flex" :align-items "center" :gap "6px"}}
    [icon item-props] 
    [:p alias]])

(defn date-cell [date]
  [:p {:style {:font-size "0.875rem"
               :color     "#444746"
               :padding-left "15px"}}
    (if date 
      (time/format-date-no-time date)
      "-")])

(defn row [_index {:keys [id type added_by added_at] :as item-props}]
  [:div {:key             id
         :data-id         id
         :style           {:padding     "15px"
                           :user-select "none"
                           :height      "40px"
                           :align-items "center"}
         :on-context-menu #(context-menu/open % item-props)
         :on-click        #(when (= type "directory")
                             (r/dispatch [:storage/open-dir! item-props]))
         :on-double-click #(when (= type "file")
                             (file-preview/open item-props))}
    [name-cell item-props]
    [:p added_by]
    [date-cell added_at]])

;; ---- Row ----

(defn body []
  (let [data @(r/subscribe [:db/get-in [:storage :downloaded-items]])]
    (if (empty? data)
       [:div {:style {:display     "grid"
                      :place-items "center"
                      :height      "100px"
                      :margin      "50px 0 0"
                      :font-size   "1.3rem"
                      :color       "#6c757d"}}
         [:i {:class ["fa-solid" "fa-ban"]}]
         "No files or folders found"]
      (if @(r/subscribe [:db/get-in [:storage :view-mode]])
        [table/view {:columns columns
                     :sticky-header? true
                     :layout  "2fr minmax(50px, 150px) minmax(50px, 150px)"
                     :data    (sort-by (juxt :type :alias) data)
                     :style   {:max-height "100%"
                               :height     "fit-content"}}
          column
          row]
        [grid-view/grid-view]))))

;; ---- Body ----
;; -----------------------------------------------------------------------------

(defn file-upload-zone []
  [drop-zone/view {:on-drop (fn [files data-id]
                              (r/dispatch [:storage/upload-files! files data-id]))

                   :content-f (fn [_state]
                                [:div {:style {:position    "fixed"
                                               :top         "0px"
                                               :background  "white"
                                               :padding     "15px"}}
                                  (let [id (get _state :data-id)]
                                    (if-let [alias (:alias (first (filter #(and (= "directory" (:type %)) (= id (:id %))) 
                                                                   @(r/subscribe [:db/get-in [:storage :downloaded-items]]))))]
                                      alias
                                      (-> @(r/subscribe [:db/get-in [:storage :path]])
                                          last 
                                          (get :alias "Home"))))])}])

(defn view []
  (react/useEffect
    (fn []
      (r/dispatch [:storage/open-dir-query "root"])
      (fn []))
    #js[])
  
  [:<>
    [rename-popup/view :rename-popup {}]
    [file-preview/view :file-preview {}]
    [:div {:style {:position "relative" 
                   :display  "flex" 
                   :flex-direction "column"
                   :gap      "15px" 
                   :height   "100%"}}
      [file-upload-zone]
      [header]
      [body]]])