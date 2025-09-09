(ns features.flex.dashboard.backend.write
  (:require
   [email.core :as email]
   [users.backend.resolvers :as users]
   [features.flex.workspaces.backend.db :as workspace-db]))

(defn get-user-id-from-request
  "Get user ID from request session"
  [request]
  (get-in request [:session :user-id]))

(defn format-current-date
  "Format current date for email"
  []
  (let [current-date (java.time.LocalDateTime/now)
        formatter (java.time.format.DateTimeFormatter/ofPattern "MMMM d, yyyy 'at' h:mm a")]
    (.format current-date formatter)))

(defn create-email-header
  "Create email header HTML"
  [feedback-type]
  (str
   "<!-- Header -->"
   "<tr><td style=\"background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%); padding: 40px 40px 30px; text-align: center;\">"
   "<img src=\"https://ironrainbowcoating.com/logo/logo-good-size.png\" alt=\"Iron Rainbow\" style=\"height: 80px; margin-bottom: 20px;\">"
   "<h1 style=\"margin: 0; color: #333333; font-size: 24px; font-weight: 700;\">💬 USER FEEDBACK</h1>"
   "<div style=\"background: rgba(51,51,51,0.1); padding: 8px 16px; border-radius: 20px; display: inline-block; margin-top: 12px; font-size: 14px; font-weight: 600; color: #333333;\">"
   (or feedback-type "General Feedback")
   "</div>"
   "</td></tr>"))

(defn create-email-content
  "Create email content HTML"
  [body]
  (str
   "<!-- Message Content -->"
   "<tr><td style=\"padding: 40px;\">"
   "<h2 style=\"margin: 0 0 20px; color: #333333; font-size: 20px; font-weight: 600;\">📝 Message</h2>"
   "<div style=\"background: #f8fafc; border-left: 4px solid #ffd700; padding: 20px; border-radius: 6px; font-size: 16px; line-height: 1.7; color: #333333;\">"
   (clojure.string/replace body "\n" "<br>")
   "</div>"
   "</td></tr>"))

(defn create-user-info-row
  "Create user info table row"
  [label value]
  (str "<tr style=\"border-bottom: 1px solid #e2e8f0;\">"
       "<td style=\"font-weight: 600; color: #4a5568; width: 120px;\">" label ":</td>"
       "<td style=\"color: #333333;\">" value "</td>"
       "</tr>"))

(defn create-workspace-info-rows
  "Create workspace info rows if workspace exists"
  [workspace workspace-id]
  (str
   (when workspace
     (create-user-info-row "Workspace" (:name workspace)))
   (when workspace-id
     (str "<tr style=\"border-bottom: 1px solid #e2e8f0;\">"
          "<td style=\"font-weight: 600; color: #4a5568;\">Workspace ID:</td>"
          "<td style=\"font-family: monospace; background: #edf2f7; padding: 4px 8px; border-radius: 4px; font-size: 14px; color: #333333;\">" workspace-id "</td>"
          "</tr>"))))

(defn create-user-info-section
  "Create user info section HTML"
  [user user-id workspace workspace-id formatted-date]
  (str
   "<!-- User Information -->"
   "<tr><td style=\"padding: 0 40px 40px; background: #fafbfc;\">"
   "<h3 style=\"margin: 0 0 20px; color: #333333; font-size: 18px; font-weight: 600;\">👤 User Information</h3>"
   "<table width=\"100%\" cellpadding=\"8\" cellspacing=\"0\" style=\"border-collapse: collapse;\">"
   (create-user-info-row "Name" (or (str (:first_name user) " " (:last_name user)) (:email user)))
   "<tr style=\"border-bottom: 1px solid #e2e8f0;\">"
   "<td style=\"font-weight: 600; color: #4a5568;\">Email:</td>"
   "<td style=\"color: #ffd700;\">" (:email user) "</td>"
   "</tr>"
   "<tr style=\"border-bottom: 1px solid #e2e8f0;\">"
   "<td style=\"font-weight: 600; color: #4a5568;\">User ID:</td>"
   "<td style=\"font-family: monospace; background: #edf2f7; padding: 4px 8px; border-radius: 4px; font-size: 14px; color: #333333;\">" user-id "</td>"
   "</tr>"
   (create-workspace-info-rows workspace workspace-id)
   (create-user-info-row "Date" formatted-date)
   "</table>"
   "</td></tr>"))

(defn create-email-footer
  "Create email footer HTML"
  []
  (str
   "<!-- Footer -->"
   "<tr><td style=\"background-color: #333333; padding: 30px 40px; text-align: center;\">"
   "<p style=\"margin: 0 0 10px; color: #ffffff; font-size: 18px; font-weight: 600;\">Iron Rainbow</p>"
   "<p style=\"margin: 0; color: #999999; font-size: 14px;\">Customer Feedback System</p>"
   "</td></tr>"))

(defn create-feedback-email-html
  "Create professional HTML email template for user feedback"
  [user workspace feedback-type body user-id workspace-id]
  (let [formatted-date (format-current-date)]
    (str
     "<!DOCTYPE html>"
     "<html lang=\"en\">"
     "<head>"
     "<meta charset=\"UTF-8\">"
     "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
     "<title>User Feedback</title>"
     "</head>"
     "<body style=\"margin: 0; padding: 0; background-color: #333333; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">"
     
     "<!-- Main Container -->"
     "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #333333; min-height: 100vh;\">"
     "<tr><td align=\"center\" style=\"padding: 40px 20px;\">"
     
     "<!-- Email Content -->"
     "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.3);\">"
     
     (create-email-header feedback-type)
     (create-email-content body)
     (create-user-info-section user user-id workspace workspace-id formatted-date)
     (create-email-footer)
     
     "</table>"
     "</td></tr></table>"
     "</body></html>")))

(defn create-text-body
  "Create plain text email body"
  [user workspace feedback-type body user-id workspace-id]
  (str "USER FEEDBACK\n\n"
       "Feedback Type: " (or feedback-type "General Feedback") "\n"
       "User: " (or (str (:first_name user) " " (:last_name user)) (:email user)) "\n"
       "Email: " (:email user) "\n"
       "User ID: " user-id "\n"
       (when workspace
         (str "Workspace: " (:name workspace) "\n"
              "Workspace ID: " workspace-id "\n"))
       "Date: " (java.time.LocalDateTime/now) "\n\n"
       "MESSAGE:\n" body "\n\n"
       "---\nIron Rainbow Coating - Customer Feedback System"))

(defn send-feedback-email
  "Send feedback email"
  [from-email to-email subject html-body text-body]
  (try
    (let [response (email/send-html-email from-email to-email subject html-body text-body)]
      (if (= (:error response) 0)
        {:success true :message "Email sent successfully" :debug response}
        {:success false :message (str "Failed to send email. Response: " response) :debug response}))
    (catch Exception e
      {:success false :message (str "Error sending email: " (.getMessage e)) :debug {:exception (.getMessage e)}})))

(defn send-dashboard-email
  "Send dashboard feedback email"
  [{:parquery/keys [context request] :as params}]
  (let [user-id (or (:user-id context) (get-user-id-from-request request))
        {:keys [subject body to-email feedback-type workspace-id]} params
        user (users/get-user-by-id-fn user-id)
        workspace (when workspace-id (workspace-db/get-workspace-by-id workspace-id))
        from-email "partner@ironrainbowcoating.com"]
    
    (if (email/email-configured?)
      (let [html-body (create-feedback-email-html user workspace feedback-type body user-id workspace-id)
            text-body (create-text-body user workspace feedback-type body user-id workspace-id)]
        (send-feedback-email from-email to-email subject html-body text-body))
      {:success false 
       :message "Email not configured. Please set environment variables." 
       :debug {:configured false}})))