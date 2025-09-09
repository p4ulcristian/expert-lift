(ns zero.backend.state.env
  (:require [zero.backend.tools.log :as log]))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Environment Variable Helper
;;; ═══════════════════════════════════════════════════════════════════════════

(defn env-variable 
  "Gets environment variable. Throws exception if required and missing."
  ([var-name] (env-variable var-name true))
  ([var-name required?]
   (let [var (System/getenv var-name)]
     (if (nil? var)
       (if required?
         (throw (ex-info 
                  (str "\n\n🚨 MISSING REQUIRED ENVIRONMENT VARIABLE\n"
                       "────────────────────────────────────\n"
                       "Variable: " var-name "\n"
                       "\nThis application requires the '" var-name "' environment variable to be set.\n"
                       "Please add it to your .env file or export it in your shell:\n\n"
                       "  export " var-name "=<your-value>\n\n"
                       "Then restart the application.\n")
                  {:missing-env-var var-name}))
         (do (log/warning-log (str "Optional env variable missing: " var-name))
             nil))
       var))))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Core Configuration
;;; ═══════════════════════════════════════════════════════════════════════════

(def dev?   (delay (env-variable "IRONRAINBOW_DEV")))
(def port   (delay (Integer/parseInt (env-variable "IRONRAINBOW_PORT"))))
(def domain (delay (str "https://" (env-variable "IRONRAINBOW_DOMAIN"))))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Auth0 Configuration
;;; ═══════════════════════════════════════════════════════════════════════════

(def auth0-customizer-client-id     (delay (env-variable "IRONRAINBOW_AUTH0_CUSTOMIZER_CLIENT_ID")))
(def auth0-customizer-client-secret (delay (env-variable "IRONRAINBOW_AUTH0_CUSTOMIZER_SECRET")))
(def auth0-flex-client-id           (delay (env-variable "IRONRAINBOW_AUTH0_FLEX_CLIENT_ID")))
(def auth0-flex-client-secret       (delay (env-variable "IRONRAINBOW_AUTH0_FLEX_SECRET")))
(def auth0-labs-client-id           (delay (env-variable "IRONRAINBOW_AUTH0_LABS_CLIENT_ID")))
(def auth0-labs-client-secret       (delay (env-variable "IRONRAINBOW_AUTH0_LABS_SECRET")))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; External Services
;;; ═══════════════════════════════════════════════════════════════════════════

;; MinIO Storage
(def minio-url (delay (env-variable "IRONRAINBOW_MINIO_URL")))
(def minio-endpoint (delay (env-variable "IRONRAINBOW_MINIO_ENDPOINT")))
(def minio-access-key (delay (env-variable "IRONRAINBOW_MINIO_ACCESS_KEY")))
(def minio-secret-key (delay (env-variable "IRONRAINBOW_MINIO_SECRET_KEY")))

;; Email Configuration
(def email-host     (delay (env-variable "IRONRAINBOW_EMAIL_HOST")))
(def email-username (delay (env-variable "IRONRAINBOW_EMAIL_USERNAME")))
(def email-password (delay (env-variable "IRONRAINBOW_EMAIL_PASSWORD")))

;; Stripe Payment Processing
(def stripe-secret-key     (delay (env-variable "IRONRAINBOW_STRIPE_SECRET_KEY")))
(def stripe-webhook-secret (delay (env-variable "IRONRAINBOW_STRIPE_WEBHOOK_SECRET_KEY")))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Database Configuration
;;; ═══════════════════════════════════════════════════════════════════════════

(def db-host     (delay (env-variable "IRONRAINBOW_DB_HOST")))
(def db-port     (delay (Integer/parseInt (env-variable "IRONRAINBOW_DB_PORT"))))
(def db-user     (delay (env-variable "IRONRAINBOW_DB_USER")))
(def db-password (delay (env-variable "IRONRAINBOW_DB_PASSWORD")))
(def db-name     (delay (env-variable "IRONRAINBOW_DB_NAME")))
