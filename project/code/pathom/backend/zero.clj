(ns pathom.backend.zero
  (:require
   [cheshire.core :as json]
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]))

(def pathom-handlers (atom []))

(defn set-handlers! [handlers]
  (reset! pathom-handlers handlers))

(defn process-pathom-query-with-request [request initial-data query]
  (let [env (pci/register  (vec @pathom-handlers))
        env-with-request (assoc env :request request)]
    (try
      (p.eql/process env-with-request initial-data query)
      (catch AssertionError e
        (println "💥 Assertion failed:" (.getMessage e))
        {:error (.getMessage e)})
      (catch Exception e
        (println "Error: " (.getMessage e))
        {:error (.getMessage e)}))))

(defn handle-transit-query! [request]
  ;; There is a better way
  ;; We could use application/edn for communicating
  (let [params  (:transit-params request)
        query   (:query params)
        initial-data (:initial-data params)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/generate-string
                (process-pathom-query-with-request
                  request initial-data query))}))

;; -----------------------------------------------------------------------------
;; ---- Pathom Process With Formdata ----

(defn request->multipart-params->query [req]
  (-> req
      (get-in [:multipart-params "query"])
      read-string))

(defn handle-formdata-query! [request]
  ;; There is a better way
  ;; We could use application/edn for communicating
  (let [query-props  (request->multipart-params->query request)
        query        (:query query-props)
        workspace-id (:workspace-id query-props)]
    {:status  200
     :headers {"Content-Type" "text/plain"}
     :body    (str (process-pathom-query-with-request 
                     request {:workspace/id workspace-id} query))}))

;; ---- Pathom Process With Formdata ----
;; -----------------------------------------------------------------------------

(def routes
  ;;Every pathom request happens through this route
  [{:path "/query"
    :post handle-transit-query!}
   {:path "/fquery"
    :post handle-formdata-query!}])

