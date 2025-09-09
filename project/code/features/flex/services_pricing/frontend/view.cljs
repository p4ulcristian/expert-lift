(ns features.flex.services-pricing.frontend.view
  (:require
   [features.flex.services-pricing.frontend.services :as services]
   [features.flex.shared.frontend.components.body :as body]
   [re-frame.core :as r]
   [router.frontend.zero :as router]
   [ui.button :as button]))

(defn view []
  (let [wsid @(r/subscribe [:workspace/get-id])]
    [body/view
     {:title "Services Pricing"
      :description "Manage services pricing. Active services are shown in the customizer."
      :body [services/view]}])) 