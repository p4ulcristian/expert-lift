(ns translations.core
  "Core translation system for EN/HU language support"
  (:require [zero.frontend.re-frame :as rf]))

;; Translation data
(def translations
  {:en {:header/logout "Logout"
        :header/brand "ElevaThor"
        :dashboard/title "Dashboard"
        :dashboard/welcome "Welcome back!"
        :dashboard/access-denied "Access Denied"
        :dashboard/access-denied-message "Sorry, you don't have access to this workspace or it doesn't exist. Please log in with the appropriate account."
        :dashboard/go-to-login "Go to Login"
        :dashboard/workspace-id "Workspace ID"
        :features/material-templates "Material Templates"
        :features/material-templates-desc "Manage standard materials and supplies"
        :features/addresses "Addresses"
        :features/addresses-desc "Manage workspace addresses and locations"
        :features/worksheets "Worksheets"
        :features/worksheets-desc "Manage work orders and service reports"
        :features/settings "Settings"
        :features/settings-desc "Configure workspace settings and preferences"
        :features/teams "Teams"
        :features/teams-desc "Manage your service team members"
        :common/loading "Loading..."
        :common/save "Save"
        :common/cancel "Cancel"
        :welcome/ready-to-keep-running "Ready to keep elevators running smoothly today?"
        :welcome/ensure-safe-comfortable "Let's ensure every ride is safe and comfortable."
        :welcome/elevate-service "Time to elevate service excellence."
        :welcome/expertise-keeps-moving "Your expertise keeps people moving safely."
        :welcome/lift-standards "Ready to lift standards in elevator care?"
        :welcome/vertical-excellence "Another day of maintaining vertical excellence."
        :welcome/skilled-hands "Your skilled hands keep the city moving."
        :welcome/perfect-journey "Let's make every elevator journey perfect today."
        :welcome/safety-reliability "Safety and reliability start with you."
        :welcome/rise-to-challenges "Ready to rise to today's maintenance challenges?"
        :welcome/dedication-connects "Your dedication keeps towers connected."
        :welcome/smooth-rides "Time to ensure smooth rides for everyone today."
        :welcome-prefix/welcome-back "Welcome back"
        :welcome-prefix/hello "Hello"
        :welcome-prefix/good-morning "Good morning"
        :welcome-prefix/great-to-see "Great to see you"
        :welcome-prefix/welcome "Welcome"
        :welcome-prefix/good-to-see "Good to see you"}
   :hu {:header/logout "Kijelentkezés"
        :header/brand "ElevaThor"
        :dashboard/title "Irányítópult"
        :dashboard/welcome "Üdvözlünk újra!"
        :dashboard/access-denied "Hozzáférés megtagadva"
        :dashboard/access-denied-message "Sajnáljuk, nincs hozzáférése ehhez a munkaterülethez, vagy nem létezik. Kérjük, jelentkezzen be a megfelelő fiókkal."
        :dashboard/go-to-login "Bejelentkezéshez"
        :dashboard/workspace-id "Munkaterület azonosító"
        :features/material-templates "Anyagsablonok"
        :features/material-templates-desc "Szabványos anyagok és kellékek kezelése"
        :features/addresses "Címek"
        :features/addresses-desc "Munkaterület címeinek és helyszíneinek kezelése"
        :features/worksheets "Munkalapok"
        :features/worksheets-desc "Munkarendelések és szervizelési jelentések kezelése"
        :features/settings "Beállítások"
        :features/settings-desc "Munkaterület beállításainak és preferenciáinak konfigurálása"
        :features/teams "Csapatok"
        :features/teams-desc "Szervizelési csapat tagjai kezelése"
        :common/loading "Betöltés..."
        :common/save "Mentés"
        :common/cancel "Mégse"
        :welcome/ready-to-keep-running "Készen állsz arra, hogy ma gördülékenyen működjenek a liftek?"
        :welcome/ensure-safe-comfortable "Biztosítsuk, hogy minden út biztonságos és kényelmes legyen."
        :welcome/elevate-service "Ideje emelni a szolgáltatási kiválóságot."
        :welcome/expertise-keeps-moving "A szakértelmeddel az emberek biztonságosan közlekednek."
        :welcome/lift-standards "Készen állsz emelni a liftgondozás színvonalát?"
        :welcome/vertical-excellence "Újabb nap a függőleges kiválóság fenntartásához."
        :welcome/skilled-hands "Képzett kezeid mozgásban tartják a várost."
        :welcome/perfect-journey "Tegyük tökéletessé minden liftút ma."
        :welcome/safety-reliability "A biztonság és megbízhatóság veled kezdődik."
        :welcome/rise-to-challenges "Készen állsz a mai karbantartási kihívásokra?"
        :welcome/dedication-connects "Az elkötelezettséged összeköti a tornyokat."
        :welcome/smooth-rides "Ideje biztosítani mindenkinek a gördülékeny utazást ma."
        :welcome-prefix/welcome-back "Üdvözlünk újra"
        :welcome-prefix/hello "Helló"
        :welcome-prefix/good-morning "Jó reggelt"
        :welcome-prefix/great-to-see "Szuper látni téged"
        :welcome-prefix/welcome "Üdvözöljük"
        :welcome-prefix/good-to-see "Jó látni téged"}})

;; Re-frame events and subscriptions for translations
(rf/reg-event-db
 :translations/initialize
 (fn [db _]
   (assoc db :translations translations)))

; Use existing header language subscription instead of duplicate

; Simple direct subscription
(rf/reg-sub
 :translate
 (fn [db [_ key]]
   (let [lang (get-in db [:header :language] :en)
         translations (:translations db)]
     (or (get-in translations [lang key])
         (get-in translations [:en key])
         (name key)))))

;; Helper function for translation
(defn tr
  "Get translation for key in current language"
  [key]
  @(rf/subscribe [:translate key]))

;; Initialize translations
(rf/dispatch [:translations/initialize])