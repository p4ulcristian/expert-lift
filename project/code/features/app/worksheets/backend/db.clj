(ns features.app.worksheets.backend.db
  (:require [zero.backend.state.postgres :as postgres]
            [clojure.string :as str]
            [clojure.instant :as instant]))

(defn- normalize-hungarian
  "Normalize Hungarian characters for search - convert accented chars to base chars"
  [text]
  (when text
    (-> text
        (str/replace #"[áÁ]" "a")
        (str/replace #"[éÉ]" "e") 
        (str/replace #"[íÍ]" "i")
        (str/replace #"[óÓöÖőŐ]" "o")
        (str/replace #"[úÚüÜűŰ]" "u")
        (str/lower-case))))

(defn- calculate-work-duration
  "Calculate work duration in hours from arrival and departure times, rounded up to nearest quarter hour"
  [arrival-time departure-time]
  (when (and arrival-time departure-time)
    (try
      (let [arrival (if (string? arrival-time) (instant/read-instant-date arrival-time) arrival-time)
            departure (if (string? departure-time) (instant/read-instant-date departure-time) departure-time)]
        (when (pos? (compare departure arrival))
          (let [diff-ms (- (.getTime departure) (.getTime arrival))
                diff-hours (/ diff-ms 1000.0 60 60)]
            ;; Round up to nearest quarter hour
            (/ (Math/ceil (* diff-hours 4)) 4))))
      (catch Exception e
        (println "Error calculating duration:" (.getMessage e))
        nil))))

(defn get-worksheets-by-workspace
  "Get all worksheets for a workspace (via address relationship)"
  [workspace-id]
  (postgres/execute-sql 
   "SELECT w.*, 
           a.name as address_name,
           a.city as address_city,
           cu.full_name as created_by_name,
           au.full_name as assigned_to_name
    FROM expert_lift.worksheets w
    JOIN expert_lift.addresses a ON w.address_id = a.id  
    LEFT JOIN expert_lift.users cu ON w.created_by_user_id = cu.id
    LEFT JOIN expert_lift.users au ON w.assigned_to_user_id = au.id
    WHERE a.workspace_id = $1 
    ORDER BY w.creation_date DESC, w.serial_number DESC"
   {:params [workspace-id]}))

(defn get-worksheets-paginated
  "Get worksheets with server-side filtering, sorting, and pagination"
  [workspace-id {:keys [search sort-by sort-direction page page-size]
                 :or {sort-by "creation_date" sort-direction "desc" page 0 page-size 10}}]
  (let [offset (* page page-size)
        has-search? (and search (not (str/blank? search)))
        search-condition (if has-search?
                          "AND (LOWER(w.serial_number) LIKE $2 OR LOWER(w.work_description) LIKE $2 OR LOWER(w.notes) LIKE $2 OR LOWER(a.name) LIKE $2 OR LOWER(w.work_type::text) LIKE $2 OR LOWER(w.status::text) LIKE $2 OR translate(LOWER(w.serial_number || ' ' || w.work_description || ' ' || COALESCE(w.notes, '') || ' ' || a.name || ' ' || w.work_type::text || ' ' || w.status::text), 'áéíóöőúüű', 'aeiooouuu') LIKE $2)"
                          "")
        search-param (when has-search? (str "%" (normalize-hungarian search) "%"))
        order-direction (if (= sort-direction "desc") "DESC" "ASC")
        
        ;; Map frontend column names to database columns
        db-column (case sort-by
                    "worksheet/serial-number" "w.serial_number"
                    "worksheet/creation-date" "w.creation_date" 
                    "worksheet/work-type" "w.work_type"
                    "worksheet/status" "w.status"
                    "worksheet/address-name" "a.name"
                    "worksheet/assigned-to" "au.full_name"
                    "w.creation_date")
        
        ;; Build the query parameters correctly
        params (if has-search? 
                [workspace-id search-param page-size offset]
                [workspace-id page-size offset])
        
        query (str "SELECT w.*, 
                           a.name as address_name,
                           a.city as address_city,
                           cu.full_name as created_by_name,
                           au.full_name as assigned_to_name
                    FROM expert_lift.worksheets w
                    JOIN expert_lift.addresses a ON w.address_id = a.id  
                    LEFT JOIN expert_lift.users cu ON w.created_by_user_id = cu.id
                    LEFT JOIN expert_lift.users au ON w.assigned_to_user_id = au.id
                    WHERE a.workspace_id = $1 " 
                   search-condition
                   " ORDER BY " db-column " " order-direction
                   " LIMIT $" (if has-search? "3" "2")
                   " OFFSET $" (if has-search? "4" "3"))
        
        count-query (str "SELECT COUNT(*) as total 
                         FROM expert_lift.worksheets w
                         JOIN expert_lift.addresses a ON w.address_id = a.id  
                         LEFT JOIN expert_lift.users cu ON w.created_by_user_id = cu.id
                         LEFT JOIN expert_lift.users au ON w.assigned_to_user_id = au.id
                         WHERE a.workspace_id = $1 " 
                         search-condition)
        count-params (if has-search? [workspace-id search-param] [workspace-id])]
    
    (println "DEBUG get-worksheets-paginated:")
    (println "  Query:" query)
    (println "  Params:" params)
    (println "  Count query:" count-query)
    (println "  Count params:" count-params)
    
    (let [worksheets (postgres/execute-sql query {:params params})
          total-count (:total (first (postgres/execute-sql count-query {:params count-params})))]
      
      (println "DEBUG Results:")
      (println "  Found" (count worksheets) "worksheets")
      (println "  Total count:" total-count)
      
      {:worksheets worksheets
       :total-count total-count
       :page page
       :page-size page-size
       :total-pages (Math/ceil (/ total-count page-size))})))

(defn get-worksheet-by-id
  "Get worksheet by ID (within workspace)"
  [worksheet-id workspace-id]
  (postgres/execute-sql 
   "SELECT w.*, 
           a.name as address_name,
           a.city as address_city,
           cu.full_name as created_by_name,
           au.full_name as assigned_to_name
    FROM expert_lift.worksheets w
    JOIN expert_lift.addresses a ON w.address_id = a.id  
    LEFT JOIN expert_lift.users cu ON w.created_by_user_id = cu.id
    LEFT JOIN expert_lift.users au ON w.assigned_to_user_id = au.id
    WHERE w.id = $1 AND a.workspace_id = $2"
   {:params [worksheet-id workspace-id]}))

(defn- generate-next-serial-number
  "Generate next serial number in format YYYY-MM-DD/NNN for workspace"
  [workspace-id creation-date]
  (let [date-str (cond
                   (string? creation-date) 
                   (if (.contains creation-date "T")
                     ;; If it's an ISO datetime, extract just the date part
                     (first (str/split creation-date #"T"))
                     ;; If it's already just a date
                     creation-date)
                   
                   (instance? java.time.LocalDate creation-date)
                   (.format creation-date (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd"))
                   
                   :else
                   (.format (java.time.LocalDate/now) (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd")))
        count-query "SELECT COUNT(*) as count 
                    FROM expert_lift.worksheets w
                    JOIN expert_lift.addresses a ON w.address_id = a.id  
                    WHERE a.workspace_id = $1 
                      AND DATE(w.creation_date) = DATE($2)"
        count-result (postgres/execute-sql count-query {:params [workspace-id date-str]})
        next-number (inc (:count (first count-result)))]
    (str date-str "/" (format "%03d" next-number))))

(defn create-worksheet
  "Create new worksheet in workspace with auto-generated serial number"
  [workspace-id creation-date work-type service-type work-description 
   material-usage notes status address-id elevator-id created-by-user-id assigned-to-user-id 
   arrival-time departure-time work-duration-hours maintainer-signature customer-signature]
  (let [auto-serial-number (generate-next-serial-number workspace-id creation-date)
        calculated-duration (or (calculate-work-duration arrival-time departure-time) work-duration-hours)]
    (postgres/execute-sql 
     "INSERT INTO expert_lift.worksheets 
      (serial_number, creation_date, work_type, service_type, work_description, 
       material_usage, notes, status, address_id, elevator_id, created_by_user_id, 
       assigned_to_user_id, arrival_time, departure_time, work_duration_hours, 
       maintainer_signature, customer_signature) 
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17) 
      RETURNING *"
     {:params [auto-serial-number creation-date work-type service-type work-description
               material-usage notes status address-id elevator-id created-by-user-id
               assigned-to-user-id arrival-time departure-time calculated-duration
               maintainer-signature customer-signature]})))

(defn update-worksheet
  "Update existing worksheet (within workspace)"
  [worksheet-id workspace-id serial-number creation-date work-type service-type work-description 
   material-usage notes status address-id elevator-id assigned-to-user-id 
   arrival-time departure-time work-duration-hours maintainer-signature customer-signature]
  (let [calculated-duration (or (calculate-work-duration arrival-time departure-time) work-duration-hours)]
    (postgres/execute-sql 
     "UPDATE expert_lift.worksheets w
      SET serial_number = $1, creation_date = $2, work_type = $3, service_type = $4, 
          work_description = $5, material_usage = $6, notes = $7, status = $8, 
          address_id = $9, elevator_id = $10, assigned_to_user_id = $11, 
          arrival_time = $12, departure_time = $13, work_duration_hours = $14, 
          maintainer_signature = $15, customer_signature = $16, updated_at = NOW()
      FROM expert_lift.addresses a
      WHERE w.id = $17 AND w.address_id = a.id AND a.workspace_id = $18
      RETURNING w.*"
     {:params [serial-number creation-date work-type service-type work-description
               material-usage notes status address-id elevator-id assigned-to-user-id
               arrival-time departure-time calculated-duration maintainer-signature 
               customer-signature worksheet-id workspace-id]})))

(defn delete-worksheet
  "Delete worksheet (within workspace)"
  [worksheet-id workspace-id]
  (postgres/execute-sql 
   "DELETE FROM expert_lift.worksheets w
    USING expert_lift.addresses a
    WHERE w.id = $1 AND w.address_id = a.id AND a.workspace_id = $2
    RETURNING w.id"
   {:params [worksheet-id workspace-id]}))