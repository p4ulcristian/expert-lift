(ns authentication.middlewares.customizer
  (:require
   [authentication.helpers :as helpers]
   [authentication.token.token-helpers :as token-helpers]
   [schemas.session :as schema]))

(defn redirect-to-login-response 
  "Redirect to login with return URL"
  [req uri]
  (assoc 
   (helpers/redirect-handler "/login/customizer")
   :session (schema/validate-session
             (assoc (:session req)
                    :redirect-url uri))))


(defn require-customizer-role 
  "Middleware to require user role authorization"
  [handler]
  (fn [req] 
    (let [session       (schema/validate-session (:session req))
          redirect-url  (:uri req)
          user-id       (:user-id session)
          user-roles     (:user-roles session)
          token         (:token session)
          response      (handler req)
          valid-token?  (token-helpers/is-valid-token? token :module :customizer)
          customizer?         (some #(= "customizer" %) user-roles)] 
      (println "Customizer middleware - User ID from session:" user-id user-roles valid-token?) 
      (if (and valid-token? customizer?)
        response
        (redirect-to-login-response req redirect-url)))))