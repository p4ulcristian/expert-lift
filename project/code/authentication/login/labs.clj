(ns authentication.login.labs
  (:require
   [authentication.config :as config]
   [authentication.helpers :as helpers]
   [authentication.utils :as utils]
   [zero.backend.state.env :as env]))

(def login-route "/login/labs")

(defn auth0-authentication-redirect-url []
  (str config/auth0-url
       "/authorize"
       "?response_type=code"
       "&client_id=" @env/auth0-labs-client-id
       "&redirect_uri=" (config/labs-authorize-url)
       "&scope=openid profile email"))

(defn login-handler
  "Login handler for labs that redirects to Auth0"
  [req]
  (let [global-role (utils/get-global-role req)] 
    (helpers/redirect-handler
     (auth0-authentication-redirect-url))))