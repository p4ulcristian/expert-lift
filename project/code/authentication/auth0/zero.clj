(ns authentication.auth0.zero
  (:require
   [authentication.config :as config]
   [authentication.token.token-helpers :as jwt-token]
   [buddy.sign.jwt :as jwt]
   [hato.client :as hato]))

(defn code->get-auth0-token [code redirect-url & {:keys [module]}]
  (let [response (try
                   (hato/post config/auth0-oauth-public-url
                              {:content-type "application/x-www-form-urlencoded"
                               :form-params {:grant_type    "authorization_code"
                                             :client_id     (config/get-auth0-client-id module)
                                             :client_secret (config/get-auth0-client-secret module)
                                             :code          code
                                             :redirect_uri  redirect-url}})
                   (catch Exception e 
                          (println
                           "🔥 Error when fetching oauth token: 
                                          🔥" (:body (ex-data e)))))]
    (when response
      (let [token (jwt-token/response->id-token response)]
        token))))

(defn validate-jwt-token [token]
  ;; Fourth step
  ;; We need to validate the oauth token which is a 
  ;; 4 step process 
  (try
    (let [;; 1. Gather jwk (public keys) from /.well-known/jwks.json
          auth0-jwks (jwt-token/fetch-jwks config/auth0-jwks-url)
          ;; 2. From the tokens header extract the key id (kid)
          kid   (jwt-token/jwt-token->key-id token)
          ;; 3. We get same 'kid' jwk, then convert it to public key
          public-key (jwt-token/kid->public-key auth0-jwks kid)]
      ;; 4. Unsign the token, validate it. Otherwise throw error
      (try (jwt/unsign token public-key {:alg :rs256})
           (catch Exception e
             (println "jwt/unsign failed - " e))))
    (catch Exception e
      (println "validate-jwt-token failed - " e))))

(defn get-validated-token [code redirect-url & {:keys [module]}]
  (println "Validating through auth0")
  (when (and code (seq code))
    (let [token (code->get-auth0-token code redirect-url :module module)]
      (when token
        (validate-jwt-token token)))))