(ns features.app.pdf-generator.routes
  #?(:clj (:require [features.app.pdf-generator.backend.handlers :as handlers]
                    [app.backend.favicons :as favicons]
                    [hiccup.page :refer [html5]]
                    [router.backend.middleware :refer [wrap-require-authentication]])))

#?(:clj
   (defn- pdf-viewer-page
     "HTML page that embeds PDF in iframe with favicon"
     [worksheet-id]
     (html5
      [:head
       [:meta {:charset "UTF-8"}]
       [:title "ElevaThor - Munkalap"]
       (favicons/favicons)
       [:style
        "* { margin: 0; padding: 0; box-sizing: border-box; }
         html, body { height: 100%; width: 100%; overflow: hidden; }
         iframe { width: 100%; height: 100%; border: none; }"]]
      [:body
       [:iframe {:src (str "/pdf-generator/worksheet/" worksheet-id "/raw")}]])))

(def routes
  #?(:clj [{:path "/pdf-generator/worksheet/:worksheet-id"
            :get (fn [{{:keys [worksheet-id]} :path-params :as request}]
                   {:status 200
                    :headers {"Content-Type" "text/html"}
                    :body (pdf-viewer-page worksheet-id)})
            :middleware [wrap-require-authentication]}

           {:path "/pdf-generator/worksheet/:worksheet-id/raw"
            :get (fn [{{:keys [worksheet-id]} :path-params
                      {:keys [workspace-id]} :session
                      :as request}]
                   (println "DEBUG: PDF route hit - worksheet-id:" worksheet-id "workspace-id:" workspace-id)
                   (try
                     (let [pdf-bytes (handlers/generate-worksheet-pdf
                                       {:parquery/context {:workspace-id workspace-id}
                                        :parquery/request request
                                        :worksheet/id worksheet-id})]
                       (println "DEBUG: PDF generated successfully")
                       {:status 200
                        :headers {"Content-Type" "application/pdf"
                                  "Content-Disposition" "inline; filename=worksheet.pdf"}
                        :body (java.io.ByteArrayInputStream. pdf-bytes)})
                     (catch Exception e
                       (println "ERROR: PDF generation failed:" (.getMessage e))
                       (.printStackTrace e)
                       {:status 500
                        :headers {"Content-Type" "text/plain"}
                        :body (str "Error generating PDF: " (.getMessage e))})))
            :middleware [wrap-require-authentication]}]
     :cljs []))