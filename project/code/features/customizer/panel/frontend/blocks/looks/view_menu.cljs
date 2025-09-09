(ns features.customizer.panel.frontend.blocks.looks.view-menu
  (:require
    [reagent.core  :refer [atom]]
    [re-frame.core       :as r]
    ["react"             :as react]
    ["keen-slider/react" :as keen]
   
    [features.customizer.panel.frontend.blocks.looks.utils :as utils]))

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn adaptive-height [slider]
  ;; Set the slider container height based on the active item height in the container.
  (letfn [(f []
            (let [active-idx    (-> slider .-track .-details .-rel)
                  slide-height  (.-offsetHeight (nth (.-slides slider) active-idx))]
              (set! (-> slider .-container .-style .-height)
                    (str slide-height "px"))))]

    (.on slider "created"      f)
    (.on slider "slideChanged" f)))

(defn y-handle [oidx idx length]
  (when-not (nil? oidx)
    (cond
      (and (= idx length) (= (dec oidx) -1)) :b
      (and (= idx 0) (= (inc oidx) (inc length))) :b
      (or (= oidx (dec idx)) (= oidx (inc idx))) :b
      (= oidx idx) :a
      :else false)))

(defn get-selected-look-index [collection selected-look]
  "Get the index of the selected look in the collection"
  (when selected-look
    (first (keep-indexed #(when (= (:id %2) (:id selected-look)) %1) collection))))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Configs ----

(defn look_SELECTOR_CONFIG [collection move?]
  #js{:loop           true
      :mode           "free-snap"
      ;; :animationEnded (fn [props]) ;(nth data (get-active-index! props))
                        ;; (when move? 
                        ;;   (r/dispatch [:looks/select-by-index! (-> props .-track .-details .-rel)]))) 

      ;; :breakpoints    #js{
      ;;                     "(min-width: 750px)" #js{:slides #js{:perView 5 "origin"  "center" :spacing 10}}
      ;;                     "(min-width: 800px)"  #js{:slides #js{:perView 7 "origin"  "center" :spacing 5}}
      ;;                     "(min-width: 1000px)" #js{:slides #js{:perView 9 "origin"  "center" :spacing 10}}
      ;;                     "(min-width: 1200px)" #js{:slides #js{:perView 11 "origin"  "center" :spacing 10}}
      ;;                     "(min-width: 1400px)" #js{:slides #js{:perView 13 "origin"  "center" :spacing 10}}}
      :slides         #js{"perView" "auto"
                          "origin"  (if (< (count collection) 3) "auto" "center")
                          "spacing" 15}})

;; ---- Configs ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Hooks ----

(defn- inspect-move! [^js slider-props selected-look looks-data move?]
  (react/useEffect
    (fn []
      (when move?
        (utils/move-to! (.-current slider-props)
                        (get-selected-look-index looks-data selected-look))
        (r/dispatch [:db/assoc-in [:move] nil]))
      (fn []))
    #js[move?]))

(defn refresh-slider! [slider-props collection]
  ;; Refresh the slider when the collection changes.
  (react/useEffect
    (fn []
      (.update (.-current slider-props))
      (fn []))
    #js[collection]))

(defn- did-mount! [slider-props collection]
  (react/useEffect
     (fn []
       (r/dispatch [:looks/did-mount! slider-props collection])
       (fn []))
     #js[]))

;; ---- Hooks ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Components ----

(defn look-card [pinned-looks selected-id item-index {:keys [id thumbnail] :as item-props}]
  (let [pinned?   (contains? pinned-looks id)
        selected? (= id selected-id)]
        
    [:div {:class "dock-card-container"
           :data-selected   selected?
           :on-click        #(try (.focus (.-firstChild (.-target %))) (catch js/Error e nil))}  ;; Focus the button to ensure the keyboard controls work.
      [:button {:class           "dock-card"
                :data-selected   selected?
                :style           {:y (when pinned? -8)}
                :on-click        #(r/dispatch [:looks.card/click! (assoc item-props :index item-index)])
                :on-double-click #(r/dispatch [:looks/pin! (assoc item-props :index item-index)])}
        
        [:img {:src     thumbnail
               :loading "lazy"
               :class   "__img"}]]
        
      [:div {:class "dock-dot" :style {:opacity (if pinned? 1 0)}}]]))

(defn- selected-look-name [selected-look]
  (let [{:keys [id name]} selected-look
        pinned-looks @(r/subscribe [:db/get-in [:pinned/looks]])
        is-pinned? (some #(= (:id %) id) pinned-looks)]
    [:div {:id "customizer--look-name" 
           :style {:display "flex" 
                   :align-items "center" 
                   :justify-content "center"
                   :gap "8px"}}
      [:span (str (or name "looks"))]
      (when id
        [:button {:class "star-button"
                  :style {:background "transparent"
                          :border "none"
                          :cursor "pointer"
                          :font-size "18px"
                          :color (if is-pinned? "#FFD700" "#ccc")
                          :padding "0"
                          :display "flex"
                          :align-items "center"}
                  :on-click #(r/dispatch [:looks/pin! selected-look])}
         [:i {:class (if is-pinned? "fa-solid fa-star" "fa-regular fa-star")}]])]))

(def timeout (atom nil))

(defn next-timout [slider]
    (js/clearTimeout  timeout)
    (reset! timeout (js/setTimeout (fn []
                                    (.next slider)
                                    (next-timout slider))
                                   11000)))

(defn- slider [collection]
  (let [move? @(r/subscribe [:db/get-in [:move]])
        [slider-ref slider-props] (keen/useKeenSlider (look_SELECTOR_CONFIG collection move?)
                                                      #js[utils/wheel-controls
                                                          utils/keyboard-controls])

         pinned-look   (set (map :id @(r/subscribe [:db/get-in [:pinned/looks]])))
         selected-look @(r/subscribe [:db/get-in [:customizer :selected-look]])]
    (refresh-slider! slider-props collection)
    (did-mount! slider-props collection)
    (inspect-move! slider-props selected-look collection move?)

    [:<>
      [selected-look-name selected-look]
      [:div {:class "keen-slider"
             :ref   slider-ref}
        (map-indexed (fn [item-index item-props]
                       [:div {:key   (str "customizer-look-selector-" (:id item-props))
                              :class "keen-slider__slide"}
                         [look-card pinned-look (:id selected-look) item-index item-props]])
                     collection)]]))

(defn- looks-loading-spinner []
  [:div {:style {:display "flex"
                 :justify-content "center"
                 :align-items "center"
                 :padding "20px"
                 :gap "10px"}}
    [:div {:style {:width "20px"
                   :height "20px"
                   :border "2px solid rgba(255,255,255,0.3)"
                   :border-top "2px solid #fff"
                   :border-radius "50%"
                   :animation "spin 1s linear infinite"}}]
    [:span {:style {:color "#fff"
                    :font-size "14px"}}
     "Loading looks..."]])

(defn- look-selector []
  (let [looks @(r/subscribe [:db/get-in [:customizer/looks :items] false])
        loading? (or (nil? looks) (false? looks))]
    [:div {:id    "look-selector"
           :class "dock--outer"}
      [:div {:class "dock--inner"
             :style {:display         "flex"
                     :justify-content "center"}}
        [:> react/Suspense {:fallback [looks-loading-spinner]}
                            ;; :key (count looks)}
          (when (coll? looks) [slider looks])]]]))
          

;; ---- Components ----
;; -----------------------------------------------------------------------------

(defn view []
  [look-selector])