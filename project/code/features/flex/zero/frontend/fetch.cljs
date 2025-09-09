(ns features.flex.zero.frontend.fetch
  (:require
   ["react" :as react]
   [re-frame.core :as rf]
   [features.flex.shared.frontend.request :as shared-request]
   [parquery.frontend.request :as parquery]
   [zero.frontend.react :as zero-react]))

(defn use-user-data-effect
  "Fetches current user data and stores in re-frame"
  []
  (zero-react/use-effect
   {:mount (fn []
             (shared-request/get-current-user
              (fn [response]
                (let [user-data (:current-user/basic-data response)]
                  (rf/dispatch [:user/set user-data])))))
    :params []}))

(defn use-workspace-data-effect
  "Fetches workspace data when workspace-id is available and retriggers on change"
  [workspace-id]
  (zero-react/use-effect
   {:mount (fn []
             (when workspace-id
               ;; Immediately set the workspace ID so other effects can use it
               (rf/dispatch [:workspace/set {:workspace/id workspace-id}])
               ;; Then fetch the full workspace data
               (parquery/send-queries
                {:queries {:workspace/get {:workspace-id workspace-id}}
                 :parquery/context {:workspace-id workspace-id}
                 :callback (fn [response]
                             (let [workspace-data (:workspace/get response)]
                               (rf/dispatch [:workspace/set workspace-data])))})))
    :params #js[workspace-id]}))