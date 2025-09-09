(ns parquery.backend.config
  (:require
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
   :current-user/basic-data #'get-current-user})

(def write-queries
  "Write operations - mapped to handler functions"  
  {})

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