(ns zero.backend.utils.responses)

(def content-types
  {:json "application/json"
   :html "text/html"
   :text "text/plain"})

(defn get-content-type [type]
  (get content-types type))

(defn response
  [type response-fn]
  (fn [req]
    {:headers {"Content-Type" (get-content-type type)}
     :status 200
     :body (response-fn req)}))

(defn text [response-fn]
  (response :text response-fn))

(defn html [response-fn]
  (response :html response-fn))

(defn json [response-fn]
  (response :json response-fn))