(ns zero.backend.tools.reveal
  (:require
   [portal.api :as p]
   [zero.backend.state.env :as env]))

(defn value
  "Reveal a value in the portal"
  ([_value]
   (value "Reveal window" _value))

  ([title _value]
   (when @env/dev?  ; Add portal as a tap> target  
     (p/inspect _value
                {:launcher     :vs-code
                 :window-title title}))))