(ns features.app.settings.backend.handlers
  "HTTP handlers for settings file uploads"
  (:require [ring.util.response :as response]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [workspaces.backend.db :as workspace-db]))

(defn upload-logo
  "Handle workspace logo upload and settings update via multipart form"
  [request]
  (println "DEBUG: upload-logo handler called with request keys:" (keys request))
  (try
    (let [workspace-id (get-in request [:path-params :workspace-id])
          multipart-params (:multipart-params request)
          file-param (get multipart-params "file")
          workspace-name (get multipart-params "workspaceName")]
      
      (println "DEBUG: upload-logo called")
      (println "DEBUG: workspace-id:" workspace-id)
      (println "DEBUG: multipart-params keys:" (keys multipart-params))
      (println "DEBUG: workspace-name:" workspace-name)
      (println "DEBUG: file-param:" file-param)
      
      (when workspace-id
        (let [results (atom {:success true})]
          
          ;; Update workspace name if provided
          (when (and workspace-name (not (str/blank? workspace-name)))
            (try
              (let [current-workspace (first (workspace-db/get-workspace-by-id (java.util.UUID/fromString workspace-id)))]
                (when current-workspace
                  (workspace-db/update-workspace 
                   (java.util.UUID/fromString workspace-id)
                   workspace-name
                   (:description current-workspace)
                   (:active current-workspace))
                  (swap! results assoc :workspace-name-updated true)
                  (println "Workspace name updated to:" workspace-name)))
              (catch Exception e
                (println "Error updating workspace name:" (.getMessage e))
                (swap! results assoc :workspace-name-error (.getMessage e)))))
          
          ;; Upload file if provided
          (when file-param
            (try
              (let [temp-file (:tempfile file-param)
                    original-filename (:filename file-param)
                    file-extension (when original-filename
                                     (last (str/split original-filename #"\.")))
                    new-filename (str workspace-id "." (str/lower-case file-extension))
                    upload-base-dir (or (System/getenv "EXPERT_LIFT_UPLOAD_DIR") "uploads")
                    upload-dir (io/file upload-base-dir "logos")
                    target-file (io/file upload-dir new-filename)]
                
                ;; Create directory if it doesn't exist
                (.mkdirs upload-dir)
                
                ;; Copy uploaded file to target location
                (io/copy temp-file target-file)
                
                (swap! results assoc 
                       :file-uploaded true
                       :filename new-filename
                       :path (.getPath target-file))
                (println "Logo uploaded successfully:" (.getPath target-file)))
              (catch Exception e
                (println "Error uploading file:" (.getMessage e))
                (swap! results assoc :file-error (.getMessage e)))))
          
          (-> (response/response (json/write-str @results))
              (response/content-type "application/json")))))
    
    (catch Exception e
      (println "Error in upload-logo handler:" (.getMessage e))
      (-> (response/response (json/write-str {:success false
                                               :error (.getMessage e)}))
          (response/status 500)
          (response/content-type "application/json")))))

(defn serve-logo
  "Serve uploaded logo files"
  [request]
  (try
    (let [filename (get-in request [:path-params :filename])
          upload-base-dir (or (System/getenv "EXPERT_LIFT_UPLOAD_DIR") "uploads")
          file-path (str upload-base-dir "/logos/" filename)
          file (io/file file-path)]
      (if (.exists file)
        (-> (response/response file)
            (response/content-type (cond
                                    (str/ends-with? filename ".jpg") "image/jpeg"
                                    (str/ends-with? filename ".jpeg") "image/jpeg"
                                    (str/ends-with? filename ".png") "image/png"
                                    (str/ends-with? filename ".gif") "image/gif"
                                    :else "application/octet-stream")))
        (-> (response/response "File not found")
            (response/status 404))))
    (catch Exception e
      (println "Error serving logo:" (.getMessage e))
      (-> (response/response "Internal server error")
          (response/status 500)))))