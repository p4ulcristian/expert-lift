(ns parquery.example-handlers)

(defn for-you
  "Example handler function - returns personalized data"
  [{:parquery/keys [context] :as params}]
  (let [user-id (:user-id params)
        message (:message params "Hello")]
    {:user-id user-id
     :personalized-message (str message " just for you!")
     :timestamp (str (java.time.Instant/now))
     :parquery/context-data context}))

(defn get-items
  "Example handler - returns a list of items"
  [{:parquery/keys [context] :as params}]
  (let [{:keys [limit offset category]} params
        workspace-id (:workspace-id context)]
    {:items [{:id 1 :name "Item 1" :category category :workspace workspace-id}
             {:id 2 :name "Item 2" :category category :workspace workspace-id}]
     :pagination {:limit (or limit 10) :offset (or offset 0)}
     :total-count 50}))

;; To register these handlers, add them to parquery.backend.config:
;;
;; (def read-queries
;;   {:example/for-you #'parquery.example-handlers/for-you
;;    :example/get-items #'parquery.example-handlers/get-items})