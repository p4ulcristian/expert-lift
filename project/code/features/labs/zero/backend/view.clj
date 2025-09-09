
(ns features.labs.zero.backend.view
  (:require 
   [hiccup.page :refer [html5 include-css include-js]]
   [app.backend.favicons :as favicons]
   [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]))


(defn loading []
  [:div {:style "height: 100vh; width: 100vw; display: flex; justify-content: center; align-items: center;"}
   [:div 
    [:img {:src "/logo/logo-good-size.png" :style "width: 50vw; max-width: 500px; min-width: 100px;"}]]])
   

(defn font-awesome-include []
  [:script {:type "text/javascript"
            :crossorigin "anonymous"
            :src "/external-js/fontawesome.js"}])

(defn app-html-page
  "Generates the HTML page for the app."
  []
  (html5
    [:head
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]

      [:title "Labs"]
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
      (font-awesome-include) 
      (include-js "/js/libs/labs.js")
      (include-js "/js/core/labs.js")]))

(defn response [_req]
  {:status  200
   :body    (app-html-page)
   :headers {"Content-Type" "text/html"}})