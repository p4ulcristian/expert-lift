(ns authentication.helpers
  (:require [hiccup.page :refer [html5]]))


(defn redirect-page [redirect-url]
  (html5
   [:head
    (when redirect-url
      [:meta {:http-equiv "refresh"
              :content (str "0; URL=" redirect-url)}])]
   [:body
    {:style "background-color: #333; 
                         color: #ccc;
                         font-family: Arial, sans-serif; 
                         text-align: center; 
                         padding: 50px;"}
    [:h1 "Redirecting..."]
    [:p "Please wait while we redirect you."]
    [:code redirect-url]]))


(defn redirect-handler [redirect-url]
  {:status  200
   :headers {"Content-type" "text/html"}
   :body    (redirect-page redirect-url)})



