
(ns features.customizer.blocks.local-storage
  (:require
    ["react" :as react]
    [re-frame.core :as r]))
;; -----------------------------------------------------------------------------
;; ---- Core ----

(defn set-item [key value]
  (try
    (.setItem js/localStorage key (js/JSON.stringify (clj->js value)))
    (catch :default e
      (println "Error setting item in localStorage:" e))))

(defn get-item [key]
  (try
    (-> (.getItem js/localStorage key)
        js/JSON.parse
        (js->clj :keywordize-keys true))
    (catch :default e
      (println "Error getting item from localStorage:" e))))

(defn remove-item [key]
  (try
   (.removeItem js/localStorage key)
   (catch :default e
     (println "Error removing item from localStorage:" e))))

(defn clear-namespace [namespace]
  (try
    (.clearNamespace js/localStorage namespace)
    (catch :default e
      (println "Error clearing namespace from localStorage:" e))))

(defn clear-all []
  (try
    (.clear js/localStorage)
    (catch :default e
      (println "Error clearing all from localStorage:" e))))


(defn load-once [path key]
  (react/useEffect
    (fn []
      (when (nil? @(r/subscribe [:db/get-in path]))
        (r/dispatch [:local-storage/load-item path key]))
      (fn []))
    #js[]))

;; ---- Core ----
;; -----------------------------------------------------------------------------

(r/reg-fx
  :local-storage/set-item
  (fn [[key value]]
    (set-item key value)))

(r/reg-fx
  :local-storage/remove-item
  (fn [[key]]
    (remove-item key)))

(r/reg-fx
  :local-storage/clear-namespace
  (fn [[namespace]]
    (clear-namespace namespace)))

(r/reg-fx
  :local-storage/clear-all
  (fn [_]
    (clear-all)))

(r/reg-event-fx
  :local-storage/set-item!
  (fn [db [_ key value]]
    {:local-storage/set-item [key value]}))

(r/reg-event-db
  :local-storage/load-item
  (fn [db [_ path key]]
    (try
      (if-let [item (get-item key)]
        (assoc-in db path item)
        db)
      (catch :default e
        (println "Error loading item from localStorage:" e)
        db))))
   

(r/reg-event-fx
  :local-storage/remove-item
  (fn [db [_ key]]
    {:local-storage/remove-item [key]}))

(r/reg-event-fx
  :local-storage/clear-namespace
  (fn [db [_ namespace]]
    {:local-storage/clear-namespace [namespace]}))

(r/reg-event-fx
  :local-storage/clear-all
  (fn [db [_]]
    {:local-storage/clear-all []}))
