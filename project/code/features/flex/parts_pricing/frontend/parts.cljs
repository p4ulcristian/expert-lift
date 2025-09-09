(ns features.flex.parts-pricing.frontend.parts
  (:require
   ["react" :as react]
   [features.flex.parts-pricing.frontend.request :as parts-request]
   [clojure.string :as str]
   [features.flex.parts-pricing.frontend.pricing :as pricing]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [ui.card :as card]
   [ui.accordion :as accordion]
   [zero.frontend.react :as zero-react]))

(def hierarchical-data (r/atom []))

(defn breadcrumb-item [text is-last?]
  [:span {:style {:display "inline-flex"
                  :align-items "center"
                  :color (if is-last? "var(--text-clr)" "var(--muted-clr)")
                  :font-size "0.85em"
                  :font-weight (if is-last? "500" "400")}}
   text
   (when-not is-last?
     [:span {:style {:margin "0 0.5rem"
                     :color "var(--muted-clr)"
                     :font-size "0.8em"}}
      "→"])])

(defn badge [type count]
  (let [badge-styles (case type
                      :category {:background "linear-gradient(135deg, #374151 0%, #1f2937 100%)"
                                :color "#ffffff"
                                :icon "🏷️"}
                      :package {:background "linear-gradient(135deg, #6b7280 0%, #4b5563 100%)"
                               :color "#ffffff" 
                               :icon "📦"})]
    [:span {:style {:display "inline-flex"
                    :align-items "center"
                    :gap "0.25rem"
                    :padding "0.25rem 0.6rem"
                    :border-radius "12px"
                    :background (:background badge-styles)
                    :color (:color badge-styles)
                    :font-size "0.7rem"
                    :font-weight "600"
                    :text-shadow "0 1px 2px rgba(0,0,0,0.1)"
                    :box-shadow "0 2px 4px rgba(0,0,0,0.1)"
                    :margin-left "0.5rem"}}
     [:span {:style {:font-size "0.8rem"}} (:icon badge-styles)]
     [:span count]]))

(defn part-item [part]
  (let [workspace-id @(rf/subscribe [:db/get-in [:router :path-params :workspace-id]])
        initial-active (get-in part [:pricing :active] false)
        [is-active set-active] (react/useState initial-active)
        auto-save-fn (react/useRef nil)
        
        handle-active-change (fn [new-active]
                              (set-active new-active)
                              (when (.-current auto-save-fn)
                                ((.-current auto-save-fn) new-active 
                                               (fn [_response] 
                                                 (js/console.log "Auto-saved pricing for part" (:id part) "active:" new-active)))))]
    [:div {:style {:margin-bottom "0.75rem"
                   :padding "1rem"
                   :background-color (if is-active "#fefefe" "#f5f5f5")
                   :border-radius "8px"
                   :border (str "2px solid " (if is-active "#3b82f6" "#d1d5db"))
                   :border-left (str "4px solid " (if is-active "#1d4ed8" "#9ca3af"))
                   :opacity (if is-active "1" "0.7")
                   :transition "all 0.2s ease"
                   :box-shadow "0 1px 3px rgba(0, 0, 0, 0.1)"}}
     
     ;; Header row: Picture + Name + Active/Inactive
     [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :align-items "center"
                   :margin-bottom "0.5rem"
                   :gap "1rem"}}
      
      ;; Picture and name
      [:div {:style {:display "flex"
                    :align-items "center"
                    :gap "0.75rem"
                    :flex "1"}}
       
       ;; Picture
       (if (:picture_url part)
         [:div {:style {:width "40px"
                        :height "40px"
                        :border "1px solid var(--border-clr)"
                        :border-radius "6px"
                        :overflow "hidden"
                        :flex-shrink "0"}}
          [:img {:src (:picture_url part)
                 :style {:width "100%"
                         :height "100%"
                         :object-fit "cover"}}]]
         [:div {:style {:width "40px"
                        :height "40px"
                        :border "1px solid var(--border-clr)"
                        :border-radius "6px"
                        :background-color "var(--bg-clr)"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"
                        :color "var(--muted-clr)"
                        :font-size "16px"
                        :flex-shrink "0"}}
          "🖼️"])
       
       ;; Name
       [:div {:style {:font-weight "600"
                      :color (if is-active "var(--text-clr)" "var(--muted-clr)")}}
        (:name part)]]
      
      ;; Active/Inactive toggle
      [:div {:style {:display "flex"
                    :align-items "center"
                    :gap "0.5rem"
                    :flex-shrink "0"}}
       [:span {:style {:font-size "0.8rem"
                      :color (if is-active "var(--text-clr)" "var(--muted-clr)")
                      :font-weight "500"}}
        (if is-active "Active" "Inactive")]
       [:input {:type "checkbox"
                :checked is-active
                :on-change #(handle-active-change (.. % -target -checked))
                :style {:width "18px"
                       :height "18px"
                       :cursor "pointer"}}]]]
     
     ;; Description
     (when (:description part)
       [:div {:style {:color (if is-active "var(--muted-clr)" "#9ca3af")
                      :margin-bottom "0.75rem"
                      :font-size "0.85rem"
                      :max-height "60px"
                      :overflow "hidden"}}
        (:description part)])
     
     ;; Pricing section
     (when (:id part)
       [:div {:style {:opacity (if is-active "1" "0.5")}}
        [pricing/pricing-section {:part-id (:id part)
                                 :workspace-id workspace-id
                                 :part-data part
                                 :is-active is-active
                                 :on-active-change handle-active-change
                                 :on-auto-save (fn [save-fn] 
                                                (set! (.-current auto-save-fn) save-fn))}]])]))

(defn package-accordion [package]
  (let [parts-count (count (:parts package))]
    [accordion/view {:title [:div {:style {:display "flex" :align-items "center"}}
                             (:name package)
                             [badge :package parts-count]]
                     :open? false
                     :style {:margin-bottom "0.5rem"
                             :border "1px solid #d97706"
                             :border-left "3px solid #ea580c"
                             :box-shadow "0 1px 2px rgba(217, 119, 6, 0.1)"}}
     (for [part (:parts package)]
       ^{:key (:id part)}
       [part-item part])]))

(defn category-accordion [category]
  (let [total-packages (count (:packages category))
        total-subcategories (count (:children category))
        total-items (+ total-packages total-subcategories)]
    [accordion/view {:title [:div {:style {:display "flex" :align-items "center"}}
                             (:name category)
                             [badge :category total-items]]
                     :open? false
                     :style {:margin-bottom "1rem"
                             :border "2px solid #059669"
                             :border-left "4px solid #047857"
                             :box-shadow "0 2px 4px rgba(5, 150, 105, 0.15)"
                             :background-color "#f0fdf4"}}
     ;; Child categories (subcategories)
     (for [child-category (:children category)]
       ^{:key (:id child-category)}
       [category-accordion child-category])
     
     ;; Packages in this category
     (for [package (:packages category)]
       ^{:key (:id package)}
       [package-accordion package])]))


(defn hierarchical-view []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "1rem"
                 :padding "1rem"
                 :max-width "1200px"
                 :margin "0 auto"}}
   (for [category @hierarchical-data]
     ^{:key (:id category)}
     [category-accordion category])])

(defn view []
  (zero-react/use-effect
   {:mount (fn []
             (let [workspace-id @(rf/subscribe [:db/get-in [:router :path-params :workspace-id]])]
               (parts-request/get-parts
                workspace-id
                (fn [response]
                  (js/console.log "Response from backend:" response)
                  (let [categories (:workspace-parts/get-parts response)]
                    (js/console.log "Hierarchical categories data:" categories)
                    (if (sequential? categories)
                      (reset! hierarchical-data categories)
                      (do
                        (js/console.error "Expected sequential data, got:" (type categories))
                        (reset! hierarchical-data []))))))))})

  [body/view
   {:title "Parts Pricing"
    :description "Manage parts pricing with four price groups: basic, basic+, pro, and pro+. Active parts are shown in the customizer."
    :body [hierarchical-view]}])
