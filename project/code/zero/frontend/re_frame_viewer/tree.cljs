(ns zero.frontend.re-frame-viewer.tree
  (:require
   ["react" :as react]
   [zero.frontend.re-frame :as r]))

;; LocalStorage helpers
(defn save-to-local-storage [key value]
  (try
    (.setItem js/localStorage key (pr-str value))
    (catch js/Error e
      (js/console.warn "Failed to save to localStorage:" e))))

(defn load-from-local-storage [key default-value]
  (try
    (let [stored (.getItem js/localStorage key)]
      (if (nil? stored)
        default-value
        (cljs.reader/read-string stored)))
    (catch js/Error e
      (js/console.warn "Failed to load from localStorage:" e)
      default-value)))

;; Path parsing helper
(defn parse-path-input [input]
  (if (empty? input)
    []
    (->> (clojure.string/split input #"\s+")
         (map clojure.string/trim)
         (filter #(not (empty? %)))
         (map #(if (clojure.string/starts-with? % ":")
                 (keyword (subs % 1))
                 %))
         vec)))

;; Tree expansion state management
(r/reg-sub
  :tree-viewer/expanded-paths
  (fn [db _]
    (get-in db [:dev :tree-viewer :expanded] #{})))

(r/reg-sub
  :tree-viewer/path-input
  (fn [db _]
    (get-in db [:dev :tree-viewer :path-input] 
            (load-from-local-storage "tree-viewer-path-input" ""))))

(r/reg-sub
  :tree-viewer/current-path
  (fn [db _]
    (get-in db [:dev :tree-viewer :current-path] 
            (load-from-local-storage "tree-viewer-current-path" []))))

(r/reg-event-db
  :tree-viewer/toggle-path
  (fn [db [_ path]]
    (let [expanded (get-in db [:dev :tree-viewer :expanded] #{})]
      (assoc-in db [:dev :tree-viewer :expanded]
                (if (contains? expanded path)
                  (disj expanded path)
                  (conj expanded path))))))

(r/reg-event-db
  :tree-viewer/set-path-input
  (fn [db [_ input]]
    (let [parsed-path (parse-path-input input)]
      (save-to-local-storage "tree-viewer-path-input" input)
      (save-to-local-storage "tree-viewer-current-path" parsed-path)
      (-> db
          (assoc-in [:dev :tree-viewer :path-input] input)
          (assoc-in [:dev :tree-viewer :current-path] parsed-path)))))

;; Styling for tree viewer
(defn path-input-style []
  {:width "100%"
   :padding "8px 12px"
   :margin-bottom "12px"
   :background "#222"
   :border "1px solid #DAA520"
   :border-radius "0"
   :font-size "12px"
   :font-family "'Courier New', monospace"
   :color "#DAA520"
   :outline "none"})

(defn tree-container-style []
  {:flex "1"
   :overflow "auto"
   :border "2px solid #DAA520"
   :border-radius "0"
   :background "#222"
   :padding "10px"})

(defn tree-container-indent [level]
  {:margin-left (str (* level 15) "px")
   :margin-bottom "4px"})

(defn tree-key-button-style [expandable? expanded?]
  {:display "block"
   :width "100%"
   :text-align "left"
   :padding "8px 12px"
   :margin "2px 0"
   :font-family "'Courier New', Monaco, monospace"
   :font-size "13px"
   :font-weight "bold"
   :background (cond
                 (and expandable? expanded?) "#DAA520"
                 expandable? "rgba(218, 165, 32, 0.2)"
                 :else "rgba(135, 206, 235, 0.1)")
   :color (cond
            (and expandable? expanded?) "#333"
            expandable? "#DAA520"
            :else "#87CEEB")
   :border (if expandable?
             "1px solid #DAA520"
             "1px solid #87CEEB")
   :border-radius "0"
   :cursor "pointer"
   :transition "all 0.2s"})

(defn tree-value-container-style []
  {:margin-top "8px"
   :padding-left "20px"
   :border-left "2px solid rgba(218, 165, 32, 0.3)"})

;; Helper functions
(defn expandable? [value]
  (or (map? value) (vector? value) (list? value) (set? value)))

(defn get-value-preview [value]
  (cond
    (map? value) (str "{" (count value) " items}")
    (vector? value) (str "[" (count value) " items]")
    (list? value) (str "(" (count value) " items)")
    (set? value) (str "#{" (count value) " items}")
    (string? value) (str "\"" value "\"")
    (keyword? value) (str value)
    (nil? value) "nil"
    :else (str value)))

;; Forward declaration
(declare tree-node)


;; Tree node components
(defn tree-key-button [key value level path expanded? expandable?]
  (let [button-text (if expandable?
                     (str key " " (get-value-preview value))
                     (str key " " (get-value-preview value)))]
    [:div {:style (tree-container-indent level)}
     [:button {:style (tree-key-button-style expandable? expanded?)
               :on-click (if expandable?
                          #(r/dispatch [:tree-viewer/toggle-path path])
                          identity)
               :on-double-click #(let [path-string (clojure.string/join " " (map str path))]
                                  (r/dispatch [:tree-viewer/set-path-input path-string]))}
      button-text]]))

(defn tree-children [value level path]
  (when (expandable? value)
    [:div {:style (tree-value-container-style)}
     (doall
      (map-indexed
       (fn [idx [child-key child-value]]
         (let [child-path (conj path child-key)]
           ^{:key (str path "-" idx)}
           [tree-node child-key child-value (inc level) child-path]))
       (if (map? value)
         (sort-by first (seq value))
         (map-indexed vector value))))]))

(defn tree-node [key value level path]
  (let [expanded-paths @(r/subscribe [:tree-viewer/expanded-paths])
        expanded? (contains? expanded-paths path)
        expandable? (expandable? value)]
    
    [:div
     [tree-key-button key value level path expanded? expandable?]
     (when (and expandable? expanded?)
       [tree-children value level path])]))


;; Root tree viewer component
(defn tree-viewer [filtered-data current-path]
  [:div {:style (tree-container-style)}
   (cond
     (nil? filtered-data)
     [:div {:style {:color "#ff6b6b" :padding "20px" :text-align "center"}}
      "Path not found"]
     
     (map? filtered-data)
     (doall
      (map-indexed
       (fn [idx [key value]]
         ^{:key idx}
         [tree-node (str key) value 0 [key]])
       (sort-by first (seq filtered-data))))
     
     :else
     [:div {:style {:color "#DAA520" :padding "20px" :font-family "'Courier New', monospace"}}
      (get-value-preview filtered-data)])])