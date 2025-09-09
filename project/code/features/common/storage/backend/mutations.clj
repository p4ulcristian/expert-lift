(ns features.common.storage.backend.mutations
  (:require
   [clojure.java.io :as io]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.entity-tree       :as pe]
   [zero.backend.state.env                :as env]
   [zero.backend.state.file-storage       :as file-storage]
   [zero.backend.state.postgres           :as postgres]
   [features.common.storage.backend.utils :as storage.utils]
   [features.common.storage.backend.db    :as db]))

;; SQL functions are now in db.clj

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn create-directory-entry [alias user-id workspace-id is-superadmin]
  {:id            (java.util.UUID/randomUUID)
   :alias         alias
   :type          "directory"
   :path          []
   :items         []
   :is-superadmin is-superadmin
   :added-by      user-id
   :added-at      (java.time.Instant/now)
   :workspace-id  workspace-id})

(defn create-file-entry [id alias mime-type size url user-id workspace-id is-superadmin]
  {:id            id
   :alias         alias
   :type          "file"
   :mime-type     mime-type
   :size          size
   :url           url
   :is-superadmin is-superadmin
   :path          []
   :added-by      user-id
   :added-at      (java.time.Instant/now)
   :workspace-id  workspace-id})

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Create Directory ----

(defn create-directory-to-dir-f [destination-id directory-entry]
  (try
    (db/create-directory-to-dir (:id directory-entry)
                                 (:alias directory-entry)
                                 (:type directory-entry)
                                 (:added-by directory-entry)
                                 (:workspace-id directory-entry)
                                 (:is-superadmin directory-entry)
                                 destination-id)
    (catch Exception e
      (println "Storage insert to dir error" e)
      (ex-data e))))

(defn create-directory-to-root-f [directory-entry]
  (try 
    (db/create-directory-to-root (:id directory-entry)
                                  (:alias directory-entry)
                                  (:type directory-entry)
                                  (:added-by directory-entry)
                                  (:workspace-id directory-entry)
                                  (:is-superadmin directory-entry))
    (catch Exception e
      (println "Storage insert to root error" (ex-data e))
      (ex-data e))))


(defn create-directory-f [_env {:keys [destination-id alias] :as _mutation-props}]
  (let [session       (get-in _env [:request :session])
        user-id       (get-in session [:user-id])
        workspace-id  (-> _env pe/entity :workspace/id)
        user-roles    (get-in session [:user-roles])
        is-superadmin (storage.utils/is-superadmin? user-roles workspace-id)]
    
    (println "Creating directory" session is-superadmin)
    (storage.utils/convert-instances
      (let [directory-entry (create-directory-entry alias user-id workspace-id is-superadmin)]
        (if destination-id
          (create-directory-to-dir-f destination-id directory-entry)
          (create-directory-to-root-f directory-entry))))))

(pco/defmutation create-directory!
  [env mutation-props]
  {::pco/op-name 'storage/create-directory!}
  (create-directory-f env mutation-props))

;; ---- Create Directory ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Delete Item ----

;; (file-storage/remove-object {:bucket    "ironrainbow"
;;                              :file-name (str (:id file) "." (storage.utils/file->extenstion file))})

(defn delete-item-f [_env {:keys [item-id] :as _mutation-props}]
  (let [session       (get-in _env [:request :session])
        user-roles    (get-in session [:user-roles])
        workspace-id  (-> _env pe/entity :workspace/id)
        is-superadmin (storage.utils/is-superadmin? user-roles workspace-id)
        bucket        (storage.utils/get-bucket-name user-roles is-superadmin)]
    (try
      (let [files (filter #(= (:type %) "file")
                          (db/delete-item item-id workspace-id is-superadmin))]
        (println "Deleting files" files)
        (for [file files]
          (file-storage/remove-object 
            {:bucket    bucket
             :file-name (str (:id file) "." (storage.utils/file->extenstion file))})))
               
      (catch Exception e
        (println "Storage delete item error" (ex-data e))
        (ex-data e)))))

(pco/defmutation delete-item!
  [env mutation-props]
  {::pco/op-name 'storage/delete-item!}
  (delete-item-f env mutation-props))

;; ---- Delete Item ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Upload file ----

(defn upload-file-to-bucket [bucket minio-name file-data mime-type]
  (try 
    (println "Uploading file to bucket" bucket minio-name (-> file-data :tempfile str) mime-type)
    (file-storage/upload-object {:bucket       bucket
                                 :file-name    minio-name
                                 :file-path    (-> file-data :tempfile str)
                                 :content-type mime-type})
    (catch Exception e
      (println "Storage file upload to bucket error" (ex-data e))
      (ex-data e))))

(defn file-insert-to-dir-f [destination-id file-entry]
  (try
    (db/file-insert-to-dir (:id file-entry)
                            (:alias file-entry)
                            (:type file-entry)
                            (:mime-type file-entry)
                            (:size file-entry)
                            (:url file-entry)
                            (:workspace-id file-entry)
                            (:added-by file-entry)
                            (:is-superadmin file-entry)
                            destination-id)
    (catch Exception e
      (println "Storage file insert to dir error" (ex-data e))
      (ex-data e))))

(defn file-insert-to-root-f [file-entry]
  (try
    (db/file-insert-to-root (:id file-entry)
                             (:alias file-entry)
                             (:type file-entry)
                             (:mime-type file-entry)
                             (:size file-entry)
                             (:url file-entry)
                             (:workspace-id file-entry)
                             (:added-by file-entry)
                             (:is-superadmin file-entry))                  
    (catch Exception e
      (println "Storage file insert to root error" (ex-data e))
      (ex-data e))))


(defn upload-files-f [env {:keys [destination-id] :as _mutation-props}]
  (let [files         (-> env
                          (get-in [:request :multipart-params])
                          (dissoc "query"))
        user-id       (get-in env [:request :session :user-id])
        user-roles    (get-in env [:request :session :user-roles])
        workspace-id  (-> env pe/entity :workspace/id)
        is-superadmin (storage.utils/is-superadmin? user-roles workspace-id)
        bucket        (storage.utils/get-bucket-name user-roles is-superadmin)]

    (println "Uploading files to bucket" workspace-id bucket)

    (when bucket
      (storage.utils/convert-instances
       (for [[_ file-data] files]
         (let [id         (java.util.UUID/randomUUID)
               size       (:size file-data)
               alias      (:filename file-data)
               mime-type  (storage.utils/mime-by-temp file-data) 
               extenstion (storage.utils/temp->extenstion file-data)
               minio-name (str id "." extenstion)
               url        (str @env/minio-url "/" bucket "/" minio-name)]

           (upload-file-to-bucket bucket minio-name file-data mime-type)

           (let [file-entry (create-file-entry id alias mime-type size url user-id workspace-id is-superadmin)]
             (if destination-id
               (file-insert-to-dir-f destination-id file-entry)
               (file-insert-to-root-f file-entry)))))))))

(pco/defmutation upload-files! [env mutation-props]
  {::pco/op-name 'storage/upload-files!}
  (upload-files-f env mutation-props))

;; ---- Upload file ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Rename Item ----

(defn rename-item-f [_env {:keys [item-id new-alias] :as _mutation-props}]
  (let [session       (get-in _env [:request :session])
        user-roles    (get-in session [:user-roles])
        workspace-id  (-> _env pe/entity :workspace/id)
        is-superadmin (storage.utils/is-superadmin? user-roles workspace-id)]

    (storage.utils/convert-instances
      (db/rename-item new-alias item-id workspace-id is-superadmin))))

(pco/defmutation rename-item!
  [env mutation-props]
  {::pco/op-name 'storage/rename-item!}
  (rename-item-f env mutation-props))

;; ---- Rename Item ----
;; -----------------------------------------------------------------------------

(def mutations [create-directory! upload-files! delete-item! rename-item!])
