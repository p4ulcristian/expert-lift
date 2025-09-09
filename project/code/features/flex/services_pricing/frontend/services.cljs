(ns features.flex.services-pricing.frontend.services
  (:require
   ["react" :as react]
   [features.flex.services-pricing.frontend.request :as services-request]
   [features.flex.services-pricing.frontend.pricing :as pricing]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [ui.card :as card]
   [zero.frontend.react :as zero-react]))

(def services-data (r/atom []))

(defn service-card [[id service]]
  ;; Debug: Log service data to see what we're receiving
  (js/console.log "Service card data - ID:" id "Service:" service)
  (let [workspace-id @(rf/subscribe [:db/get-in [:router :path-params :workspace-id]])
        ;; Initialize active state from pricing data, default to false if no pricing exists
        initial-active (get-in service [:pricing :active] false)
        [is-active set-active] (react/useState initial-active)
        ;; Store auto-save function from pricing component
        auto-save-fn (react/useRef nil)
        
        ;; Function to handle active checkbox change
        handle-active-change (fn [new-active]
                              (set-active new-active)
                              ;; Auto-save pricing when active state changes
                              (when (.-current auto-save-fn)
                                ((.-current auto-save-fn) new-active 
                                               (fn [_response] 
                                                 (js/console.log "Auto-saved pricing for service" id "active:" new-active)))))]
    [card/view
     {:content
      [:div {:style {:width "100%"
                     :display "flex"
                     :flex-direction "column"
                     :gap "0.5rem"
                     :padding "0.75rem"
                     :background-color (if is-active "#fefefe" "#f5f5f5")
                     :border-radius "8px"
                     :border (str "1px solid " (if is-active "#e1e5e9" "#d1d5db"))
                     :opacity (if is-active "1" "0.7")
                     :transition "all 0.2s ease"}}
       
       ;; Header row: Service Icon + Name + Active/Inactive
       [:div {:style {:display "flex"
                     :justify-content "space-between"
                     :align-items "center"
                     :padding "0.75rem"
                     :background-color (if is-active "#f9fafb" "#f0f0f0")
                     :border-radius "6px"
                     :border (str "1px solid " (if is-active "#e5e7eb" "#d1d5db"))
                     :gap "1rem"}}
        
        ;; Service icon on the left
        [:div {:style {:flex-shrink "0"}}
         [:div {:style {:width "50px"
                        :height "50px"
                        :border "1px solid var(--border-clr)"
                        :border-radius "6px"
                        :background-color "var(--bg-clr)"
                        :display "flex"
                        :align-items "center"
                        :justify-content "center"
                        :color "var(--muted-clr)"
                        :font-size "24px"
                        :opacity (if is-active "1" "0.6")}}
          "🛠️"]]
        
        ;; Service name
        [:div {:style {:flex "1"
                      :min-width "0"
                      :display "flex"
                      :align-items "center"}}
         [:span {:style {:font-weight "600"
                        :color (if is-active "var(--text-clr)" "var(--muted-clr)")
                        :font-size "1rem"}}
          (:name service)]]
        
        ;; Active/Inactive controls on the right
        [:div {:style {:display "flex"
                      :align-items "center"
                      :gap "0.5rem"
                      :flex-shrink "0"}}
         [:span {:style {:font-size "0.8rem"
                        :color (if is-active "var(--text-clr)" "var(--muted-clr)")
                        :font-weight "500"
                        :white-space "nowrap"}}
          (if is-active "Active" "Inactive")]
         [:input {:type "checkbox"
                  :checked is-active
                  :on-change #(handle-active-change (.. % -target -checked))
                  :style {:width "18px"
                         :height "18px"
                         :cursor "pointer"}}]]]
       
       ;; Content section: Description
       [:div {:style {:display "flex"
                     :flex-direction "column"
                     :gap "0.5rem"
                     :opacity (if is-active "1" "0.8")}}
        
        ;; Description
        (when (:description service)
          [:div {:style {:color (if is-active "var(--muted-clr)" "#9ca3af")
                         :margin-bottom "0.5rem"
                         :max-height "60px"
                         :overflow "hidden"
                         :text-overflow "ellipsis"
                         :display "-webkit-box"
                         :-webkit-line-clamp "2"
                         :-webkit-box-orient "vertical"
                         :font-size "0.85rem"}}
           (:description service)])]
       
       ;; Pricing section at the bottom - disabled when inactive
       (when (and id workspace-id)
         [:div {:style {:opacity (if is-active "1" "0.5")}}
          [pricing/pricing-section {:service-id id
                                   :workspace-id workspace-id
                                   :service-data service
                                   :is-active is-active
                                   :on-active-change handle-active-change
                                   :on-auto-save (fn [save-fn] 
                                                  (set! (.-current auto-save-fn) save-fn))}]])]}]))

(defn services-grid []
  [:div {:style {:display "flex"
                 :flex-direction "column"
                 :gap "1rem"
                 :max-width "800px"
                 :margin "0 auto"}}
   (when (sequential? @services-data)
     (for [service-entry @services-data]
       (cond
         ;; Handle [id service] vector format
         (and (vector? service-entry) (= 2 (count service-entry)))
         (let [[id service] service-entry]
           ^{:key (str id)}
           [service-card [id service]])
         
         ;; Handle map format with :id key
         (map? service-entry)
         (let [id (:id service-entry)
               service (dissoc service-entry :id)]
           ^{:key (str id)}
           [service-card [id service]])
         
         ;; Fallback - log and skip
         :else
         (do
           (js/console.warn "Unexpected service entry format:" service-entry)
           nil))))])

(defn view []
  (zero-react/use-effect
   {:mount (fn []
             (let [workspace-id @(rf/subscribe [:db/get-in [:router :path-params :workspace-id]])]
              (services-request/get-services
               workspace-id
               (fn [response]
                 (js/console.log "Response from backend:" response)
                 (let [services (:workspace-services-pricing/get-services response)]
                   (js/console.log "Workspace services data:" services)
                   (if (sequential? services)
                     (reset! services-data services)
                     (do
                       (js/console.error "Expected sequential data, got:" (type services))
                       (reset! services-data []))))))))})

  [services-grid])