(ns features.flex.teams.backend.email
  (:require
   [zero.backend.state.env :as env]))

(defn create-invitation-email-html
  "Create a fancy HTML email template for team invitations"
  [inviter-name workspace-name role invitation-id expires-at]
  (str
   "<!DOCTYPE html>"
   "<html lang=\"en\">"
   "<head>"
   "<meta charset=\"UTF-8\">"
   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
   "<title>Team Invitation</title>"
   "</head>"
   "<body style=\"margin: 0; padding: 0; background-color: #333333; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\">"
   
   "<!-- Main Container -->"
   "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #333333; min-height: 100vh;\">"
   "<tr><td align=\"center\" style=\"padding: 40px 20px;\">"
   
   "<!-- Email Content -->"
   "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 20px 40px rgba(0,0,0,0.3);\">"
   
   "<!-- Header with Logo -->"
   "<tr><td style=\"background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%); padding: 40px 40px 30px; text-align: center;\">"
   "<img src=\"https://ironrainbowcoating.com/logo/logo-good-size.png\" alt=\"Iron Rainbow\" style=\"height: 100px; margin-bottom: 20px;\">"
   "<h1 style=\"margin: 0; color: #333333; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;\">YOU ARE INVITED TO RAINBOW FLEX</h1>"
   "</td></tr>"
   
   "<!-- Main Content -->"
   "<tr><td style=\"padding: 40px;\">"
   "<h2 style=\"margin: 0 0 20px; color: #333333; font-size: 24px; font-weight: 600; line-height: 1.3;\">"
   "Join <span style=\"color: #ffd700;\">" workspace-name "</span>"
   "</h2>"
   
   "<p style=\"margin: 0 0 25px; color: #666666; font-size: 16px; line-height: 1.6;\">"
   "<strong style=\"color: #333333;\">" inviter-name "</strong> has invited you to collaborate on Iron Rainbow's most advanced coating management platform."
   "</p>"
   
   
   "<!-- CTA Button -->"
   "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin: 35px 0;\">"
   "<tr><td align=\"center\">"
   "<a href=\"" @env/domain "/accept-invitation?id=" invitation-id "\" "
   "style=\"display: inline-block; background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%); "
   "color: #333333; text-decoration: none; padding: 18px 40px; border-radius: 50px; "
   "font-weight: 700; font-size: 16px; letter-spacing: 0.5px; text-transform: uppercase; "
   "box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4); transition: all 0.3s ease;\">"
   "ACCEPT INVITATION"
   "</a>"
   "</td></tr></table>"
   
   "<p style=\"margin: 30px 0 15px; color: #999999; font-size: 14px; text-align: center;\">"
   "This invitation expires on <strong style=\"color: #333333;\">" expires-at "</strong>"
   "</p>"
   
   "<p style=\"margin: 0; color: #999999; font-size: 14px; text-align: center;\">"
   "Can't click the button? Copy and paste this link:<br>"
   "<span style=\"color: #ffd700; word-break: break-all;\">" @env/domain "/accept-invitation?id=" invitation-id "</span>"
   "</p>"
   
   "</td></tr>"
   
   "<!-- Footer -->"
   "<tr><td style=\"background-color: #333333; padding: 30px 40px; text-align: center;\">"
   "<p style=\"margin: 0 0 10px; color: #ffffff; font-size: 18px; font-weight: 600;\">Iron Rainbow</p>"
   "<p style=\"margin: 0; color: #999999; font-size: 14px;\">Advanced Coating Management Platform</p>"
   "</td></tr>"
   
   "</table>"
   "</td></tr></table>"
   "</body></html>"))

(defn create-invitation-email-text
  "Create plain text version of team invitation email"
  [inviter-name workspace-name role invitation-id expires-at]
  (str "Hello!\n\n"
       inviter-name " has invited you to join the \"" workspace-name "\" workspace on Iron Rainbow.\n\n"
       "Role: " (clojure.string/capitalize role) "\n\n"
       "To accept this invitation, please click the link below:\n"
       @env/domain "/accept-invitation?id=" invitation-id "\n\n"
       "This invitation will expire on " expires-at ".\n\n"
       "Best regards,\n"
       "The Iron Rainbow Team"))