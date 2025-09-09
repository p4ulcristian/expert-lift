(ns authentication.utils)

(defn get-global-role 
  "Extract the global_role parameter from request"
  ([req] 
   (get-global-role req "customizer"))
  ([req default-role]
   (get-in req [:params :as] default-role)))
