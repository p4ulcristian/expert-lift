(ns ui.header
  (:require
   [ui.button :as button]
   [zero.frontend.re-frame :as rf]
   [parquery.frontend.request :as parquery]))

;; Re-frame events and subscriptions
(rf/reg-event-db
 :header/set-language
 (fn [db [language]]
   (println "DEBUG: re-frame event :header/set-language called with:" language)
   (let [updated-db (assoc-in db [:header :language] language)]
     (println "DEBUG: Updated db header section:" (get-in updated-db [:header]))
     updated-db)))

(rf/reg-sub
 :header/current-language
 (fn [db _]
   (let [current-lang (get-in db [:header :language] "en")]
     (println "DEBUG: subscription :header/current-language returning:" current-lang)
     current-lang)))

;; Initialize default language
(rf/dispatch [:header/set-language "en"])

;; Event handlers
(defn handle-logout []
  "Handle user logout"
  (parquery/send-queries
    {:queries {:users/logout {}}
     :callback (fn [response]
                 (:success (:users/logout response)))}))

(defn handle-language-toggle [current-language]
  "Toggle language between en and hu"
  (let [new-language (if (= current-language "en") "hu" "en")]
    (println "DEBUG: Toggling language from" current-language "to" new-language)
    (rf/dispatch [:header/set-language new-language])
    (println "DEBUG: Dispatched event")))

(defn language-toggle
  "Language toggle between English and Hungarian"
  []
  (let [current-language (rf/subscribe [:header/current-language])]
    [:div.language-toggle
     [button/view 
      {:class "language-toggle-btn"
       :on-click #(handle-language-toggle @current-language)}
      (if (= @current-language "en") "EN" "HU")]]))

(defn header
  "Main application header with logo, language toggle, and logout button"
  []
  [:header.app-header
   [:div.header-content
    [:div.header-left
     [:img.logo {:src "/logo/logo.png" :alt "Logo"}]
     [:span.brand-name "ElevaThor"]]
    [:div.header-right
     [language-toggle]
     [button/view 
      {:type :secondary
       :on-click handle-logout
       :class "logout-btn"}
      "Logout"]]]])

(defn view
  "Header component view function"
  []
  [header])