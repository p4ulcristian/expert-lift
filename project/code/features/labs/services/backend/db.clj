(ns features.labs.services.backend.db
  (:require
   [zero.backend.state.postgres :as postgres]))

(defn get-services
  "Get all services ordered by id"
  []
  (postgres/execute-sql
   "SELECT 
      s.id,
      s.name,
      s.description,
      s.picture_url
    FROM services s
    ORDER BY s.id"
   {:params []}))

(defn create-service
  "Insert a new service and return id"
  [{:keys [id name description picture_url]}]
  (postgres/execute-sql
   "INSERT INTO services (id, name, description, picture_url)
    VALUES ($1, $2, $3, $4)
    RETURNING id"
   {:params [id name description picture_url]}))

(defn update-service
  "Update an existing service and return id"
  [{:keys [id name description picture_url]}]
  (postgres/execute-sql
   "UPDATE services 
    SET name = $2,
        description = $3,
        picture_url = $4
    WHERE id = $1
    RETURNING id"
   {:params [id name description picture_url]}))

(defn delete-service
  "Delete a service by id and return id"
  [{:keys [id]}]
  (postgres/execute-sql
   "DELETE FROM services 
    WHERE id = $1
    RETURNING id"
   {:params [id]}))