(ns authentication.authorize.flex
  (:require
   [authentication.auth0.zero :as auth0]
   [authentication.authorize-flows.flex :as flex-flow]
   [authentication.config :as config]))

(def authorize-route "/authorize/flex")

(defn authorize-handler 
  "Handle OAuth authorization callback from Auth0 for Flex"
  [req]
  (let [session      (:session req)
        redirect-url (:redirect-url session)
        code         (-> req :params :code)]
    (if code
      (let [token (auth0/get-validated-token code (config/flex-authorize-url) :module :flex)] 
        (flex-flow/handle-flex-authorization token redirect-url))
      ;; No code parameter - redirect to login
      {:status 302
       :headers {"Location" "/login/flex"}
       :body "Redirecting to login..."})))