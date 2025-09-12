(ns features.app.pdf-generator.routes)

(def routes
  ["/pdf-generator"
   {:name :app/pdf-generator}
   
   ["/work-report" 
    {:name :app/pdf-generator-work-report}]])