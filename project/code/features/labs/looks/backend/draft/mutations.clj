
(ns features.labs.looks.backend.draft.mutations
  (:require 
    [com.wsscode.pathom3.connect.operation :as pco]
    [features.labs.looks.backend.draft.db :as db]))

;; -----------------------------------------------------------------------------
;; ---- Create Look Draft ----

(defn create-look-draft-fn [_ {:keys [name price-group-key tags texture basecolor color-family layers layers-count] :as _mut-props}]
  (try 
    (db/create-look-draft
      {:name            name
       :price-group-key price-group-key
       :tags            (if (seq tags) (vec tags) nil)
       :texture         (or texture {})
       :basecolor       basecolor
       :color-family    color-family
       :layers          (if (seq layers) (vec layers) nil)
       :layers-count    (or layers-count 0)})
    
    (catch Exception e
      (println "Error saving look draft:" e)
      (ex-data e))))

;; (create-look-draft-fn nil {:name "test1" :price-group-key "basic" :tags ["test"] :texture {} :basecolor "#ffffff" :color-family "red" :layers [] :layers-count 0})

(pco/defmutation create-look-draft-mutation [env mutation-props]
  {::pco/op-name 'looks.draft/create!}
  (create-look-draft-fn env mutation-props))

;; ---- Create Look Draft ----
;; -----------------------------------------------------------------------------

(def mutations [create-look-draft-mutation])