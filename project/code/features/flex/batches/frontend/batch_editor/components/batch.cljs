(ns features.flex.batches.frontend.batch-editor.components.batch
  (:require ["react-sortablejs" :as ReactSortable]
            [features.flex.batches.frontend.batch-editor.components.shared :as shared]
            [features.flex.batches.frontend.batch-editor.components.process :as process]
            [features.flex.batches.frontend.batch-editor.utils :as utils]
            [features.flex.batches.frontend.batch-editor.drag-drop :as drag-drop]))

;; -----------------------------------------------------------------------------
;; ---- Batch Process Area Components ----

(defn- empty-batch-drop-zone [batch batches on-batches-change available-processes available-recipes]
  "Empty state component for when batch has no processes - accepts drops"
  [:> (.-ReactSortable ReactSortable)
   {:list (clj->js [])
    :setList (fn [new-list]
               ;; Don't automatically update - let onAdd handle it
               nil)
    :group {:name "shared" :pull true :put true}
    :onAdd (fn [evt]
             (drag-drop/handle-cross-container-add evt batch batches available-processes available-recipes on-batches-change))
    :style {:min-height "120px"
            :background-color "#fff"
            :border "2px dashed #ccc"
            :border-radius "4px"
            :padding "10px"
            :text-align "center"
            :color "#666"}}
   [:p "Drop recipes or processes here"]
   [:div {:style {:font-size "24px"}} "⚙️ 🧾"]])

(defn- batch-process-list [batch-processes batch batches on-batches-change available-processes available-recipes]
  "Renders the list of processes for a batch with sortable functionality"
  [:> (.-ReactSortable ReactSortable)
   {:list (clj->js (mapv #(assoc % :id (:process/id %)) batch-processes))
    :setList (fn [new-list]
               ;; Don't automatically update - let onAdd handle cross-container drops
               (let [js-list (js->clj new-list :keywordize-keys true)]
                 (when (= (count js-list) (count batch-processes))
                   ;; Only update for internal reordering, not cross-container adds
                   (let [batch-id (:batch/id batch)
                         new-batches (mapv #(if (= (:batch/id %) batch-id)
                                             (-> %
                                                 (assoc :batch/processes js-list))
                                             %)
                                           batches)]
                     (on-batches-change new-batches)))))
    :group {:name "shared" :pull true :put true}
    :onAdd (fn [evt]
             (drag-drop/handle-cross-container-add evt batch batches available-processes available-recipes on-batches-change))
    :animation 200
    :delay 0
    :delayOnTouchStart true
    :style {:min-height "120px"
            :background-color "#fff"
            :border "2px dashed #ccc"
            :border-radius "4px"
            :padding "10px"}}
   (map (fn [process-item] 
          ^{:key (:process/id process-item)} 
          [:div {:data-id (:process/id process-item)
                 :style {:cursor "grab"}}
           [process/process-component process-item (.indexOf batch-processes process-item) (:batch/id batch) batches on-batches-change nil]]) 
        batch-processes)])

(defn batch-processes-area [batch batches on-batches-change available-processes available-recipes]
  (let [batch-processes (:batch/processes batch)]
    (if (empty? batch-processes)
      [empty-batch-drop-zone batch batches on-batches-change available-processes available-recipes]
      [batch-process-list batch-processes batch batches on-batches-change available-processes available-recipes])))

;; -----------------------------------------------------------------------------
;; ---- Batch Component Helper Functions ----

(defn- batch-part-image-section [batch]
  "Renders batch part image if available"
  (when (:batch/part-picture-url batch)
    [:img {:src (:batch/part-picture-url batch)
           :alt (:batch/part-name batch)
           :class "batch-part-image"}]))

(defn- batch-color-info-section [batch]
  "Renders batch color information if available"
  (let [color-name (:batch/color-name batch)
        color-basecolor (:batch/color-basecolor batch)]
    (when (and color-name color-basecolor)
      [:div {:class "batch-color-info"}
       [:div {:class "batch-color-dot"
              :style {:background-color color-basecolor}}]
       [:span color-name]])))

(defn- batch-info-section [batch batches on-batches-change]
  "Renders batch information section with image and details"
  [:div {:class "batch-info"}
   [batch-part-image-section batch]
   [:div {:class "batch-details"}
    [:input {:type "text"
             :value (or (:batch/display-name batch) (:batch/description batch) "")
             :on-change #(utils/rename-batch! batches (:batch/id batch) (.. ^js % -target -value) on-batches-change)
             :class "batch-name-input"}]
    [batch-color-info-section batch]
    [shared/step-progress-indicator batch]]])

(defn- batch-controls-section [batch on-split-dialog]
  "Renders batch controls with quantity and split button"
  [:div {:class "batch-controls"}
   [:span {:class "batch-quantity"} (str "Qty: " (:batch/quantity batch))]
   [:button {:on-click #(on-split-dialog batch)
             :class "batch-split-btn"}
    "✂️"]])

(defn batch-component [batch batches on-batches-change on-split-dialog available-processes available-recipes]
  [:div {:class "batch-card"}
   [:div {:class "batch-header"}
    [batch-info-section batch batches on-batches-change]
    [batch-controls-section batch on-split-dialog]]
   [:div {:class "batch-processes"}
    [batch-processes-area batch batches on-batches-change available-processes available-recipes]]])