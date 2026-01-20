(ns features.app.feedback.backend.db
  "Feedback backend database operations"
  (:require [zero.backend.state.postgres :as postgres]))

(defn get-all-feedbacks
  "Get all feedbacks with user and workspace info, ordered by newest first"
  []
  (postgres/execute-sql
   "SELECT f.id, f.message, f.created_at,
           u.id as user_id, u.full_name as user_full_name, u.email as user_email,
           w.id as workspace_id, w.name as workspace_name
    FROM expert_lift.feedbacks f
    JOIN expert_lift.users u ON f.user_id = u.id
    JOIN expert_lift.workspaces w ON f.workspace_id = w.id
    ORDER BY f.created_at DESC"
   {:params []}))

(defn get-user-feedbacks
  "Get feedbacks for a specific user, ordered by newest first"
  [user-id]
  (postgres/execute-sql
   "SELECT id, message, created_at
    FROM expert_lift.feedbacks
    WHERE user_id = $1
    ORDER BY created_at DESC"
   {:params [(java.util.UUID/fromString user-id)]}))

(defn create-feedback
  "Create new feedback"
  [message user-id workspace-id]
  (postgres/execute-sql
   "INSERT INTO expert_lift.feedbacks (message, user_id, workspace_id)
    VALUES ($1, $2, $3)
    RETURNING id, message, user_id, workspace_id, created_at"
   {:params [message (java.util.UUID/fromString user-id) (java.util.UUID/fromString workspace-id)]}))

(defn update-feedback
  "Update feedback message (only if user owns it)"
  [feedback-id user-id message]
  (postgres/execute-sql
   "UPDATE expert_lift.feedbacks
    SET message = $1
    WHERE id = $2 AND user_id = $3
    RETURNING id, message, created_at"
   {:params [message (java.util.UUID/fromString feedback-id) (java.util.UUID/fromString user-id)]}))

(defn delete-feedback
  "Delete feedback (only if user owns it)"
  [feedback-id user-id]
  (postgres/execute-sql
   "DELETE FROM expert_lift.feedbacks
    WHERE id = $1 AND user_id = $2
    RETURNING id"
   {:params [(java.util.UUID/fromString feedback-id) (java.util.UUID/fromString user-id)]}))
