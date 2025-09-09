(ns parquery.backend.view
  (:require [hiccup.page :refer [html5 include-css include-js]]
            [app.backend.favicons :as favicons]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))

(defn loading []
  [:div {:style "height: 100vh; width: 100vw; display: flex; justify-content: center; align-items: center;"}
   [:h1 "Parquery Loading..."]])

(defn app-html-page
  "Generates the HTML page for parquery"
  []
  (html5
    [:head
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title "Parquery"]
     (favicons/favicons)
     (include-css "/css/normalize.css")
     (include-css "/fonts/montserrat.css")
     (include-css "/css/app.css")
     (include-css "/css/ui.css")
     (include-css "/css/labs.css")]
    [:body
     (let [csrf-token (force *anti-forgery-token*)]
       [:div#csrf-token {:data-csrf-token csrf-token}])
     [:div#reagent-container (loading)]
     (include-js "/js/libs/labs.js")
     (include-js "/js/core/labs.js")]))

(defn render-html
  "Renders the HTML view for parquery"
  [_request]
  {:status  200
   :body    (app-html-page)
   :headers {"Content-Type" "text/html"}})