
(ns app.frontend.request
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [zero.frontend.re-frame :as r]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]))

(defn get-csrf-token []
  (let [csrf-el    (.getElementById js/document "csrf-token")
        csrf-token (.getAttribute csrf-el "data-csrf-token")]
    csrf-token))

(def csrf-token (get-csrf-token))

(defn get-body [response]
  (let [status (:status response)
        body   (:body response)
        error? (contains? body :error)]
    (if (= status 200)
      (if error?
        (js/console.error "Error:" body)
        body)
      (js/console.error "Error:" status))))

(defn pathom [{:keys [query callback initial-data]}]
  (go
    (let [response (<! (http/post "/query"
                                  (merge
                                   {:headers         {"X-CSRF-Token" csrf-token}
                                    :transit-params  {:query query
                                                      :initial-data (or initial-data {})}})))
          body (get-body response)]
      (callback body))))

(r/reg-fx :pathom/request
  (fn [pathom-props]
    (pathom pathom-props)))

(r/reg-event-fx :pathom/request!
  (fn [_ [_ pathom-props]]
    {:pathom/request pathom-props}))

(defn pathom-with-workspace-id
  [{:keys [query callback initial-data]}]
  (let [workspace-id (r/subscribe [:workspace/get-id])]
    (go
      (let [response (<! (http/post "/query"
                                    (merge
                                     {:headers         {"X-CSRF-Token" csrf-token}
                                      :transit-params  {:query query
                                                        :initial-data 
                                                        (merge 
                                                         {:workspace/id @workspace-id}
                                                         (or initial-data {}))}})))
            body (get-body response)]
        (callback body)))))

(r/reg-fx :pathom/ws-request
  (fn [pathom-props]
    (pathom-with-workspace-id pathom-props)))

(r/reg-event-fx :pathom/ws-request!
  (fn [_ [_ pathom-props]]
    {:pathom/ws-request pathom-props}))

;; -----------------------------------------------------------------------------
;; ---- Pathom Query With Formdata ----

(defn pathom-formdata [{:keys [query files]}]
  (let [form-data    (js/FormData.)
        workspace-id (r/subscribe [:workspace/get-id])]
    (doall
      (map-indexed (fn [index file]
                     (.append form-data index file))
                   files))
    (.append form-data "query" {:query        query
                                :workspace-id @workspace-id})
    form-data))

(defn pathom-with-file [{:keys [callback] :as props}]
  (let [formdata (pathom-formdata props)]
    (go
      (callback
        (:body (<! (http/post "/fquery"
                              {:headers {"X-CSRF-Token" csrf-token}
                               :body    formdata})))))))

(r/reg-fx :pathom/formdata
          (fn [pathom-props]
            (pathom-with-file pathom-props)))

(r/reg-event-fx :pathom/frequest!
                (fn [_ [_ pathom-props]]
                  {:pathom/formdata pathom-props}))

;; ---- Pathom Query With Formdata ----
;; -----------------------------------------------------------------------------
