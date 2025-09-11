
(ns features.common.storage.backend.utils
  (:require
   [clojure.java.io :as io]
   [clojure.string  :as string]
   [clojure.walk    :as walk])
  (:import
   [java.nio.file Files]))

;; -----------------------------------------------------------------------------
;; -----------------------------------------------------------------------------

(def CONTENT-TYPES 
  {"image/png"     "png"
   "image/jpeg"    "jpg"
   "image/jpg"     "jpg"
   "image/gif"     "gif"
   "image/webp"    "webp"
   "image/svg+xml" "svg"
   "image/tiff"    "tiff"
   "image/bmp"     "bmp"
   "image/ico"     "ico"
   "image/heic"    "heic"
   "image/heif"    "heif"
   "image/avif"    "avif"
   ;; 3D Models
   "model/gltf+json"   "gltf"
   "model/gltf-binary" "glb"
   "model/obj"         "obj"
   "model/stl"         "stl"
   "model/fbx"         "fbx"
   "model/3ds"         "3ds"
   "model/dae"         "dae"
   "model/ply"         "ply"
   "model/wrl"         "wrl"
   "model/x3d"         "x3d"
   "model/usd"         "usd"
   "model/usdz"        "usdz"
   ;; Documents
   "text/plain"         "txt"
   "text/markdown"      "md"
   "text/csv"           "csv"
   "text/html"          "html"
   "text/css"           "css"
   "text/javascript"    "js"
   "text/xml"           "xml"
   "text/json"          "json"
   "text/yaml"          "yml"
   "application/pdf"    "pdf"
   "application/msword" "doc"

   "application/vnd.openxmlformats-officedocument.wordprocessingml.document" "docx"
   "application/vnd.ms-excel" "xls"
   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" "xlsx"
   "application/vnd.ms-powerpoint" "ppt"
   "application/vnd.openxmlformats-officedocument.presentationml.presentation" "pptx"
   "application/vnd.oasis.opendocument.text" "odt"
   "application/vnd.oasis.opendocument.spreadsheet" "ods"
   "application/vnd.oasis.opendocument.presentation" "odp"
   "application/rtf" "rtf"
   ;; Archives
   "application/zip"              "zip"
   "application/x-rar-compressed" "rar"
   "application/x-7z-compressed"  "7z"
   "application/x-tar"            "tar"
   "application/gzip"             "gz"
   "application/x-bzip2"          "bz2"
   ;; Audio
   "audio/mpeg" "mp3"
   "audio/wav"  "wav"
   "audio/ogg"  "ogg"
   "audio/flac" "flac"
   "audio/aac"  "aac"
   "audio/webm" "webm"
   ;; Video
   "video/mp4"  "mp4"
   "video/webm" "webm"
   "video/ogg"  "ogv"
   "video/avi"  "avi"
   "video/mov"  "mov"
   "video/wmv"  "wmv"
   "video/flv"  "flv"
   "video/mkv"  "mkv"
   ;; CAD/Design Files
   "application/dxf"           "dxf"
   "application/dwg"           "dwg"
   "application/step"          "step"
   "application/iges"          "igs"
   "application/x-3ds"         "3ds"
   "application/x-blend"       "blend"
   "application/x-maya"        "ma"
   "application/x-maya-binary" "mb"
   "application/x-max"         "max"
   "application/x-c4d"         "c4d"
   ;; Fonts
   "font/ttf"   "ttf"
   "font/otf"   "otf"
   "font/woff"  "woff"
   "font/woff2" "woff2"
   "font/eot"   "eot"
   ;; Other
   "application/octet-stream"    "bin"
   "application/x-executable"    "exe"
   "application/x-dmg"           "dmg"
   "application/x-iso9660-image" "iso"})

;; -----------------------------------------------------------------------------
;; -----------------------------------------------------------------------------

(defn get-mime-type [file]
  (try
    (if-let [mime (Files/probeContentType (.toPath file))]
      mime
      "application/octet-stream")
    (catch Exception e
      (ex-message e))))

(defn mime-by-temp [temp-file]
  (if-let [mime (get temp-file :content-type)]
    mime
    (get-mime-type (io/file (:tempfile temp-file)))))

(defn temp->extenstion [temp-file]
  (-> (:filename temp-file)
      (clojure.string/split #"\.")
      last))

(defn file->extenstion [file-entry]
  (-> (:url file-entry)
      (clojure.string/split #"\.")
      last))

(defn convert-instances [item]
  (walk/postwalk
    (fn [x]
      (cond
        ;; Convert UUIDs to strings
        (instance? java.util.UUID x) (str x)

        ;; Convert timestamps to ISO-8601 strings
        (or (instance? java.time.OffsetDateTime x)
            (instance? java.time.LocalDateTime x)) (str x)

        ;; Convert PostgreSQL Array to a Clojure vector
        (instance? org.postgresql.jdbc.PgArray x) (vec (.getArray x))

        :else x))
    item))

(defn is-superadmin? [user-roles workspace-id]
  (when-not workspace-id
    (boolean (some #(= "labs" %) user-roles))))

(defn is-coating-partner? [user-roles]
  (some #(= "coating_partner" %) user-roles))

(defn get-bucket-name [user-roles is-superadmin]
  (println "is-superadmin" is-superadmin)
  (cond
    is-superadmin "expertlift"              ;; Superadmin has access to all buckets
    (is-coating-partner? user-roles) "demo" ;; Coating partners have access to the demo bucket, other roles have no access
    :else false))

