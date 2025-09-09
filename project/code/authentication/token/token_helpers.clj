(ns authentication.token.token-helpers
  (:require
   [cheshire.core :as json]
   [buddy.core.keys :as buddy-keys]
   [clojure.string :as string]
   [clojure.walk :as clojure.walk]
   [hato.client :as hato]
   [authentication.config :as config])
  (:import
   (java.util Base64)
   (java.time ZoneId ZonedDateTime)))

; -----------------------------------------
; -----------------------------------------

(defn kid->public-key [public-keys kid]
  ;; Fetching public keys
  ;; Filtering public keys by kid
  ;; Jwk key transform to valid public-key
  (let [compare-fn  #(= kid (get % "kid"))]
    (->> public-keys

         (filter compare-fn)
         first
         clojure.walk/keywordize-keys
         buddy-keys/jwk->public-key)))

; -----------------------------------------
; -----------------------------------------

(defn fetch-jwks [auth0-public-keys-url]
  (get (json/decode (:body (hato/get auth0-public-keys-url)))
       "keys"))

; -----------------------------------------
; -----------------------------------------

(defn response->id-token [response]
  ;; Get id_token, so the oauth token from response
  (-> response :body json/decode (get "id_token")))

; -----------------------------------------
; ----------------------------------------- 

(defn base64-decode
  "Utility function over the Java 8 base64 decoder"
  [to-decode]
  (String. (.decode (Base64/getDecoder) ^String to-decode)))

(defn string->edn
  "Parse JSON from a string returning an edn map, otherwise nil"
  [string]
  (when-let [edn (json/decode string true)]
    (when (map? edn)
      edn)))

; Using with buddy, so leave the signature undecoded
(defn decode-jwt-token
  "Transform a properly formed JWT into a Clojure map"
  [jwt]
  (when (and jwt (string? jwt) (seq jwt))
    (when-let [jwt-parts (string/split jwt #"\.")]
      (when (= 3 (count jwt-parts))
        (let [[b64-header b64-payload b64-signature] jwt-parts]
          {:header    (string->edn (base64-decode b64-header))
           :payload   (string->edn (base64-decode b64-payload))
           :signature b64-signature})))))

(defn jwt-token->key-id [token]
  ;;Decoding token, getting kid (key id)
  (when token
    (let [decoded-token (decode-jwt-token token)
          kid (-> decoded-token :header :kid)]
      kid)))

; -----------------------------------------
; Token Validation
; -----------------------------------------

(defn epoch-over-now?
  "Check if epoch time is in the future"
  ([epoch-time]
   (epoch-over-now? epoch-time (ZoneId/systemDefault)))
  ([epoch-time timezone]
   (when (int? epoch-time)
     (let [zone-id (if (instance? ZoneId timezone)
                     timezone
                     (ZoneId/of (str timezone)))
           current-epoch (-> (ZonedDateTime/now zone-id)
                             .toInstant
                             .toEpochMilli
                             (/ 1000))]               ;; Convert into seconds
       (> epoch-time current-epoch)))))

(defn is-valid-token? 
  "Check if authentication token is valid for the specified module"
  [token & {:keys [module]}]
  (and
   (epoch-over-now? (:exp token))
   (= (:aud token) (config/get-auth0-client-id module))
   (= (:iss token) (str config/auth0-url "/"))
   ;; TODO: Add token blacklist check here
   ;; (not (token-blacklisted? (:jti token)))
   ))