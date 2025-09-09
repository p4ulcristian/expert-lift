(ns zero.backend.state.file-storage
  (:require
   [zero.backend.state.env :as env]) 
  
  (:import
   [io.minio
    BucketExistsArgs
    GetObjectArgs
    ListObjectsArgs
    MakeBucketArgs
    MinioClient
    PutObjectArgs
    RemoveBucketArgs
    RemoveObjectArgs
    SetBucketPolicyArgs
    UploadObjectArgs]))

;; -----------------------------------------------------------------------------
;; ---- Configurations ----

(def minio-client
  (delay
    (->
     (MinioClient/builder)
     (.endpoint @env/minio-endpoint)
     (.credentials @env/minio-access-key @env/minio-secret-key)
     (.build))))

(defn public-bucket-policy-template [bucket-name]
  (format "{
    \"Version\": \"2012-10-17\",
    \"Statement\": [
      {
        \"Effect\": \"Allow\",
        \"Principal\": {\"AWS\": [\"*\"]},
        \"Action\": [\"s3:GetObject\"],
        \"Resource\": [
          \"arn:aws:s3:::%s/*\",
          \"arn:aws:s3:::%s\"
        ]
      }
    ]
  }" bucket-name bucket-name))
            
        
    


;; ---- Configurations ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn url [bucket file-name]
  (str @env/minio-url "/" bucket "/" file-name))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Bucket Operations ----

;; ---- Bucket Existence ----

(defn bucket-exists? [bucket-name]
  (try
    (.bucketExists @minio-client
                   (-> (BucketExistsArgs/builder)
                       (.bucket bucket-name)
                       (.build)))
    (catch Exception e
      (println "Error checking bucket existence:" (ex-message e))
      nil)))

;; ---- Bucket Existence ----

;; ---- Set Bucket Public ----

(defn set-bucket-public! [bucket-name]
  (try
    (let [policy-json (public-bucket-policy-template bucket-name)]
      (.setBucketPolicy @minio-client
                       (-> (SetBucketPolicyArgs/builder)
                           (.bucket bucket-name)
                           (.config policy-json)
                           (.build)))
      {:bucket bucket-name
       :public true})
    (catch Exception e
      (println "Error setting bucket public:" (ex-message e))
      nil)))

;; ---- Set Bucket Public ----

;; ---- Create Bucket ----

(defn create-bucket! [bucket-name]
  (try
    (.makeBucket @minio-client
                 (-> (MakeBucketArgs/builder)
                     (.bucket bucket-name)
                     (.objectLock false)
                     (.build)))
    (set-bucket-public! bucket-name)
    {:name    bucket-name
     :created true
     :public  true}
    (catch Exception e
      (println "Error creating bucket:" (ex-message e))
      nil)))

;; ---- Create Bucket ----

;; ---- Delete Bucket ----

(defn delete-bucket! [bucket-name]
  (try
    (.removeBucket @minio-client
                   (-> (RemoveBucketArgs/builder)
                       (.bucket bucket-name)
                       (.build)))
    {:name bucket-name
     :deleted true}
    (catch Exception e
      (println "Error deleting bucket:" (ex-message e))
      nil)))

;; ---- Delete Bucket ----

;; ---- Bucket Operations ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Object Operations ----

;; ---- Put Object ----

(defn put-object [{:keys [bucket file-name input-stream content-type]}]
  (try
    (let [size (.available input-stream)]  ; Get the available bytes as a long
      (.putObject @minio-client
                  (-> (PutObjectArgs/builder)
                      (.bucket bucket)
                      (.object file-name)
                      (.stream input-stream size -1)  ; size and -1 for partSize
                      (.contentType (or content-type "application/octet-stream"))
                      (.build))))
    {:bucket    bucket
     :file-name file-name
     :uploaded  true
     :url       (url bucket file-name)}
    (catch Exception e
      (println "Error putting object:" (ex-message e))
      nil)))

;; ---- Put Object ----

;; ---- Upload Object ----

(defn upload-object [{:keys [bucket file-name file-path content-type]}]
  (try
    (.uploadObject @minio-client
                   (-> (UploadObjectArgs/builder)
                       (.bucket bucket)
                       (.object file-name)
                       (.filename file-path)
                       (.contentType content-type)
                       (.build)))
    {:bucket    bucket
     :file-name file-name
     :uploaded  true
     :url       (url bucket file-name)}
    (catch Exception e
      (println "Error uploading object:" (ex-message e))
      nil)))

;; ---- Upload Object ----

;; ---- Get Object ----

(defn get-object [{:keys [bucket file-name]}]
  (try
    (let [build (-> (GetObjectArgs/builder)
                    (.bucket bucket)
                    (.object file-name)
                    (.build))]
      (.getObject @minio-client build))
    (catch Exception e
      (println "Error getting object:" (ex-message e))
      nil)))

;; ---- Get Object ----

;; ---- Remove Object ----

(defn remove-object [{:keys [bucket file-name]}]
  (try
    (.removeObject @minio-client
                   (-> (RemoveObjectArgs/builder)
                       (.bucket bucket)
                       (.object file-name)
                       (.build)))
    {:bucket bucket
     :file-name file-name
     :removed true}
    (catch Exception e
      (println "Error removing object:" (ex-message e))
      nil)))

;; ---- Remove Object ----

;; ---- List Objects ----

(defn list-objects [{:keys [bucket]}]
  (try
    (.listObjects @minio-client
                  (-> (ListObjectsArgs/builder)
                      (.bucket bucket)
                      (.build)))
    (catch Exception e
      (println "Error listing objects:" (ex-message e))
      nil)))

;; ---- List Objects ----

;; ---- Object Operations ----
;; -----------------------------------------------------------------------------