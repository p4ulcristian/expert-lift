(ns authentication.authorize.labs
  (:require
   [authentication.auth0.zero :as auth0]
   [authentication.authorize-flows.labs :as labs-flow]
   [authentication.config :as config]))

(def authorize-route "/authorize/labs")

(defn authorize-handler 
  "Handle OAuth authorization callback from Auth0 for Labs"
  [req]
  (let [session      (:session req)
        redirect-url (:redirect-url session)
        code         (-> req :params :code)]
    (if code
      (let [token (auth0/get-validated-token code (config/labs-authorize-url) :module :labs)] 
        (labs-flow/handle-labs-authorization token redirect-url))
      ;; No code parameter - redirect to login
      {:status 302
       :headers {"Location" "/login/labs"}
       :body "Redirecting to login..."})))