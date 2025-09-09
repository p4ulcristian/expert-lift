(ns authentication.middlewares.labs
  (:require
   [authentication.helpers :as helpers]
   [authentication.token.token-helpers :as token-helpers]
   [schemas.session :as schema]))


(defn redirect-to-login-response 
  "Redirect to login with return URL"
  [req uri]
  (assoc 
   (helpers/redirect-handler "/login/labs")
   :session (schema/validate-session
             (assoc (:session req)
                    :redirect-url uri))))




(defn require-labs-role 
  "Middleware to require labs role authorization"
  [handler]
  (fn [req] 
    (let [session       (schema/validate-session (:session req))
          redirect-url  (:uri req)
          user-id       (:user-id session)
          user-roles    (:user-roles session)
          token         (:token session)
          response      (handler req)
          valid-token?  (token-helpers/is-valid-token? token :module :labs)
          labs?         (some #(= "labs" %) user-roles)] 
      (println "Labs middleware - User ID from session:" user-id user-roles valid-token?) 
      (if (and valid-token? labs?)
        response
        (redirect-to-login-response req redirect-url)))))