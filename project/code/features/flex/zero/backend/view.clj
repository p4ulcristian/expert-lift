(ns features.flex.zero.backend.view
  (:require
   [app.backend.favicons :as favicons]
   [hiccup.page :refer [html5 include-css]]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(defn loading []
  [:div {:style "height: 100vh; width: 100vw; display: flex; justify-content: center; align-items: center;"}
   [:div
    [:img {:src "/logo/logo-good-size.png" :style "width: 50vw; max-width: 500px; min-width: 100px;"}]]])

(defn font-awesome-include []
  [:script {:type "text/javascript"
            :crossorigin "anonymous"
            :src "/external-js/fontawesome.js"}])

(defn maptiler-sdk-include []
  [:script {:type "text/javascript"
            :src "https://cdn.maptiler.com/maptiler-sdk-js/v2.2.2/maptiler-sdk.umd.min.js"}])

(defn maptiler-sdk-css-include []
  [:link {:type "text/css"
          :href "https://cdn.maptiler.com/maptiler-sdk-js/v2.2.2/maptiler-sdk.css"
          :rel "stylesheet"}])

(defn maptiler-geocoding-control-include []
  [:link {:type "text/css"
          :href "https://cdn.maptiler.com/maptiler-geocoding-control/v1.3.3/style.css"
          :rel "stylesheet"}])

(defn vanilla-calendar-css-include []
  [:link {:type "text/css"
          :href "/css/vanilla-calendar.css"
          :rel "stylesheet"}])
 
(defn maptiler-geocoding-control-js-include []
  [:script {:type "text/javascript"
            :src "https://cdn.maptiler.com/maptiler-geocoding-control/v1.3.3/maptilersdk.umd.js"}])

(def project-relative-path-start "project/resources/public/")


(defn relative-path->absolute-path [path]
  (str project-relative-path-start path))


(defn checksum [path]
  (let [md5-path (str path ".md5")]
    (try
      (slurp md5-path)
      (catch Exception _
        nil))))


(defn url-with-version [url]
  (if-let [version (checksum (relative-path->absolute-path url))]
    (str url "?mdfive=" version)
    url))

(defn include-js-with-version [path]
  (let [url (url-with-version path)] 
    [:script {:type "text/javascript"
              :src url}]))

(defn app-html-page
  "Generates the HTML page for the app."
  []
  (html5
   [:head
    [:title "Flex"]
    (favicons/favicons)
    (include-css "/css/normalize.css")
    (include-css "/fonts/montserrat.css")
    (include-css "/css/app.css")
    (include-css "/css/ui.css")
    (include-css "/css/flex.css")
    (maptiler-sdk-css-include)
    (maptiler-geocoding-control-include)
    (vanilla-calendar-css-include)]
   [:body
    (let [csrf-token (force *anti-forgery-token*)]
      [:div#csrf-token {:data-csrf-token csrf-token}])
    [:div#reagent-container (loading)]
    (font-awesome-include)
    (include-js-with-version "/js/libs/flex.js")
    (include-js-with-version "/js/core/flex.js")
    (maptiler-sdk-include)
    (maptiler-geocoding-control-js-include)]))

(defn response [_req]
  {:status 200
   :body   (app-html-page)
   :headers {"Content-Type" "text/html"}})