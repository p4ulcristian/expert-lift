(ns zero.frontend.re-frame-viewer.view
  (:require
   ["react" :as react]
   [zero.frontend.re-frame :as r]
   [zero.frontend.re-frame-viewer.raw :as raw]
   [zero.frontend.re-frame-viewer.tree :as tree]))

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

;; State subscriptions
(r/reg-sub
  :re-frame-viewer/app-db
  (fn [db _]
    db))

(r/reg-sub
  :re-frame-viewer/visible?
  (fn [db _]
    (get-in db [:dev :re-frame-viewer :visible?] 
            (load-from-local-storage "re-frame-viewer-visible" false))))

(r/reg-sub
  :re-frame-viewer/view-mode
  (fn [db _]
    (get-in db [:dev :re-frame-viewer :view-mode] 
            (load-from-local-storage "re-frame-viewer-mode" :tree))))

(r/reg-sub
  :re-frame-viewer/hidden?
  (fn [db _]
    (get-in db [:dev :re-frame-viewer :hidden?] 
            (load-from-local-storage "re-frame-viewer-hidden" false))))

;; Events
(r/reg-event-db
  :re-frame-viewer/toggle
  (fn [db _]
    (let [new-visible? (not (get-in db [:dev :re-frame-viewer :visible?] false))]
      (save-to-local-storage "re-frame-viewer-visible" new-visible?)
      (-> db
          (assoc-in [:dev :re-frame-viewer :visible?] new-visible?)
          (assoc-in [:dev :re-frame-viewer :hidden?] false)))))

(r/reg-event-db
  :re-frame-viewer/hide
  (fn [db _]
    (let [new-hidden? (not (get-in db [:dev :re-frame-viewer :hidden?] false))]
      (save-to-local-storage "re-frame-viewer-hidden" new-hidden?)
      (assoc-in db [:dev :re-frame-viewer :hidden?] new-hidden?))))

(r/reg-event-db
  :re-frame-viewer/set-view-mode
  (fn [db [_ mode]]
    (save-to-local-storage "re-frame-viewer-mode" mode)
    (assoc-in db [:dev :re-frame-viewer :view-mode] mode)))

;; Initialize state from localStorage
(r/reg-event-db
  :re-frame-viewer/init-from-storage
  (fn [db _]
    (let [visible? (load-from-local-storage "re-frame-viewer-visible" false)
          view-mode (load-from-local-storage "re-frame-viewer-mode" :tree)
          hidden? (load-from-local-storage "re-frame-viewer-hidden" false)
          path-input (load-from-local-storage "tree-viewer-path-input" "")
          current-path (load-from-local-storage "tree-viewer-current-path" [])]
      (-> db
          (assoc-in [:dev :re-frame-viewer :visible?] visible?)
          (assoc-in [:dev :re-frame-viewer :view-mode] view-mode)
          (assoc-in [:dev :re-frame-viewer :hidden?] hidden?)
          (assoc-in [:dev :tree-viewer :path-input] path-input)
          (assoc-in [:dev :tree-viewer :current-path] current-path)))))

;; Styling helpers
(defn main-container-style [hidden?]
  {:position "fixed"
   :top "20px"
   :left "20px"
   :right "20px"
   :bottom "20px"
   :z-index 9999
   :display "flex"
   :flex-direction "column"
   :padding "24px"
   :background "linear-gradient(135deg, rgba(51, 51, 51, 0.95) 0%, rgba(51, 51, 51, 0.95) 70%, rgba(218, 165, 32, 0.95) 100%)"
   :border-radius "12px"
   :box-shadow "0 4px 12px rgba(0, 0, 0, 0.3)"
   :overflow "hidden"
   :opacity (if hidden? 0.15 1)
   :pointer-events (if hidden? "none" "auto")})

(defn header-container-style []
  {:display "grid"
   :grid-template-columns "auto 1fr"
   :gap "20px"
   :align-items "center"
   :margin-bottom "20px"})

(defn breadcrumb-container-style []
  {:margin-bottom "20px"
   :border-bottom "2px solid #DAA520"
   :padding-bottom "16px"})

(defn ascii-text-style []
  {:font-family "'Courier New', monospace"
   :font-size "14px"
   :line-height "1.2"
   :color "#DAA520"
   :font-weight "bold"
   :white-space "pre"})

(defn button-style [active?]
  {:padding "6px 12px"
   :margin "0 4px"
   :font-size "12px"
   :font-family "'Courier New', monospace"
   :background (if active? "#DAA520" "transparent")
   :color (if active? "#333" "#DAA520")
   :border "1px solid #DAA520"
   :border-radius "0"
   :cursor "pointer"
   :text-transform "uppercase"
   :font-weight "bold"
   :transition "all 0.2s"})

;; ASCII ZERO art
(defn ascii-zero []
  "███████╗███████╗██████╗  ██████╗ \n╚══███╔╝██╔════╝██╔══██╗██╔═══██╗\n  ███╔╝ █████╗  ██████╔╝██║   ██║\n ███╔╝  ██╔══╝  ██╔══██╗██║   ██║\n███████╗███████╗██║  ██║╚██████╔╝\n╚══════╝╚══════╝╚═╝  ╚═╝ ╚═════╝")

;; Header components
(defn ascii-logo []
  [:div {:style (ascii-text-style)}
   (ascii-zero)])

(defn mode-buttons []
  (let [current-mode @(r/subscribe [:re-frame-viewer/view-mode])
        hidden? @(r/subscribe [:re-frame-viewer/hidden?])]
    [:div {:style {:display "grid"
                   :grid-template-columns "1fr 1fr auto"
                   :gap "4px"}}
     [:button {:on-click #(r/dispatch [:re-frame-viewer/set-view-mode :tree])
               :style (button-style (= current-mode :tree))}
      "TREE"]
     [:button {:on-click #(r/dispatch [:re-frame-viewer/set-view-mode :raw])
               :style (button-style (= current-mode :raw))}
      "RAW"]
     [:button {:on-click #(r/dispatch [:re-frame-viewer/hide])
               :style (merge (button-style false)
                             (when hidden? {:pointer-events "auto"}))}
      "HIDE"]]))

(defn breadcrumb-style []
  {:font-family "'Courier New', monospace"
   :font-size "12px"
   :color "#DAA520"
   :margin-top "10px"
   :margin-bottom "5px"
   :padding "8px 12px"
   :background "rgba(218, 165, 32, 0.1)"
   :border "1px solid rgba(218, 165, 32, 0.3)"
   :border-radius "0"})

(defn breadcrumb-part-style [clickable?]
  {:color (if clickable? "#DAA520" "#999")
   :cursor (if clickable? "pointer" "default")
   :text-decoration (if clickable? "underline" "none")
   :margin "0 2px"})

(defn breadcrumb []
  (let [current-path @(r/subscribe [:tree-viewer/current-path])]
    (when (seq current-path)
      [:div {:style (breadcrumb-style)}
       "Path: "
       (doall
         (map-indexed
           (fn [idx path-part]
             (let [partial-path (take (inc idx) current-path)
                   path-string (clojure.string/join " " (map str partial-path))
                   clickable? (< idx (dec (count current-path)))]
               ^{:key idx}
               [:span
                [:span {:style (breadcrumb-part-style clickable?)
                        :on-click (when clickable?
                                   #(r/dispatch [:tree-viewer/set-path-input path-string]))}
                 (str path-part)]
                (when (< idx (dec (count current-path)))
                  [:span {:style {:color "#666" :margin "0 4px"}} "→"])]))
           current-path))])))

(defn path-input []
  (let [path-input @(r/subscribe [:tree-viewer/path-input])]
    [:input {:type "text"
             :placeholder "Enter path (e.g. :router :inner-data)"
             :value path-input
             :on-change #(r/dispatch [:tree-viewer/set-path-input (-> % .-target .-value)])
             :style {:width "100%"
                     :padding "8px 12px"
                     :margin-top "5px"
                     :background "#222"
                     :border "1px solid #DAA520"
                     :border-radius "0"
                     :font-size "12px"
                     :font-family "'Courier New', monospace"
                     :color "#DAA520"
                     :outline "none"}}]))

(defn header-section []
  [:div
   [:div {:style (header-container-style)}
    [ascii-logo]
    [:div
     [mode-buttons]
     [path-input]]]
   [:div {:style (breadcrumb-container-style)}
    [breadcrumb]]])

;; Content container style
(defn content-container-style []
  {:flex "1"
   :overflow "auto"
   :margin-top "8px"})

;; Get data at current path helper
(defn get-data-at-path [data path]
  (if (empty? path)
    data
    (get-in data path)))

;; Content display based on mode
(defn content-display []
  (let [view-mode @(r/subscribe [:re-frame-viewer/view-mode])
        app-db @(r/subscribe [:re-frame-viewer/app-db])
        current-path @(r/subscribe [:tree-viewer/current-path])
        filtered-data (get-data-at-path app-db current-path)]
    [:div {:style (content-container-style)}
     (case view-mode
       :raw [raw/raw-viewer filtered-data]
       :tree [tree/tree-viewer filtered-data current-path]
       [tree/tree-viewer filtered-data current-path])])) ; default to tree

;; Main viewer component
(defn re-frame-viewer []
  (let [visible? @(r/subscribe [:re-frame-viewer/visible?])
        hidden? @(r/subscribe [:re-frame-viewer/hidden?])]
    (when visible?
      [:div {:style (main-container-style hidden?)}
       [header-section]
       [content-display]])))

;; Keyboard listener setup
(defn use-keyboard-listener []
  (react/useEffect
   (fn []
     (let [handle-keydown 
           (fn [event]
             (when (and (or (.-ctrlKey event) (.-metaKey event))
                       (= (.-key event) "m"))
               (.preventDefault event)
               (r/dispatch [:re-frame-viewer/toggle])))]
       
       (.addEventListener js/document "keydown" handle-keydown)
       
       ;; Cleanup
       (fn []
         (.removeEventListener js/document "keydown" handle-keydown))))
   #js []))

;; Keyboard listener component (separate from modal)
(defn keyboard-listener []
  (use-keyboard-listener)
  nil) ; Return nothing, just handles the keyboard event

;; Initialize function (dev only)
(defn init!
  "Initialize the re-frame viewer for development"
  []
  (when ^boolean goog.DEBUG
    ;; Restore state from localStorage
    (r/dispatch [:re-frame-viewer/init-from-storage])
    nil))