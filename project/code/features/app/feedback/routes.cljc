(ns features.app.feedback.routes
  #?(:cljs (:require [features.app.feedback.frontend.view :as feedback]))
  #?(:clj  (:require [features.app.zero.backend.view :as backend-view]
                     [router.backend.middleware :refer [wrap-require-authentication]])))

(def feedback-path "/app/:workspace-id/feedback")

(def routes
  #?(:cljs [{:path feedback-path
             :view #'feedback/view
             :title "Feedback"}]
     :clj  [{:path feedback-path
             :get #'backend-view/response
             :middleware [wrap-require-authentication]}]))
