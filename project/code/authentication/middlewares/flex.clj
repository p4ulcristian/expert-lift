(ns authentication.middlewares.flex
  (:require
   [authentication.helpers :as helpers]
   [authentication.token.token-helpers :as token-helpers]
   [schemas.session :as schema]))

(defn redirect-to-login-response 
  "Redirect to login with return URL"
  [req uri]
  (assoc 
   (helpers/redirect-handler "/login/flex")
   :session (schema/validate-session
             (assoc (:session req)
                    :redirect-url uri))))


(defn require-flex-role 
  "Middleware to require flex role authorization"
  [handler]
  (fn [req] 
    (let [session       (schema/validate-session (:session req))
          redirect-url  (:uri req)
          user-id       (:user-id session)
          user-roles     (:user-roles session)
          token         (:token session)
          response      (handler req)
          valid-token?  (token-helpers/is-valid-token? token :module :flex)
          flex?         (some #(= "flex" %) user-roles)] 
      (println "Flex middleware - User ID from session:" user-id "[flex]" flex? "[valid-token]" valid-token?) 
      (if (and valid-token? flex?)
        response
        (redirect-to-login-response req redirect-url)))))