(ns features.labs.zero.frontend.zero
  (:require
   [app.frontend.request]
   [zero.frontend.re-frame]
   [router.frontend.zero           :as router]
   [features.labs.looks.routes     :as looks]
   [features.labs.forms.routes     :as forms-routes]
   [features.labs.users.routes     :as users-routes]
   [features.labs.services.routes  :as services-routes]
   [features.labs.dashboard.routes :as dashboard-routes]
   [features.labs.parts.routes     :as categories-routes]
   [features.labs.storage.routes   :as storage-routes]
   [features.labs.zip-codes.routes :as zip-codes-routes]
   [features.labs.workspaces.routes :as workspaces-routes]
   [parquery.routes                :as parquery-routes]

   [ui.modals.zero     :as modals]
   [ui.notification    :as notification]
   [ui.popover-manager :as popover-manager]))

(def routes 
  (concat 
   looks/routes
   dashboard-routes/routes
   forms-routes/routes
   categories-routes/routes
   services-routes/routes
   users-routes/routes
   storage-routes/routes
   zip-codes-routes/routes
   workspaces-routes/routes
   parquery-routes/routes))

(defn view []
  (let [router-data  (:data @router/state)]
     ;; This is the router component and the component-params 
     ;; are passed to the component 
     ;; Wrapper + router-component + component-params
    ;; [ui.floater-manager/view]
    ;; [ui.popup-manager/view]
    [:<>
      [:div {:id "main-content"
             :style {:overflow-y "auto"
                     :height     "100vh"}}

         [(:view router-data)]
       [popover-manager/view {:scroll-ref #(.getElementById js/document "main-content")}]
       [modals/modals]
       [notification/hub {:toastOptions {:duration 1500
                                         :style {:background "var(--ir-primary)"
                                                 :color      "var(--ir-text-primary)"}}}]]]))
                         
              