(ns features.common.storage.frontend.picker
  (:require
   ["react" :as react]
   [re-frame.core :as r]
   [utils.time    :as time]
   
   [ui.button         :as button]
   [ui.text-field     :as text-field]
   [ui.table          :as table]
   [ui.file-drop-zone :as drop-zone]
   [ui.file-selector  :as file-selector]

   [features.common.storage.frontend.effects]
   [features.common.storage.frontend.blocks.create-dir   :as create-dir.popup]
   [features.common.storage.frontend.blocks.rename-popup :as rename-popup]
   [features.common.storage.frontend.blocks.context-menu :as context-menu]
   [features.common.storage.frontend.blocks.file-preview :as file-preview]))

;; -----------------------------------------------------------------------------
;; ---- Subs ----

(r/reg-sub
  :storage.picker/selected?
  (fn [db [_ multiple? value]]
    (let [selected (get-in db [:storage :picker :selected])]
      (if multiple?
        (some #(= (:url %) (:url value)) selected)
        (= (:url selected) (:url value))))))

;; ---- Subs ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Effects ----

(r/reg-event-db
  :storage.picker.multi/select!
  (fn [db [_ selected? row-props]]
    
    (if selected?
      (update-in db [:storage :picker :selected] #(remove (fn [selected] (= (:url selected) (:url row-props))) %))
      (update-in db [:storage :picker :selected] conj row-props))))
 
(r/reg-event-db
  :storage.picker.solo/select!
  (fn [db [_ selected? row-props]]
    (if selected?
      (assoc-in db [:storage :picker :selected] nil)
      (assoc-in db [:storage :picker :selected] row-props))))

(r/reg-event-fx
  :storage.picker/mount
  (fn [{:keys [_db]} [_ {:keys [multiple?] :as props}]]
    {:dispatch-n [[:db/assoc-in [:storage :filters] {:mime (:accept props)}]
                  [:storage/open-dir-query "root"]]
     :dispatch   
        (if multiple?
          [:db/assoc-in [:storage :picker :selected] (set (:value props []))]
          [:db/assoc-in [:storage :picker :selected] (:value props)])}))

(r/reg-event-db
  :storage.picker/clean-up
  (fn [db [_]]
    (update-in db [:storage] dissoc :picker :filters)))

;; ---- Effects ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn search-field []
  [text-field/view {:value          @(r/subscribe [:db/get-in [:search-term] ""])
                    :on-change      #(r/dispatch [:db/assoc-in [:search-term] %])
                    :on-enter       #(if (empty? %)
                                       (r/dispatch [:storage/open-current-dir!])
                                       (r/dispatch [:storage/request-items!]))
                    :on-type-ended  #(if (empty? %)
                                       (r/dispatch [:storage/open-current-dir!])
                                       (r/dispatch [:storage/request-items!]))
                    :left-adornment [:i {:class "fas fa-search" :style {:font-size "16px"}}]
                    :placeholder "Search"}])

(defn upload-button []
  [file-selector/button (fn [files]
                          (r/dispatch [:storage/upload-files! files nil]))
    [:i {:class ["fa-solid" "fa-cloud-arrow-up"]}]])

(defn breadcrumbs []
  (when-let [data @(r/subscribe [:db/get-in [:storage :path]])]
    [:div {:style {:display        "flex"
                   :gap            "8px"
                   :flex-direction "reverse-row"
                   :align-items    "center"}}
      [button/view {:mode :clear_2
                    :type :secondary
                    :style {:padding "6px"
                            :border-radius "6px"}
                    :on-click #(do
                                 (r/dispatch [:db/assoc-in [:storage :path] nil])
                                 (r/dispatch [:db/assoc-in [:storage :filters :dest-id] nil])
                                 (r/dispatch [:storage/open-dir-query "root"]))}
        [:i {:class "fa-solid fa-house"}]]
      (map-indexed (fn [index {:keys [id alias] :as item}]
                     ^{:key id}
                     [:<>
                       [:i {:class "fa-solid fa-caret-right"
                            :style {:color "var(--seco-clr)"
                                    :font-size "12px"}}]
                       [button/view {:mode :clear_2
                                     :type :secondary
                                     :style {:padding "3px 6px"
                                             :border-radius "6px"}
                                     :on-click #(r/dispatch [:storage/back index item])}
                         alias]])
                   data)]))

(defn header [_props]
  [:<>
    [:div {:style {:display               "grid"
                   :gap                   "8px"
                   :grid-template-columns "1fr auto auto"}}
      [search-field]
      [create-dir.popup/icon-button {}]
      [upload-button]]
    [breadcrumbs]])

;; -----------------------------------------------------------------------------
;; ---- Table ----

;; ---- Column ----

(def columns [{:id :alias    :label "Alias"}
              {:id :added_by :label "Added by"}
              {:id :added_at :label "Added on"}])

(defn column [{:keys [label]}]
  [button/view {:mode :clear
                :color "black"
                :style {:padding    "8px 15px"
                        :text-align "left"}}
   label])

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

(defn icon [_selected? item-props]
  [:i {:class (icon-class item-props)
       :style {:min-width  "24px"
               :text-align "center"}}])

(defn selected-flag [selected?]
  (if selected?
    [:i {:class ["fa-solid" "fa-circle-check"]
         :style {:color "var(--prim-clr)"}}]
    [:i {:class ["fa-regular" "fa-circle"]}]))

(defn name-cell [selected? {:keys [alias type _url] :as row-props}]
  [:div {:style {:display "flex" :align-items "center" :gap "6px"}}
    
    (when (= type "file") [selected-flag selected?])
    [icon selected? row-props]
    [:p alias]])

(defn date-cell [{:keys [added_at]}]
  [:p {:style {:font-size    "0.875rem"
               :color        "#444746"
               :padding-left "15px"}}
   (if added_at
     (time/format-date-no-time added_at)
     "-")])

(defn row [_index {:keys [id type _url] :as row-props} 
                 {{:keys [multiple?]} :picker-props}]
  (let [selected? (when (= type "file") @(r/subscribe [:storage.picker/selected? multiple? row-props]))]
    [:div {:key             id
           :data-id         id
           :on-context-menu #(context-menu/open % row-props)
           :on-double-click #(when (= type "directory")
                               (r/dispatch [:storage/open-dir! row-props]))
           :on-click        #(when (= type "file")
                               (if multiple?
                                 (r/dispatch [:storage.picker.multi/select! selected? row-props])
                                 (r/dispatch [:storage.picker.solo/select! selected? row-props])))
           :style           {:padding     "15px"
                             :user-select "none"
                             :align-items "center"
                             :background  (when selected? "hsl(from var(--prim-clr) h s l / 0.2)")}}
     [name-cell selected? row-props]
     [:p (:added_by row-props)]
     [date-cell row-props]]))

;; ---- Row ----

(defn item-table [props]
  [:div {:style {:flex-grow 1 :overflow "auto"}}
    [table/view {:columns columns
                 :sticky-header? true
                 :layout  "2fr minmax(50px, 150px) minmax(50px, 150px)"
                 :data    (sort-by :type @(r/subscribe [:db/get-in [:storage :downloaded-items]]))
                 :style   {:max-height "100%"
                           :height     "fit-content"}
                 :picker-props props}
      column
      row]])

;; ---- Table ----
;; -----------------------------------------------------------------------------

(defn file-upload-zone []
  [drop-zone/view {:on-drop (fn [files data-id]
                              (r/dispatch [:storage/upload-files! files data-id]))
                   
                   :content-f (fn [_state]
                                [:div {:style {:background    "white"
                                               :padding       "15px"
                                               :border-radius "6px"}}
                                  (let [id (get _state :data-id)]
                                    (if-let [alias (:alias (first (filter #(and (= "directory" (:type %)) (= id (:id %))) 
                                                                   @(r/subscribe [:db/get-in [:storage :downloaded-items]]))))]
                                      alias
                                      (-> @(r/subscribe [:db/get-in [:storage :path]])
                                          last 
                                          (get :alias "Home"))))])}])

(defn confirm-button [props]
  (let [selected @(r/subscribe [:db/get-in [:storage :picker :selected]])]
    [button/view {:type     :primary
                  :disabled (empty? selected)
                  :on-click (fn []
                              (when-let [on-select (:on-select props)]
                                (let [value (if (:multiple? props)
                                              (vec selected)
                                              selected)]
                                  (on-select value)))
                              (r/dispatch [:modals/close :storage.picker/modal]))}
      "Select"]))

(defn picker-modal [props]
  (react/useLayoutEffect
    (fn []
      (r/dispatch [:storage.picker/mount props])
      (fn []
        (r/dispatch [:storage.picker/clean-up])))  
    #js[])
  
  [:<>
    [rename-popup/view :rename-popup {}]
    [file-preview/view :file-preview {}]
    [file-upload-zone]
    [:div {:style {:height         "100%"
                   :display        "flex"
                   :gap            "15px"
                   :flex-direction "column"
                   :overflow       "hidden"}}
      [header props]
      [item-table props]
      [confirm-button props]]])

(defn picker [props]
  [:<>
    [button/view {:style {:display "flex"
                          :align-items "center"
                          :gap "12px"}
                  :on-click
                  #(r/dispatch [:modals/add {:id      :storage.picker/modal
                                             :label   "Media Picker"
                                             :content [picker-modal props]
                                             :open?   true}])}
   
      [:i {:class ["fa-solid" "fa-file-arrow-down"]}]
      (or (:label props) (if (:multiple? props) "Select Files" "Select File"))]])
  
;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [props]
  [:<>
    ;; [:p (str (:value props))]
    [picker props]])