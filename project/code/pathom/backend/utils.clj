(ns pathom.backend.utils)


(defn get-entity-from-mutation [env k]
  (get-in env 
          [:com.wsscode.pathom3.connect.runner/source-entity k]))