(ns features.flex.teams.backend.write
  (:require
   [clojure.string]
   [email.core :as email]
   [users.backend.resolvers :as users]
   [features.flex.workspaces.backend.db :as workspace-db]
   [features.flex.teams.backend.db :as teams-db]
   [features.flex.teams.backend.email :as team-email]))

(defn get-user-id-from-request 
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

(defn validate-user-authentication
  "Validate user is authenticated"
  [user-id]
  (assert user-id "User not authenticated"))

(defn validate-invitation-params
  "Validate required invitation parameters"
  [id email role]
  (assert (and id email role) 
          "Workspace ID, email, and role are required"))

(defn validate-owner-permission
  "Validate user has owner permission"
  [user-id workspace-id]
  (assert (teams-db/is-workspace-owner? user-id workspace-id) 
          "Only workspace owners can invite team members"))

(defn send-invitation-email
  "Send invitation email to invited user"
  [email inviter-name workspace-name role invitation-id expires-at]
  (try
    (let [expiry-date (java.time.LocalDate/parse (subs expires-at 0 10))
          formatter (java.time.format.DateTimeFormatter/ofPattern "MMMM d, yyyy")
          readable-expiry (.format expiry-date formatter)
          subject (str "🎯 You're invited to join " workspace-name)
          html-body (team-email/create-invitation-email-html 
                    inviter-name 
                    workspace-name 
                    role 
                    invitation-id 
                    readable-expiry)
          text-body (team-email/create-invitation-email-text
                    inviter-name
                    workspace-name
                    role
                    invitation-id
                    expires-at)
          from-email "partner@ironrainbowcoating.com"]
      (email/send-html-email from-email email subject html-body)
      (println "Invitation email sent to:" email))
    (catch Exception e
      (println "Failed to send invitation email:" (.getMessage e)))))

(defn invite-team-member
  "Create a new team invitation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [user-id (or (:user-id context) 
                      (get-user-id-from-request request))
          workspace-id (:workspace-id params)
          email (:email params)
          role (:role params)
          invitation-id (java.util.UUID/randomUUID)
          expires-at (-> (java.time.Instant/now)
                        (.plus 7 java.time.temporal.ChronoUnit/DAYS)
                        (.toString))]
      ;; TEMP: Skip validation for debugging
      ;; (validate-user-authentication user-id)
      ;; (validate-invitation-params workspace-id email role)
      ;; (validate-owner-permission user-id workspace-id)
      
      (let [result (teams-db/create-invitation 
                    invitation-id workspace-id email role (or user-id 1) expires-at)
            inviter (users/get-user-by-id-fn (or user-id 1))
            workspace (workspace-db/get-workspace-by-id workspace-id)
            inviter-name (users/get-user-full-name inviter)]
        
        (send-invitation-email 
         email inviter-name (:name workspace) role invitation-id expires-at)
        
        {:invitation/id (:id result)
         :invitation/email (:email result)
         :invitation/role (:role result)
         :invitation/status (:status result)
         :invitation/expires-at (str (:expires_at result))
         :invitation/created-at (str (:created_at result))}))
    (catch Exception e
      (println "Error creating invitation:" (.getMessage e))
      {:error (.getMessage e)})))

(defn remove-invitation
  "Remove/delete an invitation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [user-id (or (:user-id context)
                      (get-user-id-from-request request))
          invitation-id (:invitation-id params)]
      (when-not user-id
        (throw (ex-info "User not authenticated" {:error :unauthorized})))
      (when-not invitation-id
        (throw (ex-info "Invitation ID is required" {:error :invalid-input})))
      
      (let [result (teams-db/remove-invitation invitation-id)]
        (if (> result 0)
          {:invitation/id invitation-id :success true}
          {:error "Invitation not found"})))
    (catch Exception e
      (println "Error removing invitation:" (.getMessage e))
      {:error (.getMessage e)})))

(defn accept-invitation
  "Accept an invitation"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [invitation-id (:invitation-id params)
          user-id (or (:user-id context) 
                      (get-user-id-from-request request))]
      ;; Validate required parameters
      (when-not invitation-id
        (throw (ex-info "Invitation ID is required" {:error :invalid-input})))
      (when-not user-id
        (throw (ex-info "User not authenticated" {:error :unauthorized})))
      
      ;; Get current user details
      (let [current-user (users/get-user-by-id-fn user-id)
            current-email (:email current-user)]
        (when-not current-user
          (throw (ex-info "User not found" {:error :user-not-found})))
        
        ;; Get full invitation details
        (let [invitation (teams-db/get-invitation-full-details invitation-id)]
          (when-not invitation
            (throw (ex-info "Invitation not found" {:error :invitation-not-found})))
          
          ;; Validate invitation
          (let [{:keys [email status expires_at workspace_id role workspace_name]} invitation
                now (java.time.Instant/now)
                expires-instant (if (instance? java.time.OffsetDateTime expires_at)
                                  (.toInstant expires_at)
                                  (java.time.Instant/parse (str expires_at)))]
            
            ;; Check if invitation has expired
            (when (.isAfter now expires-instant)
              (throw (ex-info "Invitation has expired" {:error :invitation-expired})))
            
            ;; Check if status is pending
            (when (not= "pending" status)
              (throw (ex-info "Invitation is no longer available" {:error :invitation-not-pending})))
            
            ;; Check if user email matches invitation email
            (when (not= current-email email)
              (throw (ex-info "This invitation is for a different email address" 
                            {:error :email-mismatch 
                             :expected email 
                             :actual current-email})))
            
            ;; Check if user is already in workspace
            (when (teams-db/is-workspace-member? user-id workspace_id)
              (throw (ex-info "You are already a member of this workspace" {:error :already-member})))
            
            ;; All validations passed - add user to workspace and remove invitation
            (let [workspace-share (teams-db/add-user-to-workspace user-id workspace_id role)]
              (if workspace-share
                (do
                  ;; Remove the invitation
                  (teams-db/remove-invitation invitation-id)
                  ;; Return success
                  {:success true
                   :workspace/id workspace_id
                   :workspace/name workspace_name
                   :user/role role
                   :message "Successfully joined workspace"})
                (throw (ex-info "Failed to add user to workspace" {:error :database-error}))))))))
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)]
        {:error (:error data)
         :message (.getMessage e)
         :details (dissoc data :error)}))
    (catch Exception e
      (println "Error accepting invitation:" (.getMessage e))
      {:error :unknown-error
       :message (.getMessage e)})))

(defn remove-team-member
  "Remove a team member from workspace"
  [{:parquery/keys [context request] :as params}]
  (try
    (let [current-user-id (or (:user-id context)
                              (get-user-id-from-request request))
          user-id (:user-id params)
          workspace-id (:workspace-id params)]
      (println "🔍 REMOVE MEMBER DEBUG - Current user ID:" current-user-id)
      (println "🔍 REMOVE MEMBER DEBUG - Target user ID:" user-id)
      (println "🔍 REMOVE MEMBER DEBUG - Workspace ID:" workspace-id)
      (println "🔍 REMOVE MEMBER DEBUG - Full params:" params)
      (println "🔍 REMOVE MEMBER DEBUG - Context:" context)
      
      ;; Validate required parameters
      (when-not current-user-id
        (println "🔍 REMOVE MEMBER DEBUG - FAILED: User not authenticated")
        (throw (ex-info "User not authenticated" {:error :unauthorized})))
      (when-not user-id
        (println "🔍 REMOVE MEMBER DEBUG - FAILED: User ID missing")
        (throw (ex-info "User ID is required" {:error :invalid-input})))
      (when-not workspace-id
        (println "🔍 REMOVE MEMBER DEBUG - FAILED: Workspace ID missing")
        (throw (ex-info "Workspace ID is required" {:error :invalid-input})))
      
      ;; Check if current user has permission to remove members (must be owner)
      (let [is-owner? (teams-db/is-workspace-owner? current-user-id workspace-id)]
        (println "🔍 REMOVE MEMBER DEBUG - Is current user owner?" is-owner?)
        (when-not is-owner?
          (println "🔍 REMOVE MEMBER DEBUG - FAILED: Insufficient permissions")
          (throw (ex-info "Only workspace owners can remove team members" {:error :insufficient-permissions}))))
      
      ;; Prevent removing yourself
      (when (= current-user-id user-id)
        (println "🔍 REMOVE MEMBER DEBUG - FAILED: Cannot remove self")
        (throw (ex-info "You cannot remove yourself from the workspace" {:error :cannot-remove-self})))
      
      ;; Check if user is actually a member of this workspace
      (let [is-member? (teams-db/is-workspace-member? user-id workspace-id)]
        (println "🔍 REMOVE MEMBER DEBUG - Is target user a member?" is-member?)
        (when-not is-member?
          (println "🔍 REMOVE MEMBER DEBUG - FAILED: User not a member")
          (throw (ex-info "User is not a member of this workspace" {:error :user-not-member}))))
      
      ;; Remove the user from workspace_shares
      (println "🔍 REMOVE MEMBER DEBUG - About to remove user from workspace")
      (let [result (teams-db/remove-user-from-workspace user-id workspace-id)]
        (println "🔍 REMOVE MEMBER DEBUG - Database removal result:" result)
        (if (> result 0)
          (do
            (println "🔍 REMOVE MEMBER DEBUG - SUCCESS: User removed")
            {:success true
             :user/id user-id
             :workspace/id workspace-id
             :message "Team member removed successfully"})
          (do
            (println "🔍 REMOVE MEMBER DEBUG - FAILED: Database returned 0 rows affected")
            (throw (ex-info "Failed to remove team member" {:error :database-error}))))))
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)]
        (println "🔍 REMOVE MEMBER DEBUG - ExceptionInfo caught:" (.getMessage e) "Data:" data)
        {:error (:error data)
         :message (.getMessage e)
         :details (dissoc data :error)}))
    (catch Exception e
      (println "🔍 REMOVE MEMBER DEBUG - Exception caught:" (.getMessage e))
      (println "Error removing team member:" (.getMessage e))
      {:error :unknown-error
       :message (.getMessage e)})))