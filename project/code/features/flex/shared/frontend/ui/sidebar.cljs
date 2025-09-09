(ns features.flex.shared.frontend.ui.sidebar
  (:require
   ["framer-motion" :refer [motion]]
   [re-frame.core       :as r]
   [reagent.core        :as reagent]
   [router.frontend.zero :as router]
   [ui.button  :as button]
   [ui.tooltip :as tooltip]
   [zero.frontend.react :as zero-react]
   [zero.frontend.window-dimensions :as window-dimensions]))

(def open? (reagent/atom true))

(def sidebar-color "linear-gradient(135deg, #1a1f2e 0%, #0f1419 100%)")
(def small-sidebar-width 50)
(def sidebar-item-height 20)
(defn add-ws-id-to-url [url]
  (let [wsid @(r/subscribe [:workspace/get-id])]
    (str "/flex/ws/" wsid "/" url)))



(defn business [] 
  [
   {:icon       "fa-solid fa-clipboard-list"
    :url (add-ws-id-to-url "orders")
    :label      "Orders"}

   {:icon       "fa-solid fa-tasks"
    :url (add-ws-id-to-url "jobs")
    :label      "Jobs"}])

(defn facility [] 
  [
   {:icon      "fa-solid fa-boxes-stacked"
    :url       (add-ws-id-to-url "inventory")
    :label     "Inventory"}
  
   {:icon       "fa-solid fa-location-dot"
    :url        (add-ws-id-to-url "locations")
    :label      "Locations"}

   {:icon       "fa-solid fa-gear"
    :url        (add-ws-id-to-url "machines")
    :label      "Machines"}
  
   {:icon       "fa-solid fa-desktop"
    :url        (add-ws-id-to-url "workstations")
    :label      "Workstations"}

   {:icon       "fa-solid fa-diagram-project"
    :url        (add-ws-id-to-url "processes")
    :label      "Processes"}
  
   {:icon       "fa-solid fa-book-open"
    :url        (add-ws-id-to-url "recipes")
    :label      "Recipes"}])

(defn customizer []
  [
   {:icon       "fa-solid fa-puzzle-piece"
    :url        (add-ws-id-to-url "parts")
    :label      "Parts"}

   {:icon       "fa-solid fa-wrench"
    :url        (add-ws-id-to-url "services")
    :label      "Services"}])

(defn facility-info []
  [
   {:icon       "fa-solid fa-map-marked-alt"
    :url (add-ws-id-to-url "service-area")
    :label      "Service Area"}

   {:icon       "fa-solid fa-info-circle"
    :url (add-ws-id-to-url "business-info")
    :label      "Business Info"}

   {:icon       "fa-solid fa-users"
    :url (add-ws-id-to-url "team-roles")
    :label      "Team & Roles"}

   {:icon       "fa-solid fa-folder-tree"
    :url (add-ws-id-to-url "storage")
    :label      "File Storage"}])


;; -----------------------------------------------------------------------------
;; ---- Components ----


(defn sidebar-item [{:keys [_minified? group-color label icon url action]}]
  (let [current-path @(r/subscribe [:db/get-in [:router :path]])
        active? (and url (= current-path url))
        click-handler #(router/navigate! {:path url})]
    [button/view {:mode  :filled
                  :color (if active? group-color sidebar-color)
                  :on-click click-handler
                  :style {:box-shadow            "none"
                          :width                 "100%"
                          :color                 "white"
                          :text-align            "left"
                          :padding               "0"
                          :display               "grid"
                          :font-weight           "500"
                          :grid-template-columns (str small-sidebar-width 
                                                      "px 1fr")
                          :align-items           "center"
                          :opacity               (if active? 1.0 0.8)
                          :height                "44px"}}

     [:div 
      {:style {:width small-sidebar-width
               :height sidebar-item-height
               :display :flex 
               :align-items :center 
               :justify-content :center}}
      [:i {:class icon
                :style {:color (if active? "white" group-color)
                        :width "16px"
                        :text-align "center"
                        :display "inline-block"}}]]
     ""
     [:div {:class "menu-item-label"
            :style {:white-space "nowrap"
                    :overflow "hidden"
                    :text-overflow "ellipsis"
                    :padding-left "8px"
                    :padding-right "12px"
                    :display "flex"
                    :align-items "center"}} label]]))

(defn sidebar-button [{:keys [minified? label _action] :as item-props}]
  (if minified?
    [tooltip/view {:tooltip  label
                   :duration 200
                   :align    [:center :right]
                   :anchor   [:center :left]
                   :style    {:margin-left "10px"}}
     [sidebar-item item-props]]
    [sidebar-item item-props]))

(defn sidebar-items [{:keys [items
                             minified?
                             group-color]}]
  [:<>
   (map (fn [{:keys [icon label url action]}]
          ^{:key label}
          [sidebar-button {:label       label
                           :url         url
                           :action      action
                           :minified?   minified?
                           :group-color group-color
                           :icon        icon}])

        items)])

(defn sidebar-group [group-label menu-items-props]
  [:div {:class "flex-sidebar--group"
         :style {:width "100%"}}

   [:div {:class "flex-sidebar--group-label"
          :style {:color (:group-color menu-items-props)
                  :height "2em"
                  :white-space "nowrap"
                  :overflow "hidden"
                  :text-overflow "ellipsis"
                  :display "flex"
                  :align-items "center"
                  :justify-content (if (:minified? menu-items-props) "center" "flex-start")
                  :width (if (:minified? menu-items-props) (str small-sidebar-width "px") "100%")
                  :padding-left (when-not (:minified? menu-items-props) "8px")
                  :padding-right (when-not (:minified? menu-items-props) "8px")}}
    [:span (if (:minified? menu-items-props)
             (first group-label)
             group-label)]]

   (when (:profile-section? menu-items-props)
     (:profile-picture menu-items-props))

   [sidebar-items menu-items-props]])

;; ---- Sidebar Content Components ----

(defn sidebar-groups [minified? user-data]
  [:div {:style {:padding-top "16px"}}
   [sidebar-group "Business" {:items (business) :minified? minified? :group-color "var(--prim-clr)"}]
   [sidebar-group "Facility" {:items (facility) :minified? minified? :group-color "var(--seco-clr)"}]
   [sidebar-group "Customizer" {:items (customizer) :minified? minified? :group-color "var(--succ-clr)"}]
   [sidebar-group "Facility Info" {:items (facility-info) :minified? minified? :group-color "#8B5CF6"}]])

(defn scroll-container [minified? user-data]
  [:div {:class "scroll-container"
         :style {:overflow "auto"
                 :height "100%"
                 :scrollbar-width "none"
                 :-ms-overflow-style "none"
                 :-webkit-scrollbar {:display "none"}}}
   [sidebar-groups minified? user-data]])

(defn sidebar-motion-wrapper [sidebar-width minified? window-small? close-fn open-fn user-data]
  [:div {:id "flex-sidebar"
         :style {:width (str sidebar-width "px")
                 :background sidebar-color
                 :border-radius "12px"
                 :overflow "hidden"
                 :height "100%"
                 :box-shadow "0 4px 20px rgba(0, 0, 0, 0.15), 0 1px 4px rgba(0, 0, 0, 0.1)"}}
   [scroll-container minified? user-data]])

;; ---- Main Component ----

(defn animated-sidebar [minified? window-small? close-fn open-fn user-data] 
  [:> (.-div motion)
   {:initial {:width (if minified? small-sidebar-width 200)}
    :animate {:width (if minified? small-sidebar-width 200)}
    :transition {:type "spring" :stiffness 120 :damping 15}
    :style {:height "100%"}}
   [sidebar-motion-wrapper 
    (if minified? small-sidebar-width 200)
    minified? 
    window-small? 
    close-fn 
    open-fn 
    user-data]])

(defn view [{:keys [_items on-change user-data grid-column grid-row]}]
  (let [window-small? (window-dimensions/small?)
        minified? (or window-small? (not @open?))
        workspace-id @(r/subscribe [:workspace/get-id])
        show-overlay? (nil? workspace-id)
        
        close-fn #(do
                   (when on-change (on-change false))
                   (reset! open? false))
        open-fn #(do
                  (when on-change (on-change true))
                  (reset! open? true))]

    (zero-react/use-effect
     {:mount #(if window-small? (close-fn) (open-fn))
      :params #js [window-small?]})

    [:div {:class "hide-scroll"
           :style (merge {:border-radius "12px"
                         :overflow "hidden"
                         :height "100%"
                         :padding "16px 8px 16px 16px"
                         :box-sizing "border-box"
                         :position "relative"}
                        (when grid-column {:grid-column grid-column})
                        (when grid-row {:grid-row grid-row}))}
     [animated-sidebar minified? window-small? close-fn open-fn user-data]
     (when show-overlay?
       [:div {:style {:position "absolute"
                      :top "0"
                      :left "0"
                      :right "0"
                      :bottom "0"
                      :backdrop-filter "blur(4px)"
                      :pointer-events "auto"
                      :cursor "not-allowed"
                      :z-index "10"}}])]))