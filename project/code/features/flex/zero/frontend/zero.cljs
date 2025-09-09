(ns features.flex.zero.frontend.zero
  (:require
   ["react" :as react]
   [re-frame.core :as rf]
   [zero.frontend.re-frame]
   [router.frontend.zero            :as router]
   [features.flex.shared.frontend.ui.layout :as layout]
   [features.flex.zero.frontend.fetch :as fetch]
   [features.flex.zero.frontend.mvc]
   [features.flex.dashboard.routes :as dashboard-routes]
   [features.flex.storage.routes :as storage.routes]
   [features.flex.workspaces.routes :as workspaces.routes]
   [features.flex.orders.routes :as orders.routes]
   [features.flex.jobs.routes :as jobs.routes]
   [features.flex.services-pricing.routes :as services.routes]
   [features.flex.processes.routes :as processes.routes]
   [features.flex.recipes.routes :as recipes.routes]
   [features.flex.inventory.routes :as inventory.routes]
   [features.flex.workstations.routes :as workstations.routes]
   [features.flex.batches.routes :as batches.routes]
   [features.flex.parts-pricing.routes :as parts.routes]
   [features.flex.locations.routes :as locations.routes]
   [features.flex.machines.routes :as machines.routes]
   [features.flex.service-areas.routes :as service-areas.routes]
   [features.flex.business-info.routes :as business-info.routes]
   [features.flex.teams.routes :as teams.routes]
))

(def routes (concat
             dashboard-routes/routes
             storage.routes/routes
             workspaces.routes/routes
             orders.routes/routes
             jobs.routes/routes
             services.routes/routes
             processes.routes/routes
             recipes.routes/routes
             inventory.routes/routes
             workstations.routes/routes
             batches.routes/routes
             parts.routes/routes
             locations.routes/routes
             machines.routes/routes
             service-areas.routes/routes
             business-info.routes/routes
             teams.routes/routes
))

(defn no-view-found []
  [:div
   {:style {:width "100%"
            :height "200px"
            :display :flex
            :font-size "30px"
            :font-weight "bold"
            :justify-content :center
            :align-items :center
            :flex-direction :column}}
   [:i {:style {:padding "10px"}
        :class "fa-solid fa-ban"}]
   [:div "No view found"]])



(defn current-view [view]
  (if view [view] [no-view-found]))

(defn set-page-title [title]
  (set! (.-title js/document) title))

(defn view []
  (let [router-data  (:data @router/state)
        path         (:path router-data)
        view         (:view router-data)
        title        (:title router-data)
        workspace-id (rf/subscribe [:db/get-in [:router :path-params :workspace-id]])]
    
    ;; Fetch user data on app initialization
    (fetch/use-user-data-effect)
    
    ;; Fetch workspace data when workspace-id is available
    (fetch/use-workspace-data-effect @workspace-id)
    
    (when title (set-page-title title))
    [layout/view 
     {:content [current-view view] 
      :title title
      :path path}]))
