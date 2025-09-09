(ns features.flex.batches.frontend.batch-editor.modals
  (:require [reagent.core :as r]
            [zero.frontend.re-frame :as rf]
            [ui.button :as button]
            [features.flex.batches.frontend.batch-editor.utils :as utils]))

;; -----------------------------------------------------------------------------
;; ---- Split Modal Components ----

(defn- current-batch-info-section [batch]
  "Displays the current batch information"
  [:div {:style {:margin-bottom "24px"}}
   [:div {:style {:font-size "16px"
                  :font-weight "600"
                  :color "#333"
                  :margin-bottom "8px"}}
    "Current Batch"]
   [:div {:style {:font-size "14px"
                  :color "#666"
                  :background-color "#f8f9fa"
                  :padding "12px"
                  :border-radius "6px"
                  :border "1px solid #e9ecef"}}
    (str "Quantity: " (:quantity batch))]])

(defn- split-quantity-input-section [batch split-quantity]
  "Input section for specifying split quantity"
  [:div {:style {:margin-bottom "24px"}}
   [:label {:style {:display "block"
                    :font-size "14px"
                    :font-weight "500"
                    :color "#333"
                    :margin-bottom "8px"}}
    "Split Quantity:"]
   [:input {:type "number"
            :min 1
            :max (dec (:quantity batch))
            :value @split-quantity
            :on-change #(reset! split-quantity (js/parseInt (.. ^js % -target -value) 10))
            :style {:width "100%"
                    :padding "8px 12px"
                    :border "1px solid #d1d5db"
                    :border-radius "6px"
                    :font-size "14px"
                    :outline "none"
                    :transition "border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out"}
            :on-focus #(set! (-> ^js % .-target .-style .-borderColor) "#5da7d9")
            :on-blur #(set! (-> ^js % .-target .-style .-borderColor) "#d1d5db")}]])

(defn- split-result-preview-section [batch split-quantity]
  "Preview section showing the result of the split"
  [:div {:style {:background-color "#e8f4f8"
                 :border "1px solid #b8e6f0"
                 :border-radius "6px"
                 :padding "16px"}}
   [:div {:style {:font-size "14px"
                  :font-weight "500"
                  :color "#0f5132"
                  :margin-bottom "8px"}}
    "Split Result:"]
   [:div {:style {:font-size "14px"
                  :color "#0f5132"
                  :line-height "1.5"}}
    [:div (str "Original batch: " (- (:quantity batch) @split-quantity) " units")]
    [:div (str "New batch: " @split-quantity " units")]]])

(defn- split-modal-body-content [batch split-quantity]
  "Main body content of the split modal"
  [:div {:style {:overflow-y "auto"
                 :flex-grow 1
                 :padding "20px"
                 :padding-bottom "10px"}}
   [current-batch-info-section batch]
   [split-quantity-input-section batch split-quantity]
   [split-result-preview-section batch split-quantity]])

(defn- split-modal-footer-buttons [batch batches split-quantity on-batches-change]
  "Footer buttons for the split modal"
  [:div {:style {:display "flex"
                 :justify-content "flex-end"
                 :gap "12px"
                 :flex-shrink 0
                 :padding "20px"
                 :padding-top "10px"
                 :border-top "1px solid #e9ecef"}}
   [button/view {:mode :clear_2
                 :type :secondary
                 :on-click #(rf/dispatch [:modals/close :split-batch])}
    "Cancel"]
   [button/view {:mode :filled
                 :type :primary
                 :on-click #(do
                              (utils/create-new-batch! batches (:id batch) @split-quantity on-batches-change)
                              (rf/dispatch [:modals/close :split-batch]))}
    "Split Batch"]])

(defn split-batch-modal-content [batch batches on-batches-change]
  (let [split-quantity (r/atom 1)]
    (fn [batch batches on-batches-change]
      [:div {:style {:min-width "400px"
                     :max-height "80vh"
                     :display "flex"
                     :flex-direction "column"}}
       [split-modal-body-content batch split-quantity]
       [split-modal-footer-buttons batch batches split-quantity on-batches-change]])))

(defn open-split-modal! [batch batches on-batches-change]
  (rf/dispatch [:modals/add {:id :split-batch
                             :open? true
                             :label (str "Split Batch: " (:name batch))
                             :content [split-batch-modal-content batch batches on-batches-change]}])
  (rf/dispatch [:modals/open :split-batch]))