(ns features.labs.workspaces.backend.resolvers
  (:require
   [com.wsscode.pathom3.connect.operation :as pco]
   [zero.backend.state.postgres :as postgres]
   [cheshire.core :as json]))

(defn timestamp-to-string 
  "Convert timestamp to ISO string for JSON serialization"
  [timestamp]
  (when timestamp
    (str timestamp)))

(defn parse-zip-codes-jsonb 
  "Parse the JSONB zip_codes column to extract structured data"
  [zip-codes-jsonb]
  (try
    (cond
      (string? zip-codes-jsonb) (json/parse-string zip-codes-jsonb true)
      (nil? zip-codes-jsonb) []
      :else zip-codes-jsonb)
    (catch Exception e
      (println "Error parsing zip codes JSONB:" (.getMessage e))
      [])))

(defn get-workspace-zip-codes-map []
  (try
    (let [zip-codes-records (postgres/execute-honey
                             {:select [:zip_codes]
                              :from [:zip_codes]})
          all-zip-codes (mapcat #(let [parsed (parse-zip-codes-jsonb (:zip_codes %))]
                                   (if (map? parsed)
                                     (if (vector? (:data parsed))
                                       (:data parsed)
                                       [])
                                     []))
                                zip-codes-records)]
      ;; Group zip codes by workspace ID
      (->> all-zip-codes
           (filter #(and (vector? %) 
                         (>= (count %) 6) ; Now checking for 6 elements (including status)
                         (not (nil? (nth % 4)))
                         (not= (str (nth % 4)) "nil")
                         (not= (str (nth % 4)) "")))
           (group-by #(str (nth % 4))) ; Group by workspace ID
           (into {} (map (fn [[workspace-id zip-codes]]
                           [workspace-id (->> zip-codes
                                               (map #(hash-map :zip-code (str (nth % 0))
                                                              :status (str (nth % 5)))) ; Extract zip code and status
                                               (sort-by :zip-code))])))))
    (catch Exception e
      (println "Error fetching workspace zip codes map:" (.getMessage e))
      {})))

(defn get-all-workspaces-with-owners-fn [{:keys [_request] :as _env}]
  (try
    (let [results (postgres/execute-honey
                   {:select [:w.id :w.name :w.created_at :w.updated_at 
                             [:u.first_name :creator_first_name] 
                             [:u.last_name :creator_last_name]
                             [:u.email :creator_email]]
                    :from [[:workspaces :w]]
                    :left-join [[:workspace_shares :ws] [:= :w.id :ws.workspace_id]
                                [:users :u] [:= :ws.user_id :u.id]]
                    :where [:= :ws.role "owner"]
                    :order-by [[:w.created_at :desc]]})
          workspace-zip-codes-map (get-workspace-zip-codes-map)]
      ;; Get zip codes map once for all workspaces
    
      ;; Convert timestamps to strings and add zip codes for each workspace
      (mapv (fn [workspace]
              (let [workspace-id (str (:id workspace))
                    zip-codes (get workspace-zip-codes-map workspace-id [])]
                (-> workspace
                    (update :created_at timestamp-to-string)
                    (update :updated_at timestamp-to-string)
                    (assoc :zip_codes zip-codes))))
            results))
    (catch Exception e
      (println "Error fetching all workspaces with owners:" (.getMessage e))
      [])))

(pco/defresolver get-all-workspaces-res [env _]
  {:workspaces/get-all-workspaces (get-all-workspaces-with-owners-fn env)})

(defn find-matching-zip-code [zip-codes zip-code]
   (some (fn [zc]
          (when (if (map? zc)
                 (= (:zip-code zc) zip-code)
                 (= zc zip-code))
            zc))
        zip-codes))

(defn update-zip-code-row [row workspace-id zip-codes]
  (if (and (vector? row) (>= (count row) 6)) ; Check for 6 elements including status
    (let [zip-code (str (first row))
          current-workspace (str (nth row 4))
          matching-zc (find-matching-zip-code zip-codes zip-code)]
      
      (when matching-zc
        (println "🎯 Found matching zip code:" zip-code "-> " matching-zc))
      
      (if matching-zc
        ;; This zip code should be assigned to our workspace
        (let [updated-row (-> (vec row)
                             (assoc 4 workspace-id))] ; Set workspace
          ;; Update status if provided
          (if (and (map? matching-zc) (:status matching-zc))
            (do
              (println "📝 Updating status for" zip-code "to" (:status matching-zc))
              (assoc updated-row 5 (:status matching-zc)))
            updated-row))
        ;; This zip code should NOT be assigned to our workspace
        (if (= current-workspace (str workspace-id))
          (assoc (vec row) 4 nil) ; Remove from workspace
          row))) ; Leave unchanged
    row))

(defn update-zip-codes-data [parsed-data workspace-id zip-codes]
  (if (and (map? parsed-data) (vector? (:data parsed-data)))
    (let [updated-data (mapv #(update-zip-code-row % workspace-id zip-codes)
                            (:data parsed-data))]
      (assoc parsed-data :data updated-data))
    parsed-data))

(defn update-database-record [record workspace-id zip-codes]
  (let [parsed-data (parse-zip-codes-jsonb (:zip_codes record))]
    (update-zip-codes-data parsed-data workspace-id zip-codes)))

(defn save-zip-codes-to-database [updated-records current-data] 
  (doseq [record updated-records]
    (let [record-id (:id (first current-data)) ; Using first record for now
          json-data (json/generate-string record)]
      (println "💾 Saving to database, record ID:" record-id)
      (postgres/execute-honey
       {:update :zip_codes
        :set {:zip_codes json-data}
        :where [:= :id record-id]}))))

(defn update-workspace-zip-codes-fn [{:keys [_request] :as _env} {:keys [workspace-id zip-codes]}]
  (try
    (println "🔧 Updating zip codes for workspace:" workspace-id)
    (println "🔧 New zip codes list:" zip-codes)
    (println "🔧 Zip codes data types:" (mapv #(if (map? %) (str "Map: " %) (str "String: " %)) zip-codes))
    
    ;; Get current zip codes data
    (let [current-data (postgres/execute-honey
                        {:select [:id :zip_codes]
                         :from [:zip_codes]
                         :order-by [:id]})
          updated-records (mapv #(update-database-record % workspace-id zip-codes)
                               current-data)]
      
      ;; Save updated data to database
      (save-zip-codes-to-database updated-records current-data)
      
      (println "✅ Successfully updated zip codes assignments")
      {:success true :message "Zip codes assignments updated successfully"})
    
    (catch Exception e
      (println "❌ Error updating workspace zip codes:" (.getMessage e))
      {:success false :message (.getMessage e)})))

(pco/defmutation update-workspace-zip-codes-mutation [env params]
  {::pco/op-name 'workspaces/update-workspace-zip-codes!}
  (update-workspace-zip-codes-fn env params))

(def resolvers [get-all-workspaces-res update-workspace-zip-codes-mutation])