(ns ui.header
  (:require
   [ui.button :as button]))

(defn language-toggle
  "Language toggle between English and Hungarian"
  [{:keys [current-language on-change]}]
  [:div.language-toggle
   [button/view 
    {:class "language-toggle-btn"
     :on-click #(when on-change (on-change (if (= current-language "en") "hu" "en")))}
    (if (= current-language "en") "EN → HU" "HU → EN")]])

(defn header
  "Main application header with logo, language toggle, and logout button"
  [{:keys [current-language on-language-change on-logout]}]
  [:header.app-header
   [:div.header-content
    [:div.header-left
     [:img.logo {:src "/logo/logo.png" :alt "Logo"}]
     [:span.brand-name "ElevaThor"]]
    [:div.header-right
     [language-toggle {:current-language current-language
                       :on-change on-language-change}]
     [button/view 
      {:type :secondary
       :on-click on-logout
       :class "logout-btn"}
      "Logout"]]]])

(defn view
  "Header component view function"
  [props]
  [header props])