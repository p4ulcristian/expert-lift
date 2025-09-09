(ns authentication.config
  (:require
   [zero.backend.state.env :as env]))

(def auth0-url              "https://dev-me4o6oy6ayzpw476.eu.auth0.com")
(def auth0-jwks-url         (str auth0-url "/.well-known/jwks.json"))
(def auth0-oauth-public-url (str auth0-url "/oauth/token"))
;; Auth0 client IDs for different frontend modules
(defn get-auth0-client-id 
  "Get the appropriate Auth0 client ID based on the frontend module"
  [module]
  (case module
    :customizer @env/auth0-customizer-client-id
    :flex       @env/auth0-flex-client-id
    :labs       @env/auth0-labs-client-id
    ;; Default fallback (backwards compatibility)
    @env/auth0-customizer-client-id))

(defn get-auth0-client-secret
  "Get the appropriate Auth0 client secret based on the frontend module"
  [module]
  (case module
    :customizer @env/auth0-customizer-client-secret
    :flex       @env/auth0-flex-client-secret
    :labs       @env/auth0-labs-client-secret
    ;; Default fallback (backwards compatibility)
    @env/auth0-customizer-client-secret))

;; User authentication
(defn authorize-url [] (str @env/domain "/authorize"))
(defn flex-authorize-url [] (str @env/domain "/authorize/flex"))
(defn customizer-authorize-url [] (str @env/domain "/authorize/customizer"))
(defn labs-authorize-url [] (str @env/domain "/authorize/labs"))
(def login-route           "/login")

;; Module-specific routes
(def flex-login-route      "/login/flex")
(def flex-authorize-route  "/authorize/flex")
(def flex-logout-route     "/logout/flex")

(def customizer-login-route      "/login/customizer")
(def customizer-authorize-route  "/authorize/customizer")
(def customizer-logout-route     "/logout/customizer")

(def labs-login-route      "/login/labs")
(def labs-authorize-route  "/authorize/labs")
(def labs-logout-route     "/logout/labs")


(def auth0-logout-url
  (str auth0-url "/v2/logout?federated"))

(defn flex-auth0-authentication-redirect-url []
  (str auth0-url
       "/authorize"
       "?response_type=code"
       "&client_id=" @env/auth0-flex-client-id
       "&redirect_uri=" (flex-authorize-url)
       "&scope=openid profile email"))

(defn customizer-auth0-authentication-redirect-url []
  (str auth0-url
       "/authorize"
       "?response_type=code"
       "&client_id=" @env/auth0-customizer-client-id
       "&redirect_uri=" (customizer-authorize-url)
       "&scope=openid profile email"))

(defn labs-auth0-authentication-redirect-url []
  (str auth0-url
       "/authorize"
       "?response_type=code"
       "&client_id=" @env/auth0-labs-client-id
       "&redirect_uri=" (labs-authorize-url)
       "&scope=openid profile email"))


