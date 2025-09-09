(ns features.flex.shared.frontend.fetch.user
  (:require
   ["react" :as react]
   [re-frame.core :as rf]
   [features.flex.shared.frontend.request :as shared-request]
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