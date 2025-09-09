
(ns features.labs.looks.backend.mutations
  (:require
    [com.wsscode.pathom3.connect.operation :as pco]
    [features.labs.looks.backend.db :as db]
    [features.labs.looks.backend.utils :as utils]))

;; -----------------------------------------------------------------------------
;; ---- Create Look ----

(defn save-look-fn [_ {:keys [price-group-key thumbnail tags texture-props name basecolor added-by color-family layers layers-count] :as _mut-props}]
  (let [look-id   (java.util.UUID/randomUUID)
        thumbnail (utils/base64-to-png-and-upload thumbnail "looks" (str look-id ".png"))]
    (try
      (db/create-look
        {:id              look-id
         :price-group-key price-group-key
         :thumbnail       (:url thumbnail)
         :tags            (vec tags)
         :texture         texture-props
         :name            name
         :basecolor       basecolor
         :added-by        added-by
         :color-family    color-family
         :layers          layers
         :layers-count    layers-count})
      
      (catch Exception e
        (println "Error creating look:" e)
        (ex-data e)))))

;; (save-look-fn nil {:look-id (java.util.UUID/randomUUID) :price-group-key "test" :thumbnail "test" :tags ["test"] :texture-props {"id" "test"} :name "test1" :basecolor "test" :added-by "test" :color-family "test" :layers [{:name "másik"}] :layers-count 1})

(pco/defmutation create-look-mutation [env mutation-props]
  {::pco/op-name 'looks/create!}
  (save-look-fn env mutation-props))

;; ---- Create Look ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Update Look ----

(defn update-look-fn [_ {:keys [id price-group-key thumbnail tags texture-props name basecolor added-by color-family layers layers-count] :as _mut-props}]
  (let [thumbnail (utils/base64-to-png-and-upload thumbnail "looks" (str id ".png"))]
    (try
      (db/update-look
        {:id              id
         :price-group-key price-group-key
         :thumbnail       (:url thumbnail)
         :tags            (vec tags)
         :texture         texture-props
         :name            name
         :basecolor       basecolor
         :added-by        added-by
         :color-family    color-family
         :layers          layers
         :layers-count    layers-count})
      (catch Exception e
        (println "Error updating look:" e)
        e))))

;; (update-look-fn nil {:id #uuid"b9044c99-f7ec-4812-ba7d-d8eae2f45977" :name "hola" :color-family "black" :price-group-key "pro"})

(pco/defmutation update-look-mutation [env mutation-props]
  {::pco/op-name 'looks/update!}
  (update-look-fn env mutation-props))

;; ---- Update Look ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Delete Look ----

(defn delete-look-fn [_ {:keys [id] :as _mut-props}]
  
  (try
    (println "deleting look" id)
    (db/delete-look {:id id})
    (catch Exception e
      (println "Error deleting look:" e)
      nil)))

;; (delete-look-fn nil {:id "73ce97cc-5384-434f-b409-6bbe6b234ff0"})

(pco/defmutation delete-look-mutation [env mutation-props]
  {::pco/op-name 'looks/delete!}
  (delete-look-fn env mutation-props))

;; ---- Delete Look ----
;; -----------------------------------------------------------------------------

(def mutations [create-look-mutation update-look-mutation delete-look-mutation])