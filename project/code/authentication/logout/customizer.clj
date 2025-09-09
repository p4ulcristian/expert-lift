(ns authentication.logout.customizer
  (:require
   [zero.backend.state.env :as env]))

(def logout-route "/logout/customizer")

(defn logout-handler 
  "Handle customizer logout by clearing session and redirecting to Auth0 logout"
  [req]
  (let [domain-value @env/domain
        return-url (str domain-value "/customizer")
        client-id @env/auth0-customizer-client-id
        ;; Add client_id parameter for Auth0
        auth0-logout-url (str "https://dev-me4o6oy6ayzpw476.eu.auth0.com/v2/logout?"
                              "returnTo=" (java.net.URLEncoder/encode return-url "UTF-8")
                              "&client_id=" client-id)]
    {:status  302
     :session {}
     :headers {"Location" auth0-logout-url
               "Cache-Control" "no-cache, no-store, must-revalidate"
               "Pragma" "no-cache" 
               "Expires" "0"}
     :body    "Redirecting to logout..."}))