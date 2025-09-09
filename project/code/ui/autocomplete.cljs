(ns ui.autocomplete
  (:require
   [clojure.string :as clojure.string]
   [re-frame.core :as r]
   [ui.text-field :as text-field]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn filter-options [options search-text option-label-f]
  (let [options (remove nil? options)]
    (if (or (empty? search-text)
            (empty? options))
      options
      (filter (fn [option]
                (clojure.string/starts-with?
                  (clojure.string/lower-case (if option-label-f (option-label-f option) option))
                  (clojure.string/lower-case search-text)))
              options))))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn no-result-option [{:keys [value override] :as _props}]
  [:div (merge {:class "autocomplete-options-popover-empty"
                :style {:padding "8px" :font-size "12px"}}
               (:option override))
    (if-let [no-option-element (:no-option override)]
      [no-option-element value]
      "No results found")])

(defn option-element [{:keys [option-label-f option-value-f on-change on-select override] :as _props}
                      option popover-id]
  [:button (merge {:class    "autocomplete-options-popover-option"
                   :on-click (fn []
                                (let [option-value (if option-value-f
                                                     (option-value-f option)
                                                     option)]
                                  (when on-change
                                    (on-change option-value))
                                  
                                  (when on-select
                                    (on-select option)))
                                (r/dispatch [:popover/close popover-id]))}
                  (:option override))
        
     (if option-label-f
       (option-label-f option)
       option)])

(defn options-popover [{:keys [override] :as _props} popover-id filtered-options]
  [:div (merge {:class "autocomplete-options-popover hide-scroll"}
               (:popover override))
         
    (if (empty? filtered-options)
       [no-result-option _props]
    
       (for [[_index option] (map-indexed vector filtered-options)]
         ^{:key option}
         [option-element _props option popover-id]))])

(defn open-popover [container-id popover-id props filtered-options]
  (r/dispatch [:popover/open popover-id 
                {:id            popover-id
                 :content       [options-popover props popover-id filtered-options]
                 :target        (.getElementById js/document container-id)
                 :align         [:bottom :left]
                 :width         :inherit
                 :anchor        [:top :left]
                 :on-click-away #(when (not (.contains (.getElementById js/document container-id) 
                                                       (.-target ^js %)))
                                   (r/dispatch [:popover/close popover-id]))}]))

(defn update-popover [{:keys [id] :as props} popover-id filtered-options]
  (r/dispatch-sync [:popover/update popover-id 
                      {:content [options-popover props popover-id filtered-options]
                       :target  (.getElementById js/document id)
                       :align   [:bottom :left]
                       :anchor  [:top :left]
                       :width   :inherit}]))

(defn autocomplete [{:keys [id label placeholder disabled style value on-click on-change on-type-ended on-enter options option-label-f override]
                     :or   {value nil
                            id    (random-uuid)}
                     :as   props}]

  (let [filtered-options (filter-options options value option-label-f)
        popover-id       (str id "-popover")]
    
    [:div {:id    id
           :class "autocomplete-container"}
      [text-field/view
        (merge {:id            (str id "-text-field")
                :label         label
                :placeholder   placeholder
                :disabled      disabled
                :style         style
                :value         value
                :on-click      on-click
                :on-enter      on-enter
                :on-type-ended (fn [new-value]
                                 (when on-type-ended
                                   (on-type-ended new-value
                                     (fn [_options]
                                       (let [_filtered-options (filter-options _options (or new-value "") option-label-f)]
                                         (update-popover props popover-id _filtered-options))))))
                :on-change (fn [new-value]
                             (on-change new-value)
                             (let [_filtered-options (filter-options options (or new-value "") option-label-f)]
                               (update-popover props popover-id _filtered-options)))
                :override {:on-mouse-down #(open-popover id popover-id props filtered-options)
                           :on-focus      #(open-popover id popover-id props filtered-options)
                           :on-key-down   #(when (= (.-key ^js %) "Tab")
                                             (r/dispatch [:popover/close popover-id]))
                           :on-blur       #(when (not (.contains (.getElementById js/document id) (.-target ^js %)))
                                             (r/dispatch [:popover/close popover-id]))}}
               (:text-field override))]]))

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view [props]
  [autocomplete props])

