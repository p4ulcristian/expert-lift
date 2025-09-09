(ns features.labs.looks.backend.utils
  (:require
    [clojure.string :as str]

    [zero.backend.state.file-storage :as file-storage])
  (:import [java.util Base64]))

(defn base64-to-png-and-upload
  "Converts a base64 string to a PNG file and uploads it to MinIO.
   Returns a map with the upload result or nil if there's an error.
   
   Args:
   - base64-str: The base64 encoded image string (without data:image/png;base64, prefix)
   - bucket: The MinIO bucket name
   - file-name: The name to give the file in MinIO
   
   Returns:
   - {:bucket bucket :file-name file-name :uploaded true} on success
   - nil on failure"
  [base64-str bucket file-name]
  (try
    (let [_             (when-not (str/ends-with? file-name ".png")
                          (throw (ex-info "File name must end with .png" {:file-name file-name})))
          
          clean-base64  (if-let [match (re-find #"^data:image/[^;]+;base64,(.+)$" base64-str)]
                          (second match)  ; Get the base64 data after the prefix
                          base64-str)
          
          decoded-bytes (.decode (Base64/getDecoder) clean-base64)
          input-stream  (java.io.ByteArrayInputStream. decoded-bytes)

          result        (file-storage/put-object
                          {:bucket       bucket
                           :file-name    file-name
                           :input-stream input-stream
                           :content-type "image/png"})]
      (.close input-stream)
      result)
    (catch Exception e
      (println "Error converting base64 to PNG and uploading:" (ex-message e))
      nil)))
