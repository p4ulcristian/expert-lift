(ns features.app.worksheets.frontend.queries
  "ParQuery operations for worksheets feature"
  (:require
   [parquery.frontend.request :as parquery]))

(defn load-worksheets
  "Execute ParQuery to load worksheets with pagination"
  [workspace-id params worksheets-data loading?]
  (reset! loading? true)
  (parquery/send-queries
   {:queries {:workspace-worksheets/get-paginated (or params {})}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (reset! loading? false)
                (let [result (:workspace-worksheets/get-paginated response)]
                  (reset! worksheets-data result)))}))

(defn save-worksheet
  "Execute ParQuery to save worksheet (create or update)"
  [worksheet workspace-id modal-is-new? callback modal-worksheet load-worksheets-fn]
  (let [is-new? @modal-is-new?
        query-type (if is-new? :workspace-worksheets/create :workspace-worksheets/update)
        worksheet-data (if is-new? (dissoc worksheet :worksheet/id) worksheet)
        context {:workspace-id workspace-id}]
    (parquery/send-queries
     {:queries {query-type worksheet-data}
      :parquery/context context
      :callback (fn [response]
                  (callback)
                  (if (:success (get response query-type))
                    (do (reset! modal-worksheet nil)
                        (load-worksheets-fn {}))
                    (js/alert (str "Error: " (:error (get response query-type))))))})))

(defn delete-worksheet
  "Execute ParQuery to delete worksheet"
  [worksheet-id workspace-id load-worksheets-fn]
  (parquery/send-queries
   {:queries {:workspace-worksheets/delete {:worksheet/id worksheet-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (if (:success (:workspace-worksheets/delete response))
                  (load-worksheets-fn)
                  (js/alert "Error deleting worksheet")))}))

(defn load-addresses
  "Load addresses for a workspace"
  [workspace-id addresses-atom loading-atom]
  (when workspace-id
    (reset! loading-atom true)
    (parquery/send-queries
     {:queries {:workspace-addresses/get-all {:workspace-id workspace-id}}
      :parquery/context {:workspace-id workspace-id}
      :callback (fn [response]
                  (reset! loading-atom false)
                  (let [addr-list (:workspace-addresses/get-all response [])]
                    (reset! addresses-atom addr-list)))})))
