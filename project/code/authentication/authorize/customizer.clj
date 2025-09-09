(ns authentication.authorize.customizer
  (:require
   [authentication.auth0.zero :as auth0]
   [authentication.authorize-flows.customizer :as customizer-flow]
   [authentication.config :as config]))

(def authorize-route "/authorize/customizer")

(defn authorize-handler 
  "Handle OAuth authorization callback from Auth0 for Customizer"
  [req]
  (let [session      (:session req)
        redirect-url (:redirect-url session)
        code         (-> req :params :code)]
    (if code
      (let [token (auth0/get-validated-token code (config/customizer-authorize-url) :module :customizer)] 
        (customizer-flow/handle-customizer-authorization token redirect-url))
      ;; No code parameter - redirect to login
      {:status 302
       :headers {"Location" "/login/customizer"}
       :body "Redirecting to login..."})))