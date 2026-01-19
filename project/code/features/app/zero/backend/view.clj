(ns features.app.zero.backend.view
  (:require
   [hiccup.page :refer [html5]]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(def asset-version
  "Version string for cache busting, set at server startup time"
  (str (System/currentTimeMillis)))

(defn versioned-css
  "Include CSS with cache-busting version query parameter"
  [path]
  [:link {:rel "stylesheet" :href (str path "?v=" asset-version)}])

(defn versioned-js
  "Include JS with cache-busting version query parameter"
  [path]
  [:script {:type "text/javascript" :src (str path "?v=" asset-version)}])

(defn loading []
  [:div {:style "height: 100vh; width: 100vw; display: flex; flex-direction: column; justify-content: center; align-items: center; background: #f8f9fa; gap: 20px;"}
   [:div {:class "loading-brand-container"}
    [:img {:class "loading-logo" :src "/logo/logo-256.webp" :alt "Logo"}] 
    [:div {:class "loading-brand"} "ElevaThor"]]
   [:div {:class "loading-spinner"}]])

(defn app-html-page
  "Generates the HTML page for the app."
  []
  (html5
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"}]
    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin true}]
    [:link {:href "https://fonts.googleapis.com/css2?family=Skranji:wght@400;700&display=swap" :rel "stylesheet"}]
    (versioned-css "/css/normalize.css")
    (versioned-css "/css/ui.css")
    (versioned-css "/css/app.css")]
   [:body
    (let [csrf-token (force *anti-forgery-token*)]
      [:div#csrf-token {:data-csrf-token csrf-token}])
    [:div#reagent-container (loading)]
    (versioned-js "/external-js/fontawesome.js")
    (versioned-js "/js/libs/app.js")
    (versioned-js "/js/core/app.js")]))

(defn response [_req]
  {:status 200
   :body   (app-html-page)
   :headers {"Content-Type" "text/html"}})