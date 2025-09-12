(ns features.app.addresses.backend.db
  (:require [zero.backend.state.postgres :as postgres]
            [clojure.string :as str]))

(defn get-addresses-by-workspace
  "Get all addresses for a workspace"
  [workspace-id]
  (postgres/execute-sql 
   "SELECT *, elevators::text as elevators_json FROM expert_lift.addresses 
    WHERE workspace_id = $1 
    ORDER BY name"
   {:params [workspace-id]}))

(defn get-addresses-paginated
  "Get addresses with server-side filtering, sorting, and pagination"
  [workspace-id {:keys [search sort-by sort-direction page page-size]
                 :or {sort-by "name" sort-direction "asc" page 0 page-size 10}}]
  (let [offset (* page page-size)
        has-search? (and search (not (str/blank? search)))
        search-condition (if has-search?
                          "AND (LOWER(name) LIKE $2 OR LOWER(city) LIKE $2 OR LOWER(country) LIKE $2 OR LOWER(contact_person) LIKE $2)"
                          "")
        search-param (when has-search? (str "%" (str/lower-case search) "%"))
        order-direction (if (= sort-direction "desc") "DESC" "ASC")
        
        ;; Map frontend column names to database columns
        db-column (case sort-by
                    "address/name" "name"
                    "address/city" "city" 
                    "address/country" "country"
                    "address/contact-person" "contact_person"
                    "name")
        
        ;; Build the query parameters correctly
        params (if has-search? 
                [workspace-id search-param page-size offset]
                [workspace-id page-size offset])
        
        query (str "SELECT *, elevators::text as elevators_json FROM expert_lift.addresses 
                   WHERE workspace_id = $1 " 
                   search-condition
                   " ORDER BY " db-column " " order-direction
                   " LIMIT $" (if has-search? "3" "2")
                   " OFFSET $" (if has-search? "4" "3"))
        
        count-query (str "SELECT COUNT(*) as total FROM expert_lift.addresses 
                         WHERE workspace_id = $1 " 
                         search-condition)
        count-params (if has-search? [workspace-id search-param] [workspace-id])]
    
    (println "DEBUG get-addresses-paginated:")
    (println "  Query:" query)
    (println "  Params:" params)
    (println "  Count query:" count-query)
    (println "  Count params:" count-params)
    
    (let [addresses (postgres/execute-sql query {:params params})
          total-count (:total (first (postgres/execute-sql count-query {:params count-params})))]
      
      (println "DEBUG Results:")
      (println "  Found" (count addresses) "addresses")
      (println "  Total count:" total-count)
      
      {:addresses addresses
       :total-count total-count
       :page page
       :page-size page-size
       :total-pages (Math/ceil (/ total-count page-size))})))

(defn get-address-by-id
  "Get address by ID (within workspace)"
  [address-id workspace-id]
  (postgres/execute-sql 
   "SELECT *, elevators::text as elevators_json FROM expert_lift.addresses 
    WHERE id = $1 AND workspace_id = $2"
   {:params [address-id workspace-id]}))

(defn create-address
  "Create new address in workspace"
  [workspace-id name address-line1 address-line2 city postal-code country contact-person contact-phone contact-email elevators-json]
  (postgres/execute-sql 
   "INSERT INTO expert_lift.addresses (workspace_id, name, address_line1, address_line2, city, postal_code, country, contact_person, contact_phone, contact_email, elevators) 
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11::jsonb) 
    RETURNING *, elevators::text as elevators_json"
   {:params [workspace-id name address-line1 address-line2 city postal-code country contact-person contact-phone contact-email elevators-json]}))

(defn update-address
  "Update existing address (within workspace)"
  [address-id workspace-id name address-line1 address-line2 city postal-code country contact-person contact-phone contact-email elevators-json]
  (postgres/execute-sql 
   "UPDATE expert_lift.addresses 
    SET name = $1, address_line1 = $2, address_line2 = $3, city = $4, postal_code = $5, country = $6, contact_person = $7, contact_phone = $8, contact_email = $9, elevators = $10::jsonb, updated_at = NOW()
    WHERE id = $11 AND workspace_id = $12
    RETURNING *, elevators::text as elevators_json"
   {:params [name address-line1 address-line2 city postal-code country contact-person contact-phone contact-email elevators-json address-id workspace-id]}))

(defn delete-address
  "Delete address (within workspace)"
  [address-id workspace-id]
  (postgres/execute-sql 
   "DELETE FROM expert_lift.addresses 
    WHERE id = $1 AND workspace_id = $2
    RETURNING id"
   {:params [address-id workspace-id]}))