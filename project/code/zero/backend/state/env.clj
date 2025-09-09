(ns zero.backend.state.env
  (:require [zero.backend.tools.log :as log]))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Environment Variable Helper
;;; ═══════════════════════════════════════════════════════════════════════════

(defn env-variable 
  "Gets environment variable. Throws exception if required and missing. Uses default if provided."
  ([var-name] (env-variable var-name true nil))
  ([var-name required?] (env-variable var-name required? nil))
  ([var-name required? default-value]
   (let [var (System/getenv var-name)]
     (if (nil? var)
       (if default-value
         (do (log/info-log (str "Using default value for env variable: " var-name))
             default-value)
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
               nil)))
       var))))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Core Configuration
;;; ═══════════════════════════════════════════════════════════════════════════

(def dev?   (delay (env-variable "EXPERTLIFT_DEV" false "true")))
(def port   (delay (Integer/parseInt (env-variable "EXPERTLIFT_PORT" false "3000"))))
(def domain (delay (str "https://" (env-variable "EXPERTLIFT_DOMAIN" false "localhost"))))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Auth0 Configuration
;;; ═══════════════════════════════════════════════════════════════════════════

(def auth0-app-client-id     (delay (env-variable "EXPERTLIFT_AUTH0_CLIENT_ID" false "expertlift-dev-client")))
(def auth0-app-client-secret (delay (env-variable "EXPERTLIFT_AUTH0_CLIENT_SECRET" false "dev-secret-123")))
(def auth0-domain            (delay (env-variable "EXPERTLIFT_AUTH0_DOMAIN" false "expertlift-dev.auth0.com")))
(def auth0-audience          (delay (env-variable "EXPERTLIFT_AUTH0_AUDIENCE" false "https://api.expertlift.dev")))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; External Services
;;; ═══════════════════════════════════════════════════════════════════════════

;; MinIO Storage
(def minio-url (delay (env-variable "EXPERTLIFT_MINIO_URL" false "http://localhost:9000")))
(def minio-endpoint (delay (env-variable "EXPERTLIFT_MINIO_ENDPOINT" false "localhost:9000")))
(def minio-access-key (delay (env-variable "EXPERTLIFT_MINIO_ACCESS_KEY" false "expertlift-dev")))
(def minio-secret-key (delay (env-variable "EXPERTLIFT_MINIO_SECRET_KEY" false "expertlift-dev-secret")))

;; Email Configuration
(def email-host     (delay (env-variable "EXPERTLIFT_EMAIL_HOST" false "smtp.gmail.com")))
(def email-username (delay (env-variable "EXPERTLIFT_EMAIL_USERNAME" false "dev@expertlift.local")))
(def email-password (delay (env-variable "EXPERTLIFT_EMAIL_PASSWORD" false "dev-email-password")))

;; Stripe Payment Processing
(def stripe-secret-key     (delay (env-variable "EXPERTLIFT_STRIPE_SECRET_KEY" false "sk_test_expertlift_dev")))
(def stripe-webhook-secret (delay (env-variable "EXPERTLIFT_STRIPE_WEBHOOK_SECRET" false "whsec_expertlift_dev")))


;;; ═══════════════════════════════════════════════════════════════════════════
;;; Database Configuration
;;; ═══════════════════════════════════════════════════════════════════════════

(def db-host     (delay (env-variable "EXPERTLIFT_DB_HOST" false "localhost")))
(def db-port     (delay (Integer/parseInt (env-variable "EXPERTLIFT_DB_PORT" false "5432"))))
(def db-user     (delay (env-variable "EXPERTLIFT_DB_USER" false "expertlift")))
(def db-password (delay (env-variable "EXPERTLIFT_DB_PASSWORD" false "expertlift-dev")))
(def db-name     (delay (env-variable "EXPERTLIFT_DB_NAME" false "expertlift")))
