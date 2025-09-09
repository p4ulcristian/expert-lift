(ns features.common.storage.frontend.effects
  (:require
   [re-frame.core :as r]
   [cljs.reader   :as reader]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn update-by-id [coll updated-item] 
  (mapv #(if (= (:id %) (:id updated-item))
            updated-item
            %)
        coll))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Upload Files ----

(r/reg-event-fx
 :storage/upload-files!
 (fn [{:keys [_db]} [_]]
   {:dispatch []}))

;; ---- Upload Files ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Seatch Items ----

(defn get-search-options-callback [response update-popover]
  (let [data (-> response :storage/search-suggestions)]
    (r/dispatch [:db/assoc-in [:storage :search-options] data])
    (when update-popover
      (update-popover data))))

(r/reg-event-fx :storage/request-search-options!
  (fn [{:keys [db]} [_ search-term & [update-popover]]]
    (let [params {:search-term (get-in db [:search-term] "")}]
      {:dispatch [:pathom/ws-request!
                  {:callback (fn [response]
                               (get-search-options-callback response update-popover))
                   :query    [`(:storage/search-suggestions ~params)]}]})))

;; ---- Seatch Items ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Items ----

(defn get-items-callback [response]
  (let [data (-> response :storage/search)]
    (r/dispatch [:db/assoc-in [:storage :downloaded-items] data])))

(r/reg-event-fx :storage/request-items!
  (fn [{:keys [db]} [_ & [filters]]]
    (let [params {:search-term (get-in db [:search-term])
                  :filters     (or filters (get-in db [:storage :filters]))}]
      {:dispatch [:pathom/ws-request!
                  {:callback get-items-callback
                   :query    [`(:storage/search ~params)]}]})))

;; ---- Get Items ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Open Dir ----

(defn open-dir-callback [response]
  (let [data (-> response :storage/open-dir)]
    (r/dispatch [:db/assoc-in [:storage :downloaded-items] data])))

(r/reg-event-fx :storage/open-dir-query
  (fn [{:keys [db]} [_ item & [filters]]]
    (let [params {:dir-id  (:id item false)
                  :filters (or filters (get-in db [:storage :filters]))}]
      {:dispatch-n [[:db/dissoc-in [:storage :downloaded-items]]
                    [:pathom/ws-request!
                      {:callback open-dir-callback
                       :query    [`(:storage/open-dir ~params)]}]]})))

(r/reg-event-fx :storage/back
  (fn [{:keys [db]} [_ index item]]
    {:db       (-> db
                   (update-in [:storage :path] #(->> % (take (inc index)) vec))
                   (assoc-in [:storage :filters :dest-id] (:id item)))
     :dispatch [:storage/open-dir-query item]}))

(r/reg-event-fx :storage/open-dir!
  (fn [{:keys [db]} [_ item]]
    {:db       (-> db
                   (update-in [:storage :path] #(-> % (conj item) vec))
                   (assoc-in [:storage :filters :dest-id] (:id item)))
     :dispatch [:storage/open-dir-query item]}))

(r/reg-event-fx :storage/open-current-dir!
  (fn [{:keys [db]} [_]]
    {:dispatch [:storage/open-dir-query (last (get-in db [:storage :path]))]}))

;; ---- Open Dir ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Upload File ----

(defn upload-files-callback [response into-dir?]
  (let [data (-> response reader/read-string (get 'storage/upload-files!))]
    (if (or (empty? data) (not (seq? data)))
      (r/dispatch [:notifications/error! "storage-upload" "Upload failed"])
      (do
        (when-not into-dir?
          (r/dispatch [:db/update-in [:storage :downloaded-items] concat data]))
        (r/dispatch [:notifications/success! "storage-upload" "Upload completed"])))))

(r/reg-event-fx :storage/upload-files!
  (fn [{:keys [db]} [_ files file-id]]
    (let [into-dir? (some #(and (= "directory" (:type %))            ;; check if the file(s) is uploaded into a directory, what is not opened
                                (= file-id (:id %)))
                          (get-in db [:storage :downloaded-items]))

          dest-id   (if into-dir?
                      file-id
                      (:id (last (get-in db [:storage :path]))))]
      ;; bytes to mb
      (if (> (reduce + (map #(.-size %) files)) 10000000)
        {:dispatch   [:notifications/error! "storage-upload" "Max file size is 10MB" 3000]}
        {:dispatch-n [[:notifications/loading! "storage-upload" "Uploading files..."]
                      [:pathom/frequest!
                       {:callback (fn [resp]
                                    (upload-files-callback resp into-dir?))
                        :files    files
                        :query    [`(storage/upload-files! {:destination-id ~dest-id
                                                            :bucket         "demo"})]}]]}))))
                               

;; ---- Upload File ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Delete Item ----

(defn delete-item-callback [_response item-id]
  (r/dispatch [:db/update-in [:storage :downloaded-items] #(remove (fn [item] (= (:id item) item-id)) %)]))

(r/reg-event-fx :storage/delete-item!
  (fn [{:keys [_db]} [_ item-id]]
    {:dispatch [:pathom/ws-request!
                {:callback (fn [resp]
                             (delete-item-callback resp item-id))
                 :query    [`(storage/delete-item! {:item-id ~item-id
                                                    :bucket  "demo"})]}]}))

;; ---- Delete Item ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Rename Item ----

(defn rename-item-callback [response]
  (let [data (-> response :storage/rename-item!)]
    (r/dispatch [:db/update-in [:storage :downloaded-items] #(update-by-id % data)])))

(r/reg-event-fx :storage/rename-item!
  (fn [{:keys [_db]} [_ item-id new-alias]]
    {:dispatch [:pathom/request!
                {:callback rename-item-callback
                 :query    [`(storage/rename-item! {:item-id ~item-id
                                                    :new-alias ~new-alias})]}]}))
;; ---- Rename Item ----
;; -----------------------------------------------------------------------------