(ns features.app.worksheets.backend.db
  (:require [zero.backend.state.postgres :as postgres]
            [clojure.string :as str]))

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

(defn create-worksheet
  "Create new worksheet in workspace"
  [workspace-id serial-number creation-date work-type service-type work-description 
   material-usage notes status address-id elevator-id created-by-user-id assigned-to-user-id 
   arrival-time departure-time work-duration-hours]
  (postgres/execute-sql 
   "INSERT INTO expert_lift.worksheets 
    (serial_number, creation_date, work_type, service_type, work_description, 
     material_usage, notes, status, address_id, elevator_id, created_by_user_id, 
     assigned_to_user_id, arrival_time, departure_time, work_duration_hours) 
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15) 
    RETURNING *"
   {:params [serial-number creation-date work-type service-type work-description
             material-usage notes status address-id elevator-id created-by-user-id
             assigned-to-user-id arrival-time departure-time work-duration-hours]}))

(defn update-worksheet
  "Update existing worksheet (within workspace)"
  [worksheet-id workspace-id serial-number creation-date work-type service-type work-description 
   material-usage notes status address-id elevator-id assigned-to-user-id 
   arrival-time departure-time work-duration-hours]
  (postgres/execute-sql 
   "UPDATE expert_lift.worksheets w
    SET serial_number = $1, creation_date = $2, work_type = $3, service_type = $4, 
        work_description = $5, material_usage = $6, notes = $7, status = $8, 
        address_id = $9, elevator_id = $10, assigned_to_user_id = $11, 
        arrival_time = $12, departure_time = $13, work_duration_hours = $14, 
        updated_at = NOW()
    FROM expert_lift.addresses a
    WHERE w.id = $15 AND w.address_id = a.id AND a.workspace_id = $16
    RETURNING w.*"
   {:params [serial-number creation-date work-type service-type work-description
             material-usage notes status address-id elevator-id assigned-to-user-id
             arrival-time departure-time work-duration-hours worksheet-id workspace-id]}))

(defn delete-worksheet
  "Delete worksheet (within workspace)"
  [worksheet-id workspace-id]
  (postgres/execute-sql 
   "DELETE FROM expert_lift.worksheets w
    USING expert_lift.addresses a
    WHERE w.id = $1 AND w.address_id = a.id AND a.workspace_id = $2
    RETURNING w.id"
   {:params [worksheet-id workspace-id]}))