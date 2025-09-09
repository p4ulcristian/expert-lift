(ns features.flex.service-areas.frontend.blocks.map
  (:require
    ["react" :as react]
    ["@turf/turf" :as turf]
    [reagent.core :as reagent]
    [re-frame.core :as r]
    [ui.button :as button]))

(def MAX_REACH_DISTANCE 50)
(def MAP-STYLE "https://api.maptiler.com/maps/bf556e92-46e4-4422-8267-dfc9d658b63f/style.json?key=1WKoyb3HRdfzRHkA6ZAG")

(defn format-currency [n]
  (.format (js/Intl.NumberFormat. "en-US") n))

(defn coords->point [^js coords]
  (turf/point #js[(.-lng coords) (.-lat coords)]))

(defn in-reach? [^js from ^js to]
  (> MAX_REACH_DISTANCE
     (turf/distance
      (coords->point from)
      (coords->point to)
      #js{"units" "kilometers"})))

(defn init-map [^js MAP]
  ^js (new MAP.Map
           #js{"container" "map-container"
               "style"     MAP-STYLE
               "center"    #js[-101.002121, 38.998909]
               "zoom"      3
               "geolocateControl"  false
               "navigationControl" false
               "attributionControl" false}))

(defn init-reach-area [^js ws-coords]
  (turf/circle (coords->point ws-coords)
               MAX_REACH_DISTANCE #js{"units" "kilometers"}))

(defn get-active-package-info [total-population]
  (cond
    (< 0 total-population 110000) 
    {:tier 1 :range "Up to 110k population" :desc "Perfect for small neighborhoods"}
    
    (< 110000 total-population 180000)
    {:tier 2 :range "110k - 180k population" :desc "Ideal for medium-sized areas"}
    
    (< 180000 total-population 280000)
    {:tier 3 :range "180k - 280k population" :desc "Great for large communities"}
    
    (< 280000 total-population 500000)
    {:tier 4 :range "280k - 500k population" :desc "Premium for metropolitan areas"}
    
    :else nil))

(defn get-button-colors [base-color enabled?]
  (let [colors {"#6b7280" {:r 107 :g 114 :b 128}
                "#3b82f6" {:r 59  :g 130 :b 246}
                "#8b5cf6" {:r 139 :g 92  :b 246}
                "#f59e0b" {:r 245 :g 158 :b 11}}
        rgb (get colors base-color)]
    (if enabled?
      {:background base-color
       :border (str "2px solid " base-color)
       :color "white"}
      {:background (str "rgba(" (:r rgb) "," (:g rgb) "," (:b rgb) ", 0.1)")
       :border (str "1px solid rgba(" (:r rgb) "," (:g rgb) "," (:b rgb) ", 0.3)")
       :color (str "rgba(" (:r rgb) "," (:g rgb) "," (:b rgb) ", 0.7)")})))

(defn package-tier-button [tier-num base-color enabled?]
  (let [colors (get-button-colors base-color enabled?)]
    [button/view {:style (merge colors
                               {:padding "8px 16px"
                                :cursor (if enabled? "pointer" "not-allowed")
                                :font-weight (if enabled? "600" "400")
                                :transition "all 0.2s ease"})}
     (str "Package " tier-num)]))

(defn package-tiers [total-population]
  (let [active-package (get-active-package-info total-population)
        tier1-enabled? (< 0 total-population 110000)
        tier2-enabled? (< 110000 total-population 180000)
        tier3-enabled? (< 180000 total-population 280000)
        tier4-enabled? (< 280000 total-population 500000)]
    [:div {:style {:margin-top "20px"}}
     [:div {:style {:display "flex" 
                    :gap "10px" 
                    :justify-content "center"
                    :align-items "center"}}
      [package-tier-button 1 "#6b7280" tier1-enabled?]
      [package-tier-button 2 "#3b82f6" tier2-enabled?]
      [package-tier-button 3 "#8b5cf6" tier3-enabled?]
      [package-tier-button 4 "#f59e0b" tier4-enabled?]]
     
     [:div {:style {:margin-top "12px"
                    :text-align "center"
                    :padding "12px 20px"
                    :background (if active-package 
                                  "rgba(59, 130, 246, 0.05)"
                                  "transparent")
                    :border (if active-package 
                              "1px solid rgba(59, 130, 246, 0.1)"
                              "1px solid transparent")
                    :border-radius "8px"
                    :min-height "60px"
                    :display "flex"
                    :flex-direction "column"
                    :justify-content "center"
                    :transition "all 0.2s ease"}}
      (if active-package
        [:<>
         [:div {:style {:font-size "0.9rem"
                        :color "#1f2937"
                        :font-weight "500"
                        :margin-bottom "4px"}}
          (str "Package " (:tier active-package) ": " (:range active-package))]
         [:div {:style {:font-size "0.85rem"
                        :color "#6b7280"}}
          (:desc active-package)]]
        [:div {:style {:color "#9ca3af"
                       :font-size "0.85rem"}}
         "Select more areas to unlock package tiers"])]]))

(defn service-area-table-row [[zip [population _]]]
  [:tr {:key zip :style {:border-bottom "1px solid rgba(0, 0, 0, 0.03)"}}
   [:td {:style {:padding "5px 8px"
                 :font-size "0.7rem"
                 :color "rgba(0, 0, 0, 0.7)"}} zip]
   [:td {:style {:padding "5px 8px"
                 :text-align "right"
                 :font-size "0.7rem"
                 :color "rgba(0, 0, 0, 0.7)"
                 :font-weight "500"}} (format-currency population)]])

(defn service-area-panel []
  (let [service-areas @(r/subscribe [:db/get-in [:setup :service-areas]])
        total-population (reduce (fn [acc [_ [pop _]]] (+ acc pop)) 0 service-areas)
        [expanded? set-expanded] (react/useState false)]
    [:div {:style {:display "flex"
                   :gap "6px"
                   :flex-direction "column"
                   :position "absolute"
                   :top "20px"
                   :right "20px"
                   :width "180px"}}
     [:div {:style {:background "rgba(255, 255, 255, 0.08)"
                    :backdrop-filter "blur(12px)"
                    :border "1px solid rgba(255, 255, 255, 0.18)"
                    :border-radius "8px"
                    :box-shadow "0 4px 30px rgba(0, 0, 0, 0.1)"
                    :overflow "hidden"
                    :transition "all 0.2s ease"}}
      
      [:button {:style {:all "unset"
                        :padding "12px 16px"
                        :font-weight "500"
                        :font-size "0.7rem"
                        :text-align "center"
                        :color "rgba(0, 0, 0, 0.7)"
                        :letter-spacing "0.5px"
                        :cursor "pointer"
                        :display "flex"
                        :justify-content "space-between"
                        :align-items "center"
                        :border-bottom (when expanded? "1px solid rgba(0, 0, 0, 0.08)")
                        :background "transparent"
                        :transition "background 0.2s ease"}
                :on-mouse-enter #(-> % .-target .-style .-setProperty "background" "rgba(0, 0, 0, 0.02)")
                :on-mouse-leave #(-> % .-target .-style .-setProperty "background" "transparent")
                :on-click #(set-expanded (not expanded?))}
       [:div {:style {:display "flex" :flex-direction "column" :align-items "flex-start"}}
        [:span {:style {:font-size "0.65rem" :opacity "0.7"}} "SERVICE AREAS"]
        [:span {:style {:font-size "0.8rem" :font-weight "600" :margin-top "2px"}} 
         (str (count service-areas) " areas • " (format-currency total-population))]]
       [:i {:class (str "fa-solid " (if expanded? "fa-chevron-up" "fa-chevron-down"))
            :style {:font-size "10px" :opacity "0.6"}}]]
      
      (when expanded?
        [:div {:style {:max-height "350px" :overflow-y "auto"}}
         [:table {:style {:width "100%" :border-collapse "collapse"}}
          [:thead
           [:tr {:style {:border-bottom "1px solid rgba(0, 0, 0, 0.06)"}}
            [:th {:style {:padding "6px 8px"
                          :text-align "left"
                          :font-weight "500"
                          :font-size "0.65rem"
                          :color "rgba(0, 0, 0, 0.5)"
                          :text-transform "uppercase"
                          :letter-spacing "0.3px"}} "Zip"]
            [:th {:style {:padding "6px 8px"
                          :text-align "right"
                          :font-weight "500"
                          :font-size "0.65rem"
                          :color "rgba(0, 0, 0, 0.5)"
                          :text-transform "uppercase"
                          :letter-spacing "0.3px"}} "Pop"]]]
          [:tbody
           (map service-area-table-row service-areas)]]])]]))