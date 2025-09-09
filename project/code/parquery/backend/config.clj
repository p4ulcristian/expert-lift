(ns parquery.backend.config
  (:require
   [features.flex.teams.backend.read :as teams-read]
   [features.flex.teams.backend.write :as teams-write]
   [features.flex.workspaces.backend.read :as workspaces-read]
   [features.flex.workspaces.backend.write :as workspaces-write]
   [features.flex.dashboard.backend.read :as dashboard-read]
   [features.flex.dashboard.backend.write :as dashboard-write]
   [features.flex.machines.backend.read :as machines-read]
   [features.flex.machines.backend.write :as machines-write]
   [features.flex.locations.backend.read :as locations-read]
   [features.flex.locations.backend.write :as locations-write]
   [features.flex.processes.backend.read :as processes-read]
   [features.flex.processes.backend.write :as processes-write]
   [features.flex.recipes.backend.read :as recipes-read]
   [features.flex.recipes.backend.write :as recipes-write]
   [features.flex.workstations.backend.read :as workstations-read]
   [features.flex.workstations.backend.write :as workstations-write]
   [features.flex.inventory.backend.read :as inventory-read]
   [features.flex.inventory.backend.write :as inventory-write]
   [features.flex.batches.backend.read :as batches-read]
   [features.flex.batches.backend.write :as batches-write]
   [features.flex.jobs.backend.read :as jobs-read]
   [features.flex.jobs.backend.write :as jobs-write]
   [features.flex.orders.backend.read :as orders-read]
   [features.flex.orders.backend.write :as orders-write]
   [features.flex.service-areas.backend.read :as service-areas-read]
   [features.flex.parts-pricing.backend.read :as parts-pricing-read]
   [features.flex.services-pricing.backend.read :as services-pricing-read]
   [users.backend.resolvers :as users]))

;; Example handler function structure:
;; Handler functions receive a single map parameter containing:
;; - :parquery/context: map with shared data like {:workspace-id 123 :user-id 456}
;; - other keys: individual function parameters
;;
;; (defn user-get
;;   "Example: Get user by id - receives {:parquery/context {...} :id user-id}"
;;   [{:parquery/keys [context] :as params}]
;;   (let [user-id (:id params)]
;;     {:user-id user-id
;;      :workspace-id (:workspace-id context)
;;      :name "John Doe" 
;;      :email "john@example.com"}))
;;
;; (defn orders-list
;;   "Example: List orders - receives {:parquery/context {...} :limit 10 :offset 0 :status 'active'}"
;;   [{:parquery/keys [context] :as params}]
;;   (let [{:keys [limit offset status]} params]
;;     {:orders [{:id 1 :status status :user-id (:user-id context)}]
;;      :pagination {:limit limit :offset offset}
;;      :workspace-id (:workspace-id context)}))

;; Actual handler implementations
(defn user-get
  "Get user by id"
  [{:parquery/keys [context] :as params}]
  (let [id (:id params)]
    {:user-id id
     :name "John Doe"
     :email "john@example.com"
     :parquery/context context}))

(defn get-current-user
  "Get current logged-in user data"
  [{:parquery/keys [context request] :as params}]
  (let [user-id (get-in request [:session :user-id])]
    (if user-id
      (try
        (let [user (users/get-user-by-id-fn user-id)]
          (when user
            {:user/id (:id user)
             :user/first-name (:first_name user)
             :user/last-name (:last_name user)
             :user/email (:email user)
             :user/picture-url (:picture_url user)
             :user/full-name (users/get-user-full-name user)}))
        (catch Exception e
          (println "Error fetching current user:" (.getMessage e))
          nil))
      nil)))

;; Query mappings to functions
(def read-queries
  "Read operations - mapped to handler functions"
  {:user/get #'user-get
   :user/current #'get-current-user
   :current-user/basic-data #'get-current-user
   ;; Teams
   :workspace/users #'teams-read/get-workspace-users
   :workspace/invitations #'teams-read/get-workspace-invitations
   :invitation/details #'teams-read/get-invitation-details
   ;; Workspaces
   :workspaces/get-my-workspaces #'workspaces-read/get-my-workspaces
   :workspace/get-name #'workspaces-read/get-workspace-name
   :workspace/get #'workspaces-read/get-workspace
   ;; Dashboard
   :dashboard/get-recent-orders #'dashboard-read/get-recent-orders
   :dashboard/get-order-stats #'dashboard-read/get-order-stats
   :dashboard/get-workspace-name #'dashboard-read/get-workspace-name
   :dashboard/get-client-name #'dashboard-read/get-client-name
   ;; Machines
   :machines/get-machines #'machines-read/get-machines
   :machines/get-machine #'machines-read/get-machine
   :machines/get-machines-needing-maintenance #'machines-read/get-machines-needing-maintenance
   :machines/get-machine-stats #'machines-read/get-machine-stats
   ;; Locations
   :locations/get-locations #'locations-read/get-locations
   :locations/get-location #'locations-read/get-location
   ;; Processes
   :processes/get-processes #'processes-read/get-processes
   :processes/get-process #'processes-read/get-process
   ;; Recipes
   :recipes/get-recipes #'recipes-read/get-recipes
   :recipes/get-recipe #'recipes-read/get-recipe
   ;; Workstations
   :workstations/get-workstations #'workstations-read/get-workstations
   :workstations/get-workstation #'workstations-read/get-workstation
   :workstations/get-workstation-machines #'workstations-read/get-workstation-machines-query
   :workstations/get-workstation-processes #'workstations-read/get-workstation-processes-query
   :workstations/get-available-machines #'workstations-read/get-available-machines
   :workstations/get-available-processes #'workstations-read/get-available-processes
   :workstations/get-batches-with-current-step-on-workstation #'workstations-read/get-batches-with-current-step-on-workstation
   :workstations/get-workstation-batches #'workstations-read/get-workstation-batches
   ;; Inventory
   :inventory/get-items #'inventory-read/get-inventory
   :inventory/get-item #'inventory-read/get-inventory-item
   ;; Batches
   :batches/get-current-batches #'batches-read/get-current-batches
   ;; Jobs
   :jobs/get-jobs #'jobs-read/get-jobs
   :jobs/get-job #'jobs-read/get-job
   ;; Orders
   :orders/get-orders #'orders-read/get-my-orders
   :orders/get-order #'orders-read/get-order
   ;; Service Areas
   :service-areas/get-areas #'service-areas-read/get-service-areas
   ;; Parts Pricing
   :parts-pricing/get-prices #'parts-pricing-read/get-parts
   ;; Services Pricing
   :services-pricing/get-prices #'services-pricing-read/get-services})

(def write-queries
  "Write operations - mapped to handler functions"  
  {;; Teams
   :teams/invite-member #'teams-write/invite-team-member
   :teams/remove-invitation #'teams-write/remove-invitation
   :teams/remove-member #'teams-write/remove-team-member
   :invitation/accept #'teams-write/accept-invitation
   ;; Workspaces
   :workspaces/add-workspace #'workspaces-write/add-workspace
   :workspaces/remove-workspace #'workspaces-write/remove-workspace
   ;; Dashboard
   :dashboard/send-email #'dashboard-write/send-dashboard-email
   ;; Machines
   :machines/create-machine #'machines-write/create-machine
   :machines/edit-machine #'machines-write/edit-machine
   :machines/delete-machine #'machines-write/delete-machine
   :machines/update-usage #'machines-write/update-machine-usage
   ;; Locations
   :locations/create-location #'locations-write/create-location
   :locations/edit-location #'locations-write/edit-location
   :locations/delete-location #'locations-write/delete-location
   ;; Processes
   :processes/create-process #'processes-write/create-process
   :processes/edit-process #'processes-write/edit-process
   :processes/delete-process #'processes-write/delete-process
   ;; Recipes
   :recipes/create-recipe #'recipes-write/create-recipe
   :recipes/edit-recipe #'recipes-write/edit-recipe
   :recipes/delete-recipe #'recipes-write/delete-recipe
   ;; Workstations
   :workstations/create-workstation #'workstations-write/create-workstation
   :workstations/edit-workstation #'workstations-write/edit-workstation
   :workstations/delete-workstation #'workstations-write/delete-workstation
   :workstations/assign-machine #'workstations-write/assign-machine
   :workstations/unassign-machine #'workstations-write/unassign-machine
   :workstations/assign-process #'workstations-write/assign-process
   :workstations/unassign-process #'workstations-write/unassign-process
   ;; Inventory
   :inventory/create-item #'inventory-write/create-inventory-item
   :inventory/edit-item #'inventory-write/edit-inventory-item
   :inventory/delete-item #'inventory-write/delete-inventory-item
   ;; Batches
   :batches/create-batch #'batches-write/save-batches
   :batches/edit-batch #'batches-write/confirm-batch
   ;; Jobs  
   :jobs/create-job #'jobs-write/create-ad-job
   ;; Orders
   :orders/create-order #'orders-write/add-demo-order
   :orders/edit-order #'orders-write/update-order-status
   :orders/delete-order #'orders-write/cancel-order})

(defn get-query-type
  "Returns query type based on config"
  [query-key]
  (cond
    (contains? read-queries query-key) :read
    (contains? write-queries query-key) :write
    (= "parquery" (namespace query-key)) :parquery
    :else :unknown))

(defn get-query-handler
  "Returns the handler function for a query"
  [query-key]
  (or (get read-queries query-key)
      (get write-queries query-key)))