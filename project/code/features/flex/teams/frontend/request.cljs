(ns features.flex.teams.frontend.request
  (:require [parquery.frontend.request :as parquery]))

(defn get-workspace-users
  "Get users for a workspace using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:workspace/users {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [users (:workspace/users response)]
                  (callback users)))}))

(defn get-workspace-invitations
  "Get invitations for a workspace using ParQuery"
  [workspace-id callback]
  (parquery/send-queries
   {:queries {:workspace/invitations {:workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [invitations (:workspace/invitations response)]
                  (callback invitations)))}))

(defn invite-team-member
  "Invite a team member using ParQuery"
  [workspace-id email role callback]
  (parquery/send-queries
   {:queries {:teams/invite-member {:workspace-id workspace-id
                                     :email email
                                     :role role}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:teams/invite-member response)]
                  (if (:error result)
                    (callback {:error (or (:error result) "Failed to invite member")})
                    (callback result))))}))

(defn remove-invitation
  "Remove an invitation using ParQuery"
  [invitation-id callback]
  (parquery/send-queries
   {:queries {:teams/remove-invitation {:invitation-id invitation-id}}
    :parquery/context {}
    :callback (fn [response]
                (let [result (:teams/remove-invitation response)]
                  (if (:error result)
                    (callback {:error (or (:error result) "Failed to remove invitation")})
                    (callback result))))}))

(defn remove-team-member
  "Remove a team member from workspace using ParQuery"
  [user-id workspace-id callback]
  (parquery/send-queries
   {:queries {:teams/remove-member {:user-id user-id :workspace-id workspace-id}}
    :parquery/context {:workspace-id workspace-id}
    :callback (fn [response]
                (let [result (:teams/remove-member response)]
                  (if (:error result)
                    (callback {:error (or (:error result) "Failed to remove team member")})
                    (callback result))))}))