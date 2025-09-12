(ns router.backend.middleware
  (:require
   [ring.middleware.defaults             :refer [site-defaults wrap-defaults]]
  ;;  [ring.middleware.json                 :refer [wrap-json-body]]
   [ring.middleware.params               :refer [wrap-params]]
   [ring.middleware.transit              :refer [wrap-transit-params]]
   [ring.middleware.gzip                 :refer [wrap-gzip]]
   [zero.backend.state.env :as env]))

(defn wrap-require-authentication
  "Middleware that redirects to /login if user is not authenticated"
  [handler]
  (fn [request]
    (let [user-id (get-in request [:session :user-id])
          session (get request :session)]
      (println "DEBUG: Authentication middleware - user-id:" user-id)
      (println "DEBUG: Authentication middleware - session keys:" (keys session))
      (println "DEBUG: Authentication middleware - full session:" session)
      (if user-id
        (do
          (println "DEBUG: User authenticated, proceeding with request")
          (handler request))
        (do
          (println "DEBUG: User not authenticated, redirecting to /login")
          {:status 302
           :headers {"Location" "/login"}
           :body ""})))))

(defn middleware []
  {:middleware [;#(wrap-reload           % {:dirs ["/source-code"]});watched-dirs})  
                #(wrap-gzip             %)
                #(wrap-transit-params   % {:opts {}})
                #(wrap-params           %)
                #(wrap-defaults         % (->
                                           site-defaults
                                           (assoc-in [:session :cookie-attrs :same-site] :lax)
                                           (assoc-in [:security :anti-forgery] (not @env/dev?))))]})




