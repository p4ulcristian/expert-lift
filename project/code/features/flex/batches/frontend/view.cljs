(ns features.flex.batches.frontend.view
  (:require [re-frame.core :as rf]
            [router.frontend.zero :as router]
            [zero.frontend.react :as zero-react]
            [features.flex.batches.frontend.batch-editor.view :as batch-editor]
            [features.flex.batches.frontend.events]
            [features.flex.shared.frontend.components.body :as body]
            [ui.button :as button]))

;; State is now managed by re-frame in events.cljs with key :flex/batch-editor

;; Event handlers now use re-frame dispatch

(defn handle-batches-change [batches]
  (rf/dispatch [:flex/batch-editor-update-batches batches]))

(defn handle-job-name-change [job-name]
  (rf/dispatch [:flex/batch-editor-set-job-name job-name]))

(defn handle-processes-change [processes]
  (let [clj-processes (js->clj processes :keywordize-keys true)]
    (rf/dispatch [:flex/batch-editor-set-processes clj-processes])))

(defn handle-recipes-change [recipes]
  (let [clj-recipes (js->clj recipes :keywordize-keys true)]
    (rf/dispatch [:flex/batch-editor-set-recipes clj-recipes])))

(defn handle-confirm-batches [job-id]
  (rf/dispatch [:flex/batch-editor-save job-id]))

;; Main View Components

(defn navigate-back-to-order [order-id]
  (let [wsid @(rf/subscribe [:workspace/get-id])]
    (when (and wsid order-id)
      (router/navigate! {:path (str "/flex/ws/" wsid "/orders/" order-id)}))))

(defn confirm-save-button [job-id]
  (when job-id
    (let [loading @(rf/subscribe [:flex/batch-editor-loading?])
          saving? (:saving loading)]
      [button/view {:mode :filled
                    :color "var(--seco-clr)"
                    :style {:fontWeight 500 
                            :padding "12px 24px"}
                    :disabled saving?
                    :on-click #(handle-confirm-batches job-id)}
       (if saving? "Saving..." "Confirm & Save Batches")])))

(defn back-to-order-button [order-id]
  (when order-id
    [button/view {:mode :outlined
                  :color "var(--seco-clr)"
                  :style {:fontWeight 500 
                          :padding "8px 20px"}
                  :on-click #(navigate-back-to-order order-id)}
     "Back to Jobs"]))

(defn batch-creation-body [job-id]
  (let [loading @(rf/subscribe [:flex/batch-editor-loading?])
        batches @(rf/subscribe [:flex/batch-editor-batches])
        job-name @(rf/subscribe [:flex/batch-editor-job-name])
        processes @(rf/subscribe [:flex/batch-editor-processes])
        recipes @(rf/subscribe [:flex/batch-editor-recipes])
        save-status @(rf/subscribe [:flex/batch-editor-save-status])]
    [:div
     ;; Loading States
     (when (or (:batches loading) (:processes loading) (:recipes loading))
       [:div {:style {:padding "20px" :text-align "center" :color "#666"}}
        "Loading data from backend..."])
     
     ;; Main BatchEditor Component
     (if job-id
       [:div
        [batch-editor/batch-editor
         {:initial-job-name job-name
          :initial-batches batches
          :initial-available-processes processes
          :initial-available-recipes recipes
          :on-batches-change handle-batches-change
          :on-job-name-change handle-job-name-change
          :on-processes-change handle-processes-change
          :on-recipes-change handle-recipes-change}]
        
        ;; Status Message
        (when (:show save-status)
          [:div {:style {:padding "10px 20px" 
                         :text-align "center" 
                         :background-color (if (:success save-status) "#d4edda" "#f8d7da")
                         :color (if (:success save-status) "#155724" "#721c24")
                         :border (str "1px solid " (if (:success save-status) "#c3e6cb" "#f5c6cb"))
                         :border-radius "4px"
                         :margin "0 20px"}}
           (:message save-status)])]
       [:div {:style {:padding "20px" :text-align "center" :color "#666"}}
        "No job ID provided in URL"])]))

(defn view []
  (let [job-id @(rf/subscribe [:db/get-in [:router :path-params :job_id]])
        workspace-id @(rf/subscribe [:workspace/get-id])
        batches @(rf/subscribe [:flex/batch-editor-batches])]
    (zero-react/use-effect
     {:mount (fn []
               (when (and job-id workspace-id)
                 (rf/dispatch [:flex/batch-editor-load-all job-id])))
      :params #js [workspace-id job-id]})
    
    (let [order-id (get-in (first batches) [:batch/order-id])]
      [body/view
       {:title "Batch creation"
        :description "Create and configure production batches with manufacturing processes"
        :title-buttons (list
                        ^{:key "confirm"}
                        [confirm-save-button job-id]
                        ^{:key "back"}
                        [back-to-order-button order-id])
        :body [batch-creation-body job-id]}])))