(ns authentication.logout.flex
  (:require
   [clojure.string :as str]
   [zero.backend.state.env :as env]))

(def logout-route "/logout/flex")

(defn validate-redirect-url
  "Validate redirect URL for security - only allow relative paths"
  [url]
  (when (and url 
             (str/starts-with? url "/")
             (not (str/starts-with? url "//")))
    url))

(defn logout-handler 
  "Handle logout with optional redirect parameter"
  [req]
  (let [redirect-param (get-in req [:params :redirect])
        valid-redirect (validate-redirect-url redirect-param)
        domain-value @env/domain
        return-url (if valid-redirect
                     (str domain-value valid-redirect)
                     (str domain-value "/flex"))
        client-id @env/auth0-flex-client-id
        _ (println "🔍 LOGOUT DEBUG - Domain value:" domain-value)
        _ (println "🔍 LOGOUT DEBUG - Redirect param:" redirect-param)
        _ (println "🔍 LOGOUT DEBUG - Valid redirect:" valid-redirect)
        _ (println "🔍 LOGOUT DEBUG - Return URL:" return-url)
        _ (println "🔍 LOGOUT DEBUG - Client ID:" client-id)
        ;; Add client_id parameter for Auth0
        auth0-logout-url (str "https://dev-me4o6oy6ayzpw476.eu.auth0.com/v2/logout?"
                              "returnTo=" (java.net.URLEncoder/encode return-url "UTF-8")
                              "&client_id=" client-id)
        _ (println "🔍 LOGOUT DEBUG - Full Auth0 logout URL:" auth0-logout-url)]
    {:status  302
     :session {}
     :headers {"Location" auth0-logout-url
               "Cache-Control" "no-cache, no-store, must-revalidate"
               "Pragma" "no-cache" 
               "Expires" "0"}
     :body    "Redirecting to logout..."}))