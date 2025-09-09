(ns ui.text-field
  (:require
   ["react" :as react]
   [re-frame.core :as r]))

(def DEFAULT-TIMEOUT 500)

;; -----------------------------------------------------------------------------
;; ---- Utils ----

(defn on-enter-handler [event on-enter]
  (when (= "Enter" (.-key ^js event))
    (on-enter (-> ^js event .-target .-value))))

(defn on-type-ended-handler [[timer set-timer] on-type-ended value]
  (when timer (js/clearTimeout timer)) ;; Clear previous timeout
  (set-timer
   (js/setTimeout
    (fn [] (if (fn? on-type-ended) (on-type-ended value) ((:fn on-type-ended))))
    (or (:ms on-type-ended) DEFAULT-TIMEOUT))))

(defn validate-input [rules value]
  (let [errors (keep #(when ((:test %) value) (:msg %)) rules)] ;; Collect all failed messages
    errors))

(defn safe-seq [coll]
  (if (or (nil? coll) (and (not (string? coll)) (not (seqable? coll))))
    []
    (seq coll)))

(defn safe-empty? [coll]
  (if (or (nil? coll) (and (not (string? coll)) (not (seqable? coll))))
    true
    (empty? coll)))

(defn type-class [{:keys [type]}]
  (case type
    :primary   "input-primary"
    :secondary "input-secondary"
    :warning   "input-warning"
    :success   "input-success"
    ""))

;; ---- Utils ----
;; -----------------------------------------------------------------------------

;; -----------------------------------------------------------------------------
;; ---- Hooks ----

(defn mount-hook [{:keys [on-mount un-mount default-value]}]
  (react/useEffect
   (fn []
     (when default-value (r/dispatch [:x.db/set-item! (first default-value) (last default-value)]))
     (when on-mount (on-mount))
     (fn []
       (when un-mount (un-mount))))
   #js[]))

(defn rules-check-hook [[timer set-timer] set-errors {:keys [rules rules-timeout value]}]
  (react/useEffect
   (fn []
     (when-not (safe-empty? value)
       (when timer (js/clearTimeout timer)) ;; Clear previous timeout
       (set-timer
        (js/setTimeout
         (fn []
           (when rules
             (set-errors (validate-input rules value))))
         (or rules-timeout 800))))
     (fn []))
   #js[value]))

;; ---- Hooks ----
;; -----------------------------------------------------------------------------
(defn input [{:keys [id label style disabled placeholder value on-click on-change on-focus on-blur on-type-ended on-enter rules override required] :as input-props}]
  (let [timer               (react/useState nil)
        [errors set-errors] (react/useState nil)
        error-timer         (react/useState nil)
        have-rules?         (and rules (not disabled))
        errors              (if (and (not required) (empty? value))
                              nil
                              errors)]
    
    (mount-hook input-props)
    (rules-check-hook error-timer set-errors input-props)

    [:div {:style {:position "relative"}}
      (when label [:p {:class "input--text-field--label"} 
                    label 
                    (when required [:span {:style {:color "red"}} " *"])])

      [:div {:class         ["input--text-field" (type-class input-props)]
             :data-warning  (when errors (boolean (safe-seq errors)))
             :data-success  (when errors (safe-empty? errors))
             :data-disabled disabled
             :on-click      (fn [event]
                              (when on-click
                                (on-click event)))
             :style         style}

        (when-let [left-adornment (:left-adornment input-props)]
          left-adornment)

        [:input (merge {:id           id
                        :disabled     disabled
                        :type         "text"     
                        :placeholder  placeholder
                        :value        value
                        :on-focus     (fn [event]
                                        (let [value (-> event .-target .-value)]
                                          (when on-focus
                                            (on-focus value))))
                        :on-blur      (fn [event]
                                        (let [value (-> event .-target .-value)]
                                          (when on-blur
                                            (on-blur value))
                                          (when (and rules required)
                                            (set-errors (validate-input rules value)))))

                        :on-key-down  (fn [event]
                                        (when on-enter
                                          (on-enter-handler event on-enter)))

                        :on-change    (fn [event]
                                        (let [value (-> event .-target .-value)]
                                          (when on-change
                                            (on-change value))
                                          (when (and rules errors)
                                            (println label "setting errors" value)
                                            (set-errors (validate-input rules value)))
                                          (when on-type-ended
                                            (on-type-ended-handler timer on-type-ended value))))}

                       override)]

        (when-let [right-adornment (:right-adornment input-props)]
          right-adornment)]

      (when have-rules?
        [:div {:class "input--text-field--warning-msg"}
              
         (first errors)])]))

(defn view [input-props]
  [input input-props])