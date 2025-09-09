
(ns features.labs.looks.backend.draft.resolvers
  (:require
    [com.wsscode.pathom3.connect.operation :as pco]
    [features.labs.looks.backend.draft.db :as db]))

;; -----------------------------------------------------------------------------
;; ---- Get Look Draft ----

(defn get-look-draft-fn [env]
  (let [params (-> env pco/params)]
    (try
      (db/get-look-draft
                       {:id (:id params)})
      (catch Exception e
        (println "Error getting look draft:" e)))))

(pco/defresolver get-look-draft-r [env _]
  {:looks.draft/get! (get-look-draft-fn env)})

;; ---- Get Look Draft ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- List Look Drafts ----

(defn list-look-drafts-fn [env]
  (let [params (-> env pco/params)]
    (try
      (db/list-look-drafts
                       {:search (:search params)})
      (catch Exception e
        (println "Error listing look drafts:" e)
        nil))))

;; (count (list-look-drafts-fn {:search nil}))

(pco/defresolver list-looks-drafts-r [env _]
  {:looks.draft/list! (list-look-drafts-fn env)})

;; ---- List Look Drafts ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Get Look Drafts Suggestions ----

(defn get-look-drafts-suggestions-fn [env]
  (let [params (-> env pco/params)]
    (try
      (db/get-look-drafts-suggestions
                       {:search (:search params)})
      (catch Exception e
        (println "Error getting look drafts suggestions:" e)
        nil))))

;; (count (get-look-drafts-suggestions-fn {:search nil}))

(pco/defresolver get-look-drafts-suggestions-r [env _]
  {:looks.draft/get-suggestions! (get-look-drafts-suggestions-fn env)})

;; ---- Get Look Drafts Suggestions ----
;; -----------------------------------------------------------------------------


(def resolvers [get-look-draft-r
                list-looks-drafts-r get-look-drafts-suggestions-r])
