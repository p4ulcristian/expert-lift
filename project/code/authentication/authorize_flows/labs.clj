(ns authentication.authorize-flows.labs
  (:require
   [authentication.helpers :as helpers]
   [clojure.string :as str]
   [hiccup.core :as h]
   [hiccup.page :as page]
   [users.backend.resolvers :as user-resolvers]
   [users.backend.db :as user-db]))


(defn create-funny-403-page 
  "Create a funny dark-themed HTML 403 error page using Hiccup"
  [sub]
  (h/html
   (page/html5
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:title "Nope! - 403 🚫"]
     [:style "
        body {
            margin: 0;
            padding: 0;
            font-family: 'Courier New', monospace;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
        }
        .container {
            text-align: center;
            background: rgba(0, 0, 0, 0.8);
            padding: 3rem;
            border-radius: 20px;
            border: 2px solid #ff6b6b;
            box-shadow: 0 0 50px rgba(255, 107, 107, 0.3);
            max-width: 600px;
            margin: 2rem;
            animation: glow 2s ease-in-out infinite alternate;
        }
        @keyframes glow {
            from { box-shadow: 0 0 20px rgba(255, 107, 107, 0.3); }
            to { box-shadow: 0 0 50px rgba(255, 107, 107, 0.6); }
        }
        .error-code {
            font-size: 8rem;
            font-weight: bold;
            color: #ff6b6b;
            margin: 0;
            text-shadow: 0 0 20px rgba(255, 107, 107, 0.8);
            animation: bounce 1s infinite;
        }
        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
            40% { transform: translateY(-10px); }
            60% { transform: translateY(-5px); }
        }
        .error-message {
            font-size: 2rem;
            color: #4ecdc4;
            margin: 1rem 0;
            text-shadow: 0 0 10px rgba(78, 205, 196, 0.5);
        }
        .funny-message {
            font-size: 1.2rem;
            color: #ffe66d;
            margin: 1rem 0;
            font-style: italic;
        }
        .sub-message {
            font-size: 1rem;
            color: #a8e6cf;
            margin: 1rem 0 2rem 0;
        }
        .user-info {
            background: rgba(255, 107, 107, 0.1);
            border: 1px solid #ff6b6b;
            padding: 1rem;
            border-radius: 10px;
            margin: 1rem 0;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
            color: #ddd;
        }
        .ascii-art {
            font-family: 'Courier New', monospace;
            color: #ff6b6b;
            font-size: 0.8rem;
            line-height: 1;
            margin: 1rem 0;
            white-space: pre;
        }
        .button {
            display: inline-block;
            padding: 12px 24px;
            background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
            color: white;
            text-decoration: none;
            border-radius: 25px;
            transition: all 0.3s ease;
            margin: 0 0.5rem;
            font-weight: bold;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
        }
        .button:hover {
            transform: scale(1.05);
            box-shadow: 0 5px 15px rgba(255, 107, 107, 0.4);
        }
        .emoji {
            font-size: 3rem;
            margin: 0.5rem;
            display: inline-block;
            animation: spin 3s linear infinite;
        }
        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }
        .matrix-rain {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: -1;
            opacity: 0.1;
            font-family: 'Courier New', monospace;
            font-size: 10px;
            color: #4ecdc4;
        }
     "]]
    [:body
     [:div.matrix-rain "01001000 01100101 01101100 01110000"]
     [:div.container
      [:div.emoji "🚫"]
      [:h1.error-code "403"]
      [:h2.error-message "NOPE.exe has stopped working"]
      [:p.funny-message "\"You shall not pass!\" - Gandalf (probably)"]
      [:div.ascii-art "
    ╔═══════════════════════════════╗
    ║  UNAUTHORIZED ACCESS ATTEMPT  ║
    ║        DETECTED! 🚨           ║
    ╚═══════════════════════════════╝
      "]
      [:p.sub-message "Oops! Looks like you're trying to access the secret labs cave, but you forgot your magic labs key! 🗝️"]
      [:div.user-info
       [:strong "Hacker ID: "] sub [:br]
       [:strong "Clearance Level: "] "🥔 Potato" [:br]
       [:strong "Required Level: "] "🦄 Unicorn Labs Access" [:br]
       [:strong "Status: "] "❌ Git denied!"]
      [:p "Don't worry, we won't tell the FBI... this time. 😉"]
      [:p "Maybe try asking nicely? Or bring cookies? 🍪"]
      [:a.button {:href "/"} "🏠 Retreat Safely"]
      [:a.button {:href "/login"} "🔐 Try Again"]]])))

(defn merge-to-session 
  "Merge new session data with existing session"
  [req new-session]
  (let [old-session (:session req)]
    (assoc req :session (merge old-session new-session))))

(defn create-user-from-token 
  "Create a new user from Auth0 token data and OAuth provider entry"
  [auth0-token]
  (when auth0-token
    (let [sub        (:sub auth0-token)
          email      (:email auth0-token)
          name       (:name auth0-token)
          picture    (:picture auth0-token)
          provider   (if sub (first (str/split sub #"\|")) "unknown")
          [first-name last-name] (if name 
                                   (str/split name #" " 2)
                                   ["" ""])
          user-id    (str (java.util.UUID/randomUUID))]
      ;; Create user without oauth_id
      (user-db/create-user user-id (or first-name "") (or last-name "") (or email "") picture)
      ;; Create OAuth provider entry
      (user-db/create-oauth-provider (str (java.util.UUID/randomUUID)) user-id sub provider)
      user-id)))

(defn ensure-labs-role 
  "Ensure user has labs role, assign if missing"
  [user-id]
  (let [user-roles (mapv :role (user-db/get-user-roles user-id))
        has-labs? (some #(= "labs" %) user-roles)]
    (when-not has-labs?
      (user-db/add-user-role (str (java.util.UUID/randomUUID)) user-id "labs"))))

(defn handle-labs-authorization 
  "Handle labs authorization flow - requires existing labs role"
  [auth0-token redirect-url]
  (if (nil? auth0-token)
    (helpers/redirect-handler "/login/labs")
    (let [sub         (:sub auth0-token)
          email       (:email auth0-token)
        user        (when sub (user-resolvers/get-user-by-oauth-id-fn sub))
        existing-user-by-email (when (and (not user) email) 
                                 (user-resolvers/get-user-by-email-fn email))
        user-id     (cond 
                      user (str (:id user))
                      existing-user-by-email 
                      ;; Link OAuth to existing user
                      (when sub
                        (user-db/create-oauth-provider (str (java.util.UUID/randomUUID)) 
                                                       (:id existing-user-by-email) 
                                                       sub 
                                                       (if sub (first (str/split sub #"\|")) "unknown"))
                        (str (:id existing-user-by-email)))
                      :else (create-user-from-token auth0-token))
          user-roles  (when user-id
                        (mapv :role (user-db/get-user-roles user-id)))
          has-labs?   (some #(= "labs" %) user-roles)]
      (cond 
        (and user-id has-labs?)
        (merge-to-session
         (helpers/redirect-handler (or redirect-url "/irunrainbow"))
         {:user-roles user-roles
          :user-id   user-id
          :token     auth0-token})
        
        user-id
        {:status  403
         :body    (create-funny-403-page sub)
         :headers {"Content-type" "text/html"}}
        
        :else 
        {:status  500
         :body    "Failed to create or retrieve user"
         :headers {"Content-type" "text/plain"}}))))