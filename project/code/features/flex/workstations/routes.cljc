(ns features.flex.workstations.routes
  #?(:cljs (:require [features.flex.workstations.frontend.workstation :as workstation]
                     [features.flex.workstations.frontend.workstations :as workstations]
                     [features.flex.workstations.frontend.task-board :as task-board]
                     [features.flex.workstations.frontend.operator :as operator])
     :clj  (:require [features.flex.zero.backend.view :as backend-view]
                     [authentication.middlewares.flex :as user-middleware])))

(def workstations-path "/flex/ws/:workspace-id/workstations")
(def workstation-path "/flex/ws/:workspace-id/workstations/:workstation-id")
(def workstation-task-board-path "/flex/ws/:workspace-id/workstations/:workstation-id/task-board")
(def workstation-operator-path "/flex/ws/:workspace-id/workstations/:workstation-id/operator")

(def routes
  #?(:cljs [{:path "/flex/ws/:workspace-id/workstations"
             :view workstations/view}
            {:path "/flex/ws/:workspace-id/workstations/:workstation-id"
             :view workstation/view}
            {:path "/flex/ws/:workspace-id/workstations/:workstation-id/task-board"
             :view task-board/view}
            {:path "/flex/ws/:workspace-id/workstations/:workstation-id/operator"
             :view operator/view}]
     :clj  [{:path workstations-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path workstation-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path workstation-task-board-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}
            {:path workstation-operator-path
             :get #'backend-view/response
             :middleware [user-middleware/require-flex-role]}])) 