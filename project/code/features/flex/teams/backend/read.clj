(ns features.flex.teams.backend.read
  (:require
   [features.flex.teams.backend.db :as teams-db]))

(defn timestamp-to-string 
  "Convert timestamp to ISO string"
  [timestamp]
  (when timestamp
    (str timestamp)))

(defn get-workspace-users
  "Get users for a workspace"
  [{:parquery/keys [context request] :as params}]
  (let [workspace-id (:workspace-id params)
        current-user-id (or (:user-id context)
                            (get-in request [:session :user-id]))
        users (teams-db/get-workspace-users workspace-id)]
    (mapv (fn [user]
            (let [is-current-user? (= (:user_id user) current-user-id)]
              {:user/id (:user_id user)
               :user/first-name (:first_name user)
               :user/last-name (:last_name user)
               :user/email (:email user)
               :user/role (:role user)
               :user/joined-at (timestamp-to-string (:created_at user))
               :user/display-name (str (:first_name user) " " (:last_name user))
               :user/current? is-current-user?}))
          users)))

(defn get-workspace-invitations
  "Get pending invitations for a workspace"
  [{:parquery/keys [context] :as params}]
  (let [workspace-id (:workspace-id params)
        invitations (teams-db/get-workspace-invitations workspace-id)]
    (mapv (fn [invitation]
            {:invitation/id (:id invitation)
             :invitation/email (:email invitation)
             :invitation/role (:role invitation)
             :invitation/status (:status invitation)
             :invitation/expires-at (timestamp-to-string (:expires_at invitation))
             :invitation/created-at (timestamp-to-string (:created_at invitation))
             :invitation/inviter-name (str (:first_name invitation) " " (:last_name invitation))
             :invitation/inviter-email (:inviter_email invitation)})
          invitations)))

(defn get-invitation-details
  "Get details for a specific invitation"
  [{:parquery/keys [context] :as params}]
  (let [invitation-id (:invitation-id params)]
    (try
      (let [details (teams-db/get-invitation-details invitation-id)]
        (if details
          {:workspace/name (:workspace_name details)
           :inviter/email (:inviter_email details)
           :invitee/email (:invitee_email details)}
          {:workspace/name nil
           :inviter/email nil
           :invitee/email nil}))
      (catch Exception e
        (println "Error in get-invitation-details:" (.getMessage e))
        {:workspace/name nil
         :inviter/email nil
         :invitee/email nil}))))