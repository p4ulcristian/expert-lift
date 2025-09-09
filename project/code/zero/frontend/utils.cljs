
(ns zero.frontend.utils)

(def TIMEOUTS (atom {}))

(defn set-timeout! [timeout-id {:keys [timeout fn] :as timeout-props}]
  (let [js-id         (js/setTimeout fn timeout)
        timeout-props (assoc timeout-props :js-id js-id)]
    (swap! TIMEOUTS assoc timeout-id timeout-props)))

(defn clear-timeout! [timeout-id]
  (let [js-id (get-in @TIMEOUTS [timeout-id :js-id])]
    (when js-id
      (js/clearTimeout js-id)
      (swap! TIMEOUTS dissoc timeout-id))))