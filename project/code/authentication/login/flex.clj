(ns authentication.login.flex
  (:require
   [authentication.config :as config]
   [authentication.helpers :as helpers]
   [authentication.utils :as utils]
   [clojure.string :as str]
   [schemas.session :as schema]
   [zero.backend.state.env :as env]))

(def login-route "/login/flex")

(defn validate-redirect-url
  "Validate redirect URL for security - only allow relative paths"
  [url]
  (when (and url 
             (str/starts-with? url "/")
             (not (str/starts-with? url "//")))
    url))

(defn auth0-authentication-redirect-url []
  (str config/auth0-url
       "/authorize"
       "?response_type=code"
       "&client_id=" @env/auth0-flex-client-id
       "&redirect_uri=" (config/flex-authorize-url)
       "&scope=openid profile email"))

(defn login-handler
  "Flex login handler that redirects to Auth0 with optional redirect parameter"
  [req]
  (let [redirect-param (get-in req [:params :redirect])
        valid-redirect (validate-redirect-url redirect-param)
        updated-session (if valid-redirect
                          (schema/validate-session
                           (assoc (:session req) :redirect-url valid-redirect))
                          (:session req))]
    (assoc 
     (helpers/redirect-handler (auth0-authentication-redirect-url))
     :session updated-session)))