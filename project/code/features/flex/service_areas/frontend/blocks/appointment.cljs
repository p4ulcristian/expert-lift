(ns features.flex.service-areas.frontend.blocks.appointment
  (:require
    ["react" :as react]
    ["vanilla-calendar-pro" :refer [Calendar]]
    [re-frame.core :as r]))

(defn two-weeks-later
  "Returns date string for 14 days from now"
  []
  (.substring (.toISOString (js/Date. (+ (js/Date.now) (* 14 24 60 60 1000)))) 0 10))

(defn today
  "Returns today's date string"
  []
  (.substring (.toISOString (js/Date.)) 0 10))

(defn format-date [date-str]
  (let [date-obj (js/Date. date-str)]
    (.toLocaleDateString date-obj "en-US" #js{:weekday "long" :year "numeric" :month "long" :day "numeric"})))

(defn dispatch-appointment [date time]
  (r/dispatch [:db/assoc-in [:ui :service-areas :selected-appointment] 
               {:date date :time time}]))

(defn handle-date-click [self]
  (let [context (.-context self)
        selected-dates (.-selectedDates context)
        selected-time (.-selectedTime self)]
    (when (and selected-dates (> (.-length selected-dates) 0))
      (let [date-str (aget selected-dates 0)
            formatted-date (format-date date-str)
            time-str (or selected-time "Time not selected yet")]
        (dispatch-appointment formatted-date time-str)))))

(defn get-selected-time [self]
  (let [context (.-context self)
        time-self (.-selectedTime self)
        time-context (.-selectedTime context)]
    (or time-context time-self)))

(defn handle-time-change [self event is-error]
  (when-not is-error
    (let [context (.-context self)
          selected-dates (.-selectedDates context)
          selected-time (get-selected-time self)]
      (when (and selected-dates (> (.-length selected-dates) 0) selected-time)
        (let [date-str (aget selected-dates 0)
              formatted-date (format-date date-str)]
          (dispatch-appointment formatted-date selected-time))))))

(defn create-calendar-config []
  #js{:type "default"
      :selectionTimeMode 12
      :timeStepMinute 15
      :dateMin (today)
      :dateMax (two-weeks-later)
      :onClickDate handle-date-click
      :onChangeTime handle-time-change})

(defn initialize-calendar [calendar-ref calendar-instance]
  (when (.-current calendar-ref)
    (let [config (create-calendar-config)
          calendar (Calendar. (.-current calendar-ref) config)]
      (set! (.-current calendar-instance) calendar)
      (.init calendar))))

(defn cleanup-calendar [calendar-instance]
  (when (.-current calendar-instance)
    (.destroy (.-current calendar-instance))))

(defn calendar-component []
  (let [calendar-ref (react/useRef nil)
        calendar-instance (react/useRef nil)]
    
    (react/useEffect
      (fn []
        (initialize-calendar calendar-ref calendar-instance)
        (fn [] (cleanup-calendar calendar-instance)))
      #js[])
    
    [:div {:style {:background "rgba(255, 255, 255, 0.95)"
                   :backdrop-filter "blur(8px)"
                   :border-radius "12px"
                   :box-shadow "0 4px 20px rgba(0, 0, 0, 0.1)"}}
     [:div {:ref calendar-ref
            :style {:max-width "100%"
                    :margin "0 auto"}}]]))