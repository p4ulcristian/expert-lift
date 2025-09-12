(ns features.app.pdf-generator.routes
  #?(:clj (:require [features.app.pdf-generator.backend.handlers :as handlers])))

(def routes
  ["/pdf-generator"
   {:name :app/pdf-generator}
   
   ["/work-report" 
    {:name :app/pdf-generator-work-report}]
    
   #?(:clj
      ["/worksheet/:worksheet-id"
       {:name :app/pdf-generator-worksheet
        :get (fn [{{:keys [worksheet-id]} :path-params
                  {:keys [workspace-id]} :session
                  :as request}]
               (try
                 (let [pdf-bytes (handlers/generate-worksheet-pdf 
                                   {:parquery/context {:workspace-id workspace-id}
                                    :parquery/request request
                                    :worksheet/id worksheet-id})]
                   {:status 200
                    :headers {"Content-Type" "application/pdf"
                              "Content-Disposition" "inline; filename=worksheet.pdf"}
                    :body (java.io.ByteArrayInputStream. pdf-bytes)})
                 (catch Exception e
                   {:status 500
                    :headers {"Content-Type" "text/plain"}
                    :body (str "Error generating PDF: " (.getMessage e))})))}])])