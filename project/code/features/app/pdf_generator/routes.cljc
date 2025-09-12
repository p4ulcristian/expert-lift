(ns features.app.pdf-generator.routes
  #?(:clj (:require [features.app.pdf-generator.backend.handlers :as handlers]
                    [router.backend.middleware :refer [wrap-require-authentication]])))

(def routes
  #?(:clj [{:path "/pdf-generator/worksheet/:worksheet-id"
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