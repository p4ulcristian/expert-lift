(ns features.flex.batches.frontend.events
  (:require
   [re-frame.core :as rf]
   [features.flex.batches.frontend.request :as batches-request]
   [features.flex.processes.frontend.request :as processes-request]
   [features.flex.recipes.frontend.request :as recipes-request]
   [clojure.string :as clojure.string]))

;; Subscriptions
(rf/reg-sub :flex/batch-editor
  (fn [db _]
    (get-in db [:flex/batch-editor])))

(rf/reg-sub :flex/batch-editor-loading?
  (fn [db _]
    (get-in db [:flex/batch-editor :loading] {})))

(rf/reg-sub :flex/batch-editor-batches
  (fn [db _]
    (get-in db [:flex/batch-editor :batches] [])))

(rf/reg-sub :flex/batch-editor-job-name
  (fn [db _]
    (get-in db [:flex/batch-editor :job-name] "")))

(rf/reg-sub :flex/batch-editor-processes
  (fn [db _]
    (get-in db [:flex/batch-editor :available-processes] [])))

(rf/reg-sub :flex/batch-editor-recipes
  (fn [db _]
    (get-in db [:flex/batch-editor :available-recipes] [])))

(rf/reg-sub :flex/batch-editor-save-status
  (fn [db _]
    (get-in db [:flex/batch-editor :save-status] {:success false :message "" :show false})))

;; Helper functions
(defn generate-initial-batch-name 
  "Generate smart initial batch name when loading from backend"
  [batch]
  (let [part-name (or (:batch/part-name batch) "Part")
        color-name (or (:batch/color-name batch) "")
        quantity (:batch/quantity batch)
        description (:batch/description batch)
        
        ; Use existing description if it looks custom, otherwise generate smart name
        has-custom-description (and description 
                                   (not (clojure.string/starts-with? description "Batch "))
                                   (not (= description part-name)))
        
        smart-name (if (and color-name (not= color-name ""))
                     (str part-name " - " color-name " (" quantity " units)")
                     (str part-name " (" quantity " units)"))]
    
    (if has-custom-description
      description  ; Keep existing custom names
      smart-name))) ; Use smart naming for generic names

;; Events
(rf/reg-event-db :flex/batch-editor-set-loading
  (fn [db [_ loading-key loading?]]
    (assoc-in db [:flex/batch-editor :loading loading-key] loading?)))

(rf/reg-event-db :flex/batch-editor-set-batches
  (fn [db [_ batches]]
    (let [transformed-batches 
          (mapv (fn [batch]
                  (-> batch
                      ;; Add only essential UI fields
                      (assoc :display-name (generate-initial-batch-name batch))
                      ;; Ensure processes have proper color for UI
                      (update :batch/processes 
                              (fn [processes]
                                (mapv (fn [process]
                                        (assoc process :color (or (:color process) "#666")))
                                      (or processes []))))))
                batches)]
      (-> db
          (assoc-in [:flex/batch-editor :batches] transformed-batches)
          (assoc-in [:flex/batch-editor :loading :batches] false)))))

(rf/reg-event-db :flex/batch-editor-set-processes
  (fn [db [_ processes]]
    (let [transformed-processes
          (mapv (fn [process]
                  (-> process
                      (assoc :color (or (:color process) "#666")))) 
                processes)]
      (-> db
          (assoc-in [:flex/batch-editor :available-processes] transformed-processes)
          (assoc-in [:flex/batch-editor :loading :processes] false)))))

(rf/reg-event-db :flex/batch-editor-set-recipes
  (fn [db [_ recipes]]
    (let [transformed-recipes
          (mapv (fn [recipe]
                  (-> recipe
                      (assoc :type "recipe")
                      (update :processes
                              (fn [processes]
                                (mapv (fn [process]
                                        (-> process
                                            (assoc :color (or (:color process) "#666"))))
                                      processes)))))
                recipes)]
      (-> db
          (assoc-in [:flex/batch-editor :available-recipes] transformed-recipes)
          (assoc-in [:flex/batch-editor :loading :recipes] false)))))

(rf/reg-event-db :flex/batch-editor-update-batches
  (fn [db [_ batches]]
    (let [clj-batches (js->clj batches :keywordize-keys true)
          processed-batches 
          (mapv (fn [batch]
                  (-> batch
                      ;; Ensure quantity is properly converted
                      (update :batch/quantity #(if (number? %) % (js/parseInt % 10)))
                      ;; Ensure other required fields exist
                      (assoc :batch/name (or (:batch/name batch) (:batch/description batch) (str "Batch " (:batch/id batch))))
                      (assoc :batch/status (or (:batch/status batch) "awaiting"))))
                clj-batches)]
      (assoc-in db [:flex/batch-editor :batches] processed-batches))))

(rf/reg-event-db :flex/batch-editor-set-job-name
  (fn [db [_ job-name]]
    (assoc-in db [:flex/batch-editor :job-name] job-name)))

(rf/reg-event-db :flex/batch-editor-set-save-status
  (fn [db [_ status]]
    (assoc-in db [:flex/batch-editor :save-status] status)))

;; Load batches for job
(rf/reg-event-fx :flex/batch-editor-load-batches
  (fn [{:keys [db]} [_ job-id]]
    (let [workspace-id (get-in db [:workspace :workspace/id])]
      {:db (assoc-in db [:flex/batch-editor :loading :batches] true)
       :fx [[:dispatch [:flex/batch-editor-load-batches-request workspace-id job-id]]]})))

(rf/reg-event-fx :flex/batch-editor-load-batches-request
  (fn [_ [_ workspace-id job-id]]
    (batches-request/get-batches workspace-id job-id
      (fn [batches]
        (if (and batches (seq batches))
          (rf/dispatch [:flex/batch-editor-set-batches batches])
          (do
            (rf/dispatch [:flex/batch-editor-set-batches []])))))
    {}))

;; Load processes
(rf/reg-event-fx :flex/batch-editor-load-processes
  (fn [{:keys [db]} _]
    (let [workspace-id (get-in db [:workspace :workspace/id])]
      {:db (assoc-in db [:flex/batch-editor :loading :processes] true)
       :fx [[:dispatch [:flex/batch-editor-load-processes-request workspace-id]]]})))

(rf/reg-event-fx :flex/batch-editor-load-processes-request
  (fn [_ [_ workspace-id]]
    (processes-request/get-processes workspace-id
      (fn [processes]
        (rf/dispatch [:flex/batch-editor-set-processes processes])))
    {}))

;; Load recipes
(rf/reg-event-fx :flex/batch-editor-load-recipes
  (fn [{:keys [db]} _]
    (let [workspace-id (get-in db [:workspace :workspace/id])]
      {:db (assoc-in db [:flex/batch-editor :loading :recipes] true)
       :fx [[:dispatch [:flex/batch-editor-load-recipes-request workspace-id]]]})))

(rf/reg-event-fx :flex/batch-editor-load-recipes-request
  (fn [_ [_ workspace-id]]
    (recipes-request/get-recipes workspace-id
      (fn [recipes]
        (rf/dispatch [:flex/batch-editor-set-recipes recipes])))
    {}))

;; Load all data
(rf/reg-event-fx :flex/batch-editor-load-all
  (fn [_ [_ job-id]]
    {:fx [[:dispatch [:flex/batch-editor-load-batches job-id]]
          [:dispatch [:flex/batch-editor-load-processes]]
          [:dispatch [:flex/batch-editor-load-recipes]]]}))

;; Save batches
(rf/reg-event-fx :flex/batch-editor-save
  (fn [{:keys [db]} [_ job-id]]
    (let [current-batches (get-in db [:flex/batch-editor :batches])
          workspace-id (get-in db [:workspace :workspace/id])
          validated-batches 
          (mapv (fn [batch]
                  (-> batch
                      ;; Ensure quantity is a number and not null/undefined
                      (assoc :batch/quantity (or (:batch/quantity batch) 1))
                      ;; Ensure name is present
                      (assoc :batch/name (or (:batch/name batch) (str "Batch " (:batch/id batch))))
                      ;; Ensure status is present
                      (assoc :batch/status (or (:batch/status batch) "awaiting"))))
                current-batches)]
      {:db (-> db
               (assoc-in [:flex/batch-editor :loading :saving] true)
               (assoc-in [:flex/batch-editor :save-status :show] false))
       :fx [[:dispatch [:flex/batch-editor-save-request workspace-id job-id validated-batches]]]})))

(rf/reg-event-fx :flex/batch-editor-save-request
  (fn [_ [_ workspace-id job-id batches]]
    (batches-request/create-batch workspace-id {:job/id job-id :batches batches}
      (fn [response]
        (let [result (:batches/create-batch response)]
          (rf/dispatch [:flex/batch-editor-set-loading :saving false])
          (if (:success result)
            (do
              (rf/dispatch [:flex/batch-editor-set-save-status 
                           {:success true
                            :message (or (:message result) "Batches saved successfully!")
                            :show true}])
              (rf/dispatch [:flex/batch-editor-load-batches job-id])
              (js/setTimeout #(rf/dispatch [:flex/batch-editor-set-save-status {:show false}]) 3000))
            (do
              (let [error-msg (or (:error result) 
                                 (:message result)
                                 (str "Unexpected response format: " result))]
                (rf/dispatch [:flex/batch-editor-set-save-status 
                             {:success false
                              :message error-msg
                              :show true}]))
              (js/setTimeout #(rf/dispatch [:flex/batch-editor-set-save-status {:show false}]) 5000))))))
    {}))