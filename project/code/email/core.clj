(ns email.core
  (:require
   [postal.core :as postal]
   [zero.backend.state.env :as env]))

(defn- email-config
  "Build email configuration from environment variables"
  []
  {:host @env/email-host
   :port 465 ;; Standard SMTP SSL port
   :ssl true ;; Always use SSL for security
   :user @env/email-username
   :pass @env/email-password})

(defn send-email
  "Send a plain text email
  
  Args:
    from - sender email address (string)
    to - recipient email address (string or vector of strings)
    subject - email subject (string)
    body - email body (string)
    
  Returns:
    postal response map"
  [from to subject body]
  (let [config (email-config)
        message {:from from
                 :to to
                 :subject subject
                 :body body}]
    (postal/send-message config message)))

(defn send-html-email
  "Send an HTML email with optional plain text alternative
  
  Args:
    from - sender email address (string)
    to - recipient email address (string or vector of strings)
    subject - email subject (string)
    html-body - HTML email body (string)
    text-body - plain text alternative (optional, string)
    
  Returns:
    postal response map"
  [from to subject html-body & [text-body]]
  (let [config (email-config)
        body-parts (if text-body
                     [{:type "text/plain; charset=utf-8"
                       :content text-body}
                      {:type "text/html; charset=utf-8"
                       :content html-body}]
                     [{:type "text/html; charset=utf-8"
                       :content html-body}])
        message {:from from
                 :to to
                 :subject subject
                 :body body-parts}]
    (postal/send-message config message)))

(defn send-email-with-attachments
  "Send an email with file attachments
  
  Args:
    from - sender email address (string)
    to - recipient email address (string or vector of strings)
    subject - email subject (string)
    body - email body (string)
    attachments - vector of attachment maps with :type, :content, and optionally :file-name
    
  Returns:
    postal response map"
  [from to subject body attachments]
  (let [config (email-config)
        message {:from from
                 :to to
                 :subject subject
                 :body (into [body] attachments)}]
    (postal/send-message config message)))

(defn email-configured?
  "Check if email is properly configured with required environment variables"
  []
  (let [config (email-config)]
    (and (:user config)
         (:pass config)
         (:host config))))