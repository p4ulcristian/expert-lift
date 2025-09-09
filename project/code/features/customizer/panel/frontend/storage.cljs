(ns features.customizer.panel.frontend.storage
  (:require
   [re-frame.core :as r]))

;; -----------------------------------------------------------------------------
;; ---- Constants ----

(def STORAGE_KEYS
  {:customizer-state "customizer-state"
   :customizer-data "customizer-data"
   :selected-look "customizer-selected-look"
   :form-data "customizer-form-data"
   :cart-content "customizer-cart-content"
   :recent-items "customizer-recent-items"})

;; -----------------------------------------------------------------------------
;; ---- Core Storage Functions ----

(defn put-data!
  "Store data in localStorage with the given key"
  [key data]
  (try
    (let [json-data (js/JSON.stringify (clj->js data))]
      (.setItem js/localStorage key json-data)
      true)
    (catch :default e
      (println "Error storing data in localStorage:" e)
      false)))

(defn get-data
  "Retrieve data from localStorage with the given key"
  [key]
  (try
    (let [json-data (.getItem js/localStorage key)]
      (if json-data
        (js->clj (js/JSON.parse json-data) :keywordize-keys true)
        nil))
    (catch :default e
      (println "Error retrieving data from localStorage:" e)
      nil)))

(defn delete-data!
  "Remove data from localStorage with the given key"
  [key]
  (try
    (.removeItem js/localStorage key)
    true
    (catch :default e
      (println "Error deleting data from localStorage:" e)
      false)))

(defn clear-all!
  "Clear all customizer-related data from localStorage"
  []
  (try
    (doseq [[_ key] STORAGE_KEYS]
      (.removeItem js/localStorage key))
    true
    (catch :default e
      (println "Error clearing localStorage:" e)
      false)))

;; -----------------------------------------------------------------------------
;; ---- Customizer-Specific Functions ----

(defn save-customizer-state!
  "Save the current customizer state"
  [state]
  (put-data! (:customizer-state STORAGE_KEYS) state))

(defn load-customizer-state
  "Load the saved customizer state"
  []
  (get-data (:customizer-state STORAGE_KEYS)))

(defn save-customizer-data!
  "Save customizer data (parts, packages, etc.)"
  [data]
  (put-data! (:customizer-data STORAGE_KEYS) data))

(defn load-customizer-data
  "Load saved customizer data"
  []
  (get-data (:customizer-data STORAGE_KEYS)))

(defn save-selected-look!
  "Save the currently selected look"
  [look-data]
  (put-data! (:selected-look STORAGE_KEYS) look-data))

(defn load-selected-look
  "Load the saved selected look"
  []
  (get-data (:selected-look STORAGE_KEYS)))

(defn save-form-data!
  "Save form data for the current item"
  [form-data]
  (put-data! (:form-data STORAGE_KEYS) form-data))

(defn load-form-data
  "Load saved form data"
  []
  (get-data (:form-data STORAGE_KEYS)))

(defn save-cart-content!
  "Save cart content"
  [cart-data]
  (put-data! (:cart-content STORAGE_KEYS) cart-data))

(defn load-cart-content
  "Load saved cart content"
  []
  (get-data (:cart-content STORAGE_KEYS)))

(defn save-recent-items!
  "Save recent items for quick access"
  [items]
  (put-data! (:recent-items STORAGE_KEYS) items))

(defn load-recent-items
  "Load saved recent items"
  []
  (get-data (:recent-items STORAGE_KEYS)))

;; -----------------------------------------------------------------------------
;; ---- Re-frame Events ----

(r/reg-event-fx
 :customizer.storage/save-state!
 (fn [_ [_ state]]
   {:fx [[:local-storage/save-customizer-state! state]]}))

(r/reg-event-fx
 :customizer.storage/load-state!
 (fn [_ [_]]
   {:fx [[:local-storage/load-customizer-state!]]}))

(r/reg-event-fx
 :customizer.storage/save-data!
 (fn [_ [_ data]]
   {:fx [[:local-storage/save-customizer-data! data]]}))

(r/reg-event-fx
 :customizer.storage/load-data!
 (fn [_ [_]]
   {:fx [[:local-storage/load-customizer-data!]]}))

(r/reg-event-fx
 :customizer.storage/save-look!
 (fn [_ [_ look-data]]
   {:fx [[:local-storage/save-selected-look! look-data]]}))

(r/reg-event-fx
 :customizer.storage/load-look!
 (fn [_ [_]]
   {:fx [[:local-storage/load-selected-look!]]}))

(r/reg-event-fx
 :customizer.storage/save-form!
 (fn [_ [_ form-data]]
   {:fx [[:local-storage/save-form-data! form-data]]}))

(r/reg-event-fx
 :customizer.storage/load-form!
 (fn [_ [_]]
   {:fx [[:local-storage/load-form-data!]]}))

(r/reg-event-fx
 :customizer.storage/save-cart!
 (fn [_ [_ cart-data]]
   {:fx [[:local-storage/save-cart-content! cart-data]]}))

(r/reg-event-fx
 :customizer.storage/load-cart!
 (fn [_ [_]]
   {:fx [[:local-storage/load-cart-content!]]}))

(r/reg-event-fx
 :customizer.storage/clear-all!
 (fn [_ [_]]
   {:fx [[:local-storage/clear-all!]]}))

;; -----------------------------------------------------------------------------
;; ---- Re-frame Effects ----

(r/reg-fx
 :local-storage/save-customizer-state!
 (fn [state]
   (save-customizer-state! state)))

(r/reg-fx
 :local-storage/load-customizer-state!
 (fn [_]
   (when-let [state (load-customizer-state)]
     (r/dispatch [:customizer.storage/state-loaded! state]))))

(r/reg-fx
 :local-storage/save-customizer-data!
 (fn [data]
   (save-customizer-data! data)))

(r/reg-fx
 :local-storage/load-customizer-data!
 (fn [_]
   (when-let [data (load-customizer-data)]
     (r/dispatch [:customizer.storage/data-loaded! data]))))

(r/reg-fx
 :local-storage/save-selected-look!
 (fn [look-data]
   (save-selected-look! look-data)))

(r/reg-fx
 :local-storage/load-selected-look!
 (fn [_]
   (when-let [look-data (load-selected-look)]
     (r/dispatch [:customizer.storage/look-loaded! look-data]))))

(r/reg-fx
 :local-storage/save-form-data!
 (fn [form-data]
   (save-form-data! form-data)))

(r/reg-fx
 :local-storage/load-form-data!
 (fn [_]
   (when-let [form-data (load-form-data)]
     (r/dispatch [:customizer.storage/form-loaded! form-data]))))

(r/reg-fx
 :local-storage/save-cart-content!
 (fn [cart-data]
   (save-cart-content! cart-data)))

(r/reg-fx
 :local-storage/load-cart-content!
 (fn [_]
   (when-let [cart-data (load-cart-content)]
     (r/dispatch [:customizer.storage/cart-loaded! cart-data]))))

(r/reg-fx
 :local-storage/clear-all!
 (fn [_]
   (clear-all!)))

;; -----------------------------------------------------------------------------
;; ---- Re-frame Events for Loading Results ----

(r/reg-event-db
 :customizer.storage/state-loaded!
 (fn [db [_ state]]
   (assoc-in db [:customizer :saved-state] state)))

(r/reg-event-db
 :customizer.storage/data-loaded!
 (fn [db [_ data]]
   (assoc-in db [:customizer :saved-data] data)))

(r/reg-event-db
 :customizer.storage/look-loaded!
 (fn [db [_ look-data]]
   (assoc-in db [:customizer :saved-look] look-data)))

(r/reg-event-db
 :customizer.storage/form-loaded!
 (fn [db [_ form-data]]
   (assoc-in db [:customizer :saved-form] form-data)))

(r/reg-event-db
 :customizer.storage/cart-loaded!
 (fn [db [_ cart-data]]
   (assoc-in db [:customizer :saved-cart] cart-data)))

;; -----------------------------------------------------------------------------
;; ---- Utility Functions ----

(defn has-stored-data?
  "Check if there's any stored customizer data"
  []
  (some #(get-data %) (vals STORAGE_KEYS)))

(defn get-storage-size
  "Get the total size of stored customizer data in bytes"
  []
  (try
    (reduce (fn [total key]
              (let [data (.getItem js/localStorage key)]
                (+ total (if data (.-length data) 0))))
            0
            (vals STORAGE_KEYS))
    (catch :default e
      (println "Error calculating storage size:" e)
      0)))

(defn cleanup-old-data!
  "Remove old data if storage is getting full (optional)"
  []
  (let [max-size (* 5 1024 1024)  ; 5MB limit
        current-size (get-storage-size)]
    (when (> current-size max-size)
      (println "Storage getting full, cleaning up old data...")
      (clear-all!))))

;; -----------------------------------------------------------------------------
;; ---- Auto-save Functions ----

(defn auto-save-customizer!
  "Auto-save current customizer state"
  [db]
  (let [customizer-data (get-in db [:customizer])
        selected-look (get-in db [:customizer :selected-look])
        cart-content (get-in db [:cart :content])]
    (save-customizer-data! customizer-data)
    (when selected-look
      (save-selected-look! selected-look))
    (when cart-content
      (save-cart-content! cart-content))))

(defn restore-customizer!
  "Restore customizer state from localStorage"
  []
  (r/dispatch [:customizer.storage/load-data!])
  (r/dispatch [:customizer.storage/load-look!])
  (r/dispatch [:customizer.storage/load-cart!]))

;; -----------------------------------------------------------------------------
;; ---- Export/Import Functions ----

(defn export-customizer-data
  "Export all customizer data as JSON string"
  []
  (try
    (let [all-data (reduce (fn [acc [key-name key]]
                            (assoc acc key-name (get-data key)))
                          {}
                          STORAGE_KEYS)]
      (js/JSON.stringify (clj->js all-data)))
    (catch :default e
      (println "Error exporting data:" e)
      nil)))

(defn import-customizer-data
  "Import customizer data from JSON string"
  [json-string]
  (try
    (let [data (js->clj (js/JSON.parse json-string) :keywordize-keys true)]
      (doseq [[key-name key] STORAGE_KEYS]
        (when-let [value (get data key-name)]
          (put-data! key value)))
      true)
    (catch :default e
      (println "Error importing data:" e)
      false)))