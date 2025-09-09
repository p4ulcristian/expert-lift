(ns schemas.session
  (:require [schemas.spec :as spec]))

(def Session
  [:map
   [:user-id {:optional true} string?]
   [:user-roles {:optional true} [:vector string?]]
   [:token   {:optional true} map?]
   [:redirect-url {:optional true} string?]])


(defn validate-session [data]
  (spec/validate Session data))



