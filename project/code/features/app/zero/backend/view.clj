(ns features.app.zero.backend.view
  (:require
   [hiccup.page :refer [html5 include-js include-css]]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(defn loading []
  [:div {:style "height: 100vh; width: 100vw; display: flex; flex-direction: column; justify-content: center; align-items: center; background: #f8f9fa; gap: 20px;"}
   [:div {:class "loading-brand-container"}
    [:img {:class "loading-logo" :src "/logo/logo.png" :alt "Logo"}] 
    [:div {:class "loading-brand"} "ElevaThor"]]
   [:div {:class "loading-spinner"}]])

(defn font-awesome-include []
  [:script {:type "text/javascript"
            :crossorigin "anonymous"
            :src "/external-js/fontawesome.js"}])

(defn app-html-page
  "Generates the HTML page for the app."
  []
  (html5
   [:head
    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin true}]
    [:link {:href "https://fonts.googleapis.com/css2?family=Skranji:wght@400;700&display=swap" :rel "stylesheet"}]
    (include-css "/css/normalize.css")
    (include-css "/css/ui.css")
    (include-css "/css/app.css")]
   [:body
    (let [csrf-token (force *anti-forgery-token*)]
      [:div#csrf-token {:data-csrf-token csrf-token}])
    [:div#reagent-container (loading)]
    (font-awesome-include)
    (include-js "/js/libs/app.js")
    (include-js "/js/core/app.js")]))

(defn response [_req]
  {:status 200
   :body   (app-html-page)
   :headers {"Content-Type" "text/html"}})