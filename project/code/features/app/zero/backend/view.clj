(ns features.app.zero.backend.view
  (:require
   [hiccup.page :refer [html5 include-js]]
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